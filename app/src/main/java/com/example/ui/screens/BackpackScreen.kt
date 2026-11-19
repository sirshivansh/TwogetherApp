package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.rounded.Backpack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudQueue
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.R
import com.example.data.local.AppNotificationEntity
import com.example.data.local.HomeworkEntity
import com.example.data.local.LessonEntity
import com.example.data.local.QuizAttemptEntity
import com.example.data.local.QuizQuestionEntity
import com.example.data.local.StudentProfileEntity
import com.example.ui.components.MarkdownText
import com.example.ui.components.localizedForCurrentLocale
import com.example.ui.theme.AliveAmbientBackground
import com.example.ui.theme.AliveStaggeredEntry
import com.example.ui.theme.ClayButton
import com.example.ui.theme.ClayCard
import com.example.ui.theme.CocoaSecondary
import com.example.ui.theme.EspressoInk
import com.example.ui.theme.LeafGreen
import com.example.ui.theme.LeafGreenSoft
import com.example.ui.theme.ListenButton
import com.example.ui.theme.SoftOatSurface
import com.example.ui.theme.SunAmber
import com.example.ui.theme.SunAmberSoft
import com.example.ui.theme.TerracottaCoral
import com.example.ui.theme.TerracottaSoft
import com.example.ui.theme.VoiceMicButton
import com.example.ui.theme.WarmCreamBg
import com.example.ui.viewmodel.LessonProgressionTracker
import com.example.util.LessonMediaRegistry
import com.example.util.OfflineContentTranslator
import com.example.util.TtsSpeaker
import com.example.util.VoiceInputController
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class BackpackSection(
    @StringRes val labelRes: Int,
    val icon: ImageVector,
    val testTag: String
) {
    LESSONS(R.string.backpack_sec_lessons, Icons.Rounded.PlayCircle, "backpack_sec_lessons"),
    HOMEWORK(R.string.backpack_sec_homework, Icons.AutoMirrored.Rounded.Assignment, "backpack_sec_homework"),
    QUIZZES(R.string.backpack_sec_quizzes, Icons.Rounded.Quiz, "backpack_sec_quizzes"),
    ALERTS(R.string.backpack_sec_alerts, Icons.Rounded.NotificationsActive, "backpack_sec_alerts")
}

@StringRes
fun subjectFilterLabelRes(subject: String): Int = when (subject.trim().lowercase()) {
    "all" -> R.string.subject_all
    "math", "maths", "mathematics" -> R.string.subject_math
    "science" -> R.string.subject_science
    "english" -> R.string.subject_english
    "social studies" -> R.string.subject_social_studies
    "hindi" -> R.string.subject_hindi
    else -> R.string.subject_all
}

@Composable
fun localizedSubjectName(subject: String): String {
    val activeLang = OfflineContentTranslator.currentLanguageTag()
    if (activeLang != "en" && activeLang != "hi" && activeLang != "mr") {
        val translated = OfflineContentTranslator.translateSubject(subject, activeLang)
        if (translated != subject) return translated
    }
    return when (subject.trim().lowercase()) {
        "all" -> stringResource(R.string.subject_all)
        "math", "maths", "mathematics" -> stringResource(R.string.subject_math)
        "science" -> stringResource(R.string.subject_science)
        "english" -> stringResource(R.string.subject_english)
        "social studies" -> stringResource(R.string.subject_social_studies)
        "hindi" -> stringResource(R.string.subject_hindi)
        else -> subject
    }
}

@Composable
fun localizedLessonLanguageName(lang: String): String = when (lang.trim().lowercase()) {
    "all" -> stringResource(R.string.lang_all_languages)
    "bilingual" -> stringResource(R.string.lang_bilingual)
    "english" -> stringResource(R.string.lang_english)
    "hindi" -> stringResource(R.string.lang_hindi)
    else -> lang
}

@Composable
fun BackpackScreen(
    activeStudent: StudentProfileEntity,
    lessons: List<LessonEntity>,
    allLessons: List<LessonEntity> = lessons,
    lessonUnlockMap: Map<String, Boolean> = emptyMap(),
    homeworks: List<HomeworkEntity> = emptyList(),
    quizAttempts: List<QuizAttemptEntity> = emptyList(),
    notifications: List<AppNotificationEntity> = emptyList(),
    showOnlyDownloaded: Boolean,
    searchQuery: String,
    selectedSubject: String,
    selectedGradeFilter: Int?,
    selectedLanguageFilter: String,
    activeQuizLesson: LessonEntity?,
    activeQuizQuestions: List<QuizQuestionEntity>,
    ttsSpeaker: TtsSpeaker,
    speakingId: String?,
    onToggleOnlyDownloaded: (Boolean) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSubjectSelect: (String) -> Unit,
    onGradeSelect: (Int?) -> Unit,
    onLanguageSelect: (String) -> Unit,
    onToggleDownload: (LessonEntity) -> Unit,
    onEnsureVideoReady: (LessonEntity, (LessonEntity) -> Unit) -> Unit,
    onMarkVideoWatched: (LessonEntity) -> Unit = {},
    onRateLesson: (LessonEntity, Int) -> Unit,
    onStartQuiz: (LessonEntity) -> Unit,
    onCloseQuiz: () -> Unit,
    onCompleteQuiz: (LessonEntity, Int, Int) -> Unit,
    onToggleHomeworkDone: (HomeworkEntity) -> Unit = {},
    onOpenNotification: (AppNotificationEntity) -> Unit = {},
    onTriggerReminderNow: () -> Unit = {},
    onMarkAllNotificationsRead: () -> Unit = {},
    onNotify: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var activeSection by remember { mutableStateOf(BackpackSection.LESSONS) }
    var playingLesson by remember { mutableStateOf<LessonEntity?>(null) }
    var isVoiceListening by remember { mutableStateOf(false) }

    val activeLang = OfflineContentTranslator.currentLanguageTag()
    val unreadAlertsCount = notifications.count { !it.isRead }
    val pendingHwCount = homeworks.count { hw ->
        !hw.completedByProfileIds.split(",").contains(activeStudent.id)
    }

    val voiceController = remember(context) {
        VoiceInputController(
            context = context,
            onResultText = { spoken ->
                onSearchQueryChange(spoken)
                isVoiceListening = false
            },
            onStatusMessage = { msg ->
                isVoiceListening = false
                onNotify(msg)
            }
        )
    }

    DisposableEffect(voiceController) {
        onDispose { voiceController.destroy() }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            isVoiceListening = true
            voiceController.startListening()
        } else {
            onNotify(context.localizedForCurrentLocale().getString(R.string.backpack_mic_perm_needed))
        }
    }

    val lockedLessonToast = stringResource(R.string.backpack_locked_toast)

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        AliveAmbientBackground()

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 820.dp)
        ) {
            // Hero Backpack Banner
            item {
                AliveStaggeredEntry(index = 0) {
                    ClayCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                        cornerRadius = 28.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Image(
                                painter = painterResource(id = R.drawable.img_backpack_banner),
                                contentDescription = stringResource(R.string.backpack_banner_cd),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(112.dp)
                                    .clip(RoundedCornerShape(20.dp))
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Rounded.Backpack,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(R.string.backpack_student_title, activeStudent.name),
                                            style = MaterialTheme.typography.headlineMedium,
                                            color = EspressoInk
                                        )
                                    }
                                    Text(
                                        text = stringResource(
                                            R.string.backpack_header_sub,
                                            OfflineContentTranslator.nativeLanguageLabel(activeLang)
                                        ),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = CocoaSecondary
                                    )
                                }
                                val headerSpeech = stringResource(R.string.backpack_header_speech, activeStudent.name)
                                ListenButton(
                                    isSpeaking = speakingId == "backpack_header",
                                    onClick = {
                                        ttsSpeaker.toggleSpeak("backpack_header", headerSpeech)
                                    },
                                    testTag = "backpack_listen_button"
                                )
                            }
                        }
                    }
                }
            }

            // 4-Section Offline Study Hub Switcher (Lessons | Homework | Quizzes | Alerts)
            item {
                ClayCard(
                    containerColor = SoftOatSurface,
                    cornerRadius = 24.dp,
                    elevationDp = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(BackpackSection.entries) { sec ->
                            val selected = activeSection == sec
                            val badgeCount = when (sec) {
                                BackpackSection.HOMEWORK -> pendingHwCount
                                BackpackSection.ALERTS -> unreadAlertsCount
                                else -> 0
                            }
                            Surface(
                                onClick = { activeSection = sec },
                                shape = RoundedCornerShape(18.dp),
                                color = if (selected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.85f),
                                shadowElevation = if (selected) 3.dp else 0.dp,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag(sec.testTag)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = sec.icon,
                                        contentDescription = null,
                                        tint = if (selected) Color.White else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = stringResource(sec.labelRes),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (selected) Color.White else EspressoInk,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    if (badgeCount > 0) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (selected) Color.White else TerracottaCoral
                                        ) {
                                            Text(
                                                text = "$badgeCount",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (selected) MaterialTheme.colorScheme.primary else Color.White,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            when (activeSection) {
                BackpackSection.LESSONS -> {
                    // Voice-First Search Bar with Massive Microphone
                    item {
                        ClayCard(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 26.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = onSearchQueryChange,
                                    placeholder = { Text(stringResource(R.string.backpack_search_placeholder)) },
                                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(20.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = SoftOatSurface.copy(alpha = 0.45f),
                                        unfocusedContainerColor = SoftOatSurface.copy(alpha = 0.45f),
                                        unfocusedBorderColor = Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("backpack_search_input")
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                VoiceMicButton(
                                    isListening = isVoiceListening,
                                    testTag = "backpack_voice_mic_button",
                                    onClick = {
                                        if (isVoiceListening) {
                                            voiceController.stopListening()
                                            isVoiceListening = false
                                        } else {
                                            val hasPerm = ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.RECORD_AUDIO
                                            ) == PackageManager.PERMISSION_GRANTED
                                            if (hasPerm) {
                                                isVoiceListening = true
                                                voiceController.startListening()
                                            } else {
                                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Prominent "Show Only Downloaded Lessons" Toggle + Filters + Progression Legend
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            ClayCard(
                                containerColor = if (showOnlyDownloaded) LeafGreenSoft else SoftOatSurface,
                                cornerRadius = 24.dp,
                                onClick = { onToggleOnlyDownloaded(!showOnlyDownloaded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("show_only_downloaded_toggle_card")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 18.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stringResource(R.string.backpack_only_downloaded),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = EspressoInk,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = stringResource(
                                                if (showOnlyDownloaded) R.string.backpack_only_downloaded_sub_on
                                                else R.string.backpack_only_downloaded_sub_off
                                            ),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = CocoaSecondary
                                        )
                                    }
                                    Switch(
                                        checked = showOnlyDownloaded,
                                        onCheckedChange = onToggleOnlyDownloaded,
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = LeafGreen
                                        ),
                                        modifier = Modifier.testTag("show_only_downloaded_switch")
                                    )
                                }
                            }

                            // Step-by-step progression hint strip
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = SunAmberSoft.copy(alpha = 0.7f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.EmojiEvents,
                                        contentDescription = null,
                                        tint = SunAmber,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.backpack_progression_hint),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = EspressoInk,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val subjects = listOf("All", "Math", "Science", "English", "Social Studies")
                                items(subjects) { subject ->
                                    FilterChip(
                                        selected = selectedSubject == subject,
                                        onClick = { onSubjectSelect(subject) },
                                        label = { Text(localizedSubjectName(subject)) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = Color.White
                                        ),
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    )
                                }
                            }

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(listOf("All", "Bilingual", "English", "Hindi")) { lang ->
                                    FilterChip(
                                        selected = selectedLanguageFilter == lang,
                                        onClick = { onLanguageSelect(lang) },
                                        label = { Text(localizedLessonLanguageName(lang)) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = LeafGreen,
                                            selectedLabelColor = Color.White
                                        ),
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    )
                                }
                            }

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                item {
                                    FilterChip(
                                        selected = selectedGradeFilter == null,
                                        onClick = { onGradeSelect(null) },
                                        label = { Text(stringResource(R.string.backpack_all_classes)) },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    )
                                }
                                items((1..10).toList()) { grade ->
                                    val bandTag = stringResource(
                                        if (grade <= 5) R.string.backpack_band_primary else R.string.backpack_band_mid
                                    )
                                    FilterChip(
                                        selected = selectedGradeFilter == grade,
                                        onClick = { onGradeSelect(grade) },
                                        label = {
                                            Text(
                                                if (grade == activeStudent.gradeClass) {
                                                    stringResource(R.string.backpack_class_star, grade)
                                                } else {
                                                    stringResource(R.string.backpack_class_band, grade, bandTag)
                                                }
                                            )
                                        },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    )
                                }
                            }
                        }
                    }

                    if (lessons.isEmpty()) {
                        item {
                            ClayCard(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.Backpack,
                                        contentDescription = null,
                                        tint = CocoaSecondary,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Text(
                                        stringResource(R.string.backpack_empty_title),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = EspressoInk
                                    )
                                    Text(
                                        text = stringResource(R.string.backpack_empty_sub),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = CocoaSecondary
                                    )
                                }
                            }
                        }
                    } else {
                        items(lessons, key = { it.id }) { lesson ->
                            val isUnlocked = lessonUnlockMap[lesson.id] ?: true
                            val videoWatched = LessonProgressionTracker.isWatched(lesson, activeStudent.id)
                            val quizPassed = LessonProgressionTracker.isQuizPassed(lesson, activeStudent.id)
                            val displayTitle = OfflineContentTranslator.translateLessonTitle(lesson.id, lesson.title, activeLang)
                            val displaySummary = OfflineContentTranslator.translateLessonSummary(lesson.id, lesson.summary, activeLang)
                            val displayNotes = OfflineContentTranslator.translateStudyNotes(lesson.id, lesson.studyNotes, activeLang)
                            val localizedSubject = localizedSubjectName(lesson.subject)
                            val lessonSpeech = stringResource(
                                R.string.backpack_lesson_speech,
                                displayTitle,
                                lesson.gradeClass,
                                localizedSubject,
                                displaySummary,
                                displayNotes
                            )
                            LessonBackpackCard(
                                lesson = lesson,
                                displayTitle = displayTitle,
                                displaySummary = displaySummary,
                                displayNotes = displayNotes,
                                isUnlocked = isUnlocked,
                                videoWatched = videoWatched,
                                quizPassed = quizPassed,
                                isSpeaking = speakingId == "lesson_${lesson.id}",
                                onListen = {
                                    ttsSpeaker.toggleSpeak("lesson_${lesson.id}", lessonSpeech)
                                },
                                onToggleDownload = { onToggleDownload(lesson) },
                                onWatchVideo = {
                                    if (!isUnlocked) {
                                        onNotify(lockedLessonToast)
                                    } else {
                                        onEnsureVideoReady(lesson) { readyLesson ->
                                            playingLesson = readyLesson
                                        }
                                    }
                                },
                                onRateLesson = { stars -> onRateLesson(lesson, stars) },
                                onTakeQuiz = {
                                    if (!isUnlocked) {
                                        onNotify(lockedLessonToast)
                                    } else {
                                        onStartQuiz(lesson)
                                    }
                                }
                            )
                        }
                    }
                }

                BackpackSection.HOMEWORK -> {
                    item {
                        ClayCard(
                            containerColor = SunAmberSoft.copy(alpha = 0.7f),
                            cornerRadius = 22.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.backpack_hw_header),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = EspressoInk,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = stringResource(R.string.backpack_hw_sub),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CocoaSecondary
                                )
                            }
                        }
                    }

                    if (homeworks.isEmpty()) {
                        item {
                            ClayCard(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = stringResource(R.string.backpack_hw_empty),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CocoaSecondary,
                                    modifier = Modifier.padding(18.dp)
                                )
                            }
                        }
                    } else {
                        items(homeworks, key = { it.id }) { hw ->
                            val isDone = hw.completedByProfileIds.split(",").contains(activeStudent.id)
                            val hwTitle = OfflineContentTranslator.translateHomeworkTitle(hw.id, hw.title, activeLang)
                            val hwDesc = OfflineContentTranslator.translateHomeworkDescription(hw.id, hw.description, activeLang)
                            HomeworkAssignmentCard(
                                homework = hw,
                                displayTitle = hwTitle,
                                displayDescription = hwDesc,
                                isCompleted = isDone,
                                isSpeaking = speakingId == "hw_${hw.id}",
                                onListen = { ttsSpeaker.toggleSpeak("hw_${hw.id}", "$hwTitle. $hwDesc") },
                                onToggleDone = { onToggleHomeworkDone(hw) }
                            )
                        }
                    }
                }

                BackpackSection.QUIZZES -> {
                    item {
                        ClayCard(
                            containerColor = LeafGreenSoft.copy(alpha = 0.7f),
                            cornerRadius = 22.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.backpack_quizzes_header),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = EspressoInk,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = stringResource(R.string.backpack_quizzes_sub),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CocoaSecondary
                                )
                            }
                        }
                    }

                    items(allLessons, key = { "quiz_launcher_${it.id}" }) { lesson ->
                        val isUnlocked = lessonUnlockMap[lesson.id] ?: true
                        val quizPassed = LessonProgressionTracker.isQuizPassed(lesson, activeStudent.id)
                        val displayTitle = OfflineContentTranslator.translateLessonTitle(lesson.id, lesson.title, activeLang)
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = if (isUnlocked) Color.White else SoftOatSurface.copy(alpha = 0.7f),
                            border = BorderStroke(
                                1.dp,
                                if (quizPassed) LeafGreen.copy(alpha = 0.45f) else CocoaSecondary.copy(alpha = 0.14f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .alpha(if (isUnlocked) 1f else 0.72f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (!isUnlocked) {
                                            Icon(Icons.Rounded.Lock, contentDescription = null, tint = CocoaSecondary, modifier = Modifier.size(16.dp))
                                        } else if (quizPassed) {
                                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = LeafGreen, modifier = Modifier.size(18.dp))
                                        }
                                        Text(
                                            text = displayTitle,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = EspressoInk,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Text(
                                        text = "${localizedSubjectName(lesson.subject)} • ${stringResource(R.string.common_class_label, lesson.gradeClass)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = CocoaSecondary
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                ClayButton(
                                    text = stringResource(
                                        if (!isUnlocked) R.string.backpack_locked_badge
                                        else if (quizPassed) R.string.backpack_quiz_retry
                                        else R.string.btn_practice_quiz
                                    ),
                                    containerColor = if (!isUnlocked) CocoaSecondary else MaterialTheme.colorScheme.secondary,
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                                    onClick = {
                                        if (!isUnlocked) onNotify(lockedLessonToast)
                                        else onStartQuiz(lesson)
                                    },
                                    modifier = Modifier.testTag("quizzes_tab_launch_${lesson.id}")
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = stringResource(R.string.backpack_past_scores_title, quizAttempts.size),
                            style = MaterialTheme.typography.titleLarge,
                            color = EspressoInk,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }

                    if (quizAttempts.isEmpty()) {
                        item {
                            ClayCard(containerColor = SoftOatSurface, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = stringResource(R.string.backpack_past_scores_empty),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CocoaSecondary,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    } else {
                        items(quizAttempts, key = { it.id }) { attempt ->
                            QuizScoreCard(attempt = attempt, activeLang = activeLang)
                        }
                    }
                }

                BackpackSection.ALERTS -> {
                    item {
                        ClayCard(
                            containerColor = TerracottaSoft.copy(alpha = 0.7f),
                            cornerRadius = 22.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.backpack_alerts_header),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = EspressoInk,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ClayButton(
                                        text = stringResource(R.string.notif_send_reminder),
                                        onClick = onTriggerReminderNow,
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("backpack_trigger_reminder_btn")
                                    )
                                    TextButton(
                                        onClick = onMarkAllNotificationsRead,
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Text(stringResource(R.string.notif_mark_read), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    if (notifications.isEmpty()) {
                        item {
                            ClayCard(containerColor = SoftOatSurface, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = stringResource(R.string.notif_empty),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CocoaSecondary,
                                    modifier = Modifier.padding(18.dp)
                                )
                            }
                        }
                    } else {
                        items(notifications, key = { it.id }) { item ->
                            Surface(
                                onClick = { onOpenNotification(item) },
                                shape = RoundedCornerShape(22.dp),
                                color = if (item.isRead) SoftOatSurface else Color.White,
                                border = BorderStroke(
                                    1.dp,
                                    if (item.isRead) Color.Transparent else MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = EspressoInk,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = item.message,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = CocoaSecondary
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    ListenButton(
                                        isSpeaking = speakingId == "alert_${item.id}",
                                        onClick = {
                                            ttsSpeaker.toggleSpeak("alert_${item.id}", "${item.title}. ${item.message}")
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    playingLesson?.let { lesson ->
        val displayTitle = OfflineContentTranslator.translateLessonTitle(lesson.id, lesson.title, activeLang)
        val displaySummary = OfflineContentTranslator.translateLessonSummary(lesson.id, lesson.summary, activeLang)
        val displayNotes = OfflineContentTranslator.translateStudyNotes(lesson.id, lesson.studyNotes, activeLang)
        ExoPlayerLessonDialog(
            lesson = lesson,
            displayTitle = displayTitle,
            displaySummary = displaySummary,
            displayNotes = displayNotes,
            ttsSpeaker = ttsSpeaker,
            speakingId = speakingId,
            onVideoWatched = { onMarkVideoWatched(lesson) },
            onDismiss = { playingLesson = null },
            onLaunchQuiz = {
                playingLesson = null
                onStartQuiz(lesson)
            }
        )
    }

    if (activeQuizLesson != null && activeQuizQuestions.isNotEmpty()) {
        OfflineQuizDialog(
            lesson = activeQuizLesson,
            questions = activeQuizQuestions,
            activeLang = activeLang,
            ttsSpeaker = ttsSpeaker,
            speakingId = speakingId,
            onDismiss = onCloseQuiz,
            onComplete = { correct, total ->
                onCompleteQuiz(activeQuizLesson, correct, total)
                onCloseQuiz()
            }
        )
    }
}

@kotlin.OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HomeworkAssignmentCard(
    homework: HomeworkEntity,
    displayTitle: String,
    displayDescription: String,
    isCompleted: Boolean,
    isSpeaking: Boolean,
    onListen: () -> Unit,
    onToggleDone: () -> Unit
) {
    ClayCard(
        containerColor = if (isCompleted) LeafGreenSoft.copy(alpha = 0.65f) else Color.White,
        cornerRadius = 24.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("homework_card_${homework.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FlowRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                        Text(
                            text = stringResource(
                                R.string.backpack_badge_class_subject,
                                homework.gradeClass,
                                localizedSubjectName(homework.subject)
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = EspressoInk,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        )
                    }
                    Surface(shape = RoundedCornerShape(12.dp), color = SunAmberSoft) {
                        Text(
                            text = homework.dueDateLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = EspressoInk,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        )
                    }
                    if (isCompleted) {
                        Surface(shape = RoundedCornerShape(12.dp), color = LeafGreenSoft) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (homework.syncStatus == "synced") Icons.Rounded.CloudDone else Icons.Rounded.CloudQueue,
                                    contentDescription = null,
                                    tint = LeafGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = stringResource(
                                        if (homework.syncStatus == "synced") R.string.backpack_hw_synced_badge
                                        else R.string.backpack_hw_queued_badge
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EspressoInk,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                ListenButton(isSpeaking = isSpeaking, onClick = onListen)
            }

            Text(
                text = displayTitle,
                style = MaterialTheme.typography.titleLarge,
                color = EspressoInk,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = displayDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = CocoaSecondary
            )

            ClayButton(
                text = stringResource(if (isCompleted) R.string.backpack_hw_done_btn else R.string.backpack_hw_mark_done_btn),
                containerColor = if (isCompleted) LeafGreen else MaterialTheme.colorScheme.primary,
                onClick = onToggleDone,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("homework_toggle_${homework.id}")
            )
        }
    }
}

@Composable
private fun QuizScoreCard(
    attempt: QuizAttemptEntity,
    activeLang: String
) {
    val dateLabel = remember(attempt.attemptedAtMillis) {
        SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(attempt.attemptedAtMillis))
    }
    val displayTitle = OfflineContentTranslator.translateLessonTitle(attempt.lessonId, attempt.lessonTitle, activeLang)
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, if (attempt.passed) LeafGreen.copy(alpha = 0.4f) else SunAmber.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = EspressoInk,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${localizedSubjectName(attempt.subject)} • $dateLabel",
                    style = MaterialTheme.typography.labelMedium,
                    color = CocoaSecondary
                )
            }
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (attempt.passed) LeafGreenSoft else SunAmberSoft
            ) {
                Text(
                    text = stringResource(
                        if (attempt.passed) R.string.backpack_score_passed else R.string.backpack_score_retry,
                        attempt.score,
                        attempt.total
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = EspressoInk,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@kotlin.OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LessonBackpackCard(
    lesson: LessonEntity,
    displayTitle: String,
    displaySummary: String,
    displayNotes: String,
    isUnlocked: Boolean,
    videoWatched: Boolean,
    quizPassed: Boolean,
    isSpeaking: Boolean,
    onListen: () -> Unit,
    onToggleDownload: () -> Unit,
    onWatchVideo: () -> Unit,
    onRateLesson: (Int) -> Unit,
    onTakeQuiz: () -> Unit
) {
    var notesExpanded by remember(lesson.id) { mutableStateOf(false) }
    var userStars by remember(lesson.id) { mutableIntStateOf(0) }

    ClayCard(
        containerColor = if (isUnlocked) Color.White else SoftOatSurface.copy(alpha = 0.8f),
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isUnlocked) 1f else 0.78f)
            .testTag("lesson_card_${lesson.id}"),
        cornerRadius = 26.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FlowRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = stringResource(
                                R.string.backpack_badge_class_subject,
                                lesson.gradeClass,
                                localizedSubjectName(lesson.subject)
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = EspressoInk,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SoftOatSurface
                    ) {
                        Text(
                            text = localizedLessonLanguageName(lesson.language),
                            style = MaterialTheme.typography.labelMedium,
                            color = CocoaSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (!isUnlocked) TerracottaSoft else if (lesson.downloaded) LeafGreenSoft else SunAmberSoft
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (!isUnlocked) {
                                Icon(Icons.Rounded.Lock, contentDescription = null, tint = EspressoInk, modifier = Modifier.size(13.dp))
                            }
                            Text(
                                text = when {
                                    !isUnlocked -> stringResource(R.string.backpack_locked_badge)
                                    lesson.downloaded -> stringResource(R.string.backpack_badge_offline_ready)
                                    else -> lesson.durationLabel
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = EspressoInk,
                                fontWeight = if (!isUnlocked) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
                if (videoWatched && quizPassed) {
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        Icons.Rounded.CheckCircle,
                        contentDescription = stringResource(R.string.backpack_cd_quiz_completed),
                        tint = LeafGreen
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(displayTitle, style = MaterialTheme.typography.titleLarge, color = EspressoInk)
                    Text(
                        text = "${lesson.uploadedByEducatorName} • ${lesson.feedbackRating} ★",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(displaySummary, style = MaterialTheme.typography.bodyMedium, color = CocoaSecondary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                ListenButton(
                    isSpeaking = isSpeaking,
                    onClick = onListen,
                    testTag = "listen_lesson_${lesson.id}"
                )
            }

            // Step-by-Step Progression Strip
            if (!isUnlocked) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = TerracottaSoft.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.Lock, contentDescription = null, tint = TerracottaCoral, modifier = Modifier.size(16.dp))
                        Text(
                            text = stringResource(R.string.backpack_locked_reason),
                            style = MaterialTheme.typography.labelMedium,
                            color = EspressoInk,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (videoWatched) LeafGreenSoft else SoftOatSurface,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(
                                if (videoWatched) R.string.backpack_step_video_done else R.string.backpack_step_video_todo
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = EspressoInk,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (quizPassed) LeafGreenSoft else SoftOatSurface,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(
                                if (quizPassed) R.string.backpack_step_quiz_done else R.string.backpack_step_quiz_todo
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = EspressoInk,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Expandable Pre-downloaded Offline Study Notes
            if (isUnlocked && displayNotes.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SoftOatSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { notesExpanded = !notesExpanded }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Rounded.Description, contentDescription = null, tint = CocoaSecondary, modifier = Modifier.size(16.dp))
                                Text(
                                    stringResource(R.string.backpack_study_notes),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = EspressoInk
                                )
                            }
                            Icon(
                                imageVector = if (notesExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                contentDescription = stringResource(R.string.backpack_cd_toggle_notes),
                                tint = CocoaSecondary
                            )
                        }
                        AnimatedVisibility(visible = notesExpanded) {
                            MarkdownText(
                                markdown = displayNotes,
                                color = EspressoInk,
                                baseStyle = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                }
            }

            // Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ClayButton(
                    text = stringResource(if (isUnlocked) R.string.btn_watch_video else R.string.backpack_locked_badge),
                    containerColor = if (isUnlocked) MaterialTheme.colorScheme.primary else CocoaSecondary,
                    onClick = onWatchVideo,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = if (isUnlocked) Icons.Rounded.PlayArrow else Icons.Rounded.Lock,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("watch_video_${lesson.id}")
                )

                ClayButton(
                    text = stringResource(R.string.btn_practice_quiz),
                    onClick = onTakeQuiz,
                    containerColor = if (isUnlocked) MaterialTheme.colorScheme.secondary else CocoaSecondary.copy(alpha = 0.7f),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = if (isUnlocked) Icons.Rounded.Quiz else Icons.Rounded.Lock,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quiz_button_${lesson.id}")
                )

                Surface(
                    onClick = onToggleDownload,
                    shape = RoundedCornerShape(18.dp),
                    color = if (lesson.downloaded) LeafGreenSoft else SoftOatSurface,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("download_toggle_${lesson.id}")
                ) {
                    Icon(
                        imageVector = if (lesson.downloaded) Icons.Rounded.CheckCircle else Icons.Rounded.Download,
                        contentDescription = stringResource(
                            if (lesson.downloaded) R.string.backpack_cd_saved_offline else R.string.backpack_cd_save_offline
                        ),
                        tint = if (lesson.downloaded) LeafGreen else EspressoInk,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            if (isUnlocked) {
                // Quality Feedback Stars
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.backpack_rate_clarity),
                        style = MaterialTheme.typography.labelMedium,
                        color = CocoaSecondary
                    )
                    Row {
                        (1..5).forEach { star ->
                            IconButton(
                                onClick = {
                                    userStars = star
                                    onRateLesson(star)
                                },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(
                                    imageVector = if (star <= userStars) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                                    contentDescription = stringResource(R.string.backpack_cd_rate_stars, star),
                                    tint = if (star <= userStars) SunAmber else CocoaSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun ExoPlayerLessonDialog(
    lesson: LessonEntity,
    displayTitle: String,
    displaySummary: String,
    displayNotes: String,
    ttsSpeaker: TtsSpeaker,
    speakingId: String?,
    onVideoWatched: () -> Unit,
    onDismiss: () -> Unit,
    onLaunchQuiz: () -> Unit
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(true) }
    var markedWatched by remember(lesson.id) { mutableStateOf(false) }

    val exoPlayer = remember(lesson.id, lesson.localFilePath) {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()
        ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()
            .apply {
                val file = lesson.localFilePath?.let { File(it) }
                val uri = when {
                    file != null && file.exists() && file.length() > 0L -> Uri.fromFile(file)
                    !lesson.localFilePath.isNullOrBlank() && lesson.localFilePath.startsWith("content://") ->
                        Uri.parse(lesson.localFilePath)
                    else -> {
                        val rawId = LessonMediaRegistry.resolveRawResId(lesson.rawResName)
                        Uri.parse("android.resource://${context.packageName}/$rawId")
                    }
                }
                setMediaItem(MediaItem.fromUri(uri))
                prepare()
                playWhenReady = true
            }
    }

    LaunchedEffect(lesson.id) {
        if (!markedWatched) {
            markedWatched = true
            onVideoWatched()
        }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
                if (playing && !markedWatched) {
                    markedWatched = true
                    onVideoWatched()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.w("ExoPlayerLessonDialog", "Playback error for ${lesson.localFilePath}, falling back to bundled offline clip", error)
                val rawId = LessonMediaRegistry.resolveRawResId(lesson.rawResName)
                val fallbackUri = Uri.parse("android.resource://${context.packageName}/$rawId")
                exoPlayer.setMediaItem(MediaItem.fromUri(fallbackUri))
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        ClayCard(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp)
                .padding(horizontal = 16.dp),
            cornerRadius = 28.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = displayTitle,
                            style = MaterialTheme.typography.titleLarge,
                            color = EspressoInk
                        )
                        Text(
                            text = stringResource(R.string.backpack_video_watched_recorded),
                            style = MaterialTheme.typography.labelMedium,
                            color = LeafGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.player_cd_close))
                    }
                }

                // Inflated XML PlayerView with app:surface_type="texture_view" so Compose Dialog
                // always composites full video frames + audio with zero black-screen artifacts.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black)
                ) {
                    AndroidView(
                        factory = { ctx ->
                            val playerView = LayoutInflater.from(ctx)
                                .inflate(R.layout.view_exo_texture_player, null) as PlayerView
                            playerView.layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                            playerView.keepScreenOn = true
                            playerView.player = exoPlayer
                            playerView.useController = true
                            playerView
                        },
                        update = { playerView ->
                            if (playerView.player !== exoPlayer) {
                                playerView.player = exoPlayer
                            }
                        },
                        onRelease = { playerView ->
                            playerView.player = null
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { exoPlayer.seekTo((exoPlayer.currentPosition - 10_000L).coerceAtLeast(0L)) },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(Icons.Rounded.Replay10, contentDescription = stringResource(R.string.player_cd_rewind))
                    }

                    ClayButton(
                        text = stringResource(if (isPlaying) R.string.player_pause else R.string.player_play),
                        onClick = {
                            if (exoPlayer.isPlaying) {
                                exoPlayer.pause()
                                isPlaying = false
                            } else {
                                exoPlayer.play()
                                isPlaying = true
                            }
                        },
                        leadingIcon = {
                            Icon(
                                if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    )

                    IconButton(
                        onClick = { exoPlayer.seekTo(exoPlayer.currentPosition + 10_000L) },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(Icons.Rounded.Forward10, contentDescription = stringResource(R.string.player_cd_forward))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ListenButton(
                        isSpeaking = speakingId == "player_${lesson.id}",
                        onClick = {
                            val script = LessonMediaRegistry.buildVoiceoverScript(
                                lesson = lesson,
                                localizedTitle = displayTitle,
                                localizedSummary = displaySummary
                            )
                            ttsSpeaker.toggleSpeak("player_${lesson.id}", script)
                        },
                        label = stringResource(R.string.player_read_notes)
                    )
                    ClayButton(
                        text = stringResource(R.string.btn_practice_quiz),
                        containerColor = MaterialTheme.colorScheme.secondary,
                        onClick = onLaunchQuiz,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("player_to_quiz_button")
                    )
                }
            }
        }
    }
}

@Composable
private fun OfflineQuizDialog(
    lesson: LessonEntity,
    questions: List<QuizQuestionEntity>,
    activeLang: String,
    ttsSpeaker: TtsSpeaker,
    speakingId: String?,
    onDismiss: () -> Unit,
    onComplete: (Int, Int) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableIntStateOf(-1) }
    var checked by remember { mutableStateOf(false) }
    var correctCount by remember { mutableIntStateOf(0) }

    val q = questions[currentIndex]
    val displayLessonTitle = OfflineContentTranslator.translateLessonTitle(lesson.id, lesson.title, activeLang)
    val displayPrompt = OfflineContentTranslator.translateQuizQuestion(q.id, q.questionText, activeLang)
    val displayExplanation = OfflineContentTranslator.translateQuizExplanation(q.id, q.explanation, activeLang)
    val options = listOf(q.optionA, q.optionB, q.optionC, q.optionD)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = WarmCreamBg,
        shape = RoundedCornerShape(28.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.quiz_title_progress, currentIndex + 1, questions.size),
                        style = MaterialTheme.typography.titleLarge,
                        color = EspressoInk
                    )
                    Text(displayLessonTitle, style = MaterialTheme.typography.labelMedium, color = CocoaSecondary)
                }
                val quizSpeech = stringResource(
                    R.string.quiz_speech,
                    displayPrompt,
                    q.optionA,
                    q.optionB,
                    q.optionC,
                    q.optionD
                )
                ListenButton(
                    isSpeaking = speakingId == "quiz_${q.id}",
                    onClick = {
                        ttsSpeaker.toggleSpeak("quiz_${q.id}", quizSpeech)
                    }
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(displayPrompt, style = MaterialTheme.typography.titleMedium, color = EspressoInk, fontWeight = FontWeight.Bold)

                options.forEachIndexed { idx, opt ->
                    val isSelected = selectedOption == idx
                    val isCorrectOption = idx == q.correctIndex
                    val bgColor = when {
                        !checked && isSelected -> MaterialTheme.colorScheme.primaryContainer
                        checked && isCorrectOption -> LeafGreenSoft
                        checked && isSelected && !isCorrectOption -> TerracottaSoft
                        else -> SoftOatSurface
                    }
                    Surface(
                        onClick = {
                            if (!checked) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedOption = idx
                            }
                        },
                        shape = RoundedCornerShape(18.dp),
                        color = bgColor,
                        border = BorderStroke(
                            width = if (isSelected || (checked && isCorrectOption)) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("quiz_option_$idx")
                    ) {
                        Text(opt, style = MaterialTheme.typography.bodyLarge, color = EspressoInk, modifier = Modifier.padding(14.dp))
                    }
                }

                if (checked) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (selectedOption == q.correctIndex) LeafGreenSoft else SunAmberSoft
                    ) {
                        Text(
                            text = stringResource(
                                if (selectedOption == q.correctIndex) R.string.quiz_feedback_correct else R.string.quiz_feedback_retry,
                                displayExplanation
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = EspressoInk,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (!checked) {
                ClayButton(
                    text = stringResource(R.string.quiz_check_answer),
                    enabled = selectedOption in 0..3,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        checked = true
                        if (selectedOption == q.correctIndex) correctCount++
                    },
                    modifier = Modifier.testTag("quiz_check_button")
                )
            } else {
                ClayButton(
                    text = stringResource(
                        if (currentIndex == questions.lastIndex) R.string.quiz_finish_tree else R.string.quiz_next_question
                    ),
                    onClick = {
                        if (currentIndex == questions.lastIndex) {
                            onComplete(correctCount, questions.size)
                        } else {
                            currentIndex++
                            selectedOption = -1
                            checked = false
                        }
                    },
                    modifier = Modifier.testTag("quiz_next_button")
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_close)) }
        }
    )
}
