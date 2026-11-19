package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Draw
import androidx.compose.material.icons.rounded.Flip
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.local.QuizQuestionEntity
import com.example.data.local.SeedData
import com.example.ui.theme.ClayButton
import com.example.ui.theme.ClayCard
import com.example.ui.theme.ClayErrorContainer
import com.example.ui.theme.CocoaSecondary
import com.example.ui.theme.ElevatedClayCard
import com.example.ui.theme.EspressoInk
import com.example.ui.theme.LeafGreen
import com.example.ui.theme.LeafGreenSoft
import com.example.ui.theme.ListenButton
import com.example.ui.theme.SoftOatSurface
import com.example.ui.theme.SunAmber
import com.example.ui.theme.SunAmberSoft
import com.example.ui.theme.TerracottaCoral
import com.example.ui.theme.TerracottaSoft
import com.example.ui.theme.WarmCreamBg
import com.example.util.SubjectMasteryStat
import com.example.util.TtsSpeaker
import kotlinx.coroutines.delay
import java.util.Locale

data class NcertFlashcard(
    val id: String,
    val subject: String,
    val gradeClass: Int,
    val frontQuestion: String,
    val backAnswer: String,
    val memoryTip: String
)

/**
 * Curated Class 1–10 NCERT revision flashcards available 100% offline.
 */
object OfflineFlashcardDeck {
    val cards: List<NcertFlashcard> = listOf(
        NcertFlashcard(
            id = "fc_1",
            subject = "Math",
            gradeClass = 1,
            frontQuestion = "What is the place value of 4 in 47?",
            backAnswer = "40 (4 Tens = 4 × 10)",
            memoryTip = "Left digit in a 2-digit number is always bundles of 10!"
        ),
        NcertFlashcard(
            id = "fc_2",
            subject = "Math",
            gradeClass = 3,
            frontQuestion = "How many vertices and edges does a square have?",
            backAnswer = "4 equal sides (edges) and 4 square corners (vertices).",
            memoryTip = "Every corner of a square is a 90° right angle."
        ),
        NcertFlashcard(
            id = "fc_3",
            subject = "Math",
            gradeClass = 5,
            frontQuestion = "How do you add fractions with the same denominator (e.g. 2/6 + 3/6)?",
            backAnswer = "Keep the denominator same and add numerators: (2 + 3)/6 = 5/6.",
            memoryTip = "Bottom number = slice size. Never add denominators!"
        ),
        NcertFlashcard(
            id = "fc_4",
            subject = "Math",
            gradeClass = 8,
            frontQuestion = "Solve the linear equation: 2x + 4 = 14",
            backAnswer = "x = 5  (Subtract 4 → 2x = 10 → Divide by 2 → x = 5)",
            memoryTip = "Whatever you do to LHS, always do the same to RHS."
        ),
        NcertFlashcard(
            id = "fc_5",
            subject = "Math",
            gradeClass = 10,
            frontQuestion = "State the Pythagoras Theorem for a right-angled triangle.",
            backAnswer = "a² + b² = c² (where c is the hypotenuse opposite the 90° angle).",
            memoryTip = "Classic Pythagorean triples: (3, 4, 5) and (5, 12, 13)."
        ),
        NcertFlashcard(
            id = "fc_6",
            subject = "Science",
            gradeClass = 4,
            frontQuestion = "Name the 4 main stages of the Water Cycle.",
            backAnswer = "1. Evaporation  2. Condensation  3. Precipitation  4. Collection",
            memoryTip = "Sun heats water → Clouds cool → Rain falls → Rivers collect!"
        ),
        NcertFlashcard(
            id = "fc_7",
            subject = "Science",
            gradeClass = 6,
            frontQuestion = "Write the Photosynthesis word equation.",
            backAnswer = "Carbon Dioxide + Water + Sunlight (Chlorophyll) → Glucose + Oxygen",
            memoryTip = "Leaves are green kitchens; stomata are tiny breathing pores."
        ),
        NcertFlashcard(
            id = "fc_8",
            subject = "Science",
            gradeClass = 7,
            frontQuestion = "How does litmus paper test Acids vs Bases?",
            backAnswer = "Acids turn Blue litmus Red (pH < 7). Bases turn Red litmus Blue (pH > 7).",
            memoryTip = "Remember 'A-B-R': Acid turns Blue to Red; Base turns Red to Blue!"
        ),
        NcertFlashcard(
            id = "fc_9",
            subject = "Science",
            gradeClass = 7,
            frontQuestion = "What is a Neutralization Reaction?",
            backAnswer = "Acid + Base → Salt + Water + Heat (e.g. HCl + NaOH → NaCl + H₂O).",
            memoryTip = "Antacids use mild bases to neutralize excess stomach acid."
        ),
        NcertFlashcard(
            id = "fc_10",
            subject = "Science",
            gradeClass = 9,
            frontQuestion = "State Newton's Second Law of Motion & its SI unit of Force.",
            backAnswer = "F = m × a (Force = Mass × Acceleration). SI unit is Newton (N).",
            memoryTip = "A 2 kg ball accelerated at 3 m/s² needs 2 × 3 = 6 N force."
        ),
        NcertFlashcard(
            id = "fc_11",
            subject = "Science",
            gradeClass = 9,
            frontQuestion = "State Newton's Third Law of Motion with a daily example.",
            backAnswer = "Every action has an equal and opposite reaction (e.g. rocket exhaust pushes down, rocket flies up).",
            memoryTip = "Action & reaction always act on two different objects."
        ),
        NcertFlashcard(
            id = "fc_12",
            subject = "English",
            gradeClass = 2,
            frontQuestion = "What is the difference between a Common Noun and a Proper Noun?",
            backAnswer = "Common noun = general name (river, city). Proper noun = special capitalised name (Ganga, Pune).",
            memoryTip = "Proper nouns always begin with a CAPITAL letter!"
        ),
        NcertFlashcard(
            id = "fc_13",
            subject = "English",
            gradeClass = 6,
            frontQuestion = "When do we use 'a', 'an', and 'the'?",
            backAnswer = "'an' before vowel sounds (an hour, an apple), 'a' before consonant sounds (a book), 'the' for specific things (the Sun).",
            memoryTip = "Listen to the SOUND, not just the first letter ('an honest boy')."
        ),
        NcertFlashcard(
            id = "fc_14",
            subject = "Social Studies",
            gradeClass = 10,
            frontQuestion = "How many Fundamental Rights does the Indian Constitution guarantee?",
            backAnswer = "6 Fundamental Rights: Equality, Freedom, Against Exploitation, Religion, Cultural & Educational, Constitutional Remedies.",
            memoryTip = "Dr. B.R. Ambedkar called Article 32 the 'Heart & Soul' of the Constitution."
        ),
        NcertFlashcard(
            id = "fc_15",
            subject = "Social Studies",
            gradeClass = 10,
            frontQuestion = "What does Article 21A of the Indian Constitution guarantee?",
            backAnswer = "Right to Free and Compulsory Education for all children aged 6 to 14 years.",
            memoryTip = "Twogether supports every child's right to quality education!"
        )
    )
}

/**
 * Subject Mastery visual analytics card for Student Knowledge Tree dashboard.
 */
@Composable
fun SubjectMasteryCard(
    stats: List<SubjectMasteryStat>,
    modifier: Modifier = Modifier
) {
    if (stats.isEmpty()) return
    ClayCard(
        containerColor = ElevatedClayCard,
        cornerRadius = 26.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.study_mastery_title),
                style = MaterialTheme.typography.titleLarge,
                color = EspressoInk,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.study_mastery_sub),
                style = MaterialTheme.typography.bodySmall,
                color = CocoaSecondary
            )
            stats.forEach { stat ->
                val progress = if (stat.totalLessons > 0) {
                    (stat.completedLessons.toFloat() / stat.totalLessons.toFloat()).coerceIn(0f, 1f)
                } else 0f
                val barColor = when (stat.subject.lowercase()) {
                    "math" -> TerracottaCoral
                    "science" -> LeafGreen
                    "english" -> SunAmber
                    else -> Color(0xFF7E57C2)
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stat.subject,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = EspressoInk
                        )
                        Text(
                            text = stringResource(
                                R.string.study_mastery_row_stat,
                                stat.completedLessons,
                                stat.totalLessons,
                                stat.avgQuizPercent
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = CocoaSecondary
                        )
                    }
                    LinearProgressIndicator(
                        progress = { progress.coerceAtLeast(if (stat.avgQuizPercent > 0) 0.12f else 0.04f) },
                        color = barColor,
                        trackColor = SoftOatSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }
        }
    }
}

/**
 * Compact Pomodoro Study Focus Timer Card (100% offline, awards Focus XP on completion).
 */
@Composable
fun StudyFocusTimerCard(
    onFocusCompleted: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val presetsSeconds = listOf(
        10 * 60 to "10m",
        15 * 60 to "15m",
        25 * 60 to "25m"
    )
    var totalSeconds by remember { mutableIntStateOf(15 * 60) }
    var remainingSeconds by remember { mutableIntStateOf(15 * 60) }
    var isRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning, remainingSeconds) {
        if (isRunning && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds -= 1
            if (remainingSeconds == 0) {
                isRunning = false
                onFocusCompleted(25)
            }
        }
    }

    val progress = if (totalSeconds > 0) {
        remainingSeconds.toFloat() / totalSeconds.toFloat()
    } else 0f
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val formatted = String.format(Locale.US, "%02d:%02d", minutes, seconds)

    ClayCard(
        containerColor = SunAmberSoft.copy(alpha = 0.72f),
        cornerRadius = 26.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.Timer, contentDescription = null, tint = EspressoInk)
                    Column {
                        Text(
                            text = stringResource(R.string.study_focus_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = EspressoInk
                        )
                        Text(
                            text = stringResource(R.string.study_focus_sub),
                            style = MaterialTheme.typography.labelMedium,
                            color = CocoaSecondary
                        )
                    }
                }
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(64.dp)) {
                    CircularProgressIndicator(
                        progress = { progress },
                        color = TerracottaCoral,
                        trackColor = Color.White.copy(alpha = 0.65f),
                        strokeWidth = 6.dp,
                        modifier = Modifier.fillMaxSize()
                    )
                    Text(
                        text = formatted,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = EspressoInk
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                presetsSeconds.forEach { (secs, label) ->
                    FilterChip(
                        selected = totalSeconds == secs,
                        onClick = {
                            isRunning = false
                            totalSeconds = secs
                            remainingSeconds = secs
                        },
                        label = { Text(label) }
                    )
                }
                Spacer(Modifier.weight(1f))
                IconButton(
                    onClick = {
                        isRunning = false
                        remainingSeconds = totalSeconds
                    },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("focus_timer_reset_btn")
                ) {
                    Icon(
                        Icons.Rounded.Refresh,
                        contentDescription = stringResource(R.string.study_focus_reset_cd),
                        tint = EspressoInk
                    )
                }
            }

            ClayButton(
                text = stringResource(
                    if (isRunning) R.string.study_focus_pause else R.string.study_focus_start
                ),
                containerColor = if (isRunning) CocoaSecondary else TerracottaCoral,
                onClick = {
                    if (remainingSeconds == 0) remainingSeconds = totalSeconds
                    isRunning = !isRunning
                },
                leadingIcon = {
                    Icon(
                        if (isRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        tint = Color.White
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("focus_timer_toggle_btn")
            )
        }
    }
}

/**
 * Interactive 3D-Flip NCERT Revision Flashcards Modal (100% Offline + Voice Read-Aloud).
 */
@Composable
fun NcertFlashcardsDialog(
    ttsSpeaker: TtsSpeaker,
    speakingId: String?,
    onEarnXp: (Int, String) -> Unit,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var selectedSubject by remember { mutableStateOf("All") }
    val filteredCards = remember(selectedSubject) {
        if (selectedSubject == "All") OfflineFlashcardDeck.cards
        else OfflineFlashcardDeck.cards.filter { it.subject.equals(selectedSubject, ignoreCase = true) }
    }
    var cardIndex by remember(selectedSubject) { mutableIntStateOf(0) }
    var isFlipped by remember(selectedSubject, cardIndex) { mutableStateOf(false) }
    val masteredIds = remember { mutableStateListOf<String>() }
    var xpAwardedForSession by remember { mutableStateOf(false) }

    val safeCard = filteredCards.getOrNull(cardIndex.coerceIn(0, (filteredCards.size - 1).coerceAtLeast(0)))
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 360),
        label = "flashcard_flip"
    )

    Dialog(
        onDismissRequest = {
            ttsSpeaker.stop()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(30.dp),
            color = WarmCreamBg,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Rounded.Style, contentDescription = null, tint = TerracottaCoral)
                        Column {
                            Text(
                                text = stringResource(R.string.study_flashcards_title),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = EspressoInk
                            )
                            Text(
                                text = stringResource(
                                    R.string.study_flashcards_mastered,
                                    masteredIds.size,
                                    OfflineFlashcardDeck.cards.size
                                ),
                                style = MaterialTheme.typography.labelMedium,
                                color = LeafGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            ttsSpeaker.stop()
                            onDismiss()
                        },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.common_close))
                    }
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(listOf("All", "Math", "Science", "English", "Social Studies")) { subj ->
                        FilterChip(
                            selected = selectedSubject == subj,
                            onClick = { selectedSubject = subj },
                            label = { Text(subj) }
                        )
                    }
                }

                if (safeCard != null) {
                    val isMastered = masteredIds.contains(safeCard.id)
                    Surface(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            isFlipped = !isFlipped
                        },
                        shape = RoundedCornerShape(26.dp),
                        color = if (rotation <= 90f) Color(0xFFFFFBF0) else LeafGreenSoft,
                        border = BorderStroke(2.dp, if (isMastered) LeafGreen else TerracottaCoral.copy(alpha = 0.45f)),
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(235.dp)
                            .graphicsLayer {
                                rotationY = rotation
                                cameraDistance = 14f * density
                            }
                            .testTag("flashcard_flip_surface")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (rotation <= 90f) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = TerracottaSoft
                                    ) {
                                        Text(
                                            text = "${safeCard.subject} • Class ${safeCard.gradeClass} • ${cardIndex + 1}/${filteredCards.size}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = EspressoInk,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                    Text(
                                        text = safeCard.frontQuestion,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = EspressoInk,
                                        textAlign = TextAlign.Center
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Rounded.Flip,
                                            contentDescription = null,
                                            tint = CocoaSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = stringResource(R.string.study_flashcards_tap_flip),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = CocoaSecondary
                                        )
                                    }
                                }
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.graphicsLayer { rotationY = 180f }
                                ) {
                                    Text(
                                        text = stringResource(R.string.study_flashcards_answer_label),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = LeafGreen,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = safeCard.backAnswer,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = EspressoInk,
                                        textAlign = TextAlign.Center
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color.White.copy(alpha = 0.8f)
                                    ) {
                                        Text(
                                            text = "💡 ${safeCard.memoryTip}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = CocoaSecondary,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                cardIndex = if (cardIndex > 0) cardIndex - 1 else filteredCards.lastIndex
                            },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.study_prev))
                        }

                        ListenButton(
                            isSpeaking = speakingId == "fc_${safeCard.id}",
                            onClick = {
                                val textToRead = if (isFlipped) {
                                    "${safeCard.backAnswer}. Tip: ${safeCard.memoryTip}"
                                } else {
                                    safeCard.frontQuestion
                                }
                                ttsSpeaker.toggleSpeak("fc_${safeCard.id}", textToRead)
                            }
                        )

                        OutlinedButton(
                            onClick = {
                                cardIndex = (cardIndex + 1) % filteredCards.size
                            },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Text(stringResource(R.string.study_next))
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }

                    val masteredReason = stringResource(R.string.study_flashcards_xp_reason)
                    ClayButton(
                        text = stringResource(
                            if (isMastered) R.string.study_flashcards_marked_mastered
                            else R.string.study_flashcards_mark_mastered
                        ),
                        containerColor = if (isMastered) LeafGreen else TerracottaCoral,
                        onClick = {
                            if (!isMastered) {
                                masteredIds.add(safeCard.id)
                                if (masteredIds.size >= 3 && !xpAwardedForSession) {
                                    xpAwardedForSession = true
                                    onEarnXp(25, masteredReason)
                                }
                            } else {
                                masteredIds.remove(safeCard.id)
                            }
                            if (cardIndex < filteredCards.lastIndex) {
                                cardIndex += 1
                            }
                        },
                        leadingIcon = {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Color.White)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("flashcard_master_btn")
                    )
                }
            }
        }
    }
}

/**
 * Daily 5-Question Offline Brain Sprint: pulls randomized real NCERT questions from SeedData
 * across subjects, gives instant explanations, and awards bonus Knowledge Tree XP!
 */
@Composable
fun DailyBrainSprintDialog(
    onCompleteSprint: (Int, Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val questions: List<QuizQuestionEntity> = remember {
        SeedData.initialQuizzes.shuffled().take(5)
    }
    var qIdx by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableIntStateOf(-1) }
    var score by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = WarmCreamBg,
        shape = RoundedCornerShape(28.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Rounded.Bolt, contentDescription = null, tint = SunAmber)
                Text(
                    text = stringResource(R.string.study_sprint_title),
                    fontWeight = FontWeight.Bold,
                    color = EspressoInk
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (finished) {
                    val bonusXp = score * 15 + if (score >= 4) 20 else 5
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = LeafGreenSoft,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = LeafGreen,
                                modifier = Modifier.size(44.dp)
                            )
                            Text(
                                text = stringResource(R.string.study_sprint_score, score, questions.size),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = EspressoInk
                            )
                            Text(
                                text = stringResource(R.string.study_sprint_xp_earned, bonusXp),
                                style = MaterialTheme.typography.titleMedium,
                                color = LeafGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    val q = questions[qIdx]
                    LinearProgressIndicator(
                        progress = { (qIdx + 1f) / questions.size.toFloat() },
                        color = TerracottaCoral,
                        trackColor = SoftOatSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(6.dp))
                    )
                    Text(
                        text = stringResource(R.string.study_sprint_counter, qIdx + 1, questions.size),
                        style = MaterialTheme.typography.labelLarge,
                        color = CocoaSecondary
                    )
                    Text(
                        text = q.questionText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EspressoInk
                    )

                    val options = listOf(q.optionA, q.optionB, q.optionC, q.optionD)
                    options.forEachIndexed { idx, opt ->
                        val answered = selectedOption >= 0
                        val bg = when {
                            !answered -> SoftOatSurface
                            idx == q.correctIndex -> LeafGreenSoft
                            idx == selectedOption -> ClayErrorContainer
                            else -> SoftOatSurface
                        }
                        Surface(
                            onClick = {
                                if (!answered) {
                                    selectedOption = idx
                                    if (idx == q.correctIndex) score += 1
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = bg,
                            border = BorderStroke(
                                if (answered && idx == q.correctIndex) 2.dp else 1.dp,
                                if (answered && idx == q.correctIndex) LeafGreen else Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("sprint_option_$idx")
                        ) {
                            Text(
                                text = "${'A' + idx}. $opt",
                                style = MaterialTheme.typography.bodyMedium,
                                color = EspressoInk,
                                fontWeight = if (answered && idx == q.correctIndex) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    if (selectedOption >= 0) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = SunAmberSoft,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "💡 ${q.explanation}",
                                style = MaterialTheme.typography.bodySmall,
                                color = EspressoInk,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (finished) {
                val bonusXp = score * 15 + if (score >= 4) 20 else 5
                ClayButton(
                    text = stringResource(R.string.study_sprint_claim_xp),
                    containerColor = LeafGreen,
                    onClick = {
                        onCompleteSprint(score, questions.size, bonusXp)
                        onDismiss()
                    },
                    modifier = Modifier.testTag("sprint_claim_xp_btn")
                )
            } else {
                ClayButton(
                    text = stringResource(
                        if (qIdx == questions.lastIndex) R.string.study_sprint_finish
                        else R.string.study_next
                    ),
                    enabled = selectedOption >= 0,
                    onClick = {
                        if (qIdx == questions.lastIndex) {
                            finished = true
                        } else {
                            qIdx += 1
                            selectedOption = -1
                        }
                    },
                    modifier = Modifier.testTag("sprint_next_btn")
                )
            }
        },
        dismissButton = {
            if (!finished) {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
            }
        }
    )
}

private data class ChalkStroke(
    val points: List<Offset>,
    val color: Color,
    val widthPx: Float
)

/**
 * Offline Chalkboard Rough-Work Scratchpad + Pocket Formula Strip.
 * Lets students solve Math/Science equations or sketch geometry diagrams directly on screen.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChalkboardScratchpadDialog(
    onDismiss: () -> Unit
) {
    val chalkPalette = listOf(
        Color(0xFFF9F7F1), // Classic White Chalk
        Color(0xFFFFD166), // Warm Amber Chalk
        Color(0xFF8CE0A7), // Mint Chalk
        Color(0xFFFF8F7B)  // Coral Chalk
    )
    var selectedColor by remember { mutableStateOf(chalkPalette.first()) }
    var thickStroke by remember { mutableStateOf(false) }
    var showGrid by remember { mutableStateOf(true) }
    val strokes = remember { mutableStateListOf<ChalkStroke>() }
    val activePoints = remember { mutableStateListOf<Offset>() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = WarmCreamBg,
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .padding(vertical = 16.dp)
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.Draw, contentDescription = null, tint = LeafGreen)
                        Text(
                            text = stringResource(R.string.study_chalkboard_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = EspressoInk
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.common_close))
                    }
                }

                // Pocket Formula Reference Strip
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(
                        listOf(
                            "a² + b² = c²",
                            "F = m × a",
                            "Area = l × b",
                            "Speed = Dist / Time",
                            "CO₂ + H₂O → Glucose + O₂",
                            "pH < 7 Acid | > 7 Base"
                        )
                    ) { formula ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SoftOatSurface
                        ) {
                            Text(
                                text = formula,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = EspressoInk,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Interactive Village Chalkboard Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(330.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFF1F382E))
                        .testTag("chalkboard_canvas")
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(selectedColor, thickStroke) {
                                detectDragGestures(
                                    onDragStart = { start ->
                                        activePoints.clear()
                                        activePoints.add(start)
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        activePoints.add(change.position)
                                    },
                                    onDragEnd = {
                                        if (activePoints.isNotEmpty()) {
                                            strokes.add(
                                                ChalkStroke(
                                                    points = activePoints.toList(),
                                                    color = selectedColor,
                                                    widthPx = if (thickStroke) 12f else 6f
                                                )
                                            )
                                            activePoints.clear()
                                        }
                                    }
                                )
                            }
                    ) {
                        if (showGrid) {
                            val step = 32.dp.toPx()
                            val gridColor = Color.White.copy(alpha = 0.08f)
                            var x = step
                            while (x < size.width) {
                                drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                                x += step
                            }
                            var y = step
                            while (y < size.height) {
                                drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                                y += step
                            }
                        }

                        fun drawPolyline(pts: List<Offset>, color: Color, widthPx: Float) {
                            if (pts.isEmpty()) return
                            if (pts.size == 1) {
                                drawCircle(color = color, radius = widthPx / 2f, center = pts.first())
                                return
                            }
                            val path = Path().apply {
                                moveTo(pts.first().x, pts.first().y)
                                for (i in 1 until pts.size) {
                                    lineTo(pts[i].x, pts[i].y)
                                }
                            }
                            drawPath(
                                path = path,
                                color = color,
                                style = Stroke(
                                    width = widthPx,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }

                        strokes.forEach { s -> drawPolyline(s.points, s.color, s.widthPx) }
                        if (activePoints.isNotEmpty()) {
                            drawPolyline(activePoints, selectedColor, if (thickStroke) 12f else 6f)
                        }
                    }

                    if (strokes.isEmpty() && activePoints.isEmpty()) {
                        Text(
                            text = stringResource(R.string.study_chalkboard_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.45f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = 24.dp)
                        )
                    }
                }

                // Chalk Controls Row
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        chalkPalette.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable { selectedColor = color }
                                    .then(
                                        if (selectedColor == color) {
                                            Modifier.background(Color.Transparent)
                                        } else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedColor == color) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(EspressoInk)
                                    )
                                }
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = { showGrid = !showGrid },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                Icons.Rounded.GridOn,
                                contentDescription = stringResource(R.string.study_chalkboard_grid_cd),
                                tint = if (showGrid) LeafGreen else CocoaSecondary
                            )
                        }
                        FilterChip(
                            selected = thickStroke,
                            onClick = { thickStroke = !thickStroke },
                            label = {
                                Text(
                                    stringResource(
                                        if (thickStroke) R.string.study_chalk_bold else R.string.study_chalk_fine
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                        IconButton(
                            onClick = { if (strokes.isNotEmpty()) strokes.removeAt(strokes.lastIndex) },
                            enabled = strokes.isNotEmpty(),
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                Icons.AutoMirrored.Rounded.Undo,
                                contentDescription = stringResource(R.string.study_chalkboard_undo_cd),
                                tint = EspressoInk
                            )
                        }
                        IconButton(
                            onClick = { strokes.clear() },
                            enabled = strokes.isNotEmpty(),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("chalkboard_clear_btn")
                        ) {
                            Icon(
                                Icons.Rounded.DeleteSweep,
                                contentDescription = stringResource(R.string.study_chalkboard_clear_cd),
                                tint = TerracottaCoral
                            )
                        }
                    }
                }
            }
        }
    }
}
