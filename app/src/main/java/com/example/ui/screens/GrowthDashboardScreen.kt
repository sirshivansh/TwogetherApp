package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Draw
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Park
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.DoubtQueueEntity
import com.example.data.local.EducatorProfileEntity
import com.example.data.local.LessonEntity
import com.example.data.local.QuizAttemptEntity
import com.example.data.local.StudentProfileEntity
import com.example.ui.components.AssignHomeworkDialog
import com.example.ui.components.ChalkboardScratchpadDialog
import com.example.ui.components.DailyBrainSprintDialog
import com.example.ui.components.EducatorCertificateDialog
import com.example.ui.components.NcertFlashcardsDialog
import com.example.ui.components.StudyFocusTimerCard
import com.example.ui.components.SubjectMasteryCard
import com.example.ui.components.localizedForCurrentLocale
import com.example.ui.theme.AliveAmbientBackground
import com.example.ui.theme.AlivePulseDot
import com.example.ui.theme.AliveStaggeredEntry
import com.example.ui.theme.BronzeDark
import com.example.ui.theme.BronzeLight
import com.example.ui.theme.BronzeMid
import com.example.ui.theme.ClayButton
import com.example.ui.theme.ClayCard
import com.example.ui.theme.ClayErrorContainer
import com.example.ui.theme.CocoaSecondary
import com.example.ui.theme.ElevatedClayCard
import com.example.ui.theme.EspressoInk
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldMid
import com.example.ui.theme.LeafGreen
import com.example.ui.theme.LeafGreenSoft
import com.example.ui.theme.ListenButton
import com.example.ui.theme.SilverDark
import com.example.ui.theme.SilverLight
import com.example.ui.theme.SilverMid
import com.example.ui.theme.SoftOatSurface
import com.example.ui.theme.StudentAvatarBadge
import com.example.ui.theme.SunAmber
import com.example.ui.theme.SunAmberSoft
import com.example.ui.theme.TerracottaCoral
import com.example.ui.theme.TerracottaSoft
import com.example.ui.theme.WarmCreamBg
import com.example.ui.viewmodel.DocumentVerificationUiState
import com.example.util.ReportCardExporter
import com.example.util.TtsSpeaker
import java.util.Locale

/**
 * Student Gamification Dashboard featuring the animated growing Knowledge Tree.
 */
@Composable
fun StudentKnowledgeTreeScreen(
    activeStudent: StudentProfileEntity,
    lessons: List<LessonEntity> = emptyList(),
    quizAttempts: List<QuizAttemptEntity> = emptyList(),
    ttsSpeaker: TtsSpeaker,
    speakingId: String?,
    onCelebrateTree: () -> Unit,
    onAwardBonusXp: (Int, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var showFlashcardsDialog by remember { mutableStateOf(false) }
    var showBrainSprintDialog by remember { mutableStateOf(false) }
    var showChalkboardDialog by remember { mutableStateOf(false) }

    val subjectMasteryStats = remember(activeStudent.id, lessons, quizAttempts) {
        ReportCardExporter.computeSubjectMastery(activeStudent.id, lessons, quizAttempts)
    }

    val stage = when {
        activeStudent.xpPoints >= 400 -> 5
        activeStudent.xpPoints >= 250 -> 4
        activeStudent.xpPoints >= 140 -> 3
        activeStudent.xpPoints >= 60 -> 2
        else -> 1
    }
    val stageTitle = stringResource(
        when (stage) {
            1 -> R.string.tree_stage_1
            2 -> R.string.tree_stage_2
            3 -> R.string.tree_stage_3
            4 -> R.string.tree_stage_4
            else -> R.string.tree_stage_5
        }
    )

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
            item {
                AliveStaggeredEntry(index = 0) {
                    ClayCard(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                        cornerRadius = 28.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StudentAvatarBadge(avatarId = activeStudent.avatarId, size = 60.dp)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.tree_owner_title, activeStudent.name),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = EspressoInk
                                )
                                Text(
                                    text = "$stageTitle • ${stringResource(R.string.common_class_label, activeStudent.gradeClass)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CocoaSecondary
                                )
                            }
                            val treeSpeech = stringResource(
                                R.string.tree_speech,
                                activeStudent.name,
                                stageTitle,
                                activeStudent.xpPoints,
                                activeStudent.modulesCompleted
                            )
                            ListenButton(
                                isSpeaking = speakingId == "tree_summary",
                                onClick = {
                                    ttsSpeaker.toggleSpeak("tree_summary", treeSpeech)
                                }
                            )
                        }
                    }
                }
            }

            item {
                AliveStaggeredEntry(index = 1) {
                    ClayCard(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onCelebrateTree()
                        },
                        containerColor = Color(0xFFFFFBF0),
                        cornerRadius = 32.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("knowledge_tree_canvas_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            KnowledgeTreeCanvas(
                                stage = stage,
                                xpPoints = activeStudent.xpPoints,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = stringResource(
                                    R.string.tree_celebrate_caption,
                                    activeStudent.xpPoints,
                                    activeStudent.modulesCompleted
                                ),
                                style = MaterialTheme.typography.labelMedium,
                                color = CocoaSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Offline Study Toolkit (Flashcards, Brain Sprint, Chalkboard, Share Report Card)
            item {
                AliveStaggeredEntry(index = 2) {
                    val shareChooser = stringResource(R.string.study_report_share_chooser)
                    ClayCard(
                        containerColor = ElevatedClayCard,
                        cornerRadius = 26.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.study_toolkit_title),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = EspressoInk
                            )
                            Text(
                                text = stringResource(R.string.study_toolkit_sub),
                                style = MaterialTheme.typography.bodySmall,
                                color = CocoaSecondary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                StudyToolkitTile(
                                    title = stringResource(R.string.study_tool_flashcards),
                                    icon = Icons.Rounded.Style,
                                    containerColor = TerracottaSoft,
                                    iconTint = TerracottaCoral,
                                    onClick = { showFlashcardsDialog = true },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("open_flashcards_tile")
                                )
                                StudyToolkitTile(
                                    title = stringResource(R.string.study_tool_sprint),
                                    icon = Icons.Rounded.Bolt,
                                    containerColor = SunAmberSoft,
                                    iconTint = Color(0xFFC57B00),
                                    onClick = { showBrainSprintDialog = true },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("open_brain_sprint_tile")
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                StudyToolkitTile(
                                    title = stringResource(R.string.study_tool_chalkboard),
                                    icon = Icons.Rounded.Draw,
                                    containerColor = LeafGreenSoft,
                                    iconTint = LeafGreen,
                                    onClick = { showChalkboardDialog = true },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("open_chalkboard_tile")
                                )
                                StudyToolkitTile(
                                    title = stringResource(R.string.study_tool_report_card),
                                    icon = Icons.Rounded.Share,
                                    containerColor = SoftOatSurface,
                                    iconTint = EspressoInk,
                                    onClick = {
                                        val reportText = ReportCardExporter.buildStudentReportText(
                                            student = activeStudent,
                                            stageTitle = stageTitle,
                                            lessons = lessons,
                                            quizAttempts = quizAttempts
                                        )
                                        ReportCardExporter.sharePlainText(
                                            context = context,
                                            subject = "Twogether Progress Report - ${activeStudent.name}",
                                            body = reportText,
                                            chooserTitle = shareChooser
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("share_student_report_tile")
                                )
                            }
                        }
                    }
                }
            }

            if (subjectMasteryStats.isNotEmpty()) {
                item {
                    SubjectMasteryCard(stats = subjectMasteryStats)
                }
            }

            item {
                val focusXpReason = stringResource(R.string.study_focus_xp_reason)
                StudyFocusTimerCard(
                    onFocusCompleted = { xp -> onAwardBonusXp(xp, focusXpReason) }
                )
            }

            item {
                Text(
                    text = stringResource(R.string.tree_milestones_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = EspressoInk
                )
            }

            val milestones = listOf(
                Triple(1, R.string.tree_m1_title, R.string.tree_m1_desc),
                Triple(2, R.string.tree_m2_title, R.string.tree_m2_desc),
                Triple(3, R.string.tree_m3_title, R.string.tree_m3_desc),
                Triple(4, R.string.tree_m4_title, R.string.tree_m4_desc),
                Triple(5, R.string.tree_m5_title, R.string.tree_m5_desc)
            )

            items(milestones) { (reqStage, titleRes, descRes) ->
                val unlocked = stage >= reqStage
                ClayCard(
                    containerColor = if (unlocked) LeafGreenSoft else SoftOatSurface,
                    cornerRadius = 22.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (unlocked) Icons.Rounded.Verified else Icons.Rounded.Park,
                            contentDescription = null,
                            tint = if (unlocked) LeafGreen else CocoaSecondary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(titleRes),
                                style = MaterialTheme.typography.titleMedium,
                                color = EspressoInk,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                stringResource(descRes),
                                style = MaterialTheme.typography.bodyMedium,
                                color = CocoaSecondary
                            )
                        }
                        if (unlocked) {
                            Text(
                                stringResource(R.string.tree_grown),
                                style = MaterialTheme.typography.labelMedium,
                                color = LeafGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    if (showFlashcardsDialog) {
        NcertFlashcardsDialog(
            ttsSpeaker = ttsSpeaker,
            speakingId = speakingId,
            onEarnXp = onAwardBonusXp,
            onDismiss = { showFlashcardsDialog = false }
        )
    }

    if (showBrainSprintDialog) {
        val sprintReasonTemplate = stringResource(R.string.study_sprint_xp_reason)
        DailyBrainSprintDialog(
            onCompleteSprint = { score, total, bonusXp ->
                val reason = String.format(Locale.getDefault(), sprintReasonTemplate, score, total)
                onAwardBonusXp(bonusXp, reason)
            },
            onDismiss = { showBrainSprintDialog = false }
        )
    }

    if (showChalkboardDialog) {
        ChalkboardScratchpadDialog(
            onDismiss = { showChalkboardDialog = false }
        )
    }
}

@Composable
private fun StudyToolkitTile(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ClayCard(
        onClick = onClick,
        containerColor = containerColor,
        cornerRadius = 20.dp,
        modifier = modifier.minimumInteractiveComponentSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = EspressoInk,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun KnowledgeTreeCanvas(
    stage: Int,
    xpPoints: Int,
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "tree_sway")
    val sway by infinite.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sway"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val groundY = h * 0.86f

        drawCircle(
            color = SunAmber.copy(alpha = 0.25f),
            radius = 44.dp.toPx(),
            center = Offset(w * 0.82f, h * 0.20f)
        )
        drawCircle(
            color = SunAmber,
            radius = 26.dp.toPx(),
            center = Offset(w * 0.82f, h * 0.20f)
        )

        drawOval(
            color = LeafGreen.copy(alpha = 0.25f),
            topLeft = Offset(w * 0.1f, groundY - 12.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(w * 0.8f, 34.dp.toPx())
        )

        val trunkHeight = (46 + stage * 24).dp.toPx()
        val trunkWidth = (10 + stage * 4).dp.toPx()
        val trunkTopY = groundY - trunkHeight

        val trunkPath = Path().apply {
            moveTo(cx - trunkWidth * 0.7f, groundY)
            quadraticTo(cx - trunkWidth * 0.35f, groundY - trunkHeight * 0.5f, cx - trunkWidth * 0.25f, trunkTopY)
            lineTo(cx + trunkWidth * 0.25f, trunkTopY)
            quadraticTo(cx + trunkWidth * 0.35f, groundY - trunkHeight * 0.5f, cx + trunkWidth * 0.7f, groundY)
            close()
        }
        drawPath(trunkPath, color = Color(0xFF7B4F2B))

        if (stage >= 2) {
            drawLine(
                color = Color(0xFF7B4F2B),
                start = Offset(cx, trunkTopY + trunkHeight * 0.35f),
                end = Offset(cx - 46.dp.toPx(), trunkTopY + 12.dp.toPx()),
                strokeWidth = 7.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFF7B4F2B),
                start = Offset(cx, trunkTopY + trunkHeight * 0.28f),
                end = Offset(cx + 46.dp.toPx(), trunkTopY + 10.dp.toPx()),
                strokeWidth = 7.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        val swayPx = sway * 4.dp.toPx()
        val canopyRadius = (28 + stage * 11).dp.toPx()
        drawCircle(
            color = Color(0xFF5D9B55),
            radius = canopyRadius,
            center = Offset(cx + swayPx, trunkTopY - 8.dp.toPx())
        )
        if (stage >= 2) {
            drawCircle(
                color = Color(0xFF70B062),
                radius = canopyRadius * 0.72f,
                center = Offset(cx - 42.dp.toPx() + swayPx, trunkTopY + 6.dp.toPx())
            )
            drawCircle(
                color = Color(0xFF4D8A47),
                radius = canopyRadius * 0.72f,
                center = Offset(cx + 42.dp.toPx() + swayPx, trunkTopY + 6.dp.toPx())
            )
        }
        if (stage >= 4) {
            val blossoms = listOf(
                Offset(cx - 26.dp.toPx(), trunkTopY - 22.dp.toPx()),
                Offset(cx + 28.dp.toPx(), trunkTopY - 14.dp.toPx()),
                Offset(cx, trunkTopY - 38.dp.toPx())
            )
            blossoms.forEach { pt ->
                drawCircle(color = Color(0xFFFFD6DF), radius = 8.dp.toPx(), center = pt + Offset(swayPx, 0f))
            }
        }
        if (stage >= 3) {
            val fruits = listOf(
                Offset(cx - 32.dp.toPx(), trunkTopY + 14.dp.toPx()),
                Offset(cx + 30.dp.toPx(), trunkTopY + 10.dp.toPx()),
                Offset(cx + 8.dp.toPx(), trunkTopY - 12.dp.toPx())
            )
            val fruitCount = ((xpPoints / 70).coerceIn(1, fruits.size))
            fruits.take(fruitCount).forEach { f ->
                drawCircle(color = SunAmber, radius = 9.dp.toPx(), center = f + Offset(swayPx, 0f))
                drawCircle(
                    color = Color.White.copy(alpha = 0.7f),
                    radius = 2.5.dp.toPx(),
                    center = f + Offset(swayPx - 2.dp.toPx(), -2.dp.toPx())
                )
            }
        }
    }
}

/**
 * Educator Portal: 5-step Onboarding with Gemini Vision Document Verification + Teaching & Lesson Upload Dashboard.
 */
@Composable
fun EducatorPortalScreen(
    ownerUid: String,
    educator: EducatorProfileEntity?,
    doubts: List<DoubtQueueEntity>,
    docVerificationState: DocumentVerificationUiState,
    ttsSpeaker: TtsSpeaker,
    speakingId: String?,
    onSaveEducator: (EducatorProfileEntity) -> Unit,
    onVerifyDocWithAi: (Uri, EducatorProfileEntity) -> Unit,
    onPublishLesson: (
        EducatorProfileEntity,
        String,
        String,
        Int,
        String,
        String,
        String,
        String,
        String,
        List<String>,
        Int,
        String,
        String?
    ) -> Unit,
    onAssignHomework: (EducatorProfileEntity, String, String, String, Int, String) -> Unit = { _, _, _, _, _, _ -> },
    onLogTeachingSession: (Double, Int) -> Unit,
    onReplyToDoubt: (DoubtQueueEntity, String) -> Unit,
    onNotify: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeEducator = educator ?: EducatorProfileEntity(
        id = "edu_$ownerUid",
        ownerUid = ownerUid,
        fullName = ""
    )

    val isOnboardingDone = activeEducator.trainingCompleted &&
        activeEducator.contractSigned &&
        activeEducator.onboardingStep >= 5

    if (!isOnboardingDone) {
        EducatorOnboardingWizard(
            educator = activeEducator,
            docVerificationState = docVerificationState,
            ttsSpeaker = ttsSpeaker,
            speakingId = speakingId,
            onSaveStep = onSaveEducator,
            onVerifyDocWithAi = onVerifyDocWithAi,
            onNotify = onNotify,
            modifier = modifier
        )
    } else {
        EducatorDashboard(
            educator = activeEducator,
            doubts = doubts,
            ttsSpeaker = ttsSpeaker,
            speakingId = speakingId,
            onSaveEducator = onSaveEducator,
            onPublishLesson = onPublishLesson,
            onAssignHomework = onAssignHomework,
            onLogSession = onLogTeachingSession,
            onReplyToDoubt = onReplyToDoubt,
            modifier = modifier
        )
    }
}

@Composable
private fun EducatorOnboardingWizard(
    educator: EducatorProfileEntity,
    docVerificationState: DocumentVerificationUiState,
    ttsSpeaker: TtsSpeaker,
    speakingId: String?,
    onSaveStep: (EducatorProfileEntity) -> Unit,
    onVerifyDocWithAi: (Uri, EducatorProfileEntity) -> Unit,
    onNotify: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember(educator.onboardingStep) { mutableIntStateOf(educator.onboardingStep.coerceIn(1, 5)) }
    var fullName by remember(educator.fullName) { mutableStateOf(educator.fullName) }
    var selectedTier by remember(educator.tier) { mutableStateOf(educator.tier) }
    var board12th by remember(educator.board12th) { mutableStateOf(educator.board12th.ifBlank { "CBSE" }) }
    var rollYear by remember(educator.rollYear) { mutableStateOf(educator.rollYear.ifBlank { "2022" }) }
    var subject by remember(educator.subjectSpecialty) { mutableStateOf(educator.subjectSpecialty) }
    var referenceContact by remember(educator.referenceContact) { mutableStateOf(educator.referenceContact) }
    var docAttached by remember(educator.documentAttached) { mutableStateOf(educator.documentAttached) }
    var docUri by remember(educator.documentUri) { mutableStateOf(educator.documentUri) }

    var ans1 by remember { mutableIntStateOf(-1) }
    var ans2 by remember { mutableIntStateOf(-1) }
    var ans3 by remember { mutableIntStateOf(-1) }
    var contractChecked by remember(educator.contractSigned) { mutableStateOf(educator.contractSigned) }
    var trainingChecked1 by remember { mutableStateOf(false) }
    var trainingChecked2 by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val docPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            docAttached = true
            docUri = uri.toString()
            onNotify(context.localizedForCurrentLocale().getString(R.string.edu_photo_selected_notify))
            onVerifyDocWithAi(
                uri,
                educator.copy(
                    fullName = fullName.trim().ifBlank { educator.fullName },
                    board12th = board12th.trim().ifBlank { educator.board12th }
                )
            )
        }
    }

    val stepNames = listOf(
        stringResource(R.string.edu_step_1),
        stringResource(R.string.edu_step_2),
        stringResource(R.string.edu_step_3),
        stringResource(R.string.edu_step_4),
        stringResource(R.string.edu_step_5)
    )

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
            item {
                ClayCard(
                    containerColor = SunAmberSoft,
                    cornerRadius = 28.dp,
                    modifier = Modifier.fillMaxWidth()
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.edu_onboarding_title),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = EspressoInk
                                )
                                Text(
                                    text = stepNames[step - 1],
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            val stepSpeech = stringResource(R.string.edu_onboarding_speech, stepNames[step - 1])
                            ListenButton(
                                isSpeaking = speakingId == "edu_step_$step",
                                onClick = {
                                    ttsSpeaker.toggleSpeak("edu_step_$step", stepSpeech)
                                }
                            )
                        }
                        LinearProgressIndicator(
                            progress = { step / 5f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                }
            }

            item {
                ClayCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 28.dp
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        when (step) {
                            1 -> {
                                Text(stringResource(R.string.edu_step1_heading), style = MaterialTheme.typography.titleLarge)
                                val tiers = listOf(
                                    Triple("TIER_1_PAID", stringResource(R.string.edu_tier1_title), stringResource(R.string.edu_tier1_desc)),
                                    Triple("TIER_2_UNPAID", stringResource(R.string.edu_tier2_title), stringResource(R.string.edu_tier2_desc)),
                                    Triple("TIER_3_RETIRED", stringResource(R.string.edu_tier3_title), stringResource(R.string.edu_tier3_desc))
                                )
                                tiers.forEach { (key, title, desc) ->
                                    val selected = selectedTier == key
                                    Surface(
                                        onClick = { selectedTier = key },
                                        shape = RoundedCornerShape(18.dp),
                                        color = if (selected) MaterialTheme.colorScheme.primaryContainer else SoftOatSurface,
                                        border = BorderStroke(
                                            if (selected) 2.dp else 1.dp,
                                            if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .minimumInteractiveComponentSize()
                                            .testTag("tier_option_$key")
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            Text(desc, style = MaterialTheme.typography.bodyMedium, color = CocoaSecondary)
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = fullName,
                                    onValueChange = { fullName = it },
                                    label = { Text(stringResource(R.string.auth_full_name)) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("educator_name_input")
                                )
                                OutlinedTextField(
                                    value = board12th,
                                    onValueChange = { board12th = it },
                                    label = { Text(stringResource(R.string.edu_board_label)) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = rollYear,
                                        onValueChange = { rollYear = it },
                                        label = { Text(stringResource(R.string.edu_pass_year)) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = subject,
                                        onValueChange = { subject = it },
                                        label = { Text(stringResource(R.string.live_subject_label)) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                OutlinedTextField(
                                    value = referenceContact,
                                    onValueChange = { referenceContact = it },
                                    label = { Text(stringResource(R.string.edu_ref_contact)) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                ClayButton(
                                    text = stringResource(R.string.edu_verify_documents),
                                    enabled = fullName.trim().length >= 2,
                                    onClick = {
                                        val next = educator.copy(
                                            fullName = fullName.trim(),
                                            tier = selectedTier,
                                            board12th = board12th.trim(),
                                            rollYear = rollYear.trim(),
                                            subjectSpecialty = subject.trim(),
                                            referenceContact = referenceContact.trim(),
                                            onboardingStep = 2
                                        )
                                        onSaveStep(next)
                                        step = 2
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("edu_step1_next")
                                )
                            }

                            2 -> {
                                Text(stringResource(R.string.edu_step2_heading), style = MaterialTheme.typography.titleLarge)
                                Text(
                                    text = stringResource(R.string.edu_step2_desc),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CocoaSecondary
                                )

                                OutlinedButton(
                                    onClick = {
                                        docPicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .minimumInteractiveComponentSize()
                                        .testTag("edu_upload_doc_button")
                                ) {
                                    Icon(Icons.Rounded.UploadFile, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(
                                            if (docAttached) R.string.edu_pick_another_doc else R.string.edu_pick_doc
                                        )
                                    )
                                }

                                when (docVerificationState) {
                                    is DocumentVerificationUiState.Verifying -> {
                                        Surface(
                                            shape = RoundedCornerShape(18.dp),
                                            color = SunAmberSoft,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(14.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                                                Text(
                                                    text = stringResource(R.string.edu_analyzing_with, docVerificationState.modelName),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = EspressoInk
                                                )
                                            }
                                        }
                                    }
                                    is DocumentVerificationUiState.Verified -> {
                                        val report = docVerificationState.result
                                        val ok = report.decision == "AI_VERIFIED"
                                        Surface(
                                            shape = RoundedCornerShape(18.dp),
                                            color = if (ok) LeafGreenSoft else SunAmberSoft,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(14.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Icon(Icons.Rounded.VerifiedUser, contentDescription = null, tint = if (ok) LeafGreen else SunAmber)
                                                    Text(
                                                        text = "${report.decision} • ${report.confidenceScore}% (${report.modelUsed})",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = EspressoInk
                                                    )
                                                }
                                                Text(
                                                    text = stringResource(
                                                        R.string.edu_doc_details,
                                                        report.documentType,
                                                        report.detectedName,
                                                        report.detectedBoard
                                                    ),
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = CocoaSecondary
                                                )
                                                Text(
                                                    text = report.summary,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = EspressoInk
                                                )
                                            }
                                        }
                                    }
                                    is DocumentVerificationUiState.Failed -> {
                                        Surface(
                                            shape = RoundedCornerShape(18.dp),
                                            color = ClayErrorContainer,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = stringResource(R.string.edu_doc_queued_review, docVerificationState.message),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = EspressoInk,
                                                modifier = Modifier.padding(12.dp)
                                            )
                                        }
                                    }
                                    DocumentVerificationUiState.Idle -> {
                                        if (educator.docAiSummary.isNotBlank()) {
                                            Surface(
                                                shape = RoundedCornerShape(16.dp),
                                                color = LeafGreenSoft,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "${educator.docVerificationStatus}: ${educator.docAiSummary}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    modifier = Modifier.padding(12.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { step = 1 },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Text(stringResource(R.string.common_back))
                                    }
                                    ClayButton(
                                        text = stringResource(R.string.edu_continue_test),
                                        enabled = docAttached && docVerificationState !is DocumentVerificationUiState.Verifying,
                                        onClick = {
                                            onSaveStep(
                                                educator.copy(
                                                    documentAttached = true,
                                                    documentUri = docUri,
                                                    onboardingStep = 3
                                                )
                                            )
                                            step = 3
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("edu_step2_next")
                                    )
                                }
                            }

                            3 -> {
                                Text(stringResource(R.string.edu_step3_heading), style = MaterialTheme.typography.titleLarge)
                                Text(stringResource(R.string.edu_q1))
                                listOf(
                                    stringResource(R.string.edu_q1_o1),
                                    stringResource(R.string.edu_q1_o2)
                                ).forEachIndexed { idx, opt ->
                                    Surface(
                                        onClick = { ans1 = idx },
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (ans1 == idx) LeafGreenSoft else SoftOatSurface,
                                        modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize()
                                    ) {
                                        Text(opt, modifier = Modifier.padding(12.dp))
                                    }
                                }

                                Text(stringResource(R.string.edu_q2))
                                listOf(
                                    stringResource(R.string.edu_q2_o1),
                                    stringResource(R.string.edu_q2_o2)
                                ).forEachIndexed { idx, opt ->
                                    Surface(
                                        onClick = { ans2 = idx },
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (ans2 == idx) LeafGreenSoft else SoftOatSurface,
                                        modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize()
                                    ) {
                                        Text(opt, modifier = Modifier.padding(12.dp))
                                    }
                                }

                                Text(stringResource(R.string.edu_q3))
                                listOf("30", "45").forEachIndexed { idx, opt ->
                                    Surface(
                                        onClick = { ans3 = idx },
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (ans3 == idx) LeafGreenSoft else SoftOatSurface,
                                        modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize()
                                    ) {
                                        Text(opt, modifier = Modifier.padding(12.dp))
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { step = 2 },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Text(stringResource(R.string.common_back))
                                    }
                                    ClayButton(
                                        text = stringResource(R.string.edu_submit_assessment),
                                        enabled = ans1 >= 0 && ans2 >= 0 && ans3 >= 0,
                                        onClick = {
                                            val score = (if (ans1 == 0) 1 else 0) +
                                                (if (ans2 == 0) 1 else 0) +
                                                (if (ans3 == 0) 1 else 0)
                                            onSaveStep(educator.copy(testScore = score, onboardingStep = 4))
                                            step = 4
                                        },
                                        modifier = Modifier.weight(1f).testTag("edu_step3_next")
                                    )
                                }
                            }

                            4 -> {
                                Text(stringResource(R.string.edu_step4_heading), style = MaterialTheme.typography.titleLarge)
                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = SoftOatSurface,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = stringResource(R.string.edu_pledge_text),
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = contractChecked,
                                        onCheckedChange = { contractChecked = it },
                                        modifier = Modifier.testTag("edu_contract_checkbox")
                                    )
                                    Text(stringResource(R.string.edu_accept_contract), modifier = Modifier.weight(1f))
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { step = 3 },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Text(stringResource(R.string.common_back))
                                    }
                                    ClayButton(
                                        text = stringResource(R.string.edu_sign_continue),
                                        enabled = contractChecked,
                                        onClick = {
                                            onSaveStep(educator.copy(contractSigned = true, onboardingStep = 5))
                                            step = 5
                                        },
                                        modifier = Modifier.weight(1f).testTag("edu_step4_next")
                                    )
                                }
                            }

                            else -> {
                                Text(stringResource(R.string.edu_step5_heading), style = MaterialTheme.typography.titleLarge)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = trainingChecked1, onCheckedChange = { trainingChecked1 = it })
                                    Text(stringResource(R.string.edu_train_1), modifier = Modifier.weight(1f))
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = trainingChecked2, onCheckedChange = { trainingChecked2 = it })
                                    Text(stringResource(R.string.edu_train_2), modifier = Modifier.weight(1f))
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { step = 4 },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Text(stringResource(R.string.common_back))
                                    }
                                    ClayButton(
                                        text = stringResource(R.string.edu_open_dashboard),
                                        enabled = trainingChecked1 && trainingChecked2,
                                        onClick = {
                                            onSaveStep(
                                                educator.copy(
                                                    trainingCompleted = true,
                                                    onboardingStep = 5,
                                                    hoursTaught = educator.hoursTaught.coerceAtLeast(1.0)
                                                )
                                            )
                                        },
                                        modifier = Modifier.weight(1f).testTag("edu_step5_finish")
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EducatorDashboard(
    educator: EducatorProfileEntity,
    doubts: List<DoubtQueueEntity>,
    ttsSpeaker: TtsSpeaker,
    speakingId: String?,
    onSaveEducator: (EducatorProfileEntity) -> Unit,
    onPublishLesson: (
        EducatorProfileEntity,
        String,
        String,
        Int,
        String,
        String,
        String,
        String,
        String,
        List<String>,
        Int,
        String,
        String?
    ) -> Unit,
    onAssignHomework: (EducatorProfileEntity, String, String, String, Int, String) -> Unit,
    onLogSession: (Double, Int) -> Unit,
    onReplyToDoubt: (DoubtQueueEntity, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tierLabel = when (educator.tier) {
        "TIER_1_PAID" -> stringResource(R.string.edu_tier_banner_1)
        "TIER_3_RETIRED" -> stringResource(R.string.edu_tier_banner_3)
        else -> stringResource(R.string.edu_tier_banner_2)
    }

    val openDoubts = doubts.filter { it.syncStatus != "answered" }
    var activeReplyTarget by remember { mutableStateOf<DoubtQueueEntity?>(null) }
    var replyText by remember { mutableStateOf("") }
    var showUploadLessonDialog by remember { mutableStateOf(false) }
    var showAssignHomeworkDialog by remember { mutableStateOf(false) }
    var showCertificateDialog by remember { mutableStateOf(false) }

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
            item {
                AliveStaggeredEntry(index = 0) {
                    ClayCard(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                        cornerRadius = 28.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Image(
                                painter = painterResource(id = R.drawable.img_educator_mentor),
                                contentDescription = stringResource(R.string.edu_mentor_cd),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(20.dp))
                            )
                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val displayName = educator.fullName.ifBlank { stringResource(R.string.edu_volunteer_default) }
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = displayName,
                                            style = MaterialTheme.typography.headlineMedium,
                                            color = EspressoInk
                                        )
                                        if (educator.adminApproved || educator.docVerificationStatus == "AI_VERIFIED") {
                                            Icon(
                                                Icons.Rounded.Verified,
                                                contentDescription = stringResource(R.string.edu_cd_verified),
                                                tint = LeafGreen,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = tierLabel,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = stringResource(
                                            R.string.edu_doc_lessons_sub,
                                            educator.docVerificationStatus,
                                            educator.lessonsUploadedCount
                                        ),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = CocoaSecondary
                                    )
                                }
                                val dashSpeech = stringResource(
                                    R.string.edu_dash_speech,
                                    displayName,
                                    educator.hoursTaught,
                                    educator.doubtsResolved
                                )
                                ListenButton(
                                    isSpeaking = speakingId == "edu_dash",
                                    onClick = {
                                        ttsSpeaker.toggleSpeak("edu_dash", dashSpeech)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Metrics Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricClayTile(
                        title = stringResource(R.string.edu_total_hours_taught),
                        value = stringResource(R.string.edu_hours_value, educator.hoursTaught),
                        icon = Icons.Rounded.Schedule,
                        modifier = Modifier.weight(1f)
                    )
                    MetricClayTile(
                        title = stringResource(R.string.edu_doubts_metric),
                        value = "${educator.doubtsResolved}",
                        icon = Icons.Rounded.CheckCircle,
                        modifier = Modifier.weight(1f)
                    )
                    MetricClayTile(
                        title = stringResource(R.string.edu_lessons_metric),
                        value = "${educator.lessonsUploadedCount}",
                        icon = Icons.Rounded.VideoLibrary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Upload Learning Content + Backup Roster Continuity Card
            item {
                AliveStaggeredEntry(index = 1) {
                    ClayCard(
                        containerColor = LeafGreenSoft,
                        cornerRadius = 26.dp,
                        modifier = Modifier.fillMaxWidth()
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.edu_publish_card_title),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = EspressoInk
                                    )
                                    Text(
                                        text = stringResource(R.string.edu_publish_card_sub),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = CocoaSecondary
                                    )
                                }
                                AlivePulseDot(color = LeafGreen)
                            }
                            ClayButton(
                                text = stringResource(R.string.edu_upload_lesson),
                                containerColor = LeafGreen,
                                onClick = { showUploadLessonDialog = true },
                                leadingIcon = {
                                    Icon(Icons.Rounded.CloudUpload, contentDescription = null, tint = Color.White)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("educator_upload_lesson_button")
                            )
                            ClayButton(
                                text = stringResource(R.string.edu_assign_homework_btn),
                                containerColor = TerracottaCoral,
                                onClick = { showAssignHomeworkDialog = true },
                                leadingIcon = {
                                    Icon(Icons.AutoMirrored.Rounded.Assignment, contentDescription = null, tint = Color.White)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("educator_assign_homework_button")
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.edu_backup_roster_title),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = EspressoInk,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = stringResource(R.string.edu_backup_roster_sub),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = CocoaSecondary
                                    )
                                }
                                Switch(
                                    checked = educator.isBackupRoster,
                                    onCheckedChange = { onSaveEducator(educator.copy(isBackupRoster = it)) }
                                )
                            }
                        }
                    }
                }
            }

            // Shimmering Metallic Digital Badges (Bronze / Silver / Gold)
            item {
                AliveStaggeredEntry(index = 2) {
                    ClayCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 28.dp) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.edu_cert_badges),
                                style = MaterialTheme.typography.titleLarge,
                                color = EspressoInk
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                MetallicBadgeItem(stringResource(R.string.edu_badge_bronze), 10.0, educator.hoursTaught, BronzeLight, BronzeMid, BronzeDark)
                                MetallicBadgeItem(stringResource(R.string.edu_badge_silver), 25.0, educator.hoursTaught, SilverLight, SilverMid, SilverDark)
                                MetallicBadgeItem(stringResource(R.string.edu_badge_gold), 50.0, educator.hoursTaught, GoldLight, GoldMid, GoldDark)
                            }
                            ClayButton(
                                text = stringResource(R.string.edu_log_hours_btn),
                                onClick = { onLogSession(2.0, 3) },
                                leadingIcon = {
                                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = Color.White)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("log_teaching_hours_button")
                            )
                            OutlinedButton(
                                onClick = { showCertificateDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                                    .testTag("educator_view_certificate_button")
                            ) {
                                Icon(Icons.Rounded.WorkspacePremium, contentDescription = null, tint = GoldMid)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.edu_view_cert_btn),
                                    fontWeight = FontWeight.Bold,
                                    color = EspressoInk
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = stringResource(R.string.edu_synced_doubts_title, openDoubts.size),
                    style = MaterialTheme.typography.titleLarge,
                    color = EspressoInk
                )
            }

            if (openDoubts.isEmpty()) {
                item {
                    ClayCard(containerColor = SoftOatSurface, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.edu_no_open_doubts),
                            style = MaterialTheme.typography.bodyMedium,
                            color = CocoaSecondary,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(openDoubts, key = { it.id }) { doubt ->
                    ClayCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 22.dp) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${doubt.studentName} (${stringResource(R.string.common_class_label, doubt.gradeClass)} • ${localizedSubjectName(doubt.subject)})",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = doubt.questionText,
                                style = MaterialTheme.typography.bodyLarge,
                                color = EspressoInk
                            )
                            ClayButton(
                                text = stringResource(R.string.edu_write_answer),
                                onClick = {
                                    activeReplyTarget = doubt
                                    replyText = ""
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    activeReplyTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { activeReplyTarget = null },
            containerColor = WarmCreamBg,
            title = { Text(stringResource(R.string.edu_reply_to, target.studentName)) },
            text = {
                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    label = { Text(stringResource(R.string.edu_helpful_explanation)) },
                    minLines = 3
                )
            },
            confirmButton = {
                ClayButton(
                    text = stringResource(R.string.edu_send_reply),
                    enabled = replyText.isNotBlank(),
                    onClick = {
                        onReplyToDoubt(target, replyText)
                        activeReplyTarget = null
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { activeReplyTarget = null }) { Text(stringResource(R.string.common_cancel)) }
            }
        )
    }

    if (showUploadLessonDialog) {
        UploadLessonDialog(
            defaultSubject = educator.subjectSpecialty.ifBlank { "Math" },
            onDismiss = { showUploadLessonDialog = false },
            onPublish = { title, subj, grade, lang, dur, summary, notes, qText, opts, correct, expl, videoUri ->
                showUploadLessonDialog = false
                onPublishLesson(
                    educator,
                    title,
                    subj,
                    grade,
                    lang,
                    dur,
                    summary,
                    notes,
                    qText,
                    opts,
                    correct,
                    expl,
                    videoUri
                )
            }
        )
    }

    if (showAssignHomeworkDialog) {
        AssignHomeworkDialog(
            defaultSubject = educator.subjectSpecialty.ifBlank { "Math" },
            onDismiss = { showAssignHomeworkDialog = false },
            onAssign = { title, desc, subj, grade, due ->
                showAssignHomeworkDialog = false
                onAssignHomework(educator, title, desc, subj, grade, due)
            }
        )
    }

    if (showCertificateDialog) {
        EducatorCertificateDialog(
            educator = educator,
            onDismiss = { showCertificateDialog = false }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UploadLessonDialog(
    defaultSubject: String,
    onDismiss: () -> Unit,
    onPublish: (String, String, Int, String, String, String, String, String, List<String>, Int, String, String?) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf(if (defaultSubject.contains("Math", true)) "Math" else defaultSubject) }
    var gradeClass by remember { mutableIntStateOf(5) }
    var language by remember { mutableStateOf("Bilingual") }
    var summary by remember { mutableStateOf("") }
    var studyNotes by remember { mutableStateOf("") }
    var quizQuestion by remember { mutableStateOf("") }
    var optA by remember { mutableStateOf("") }
    var optB by remember { mutableStateOf("") }
    var optC by remember { mutableStateOf("") }
    var optD by remember { mutableStateOf("") }
    var correctIdx by remember { mutableIntStateOf(0) }
    var videoUri by remember { mutableStateOf<String?>(null) }

    val videoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                // Photo Picker grants temporary read access for the current session; copied into filesDir on publish.
            }
            videoUri = uri.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = WarmCreamBg,
        shape = RoundedCornerShape(28.dp),
        title = { Text(stringResource(R.string.edu_upload_dialog_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.edu_lesson_title)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("upload_lesson_title")
                )
                androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(listOf("Math", "Science", "English", "Social Studies", "Hindi")) { s ->
                        FilterChip(
                            selected = subject == s,
                            onClick = { subject = s },
                            label = { Text(localizedSubjectName(s)) }
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.common_class_label, gradeClass))
                    OutlinedButton(onClick = { if (gradeClass > 1) gradeClass-- }) { Text("-") }
                    OutlinedButton(onClick = { if (gradeClass < 10) gradeClass++ }) { Text("+") }
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Bilingual", "English", "Hindi").forEach { l ->
                        FilterChip(
                            selected = language == l,
                            onClick = { language = l },
                            label = { Text(localizedLessonLanguageName(l)) }
                        )
                    }
                }
                OutlinedButton(
                    onClick = {
                        videoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.VideoLibrary, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        stringResource(
                            if (videoUri != null) R.string.edu_attach_video_done else R.string.edu_attach_video
                        )
                    )
                }
                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text(stringResource(R.string.edu_short_summary)) },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = studyNotes,
                    onValueChange = { studyNotes = it },
                    label = { Text(stringResource(R.string.edu_offline_notes)) },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = quizQuestion,
                    onValueChange = { quizQuestion = it },
                    label = { Text(stringResource(R.string.edu_quick_quiz_opt)) },
                    modifier = Modifier.fillMaxWidth()
                )
                if (quizQuestion.isNotBlank()) {
                    OutlinedTextField(value = optA, onValueChange = { optA = it }, label = { Text(stringResource(R.string.edu_opt_a_correct)) }, singleLine = true)
                    OutlinedTextField(value = optB, onValueChange = { optB = it }, label = { Text(stringResource(R.string.edu_opt_b)) }, singleLine = true)
                    OutlinedTextField(value = optC, onValueChange = { optC = it }, label = { Text(stringResource(R.string.edu_opt_c)) }, singleLine = true)
                    OutlinedTextField(value = optD, onValueChange = { optD = it }, label = { Text(stringResource(R.string.edu_opt_d)) }, singleLine = true)
                }
            }
        },
        confirmButton = {
            ClayButton(
                text = stringResource(R.string.edu_publish_now),
                enabled = title.trim().length >= 3,
                onClick = {
                    val res = context.localizedForCurrentLocale()
                    onPublish(
                        title,
                        subject,
                        gradeClass,
                        language,
                        "8 min",
                        summary.ifBlank { res.getString(R.string.edu_default_summary, title) },
                        studyNotes.ifBlank { res.getString(R.string.edu_default_notes, title) },
                        quizQuestion,
                        listOf(optA, optB, optC, optD),
                        correctIdx,
                        res.getString(R.string.edu_default_expl, title),
                        videoUri
                    )
                },
                modifier = Modifier.testTag("confirm_publish_lesson_button")
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
        }
    )
}

@Composable
private fun MetricClayTile(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    ClayCard(
        containerColor = SoftOatSurface,
        cornerRadius = 22.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = EspressoInk,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = CocoaSecondary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MetallicBadgeItem(
    title: String,
    targetHours: Double,
    currentHours: Double,
    light: Color,
    mid: Color,
    dark: Color
) {
    val unlocked = currentHours >= targetHours
    val pct = (currentHours / targetHours).coerceIn(0.0, 1.0).toFloat()
    val infinite = rememberInfiniteTransition(label = "badge_shimmer_$title")
    val shimmerShift by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 140f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_offset"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(light, mid, dark),
                        start = Offset(shimmerShift, 0f),
                        end = Offset(shimmerShift + 160f, 160f)
                    )
                )
                .border(3.dp, Color.White.copy(alpha = 0.85f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.EmojiEvents,
                contentDescription = stringResource(R.string.edu_badge_cd, title),
                tint = if (unlocked) Color.White else Color.White.copy(alpha = 0.55f),
                modifier = Modifier.size(36.dp)
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = EspressoInk,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (unlocked) {
                stringResource(R.string.edu_badge_earned)
            } else {
                stringResource(R.string.edu_badge_progress, (pct * 100).toInt(), targetHours.toInt())
            },
            style = MaterialTheme.typography.labelMedium,
            color = if (unlocked) LeafGreen else CocoaSecondary
        )
    }
}
