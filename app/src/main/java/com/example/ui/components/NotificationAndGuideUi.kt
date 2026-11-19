package com.example.ui.components

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Backpack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Park
import androidx.compose.material.icons.rounded.QuestionAnswer
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.VideoCall
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.local.AppNotificationEntity
import com.example.ui.theme.AlivePulseDot
import com.example.ui.theme.ClayButton
import com.example.ui.theme.ClayCard
import com.example.ui.theme.ClayShadowTint
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
import com.example.ui.theme.WarmSandBorder
import com.example.util.TtsSpeaker
import com.example.util.TwogetherNotifier
import kotlinx.coroutines.delay

/**
 * Custom Claymorphic Local Notification Design System Tokens.
 * Soft bubbly corners, sunlit top-left & warm inner shadows, and earthy pastel surfaces.
 */
@Immutable
data class ClayNotificationTokens(
    val surfaceTop: Color,
    val surfaceBottom: Color,
    val wellSurface: Color,
    val accent: Color,
    val pillContainer: Color,
    @StringRes val badgeLabelRes: Int,
    val icon: ImageVector
)

private fun resolveClayNotificationTokens(category: String): ClayNotificationTokens {
    return when (category.lowercase()) {
        "doubt" -> ClayNotificationTokens(
            surfaceTop = Color(0xFFF3FAEF),
            surfaceBottom = LeafGreenSoft,
            wellSurface = Color(0xFFE7F5E3),
            accent = LeafGreen,
            pillContainer = Color(0xFFD0ECC9),
            badgeLabelRes = R.string.notif_badge_mentor_reply,
            icon = Icons.Rounded.QuestionAnswer
        )
        "live" -> ClayNotificationTokens(
            surfaceTop = Color(0xFFFFF4EE),
            surfaceBottom = TerracottaSoft,
            wellSurface = Color(0xFFFFEAE0),
            accent = TerracottaCoral,
            pillContainer = Color(0xFFFFD8C8),
            badgeLabelRes = R.string.notif_badge_live_class,
            icon = Icons.Rounded.VideoCall
        )
        "tree" -> ClayNotificationTokens(
            surfaceTop = Color(0xFFF6FBEF),
            surfaceBottom = Color(0xFFE3F2D7),
            wellSurface = Color(0xFFEBF6DF),
            accent = LeafGreen,
            pillContainer = Color(0xFFD5EBC6),
            badgeLabelRes = R.string.notif_badge_tree,
            icon = Icons.Rounded.Park
        )
        "educator_verify" -> ClayNotificationTokens(
            surfaceTop = Color(0xFFFFF5EE),
            surfaceBottom = TerracottaSoft,
            wellSurface = Color(0xFFFFEBE0),
            accent = TerracottaCoral,
            pillContainer = Color(0xFFFFDACB),
            badgeLabelRes = R.string.notif_badge_verify,
            icon = Icons.Rounded.VerifiedUser
        )
        else -> ClayNotificationTokens(
            surfaceTop = ElevatedClayCard,
            surfaceBottom = SunAmberSoft,
            wellSurface = Color(0xFFFFF4D6),
            accent = SunAmber,
            pillContainer = Color(0xFFFFE5A3),
            badgeLabelRes = R.string.notif_badge_homework,
            icon = Icons.Outlined.Notifications
        )
    }
}

@Composable
private fun localizedNotificationTitle(notification: AppNotificationEntity): String {
    return when {
        notification.id == "notif_seed_maths_hw" ||
            notification.title == "Pending Homework Reminder" ->
            stringResource(R.string.notif_pending_homework_title)
        notification.id == "notif_seed_science_quiz" ||
            notification.title == "Offline Quiz Ready" ->
            stringResource(R.string.notif_seed_quiz_title)
        notification.id == "notif_seed_doubt_reply" ||
            notification.title == "Educator Answered Your Doubt!" ->
            stringResource(R.string.notif_doubt_answered_title)
        notification.title == "Doubt Synced to Mentors!" ->
            stringResource(R.string.notif_doubt_synced_title)
        notification.title == "Educator Verified!" ->
            stringResource(R.string.notif_edu_verified_title)
        notification.title == "Verification Returned" ->
            stringResource(R.string.notif_edu_returned_title)
        notification.title == "Live Session Starting Now!" ->
            stringResource(R.string.notif_live_starting_title)
        else -> notification.title
    }
}

@Composable
private fun localizedNotificationMessage(notification: AppNotificationEntity): String {
    return when {
        notification.id == "notif_seed_maths_hw" ||
            notification.message == "Your Maths homework is still incomplete. Please complete it today!" ->
            stringResource(R.string.notif_pending_homework_msg_default)
        notification.id == "notif_seed_science_quiz" ||
            notification.message == "Watch 'How Green Leaves Cook Food' to grow your Knowledge Tree." ->
            stringResource(R.string.notif_seed_quiz_msg)
        notification.id == "notif_seed_doubt_reply" ||
            notification.message == "Priya Didi answered your photosynthesis leaf question." ->
            stringResource(R.string.notif_seed_doubt_msg)
        else -> notification.message
    }
}

/**
 * Custom Claymorphic Notification Card:
 * - Soft 26.dp rounded corners with cushioned outer clay drop shadow
 * - Light inner shadows: bright cream inner highlight (top-left) + warm clay inner shadow (bottom-right)
 * - Recessed clay squircle bell well with inner depth rim
 * - Warm earthy category palette + minimalist copy + breathing unread pulse
 */
@Composable
fun TwogetherNotificationCard(
    notification: AppNotificationEntity,
    isSpeaking: Boolean,
    onListen: () -> Unit,
    onClick: () -> Unit,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val tokens = remember(notification.category) {
        resolveClayNotificationTokens(notification.category)
    }
    val displayTitle = localizedNotificationTitle(notification)
    val displayMessage = localizedNotificationMessage(notification)
    val timeText = formatRelativeTime(notification.createdAtMillis)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.975f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "clay_notif_scale"
    )
    val shape = RoundedCornerShape(26.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .drawBehind {
                val r = 26.dp.toPx()
                drawRoundRect(
                    color = ClayShadowTint.copy(alpha = 0.16f),
                    topLeft = Offset(2.dp.toPx(), 7.dp.toPx()),
                    size = size,
                    cornerRadius = CornerRadius(r, r)
                )
                drawRoundRect(
                    color = tokens.accent.copy(alpha = 0.08f),
                    topLeft = Offset(0f, 11.dp.toPx()),
                    size = size,
                    cornerRadius = CornerRadius(r, r)
                )
            }
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(tokens.surfaceTop, tokens.surfaceBottom)
                )
            )
            .border(1.4.dp, WarmSandBorder.copy(alpha = 0.75f), shape)
            .drawBehind {
                val r = 26.dp.toPx()
                val inset = 2.5.dp.toPx()
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.92f),
                            Color.White.copy(alpha = 0.35f),
                            ClayShadowTint.copy(alpha = 0.16f)
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
                        (r - inset).coerceAtLeast(0f),
                        (r - inset).coerceAtLeast(0f)
                    ),
                    style = Stroke(width = 2.5.dp.toPx())
                )
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                    Text(
                        text = stringResource(R.string.notif_brand_subtext),
                        color = EspressoInk,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.5.sp,
                        letterSpacing = 1.1.sp,
                        maxLines = 1
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = tokens.pillContainer
                    ) {
                        Text(
                            text = stringResource(tokens.badgeLabelRes),
                            color = EspressoInk,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                    if (!notification.isRead) {
                        AlivePulseDot(color = tokens.accent, sizeDp = 7.dp)
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = timeText,
                        color = CocoaSecondary,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    if (onDismiss != null) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.65f))
                                .clickable(onClick = onDismiss),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.notif_cd_dismiss),
                                tint = CocoaSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(tokens.wellSurface, Color.White.copy(alpha = 0.92f))
                            )
                        )
                        .drawBehind {
                            val r = 16.dp.toPx()
                            drawRoundRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        ClayShadowTint.copy(alpha = 0.22f),
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.9f)
                                    ),
                                    start = Offset.Zero,
                                    end = Offset(size.width, size.height)
                                ),
                                topLeft = Offset(1.5.dp.toPx(), 1.5.dp.toPx()),
                                size = Size(size.width - 3.dp.toPx(), size.height - 3.dp.toPx()),
                                cornerRadius = CornerRadius(r - 1.5.dp.toPx(), r - 1.5.dp.toPx()),
                                style = Stroke(width = 2.dp.toPx())
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = tokens.icon,
                        contentDescription = stringResource(R.string.notif_cd_icon),
                        tint = tokens.accent,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = displayTitle,
                        color = EspressoInk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.5.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = displayMessage,
                        color = CocoaSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ListenButton(
                    isSpeaking = isSpeaking,
                    onClick = onListen,
                    label = stringResource(R.string.common_listen),
                    testTag = "listen_notif_${notification.id}"
                )
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = tokens.accent.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, tokens.accent.copy(alpha = 0.28f))
                ) {
                    Text(
                        text = when (notification.targetTab) {
                            "doubts" -> stringResource(R.string.notif_cta_doubts)
                            "tree" -> stringResource(R.string.notif_cta_tree)
                            "ai_chat" -> stringResource(R.string.notif_cta_ai)
                            else -> stringResource(R.string.notif_cta_backpack)
                        },
                        color = tokens.accent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }
        }
    }
}

/**
 * Floating Heads-Up Claymorphic Notification Banner with bouncy spring arrival.
 */
@Composable
fun HeadsUpNotificationOverlay(
    notification: AppNotificationEntity?,
    ttsSpeaker: TtsSpeaker,
    speakingId: String?,
    onOpenNotification: (AppNotificationEntity) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(notification?.id) {
        if (notification != null) {
            delay(7500L)
            onDismiss()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = notification != null,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
        ) {
            notification?.let { item ->
                val localizedTitle = localizedNotificationTitle(item)
                val localizedMsg = localizedNotificationMessage(item)
                TwogetherNotificationCard(
                    notification = item,
                    isSpeaking = speakingId == "headsup_${item.id}",
                    onListen = {
                        ttsSpeaker.toggleSpeak(
                            "headsup_${item.id}",
                            "$localizedTitle. $localizedMsg"
                        )
                    },
                    onClick = { onOpenNotification(item) },
                    onDismiss = onDismiss,
                    modifier = Modifier.testTag("headsup_notification_banner")
                )
            }
        }
    }
}

/**
 * Notifications & Study Reminders Center Sheet.
 */
@Composable
fun NotificationsCenterDialog(
    notifications: List<AppNotificationEntity>,
    ttsSpeaker: TtsSpeaker,
    speakingId: String?,
    onTriggerReminderNow: () -> Unit,
    onNotificationClicked: (AppNotificationEntity) -> Unit,
    onMarkAllRead: () -> Unit,
    onNotify: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sentSystemBarMsg = stringResource(R.string.notif_sent_system_bar)
    val sentInAppMsg = stringResource(R.string.notif_sent_in_app)
    val sentClayMsg = stringResource(R.string.notif_sent_clay)

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        onTriggerReminderNow()
        if (granted) {
            onNotify(sentSystemBarMsg)
        } else {
            onNotify(sentInAppMsg)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        ClayCard(
            containerColor = WarmCreamBg,
            cornerRadius = 28.dp,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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
                        Icon(
                            imageVector = Icons.Rounded.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.notif_study_alerts_title),
                            style = MaterialTheme.typography.headlineMedium,
                            color = EspressoInk,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.notif_cd_close))
                    }
                }

                ClayButton(
                    text = stringResource(R.string.notif_send_study_reminder),
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            !TwogetherNotifier.hasNotificationPermission(context)
                        ) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            onTriggerReminderNow()
                            onNotify(sentClayMsg)
                        }
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("send_homework_reminder_button")
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(notifications, key = { it.id }) { notif ->
                        val localizedTitle = localizedNotificationTitle(notif)
                        val localizedMsg = localizedNotificationMessage(notif)
                        TwogetherNotificationCard(
                            notification = notif,
                            isSpeaking = speakingId == "notif_${notif.id}",
                            onListen = {
                                ttsSpeaker.toggleSpeak(
                                    "notif_${notif.id}",
                                    "$localizedTitle. $localizedMsg"
                                )
                            },
                            onClick = { onNotificationClicked(notif) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onMarkAllRead,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Text(stringResource(R.string.notif_mark_all_read))
                    }
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Text(stringResource(R.string.common_close))
                    }
                }
            }
        }
    }
}

/**
 * Minimalist Easy-to-Use Guide Dialog covering all 3 Portals & Core Features.
 */
@Composable
fun EasyUseGuideDialog(
    ttsSpeaker: TtsSpeaker,
    speakingId: String?,
    onDismiss: () -> Unit
) {
    val fullGuideSpeech = stringResource(R.string.guide_full_speech)

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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.HelpOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.guide_title),
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                ListenButton(
                    isSpeaking = speakingId == "easy_guide_full",
                    onClick = { ttsSpeaker.toggleSpeak("easy_guide_full", fullGuideSpeech) },
                    testTag = "easy_guide_listen_button"
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GuideStepRow(
                    step = "1",
                    title = stringResource(R.string.guide_step1_title),
                    desc = stringResource(R.string.guide_step1_desc),
                    icon = Icons.Rounded.Park,
                    tint = TerracottaCoral
                )
                GuideStepRow(
                    step = "2",
                    title = stringResource(R.string.guide_step2_title),
                    desc = stringResource(R.string.guide_step2_desc),
                    icon = Icons.Rounded.Backpack,
                    tint = LeafGreen
                )
                GuideStepRow(
                    step = "3",
                    title = stringResource(R.string.guide_step3_title),
                    desc = stringResource(R.string.guide_step3_desc),
                    icon = Icons.Rounded.AutoAwesome,
                    tint = MaterialTheme.colorScheme.primary
                )
                GuideStepRow(
                    step = "4",
                    title = stringResource(R.string.guide_step4_title),
                    desc = stringResource(R.string.guide_step4_desc),
                    icon = Icons.Rounded.School,
                    tint = SunAmber
                )
            }
        },
        confirmButton = {
            ClayButton(
                text = stringResource(R.string.guide_got_it),
                onClick = onDismiss,
                modifier = Modifier.testTag("easy_guide_close_button")
            )
        }
    )
}

@Composable
private fun GuideStepRow(
    step: String,
    title: String,
    desc: String,
    icon: ImageVector,
    tint: Color
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = SoftOatSurface,
        border = BorderStroke(1.dp, tint.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(tint.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(19.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$step. $title",
                    style = MaterialTheme.typography.titleMedium,
                    color = EspressoInk,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CocoaSecondary
                )
            }
        }
    }
}

@Composable
private fun formatRelativeTime(createdAtMillis: Long): String {
    val diffMs = (System.currentTimeMillis() - createdAtMillis).coerceAtLeast(0L)
    val mins = diffMs / 60_000L
    return when {
        mins < 2L -> stringResource(R.string.notif_time_just_now)
        mins < 60L -> stringResource(R.string.notif_time_mins, mins.toInt())
        mins < 1440L -> stringResource(R.string.notif_time_hours, (mins / 60L).toInt())
        else -> stringResource(R.string.notif_time_days, (mins / 1440L).toInt())
    }
}
