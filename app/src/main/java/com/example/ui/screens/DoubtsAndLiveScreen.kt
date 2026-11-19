package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.PanTool
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.VideoCall
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.local.DoubtQueueEntity
import com.example.data.local.StudentProfileEntity
import com.example.data.remote.FirestoreLiveSession
import com.example.ui.components.MarkdownText
import com.example.ui.components.localizedForCurrentLocale
import com.example.ui.theme.AliveAmbientBackground
import com.example.ui.theme.AlivePulseDot
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
import com.example.ui.viewmodel.UiState
import com.example.util.TtsSpeaker
import com.example.util.VoiceInputController

@Composable
fun DoubtsAndLiveScreen(
    activeStudent: StudentProfileEntity,
    doubts: List<DoubtQueueEntity>,
    liveSessionsState: UiState<List<FirestoreLiveSession>>,
    isOnline: Boolean,
    simulateOffline: Boolean,
    paperPlaneTrigger: Int,
    ttsSpeaker: TtsSpeaker,
    speakingId: String?,
    onToggleSimulateOffline: (Boolean) -> Unit,
    onSubmitDoubt: (String, String, String?) -> Unit,
    onSyncNow: () -> Unit,
    onStartLiveSession: (String, String, String) -> Unit,
    onJoinLiveSession: (FirestoreLiveSession) -> Unit,
    onNotify: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var selectedSubject by remember { mutableStateOf("Math") }
    var doubtText by remember { mutableStateOf("") }
    var attachedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var isVoiceListening by remember { mutableStateOf(false) }
    var activeJoinedRoom by remember { mutableStateOf<FirestoreLiveSession?>(null) }
    var showCreateRoomDialog by remember { mutableStateOf(false) }

    val voiceController = remember(context) {
        VoiceInputController(
            context = context,
            onResultText = { spoken ->
                doubtText = spoken
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
            onNotify(context.localizedForCurrentLocale().getString(R.string.doubts_mic_perm_needed))
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            attachedPhotoUri = uri
            onNotify(context.localizedForCurrentLocale().getString(R.string.doubts_photo_attached))
        }
    }

    val pendingCount = doubts.count { it.syncStatus == "pending" }

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
            // Visual 3-Step Offline Doubt Workflow Banner
            item {
                AliveStaggeredEntry(index = 0) {
                    ClayCard(
                        containerColor = Color.White,
                        cornerRadius = 24.dp,
                        elevationDp = 4.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("doubts_three_step_banner")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.doubts_steps_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = EspressoInk,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OfflineDoubtStepPill(
                                    stepNumber = "1",
                                    label = stringResource(R.string.doubts_step_1),
                                    bgColor = SunAmberSoft,
                                    accentColor = SunAmber,
                                    modifier = Modifier.weight(1f)
                                )
                                OfflineDoubtStepPill(
                                    stepNumber = "2",
                                    label = stringResource(R.string.doubts_step_2),
                                    bgColor = TerracottaSoft,
                                    accentColor = TerracottaCoral,
                                    modifier = Modifier.weight(1f)
                                )
                                OfflineDoubtStepPill(
                                    stepNumber = "3",
                                    label = stringResource(R.string.doubts_step_3),
                                    bgColor = LeafGreenSoft,
                                    accentColor = LeafGreen,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Connectivity + Outbox Banner
            item {
                AliveStaggeredEntry(index = 1) {
                    ClayCard(
                        containerColor = if (isOnline) LeafGreenSoft else SunAmberSoft,
                        cornerRadius = 24.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (isOnline) Icons.Rounded.CloudDone else Icons.Rounded.CloudOff,
                                    contentDescription = null,
                                    tint = EspressoInk,
                                    modifier = Modifier.size(26.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        AlivePulseDot(color = if (isOnline) LeafGreen else SunAmber, sizeDp = 7.dp)
                                        Text(
                                            text = stringResource(
                                                if (isOnline) R.string.common_online_sync_ready
                                                else R.string.doubts_status_offline
                                            ),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = EspressoInk,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = if (pendingCount > 0) {
                                            stringResource(R.string.doubts_queued_count, pendingCount)
                                        } else {
                                            stringResource(R.string.doubts_all_synced)
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = CocoaSecondary
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    stringResource(R.string.doubts_test_offline),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = EspressoInk
                                )
                                Switch(
                                    checked = simulateOffline,
                                    onCheckedChange = onToggleSimulateOffline,
                                    modifier = Modifier.testTag("simulate_offline_switch")
                                )
                            }
                        }
                    }
                }
            }

            // Paper Airplane into Outbox Animation
            item {
                AnimatedVisibility(visible = paperPlaneTrigger > 0) {
                    PaperAirplaneOutboxCard(triggerKey = paperPlaneTrigger)
                }
            }

            // Ask Questions Card
            item {
                AliveStaggeredEntry(index = 1) {
                    ClayCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 28.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        stringResource(R.string.doubts_ask_mentors),
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = EspressoInk
                                    )
                                    Text(
                                        text = stringResource(
                                            if (isOnline) R.string.doubts_sub_online else R.string.doubts_sub_offline
                                        ),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = CocoaSecondary
                                    )
                                }
                                val askHelpSpeech = stringResource(R.string.doubts_listen_help)
                                ListenButton(
                                    isSpeaking = speakingId == "ask_help",
                                    onClick = {
                                        ttsSpeaker.toggleSpeak("ask_help", askHelpSpeech)
                                    }
                                )
                            }

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val subjects = listOf("Math", "Science", "English", "Social Studies", "Hindi")
                                items(subjects) { subject ->
                                    FilterChip(
                                        selected = selectedSubject == subject,
                                        onClick = { selectedSubject = subject },
                                        label = { Text(stringResource(subjectFilterLabelRes(subject))) },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                VoiceMicButton(
                                    isListening = isVoiceListening,
                                    testTag = "doubt_voice_mic_button",
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
                                Spacer(Modifier.width(10.dp))
                                OutlinedTextField(
                                    value = doubtText,
                                    onValueChange = { doubtText = it },
                                    placeholder = {
                                        Text(stringResource(R.string.doubts_input_placeholder, activeStudent.gradeClass))
                                    },
                                    minLines = 2,
                                    maxLines = 4,
                                    shape = RoundedCornerShape(20.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = SoftOatSurface.copy(alpha = 0.45f),
                                        unfocusedContainerColor = SoftOatSurface.copy(alpha = 0.45f),
                                        unfocusedBorderColor = Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("doubt_question_input")
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("attach_notebook_photo_button")
                                ) {
                                    Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        stringResource(
                                            if (attachedPhotoUri != null) R.string.doubts_photo_done
                                            else R.string.doubts_photo
                                        )
                                    )
                                }

                                ClayButton(
                                    text = stringResource(
                                        if (isOnline) R.string.btn_send_doubt else R.string.btn_queue_in_outbox
                                    ),
                                    enabled = doubtText.trim().length >= 3,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onSubmitDoubt(
                                            selectedSubject,
                                            doubtText,
                                            attachedPhotoUri?.toString()
                                        )
                                        doubtText = ""
                                        attachedPhotoUri = null
                                    },
                                    leadingIcon = {
                                        Icon(
                                            if (isOnline) Icons.AutoMirrored.Rounded.Send else Icons.Rounded.Inbox,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("submit_doubt_button")
                                )
                            }

                            // Draft Page 6 Safeguarding Notice
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = LeafGreenSoft.copy(alpha = 0.7f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Rounded.Shield, contentDescription = null, tint = LeafGreen, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = stringResource(R.string.doubts_safeguarding_notice),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = EspressoInk
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Live Sessions Section (Online Classroom OR Offline Notice)
            if (isOnline) {
                item {
                    ClayCard(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                        cornerRadius = 28.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        Icons.Rounded.VideoCall,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(26.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            stringResource(R.string.live_sessions_title),
                                            style = MaterialTheme.typography.titleLarge,
                                            color = EspressoInk
                                        )
                                        Text(
                                            stringResource(R.string.live_sessions_sub),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = CocoaSecondary
                                        )
                                    }
                                }
                                TextButton(onClick = { showCreateRoomDialog = true }) {
                                    Text(stringResource(R.string.live_new_room))
                                }
                            }

                            when (liveSessionsState) {
                                is UiState.Loading -> CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                is UiState.Error -> Text(liveSessionsState.message, style = MaterialTheme.typography.bodyMedium, color = CocoaSecondary)
                                is UiState.Success -> {
                                    val sessions = liveSessionsState.data
                                    if (sessions.isEmpty()) {
                                        val defaultRoomTitle = stringResource(
                                            R.string.live_default_room_title,
                                            activeStudent.gradeClass,
                                            localizedSubjectName(selectedSubject)
                                        )
                                        val defaultRoomGrade = stringResource(
                                            R.string.common_class_label,
                                            activeStudent.gradeClass
                                        )
                                        ClayButton(
                                            text = stringResource(R.string.live_start_instant),
                                            onClick = {
                                                onStartLiveSession(
                                                    defaultRoomTitle,
                                                    selectedSubject,
                                                    defaultRoomGrade
                                                )
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("start_instant_live_session_button")
                                        )
                                    } else {
                                        sessions.forEach { session ->
                                            LiveSessionRow(
                                                session = session,
                                                onJoin = {
                                                    onJoinLiveSession(session)
                                                    activeJoinedRoom = session
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                item {
                    ClayCard(
                        containerColor = SoftOatSurface,
                        cornerRadius = 24.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("live_offline_banner_card")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CloudOff,
                                contentDescription = null,
                                tint = CocoaSecondary,
                                modifier = Modifier.size(28.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.live_offline_banner),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = EspressoInk,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = stringResource(R.string.live_offline_sub),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CocoaSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Outbox & Synced Doubts
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.live_outbox_title, doubts.size),
                        style = MaterialTheme.typography.titleLarge,
                        color = EspressoInk
                    )
                    if (pendingCount > 0 && isOnline) {
                        TextButton(onClick = onSyncNow) {
                            Icon(Icons.Rounded.Sync, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.live_sync_n_now, pendingCount))
                        }
                    }
                }
            }

            if (doubts.isEmpty()) {
                item {
                    ClayCard(containerColor = SoftOatSurface, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.live_outbox_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = CocoaSecondary,
                            modifier = Modifier.padding(18.dp)
                        )
                    }
                }
            } else {
                items(doubts, key = { it.id }) { item ->
                    val localizedSubject = localizedSubjectName(item.subject)
                    val replyPart = item.educatorReply?.let {
                        stringResource(R.string.live_mentor_reply_speech, it)
                    }.orEmpty()
                    val doubtSpeech = stringResource(
                        R.string.live_doubt_speech,
                        localizedSubject,
                        item.studentName,
                        item.questionText,
                        replyPart
                    )
                    DoubtQueueCard(
                        doubt = item,
                        isSpeaking = speakingId == "doubt_${item.id}",
                        onListen = {
                            ttsSpeaker.toggleSpeak("doubt_${item.id}", doubtSpeech)
                        }
                    )
                }
            }
        }
    }

    activeJoinedRoom?.let { room ->
        LiveClassroomDialog(
            session = room,
            studentName = activeStudent.name,
            onDismiss = { activeJoinedRoom = null }
        )
    }

    if (showCreateRoomDialog) {
        CreateLiveSessionDialog(
            defaultGrade = stringResource(R.string.common_class_label, activeStudent.gradeClass),
            onDismiss = { showCreateRoomDialog = false },
            onCreate = { title, subj, grade ->
                showCreateRoomDialog = false
                onStartLiveSession(title, subj, grade)
            }
        )
    }
}

@Composable
private fun PaperAirplaneOutboxCard(triggerKey: Int) {
    val progress = remember(triggerKey) { Animatable(0f) }

    LaunchedEffect(triggerKey) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
        )
    }

    ClayCard(
        containerColor = SunAmberSoft,
        cornerRadius = 24.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("paper_airplane_outbox_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.live_airplane_flying),
                style = MaterialTheme.typography.titleMedium,
                color = EspressoInk,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(78.dp)
            ) {
                val p = progress.value
                val startX = 24.dp.toPx()
                val endX = size.width - 54.dp.toPx()
                val baseY = size.height * 0.65f

                val arcX = startX + (endX - startX) * p
                val arcY = baseY - kotlin.math.sin(p * Math.PI).toFloat() * 44.dp.toPx()

                val boxLeft = size.width - 68.dp.toPx()
                val boxTop = size.height * 0.42f
                drawRoundRect(
                    color = TerracottaCoral.copy(alpha = 0.25f),
                    topLeft = Offset(boxLeft, boxTop),
                    size = Size(54.dp.toPx(), 38.dp.toPx()),
                    cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                )
                drawRoundRect(
                    color = TerracottaCoral,
                    topLeft = Offset(boxLeft + 6.dp.toPx(), boxTop + 14.dp.toPx()),
                    size = Size(42.dp.toPx(), 20.dp.toPx()),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )

                val plane = Path().apply {
                    moveTo(arcX + 18.dp.toPx(), arcY)
                    lineTo(arcX - 10.dp.toPx(), arcY - 10.dp.toPx())
                    lineTo(arcX - 4.dp.toPx(), arcY)
                    lineTo(arcX - 10.dp.toPx(), arcY + 10.dp.toPx())
                    close()
                }
                drawPath(path = plane, color = EspressoInk)
            }
            Text(
                text = stringResource(R.string.live_airplane_sub),
                style = MaterialTheme.typography.bodyMedium,
                color = CocoaSecondary
            )
        }
    }
}

@Composable
private fun LiveSessionRow(
    session: FirestoreLiveSession,
    onJoin: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
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
                Text(session.title, style = MaterialTheme.typography.titleMedium, color = EspressoInk, fontWeight = FontWeight.Bold)
                Text(
                    text = stringResource(
                        R.string.live_session_row_sub,
                        session.educatorName,
                        session.gradeRange,
                        session.participantCount
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = CocoaSecondary
                )
            }
            Spacer(Modifier.width(10.dp))
            ClayButton(
                text = stringResource(R.string.live_join_btn),
                onClick = onJoin,
                modifier = Modifier.testTag("join_live_session_button")
            )
        }
    }
}

@Composable
private fun DoubtQueueCard(
    doubt: DoubtQueueEntity,
    isSpeaking: Boolean,
    onListen: () -> Unit
) {
    val badgeBg = when (doubt.syncStatus) {
        "answered" -> LeafGreenSoft
        "synced" -> SunAmberSoft
        else -> TerracottaSoft
    }
    val badgeText = when (doubt.syncStatus) {
        "answered" -> stringResource(R.string.doubt_badge_answered)
        "synced" -> stringResource(R.string.doubt_badge_synced)
        else -> stringResource(R.string.doubt_badge_pending)
    }

    ClayCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 24.dp) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(shape = RoundedCornerShape(12.dp), color = badgeBg) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelMedium,
                        color = EspressoInk,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Text(
                    "${localizedSubjectName(doubt.subject)} • ${stringResource(R.string.common_class_label, doubt.gradeClass)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = CocoaSecondary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(doubt.questionText, style = MaterialTheme.typography.bodyLarge, color = EspressoInk, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                ListenButton(isSpeaking = isSpeaking, onClick = onListen)
            }

            if (!doubt.educatorReply.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = LeafGreenSoft,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.doubt_mentor_reply_header),
                            style = MaterialTheme.typography.labelMedium,
                            color = LeafGreen,
                            fontWeight = FontWeight.Bold
                        )
                        MarkdownText(
                            markdown = doubt.educatorReply,
                            color = EspressoInk,
                            baseStyle = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OfflineDoubtStepPill(
    stepNumber: String,
    label: String,
    bgColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = accentColor
            ) {
                Text(
                    text = stepNumber,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = EspressoInk,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun LiveClassroomDialog(
    session: FirestoreLiveSession,
    studentName: String,
    onDismiss: () -> Unit
) {
    var handRaised by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = WarmCreamBg,
        shape = RoundedCornerShape(28.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Groups, contentDescription = null, tint = LeafGreen)
                Spacer(Modifier.width(8.dp))
                Text(session.title, style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = EspressoInk,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Rounded.VideoCall, contentDescription = null, tint = SunAmber, modifier = Modifier.size(44.dp))
                        Spacer(Modifier.height(6.dp))
                        Text(
                            stringResource(R.string.live_connected_with, session.educatorName),
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = stringResource(R.string.live_room_learners, studentName, session.participantCount),
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                ClayButton(
                    text = stringResource(if (handRaised) R.string.live_hand_raised else R.string.live_raise_hand),
                    containerColor = if (handRaised) LeafGreen else MaterialTheme.colorScheme.tertiary,
                    onClick = { handRaised = !handRaised },
                    leadingIcon = { Icon(Icons.Rounded.PanTool, contentDescription = null, tint = Color.White) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.live_leave_room)) }
        }
    )
}

@Composable
private fun CreateLiveSessionDialog(
    defaultGrade: String,
    onDismiss: () -> Unit,
    onCreate: (String, String, String) -> Unit
) {
    val defaultCircle = stringResource(R.string.live_default_circle)
    var title by remember { mutableStateOf(defaultCircle) }
    var subject by remember { mutableStateOf("Math") }
    var gradeRange by remember { mutableStateOf(defaultGrade) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = WarmCreamBg,
        shape = RoundedCornerShape(28.dp),
        title = { Text(stringResource(R.string.live_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.live_topic_title)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text(stringResource(R.string.live_subject_label)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = gradeRange,
                    onValueChange = { gradeRange = it },
                    label = { Text(stringResource(R.string.live_classes_label)) },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            ClayButton(
                text = stringResource(R.string.live_go_live),
                enabled = title.isNotBlank(),
                onClick = { onCreate(title, subject, gradeRange) }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
        }
    )
}
