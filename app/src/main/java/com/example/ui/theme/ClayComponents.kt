package com.example.ui.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Tactile Claymorphic Card/Container with soft warm outer shadow + inner top-left sunlit highlight
 * and inner bottom-right soft clay cushion rim.
 */
@Composable
fun ClayCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    cornerRadius: Dp = 28.dp,
    elevationDp: Dp = 6.dp,
    borderColor: Color = WarmSandBorder.copy(alpha = 0.55f),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "clay_card_scale"
    )
    val shape: Shape = RoundedCornerShape(cornerRadius)

    val clickModifier = if (onClick != null) {
        Modifier
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                role = Role.Button
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
    } else {
        Modifier.clip(shape)
    }

    Box(
        modifier = modifier
            .scale(scale)
            .drawBehind {
                val radiusPx = cornerRadius.toPx()
                val offsetPx = elevationDp.toPx()
                // Soft warm outer clay shadow
                drawRoundRect(
                    color = ClayShadowTint.copy(alpha = 0.16f),
                    topLeft = Offset(offsetPx * 0.45f, offsetPx),
                    size = Size(size.width, size.height),
                    cornerRadius = CornerRadius(radiusPx, radiusPx)
                )
                // Secondary ambient clay cushion
                drawRoundRect(
                    color = ClayShadowTint.copy(alpha = 0.08f),
                    topLeft = Offset(0f, offsetPx * 1.35f),
                    size = Size(size.width, size.height),
                    cornerRadius = CornerRadius(radiusPx, radiusPx)
                )
            }
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        containerColor,
                        containerColor.copy(
                            red = (containerColor.red * 0.985f).coerceIn(0f, 1f),
                            green = (containerColor.green * 0.975f).coerceIn(0f, 1f),
                            blue = (containerColor.blue * 0.96f).coerceIn(0f, 1f)
                        )
                    )
                ),
                shape = shape
            )
            .border(width = 1.5.dp, color = borderColor, shape = shape)
            .drawBehind {
                val radiusPx = cornerRadius.toPx()
                val inset = 2.dp.toPx()
                // Inner top-left sunlit clay rim highlight
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.78f),
                            Color.White.copy(alpha = 0.25f),
                            ClayShadowTint.copy(alpha = 0.14f)
                        ),
                        start = Offset.Zero,
                        end = Offset(size.width, size.height)
                    ),
                    topLeft = Offset(inset, inset),
                    size = Size(
                        (size.width - inset * 2f).coerceAtLeast(0f),
                        (size.height - inset * 2f).coerceAtLeast(0f)
                    ),
                    cornerRadius = CornerRadius(
                        (radiusPx - inset).coerceAtLeast(0f),
                        (radiusPx - inset).coerceAtLeast(0f)
                    ),
                    style = Stroke(width = 2.2.dp.toPx())
                )
            }
            .then(clickModifier)
    ) {
        content()
    }
}

/**
 * Tactile bubbly Claymorphic Primary/Secondary Button.
 */
@Composable
fun ClayButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    leadingIcon: (@Composable () -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "clay_button_scale"
    )
    val effectiveColor = if (enabled) containerColor else containerColor.copy(alpha = 0.45f)
    val shape = RoundedCornerShape(24.dp)

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .defaultMinSize(minHeight = 48.dp)
            .scale(scale)
            .drawBehind {
                val r = 24.dp.toPx()
                drawRoundRect(
                    color = ClayShadowTint.copy(alpha = if (enabled) 0.28f else 0.10f),
                    topLeft = Offset(2.dp.toPx(), 5.dp.toPx()),
                    size = size,
                    cornerRadius = CornerRadius(r, r)
                )
            }
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        effectiveColor.copy(alpha = 0.95f),
                        effectiveColor
                    )
                ),
                shape = shape
            )
            .border(
                width = 1.5.dp,
                color = Color.White.copy(alpha = 0.42f),
                shape = shape
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = ripple(color = Color.White.copy(alpha = 0.35f)),
                role = Role.Button
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                softWrap = true,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Alive gently drifting warm ambient orbs in the background for Claymorphism depth.
 */
@Composable
fun AliveAmbientBackground(
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "alive_ambient_bg")
    val drift by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 6.2831855f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ambient_drift"
    )
    val primarySoft = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.34f)
    val secondarySoft = LeafGreenSoft.copy(alpha = 0.42f)
    val amberSoft = SunAmberSoft.copy(alpha = 0.45f)

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(WarmCreamBg)
    ) {
        val w = size.width
        val h = size.height
        val dx1 = cos(drift) * 18.dp.toPx()
        val dy1 = sin(drift) * 14.dp.toPx()
        val dx2 = sin(drift * 0.8f) * 20.dp.toPx()
        val dy2 = cos(drift * 0.8f) * 16.dp.toPx()

        drawCircle(
            color = amberSoft,
            radius = w * 0.38f,
            center = Offset(w * 0.14f + dx1, h * 0.12f + dy1)
        )
        drawCircle(
            color = primarySoft,
            radius = w * 0.34f,
            center = Offset(w * 0.88f - dx2, h * 0.28f + dy2)
        )
        drawCircle(
            color = secondarySoft,
            radius = w * 0.42f,
            center = Offset(w * 0.30f + dx2, h * 0.86f - dy1)
        )
    }
}

/**
 * Alive breathing status dot with soft clay halo (for online/unread/active badges).
 */
@Composable
fun AlivePulseDot(
    color: Color,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 8.dp
) {
    val infinite = rememberInfiniteTransition(label = "pulse_dot")
    val haloScale by infinite.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val haloAlpha by infinite.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )
    Box(
        modifier = modifier.size(sizeDp * 2.1f),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(sizeDp)
                .scale(haloScale)
                .clip(CircleShape)
                .background(color.copy(alpha = haloAlpha))
        )
        Box(
            modifier = Modifier
                .size(sizeDp)
                .clip(CircleShape)
                .background(color)
        )
    }
}

/**
 * Smooth spring staggered entry for lists and cards so the screen feels alive on load.
 */
@Composable
fun AliveStaggeredEntry(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay((index.coerceIn(0, 6) * 45L))
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(260)) +
            slideInVertically(
                initialOffsetY = { it / 5 },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ),
        modifier = modifier
    ) {
        content()
    }
}

/**
 * Massive Voice-First Microphone Button for every SearchBar and ChatBox.
 */
@Composable
fun VoiceMicButton(
    isListening: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "voice_mic_button",
    contentDescription: String? = null
) {
    val resolvedCd = contentDescription ?: stringResource(R.string.common_cd_voice_mic)
    val haptic = LocalHapticFeedback.current
    val scale by animateFloatAsState(
        targetValue = if (isListening) 1.12f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "mic_pulse"
    )
    val bgColor = if (isListening) TerracottaCoral else MaterialTheme.colorScheme.tertiary

    Box(
        modifier = modifier
            .size(56.dp)
            .scale(scale)
            .testTag(testTag)
            .drawBehind {
                drawCircle(
                    color = bgColor.copy(alpha = 0.32f),
                    radius = size.minDimension / 2f + if (isListening) 6.dp.toPx() else 3.dp.toPx(),
                    center = Offset(center.x, center.y + 3.dp.toPx())
                )
            }
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(bgColor.copy(alpha = 0.9f), bgColor)
                )
            )
            .border(2.dp, Color.White.copy(alpha = 0.65f), CircleShape)
            .clickable(role = Role.Button) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isListening) Icons.Rounded.Stop else Icons.Rounded.Mic,
            contentDescription = resolvedCd,
            tint = Color.White,
            modifier = Modifier.size(30.dp)
        )
    }
}

/**
 * Tactile "Listen" (Text-to-Speech) Pill placed next to text-heavy sections.
 */
@Composable
fun ListenButton(
    isSpeaking: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    testTag: String = "listen_tts_button"
) {
    val resolvedLabel = label ?: stringResource(R.string.common_listen)
    val stopLabel = stringResource(R.string.common_stop)
    val stopCd = stringResource(R.string.common_cd_stop_aloud)
    val listenCd = stringResource(R.string.common_cd_listen_aloud)
    val haptic = LocalHapticFeedback.current
    val container = if (isSpeaking) TerracottaSoft else SunAmberSoft
    val tint = if (isSpeaking) TerracottaCoral else EspressoInk

    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        modifier = modifier
            .minimumInteractiveComponentSize()
            .testTag(testTag),
        shape = RoundedCornerShape(20.dp),
        color = container,
        border = BorderStroke(1.2.dp, tint.copy(alpha = 0.25f)),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isSpeaking) Icons.Rounded.Stop else Icons.AutoMirrored.Rounded.VolumeUp,
                contentDescription = if (isSpeaking) stopCd else listenCd,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isSpeaking) stopLabel else resolvedLabel,
                style = MaterialTheme.typography.labelMedium,
                color = tint,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Playful Animal & Astronaut Avatar badge for Multi-Profile Student Login.
 */
enum class StudentAvatar(
    val id: String,
    val emoji: String,
    val label: String,
    val badgeBg: Color
) {
    LION("lion", "🦁", "Sunny Lion", Color(0xFFFFE5B4)),
    ASTRONAUT("astronaut", "👩‍🚀", "Star Explorer", Color(0xFFFFE1D6)),
    PANDA("panda", "🐼", "Bamboo Panda", Color(0xFFDFF2D8)),
    FOX("fox", "🦊", "Clever Fox", Color(0xFFFFDFC4)),
    OWL("owl", "🦉", "Wise Owl", Color(0xFFF3E4C9)),
    TURTLE("turtle", "🐢", "Ocean Turtle", Color(0xFFE0F0D5));

    companion object {
        fun fromId(id: String): StudentAvatar =
            entries.firstOrNull { it.id == id } ?: LION
    }
}

@Composable
fun StudentAvatarBadge(
    avatarId: String,
    size: Dp = 64.dp,
    modifier: Modifier = Modifier
) {
    val avatar = StudentAvatar.fromId(avatarId)
    Box(
        modifier = modifier
            .size(size)
            .drawBehind {
                drawCircle(
                    color = ClayShadowTint.copy(alpha = 0.18f),
                    radius = this.size.minDimension / 2f,
                    center = Offset(center.x, center.y + 3.dp.toPx())
                )
            }
            .clip(CircleShape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White, avatar.badgeBg)
                )
            )
            .border(2.5.dp, Color.White.copy(alpha = 0.85f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = avatar.emoji,
            fontSize = (size.value * 0.48f).sp
        )
    }
}

/**
 * Warm Confetti Burst micro-interaction for quiz completions and Knowledge Tree level-ups.
 */
@Composable
fun ConfettiBurstOverlay(
    triggerKey: Int,
    modifier: Modifier = Modifier
) {
    if (triggerKey <= 0) return

    val progress = remember(triggerKey) { Animatable(0f) }
    val particles = remember(triggerKey) {
        val rng = Random(triggerKey)
        val palette = listOf(
            SunAmber, TerracottaCoral, LeafGreen, Color(0xFFE88D67), Color(0xFFF4C95D)
        )
        List(36) {
            ConfettiParticle(
                angle = rng.nextFloat() * 360f,
                speed = 180f + rng.nextFloat() * 440f,
                size = 8f + rng.nextFloat() * 10f,
                color = palette[it % palette.size]
            )
        }
    }

    LaunchedEffect(triggerKey) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1350, easing = LinearEasing)
        )
    }

    val p = progress.value
    if (p < 1f) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val originX = size.width / 2f
            val originY = size.height * 0.35f
            val alpha = (1f - p).coerceIn(0f, 1f)
            particles.forEach { pt ->
                val rad = Math.toRadians(pt.angle.toDouble())
                val dx = (cos(rad) * pt.speed * p).toFloat()
                val dy = (sin(rad) * pt.speed * p + 380f * p * p).toFloat()
                drawCircle(
                    color = pt.color.copy(alpha = alpha),
                    radius = pt.size,
                    center = Offset(originX + dx, originY + dy)
                )
            }
        }
    }
}

private data class ConfettiParticle(
    val angle: Float,
    val speed: Float,
    val size: Float,
    val color: Color
)
