package com.example.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
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
import com.example.data.local.SeedData
import com.example.data.local.StudentProfileEntity
import com.example.data.local.TwogetherDatabase
import com.example.data.remote.FirestoreDoubt
import com.example.data.remote.FirestoreEducatorProfile
import com.example.data.remote.FirestoreLiveSession
import com.example.data.remote.FirestoreStudentProfile
import com.example.data.remote.FirestoreUploadedLesson
import com.example.data.remote.OperationType
import com.example.data.remote.handleFirestoreError
import com.example.ui.components.localizedForCurrentLocale
import com.example.util.LessonMediaRegistry
import com.example.util.PasswordHasher
import com.example.util.TwogetherNotifier
import com.example.worker.DoubtSyncWorker
import com.example.worker.HomeworkReminderWorker
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import java.util.UUID

class TwogetherRepository(
    context: Context,
    private val firestore: FirebaseFirestore
) {
    private val appContext = context.applicationContext
    private val localDb = TwogetherDatabase.getInstance(appContext)
    private val auth: FirebaseAuth = Firebase.auth

    fun localizedContext(): Context = appContext.localizedForCurrentLocale()

    constructor(context: Context) : this(
        context = context,
        firestore = FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing cloud storage.")
    }

    private fun canSyncToCloud(uid: String): Boolean {
        val activeUid = auth.currentUser?.uid
        return !activeUid.isNullOrBlank() && activeUid == uid && uid != LOCAL_WORKSPACE_UID
    }

    // --- Initial Offline Seeding & Local Backpack Video Materialization ---

    suspend fun ensureLocalSeedData(uid: String) = withContext(Dispatchers.IO) {
        val accountDao = localDb.portalAccountDao()
        val profileDao = localDb.studentProfileDao()
        val educatorDao = localDb.educatorProfileDao()
        val lessonDao = localDb.lessonDao()
        val quizDao = localDb.quizQuestionDao()
        val doubtDao = localDb.doubtQueueDao()

        accountDao.insertAllIgnore(SeedData.defaultPortalAccounts(uid))
        profileDao.insertAllIgnore(SeedData.defaultProfilesForUser(uid))
        educatorDao.insertAllIgnore(SeedData.defaultEducatorsForUser(uid))
        doubtDao.insertAllIgnore(SeedData.defaultDoubtsForUser(uid))

        // Sync/upgrade all 12 NCERT bundled curriculum lessons while preserving user progress
        // and leaving educator-uploaded (lesson_edu_*) lessons untouched.
        for (seedLesson in SeedData.initialLessons) {
            val existing = lessonDao.getLessonById(seedLesson.id)
            val file = materializeLessonMp4(seedLesson.id, seedLesson.rawResName)
            if (existing == null) {
                lessonDao.upsertLesson(
                    seedLesson.copy(
                        downloaded = true,
                        localFilePath = file?.absolutePath
                    )
                )
            } else {
                lessonDao.upsertLesson(
                    seedLesson.copy(
                        downloaded = true,
                        localFilePath = file?.absolutePath ?: existing.localFilePath,
                        watchedByProfileIds = existing.watchedByProfileIds,
                        completedByProfileIds = existing.completedByProfileIds,
                        feedbackRating = existing.feedbackRating,
                        feedbackCount = existing.feedbackCount
                    )
                )
            }
        }
        SeedData.initialQuizzes.forEach { quizDao.upsertQuestion(it) }

        val notifDao = localDb.appNotificationDao()
        if (notifDao.countNotifications() == 0) {
            notifDao.insertAllIgnore(SeedData.initialNotifications)
        }

        val hwDao = localDb.homeworkDao()
        hwDao.insertAllIgnore(SeedData.initialHomeworks)

        localDb.quizAttemptDao().insertAllIgnore(SeedData.defaultQuizAttemptsForUser(uid))
        HomeworkReminderWorker.scheduleDailyReminders(appContext)
    }

    fun materializeLessonMp4(lessonId: String, rawResName: String): File? {
        return try {
            val backpackDir = File(appContext.filesDir, "backpack").apply { mkdirs() }
            val outFile = File(backpackDir, "${lessonId}_${rawResName}.mp4")
            val rawId = LessonMediaRegistry.resolveRawResId(rawResName)
            val expectedLength = try {
                appContext.resources.openRawResourceFd(rawId).use { it.length }
            } catch (_: Exception) {
                -1L
            }
            if (outFile.exists() && outFile.length() > 0L && (expectedLength <= 0L || outFile.length() == expectedLength)) {
                return outFile
            }
            appContext.resources.openRawResource(rawId).use { input ->
                outFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            outFile
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save local mp4 for $lessonId", e)
            null
        }
    }

    // --- Role Portal Email & Password Authentication (Admin / Student / Educator) ---

    fun observePortalAccounts(uid: String): Flow<List<PortalAccountEntity>> =
        localDb.portalAccountDao().observeAccountsForUser(uid)

    suspend fun authenticateOnLaunch(
        expectedRole: String,
        rawEmail: String,
        rawPassword: String
    ): Result<PortalAccountEntity> {
        ensureLocalSeedData(LOCAL_WORKSPACE_UID)
        return authenticatePortalAccount(
            uid = LOCAL_WORKSPACE_UID,
            expectedRole = expectedRole,
            rawEmail = rawEmail,
            rawPassword = rawPassword
        )
    }

    suspend fun authenticatePortalAccount(
        uid: String,
        expectedRole: String,
        rawEmail: String,
        rawPassword: String
    ): Result<PortalAccountEntity> {
        val localized = localizedContext()
        val cleanEmail = rawEmail.trim().lowercase(Locale.US)
        if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
            return Result.failure(IllegalArgumentException(localized.getString(R.string.auth_err_valid_email)))
        }
        if (rawPassword.isBlank()) {
            return Result.failure(IllegalArgumentException(localized.getString(R.string.repo_enter_password)))
        }
        val account = localDb.portalAccountDao().getAccountByEmail(uid, cleanEmail)
            ?: localDb.portalAccountDao().getAnyAccountByEmail(cleanEmail)
            ?: return Result.failure(IllegalArgumentException(localized.getString(R.string.repo_no_account, cleanEmail)))
        if (account.role != expectedRole) {
            val localizedRole = when (account.role) {
                "ADMIN" -> localized.getString(R.string.role_admin)
                "EDUCATOR" -> localized.getString(R.string.role_educator)
                else -> localized.getString(R.string.role_student)
            }
            return Result.failure(
                IllegalArgumentException(localized.getString(R.string.repo_wrong_portal, localizedRole))
            )
        }
        if (!PasswordHasher.verify(rawPassword, account.passwordHash)) {
            return Result.failure(IllegalArgumentException(localized.getString(R.string.repo_wrong_password, cleanEmail)))
        }
        return Result.success(account)
    }

    /**
     * Offline-first Forgot Password reset: verifies registered email + role in local Room DB
     * and immediately updates the SHA-256 password hash offline, plus triggers Firebase
     * sendPasswordResetEmail best-effort when online.
     */
    suspend fun resetPortalPassword(
        expectedRole: String,
        rawEmail: String,
        newPassword: String,
        isOnline: Boolean
    ): Result<String> {
        ensureLocalSeedData(LOCAL_WORKSPACE_UID)
        val localized = localizedContext()
        val cleanEmail = rawEmail.trim().lowercase(Locale.US)
        if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
            return Result.failure(IllegalArgumentException(localized.getString(R.string.auth_err_valid_email)))
        }
        if (newPassword.length < 6) {
            return Result.failure(IllegalArgumentException(localized.getString(R.string.auth_err_password_len)))
        }
        val existing = localDb.portalAccountDao().getAnyAccountByEmail(cleanEmail)
            ?: return Result.failure(IllegalArgumentException(localized.getString(R.string.repo_no_account, cleanEmail)))
        if (!existing.role.equals(expectedRole, ignoreCase = true)) {
            val localizedRole = when (existing.role) {
                "ADMIN" -> localized.getString(R.string.role_admin)
                "EDUCATOR" -> localized.getString(R.string.role_educator)
                else -> localized.getString(R.string.role_student)
            }
            return Result.failure(
                IllegalArgumentException(localized.getString(R.string.repo_wrong_portal, localizedRole))
            )
        }
        localDb.portalAccountDao().upsertAccount(
            existing.copy(passwordHash = PasswordHasher.sha256(newPassword))
        )
        if (isOnline) {
            try {
                auth.sendPasswordResetEmail(cleanEmail).await()
            } catch (e: Exception) {
                Log.d(TAG, "Best-effort Firebase reset email skipped for local account: ${e.message}")
            }
        }
        return Result.success(localized.getString(R.string.forgot_pw_success, cleanEmail))
    }

    suspend fun registerAdminAccount(
        uid: String,
        fullName: String,
        email: String,
        password: String
    ): Result<PortalAccountEntity> {
        ensureLocalSeedData(uid)
        val localized = localizedContext()
        val cleanName = fullName.trim().ifBlank { "NGO Coordinator" }.take(60)
        val cleanEmail = email.trim().lowercase(Locale.US)
        if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
            return Result.failure(IllegalArgumentException(localized.getString(R.string.auth_err_valid_email)))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException(localized.getString(R.string.auth_err_password_len)))
        }
        if (localDb.portalAccountDao().getAnyAccountByEmail(cleanEmail) != null) {
            return Result.failure(IllegalArgumentException(localized.getString(R.string.repo_email_exists, cleanEmail)))
        }
        val entity = PortalAccountEntity(
            email = cleanEmail,
            ownerUid = uid,
            role = "ADMIN",
            passwordHash = PasswordHasher.sha256(password),
            displayName = cleanName,
            linkedEntityId = "admin_${UUID.randomUUID().toString().replace("-", "").take(8)}"
        )
        localDb.portalAccountDao().upsertAccount(entity)
        return Result.success(entity)
    }

    suspend fun getStudentProfileById(profileId: String): StudentProfileEntity? =
        localDb.studentProfileDao().getProfileById(profileId)

    suspend fun getEducatorById(educatorId: String): EducatorProfileEntity? =
        localDb.educatorProfileDao().getEducatorById(educatorId)

    // --- Student Profiles (Room + Firestore Sync + Admin 100% Control) ---

    fun observeStudentProfiles(uid: String): Flow<List<StudentProfileEntity>> =
        localDb.studentProfileDao().observeProfilesForUser(uid)

    suspend fun createStudentProfile(
        uid: String,
        name: String,
        email: String = "",
        password: String = "",
        gradeClass: Int,
        avatarId: String,
        themeColorKey: String,
        preferredLanguage: String = "Bilingual",
        parentalConsentGiven: Boolean = true,
        centerName: String = "Asha Kiran NGO Center",
        pinSequence: String
    ): Result<StudentProfileEntity> {
        val localized = localizedContext()
        val cleanName = name.trim().take(50)
        if (cleanName.isBlank()) return Result.failure(IllegalArgumentException(localized.getString(R.string.auth_err_full_name)))
        ensureLocalSeedData(uid)
        val cleanEmail = email.trim().lowercase(Locale.US).ifBlank {
            "${cleanName.lowercase(Locale.US).replace(Regex("[^a-z0-9]"), "")}_${UUID.randomUUID().toString().take(4)}@student.twogether.org"
        }
        val existingAccount = localDb.portalAccountDao().getAnyAccountByEmail(cleanEmail)
        if (existingAccount != null && email.isNotBlank()) {
            return Result.failure(IllegalArgumentException(localized.getString(R.string.repo_email_exists, cleanEmail)))
        }

        val profileId = "prof_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val entity = StudentProfileEntity(
            id = profileId,
            ownerUid = uid,
            name = cleanName,
            email = cleanEmail,
            gradeClass = gradeClass.coerceIn(1, 10),
            avatarId = avatarId,
            themeColorKey = themeColorKey,
            preferredLanguage = preferredLanguage.ifBlank { "Bilingual" },
            parentalConsentGiven = parentalConsentGiven,
            centerName = centerName.ifBlank { "Asha Kiran NGO Center" },
            pinSequence = pinSequence,
            xpPoints = 20,
            modulesCompleted = 0
        )
        localDb.studentProfileDao().upsertProfile(entity)

        val effectivePassword = password.ifBlank { "Study@1234" }
        localDb.portalAccountDao().upsertAccount(
            PortalAccountEntity(
                email = cleanEmail,
                ownerUid = uid,
                role = "STUDENT",
                passwordHash = PasswordHasher.sha256(effectivePassword),
                displayName = cleanName,
                linkedEntityId = profileId
            )
        )

        syncStudentProfileToFirestore(uid, entity)
        return Result.success(entity)
    }

    suspend fun updateStudentProfile(uid: String, updated: StudentProfileEntity): Result<Unit> {
        localDb.studentProfileDao().upsertProfile(updated.copy(updatedAtMillis = System.currentTimeMillis()))
        return syncStudentProfileToFirestore(uid, updated)
    }

    suspend fun deleteStudentProfile(uid: String, profileId: String): Result<Unit> {
        localDb.studentProfileDao().deleteProfile(profileId)
        localDb.portalAccountDao().deleteByLinkedId(uid, profileId)
        if (!canSyncToCloud(uid)) return Result.success(Unit)
        val path = "users/$uid/student_profiles/$profileId"
        return try {
            val activeUid = requireUserId()
            check(activeUid == uid)
            firestore.collection("users").document(uid)
                .collection("student_profiles").document(profileId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun awardKnowledgeTreeProgress(
        uid: String,
        profileId: String,
        xpEarned: Int,
        lessonCompletedId: String? = null
    ): Result<Unit> {
        val current = localDb.studentProfileDao().getProfileById(profileId)
            ?: return Result.failure(IllegalStateException("Profile not found"))
        val updated = current.copy(
            xpPoints = current.xpPoints + xpEarned,
            modulesCompleted = current.modulesCompleted + if (lessonCompletedId != null) 1 else 0,
            updatedAtMillis = System.currentTimeMillis()
        )
        localDb.studentProfileDao().upsertProfile(updated)

        if (lessonCompletedId != null) {
            val lesson = localDb.lessonDao().getLessonById(lessonCompletedId)
            if (lesson != null) {
                val ids = lesson.completedByProfileIds.split(",").filter { it.isNotBlank() }.toMutableSet()
                ids.add(profileId)
                localDb.lessonDao().upsertLesson(lesson.copy(completedByProfileIds = ids.joinToString(",")))
            }
        }
        return syncStudentProfileToFirestore(uid, updated)
    }

    private suspend fun syncStudentProfileToFirestore(
        uid: String,
        entity: StudentProfileEntity
    ): Result<Unit> {
        if (!canSyncToCloud(uid)) return Result.success(Unit)
        val path = "users/$uid/student_profiles/${entity.id}"
        return try {
            val activeUid = requireUserId()
            check(activeUid == uid) { "Signed-in user mismatch" }
            val docRef = firestore.collection("users").document(uid)
                .collection("student_profiles").document(entity.id)
            val snap = docRef.get().await()
            if (snap.exists()) {
                docRef.update(
                    mapOf(
                        "name" to entity.name.take(50),
                        "email" to entity.email.take(120),
                        "gradeClass" to entity.gradeClass.toLong().coerceIn(1L, 10L),
                        "avatarId" to entity.avatarId,
                        "themeColorKey" to entity.themeColorKey,
                        "preferredLanguage" to entity.preferredLanguage.ifBlank { "Bilingual" }.take(30),
                        "parentalConsentGiven" to entity.parentalConsentGiven,
                        "centerName" to entity.centerName.ifBlank { "Asha Kiran Kendra" }.take(80),
                        "pinSequence" to entity.pinSequence,
                        "xpPoints" to entity.xpPoints.toLong().coerceAtLeast(0L),
                        "modulesCompleted" to entity.modulesCompleted.toLong().coerceAtLeast(0L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                val payload = FirestoreStudentProfile(
                    userId = uid,
                    name = entity.name,
                    email = entity.email,
                    gradeClass = entity.gradeClass.toLong(),
                    avatarId = entity.avatarId,
                    themeColorKey = entity.themeColorKey,
                    preferredLanguage = entity.preferredLanguage,
                    parentalConsentGiven = entity.parentalConsentGiven,
                    centerName = entity.centerName,
                    pinSequence = entity.pinSequence,
                    xpPoints = entity.xpPoints.toLong(),
                    modulesCompleted = entity.modulesCompleted.toLong()
                ).toCreateMap()
                docRef.set(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    // --- Backpack Local Lessons, Teacher Uploads & Quality Feedback ---

    fun observeLessons(): Flow<List<LessonEntity>> = localDb.lessonDao().observeAllLessons()

    suspend fun downloadLessonToBackpack(lesson: LessonEntity): LessonEntity {
        val file = withContext(Dispatchers.IO) {
            materializeLessonMp4(lesson.id, lesson.rawResName)
        }
        val updated = lesson.copy(
            downloaded = true,
            localFilePath = file?.absolutePath
        )
        localDb.lessonDao().upsertLesson(updated)
        return updated
    }

    suspend fun removeLessonFromBackpack(lesson: LessonEntity) {
        localDb.lessonDao().upsertLesson(lesson.copy(downloaded = false))
    }

    suspend fun getQuizForLesson(lessonId: String): List<QuizQuestionEntity> =
        localDb.quizQuestionDao().getQuestionsForLesson(lessonId)

    /**
     * Copies a picked gallery content:// video into permanent app-private internal storage
     * (`filesDir/offline_videos/lesson_<timestamp>.mp4`) so ExoPlayer can play it 100% offline
     * forever without losing temporary URI permissions.
     */
    private suspend fun copyGalleryVideoToOfflineStorage(rawUriString: String?): File? = withContext(Dispatchers.IO) {
        if (rawUriString.isNullOrBlank()) return@withContext null
        try {
            val parsed = Uri.parse(rawUriString)
            if (parsed.scheme == "file" || rawUriString.startsWith("/")) {
                val existing = File(parsed.path ?: rawUriString)
                if (existing.exists() && existing.length() > 0L) return@withContext existing
            }
            val offlineDir = File(appContext.filesDir, "offline_videos").apply { mkdirs() }
            val outFile = File(offlineDir, "lesson_${System.currentTimeMillis()}.mp4")
            val copied = appContext.contentResolver.openInputStream(parsed)?.use { input ->
                outFile.outputStream().use { output ->
                    input.copyTo(output)
                }
                true
            } ?: false
            if (copied && outFile.exists() && outFile.length() > 0L) {
                outFile
            } else {
                outFile.delete()
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to copy gallery video to offline_videos/", e)
            null
        }
    }

    suspend fun markLessonVideoWatched(lessonId: String, profileId: String) {
        val lesson = localDb.lessonDao().getLessonById(lessonId) ?: return
        val ids = lesson.watchedByProfileIds.split(",").filter { it.isNotBlank() }.toMutableSet()
        if (ids.add(profileId)) {
            localDb.lessonDao().upsertLesson(lesson.copy(watchedByProfileIds = ids.joinToString(",")))
        }
    }

    fun observeHomeworks(): Flow<List<HomeworkEntity>> =
        localDb.homeworkDao().observeAllHomeworks()

    suspend fun toggleHomeworkCompleted(
        homework: HomeworkEntity,
        profileId: String,
        isOnline: Boolean
    ): HomeworkEntity {
        val ids = homework.completedByProfileIds.split(",").filter { it.isNotBlank() }.toMutableSet()
        val nowDone = if (ids.contains(profileId)) {
            ids.remove(profileId)
            false
        } else {
            ids.add(profileId)
            true
        }
        val updated = homework.copy(
            completedByProfileIds = ids.joinToString(","),
            syncStatus = if (nowDone && !isOnline) "queued_offline" else "synced"
        )
        localDb.homeworkDao().upsertHomework(updated)
        return updated
    }

    suspend fun createHomeworkAssignment(
        title: String,
        description: String,
        subject: String,
        gradeClass: Int,
        dueDateLabel: String,
        assignedByName: String
    ): HomeworkEntity {
        val entity = HomeworkEntity(
            id = "hw_${UUID.randomUUID().toString().replace("-", "").take(10)}",
            title = title.trim().take(100),
            description = description.trim().take(500),
            subject = subject.trim().take(40),
            gradeClass = gradeClass.coerceIn(1, 10),
            dueDateLabel = dueDateLabel.ifBlank { "Due Soon" }.take(40),
            assignedByName = assignedByName.ifBlank { "Twogether Educator" },
            completedByProfileIds = "",
            syncStatus = "synced"
        )
        localDb.homeworkDao().upsertHomework(entity)
        return entity
    }

    fun observeQuizAttempts(profileId: String): Flow<List<QuizAttemptEntity>> =
        localDb.quizAttemptDao().observeAttemptsForStudent(profileId)

    suspend fun recordQuizAttempt(
        studentProfileId: String,
        lesson: LessonEntity,
        score: Int,
        total: Int,
        passed: Boolean
    ) {
        val attempt = QuizAttemptEntity(
            id = "qa_${UUID.randomUUID().toString().replace("-", "").take(12)}",
            studentProfileId = studentProfileId,
            lessonId = lesson.id,
            lessonTitle = lesson.title,
            subject = lesson.subject,
            gradeClass = lesson.gradeClass,
            score = score,
            total = total.coerceAtLeast(1),
            passed = passed
        )
        localDb.quizAttemptDao().insertAttempt(attempt)
    }

    suspend fun getOfflineKnowledgeSnapshot(): OfflineKnowledgeSnapshot = withContext(Dispatchers.IO) {
        OfflineKnowledgeSnapshot(
            lessons = localDb.lessonDao().getAllLessonsOnce(),
            questions = localDb.quizQuestionDao().getAllQuestionsOnce(),
            homeworks = localDb.homeworkDao().getAllHomeworksOnce()
        )
    }

    suspend fun publishEducatorLesson(
        uid: String,
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
    ): Result<LessonEntity> {
        val localized = localizedContext()
        val cleanTitle = title.trim().take(100)
        if (cleanTitle.length < 3) {
            return Result.failure(IllegalArgumentException(localized.getString(R.string.repo_title_too_short)))
        }
        val lessonId = "lesson_edu_${UUID.randomUUID().toString().replace("-", "").take(10)}"
        val rawClip = when {
            subject.equals("Science", ignoreCase = true) -> "lesson_photosynthesis"
            subject.equals("English", ignoreCase = true) -> "lesson_grammar"
            else -> "lesson_fractions"
        }
        val copiedGalleryFile = copyGalleryVideoToOfflineStorage(customVideoUri)
        val finalLocalFile = copiedGalleryFile ?: withContext(Dispatchers.IO) {
            materializeLessonMp4(lessonId, rawClip)
        }
        val lesson = LessonEntity(
            id = lessonId,
            title = cleanTitle,
            subject = subject.trim().take(40),
            gradeClass = gradeClass.coerceIn(1, 10),
            durationLabel = durationLabel.ifBlank { "8 min" }.take(30),
            summary = summary.trim().take(300),
            studyNotes = studyNotes.trim().take(1500),
            language = language.ifBlank { "Bilingual" }.take(30),
            rawResName = rawClip,
            localFilePath = finalLocalFile?.absolutePath,
            downloaded = true,
            uploadedByEducatorEmail = educator.email,
            uploadedByEducatorName = educator.fullName.ifBlank { localized.getString(R.string.edu_volunteer_default) },
            qualityStatus = "APPROVED",
            feedbackRating = 5.0,
            feedbackCount = 1
        )
        localDb.lessonDao().upsertLesson(lesson)

        if (quizQuestion.isNotBlank() && options.size == 4) {
            val qEntity = QuizQuestionEntity(
                id = "q_${UUID.randomUUID().toString().replace("-", "").take(10)}",
                lessonId = lessonId,
                questionText = quizQuestion.trim(),
                optionA = options[0].trim().ifBlank { localized.getString(R.string.edu_opt_a_correct) },
                optionB = options[1].trim().ifBlank { localized.getString(R.string.edu_opt_b) },
                optionC = options[2].trim().ifBlank { localized.getString(R.string.edu_opt_c) },
                optionD = options[3].trim().ifBlank { localized.getString(R.string.edu_opt_d) },
                correctIndex = correctIndex.coerceIn(0, 3),
                explanation = explanation.trim().ifBlank { localized.getString(R.string.edu_default_expl, lesson.title) }
            )
            localDb.quizQuestionDao().upsertQuestion(qEntity)
        }

        val updatedEdu = educator.copy(
            lessonsUploadedCount = educator.lessonsUploadedCount + 1,
            hoursTaught = educator.hoursTaught + 1.0
        )
        saveEducatorProfile(uid, updatedEdu)

        createAndDispatchNotification(
            studentProfileId = "all",
            title = localized.getString(R.string.notif_new_lesson_title, lesson.gradeClass),
            message = localized.getString(
                R.string.notif_new_lesson_msg,
                lesson.uploadedByEducatorName,
                lesson.title,
                lesson.subject
            ),
            category = "homework",
            targetTab = "backpack"
        )

        syncUploadedLessonToFirestore(uid, lesson)
        return Result.success(lesson)
    }

    suspend fun rateLessonQuality(uid: String, lesson: LessonEntity, stars: Int): Result<Unit> {
        val clamped = stars.coerceIn(1, 5).toDouble()
        val newCount = lesson.feedbackCount + 1
        val newAvg = ((lesson.feedbackRating * lesson.feedbackCount) + clamped) / newCount.toDouble()
        val updated = lesson.copy(
            feedbackRating = String.format(Locale.US, "%.1f", newAvg).toDouble(),
            feedbackCount = newCount
        )
        localDb.lessonDao().upsertLesson(updated)
        return syncUploadedLessonToFirestore(uid, updated)
    }

    suspend fun updateLessonQualityStatus(uid: String, lesson: LessonEntity, status: String) {
        val updated = lesson.copy(qualityStatus = status)
        localDb.lessonDao().upsertLesson(updated)
        syncUploadedLessonToFirestore(uid, updated)
    }

    suspend fun deleteLesson(uid: String, lessonId: String) {
        localDb.lessonDao().deleteLesson(lessonId)
        localDb.quizQuestionDao().deleteQuestionsForLesson(lessonId)
        if (!canSyncToCloud(uid)) return
        try {
            val activeUid = requireUserId()
            check(activeUid == uid)
            firestore.collection("users").document(uid)
                .collection("uploaded_lessons").document(lessonId)
                .delete()
                .await()
        } catch (_: Exception) {
            // Seeded local lesson may not exist remotely
        }
    }

    private suspend fun syncUploadedLessonToFirestore(uid: String, lesson: LessonEntity): Result<Unit> {
        if (!canSyncToCloud(uid)) return Result.success(Unit)
        val path = "users/$uid/uploaded_lessons/${lesson.id}"
        return try {
            val activeUid = requireUserId()
            check(activeUid == uid)
            val docRef = firestore.collection("users").document(uid)
                .collection("uploaded_lessons").document(lesson.id)
            val snap = docRef.get().await()
            if (snap.exists()) {
                docRef.update(
                    mapOf(
                        "qualityStatus" to lesson.qualityStatus,
                        "feedbackRating" to lesson.feedbackRating,
                        "feedbackCount" to lesson.feedbackCount.toLong(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                val payload = FirestoreUploadedLesson(
                    userId = uid,
                    title = lesson.title,
                    subject = lesson.subject,
                    gradeClass = lesson.gradeClass.toLong(),
                    durationLabel = lesson.durationLabel,
                    summary = lesson.summary.ifBlank { lesson.title },
                    studyNotes = lesson.studyNotes,
                    language = lesson.language,
                    uploadedByEducatorEmail = lesson.uploadedByEducatorEmail,
                    uploadedByEducatorName = lesson.uploadedByEducatorName,
                    qualityStatus = lesson.qualityStatus,
                    feedbackRating = lesson.feedbackRating,
                    feedbackCount = lesson.feedbackCount.toLong()
                ).toCreateMap()
                docRef.set(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    // --- Doubt Queue (Offline-First Room + WorkManager + Firestore) ---

    fun observeDoubts(uid: String): Flow<List<DoubtQueueEntity>> =
        localDb.doubtQueueDao().observeDoubtsForUser(uid)

    suspend fun submitDoubt(
        uid: String,
        student: StudentProfileEntity,
        subject: String,
        questionText: String,
        attachmentUri: String?,
        isOnline: Boolean
    ): Result<DoubtQueueEntity> {
        val localized = localizedContext()
        val cleanQuestion = questionText.trim().take(500)
        if (cleanQuestion.length < 3) {
            return Result.failure(IllegalArgumentException(localized.getString(R.string.repo_question_too_short)))
        }
        val doubtId = "dbt_${UUID.randomUUID().toString().replace("-", "").take(14)}"
        val entity = DoubtQueueEntity(
            id = doubtId,
            ownerUid = uid,
            studentProfileId = student.id,
            studentName = student.name,
            gradeClass = student.gradeClass,
            subject = subject.take(40),
            questionText = cleanQuestion,
            attachmentUri = attachmentUri,
            syncStatus = "pending"
        )
        localDb.doubtQueueDao().upsertDoubt(entity)
        if (canSyncToCloud(uid)) {
            DoubtSyncWorker.enqueue(appContext)
        }

        if (isOnline) {
            syncSingleDoubtNow(uid, entity)
        }
        return Result.success(entity)
    }

    suspend fun syncPendingDoubtsNow(uid: String): Result<Int> {
        val localized = localizedContext()
        val pending = localDb.doubtQueueDao().getPendingDoubts().filter { it.ownerUid == uid }
        var syncedCount = 0
        for (doubt in pending) {
            val res = syncSingleDoubtNow(uid, doubt)
            if (res.isSuccess) syncedCount++
        }
        if (syncedCount > 0) {
            createAndDispatchNotification(
                studentProfileId = "all",
                title = localized.getString(R.string.notif_doubt_synced_title),
                message = localized.getString(R.string.notif_doubt_synced_msg, syncedCount),
                category = "doubt",
                targetTab = "doubts"
            )
        }
        return Result.success(syncedCount)
    }

    private suspend fun syncSingleDoubtNow(uid: String, doubt: DoubtQueueEntity): Result<Unit> {
        if (!canSyncToCloud(uid)) {
            localDb.doubtQueueDao().updateSyncStatus(doubt.id, "synced", doubt.educatorReply)
            return Result.success(Unit)
        }
        val path = "users/$uid/doubts/${doubt.id}"
        return try {
            val activeUid = requireUserId()
            check(activeUid == uid)
            val docRef = firestore.collection("users").document(uid)
                .collection("doubts").document(doubt.id)
            val snap = docRef.get().await()
            if (snap.exists()) {
                val existingStatus = snap.getString("status") ?: "synced"
                if (existingStatus != "answered") {
                    docRef.update(
                        mapOf(
                            "status" to "synced",
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
            } else {
                val payload = FirestoreDoubt(
                    userId = uid,
                    studentProfileId = doubt.studentProfileId,
                    studentName = doubt.studentName,
                    gradeClass = doubt.gradeClass.toLong().coerceIn(1L, 10L),
                    subject = doubt.subject,
                    questionText = doubt.questionText,
                    status = "synced",
                    educatorReply = doubt.educatorReply
                ).toCreateMap()
                docRef.set(payload).await()
            }
            localDb.doubtQueueDao().updateSyncStatus(doubt.id, "synced", doubt.educatorReply)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun replyToDoubtAsEducator(
        uid: String,
        doubt: DoubtQueueEntity,
        replyText: String
    ): Result<Unit> {
        val localized = localizedContext()
        val cleanReply = replyText.trim().take(1000)
        if (cleanReply.isBlank()) return Result.failure(IllegalArgumentException(localized.getString(R.string.repo_reply_empty)))
        localDb.doubtQueueDao().updateSyncStatus(doubt.id, "answered", cleanReply)
        createAndDispatchNotification(
            studentProfileId = doubt.studentProfileId,
            title = localized.getString(R.string.notif_doubt_answered_title),
            message = "${doubt.subject}: $cleanReply",
            category = "doubt",
            targetTab = "doubts"
        )

        if (!canSyncToCloud(uid)) return Result.success(Unit)
        val path = "users/$uid/doubts/${doubt.id}"
        return try {
            val activeUid = requireUserId()
            check(activeUid == uid)
            val docRef = firestore.collection("users").document(uid)
                .collection("doubts").document(doubt.id)
            val snap = docRef.get().await()
            if (snap.exists()) {
                docRef.update(
                    mapOf(
                        "status" to "answered",
                        "educatorReply" to cleanReply,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                val payload = FirestoreDoubt(
                    userId = uid,
                    studentProfileId = doubt.studentProfileId,
                    studentName = doubt.studentName,
                    gradeClass = doubt.gradeClass.toLong().coerceIn(1L, 10L),
                    subject = doubt.subject,
                    questionText = doubt.questionText,
                    status = "answered",
                    educatorReply = cleanReply
                ).toCreateMap()
                docRef.set(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteDoubt(uid: String, doubtId: String) {
        localDb.doubtQueueDao().deleteDoubt(doubtId)
        if (!canSyncToCloud(uid)) return
        try {
            val activeUid = requireUserId()
            check(activeUid == uid)
            firestore.collection("users").document(uid)
                .collection("doubts").document(doubtId)
                .delete()
                .await()
        } catch (_: Exception) {
        }
    }

    // --- Live Sessions ---

    fun observeLiveSessions(uid: String): Flow<List<FirestoreLiveSession>> = flow {
        if (!canSyncToCloud(uid)) {
            emit(emptyList())
            return@flow
        }
        val path = "users/$uid/live_sessions"
        emitAll(
            firestore.collection("users").document(uid).collection("live_sessions")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        FirestoreLiveSession::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun createLiveSession(
        uid: String,
        title: String,
        subject: String,
        gradeRange: String,
        educatorName: String,
        educatorTier: String
    ): Result<String> {
        if (!canSyncToCloud(uid)) {
            return Result.failure(
                IllegalStateException(localizedContext().getString(R.string.vm_live_internet_required))
            )
        }
        val sessionId = "live_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val path = "users/$uid/live_sessions/$sessionId"
        return try {
            val activeUid = requireUserId()
            check(activeUid == uid)
            val payload = FirestoreLiveSession(
                userId = uid,
                title = title.trim().take(100),
                subject = subject.trim().take(40),
                gradeRange = gradeRange.trim().take(30),
                educatorName = educatorName.trim().take(80),
                educatorTier = educatorTier,
                openSession = true,
                participantCount = 1L
            ).toCreateMap()
            firestore.collection("users").document(uid)
                .collection("live_sessions").document(sessionId)
                .set(payload)
                .await()
            createAndDispatchNotification(
                studentProfileId = "all",
                title = localizedContext().getString(R.string.notif_live_starting_title),
                message = localizedContext().getString(
                    R.string.notif_live_starting_msg,
                    educatorName,
                    title,
                    subject,
                    gradeRange
                ),
                category = "live",
                targetTab = "doubts"
            )
            Result.success(sessionId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun incrementSessionParticipants(uid: String, sessionId: String): Result<Unit> {
        if (!canSyncToCloud(uid)) return Result.success(Unit)
        val path = "users/$uid/live_sessions/$sessionId"
        return try {
            val activeUid = requireUserId()
            check(activeUid == uid)
            firestore.collection("users").document(uid)
                .collection("live_sessions").document(sessionId)
                .update(
                    mapOf(
                        "participantCount" to FieldValue.increment(1L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    // --- Educators (Multi-Educator Email Routing, Doc Verification, Admin CRUD) ---

    fun observeAllEducators(uid: String): Flow<List<EducatorProfileEntity>> =
        localDb.educatorProfileDao().observeEducatorsForUser(uid)

    fun observeEducatorProfile(uid: String): Flow<EducatorProfileEntity?> =
        localDb.educatorProfileDao().observeEducatorForUser(uid)

    suspend fun registerEducatorWithEmail(
        uid: String,
        fullName: String,
        email: String,
        password: String,
        tier: String,
        subjectSpecialty: String
    ): Result<EducatorProfileEntity> {
        ensureLocalSeedData(uid)
        val localized = localizedContext()
        val cleanEmail = email.trim().lowercase(Locale.US)
        if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
            return Result.failure(IllegalArgumentException(localized.getString(R.string.auth_err_valid_email)))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException(localized.getString(R.string.auth_err_password_len)))
        }
        if (localDb.portalAccountDao().getAnyAccountByEmail(cleanEmail) != null) {
            return Result.failure(IllegalArgumentException(localized.getString(R.string.repo_email_exists, cleanEmail)))
        }
        val cleanName = fullName.trim().ifBlank { localized.getString(R.string.edu_volunteer_default) }.take(80)
        val eduId = "edu_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val entity = EducatorProfileEntity(
            id = eduId,
            ownerUid = uid,
            fullName = cleanName,
            email = cleanEmail,
            tier = tier,
            subjectSpecialty = subjectSpecialty.ifBlank { "Mathematics" }
        )
        localDb.educatorProfileDao().upsertEducator(entity)
        localDb.portalAccountDao().upsertAccount(
            PortalAccountEntity(
                email = cleanEmail,
                ownerUid = uid,
                role = "EDUCATOR",
                passwordHash = PasswordHasher.sha256(password),
                displayName = cleanName,
                linkedEntityId = eduId
            )
        )
        saveEducatorProfile(uid, entity)
        return Result.success(entity)
    }

    suspend fun deleteEducator(uid: String, educatorId: String): Result<Unit> {
        localDb.educatorProfileDao().deleteEducator(educatorId)
        localDb.portalAccountDao().deleteByLinkedId(uid, educatorId)
        if (!canSyncToCloud(uid)) return Result.success(Unit)
        val path = "users/$uid/educators/$educatorId"
        return try {
            val activeUid = requireUserId()
            check(activeUid == uid)
            firestore.collection("users").document(uid)
                .collection("educators").document(educatorId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun saveEducatorProfile(uid: String, entity: EducatorProfileEntity): Result<Unit> {
        localDb.educatorProfileDao().upsertEducator(entity)
        if (!canSyncToCloud(uid)) return Result.success(Unit)
        val path = "users/$uid/educators/${entity.id}"
        return try {
            val activeUid = requireUserId()
            check(activeUid == uid)
            val docRef = firestore.collection("users").document(uid)
                .collection("educators").document(entity.id)
            val snap = docRef.get().await()
            val validFullName = entity.fullName.trim()
                .ifBlank { "Volunteer Educator" }
                .let { if (it.length < 2) "Educator $it" else it }
                .take(80)
            if (snap.exists()) {
                docRef.update(
                    mapOf(
                        "fullName" to validFullName,
                        "email" to entity.email.take(120),
                        "tier" to entity.tier,
                        "board12th" to entity.board12th.ifBlank { "CBSE" }.take(80),
                        "rollYear" to entity.rollYear.ifBlank { "2022" }.take(30),
                        "stream" to entity.stream.ifBlank { "Science" }.take(40),
                        "subjectSpecialty" to entity.subjectSpecialty.ifBlank { "Mathematics" }.take(50),
                        "referenceContact" to entity.referenceContact.take(120),
                        "documentAttached" to entity.documentAttached,
                        "docVerificationStatus" to entity.docVerificationStatus,
                        "docAiSummary" to entity.docAiSummary.take(400),
                        "docConfidence" to entity.docConfidence.toLong().coerceIn(0L, 100L),
                        "isBackupRoster" to entity.isBackupRoster,
                        "adminApproved" to entity.adminApproved,
                        "testScore" to entity.testScore.toLong().coerceIn(0L, 3L),
                        "contractSigned" to entity.contractSigned,
                        "trainingCompleted" to entity.trainingCompleted,
                        "onboardingStep" to entity.onboardingStep.toLong().coerceIn(1L, 5L),
                        "hoursTaught" to entity.hoursTaught.coerceAtLeast(0.0),
                        "doubtsResolved" to entity.doubtsResolved.toLong().coerceAtLeast(0L),
                        "studentsMentored" to entity.studentsMentored.toLong().coerceAtLeast(0L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                val payload = FirestoreEducatorProfile(
                    userId = uid,
                    fullName = validFullName,
                    email = entity.email,
                    tier = entity.tier,
                    board12th = entity.board12th.ifBlank { "CBSE" },
                    rollYear = entity.rollYear.ifBlank { "2022" },
                    stream = entity.stream,
                    subjectSpecialty = entity.subjectSpecialty,
                    referenceContact = entity.referenceContact,
                    documentAttached = entity.documentAttached,
                    docVerificationStatus = entity.docVerificationStatus,
                    docAiSummary = entity.docAiSummary,
                    docConfidence = entity.docConfidence.toLong(),
                    isBackupRoster = entity.isBackupRoster,
                    adminApproved = entity.adminApproved,
                    testScore = entity.testScore.toLong(),
                    contractSigned = entity.contractSigned,
                    trainingCompleted = entity.trainingCompleted,
                    onboardingStep = entity.onboardingStep.toLong(),
                    hoursTaught = entity.hoursTaught,
                    doubtsResolved = entity.doubtsResolved.toLong(),
                    studentsMentored = entity.studentsMentored.toLong()
                ).toCreateMap()
                docRef.set(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    // --- AI Chat History ---

    fun observeChatMessages(studentProfileId: String): Flow<List<ChatMessageEntity>> =
        localDb.chatMessageDao().observeMessages(studentProfileId)

    suspend fun saveChatMessage(message: ChatMessageEntity) {
        localDb.chatMessageDao().insertMessage(message)
    }

    suspend fun clearChatForStudent(studentProfileId: String) {
        localDb.chatMessageDao().clearForProfile(studentProfileId)
    }

    // --- Notifications ---

    fun observeNotifications(): Flow<List<AppNotificationEntity>> =
        localDb.appNotificationDao().observeAllNotifications()

    suspend fun createAndDispatchNotification(
        studentProfileId: String,
        title: String,
        message: String,
        category: String = "homework",
        targetTab: String = "backpack"
    ): AppNotificationEntity {
        val entity = AppNotificationEntity(
            id = "notif_${UUID.randomUUID().toString().replace("-", "").take(12)}",
            studentProfileId = studentProfileId,
            title = title,
            message = message,
            category = category,
            targetTab = targetTab,
            isRead = false
        )
        localDb.appNotificationDao().insertNotification(entity)
        TwogetherNotifier.showSystemNotification(
            context = appContext,
            notificationId = entity.id.hashCode(),
            title = title,
            message = message
        )
        return entity
    }

    suspend fun markAllNotificationsRead() {
        localDb.appNotificationDao().markAllRead()
    }

    companion object {
        private const val TAG = "TwogetherRepository"
        const val LOCAL_WORKSPACE_UID = "local_offline_workspace"
    }
}
