package com.example.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.local.AppNotificationEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.local.DoubtQueueEntity
import com.example.data.local.EducatorProfileEntity
import com.example.data.local.HomeworkEntity
import com.example.data.local.LessonEntity
import com.example.data.local.PortalAccountEntity
import com.example.data.local.QuizAttemptEntity
import com.example.data.local.QuizQuestionEntity
import com.example.data.local.SessionStore
import com.example.data.local.StudentProfileEntity
import com.example.data.remote.FirestoreLiveSession
import com.example.data.repository.DocumentVerificationResult
import com.example.data.repository.GeminiOnlineMode
import com.example.data.repository.HybridAiService
import com.example.data.repository.OfflineSyllabusEngine
import com.example.data.repository.TwogetherRepository
import com.example.util.NetworkMonitor
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

enum class AppRoleMode {
    PROFILE_HUB,
    STUDENT,
    EDUCATOR,
    ADMIN
}

sealed interface DocumentVerificationUiState {
    data object Idle : DocumentVerificationUiState
    data class Verifying(val modelName: String) : DocumentVerificationUiState
    data class Verified(val result: DocumentVerificationResult) : DocumentVerificationUiState
    data class Failed(val message: String) : DocumentVerificationUiState
}

/**
 * Deterministic sequential progression helper per Subject path:
 * - Lesson 1 in each subject is unlocked by default.
 * - Lesson N+1 unlocks only after the student watches Lesson N's video AND passes its practice quiz.
 * - Educators and Admins always have unrestricted preview access.
 */
object LessonProgressionTracker {
    fun isWatched(lesson: LessonEntity, profileId: String): Boolean =
        lesson.watchedByProfileIds.split(",").any { it.trim() == profileId }

    fun isQuizPassed(lesson: LessonEntity, profileId: String): Boolean =
        lesson.completedByProfileIds.split(",").any { it.trim() == profileId }

    fun computeUnlockMap(
        lessons: List<LessonEntity>,
        studentProfileId: String?,
        bypassLockForStaff: Boolean = false
    ): Map<String, Boolean> {
        if (bypassLockForStaff || studentProfileId.isNullOrBlank()) {
            return lessons.associate { it.id to true }
        }
        val result = mutableMapOf<String, Boolean>()
        val groupedBySubject = lessons.groupBy { it.subject.trim().lowercase() }
        groupedBySubject.values.forEach { subjectLessons ->
            val educatorUploads = subjectLessons.filter { it.id.startsWith("lesson_edu_") }
            educatorUploads.forEach { result[it.id] = true }

            val coreLessons = subjectLessons
                .filterNot { it.id.startsWith("lesson_edu_") }
                .sortedWith(compareBy<LessonEntity> { it.gradeClass }.thenBy { it.id })
            var previousCompleted = true
            coreLessons.forEachIndexed { index, lesson ->
                val unlocked = index == 0 || previousCompleted
                result[lesson.id] = unlocked
                previousCompleted = unlocked &&
                    isWatched(lesson, studentProfileId) &&
                    isQuizPassed(lesson, studentProfileId)
            }
        }
        return result
    }

    fun findNextUnlockedLessonTitle(
        lessons: List<LessonEntity>,
        completedLesson: LessonEntity
    ): String? {
        val ordered = lessons
            .filter { it.subject.equals(completedLesson.subject, ignoreCase = true) }
            .sortedWith(compareBy<LessonEntity> { it.gradeClass }.thenBy { it.id })
        val idx = ordered.indexOfFirst { it.id == completedLesson.id }
        return if (idx >= 0 && idx + 1 < ordered.size) ordered[idx + 1].title else null
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class TwogetherViewModel(
    private val repository: TwogetherRepository,
    private val aiService: HybridAiService,
    private val networkMonitor: NetworkMonitor,
    val currentUserId: String,
    private val sessionStore: SessionStore? = null
) : ViewModel() {

    private val _roleMode = MutableStateFlow(AppRoleMode.PROFILE_HUB)
    val roleMode: StateFlow<AppRoleMode> = _roleMode.asStateFlow()

    private fun str(@StringRes resId: Int, vararg args: Any): String =
        repository.localizedContext().getString(resId, *args)

    private fun localizedSubject(subject: String): String {
        val ctx = repository.localizedContext()
        return when (subject.lowercase()) {
            "math", "maths" -> ctx.getString(R.string.subject_math)
            "science" -> ctx.getString(R.string.subject_science)
            "english" -> ctx.getString(R.string.subject_english)
            "social studies" -> ctx.getString(R.string.subject_social_studies)
            "hindi" -> ctx.getString(R.string.subject_hindi)
            else -> subject
        }
    }

    private val _activeStudentId = MutableStateFlow<String?>(null)
    private val _activeEducatorId = MutableStateFlow<String?>(null)
    private val _simulateOffline = MutableStateFlow(false)
    val simulateOffline: StateFlow<Boolean> = _simulateOffline.asStateFlow()

    private val _authPortalError = MutableStateFlow<String?>(null)
    val authPortalError: StateFlow<String?> = _authPortalError.asStateFlow()

    val effectiveIsOnline: StateFlow<Boolean> = combine(
        networkMonitor.isOnline,
        _simulateOffline
    ) { realOnline, simOffline -> realOnline && !simOffline }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), true)

    val portalAccounts: StateFlow<List<PortalAccountEntity>> =
        repository.observePortalAccounts(currentUserId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val studentProfiles: StateFlow<List<StudentProfileEntity>> =
        repository.observeStudentProfiles(currentUserId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val activeStudent: StateFlow<StudentProfileEntity?> = combine(
        studentProfiles,
        _activeStudentId
    ) { list, id -> list.firstOrNull { it.id == id } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    val educators: StateFlow<List<EducatorProfileEntity>> =
        repository.observeAllEducators(currentUserId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val activeEducator: StateFlow<EducatorProfileEntity?> = combine(
        educators,
        _activeEducatorId
    ) { list, id ->
        if (id != null) list.firstOrNull { it.id == id } ?: list.firstOrNull()
        else list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    // Backward-compatible alias
    val educatorProfile: StateFlow<EducatorProfileEntity?> = activeEducator

    // --- Backpack State ---
    private val _showOnlyDownloaded = MutableStateFlow(false)
    val showOnlyDownloaded: StateFlow<Boolean> = _showOnlyDownloaded.asStateFlow()

    private val _backpackSearchQuery = MutableStateFlow("")
    val backpackSearchQuery: StateFlow<String> = _backpackSearchQuery.asStateFlow()

    private val _selectedSubject = MutableStateFlow("All")
    val selectedSubject: StateFlow<String> = _selectedSubject.asStateFlow()

    private val _selectedGradeFilter = MutableStateFlow<Int?>(null)
    val selectedGradeFilter: StateFlow<Int?> = _selectedGradeFilter.asStateFlow()

    private val _selectedLanguageFilter = MutableStateFlow("All")
    val selectedLanguageFilter: StateFlow<String> = _selectedLanguageFilter.asStateFlow()

    val allLessons: StateFlow<List<LessonEntity>> = repository.observeLessons()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val filteredLessons: StateFlow<List<LessonEntity>> = combine(
        allLessons,
        _showOnlyDownloaded,
        _backpackSearchQuery,
        _selectedSubject,
        _selectedGradeFilter,
        _selectedLanguageFilter
    ) { arr ->
        @Suppress("UNCHECKED_CAST")
        val lessons = arr[0] as List<LessonEntity>
        val onlyDownloaded = arr[1] as Boolean
        val query = arr[2] as String
        val subject = arr[3] as String
        val grade = arr[4] as Int?
        val language = arr[5] as String

        val q = query.trim().lowercase()
        lessons.filter { lesson ->
            (!onlyDownloaded || lesson.downloaded) &&
                (subject == "All" || lesson.subject.equals(subject, ignoreCase = true)) &&
                (grade == null || lesson.gradeClass == grade) &&
                (language == "All" ||
                    lesson.language.equals(language, ignoreCase = true) ||
                    lesson.language.equals("Bilingual", ignoreCase = true)) &&
                (q.isBlank() ||
                    lesson.title.lowercase().contains(q) ||
                    lesson.subject.lowercase().contains(q) ||
                    lesson.summary.lowercase().contains(q) ||
                    lesson.studyNotes.lowercase().contains(q))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val homeworks: StateFlow<List<HomeworkEntity>> = repository.observeHomeworks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val quizAttempts: StateFlow<List<QuizAttemptEntity>> = _activeStudentId
        .flatMapLatest { id ->
            if (id.isNullOrBlank()) flowOf(emptyList())
            else repository.observeQuizAttempts(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val lessonUnlockMap: StateFlow<Map<String, Boolean>> = combine(
        allLessons,
        _activeStudentId,
        _roleMode
    ) { lessons, studentId, role ->
        LessonProgressionTracker.computeUnlockMap(
            lessons = lessons,
            studentProfileId = studentId,
            bypassLockForStaff = role == AppRoleMode.EDUCATOR || role == AppRoleMode.ADMIN
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyMap())

    private val _activeQuizLesson = MutableStateFlow<LessonEntity?>(null)
    val activeQuizLesson: StateFlow<LessonEntity?> = _activeQuizLesson.asStateFlow()

    private val _activeQuizQuestions = MutableStateFlow<List<QuizQuestionEntity>>(emptyList())
    val activeQuizQuestions: StateFlow<List<QuizQuestionEntity>> = _activeQuizQuestions.asStateFlow()

    private val _confettiTrigger = MutableStateFlow(0)
    val confettiTrigger: StateFlow<Int> = _confettiTrigger.asStateFlow()

    // --- Doubt Queue & Live Sessions ---
    val doubts: StateFlow<List<DoubtQueueEntity>> = repository.observeDoubts(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val liveSessionsState: StateFlow<UiState<List<FirestoreLiveSession>>> =
        repository.observeLiveSessions(currentUserId)
            .map<List<FirestoreLiveSession>, UiState<List<FirestoreLiveSession>>> {
                UiState.Success(it)
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing live sessions", rootError)
                emit(UiState.Error(rootError.message ?: "Failed to load live sessions"))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    private val _paperPlaneTrigger = MutableStateFlow(0)
    val paperPlaneTrigger: StateFlow<Int> = _paperPlaneTrigger.asStateFlow()

    // --- Smart AI Chatbox State (2 Modes: Offline FAQs vs Online Gemini) ---
    private val _preferOfflineAi = MutableStateFlow(true)
    val preferOfflineAi: StateFlow<Boolean> = _preferOfflineAi.asStateFlow()

    private val _onlineGeminiMode = MutableStateFlow(GeminiOnlineMode.BALANCED)
    val onlineGeminiMode: StateFlow<GeminiOnlineMode> = _onlineGeminiMode.asStateFlow()

    private val _geminiNanoAvailable = MutableStateFlow(false)
    val geminiNanoAvailable: StateFlow<Boolean> = _geminiNanoAvailable.asStateFlow()

    private val _isAiGenerating = MutableStateFlow(false)
    val isAiGenerating: StateFlow<Boolean> = _isAiGenerating.asStateFlow()

    private val _streamingText = MutableStateFlow("")
    val streamingText: StateFlow<String> = _streamingText.asStateFlow()

    private val _streamingEngine = MutableStateFlow("")
    val streamingEngine: StateFlow<String> = _streamingEngine.asStateFlow()

    private val _aiError = MutableStateFlow<String?>(null)
    val aiError: StateFlow<String?> = _aiError.asStateFlow()

    val chatMessages: StateFlow<List<ChatMessageEntity>> = _activeStudentId
        .flatMapLatest { id ->
            if (id.isNullOrBlank()) flowOf(emptyList())
            else repository.observeChatMessages(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    // --- Document Verification AI State ---
    private val _docVerificationState =
        MutableStateFlow<DocumentVerificationUiState>(DocumentVerificationUiState.Idle)
    val docVerificationState: StateFlow<DocumentVerificationUiState> =
        _docVerificationState.asStateFlow()

    // --- Notifications & Pending Homework Reminders ---
    val notifications: StateFlow<List<AppNotificationEntity>> =
        repository.observeNotifications()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val _activeBannerNotification = MutableStateFlow<AppNotificationEntity?>(null)
    val activeBannerNotification: StateFlow<AppNotificationEntity?> =
        _activeBannerNotification.asStateFlow()

    private val _bannerMessage = MutableStateFlow<String?>(null)
    val bannerMessage: StateFlow<String?> = _bannerMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureLocalSeedData(currentUserId)
            _geminiNanoAvailable.value = aiService.isGeminiNanoAvailable()
        }
        viewModelScope.launch {
            effectiveIsOnline.collect { online ->
                if (online) {
                    repository.syncPendingDoubtsNow(currentUserId)
                }
            }
        }
    }

    private var initialLaunchApplied = false

    fun applyInitialLaunch(launchRole: AppRoleMode, entityId: String?) {
        if (initialLaunchApplied) return
        initialLaunchApplied = true
        when (launchRole) {
            AppRoleMode.ADMIN -> {
                _roleMode.value = AppRoleMode.ADMIN
            }
            AppRoleMode.EDUCATOR -> {
                if (!entityId.isNullOrBlank()) {
                    _activeEducatorId.value = entityId
                }
                _docVerificationState.value = DocumentVerificationUiState.Idle
                _roleMode.value = AppRoleMode.EDUCATOR
            }
            AppRoleMode.STUDENT -> {
                if (!entityId.isNullOrBlank()) {
                    viewModelScope.launch {
                        val profile = studentProfiles.value.firstOrNull { it.id == entityId }
                            ?: repository.getStudentProfileById(entityId)
                        if (profile != null) {
                            unlockStudentProfile(profile)
                        }
                    }
                }
            }
            AppRoleMode.PROFILE_HUB -> Unit
        }
    }

    fun clearBanner() {
        _bannerMessage.value = null
    }

    fun showBanner(msg: String) {
        _bannerMessage.value = msg
    }

    fun clearAuthPortalError() {
        _authPortalError.value = null
    }

    // --- 3-Role Email & Password Portal Login + Shared Device PIN ---

    fun loginWithEmail(expectedRole: String, email: String, password: String) {
        _authPortalError.value = null
        viewModelScope.launch {
            val res = repository.authenticatePortalAccount(
                uid = currentUserId,
                expectedRole = expectedRole,
                rawEmail = email,
                rawPassword = password
            )
            res.onSuccess { account ->
                when (account.role) {
                    "ADMIN" -> {
                        _roleMode.value = AppRoleMode.ADMIN
                        persistActiveSession("ADMIN", account.linkedEntityId, account.email, account.displayName)
                        _bannerMessage.value = str(R.string.vm_admin_unlocked, account.email)
                    }
                    "STUDENT" -> {
                        val profile = studentProfiles.value.firstOrNull { it.id == account.linkedEntityId }
                            ?: repository.getStudentProfileById(account.linkedEntityId)
                        if (profile != null) {
                            unlockStudentProfile(profile)
                        } else {
                            _authPortalError.value = str(R.string.vm_student_not_found, account.email)
                        }
                    }
                    "EDUCATOR" -> {
                        _activeEducatorId.value = account.linkedEntityId
                        _docVerificationState.value = DocumentVerificationUiState.Idle
                        _roleMode.value = AppRoleMode.EDUCATOR
                        persistActiveSession("EDUCATOR", account.linkedEntityId, account.email, account.displayName)
                        _bannerMessage.value = str(R.string.vm_welcome_educator, account.displayName)
                    }
                }
            }.onFailure { err ->
                _authPortalError.value = err.localizedMessage ?: str(R.string.vm_invalid_credentials)
            }
        }
    }

    private fun persistActiveSession(
        roleKey: String,
        entityId: String,
        email: String = "",
        name: String = ""
    ) {
        viewModelScope.launch {
            sessionStore?.saveSession(
                workspaceUid = currentUserId,
                roleMode = roleKey,
                entityId = entityId,
                userEmail = email,
                userName = name
            )
        }
    }

    fun clearPersistedSession() {
        viewModelScope.launch {
            sessionStore?.clearSession()
        }
    }

    fun resetPortalPassword(
        roleKey: String,
        email: String,
        newPassword: String,
        onResult: (Result<String>) -> Unit
    ) {
        viewModelScope.launch {
            val res = repository.resetPortalPassword(
                expectedRole = roleKey,
                rawEmail = email,
                newPassword = newPassword,
                isOnline = effectiveIsOnline.value
            )
            res.onSuccess { msg -> _bannerMessage.value = msg }
            onResult(res)
        }
    }

    fun registerStudentAccount(
        name: String,
        email: String,
        password: String,
        gradeClass: Int,
        avatarId: String,
        themeColorKey: String,
        preferredLanguage: String,
        parentalConsentGiven: Boolean,
        centerName: String,
        pinSequence: String
    ) {
        _authPortalError.value = null
        viewModelScope.launch {
            val cleanEmail = email.trim()
            if (cleanEmail.isNotBlank()) {
                if (!cleanEmail.contains("@")) {
                    _authPortalError.value = str(R.string.auth_err_valid_email)
                    return@launch
                }
                if (password.length < 6) {
                    _authPortalError.value = str(R.string.auth_err_password_len)
                    return@launch
                }
            }
            if (!parentalConsentGiven) {
                _authPortalError.value = str(R.string.auth_err_consent)
                return@launch
            }
            val res = repository.createStudentProfile(
                uid = currentUserId,
                name = name,
                email = cleanEmail,
                password = password,
                gradeClass = gradeClass,
                avatarId = avatarId,
                themeColorKey = themeColorKey,
                preferredLanguage = preferredLanguage,
                parentalConsentGiven = parentalConsentGiven,
                centerName = centerName,
                pinSequence = pinSequence
            )
            res.onSuccess { created ->
                unlockStudentProfile(created)
                _bannerMessage.value = str(R.string.vm_welcome_student, created.name)
            }.onFailure { err ->
                _authPortalError.value = err.localizedMessage ?: str(R.string.vm_student_reg_failed)
            }
        }
    }

    fun registerEducatorAccount(
        fullName: String,
        email: String,
        password: String,
        tier: String,
        subjectSpecialty: String
    ) {
        _authPortalError.value = null
        viewModelScope.launch {
            val res = repository.registerEducatorWithEmail(
                uid = currentUserId,
                fullName = fullName,
                email = email,
                password = password,
                tier = tier,
                subjectSpecialty = subjectSpecialty
            )
            res.onSuccess { created ->
                _activeEducatorId.value = created.id
                _docVerificationState.value = DocumentVerificationUiState.Idle
                _roleMode.value = AppRoleMode.EDUCATOR
                persistActiveSession("EDUCATOR", created.id, created.email, created.fullName)
                _bannerMessage.value = str(R.string.vm_educator_created)
            }.onFailure { err ->
                _authPortalError.value = err.localizedMessage ?: str(R.string.vm_educator_reg_failed)
            }
        }
    }

    fun registerAdminAccount(
        fullName: String,
        email: String,
        password: String
    ) {
        _authPortalError.value = null
        viewModelScope.launch {
            val res = repository.registerAdminAccount(
                uid = currentUserId,
                fullName = fullName,
                email = email,
                password = password
            )
            res.onSuccess { created ->
                _roleMode.value = AppRoleMode.ADMIN
                persistActiveSession("ADMIN", created.linkedEntityId, created.email, created.displayName)
                _bannerMessage.value = str(R.string.vm_admin_created, created.email)
            }.onFailure { err ->
                _authPortalError.value = err.localizedMessage ?: str(R.string.vm_admin_reg_failed)
            }
        }
    }

    fun unlockStudentProfile(profile: StudentProfileEntity) {
        _activeStudentId.value = profile.id
        _selectedGradeFilter.value = null
        _roleMode.value = AppRoleMode.STUDENT
        persistActiveSession("STUDENT", profile.id, profile.email, profile.name)
        checkAndShowPendingHomeworkBanner(profile)
    }

    private fun checkAndShowPendingHomeworkBanner(profile: StudentProfileEntity) {
        val pendingLesson = allLessons.value.firstOrNull { lesson ->
            val completedIds = lesson.completedByProfileIds.split(",").filter { it.isNotBlank() }
            !completedIds.contains(profile.id)
        }
        if (pendingLesson != null) {
            val subjectLabel = localizedSubject(pendingLesson.subject)
            val banner = AppNotificationEntity(
                id = "banner_${profile.id}_${pendingLesson.id}",
                studentProfileId = profile.id,
                title = str(R.string.notif_pending_homework_title),
                message = str(R.string.notif_pending_homework_msg_subject, subjectLabel),
                category = "homework",
                targetTab = "backpack",
                isRead = false
            )
            _activeBannerNotification.value = banner
        }
    }

    fun dismissBannerNotification() {
        _activeBannerNotification.value = null
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead()
        }
    }

    fun triggerHomeworkReminderNow() {
        val student = activeStudent.value
        val pendingLesson = allLessons.value.firstOrNull { lesson ->
            student == null || !lesson.completedByProfileIds.split(",").contains(student.id)
        }
        val subjectLabel = if (pendingLesson == null) {
            str(R.string.subject_math)
        } else {
            localizedSubject(pendingLesson.subject)
        }
        val message = str(R.string.notif_pending_homework_msg_subject, subjectLabel)
        viewModelScope.launch {
            val created = repository.createAndDispatchNotification(
                studentProfileId = student?.id ?: "all",
                title = str(R.string.notif_pending_homework_title),
                message = message,
                category = "homework",
                targetTab = "backpack"
            )
            _activeBannerNotification.value = created
        }
    }

    fun switchToProfileHub() {
        _authPortalError.value = null
        _roleMode.value = AppRoleMode.PROFILE_HUB
    }

    fun createStudentProfile(
        name: String,
        gradeClass: Int,
        avatarId: String,
        themeColorKey: String,
        pinSequence: String
    ) {
        registerStudentAccount(
            name = name,
            email = "",
            password = "",
            gradeClass = gradeClass,
            avatarId = avatarId,
            themeColorKey = themeColorKey,
            preferredLanguage = "Bilingual",
            parentalConsentGiven = true,
            centerName = "Asha Kiran NGO Center",
            pinSequence = pinSequence
        )
    }

    // --- Backpack Actions ---

    fun setShowOnlyDownloaded(onlyDownloaded: Boolean) {
        _showOnlyDownloaded.value = onlyDownloaded
    }

    fun setBackpackSearchQuery(query: String) {
        _backpackSearchQuery.value = query
    }

    fun setSubjectFilter(subject: String) {
        _selectedSubject.value = subject
    }

    fun setGradeFilter(grade: Int?) {
        _selectedGradeFilter.value = grade
    }

    fun setLanguageFilter(language: String) {
        _selectedLanguageFilter.value = language
    }

    fun toggleLessonDownload(lesson: LessonEntity) {
        viewModelScope.launch {
            if (lesson.downloaded) {
                repository.removeLessonFromBackpack(lesson)
                _bannerMessage.value = str(R.string.vm_lesson_removed, lesson.title)
            } else {
                repository.downloadLessonToBackpack(lesson)
                _bannerMessage.value = str(R.string.vm_lesson_saved, lesson.title)
            }
        }
    }

    fun ensureLocalLessonReady(lesson: LessonEntity, onReady: (LessonEntity) -> Unit) {
        viewModelScope.launch {
            val ready = if (lesson.localFilePath.isNullOrBlank()) {
                repository.downloadLessonToBackpack(lesson)
            } else {
                lesson
            }
            onReady(ready)
        }
    }

    fun rateLesson(lesson: LessonEntity, stars: Int) {
        viewModelScope.launch {
            repository.rateLessonQuality(currentUserId, lesson, stars)
            _bannerMessage.value = str(R.string.vm_lesson_rated, lesson.title, stars)
        }
    }

    fun startQuizForLesson(lesson: LessonEntity) {
        viewModelScope.launch {
            val questions = repository.getQuizForLesson(lesson.id)
            _activeQuizQuestions.value = questions
            _activeQuizLesson.value = lesson
        }
    }

    fun closeQuiz() {
        _activeQuizLesson.value = null
        _activeQuizQuestions.value = emptyList()
    }

    fun markLessonVideoWatched(lesson: LessonEntity) {
        val student = activeStudent.value ?: return
        val alreadyWatched = LessonProgressionTracker.isWatched(lesson, student.id)
        viewModelScope.launch {
            repository.markLessonVideoWatched(lesson.id, student.id)
            if (!alreadyWatched && LessonProgressionTracker.isQuizPassed(lesson, student.id)) {
                val nextTitle = LessonProgressionTracker.findNextUnlockedLessonTitle(allLessons.value, lesson)
                if (nextTitle != null) {
                    _confettiTrigger.value += 1
                    _bannerMessage.value = str(R.string.vm_next_lesson_unlocked, nextTitle)
                }
            }
        }
    }

    fun toggleHomeworkCompleted(homework: HomeworkEntity) {
        val student = activeStudent.value ?: return
        val online = effectiveIsOnline.value
        viewModelScope.launch {
            val updated = repository.toggleHomeworkCompleted(homework, student.id, online)
            val isNowDone = updated.completedByProfileIds.split(",").any { it.trim() == student.id }
            if (isNowDone) {
                _confettiTrigger.value += 1
                _bannerMessage.value = if (online) {
                    str(R.string.vm_hw_marked_done, homework.title)
                } else {
                    str(R.string.vm_hw_queued_offline, homework.title)
                }
            }
        }
    }

    fun completeQuiz(lesson: LessonEntity, correctCount: Int, totalCount: Int) {
        val student = activeStudent.value ?: return
        val safeTotal = totalCount.coerceAtLeast(1)
        val passed = correctCount * 2 >= safeTotal // >= 50% passes & unlocks next lesson once video is watched
        val xp = if (passed) (correctCount * 25) + 30 else (correctCount * 15) + 10
        _confettiTrigger.value += 1
        viewModelScope.launch {
            repository.recordQuizAttempt(
                studentProfileId = student.id,
                lesson = lesson,
                score = correctCount,
                total = safeTotal,
                passed = passed
            )
            repository.awardKnowledgeTreeProgress(
                uid = currentUserId,
                profileId = student.id,
                xpEarned = xp,
                lessonCompletedId = if (passed) lesson.id else null
            )
            val watched = LessonProgressionTracker.isWatched(lesson, student.id)
            val nextTitle = if (passed && watched) {
                LessonProgressionTracker.findNextUnlockedLessonTitle(allLessons.value, lesson)
            } else null

            _bannerMessage.value = when {
                nextTitle != null -> str(R.string.vm_quiz_unlocked_next, correctCount, safeTotal, xp, nextTitle)
                passed && !watched -> str(R.string.vm_quiz_passed_watch_video, correctCount, safeTotal, xp)
                else -> str(R.string.vm_quiz_completed_xp, correctCount, safeTotal, xp)
            }
        }
    }

    fun triggerConfetti() {
        _confettiTrigger.value += 1
    }

    fun awardStudyBonusXp(xpEarned: Int, reason: String) {
        val student = activeStudent.value ?: return
        val safeXp = xpEarned.coerceAtLeast(1)
        _confettiTrigger.value += 1
        viewModelScope.launch {
            repository.awardKnowledgeTreeProgress(
                uid = currentUserId,
                profileId = student.id,
                xpEarned = safeXp,
                lessonCompletedId = null
            )
            _bannerMessage.value = str(R.string.vm_bonus_xp_earned, safeXp, reason)
        }
    }

    // --- Offline Doubt Queue & Live Sessions ---

    fun setSimulateOffline(simOffline: Boolean) {
        _simulateOffline.value = simOffline
        if (!simOffline) {
            syncDoubtsNow()
        }
    }

    fun submitDoubt(subject: String, questionText: String, attachmentUri: String?) {
        val student = activeStudent.value
        if (student == null) {
            _bannerMessage.value = str(R.string.vm_signin_student_first)
            return
        }
        val online = effectiveIsOnline.value
        viewModelScope.launch {
            val res = repository.submitDoubt(
                uid = currentUserId,
                student = student,
                subject = subject,
                questionText = questionText,
                attachmentUri = attachmentUri,
                isOnline = online
            )
            res.onSuccess {
                if (!online) {
                    _paperPlaneTrigger.value += 1
                    _bannerMessage.value = str(R.string.vm_doubt_queued_offline)
                } else {
                    _bannerMessage.value = str(R.string.vm_doubt_sent_online)
                }
            }.onFailure { err ->
                _bannerMessage.value = err.localizedMessage ?: str(R.string.vm_doubt_save_failed)
            }
        }
    }

    fun syncDoubtsNow() {
        if (!effectiveIsOnline.value) {
            _bannerMessage.value = str(R.string.vm_still_offline)
            return
        }
        viewModelScope.launch {
            repository.syncPendingDoubtsNow(currentUserId).onSuccess { count ->
                if (count > 0) {
                    _bannerMessage.value = str(R.string.vm_doubts_synced_count, count)
                }
            }
        }
    }

    fun startNewLiveSession(title: String, subject: String, gradeRange: String) {
        if (!effectiveIsOnline.value) {
            _bannerMessage.value = str(R.string.vm_live_internet_required)
            return
        }
        val educator = activeEducator.value
        val name = educator?.fullName?.ifBlank { str(R.string.edu_volunteer_default) }
            ?: str(R.string.edu_volunteer_default)
        val tier = educator?.tier ?: "TIER_1_PAID"
        viewModelScope.launch {
            repository.createLiveSession(
                uid = currentUserId,
                title = title,
                subject = subject,
                gradeRange = gradeRange,
                educatorName = name,
                educatorTier = tier
            ).onSuccess {
                _bannerMessage.value = str(R.string.vm_live_opened, title)
            }.onFailure { err ->
                _bannerMessage.value = err.localizedMessage ?: str(R.string.vm_live_failed)
            }
        }
    }

    fun joinLiveSession(sessionId: String) {
        if (sessionId.isBlank()) return
        viewModelScope.launch {
            repository.incrementSessionParticipants(currentUserId, sessionId)
        }
    }

    // --- Smart AI Chatbox Actions (Bulletproof Offline + Online Cascade) ---

    fun setPreferOfflineAi(prefer: Boolean) {
        _preferOfflineAi.value = prefer
        _aiError.value = null
    }

    fun setOnlineGeminiMode(mode: GeminiOnlineMode) {
        _onlineGeminiMode.value = mode
    }

    fun clearAiError() {
        _aiError.value = null
    }

    fun clearChatHistory() {
        val student = activeStudent.value ?: return
        viewModelScope.launch {
            repository.clearChatForStudent(student.id)
            _streamingText.value = ""
            _aiError.value = null
        }
    }

    fun sendAiQuestion(question: String) {
        val clean = question.trim()
        val student = activeStudent.value ?: return
        if (clean.isBlank() || _isAiGenerating.value) return

        _aiError.value = null
        _isAiGenerating.value = true
        _streamingText.value = ""

        val online = effectiveIsOnline.value
        val preferOffline = _preferOfflineAi.value
        val priorHistory = chatMessages.value
        val onlineModel = _onlineGeminiMode.value

        viewModelScope.launch {
            repository.saveChatMessage(
                ChatMessageEntity(
                    id = "msg_${UUID.randomUUID()}",
                    studentProfileId = student.id,
                    role = "user",
                    content = clean,
                    engineBadge = "Student (Class ${student.gradeClass})"
                )
            )

            val knowledge = repository.getOfflineKnowledgeSnapshot()
            try {
                aiService.askQuestionStream(
                    question = clean,
                    gradeClass = student.gradeClass,
                    history = priorHistory,
                    isOnline = online,
                    offlineModeSelected = preferOffline,
                    onlineModelMode = onlineModel,
                    knowledge = knowledge
                ).collect { update ->
                    _streamingText.value = update.text
                    _streamingEngine.value = update.engineBadge
                    if (update.isFinal) {
                        repository.saveChatMessage(
                            ChatMessageEntity(
                                id = "msg_${UUID.randomUUID()}",
                                studentProfileId = student.id,
                                role = "assistant",
                                content = update.text,
                                engineBadge = update.engineBadge
                            )
                        )
                        _streamingText.value = ""
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Cloud AI unavailable, rescuing with Offline Study Engine", e)
                val rescueReply = OfflineSyllabusEngine.answer(
                    rawQuery = clean,
                    gradeClass = student.gradeClass,
                    knowledge = knowledge
                ).markdown
                repository.saveChatMessage(
                    ChatMessageEntity(
                        id = "msg_${UUID.randomUUID()}",
                        studentProfileId = student.id,
                        role = "assistant",
                        content = rescueReply,
                        engineBadge = "Offline Study AI"
                    )
                )
                _streamingText.value = ""
            } finally {
                _isAiGenerating.value = false
            }
        }
    }

    // --- Educator Onboarding, Gemini Vision Verification & Lesson Upload ---

    fun resetDocVerificationState() {
        _docVerificationState.value = DocumentVerificationUiState.Idle
    }

    fun verifyEducatorDocumentWithAi(
        context: Context,
        imageUri: Uri,
        educator: EducatorProfileEntity
    ) {
        viewModelScope.launch {
            _docVerificationState.value = DocumentVerificationUiState.Verifying("gemini-3.5-flash-lite")
            val res = aiService.verifyEducatorDocument(
                context = context,
                imageUri = imageUri,
                expectedName = educator.fullName,
                expectedBoard = educator.board12th
            )
            res.onSuccess { report ->
                val updated = educator.copy(
                    documentAttached = true,
                    documentUri = imageUri.toString(),
                    docVerificationStatus = report.decision,
                    docAiSummary = report.summary,
                    docConfidence = report.confidenceScore,
                    docExtractedName = report.detectedName,
                    docExtractedBoard = report.detectedBoard
                )
                repository.saveEducatorProfile(currentUserId, updated)
                _docVerificationState.value = DocumentVerificationUiState.Verified(report)
                _bannerMessage.value = str(
                    R.string.vm_doc_verified_banner,
                    report.modelUsed,
                    report.decision,
                    report.confidenceScore
                )
            }.onFailure { err ->
                val msg = err.localizedMessage ?: str(R.string.vm_doc_unable)
                val fallbackQueued = educator.copy(
                    documentAttached = true,
                    documentUri = imageUri.toString(),
                    docVerificationStatus = "NEEDS_REVIEW",
                    docAiSummary = str(R.string.vm_doc_queued_summary, msg)
                )
                repository.saveEducatorProfile(currentUserId, fallbackQueued)
                _docVerificationState.value = DocumentVerificationUiState.Failed(msg)
            }
        }
    }

    fun saveEducatorStep(updated: EducatorProfileEntity) {
        viewModelScope.launch {
            repository.saveEducatorProfile(currentUserId, updated).onFailure { err ->
                Log.w(TAG, "Saved locally; cloud sync pending: ${err.message}")
            }
        }
    }

    fun publishEducatorLesson(
        educator: EducatorProfileEntity,
        title: String,
        subject: String,
        gradeClass: Int,
        language: String,
        durationLabel: String,
        summary: String,
        studyNotes: String,
        quizQuestion: String,
        options: List<String>,
        correctIndex: Int,
        explanation: String,
        customVideoUri: String?
    ) {
        viewModelScope.launch {
            val res = repository.publishEducatorLesson(
                uid = currentUserId,
                educator = educator,
                title = title,
                subject = subject,
                gradeClass = gradeClass,
                language = language,
                durationLabel = durationLabel,
                summary = summary,
                studyNotes = studyNotes,
                quizQuestion = quizQuestion,
                options = options,
                correctIndex = correctIndex,
                explanation = explanation,
                customVideoUri = customVideoUri
            )
            res.onSuccess { created ->
                _confettiTrigger.value += 1
                _bannerMessage.value = str(R.string.vm_published_lesson, created.title)
            }.onFailure { err ->
                _bannerMessage.value = err.localizedMessage ?: str(R.string.vm_publish_failed)
            }
        }
    }

    fun assignHomeworkAsEducator(
        educator: EducatorProfileEntity,
        title: String,
        description: String,
        subject: String,
        gradeClass: Int,
        dueDateLabel: String
    ) {
        viewModelScope.launch {
            val created = repository.createHomeworkAssignment(
                title = title,
                description = description,
                subject = subject,
                gradeClass = gradeClass,
                dueDateLabel = dueDateLabel,
                assignedByName = educator.fullName.ifBlank { str(R.string.edu_volunteer_default) }
            )
            repository.saveEducatorProfile(
                currentUserId,
                educator.copy(hoursTaught = educator.hoursTaught + 0.5)
            )
            val notif = repository.createAndDispatchNotification(
                studentProfileId = "all",
                title = str(R.string.notif_new_hw_title),
                message = str(
                    R.string.notif_new_hw_msg,
                    created.assignedByName,
                    created.title,
                    created.gradeClass,
                    created.dueDateLabel
                ),
                category = "homework",
                targetTab = "backpack"
            )
            _activeBannerNotification.value = notif
            _confettiTrigger.value += 1
            _bannerMessage.value = str(R.string.vm_hw_assigned, created.title, created.gradeClass)
        }
    }

    fun logEducatorSession(hours: Double, studentsCount: Int) {
        val current = activeEducator.value ?: return
        val updated = current.copy(
            hoursTaught = current.hoursTaught + hours,
            studentsMentored = current.studentsMentored + studentsCount
        )
        _confettiTrigger.value += 1
        viewModelScope.launch {
            repository.saveEducatorProfile(currentUserId, updated)
            _bannerMessage.value = str(R.string.vm_hours_logged, hours)
        }
    }

    fun replyToStudentDoubt(doubt: DoubtQueueEntity, reply: String) {
        viewModelScope.launch {
            repository.replyToDoubtAsEducator(currentUserId, doubt, reply).onSuccess {
                val edu = activeEducator.value
                if (edu != null) {
                    repository.saveEducatorProfile(
                        currentUserId,
                        edu.copy(
                            doubtsResolved = edu.doubtsResolved + 1,
                            hoursTaught = edu.hoursTaught + 0.5
                        )
                    )
                }
                _bannerMessage.value = str(R.string.vm_reply_sent, doubt.studentName)
            }
        }
    }

    // --- 100% Admin Control Center Actions ---

    fun adminCreateStudent(
        name: String,
        email: String,
        password: String,
        gradeClass: Int,
        preferredLanguage: String,
        centerName: String
    ) {
        viewModelScope.launch {
            val res = repository.createStudentProfile(
                uid = currentUserId,
                name = name,
                email = email,
                password = password,
                gradeClass = gradeClass,
                avatarId = "owl",
                themeColorKey = "sage",
                preferredLanguage = preferredLanguage,
                parentalConsentGiven = true,
                centerName = centerName,
                pinSequence = "0,1,2,3"
            )
            res.onSuccess { created ->
                _bannerMessage.value = str(R.string.vm_admin_added_student, created.name, created.gradeClass)
            }.onFailure { err ->
                _bannerMessage.value = err.localizedMessage ?: str(R.string.vm_admin_add_student_failed)
            }
        }
    }

    fun adminCreateEducator(
        fullName: String,
        email: String,
        password: String,
        tier: String,
        subjectSpecialty: String
    ) {
        viewModelScope.launch {
            val res = repository.registerEducatorWithEmail(
                uid = currentUserId,
                fullName = fullName,
                email = email,
                password = password,
                tier = tier,
                subjectSpecialty = subjectSpecialty
            )
            res.onSuccess { created ->
                _bannerMessage.value = str(R.string.vm_admin_added_edu, created.fullName)
            }.onFailure { err ->
                _bannerMessage.value = err.localizedMessage ?: str(R.string.vm_admin_add_edu_failed)
            }
        }
    }

    fun adminUpdateStudent(updated: StudentProfileEntity) {
        viewModelScope.launch {
            repository.updateStudentProfile(currentUserId, updated)
            _bannerMessage.value = str(R.string.vm_admin_updated_student, updated.name, updated.gradeClass)
        }
    }

    fun adminDeleteStudent(profileId: String, name: String) {
        viewModelScope.launch {
            repository.deleteStudentProfile(currentUserId, profileId)
            _bannerMessage.value = str(R.string.vm_admin_removed_student, name)
        }
    }

    fun adminApproveEducator(educator: EducatorProfileEntity, approved: Boolean) {
        viewModelScope.launch {
            val status = if (approved) "ADMIN_APPROVED" else "REJECTED"
            val updated = educator.copy(
                adminApproved = approved,
                docVerificationStatus = status
            )
            repository.saveEducatorProfile(currentUserId, updated)
            repository.createAndDispatchNotification(
                studentProfileId = "all",
                title = str(
                    if (approved) R.string.notif_edu_verified_title
                    else R.string.notif_edu_returned_title
                ),
                message = str(
                    if (approved) R.string.notif_edu_verified_msg
                    else R.string.notif_edu_returned_msg,
                    educator.fullName
                ),
                category = "educator_verify",
                targetTab = "backpack"
            )
            _bannerMessage.value = str(R.string.vm_admin_edu_status, educator.fullName, status)
        }
    }

    fun adminUpdateEducatorTier(educator: EducatorProfileEntity, tier: String) {
        viewModelScope.launch {
            repository.saveEducatorProfile(currentUserId, educator.copy(tier = tier))
            _bannerMessage.value = str(R.string.vm_admin_edu_tier, educator.fullName)
        }
    }

    fun adminToggleBackupRoster(educator: EducatorProfileEntity, isBackup: Boolean) {
        viewModelScope.launch {
            repository.saveEducatorProfile(currentUserId, educator.copy(isBackupRoster = isBackup))
            _bannerMessage.value = str(
                if (isBackup) R.string.vm_admin_backup_added else R.string.vm_admin_backup_removed,
                educator.fullName
            )
        }
    }

    fun adminDeleteEducator(educatorId: String, name: String) {
        viewModelScope.launch {
            repository.deleteEducator(currentUserId, educatorId)
            _bannerMessage.value = str(R.string.vm_admin_removed_edu, name)
        }
    }

    fun adminUpdateLessonQuality(lesson: LessonEntity, status: String) {
        viewModelScope.launch {
            repository.updateLessonQualityStatus(currentUserId, lesson, status)
            _bannerMessage.value = str(R.string.vm_admin_lesson_status, lesson.title, status)
        }
    }

    fun adminDeleteLesson(lesson: LessonEntity) {
        viewModelScope.launch {
            repository.deleteLesson(currentUserId, lesson.id)
            _bannerMessage.value = str(R.string.vm_admin_deleted_lesson, lesson.title)
        }
    }

    fun adminDeleteDoubt(doubt: DoubtQueueEntity) {
        viewModelScope.launch {
            repository.deleteDoubt(currentUserId, doubt.id)
            _bannerMessage.value = str(R.string.vm_admin_deleted_doubt, doubt.studentName)
        }
    }

    fun adminBroadcastNotification(
        title: String,
        message: String,
        category: String = "homework",
        targetTab: String = "backpack"
    ) {
        viewModelScope.launch {
            val created = repository.createAndDispatchNotification(
                studentProfileId = "all",
                title = title,
                message = message,
                category = category,
                targetTab = targetTab
            )
            _activeBannerNotification.value = created
            _bannerMessage.value = str(R.string.vm_admin_broadcast_sent)
        }
    }

    private companion object {
        const val TAG = "TwogetherVM"
    }
}
