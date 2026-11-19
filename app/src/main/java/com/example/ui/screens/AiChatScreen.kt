package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.annotation.StringRes
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.local.ChatMessageEntity
import com.example.data.local.StudentProfileEntity
import com.example.data.repository.OfflineFaqCatalog
import com.example.ui.components.MarkdownText
import com.example.ui.components.localizedForCurrentLocale
import com.example.ui.theme.AliveAmbientBackground
import com.example.ui.theme.AlivePulseDot
import com.example.ui.theme.ClayCard
import com.example.ui.theme.ClayErrorContainer
import com.example.ui.theme.CocoaSecondary
import com.example.ui.theme.EspressoInk
import com.example.ui.theme.LeafGreen
import com.example.ui.theme.LeafGreenSoft
import com.example.ui.theme.ListenButton
import com.example.ui.theme.SoftOatSurface
import com.example.ui.theme.SunAmber
import com.example.ui.theme.SunAmberSoft
import com.example.ui.theme.VoiceMicButton
import com.example.util.OfflineContentTranslator
import com.example.util.TtsSpeaker
import com.example.util.VoiceInputController

@StringRes
private fun faqCategoryLabelRes(cat: String): Int = when (cat) {
    "App Help" -> R.string.faq_cat_app_help
    "Maths" -> R.string.faq_cat_maths
    "Science" -> R.string.faq_cat_science
    "English" -> R.string.faq_cat_english
    "Civics" -> R.string.faq_cat_civics
    else -> R.string.faq_cat_all
}

private fun faqQuestionRes(faqId: String): Int? = when (faqId) {
    "faq_offline_videos" -> R.string.faq_offline_videos_q
    "faq_doubt_outbox" -> R.string.faq_doubt_outbox_q
    "faq_knowledge_tree" -> R.string.faq_knowledge_tree_q
    "faq_multi_profile" -> R.string.faq_multi_profile_q
    "faq_fractions" -> R.string.faq_fractions_q
    "faq_linear_eq" -> R.string.faq_linear_eq_q
    "faq_photosynthesis" -> R.string.faq_photosynthesis_q
    "faq_newton" -> R.string.faq_newton_q
    "faq_grammar" -> R.string.faq_grammar_q
    "faq_constitution" -> R.string.faq_constitution_q
    else -> null
}

private fun faqAnswerRes(faqId: String): Int? = when (faqId) {
    "faq_offline_videos" -> R.string.faq_offline_videos_a
    "faq_doubt_outbox" -> R.string.faq_doubt_outbox_a
    "faq_knowledge_tree" -> R.string.faq_knowledge_tree_a
    "faq_multi_profile" -> R.string.faq_multi_profile_a
    "faq_fractions" -> R.string.faq_fractions_a
    "faq_linear_eq" -> R.string.faq_linear_eq_a
    "faq_photosynthesis" -> R.string.faq_photosynthesis_a
    "faq_newton" -> R.string.faq_newton_a
    "faq_grammar" -> R.string.faq_grammar_a
    "faq_constitution" -> R.string.faq_constitution_a
    else -> null
}

@Composable
fun AiChatScreen(
    activeStudent: StudentProfileEntity,
    messages: List<ChatMessageEntity>,
    streamingText: String,
    streamingEngine: String,
    isGenerating: Boolean,
    aiError: String?,
    isOnline: Boolean,
    preferOfflineAi: Boolean,
    geminiNanoAvailable: Boolean,
    ttsSpeaker: TtsSpeaker,
    speakingId: String?,
    onTogglePreferOffline: (Boolean) -> Unit,
    onSendQuestion: (String) -> Unit,
    onClearChat: () -> Unit,
    onDismissError: () -> Unit,
    onNotify: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    var isVoiceListening by remember { mutableStateOf(false) }
    var selectedFaqCategory by remember { mutableStateOf("All") }
    var expandedFaqId by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()

    val voiceController = remember(context) {
        VoiceInputController(
            context = context,
            onResultText = { spoken ->
                inputText = spoken
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
            onNotify(context.localizedForCurrentLocale().getString(R.string.aichat_mic_perm_needed))
        }
    }

    LaunchedEffect(messages.size, streamingText) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    val activeLang = OfflineContentTranslator.currentLanguageTag()
    val isEffectiveOfflineMode = preferOfflineAi || !isOnline
    val activeEngineTitle = if (isEffectiveOfflineMode) {
        stringResource(if (geminiNanoAvailable) R.string.aichat_engine_nano else R.string.aichat_engine_offline)
    } else {
        stringResource(R.string.aichat_engine_online_simple)
    }

    val visibleFaqs = remember(selectedFaqCategory) {
        if (selectedFaqCategory == "All") OfflineFaqCatalog.faqs
        else OfflineFaqCatalog.faqs.filter { it.category == selectedFaqCategory }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        AliveAmbientBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 820.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 2-Mode Switcher
            ClayCard(
                containerColor = if (isEffectiveOfflineMode) LeafGreenSoft else SunAmberSoft,
                cornerRadius = 24.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                AlivePulseDot(
                                    color = if (isEffectiveOfflineMode) LeafGreen else SunAmber,
                                    sizeDp = 7.dp
                                )
                                Text(
                                    text = stringResource(R.string.aichat_title, activeStudent.gradeClass),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = EspressoInk,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = activeEngineTitle,
                                style = MaterialTheme.typography.labelMedium,
                                color = CocoaSecondary
                            )
                        }
                        if (messages.isNotEmpty()) {
                            IconButton(
                                onClick = onClearChat,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("aichat_clear_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = stringResource(R.string.aichat_cd_clear),
                                    tint = CocoaSecondary
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.65f))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ModeTogglePill(
                            selected = preferOfflineAi,
                            title = stringResource(R.string.aichat_offline_faqs),
                            icon = {
                                Icon(
                                    Icons.Rounded.CloudOff,
                                    contentDescription = null,
                                    tint = if (preferOfflineAi) Color.White else EspressoInk,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            selectedColor = LeafGreen,
                            onClick = { onTogglePreferOffline(true) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("mode_toggle_offline_faqs")
                        )

                        ModeTogglePill(
                            selected = !preferOfflineAi,
                            title = stringResource(R.string.aichat_online_gemini),
                            icon = {
                                Icon(
                                    Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = if (!preferOfflineAi) Color.White else EspressoInk,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            selectedColor = MaterialTheme.colorScheme.primary,
                            onClick = { onTogglePreferOffline(false) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("mode_toggle_online_gemini")
                        )
                    }

                    if (isEffectiveOfflineMode) {
                        if (!preferOfflineAi && !isOnline) {
                            Text(
                                text = stringResource(R.string.aichat_auto_offline_hint),
                                style = MaterialTheme.typography.labelMedium,
                                color = EspressoInk
                            )
                        }
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val categories = listOf("All", "App Help", "Maths", "Science", "English", "Civics")
                            items(categories) { cat ->
                                FilterChip(
                                    selected = selectedFaqCategory == cat,
                                    onClick = { selectedFaqCategory = cat },
                                    label = { Text(stringResource(faqCategoryLabelRes(cat))) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = LeafGreen,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                )
                            }
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.aichat_online_auto_note),
                            style = MaterialTheme.typography.labelMedium,
                            color = CocoaSecondary
                        )
                    }
                }
            }

            if (aiError != null) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = ClayErrorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = aiError,
                            style = MaterialTheme.typography.bodyMedium,
                            color = EspressoInk,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { onDismissError() }) {
                            Text(stringResource(R.string.common_ok))
                        }
                    }
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isEffectiveOfflineMode) {
                    item {
                        Text(
                            text = stringResource(R.string.aichat_faqs_count, visibleFaqs.size),
                            style = MaterialTheme.typography.titleMedium,
                            color = EspressoInk,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(visibleFaqs, key = { it.id }) { faq ->
                        val baseQ = faqQuestionRes(faq.id)?.let { stringResource(it) } ?: faq.question
                        val baseA = faqAnswerRes(faq.id)?.let { stringResource(it) } ?: faq.answer
                        val qText = if (activeLang in setOf("en", "hi", "mr")) baseQ else OfflineContentTranslator.translateStudyText(baseQ, activeLang)
                        val aText = if (activeLang in setOf("en", "hi", "mr")) baseA else OfflineContentTranslator.translateStudyText(baseA, activeLang)
                        val catText = stringResource(faqCategoryLabelRes(faq.category))
                        OfflineFaqCard(
                            categoryLabel = catText,
                            question = qText,
                            answer = aText,
                            expanded = expandedFaqId == faq.id,
                            isSpeaking = speakingId == faq.id,
                            onToggleExpand = {
                                expandedFaqId = if (expandedFaqId == faq.id) null else faq.id
                            },
                            onListen = {
                                ttsSpeaker.toggleSpeak(faq.id, "$qText $aText")
                            },
                            onAskInChat = { onSendQuestion(qText) }
                        )
                    }
                } else if (messages.isEmpty() && !isGenerating) {
                    item {
                        ClayCard(
                            containerColor = SoftOatSurface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.aichat_ask_header, activeStudent.name),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = EspressoInk
                                )
                                val prompts = listOf(
                                    stringResource(R.string.aichat_prompt_1),
                                    stringResource(R.string.aichat_prompt_2),
                                    stringResource(R.string.aichat_prompt_3),
                                    stringResource(R.string.aichat_prompt_4)
                                )
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(prompts) { prompt ->
                                        AssistChip(
                                            onClick = { onSendQuestion(prompt) },
                                            label = { Text(prompt) },
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier.minimumInteractiveComponentSize()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                items(messages, key = { it.id }) { msg ->
                    val isUser = msg.role == "user"
                    ChatBubbleItem(
                        text = msg.content,
                        badge = msg.engineBadge,
                        isUser = isUser,
                        isSpeaking = speakingId == msg.id,
                        onListen = { ttsSpeaker.toggleSpeak(msg.id, msg.content) }
                    )
                }

                if (isGenerating) {
                    item {
                        if (streamingText.isNotBlank()) {
                            ChatBubbleItem(
                                text = streamingText,
                                badge = streamingEngine.ifBlank { stringResource(R.string.aichat_thinking) },
                                isUser = false,
                                isSpeaking = false,
                                onListen = {}
                            )
                        } else {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.5.dp
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = stringResource(R.string.aichat_thinking),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CocoaSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Voice-First Input Bar
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 28.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VoiceMicButton(
                        isListening = isVoiceListening,
                        testTag = "aichat_voice_mic_button",
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
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                text = stringResource(R.string.aichat_placeholder_ask),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        maxLines = 3,
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SoftOatSurface.copy(alpha = 0.45f),
                            unfocusedContainerColor = SoftOatSurface.copy(alpha = 0.45f),
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("aichat_text_input")
                    )
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        onClick = {
                            val q = inputText.trim()
                            if (q.isNotBlank()) {
                                inputText = ""
                                onSendQuestion(q)
                            }
                        },
                        enabled = inputText.isNotBlank() && !isGenerating,
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(50.dp)
                            .testTag("aichat_send_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Send,
                                contentDescription = stringResource(R.string.aichat_cd_send),
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeTogglePill(
    selected: Boolean,
    title: String,
    icon: @Composable () -> Unit,
    selectedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (selected) selectedColor else Color.Transparent,
        modifier = modifier.minimumInteractiveComponentSize()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Spacer(Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) Color.White else EspressoInk,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                softWrap = true,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun OfflineFaqCard(
    categoryLabel: String,
    question: String,
    answer: String,
    expanded: Boolean,
    isSpeaking: Boolean,
    onToggleExpand: () -> Unit,
    onListen: () -> Unit,
    onAskInChat: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, LeafGreen.copy(alpha = 0.25f)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onToggleExpand)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Quiz,
                        contentDescription = null,
                        tint = LeafGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(categoryLabel, style = MaterialTheme.typography.labelMedium, color = LeafGreen)
                        Text(
                            text = question,
                            style = MaterialTheme.typography.titleMedium,
                            color = EspressoInk,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Icon(
                    imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = stringResource(
                        if (expanded) R.string.aichat_cd_collapse else R.string.aichat_cd_expand
                    ),
                    tint = CocoaSecondary
                )
            }

            if (expanded) {
                MarkdownText(
                    markdown = answer,
                    color = EspressoInk,
                    baseStyle = MaterialTheme.typography.bodyMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ListenButton(isSpeaking = isSpeaking, onClick = onListen)
                    TextButton(onClick = onAskInChat) {
                        Text(stringResource(R.string.aichat_save_in_chat))
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubbleItem(
    text: String,
    badge: String,
    isUser: Boolean,
    isSpeaking: Boolean,
    onListen: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        ClayCard(
            containerColor = if (isUser) MaterialTheme.colorScheme.primaryContainer else Color.White,
            cornerRadius = 22.dp,
            elevationDp = 4.dp,
            modifier = Modifier.widthIn(max = 520.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(badge, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                if (isUser) {
                    Text(text, style = MaterialTheme.typography.bodyLarge, color = EspressoInk)
                } else {
                    MarkdownText(
                        markdown = text,
                        color = EspressoInk,
                        baseStyle = MaterialTheme.typography.bodyLarge
                    )
                }
                if (!isUser && text.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        ListenButton(isSpeaking = isSpeaking, onClick = onListen)
                    }
                }
            }
        }
    }
}
