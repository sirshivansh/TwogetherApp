package com.example.ui.navigation

import androidx.annotation.StringRes
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Backpack
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Park
import androidx.compose.material.icons.rounded.QuestionAnswer
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.R
import com.example.data.local.AppNotificationEntity
import com.example.data.local.SessionStore
import com.example.data.repository.HybridAiService
import com.example.data.repository.TwogetherRepository
import com.example.ui.components.ClayLanguageSelectorButton
import com.example.ui.components.EasyUseGuideDialog
import com.example.ui.components.HeadsUpNotificationOverlay
import com.example.ui.components.NotificationsCenterDialog
import com.example.ui.components.TwogetherBrandHeader
import com.example.ui.components.localizedForCurrentLocale
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AiChatScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BackpackScreen
import com.example.ui.screens.DoubtsAndLiveScreen
import com.example.ui.screens.EducatorPortalScreen
import com.example.ui.screens.LocalAuthLaunch
import com.example.ui.screens.ProfileHubScreen
import com.example.ui.screens.StudentKnowledgeTreeScreen
import com.example.ui.screens.onGoogleSignInClicked
import com.example.ui.screens.signOutTwogether
import com.example.ui.theme.CocoaSecondary
import com.example.ui.theme.ConfettiBurstOverlay
import com.example.ui.theme.EspressoInk
import com.example.ui.theme.LeafGreenSoft
import com.example.ui.theme.SoftOatSurface
import com.example.ui.theme.StudentAvatarBadge
import com.example.ui.theme.StudentThemeColor
import com.example.ui.theme.SunAmberSoft
import com.example.ui.theme.TwogetherTheme
import com.example.ui.theme.WarmCreamBg
import com.example.ui.viewmodel.AppRoleMode
import com.example.ui.viewmodel.TwogetherViewModel
import com.example.util.NetworkMonitor
import com.example.util.TtsSpeaker
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

enum class StudentTab(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
    val testTag: String
) {
    BACKPACK("backpack", R.string.tab_backpack, Icons.Rounded.Backpack, "nav_tab_backpack"),
    AI_CHAT("ai_chat", R.string.tab_study_ai, Icons.Rounded.AutoAwesome, "nav_tab_ai_chat"),
    DOUBTS("doubts", R.string.tab_doubts_live, Icons.Rounded.QuestionAnswer, "nav_tab_doubts"),
    TREE("tree", R.string.tab_my_tree, Icons.Rounded.Park, "nav_tab_tree")
}

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

@Composable
fun TwogetherRootApp(auth: FirebaseAuth = Firebase.auth) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sessionStore = remember(context) { SessionStore(context) }
    val ttsSpeaker = remember(context) { TtsSpeaker(context) }
    val speakingId by ttsSpeaker.speakingId.collectAsStateWithLifecycle()

    DisposableEffect(ttsSpeaker) {
        onDispose { ttsSpeaker.shutdown() }
    }

    val currentUser by auth.authStateFlow()
        .collectAsStateWithLifecycle(initialValue = auth.currentUser)

    var sessionLoaded by remember { mutableStateOf(false) }
    var pendingRoleKey by remember { mutableStateOf<String?>(null) }
    var emailSession by remember { mutableStateOf<LocalAuthLaunch?>(null) }

    // Restore YouTube-style persistent login session completely offline before showing AuthScreen
    LaunchedEffect(sessionStore) {
        val persisted = sessionStore.getSessionOnce()
        if (persisted.isLoggedIn && persisted.workspaceUid.isNotBlank()) {
            val restoredMode = when (persisted.roleMode.uppercase()) {
                "ADMIN" -> AppRoleMode.ADMIN
                "EDUCATOR" -> AppRoleMode.EDUCATOR
                "STUDENT" -> AppRoleMode.STUDENT
                else -> AppRoleMode.PROFILE_HUB
            }
            emailSession = LocalAuthLaunch(
                workspaceUid = persisted.workspaceUid,
                roleMode = restoredMode,
                entityId = persisted.entityId.ifBlank { null }
            )
        }
        sessionLoaded = true
    }

    val activeFirebaseUid = currentUser?.uid
    val activeWorkspaceUid = activeFirebaseUid ?: emailSession?.workspaceUid

    if (!sessionLoaded) {
        TwogetherTheme(themeColorKey = StudentThemeColor.TERRACOTTA.key) {
            Surface(
                color = WarmCreamBg,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TwogetherBrandHeader(compact = true)
                }
            }
        }
    } else if (activeWorkspaceUid == null) {
        TwogetherTheme(themeColorKey = StudentThemeColor.TERRACOTTA.key) {
            AuthScreen(
                ttsSpeaker = ttsSpeaker,
                speakingId = speakingId,
                sessionStore = sessionStore,
                onAuthSuccess = { chosenRole ->
                    emailSession = null
                    if (chosenRole != null) {
                        pendingRoleKey = chosenRole
                    }
                },
                onEmailAuthSuccess = { launch ->
                    pendingRoleKey = null
                    emailSession = launch
                }
            )
        }
    } else {
        key(activeWorkspaceUid) {
            TwogetherAuthenticatedShell(
                currentUserId = activeWorkspaceUid,
                isGoogleConnected = activeFirebaseUid != null,
                sessionStore = sessionStore,
                initialLaunch = emailSession,
                initialRoleKey = pendingRoleKey,
                onConsumeInitialRole = { pendingRoleKey = null },
                onClearLocalSession = {
                    emailSession = null
                    scope.launch { sessionStore.clearSession() }
                },
                ttsSpeaker = ttsSpeaker,
                speakingId = speakingId
            )
        }
    }
}

@Composable
private fun TwogetherAuthenticatedShell(
    currentUserId: String,
    isGoogleConnected: Boolean,
    sessionStore: SessionStore,
    initialLaunch: LocalAuthLaunch?,
    initialRoleKey: String?,
    onConsumeInitialRole: () -> Unit,
    onClearLocalSession: () -> Unit,
    ttsSpeaker: TtsSpeaker,
    speakingId: String?
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val networkMonitor = remember(context) { NetworkMonitor(context) }

    DisposableEffect(networkMonitor) {
        onDispose { networkMonitor.release() }
    }

    val viewModel: TwogetherViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                TwogetherViewModel(
                    repository = TwogetherRepository(app),
                    aiService = HybridAiService(),
                    networkMonitor = networkMonitor,
                    currentUserId = currentUserId,
                    sessionStore = sessionStore
                )
            }
        }
    )

    val roleMode by viewModel.roleMode.collectAsStateWithLifecycle()
    val profiles by viewModel.studentProfiles.collectAsStateWithLifecycle()
    val activeStudent by viewModel.activeStudent.collectAsStateWithLifecycle()
    val educators by viewModel.educators.collectAsStateWithLifecycle()
    val activeEducator by viewModel.activeEducator.collectAsStateWithLifecycle()
    val isOnline by viewModel.effectiveIsOnline.collectAsStateWithLifecycle()
    val simulateOffline by viewModel.simulateOffline.collectAsStateWithLifecycle()
    val authPortalError by viewModel.authPortalError.collectAsStateWithLifecycle()
    val docVerificationState by viewModel.docVerificationState.collectAsStateWithLifecycle()

    val allLessons by viewModel.allLessons.collectAsStateWithLifecycle()
    val lessons by viewModel.filteredLessons.collectAsStateWithLifecycle()
    val lessonUnlockMap by viewModel.lessonUnlockMap.collectAsStateWithLifecycle()
    val homeworks by viewModel.homeworks.collectAsStateWithLifecycle()
    val quizAttempts by viewModel.quizAttempts.collectAsStateWithLifecycle()
    val showOnlyDownloaded by viewModel.showOnlyDownloaded.collectAsStateWithLifecycle()
    val backpackQuery by viewModel.backpackSearchQuery.collectAsStateWithLifecycle()
    val selectedSubject by viewModel.selectedSubject.collectAsStateWithLifecycle()
    val selectedGrade by viewModel.selectedGradeFilter.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguageFilter.collectAsStateWithLifecycle()
    val activeQuizLesson by viewModel.activeQuizLesson.collectAsStateWithLifecycle()
    val activeQuizQuestions by viewModel.activeQuizQuestions.collectAsStateWithLifecycle()
    val confettiTrigger by viewModel.confettiTrigger.collectAsStateWithLifecycle()

    val doubts by viewModel.doubts.collectAsStateWithLifecycle()
    val liveSessionsState by viewModel.liveSessionsState.collectAsStateWithLifecycle()
    val paperPlaneTrigger by viewModel.paperPlaneTrigger.collectAsStateWithLifecycle()

    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val streamingText by viewModel.streamingText.collectAsStateWithLifecycle()
    val streamingEngine by viewModel.streamingEngine.collectAsStateWithLifecycle()
    val isAiGenerating by viewModel.isAiGenerating.collectAsStateWithLifecycle()
    val aiError by viewModel.aiError.collectAsStateWithLifecycle()
    val preferOfflineAi by viewModel.preferOfflineAi.collectAsStateWithLifecycle()
    val geminiNanoAvailable by viewModel.geminiNanoAvailable.collectAsStateWithLifecycle()

    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val activeBannerNotification by viewModel.activeBannerNotification.collectAsStateWithLifecycle()
    val bannerMessage by viewModel.bannerMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(initialLaunch) {
        if (initialLaunch != null) {
            viewModel.applyInitialLaunch(initialLaunch.roleMode, initialLaunch.entityId)
        }
    }
    LaunchedEffect(bannerMessage) {
        val msg = bannerMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearBanner()
        }
    }

    var currentTab by remember { mutableStateOf(StudentTab.BACKPACK) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showEasyGuideDialog by remember { mutableStateOf(false) }
    val unreadNotificationsCount = notifications.count { !it.isRead }

    val openTargetTab: (AppNotificationEntity) -> Unit = { item ->
        viewModel.dismissBannerNotification()
        viewModel.markAllNotificationsRead()
        showNotificationsDialog = false
        currentTab = when (item.targetTab) {
            "doubts" -> StudentTab.DOUBTS
            "tree" -> StudentTab.TREE
            "ai_chat" -> StudentTab.AI_CHAT
            else -> StudentTab.BACKPACK
        }
    }

    val activeThemeKey = when (roleMode) {
        AppRoleMode.STUDENT -> activeStudent?.themeColorKey ?: StudentThemeColor.TERRACOTTA.key
        else -> StudentThemeColor.TERRACOTTA.key
    }

    if (roleMode != AppRoleMode.PROFILE_HUB) {
        BackHandler {
            viewModel.switchToProfileHub()
        }
    }

    TwogetherTheme(themeColorKey = activeThemeKey) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (roleMode) {
                AppRoleMode.PROFILE_HUB -> {
                    Scaffold(
                        containerColor = WarmCreamBg,
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        contentWindowInsets = WindowInsets.safeDrawing
                    ) { innerPadding ->
                        ProfileHubScreen(
                            profiles = profiles,
                            isOnline = isOnline,
                            isGoogleConnected = isGoogleConnected,
                            unreadNotificationsCount = unreadNotificationsCount,
                            authPortalError = authPortalError,
                            initialRoleKey = initialRoleKey,
                            onConsumeInitialRole = onConsumeInitialRole,
                            ttsSpeaker = ttsSpeaker,
                            speakingId = speakingId,
                            onConnectGoogle = {
                                onGoogleSignInClicked(
                                    context = context,
                                    credentialManager = credentialManager,
                                    onAuthSuccess = {
                                        onClearLocalSession()
                                        viewModel.showBanner(
                                            context.localizedForCurrentLocale()
                                                .getString(R.string.hub_google_sync_connected)
                                        )
                                    },
                                    onAuthError = { msg -> viewModel.showBanner(msg) },
                                    scope = scope
                                )
                            },
                            onLoginWithEmail = { role, email, password ->
                                currentTab = StudentTab.BACKPACK
                                viewModel.loginWithEmail(role, email, password)
                            },
                            onResetPassword = { role, email, newPw, cb ->
                                viewModel.resetPortalPassword(role, email, newPw, cb)
                            },
                            onClearAuthError = { viewModel.clearAuthPortalError() },
                            onUnlockStudent = { profile ->
                                currentTab = StudentTab.BACKPACK
                                viewModel.unlockStudentProfile(profile)
                            },
                            onRegisterStudent = { name, email, pass, grade, avatar, colorKey, lang, consent, center, pin ->
                                currentTab = StudentTab.BACKPACK
                                viewModel.registerStudentAccount(
                                    name, email, pass, grade, avatar, colorKey, lang, consent, center, pin
                                )
                            },
                            onRegisterEducator = { name, email, pass, tier, subj ->
                                viewModel.registerEducatorAccount(name, email, pass, tier, subj)
                            },
                            onRegisterAdmin = { name, email, pass ->
                                viewModel.registerAdminAccount(name, email, pass)
                            },
                            onOpenNotifications = { showNotificationsDialog = true },
                            onOpenGuide = { showEasyGuideDialog = true },
                            onSignOut = {
                                viewModel.clearPersistedSession()
                                signOutTwogether(
                                    credentialManager = credentialManager,
                                    scope = scope,
                                    onSignOutComplete = { onClearLocalSession() }
                                )
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }

                AppRoleMode.ADMIN -> {
                    Scaffold(
                        containerColor = WarmCreamBg,
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        topBar = {
                            TopRoleHeader(
                                title = stringResource(R.string.header_admin_console),
                                subtitle = stringResource(R.string.header_admin_sub),
                                avatarId = null,
                                isOnline = isOnline,
                                unreadNotificationsCount = unreadNotificationsCount,
                                onOpenNotifications = { showNotificationsDialog = true },
                                onOpenGuide = { showEasyGuideDialog = true },
                                onSwitchProfile = {
                                    viewModel.clearPersistedSession()
                                    viewModel.switchToProfileHub()
                                }
                            )
                        }
                    ) { innerPadding ->
                        AdminDashboardScreen(
                            students = profiles,
                            educators = educators,
                            lessons = allLessons,
                            doubts = doubts,
                            ttsSpeaker = ttsSpeaker,
                            speakingId = speakingId,
                            onCreateStudent = { n, e, p, g, l, c -> viewModel.adminCreateStudent(n, e, p, g, l, c) },
                            onCreateEducator = { n, e, p, t, s -> viewModel.adminCreateEducator(n, e, p, t, s) },
                            onUpdateStudent = { viewModel.adminUpdateStudent(it) },
                            onDeleteStudent = { id, name -> viewModel.adminDeleteStudent(id, name) },
                            onApproveEducator = { edu, approved -> viewModel.adminApproveEducator(edu, approved) },
                            onUpdateEducatorTier = { edu, tier -> viewModel.adminUpdateEducatorTier(edu, tier) },
                            onToggleBackupRoster = { edu, backup -> viewModel.adminToggleBackupRoster(edu, backup) },
                            onDeleteEducator = { id, name -> viewModel.adminDeleteEducator(id, name) },
                            onUpdateLessonQuality = { lesson, status -> viewModel.adminUpdateLessonQuality(lesson, status) },
                            onDeleteLesson = { viewModel.adminDeleteLesson(it) },
                            onReplyDoubtAsAdmin = { doubt, reply -> viewModel.replyToStudentDoubt(doubt, reply) },
                            onDeleteDoubt = { viewModel.adminDeleteDoubt(it) },
                            onBroadcastAlert = { t, m, c, tab -> viewModel.adminBroadcastNotification(t, m, c, tab) },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }

                AppRoleMode.EDUCATOR -> {
                    Scaffold(
                        containerColor = WarmCreamBg,
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        topBar = {
                            TopRoleHeader(
                                title = activeEducator?.fullName?.ifBlank {
                                    stringResource(R.string.header_educator_portal)
                                } ?: stringResource(R.string.header_educator_portal),
                                subtitle = stringResource(R.string.header_educator_sub),
                                avatarId = null,
                                isOnline = isOnline,
                                unreadNotificationsCount = unreadNotificationsCount,
                                onOpenNotifications = { showNotificationsDialog = true },
                                onOpenGuide = { showEasyGuideDialog = true },
                                onSwitchProfile = {
                                    viewModel.clearPersistedSession()
                                    viewModel.switchToProfileHub()
                                }
                            )
                        }
                    ) { innerPadding ->
                        EducatorPortalScreen(
                            ownerUid = currentUserId,
                            educator = activeEducator,
                            doubts = doubts,
                            docVerificationState = docVerificationState,
                            ttsSpeaker = ttsSpeaker,
                            speakingId = speakingId,
                            onSaveEducator = { viewModel.saveEducatorStep(it) },
                            onVerifyDocWithAi = { uri, edu ->
                                viewModel.verifyEducatorDocumentWithAi(context, uri, edu)
                            },
                            onPublishLesson = { edu, title, subj, grade, lang, dur, sum, notes, q, opts, corr, expl, vid ->
                                viewModel.publishEducatorLesson(
                                    edu, title, subj, grade, lang, dur, sum, notes, q, opts, corr, expl, vid
                                )
                            },
                            onAssignHomework = { edu, title, desc, subj, grade, due ->
                                viewModel.assignHomeworkAsEducator(edu, title, desc, subj, grade, due)
                            },
                            onLogTeachingSession = { hrs, count -> viewModel.logEducatorSession(hrs, count) },
                            onReplyToDoubt = { doubt, reply -> viewModel.replyToStudentDoubt(doubt, reply) },
                            onNotify = { viewModel.showBanner(it) },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }

                AppRoleMode.STUDENT -> {
                    val student = activeStudent
                    if (student == null) {
                        LaunchedEffect(Unit) {
                            viewModel.switchToProfileHub()
                        }
                    } else {
                        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                            val useRail = maxWidth >= 700.dp

                            Scaffold(
                                containerColor = WarmCreamBg,
                                snackbarHost = { SnackbarHost(snackbarHostState) },
                                topBar = {
                                    TopRoleHeader(
                                        title = stringResource(R.string.common_class_xp, student.name, student.gradeClass),
                                        subtitle = stringResource(R.string.common_xp_subtitle, student.xpPoints),
                                        avatarId = student.avatarId,
                                        isOnline = isOnline,
                                        unreadNotificationsCount = unreadNotificationsCount,
                                        onOpenNotifications = { showNotificationsDialog = true },
                                        onOpenGuide = { showEasyGuideDialog = true },
                                        onSwitchProfile = {
                                            viewModel.clearPersistedSession()
                                            viewModel.switchToProfileHub()
                                        }
                                    )
                                },
                                bottomBar = {
                                    if (!useRail) {
                                        NavigationBar(
                                            containerColor = SoftOatSurface,
                                            windowInsets = WindowInsets(0, 0, 0, 0),
                                            modifier = Modifier
                                                .windowInsetsPadding(WindowInsets.navigationBars)
                                        ) {
                                            StudentTab.entries.forEach { tab ->
                                                val tabLabel = stringResource(tab.labelRes)
                                                NavigationBarItem(
                                                    selected = currentTab == tab,
                                                    onClick = { currentTab = tab },
                                                    icon = { Icon(tab.icon, contentDescription = tabLabel) },
                                                    label = {
                                                        Text(
                                                            text = tabLabel,
                                                            style = MaterialTheme.typography.labelMedium,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    },
                                                    colors = NavigationBarItemDefaults.colors(
                                                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                                        indicatorColor = MaterialTheme.colorScheme.primary
                                                    ),
                                                    modifier = Modifier.testTag(tab.testTag)
                                                )
                                            }
                                        }
                                    }
                                }
                            ) { innerPadding ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding)
                                ) {
                                    if (useRail) {
                                        NavigationRail(
                                            containerColor = SoftOatSurface,
                                            modifier = Modifier.fillMaxHeight()
                                        ) {
                                            StudentTab.entries.forEach { tab ->
                                                val tabLabel = stringResource(tab.labelRes)
                                                NavigationRailItem(
                                                    selected = currentTab == tab,
                                                    onClick = { currentTab = tab },
                                                    icon = { Icon(tab.icon, contentDescription = tabLabel) },
                                                    label = {
                                                        Text(
                                                            text = tabLabel,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    },
                                                    modifier = Modifier.testTag(tab.testTag)
                                                )
                                            }
                                        }
                                    }

                                    Box(modifier = Modifier.weight(1f)) {
                                        when (currentTab) {
                                            StudentTab.BACKPACK -> BackpackScreen(
                                                activeStudent = student,
                                                lessons = lessons,
                                                allLessons = allLessons,
                                                lessonUnlockMap = lessonUnlockMap,
                                                homeworks = homeworks,
                                                quizAttempts = quizAttempts,
                                                notifications = notifications,
                                                showOnlyDownloaded = showOnlyDownloaded,
                                                searchQuery = backpackQuery,
                                                selectedSubject = selectedSubject,
                                                selectedGradeFilter = selectedGrade,
                                                selectedLanguageFilter = selectedLanguage,
                                                activeQuizLesson = activeQuizLesson,
                                                activeQuizQuestions = activeQuizQuestions,
                                                ttsSpeaker = ttsSpeaker,
                                                speakingId = speakingId,
                                                onToggleOnlyDownloaded = { viewModel.setShowOnlyDownloaded(it) },
                                                onSearchQueryChange = { viewModel.setBackpackSearchQuery(it) },
                                                onSubjectSelect = { viewModel.setSubjectFilter(it) },
                                                onGradeSelect = { viewModel.setGradeFilter(it) },
                                                onLanguageSelect = { viewModel.setLanguageFilter(it) },
                                                onToggleDownload = { viewModel.toggleLessonDownload(it) },
                                                onEnsureVideoReady = { l, cb -> viewModel.ensureLocalLessonReady(l, cb) },
                                                onMarkVideoWatched = { viewModel.markLessonVideoWatched(it) },
                                                onRateLesson = { l, stars -> viewModel.rateLesson(l, stars) },
                                                onStartQuiz = { viewModel.startQuizForLesson(it) },
                                                onCloseQuiz = { viewModel.closeQuiz() },
                                                onCompleteQuiz = { l, c, t -> viewModel.completeQuiz(l, c, t) },
                                                onToggleHomeworkDone = { viewModel.toggleHomeworkCompleted(it) },
                                                onOpenNotification = openTargetTab,
                                                onTriggerReminderNow = { viewModel.triggerHomeworkReminderNow() },
                                                onMarkAllNotificationsRead = { viewModel.markAllNotificationsRead() },
                                                onNotify = { viewModel.showBanner(it) }
                                            )

                                            StudentTab.AI_CHAT -> AiChatScreen(
                                                activeStudent = student,
                                                messages = chatMessages,
                                                streamingText = streamingText,
                                                streamingEngine = streamingEngine,
                                                isGenerating = isAiGenerating,
                                                aiError = aiError,
                                                isOnline = isOnline,
                                                preferOfflineAi = preferOfflineAi,
                                                geminiNanoAvailable = geminiNanoAvailable,
                                                ttsSpeaker = ttsSpeaker,
                                                speakingId = speakingId,
                                                onTogglePreferOffline = { viewModel.setPreferOfflineAi(it) },
                                                onSendQuestion = { viewModel.sendAiQuestion(it) },
                                                onClearChat = { viewModel.clearChatHistory() },
                                                onDismissError = { viewModel.clearAiError() },
                                                onNotify = { viewModel.showBanner(it) }
                                            )

                                            StudentTab.DOUBTS -> DoubtsAndLiveScreen(
                                                activeStudent = student,
                                                doubts = doubts,
                                                liveSessionsState = liveSessionsState,
                                                isOnline = isOnline,
                                                simulateOffline = simulateOffline,
                                                paperPlaneTrigger = paperPlaneTrigger,
                                                ttsSpeaker = ttsSpeaker,
                                                speakingId = speakingId,
                                                onToggleSimulateOffline = { viewModel.setSimulateOffline(it) },
                                                onSubmitDoubt = { s, q, uri -> viewModel.submitDoubt(s, q, uri) },
                                                onSyncNow = { viewModel.syncDoubtsNow() },
                                                onStartLiveSession = { t, s, g -> viewModel.startNewLiveSession(t, s, g) },
                                                onJoinLiveSession = { viewModel.joinLiveSession(it.id) },
                                                onNotify = { viewModel.showBanner(it) }
                                            )

                                            StudentTab.TREE -> StudentKnowledgeTreeScreen(
                                                activeStudent = student,
                                                lessons = allLessons,
                                                quizAttempts = quizAttempts,
                                                ttsSpeaker = ttsSpeaker,
                                                speakingId = speakingId,
                                                onCelebrateTree = { viewModel.triggerConfetti() },
                                                onAwardBonusXp = { xp, reason -> viewModel.awardStudyBonusXp(xp, reason) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            HeadsUpNotificationOverlay(
                notification = activeBannerNotification,
                ttsSpeaker = ttsSpeaker,
                speakingId = speakingId,
                onOpenNotification = openTargetTab,
                onDismiss = { viewModel.dismissBannerNotification() },
                modifier = Modifier.align(Alignment.TopCenter)
            )

            if (showNotificationsDialog) {
                NotificationsCenterDialog(
                    notifications = notifications,
                    ttsSpeaker = ttsSpeaker,
                    speakingId = speakingId,
                    onTriggerReminderNow = { viewModel.triggerHomeworkReminderNow() },
                    onNotificationClicked = openTargetTab,
                    onMarkAllRead = { viewModel.markAllNotificationsRead() },
                    onNotify = { viewModel.showBanner(it) },
                    onDismiss = { showNotificationsDialog = false }
                )
            }

            if (showEasyGuideDialog) {
                EasyUseGuideDialog(
                    ttsSpeaker = ttsSpeaker,
                    speakingId = speakingId,
                    onDismiss = { showEasyGuideDialog = false }
                )
            }

            ConfettiBurstOverlay(triggerKey = confettiTrigger)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TopRoleHeader(
    title: String,
    subtitle: String,
    avatarId: String?,
    isOnline: Boolean,
    unreadNotificationsCount: Int,
    onOpenNotifications: () -> Unit,
    onOpenGuide: () -> Unit,
    onSwitchProfile: () -> Unit
) {
    Surface(
        color = SoftOatSurface,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (avatarId != null) {
                    StudentAvatarBadge(avatarId = avatarId, size = 38.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = EspressoInk,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelMedium,
                            color = CocoaSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isOnline) LeafGreenSoft else SunAmberSoft
                        ) {
                            Text(
                                text = stringResource(
                                    if (isOnline) R.string.common_online else R.string.common_offline
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = EspressoInk,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.width(6.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ClayLanguageSelectorButton()

                IconButton(
                    onClick = onOpenNotifications,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("notifications_bell_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (unreadNotificationsCount > 0) {
                                Badge { Text("$unreadNotificationsCount") }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Notifications,
                            contentDescription = stringResource(R.string.common_cd_notifications),
                            tint = EspressoInk
                        )
                    }
                }

                IconButton(
                    onClick = onOpenGuide,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("how_to_use_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.HelpOutline,
                        contentDescription = stringResource(R.string.common_cd_quick_guide),
                        tint = CocoaSecondary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onSwitchProfile)
                        .testTag("switch_profile_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SwapHoriz,
                            contentDescription = stringResource(R.string.common_cd_switch_portal),
                            tint = EspressoInk,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            text = stringResource(R.string.common_switch),
                            style = MaterialTheme.typography.labelMedium,
                            color = EspressoInk,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
