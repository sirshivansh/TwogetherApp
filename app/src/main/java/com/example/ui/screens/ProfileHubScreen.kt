package com.example.ui.screens

import androidx.annotation.StringRes
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Backpack
import androidx.compose.material.icons.rounded.ChangeHistory
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Hexagon
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Park
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.QuestionAnswer
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.StudentProfileEntity
import com.example.ui.components.ClayLanguageSelectorButton
import com.example.ui.components.ForgotPasswordDialog
import com.example.ui.components.TwogetherBrandHeader
import com.example.ui.components.twogetherAutofill
import com.example.ui.theme.AliveAmbientBackground
import com.example.ui.theme.AlivePulseDot
import com.example.ui.theme.AliveStaggeredEntry
import com.example.ui.theme.ClayButton
import com.example.ui.theme.ClayCard
import com.example.ui.theme.ClayError
import com.example.ui.theme.ClayErrorContainer
import com.example.ui.theme.CocoaSecondary
import com.example.ui.theme.ElevatedClayCard
import com.example.ui.theme.EspressoInk
import com.example.ui.theme.LeafGreen
import com.example.ui.theme.LeafGreenSoft
import com.example.ui.theme.ListenButton
import com.example.ui.theme.SoftOatSurface
import com.example.ui.theme.StudentAvatar
import com.example.ui.theme.StudentAvatarBadge
import com.example.ui.theme.StudentThemeColor
import com.example.ui.theme.SunAmber
import com.example.ui.theme.SunAmberSoft
import com.example.ui.theme.TerracottaCoral
import com.example.ui.theme.TerracottaSoft
import com.example.ui.theme.WarmCreamBg
import com.example.util.TtsSpeaker

data class PinShapeToken(
    val index: Int,
    @StringRes val labelRes: Int,
    val color: Color,
    val icon: ImageVector
)

val pinShapeTokens = listOf(
    PinShapeToken(0, R.string.pin_shape_sun, SunAmber, Icons.Rounded.Circle),
    PinShapeToken(1, R.string.pin_shape_leaf, LeafGreen, Icons.Rounded.ChangeHistory),
    PinShapeToken(2, R.string.pin_shape_star, TerracottaCoral, Icons.Rounded.Star),
    PinShapeToken(3, R.string.pin_shape_berry, Color(0xFF9C4768), Icons.Rounded.Hexagon)
)

@StringRes
fun themeColorLabelRes(key: String): Int = when (key) {
    StudentThemeColor.MARIGOLD.key -> R.string.theme_color_marigold
    StudentThemeColor.SAGE.key -> R.string.theme_color_sage
    StudentThemeColor.CINNAMON.key -> R.string.theme_color_cinnamon
    StudentThemeColor.PLUM.key -> R.string.theme_color_plum
    else -> R.string.theme_color_terracotta
}

@StringRes
fun languageFilterLabelRes(lang: String): Int = when (lang) {
    "English" -> R.string.lang_english
    "Hindi" -> R.string.lang_hindi
    "Bilingual" -> R.string.lang_bilingual
    else -> R.string.lang_all_languages
}

enum class PortalRoleChoice(
    val number: String,
    val roleKey: String,
    @StringRes val labelRes: Int,
    @StringRes val subtitleRes: Int,
    val icon: ImageVector,
    val accent: Color,
    val bgTint: Color
) {
    STUDENT(
        number = "1",
        roleKey = "STUDENT",
        labelRes = R.string.role_student,
        subtitleRes = R.string.role_student_subtitle,
        icon = Icons.Rounded.Park,
        accent = LeafGreen,
        bgTint = LeafGreenSoft
    ),
    EDUCATOR(
        number = "2",
        roleKey = "EDUCATOR",
        labelRes = R.string.role_educator,
        subtitleRes = R.string.role_educator_subtitle,
        icon = Icons.Rounded.School,
        accent = SunAmber,
        bgTint = SunAmberSoft
    ),
    ADMIN(
        number = "3",
        roleKey = "ADMIN",
        labelRes = R.string.role_admin,
        subtitleRes = R.string.role_admin_subtitle,
        icon = Icons.Rounded.AdminPanelSettings,
        accent = TerracottaCoral,
        bgTint = TerracottaSoft
    );

    companion object {
        fun fromKey(key: String?): PortalRoleChoice? =
            entries.firstOrNull { it.roleKey.equals(key, ignoreCase = true) }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalComposeUiApi::class)
@Composable
fun ProfileHubScreen(
    profiles: List<StudentProfileEntity>,
    isOnline: Boolean,
    isGoogleConnected: Boolean = true,
    unreadNotificationsCount: Int,
    authPortalError: String?,
    initialRoleKey: String? = null,
    onConsumeInitialRole: () -> Unit = {},
    ttsSpeaker: TtsSpeaker,
    speakingId: String?,
    onConnectGoogle: () -> Unit = {},
    onLoginWithEmail: (String, String, String) -> Unit,
    onResetPassword: (String, String, String, (Result<String>) -> Unit) -> Unit = { _, _, _, cb -> cb(Result.failure(IllegalStateException())) },
    onClearAuthError: () -> Unit,
    onUnlockStudent: (StudentProfileEntity) -> Unit,
    onRegisterStudent: (String, String, String, Int, String, String, String, Boolean, String, String) -> Unit,
    onRegisterEducator: (String, String, String, String, String) -> Unit,
    onRegisterAdmin: (String, String, String) -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenGuide: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    // null = Step 1 "Login as: 1. Student, 2. Educator, 3. Admin" prompt
    // non-null = Step 2 dedicated Login / Sign Up page with email & password inputs
    var activeRole by remember { mutableStateOf(PortalRoleChoice.fromKey(initialRoleKey)) }
    var isSignUpMode by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    LaunchedEffect(initialRoleKey) {
        val mapped = PortalRoleChoice.fromKey(initialRoleKey)
        if (mapped != null) {
            activeRole = mapped
            isSignUpMode = false
            onConsumeInitialRole()
        }
    }

    if (activeRole != null) {
        BackHandler {
            activeRole = null
            isSignUpMode = false
            onClearAuthError()
        }
    }

    // Login inputs
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var showLoginPassword by remember { mutableStateOf(false) }

    // Shared Sign Up fields
    var signUpName by remember { mutableStateOf("") }
    var signUpEmail by remember { mutableStateOf("") }
    var signUpPassword by remember { mutableStateOf("") }
    var showSignUpPassword by remember { mutableStateOf(false) }

    // Student Sign Up fields
    var studentCenter by remember { mutableStateOf("Asha Kiran NGO Center") }
    var studentClass by remember { mutableIntStateOf(5) }
    var studentLanguage by remember { mutableStateOf("Bilingual") }
    var studentAvatar by remember { mutableStateOf("lion") }
    var studentTheme by remember { mutableStateOf("terracotta") }
    val studentPinList = remember { mutableStateListOf(0, 1, 2, 3) }
    var studentConsent by remember { mutableStateOf(true) }

    // Educator Sign Up fields
    var educatorSubject by remember { mutableStateOf("Mathematics") }
    var educatorTier by remember { mutableStateOf("TIER_1_PAID") }

    // Shared device kid switcher state
    var showKidGrid by remember { mutableStateOf(true) }
    var selectedForPin by remember { mutableStateOf<StudentProfileEntity?>(null) }

    val resetAuthForms: (PortalRoleChoice?) -> Unit = { nextRole ->
        activeRole = nextRole
        isSignUpMode = false
        loginEmail = ""
        loginPassword = ""
        signUpName = ""
        signUpEmail = ""
        signUpPassword = ""
        onClearAuthError()
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        AliveAmbientBackground()

        val selectedRoleLabel = activeRole?.let { stringResource(it.labelRes) }.orEmpty()

        LazyColumn(
            contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp)
        ) {
            // Top Utility Bar
            item {
                AliveStaggeredEntry(index = 0) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.app_name),
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = EspressoInk,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = stringResource(R.string.app_tagline),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CocoaSecondary
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                ClayLanguageSelectorButton()
                                IconButton(
                                    onClick = onOpenNotifications,
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("hub_notifications_button")
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
                                        .testTag("hub_guide_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.HelpOutline,
                                        contentDescription = stringResource(R.string.common_cd_quick_guide),
                                        tint = CocoaSecondary
                                    )
                                }
                                IconButton(
                                    onClick = onSignOut,
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("sign_out_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.Logout,
                                        contentDescription = stringResource(R.string.hub_cd_sign_out),
                                        tint = CocoaSecondary
                                    )
                                }
                            }
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isOnline) LeafGreenSoft else SunAmberSoft
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    AlivePulseDot(
                                        color = if (isOnline) LeafGreen else SunAmber,
                                        sizeDp = 7.dp
                                    )
                                    Text(
                                        text = stringResource(
                                            if (isOnline) R.string.common_online_sync_ready
                                            else R.string.common_offline_first_ready
                                        ),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = EspressoInk
                                    )
                                }
                            }
                            if (!isGoogleConnected) {
                                Surface(
                                    onClick = onConnectGoogle,
                                    shape = RoundedCornerShape(20.dp),
                                    color = TerracottaSoft,
                                    modifier = Modifier.testTag("connect_google_chip")
                                ) {
                                    Text(
                                        text = stringResource(R.string.hub_connect_google_sync),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = EspressoInk,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                            val hubIntroRolesSpeech = stringResource(R.string.hub_intro_speech_roles)
                            val activeRoleLabel = activeRole?.let { stringResource(it.labelRes) } ?: ""
                            val hubIntroPortalSpeech = stringResource(R.string.hub_intro_speech_portal, activeRoleLabel)
                            ListenButton(
                                isSpeaking = speakingId == "hub_intro",
                                onClick = {
                                    val speech = if (activeRole == null) {
                                        hubIntroRolesSpeech
                                    } else {
                                        hubIntroPortalSpeech
                                    }
                                    ttsSpeaker.toggleSpeak("hub_intro", speech)
                                },
                                testTag = "hub_listen_button"
                            )
                        }
                    }
                }
            }

            if (activeRole == null) {
                item {
                    AliveStaggeredEntry(index = 0) {
                        TwogetherBrandHeader(compact = true)
                    }
                }

                // ========================================================
                // STEP 1: "Login as: 1. Student, 2. Educator, 3. Admin"
                // ========================================================
                item {
                    AliveStaggeredEntry(index = 1) {
                        ClayCard(
                            containerColor = ElevatedClayCard,
                            cornerRadius = 28.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.auth_login_as),
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = EspressoInk,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = stringResource(R.string.auth_select_role_desc),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CocoaSecondary
                                )

                                PortalRoleChoice.entries.forEach { role ->
                                    val roleLabel = stringResource(role.labelRes)
                                    ClayCard(
                                        onClick = { resetAuthForms(role) },
                                        containerColor = role.bgTint,
                                        cornerRadius = 24.dp,
                                        elevationDp = 4.dp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("role_choice_${role.roleKey.lowercase()}")
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(CircleShape)
                                                    .background(role.accent),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = role.number,
                                                    style = MaterialTheme.typography.titleLarge,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .background(Color.White.copy(alpha = 0.78f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = role.icon,
                                                    contentDescription = null,
                                                    tint = role.accent,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "${role.number}. $roleLabel",
                                                    style = MaterialTheme.typography.titleLarge,
                                                    color = EspressoInk,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = stringResource(role.subtitleRes),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = CocoaSecondary
                                                )
                                            }
                                            Surface(
                                                shape = CircleShape,
                                                color = Color.White.copy(alpha = 0.85f),
                                                modifier = Modifier.size(34.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                                        contentDescription = stringResource(R.string.hub_open_role_cd, roleLabel),
                                                        tint = role.accent,
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
                }

                // Core Highlights Strip
                item {
                    AliveStaggeredEntry(index = 2) {
                        ClayCard(
                            containerColor = SoftOatSurface,
                            cornerRadius = 24.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.hub_core_features),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = EspressoInk,
                                    fontWeight = FontWeight.Bold
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CoreHighlightTag(Icons.Rounded.Backpack, stringResource(R.string.hub_feat_backpack), LeafGreenSoft)
                                    CoreHighlightTag(Icons.Rounded.AutoAwesome, stringResource(R.string.hub_feat_ai), SunAmberSoft)
                                    CoreHighlightTag(Icons.Rounded.QuestionAnswer, stringResource(R.string.hub_feat_doubts), TerracottaSoft)
                                    CoreHighlightTag(Icons.Rounded.VerifiedUser, stringResource(R.string.hub_feat_verify), LeafGreenSoft)
                                }
                            }
                        }
                    }
                }

                // Shared Device Quick Kid PIN Switcher
                item {
                    AliveStaggeredEntry(index = 3) {
                        SharedDeviceKidSwitcherCard(
                            profiles = profiles,
                            expanded = showKidGrid,
                            onToggleExpand = { showKidGrid = !showKidGrid },
                            onSelectProfile = { selectedForPin = it },
                            onAddNewKid = {
                                resetAuthForms(PortalRoleChoice.STUDENT)
                                isSignUpMode = true
                            }
                        )
                    }
                }
            } else {
                // ========================================================
                // STEP 2: Dedicated Role Login / Sign Up Page
                // ========================================================
                val role = activeRole!!
                val roleLabel = selectedRoleLabel

                item {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            onClick = { resetAuthForms(null) },
                            shape = RoundedCornerShape(18.dp),
                            color = SoftOatSurface,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("back_to_roles_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = stringResource(R.string.hub_cd_back_roles),
                                    tint = EspressoInk,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = stringResource(R.string.hub_change_role),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = EspressoInk,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = role.bgTint
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(role.icon, contentDescription = null, tint = role.accent, modifier = Modifier.size(18.dp))
                                Text(
                                    text = stringResource(R.string.hub_role_portal_badge, role.number, roleLabel),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = EspressoInk,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                item {
                    ClayCard(
                        containerColor = ElevatedClayCard,
                        cornerRadius = 28.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Login | Sign Up Toggle Pills
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(SoftOatSurface)
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    false to stringResource(R.string.auth_login),
                                    true to stringResource(R.string.auth_sign_up)
                                ).forEach { (signUpTab, label) ->
                                    val selected = isSignUpMode == signUpTab
                                    Surface(
                                        onClick = {
                                            isSignUpMode = signUpTab
                                            onClearAuthError()
                                        },
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (selected) role.accent else Color.Transparent,
                                        modifier = Modifier
                                            .weight(1f)
                                            .minimumInteractiveComponentSize()
                                            .testTag(if (signUpTab) "auth_mode_signup" else "auth_mode_login")
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 11.dp, horizontal = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = if (selected) Color.White else EspressoInk,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }

                            if (!isSignUpMode) {
                                // ---------- LOGIN VIEW ----------
                                Text(
                                    text = stringResource(R.string.hub_role_login_title, roleLabel),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = EspressoInk,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = stringResource(role.subtitleRes),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CocoaSecondary
                                )

                                OutlinedTextField(
                                    value = loginEmail,
                                    onValueChange = {
                                        loginEmail = it
                                        if (authPortalError != null) onClearAuthError()
                                    },
                                    label = { Text(stringResource(R.string.auth_role_email, roleLabel)) },
                                    leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = SoftOatSurface.copy(alpha = 0.4f),
                                        unfocusedContainerColor = SoftOatSurface.copy(alpha = 0.4f),
                                        unfocusedBorderColor = Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .twogetherAutofill(listOf(AutofillType.EmailAddress)) { filled ->
                                            loginEmail = filled
                                            if (authPortalError != null) onClearAuthError()
                                        }
                                        .testTag("portal_email_input")
                                )

                                OutlinedTextField(
                                    value = loginPassword,
                                    onValueChange = {
                                        loginPassword = it
                                        if (authPortalError != null) onClearAuthError()
                                    },
                                    label = { Text(stringResource(R.string.auth_password)) },
                                    leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                                    trailingIcon = {
                                        IconButton(onClick = { showLoginPassword = !showLoginPassword }) {
                                            Icon(
                                                imageVector = if (showLoginPassword) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                                contentDescription = stringResource(R.string.auth_toggle_password_cd)
                                            )
                                        }
                                    },
                                    singleLine = true,
                                    visualTransformation = if (showLoginPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = SoftOatSurface.copy(alpha = 0.4f),
                                        unfocusedContainerColor = SoftOatSurface.copy(alpha = 0.4f),
                                        unfocusedBorderColor = Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .twogetherAutofill(listOf(AutofillType.Password)) { filled ->
                                            loginPassword = filled
                                            if (authPortalError != null) onClearAuthError()
                                        }
                                        .testTag("portal_password_input")
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = { showForgotPasswordDialog = true },
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .testTag("portal_forgot_password_button")
                                    ) {
                                        Text(
                                            text = stringResource(R.string.auth_forgot_password),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = role.accent,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                if (authPortalError != null) {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = ClayErrorContainer,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = authPortalError,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = EspressoInk,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }

                                ClayButton(
                                    text = stringResource(R.string.auth_login_as_role, roleLabel),
                                    containerColor = role.accent,
                                    onClick = {
                                        onLoginWithEmail(role.roleKey, loginEmail, loginPassword)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("portal_login_button")
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    TextButton(
                                        onClick = {
                                            isSignUpMode = true
                                            onClearAuthError()
                                        },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Text(
                                            text = stringResource(R.string.auth_new_role_sign_up, roleLabel),
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            } else {
                                // ---------- SIGN UP VIEW ----------
                                Text(
                                    text = stringResource(R.string.hub_role_signup_title, roleLabel),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = EspressoInk,
                                    fontWeight = FontWeight.Bold
                                )

                                OutlinedTextField(
                                    value = signUpName,
                                    onValueChange = { signUpName = it },
                                    label = { Text(stringResource(R.string.auth_full_name)) },
                                    leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(18.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("signup_name_input")
                                )

                                OutlinedTextField(
                                    value = signUpEmail,
                                    onValueChange = {
                                        signUpEmail = it
                                        if (authPortalError != null) onClearAuthError()
                                    },
                                    label = { Text(stringResource(R.string.auth_email_address)) },
                                    leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    shape = RoundedCornerShape(18.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .twogetherAutofill(listOf(AutofillType.EmailAddress)) { filled ->
                                            signUpEmail = filled
                                            if (authPortalError != null) onClearAuthError()
                                        }
                                        .testTag("signup_email_input")
                                )

                                OutlinedTextField(
                                    value = signUpPassword,
                                    onValueChange = {
                                        signUpPassword = it
                                        if (authPortalError != null) onClearAuthError()
                                    },
                                    label = { Text(stringResource(R.string.auth_password_min_6)) },
                                    leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                                    trailingIcon = {
                                        IconButton(onClick = { showSignUpPassword = !showSignUpPassword }) {
                                            Icon(
                                                imageVector = if (showSignUpPassword) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                                contentDescription = stringResource(R.string.auth_toggle_password_cd)
                                            )
                                        }
                                    },
                                    singleLine = true,
                                    visualTransformation = if (showSignUpPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    shape = RoundedCornerShape(18.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .twogetherAutofill(listOf(AutofillType.NewPassword, AutofillType.Password)) { filled ->
                                            signUpPassword = filled
                                            if (authPortalError != null) onClearAuthError()
                                        }
                                        .testTag("signup_password_input")
                                )

                                when (role) {
                                    PortalRoleChoice.STUDENT -> {
                                        OutlinedTextField(
                                            value = studentCenter,
                                            onValueChange = { studentCenter = it },
                                            label = { Text(stringResource(R.string.auth_school_ngo_center)) },
                                            singleLine = true,
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Text(stringResource(R.string.auth_class_1_to_10), style = MaterialTheme.typography.titleMedium)
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            (1..10).forEach { c ->
                                                val active = studentClass == c
                                                Surface(
                                                    onClick = { studentClass = c },
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = if (active) role.accent else SoftOatSurface,
                                                    modifier = Modifier.minimumInteractiveComponentSize()
                                                ) {
                                                    Text(
                                                        text = "$c",
                                                        color = if (active) Color.White else EspressoInk,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Text(stringResource(R.string.hub_preferred_language), style = MaterialTheme.typography.titleMedium)
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            listOf("Bilingual", "English", "Hindi").forEach { lang ->
                                                FilterChip(
                                                    selected = studentLanguage == lang,
                                                    onClick = { studentLanguage = lang },
                                                    label = { Text(stringResource(languageFilterLabelRes(lang))) }
                                                )
                                            }
                                        }

                                        Text(stringResource(R.string.hub_animal_avatar), style = MaterialTheme.typography.titleMedium)
                                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            StudentAvatar.entries.forEach { avatar ->
                                                val selected = studentAvatar == avatar.id
                                                Box(
                                                    modifier = Modifier
                                                        .clip(CircleShape)
                                                        .border(
                                                            width = if (selected) 3.dp else 1.dp,
                                                            color = if (selected) role.accent else Color.Transparent,
                                                            shape = CircleShape
                                                        )
                                                        .clickable { studentAvatar = avatar.id }
                                                        .padding(3.dp)
                                                ) {
                                                    StudentAvatarBadge(avatarId = avatar.id, size = 44.dp)
                                                }
                                            }
                                        }

                                        Text(stringResource(R.string.hub_favorite_theme), style = MaterialTheme.typography.titleMedium)
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            StudentThemeColor.entries.forEach { theme ->
                                                val selected = studentTheme == theme.key
                                                Surface(
                                                    onClick = { studentTheme = theme.key },
                                                    shape = RoundedCornerShape(16.dp),
                                                    color = theme.primaryContainer,
                                                    border = BorderStroke(
                                                        if (selected) 2.5.dp else 1.dp,
                                                        if (selected) theme.primary else Color.Transparent
                                                    )
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(14.dp)
                                                                .clip(CircleShape)
                                                                .background(theme.primary)
                                                        )
                                                        Spacer(Modifier.width(6.dp))
                                                        Text(
                                                            text = stringResource(themeColorLabelRes(theme.key)),
                                                            style = MaterialTheme.typography.labelMedium,
                                                            color = EspressoInk
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Text(stringResource(R.string.hub_quick_pin), style = MaterialTheme.typography.titleMedium)
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            pinShapeTokens.forEach { tok ->
                                                Surface(
                                                    onClick = {
                                                        if (studentPinList.size >= 4) studentPinList.clear()
                                                        studentPinList.add(tok.index)
                                                    },
                                                    shape = RoundedCornerShape(14.dp),
                                                    color = tok.color.copy(alpha = 0.16f),
                                                    modifier = Modifier.minimumInteractiveComponentSize()
                                                ) {
                                                    Icon(
                                                        imageVector = tok.icon,
                                                        contentDescription = stringResource(tok.labelRes),
                                                        tint = tok.color,
                                                        modifier = Modifier.padding(10.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(stringResource(R.string.hub_selected_pin), style = MaterialTheme.typography.labelMedium)
                                            studentPinList.forEach { idx ->
                                                val tok = pinShapeTokens.getOrElse(idx) { pinShapeTokens[0] }
                                                Icon(tok.icon, contentDescription = null, tint = tok.color, modifier = Modifier.size(18.dp))
                                            }
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.clickable { studentConsent = !studentConsent }
                                        ) {
                                            Checkbox(
                                                checked = studentConsent,
                                                onCheckedChange = { studentConsent = it }
                                            )
                                            Text(
                                                text = stringResource(R.string.auth_parent_guardian_consent),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = EspressoInk
                                            )
                                        }
                                    }

                                    PortalRoleChoice.EDUCATOR -> {
                                        OutlinedTextField(
                                            value = educatorSubject,
                                            onValueChange = { educatorSubject = it },
                                            label = { Text(stringResource(R.string.auth_primary_subject_specialty)) },
                                            singleLine = true,
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Text(stringResource(R.string.auth_volunteer_tier), style = MaterialTheme.typography.titleMedium)
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            listOf(
                                                "TIER_1_PAID" to R.string.auth_tier_1_paid,
                                                "TIER_2_UNPAID" to R.string.auth_tier_2_unpaid,
                                                "TIER_3_RETIRED" to R.string.auth_tier_3_retired
                                            ).forEach { (key, labelRes) ->
                                                FilterChip(
                                                    selected = educatorTier == key,
                                                    onClick = { educatorTier = key },
                                                    label = { Text(stringResource(labelRes)) }
                                                )
                                            }
                                        }
                                    }

                                    PortalRoleChoice.ADMIN -> {
                                        Text(
                                            text = stringResource(R.string.auth_admin_portal_desc),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = CocoaSecondary
                                        )
                                    }
                                }

                                if (authPortalError != null) {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = ClayErrorContainer,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = authPortalError,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = EspressoInk,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }

                                val canSignUp = signUpName.trim().isNotEmpty() &&
                                    signUpEmail.contains("@") &&
                                    signUpPassword.length >= 6 &&
                                    (role != PortalRoleChoice.STUDENT || (studentPinList.size == 4 && studentConsent))

                                ClayButton(
                                    text = stringResource(R.string.auth_sign_up_as_role, roleLabel),
                                    containerColor = role.accent,
                                    enabled = canSignUp,
                                    onClick = {
                                        when (role) {
                                            PortalRoleChoice.STUDENT -> onRegisterStudent(
                                                signUpName,
                                                signUpEmail,
                                                signUpPassword,
                                                studentClass,
                                                studentAvatar,
                                                studentTheme,
                                                studentLanguage,
                                                studentConsent,
                                                studentCenter,
                                                studentPinList.joinToString(",")
                                            )
                                            PortalRoleChoice.EDUCATOR -> onRegisterEducator(
                                                signUpName,
                                                signUpEmail,
                                                signUpPassword,
                                                educatorTier,
                                                educatorSubject
                                            )
                                            PortalRoleChoice.ADMIN -> onRegisterAdmin(
                                                signUpName,
                                                signUpEmail,
                                                signUpPassword
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("portal_signup_button")
                                )
                            }
                        }
                    }
                }

                if (role == PortalRoleChoice.STUDENT && !isSignUpMode) {
                    item {
                        SharedDeviceKidSwitcherCard(
                            profiles = profiles,
                            expanded = showKidGrid,
                            onToggleExpand = { showKidGrid = !showKidGrid },
                            onSelectProfile = { selectedForPin = it },
                            onAddNewKid = { isSignUpMode = true }
                        )
                    }
                }
            }
        }
    }

    selectedForPin?.let { profile ->
        PinUnlockDialog(
            profile = profile,
            onDismiss = { selectedForPin = null },
            onUnlocked = {
                selectedForPin = null
                onUnlockStudent(profile)
            }
        )
    }

    if (showForgotPasswordDialog && activeRole != null) {
        val role = activeRole!!
        val roleLabel = stringResource(role.labelRes)
        ForgotPasswordDialog(
            roleLabel = roleLabel,
            initialEmail = loginEmail,
            accentColor = role.accent,
            onDismiss = { showForgotPasswordDialog = false },
            onSubmitReset = { resetEmail, newPw, onDone ->
                onResetPassword(role.roleKey, resetEmail, newPw) { result ->
                    result.onSuccess {
                        loginEmail = resetEmail
                        loginPassword = newPw
                    }
                    onDone(result)
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SharedDeviceKidSwitcherCard(
    profiles: List<StudentProfileEntity>,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onSelectProfile: (StudentProfileEntity) -> Unit,
    onAddNewKid: () -> Unit
) {
    ClayCard(
        containerColor = ElevatedClayCard,
        cornerRadius = 26.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.hub_shared_kid_switch),
                        style = MaterialTheme.typography.titleLarge,
                        color = EspressoInk
                    )
                    Text(
                        text = stringResource(R.string.hub_shared_kid_sub),
                        style = MaterialTheme.typography.bodyMedium,
                        color = CocoaSecondary
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = stringResource(R.string.hub_cd_toggle_kid_switch)
                )
            }

            AnimatedVisibility(visible = expanded) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    profiles.forEach { profile ->
                        val themeColor = StudentThemeColor.fromKey(profile.themeColorKey)
                        ClayCard(
                            onClick = { onSelectProfile(profile) },
                            containerColor = themeColor.primaryContainer.copy(alpha = 0.55f),
                            cornerRadius = 24.dp,
                            modifier = Modifier
                                .widthIn(min = 138.dp, max = 168.dp)
                                .testTag("student_profile_card_${profile.name.lowercase()}")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                StudentAvatarBadge(avatarId = profile.avatarId, size = 56.dp)
                                Text(
                                    text = profile.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = EspressoInk,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = themeColor.primary.copy(alpha = 0.16f)
                                ) {
                                    Text(
                                        text = stringResource(
                                            R.string.hub_kid_xp_badge,
                                            profile.gradeClass,
                                            profile.xpPoints
                                        ),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = EspressoInk,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }

                    ClayCard(
                        onClick = onAddNewKid,
                        containerColor = SoftOatSurface,
                        cornerRadius = 24.dp,
                        modifier = Modifier
                            .widthIn(min = 138.dp, max = 168.dp)
                            .testTag("add_student_profile_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.8f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = stringResource(R.string.hub_cd_add_student),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Text(
                                text = stringResource(R.string.hub_new_kid),
                                style = MaterialTheme.typography.titleMedium,
                                color = EspressoInk,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = stringResource(R.string.hub_classes_1_10),
                                style = MaterialTheme.typography.labelMedium,
                                color = CocoaSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CoreHighlightTag(icon: ImageVector, label: String, bg: Color) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bg
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(icon, contentDescription = null, tint = EspressoInk, modifier = Modifier.size(16.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = EspressoInk, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PinUnlockDialog(
    profile: StudentProfileEntity,
    onDismiss: () -> Unit,
    onUnlocked: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val entered = remember(profile.id) { mutableStateListOf<Int>() }
    var wrongAttempt by remember { mutableStateOf(false) }

    val hintColors = remember(profile.pinSequence) {
        profile.pinSequence.split(",").mapNotNull { it.toIntOrNull() }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = WarmCreamBg,
        shape = RoundedCornerShape(30.dp),
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                StudentAvatarBadge(avatarId = profile.avatarId, size = 64.dp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.hub_hi_student, profile.name),
                    style = MaterialTheme.typography.headlineMedium,
                    color = EspressoInk
                )
                Text(
                    text = stringResource(R.string.hub_tap_pin),
                    style = MaterialTheme.typography.bodyMedium,
                    color = CocoaSecondary
                )
                if (hintColors.size == 4) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.hub_shape_hint),
                            style = MaterialTheme.typography.labelMedium,
                            color = CocoaSecondary
                        )
                        hintColors.forEach { idx ->
                            val tok = pinShapeTokens.getOrElse(idx) { pinShapeTokens[0] }
                            Icon(
                                imageVector = tok.icon,
                                contentDescription = stringResource(tok.labelRes),
                                tint = tok.color,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (slot in 0 until 4) {
                        val tokenIdx = entered.getOrNull(slot)
                        val token = tokenIdx?.let { pinShapeTokens.getOrNull(it) }
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(token?.color?.copy(alpha = 0.2f) ?: SoftOatSurface)
                                .border(
                                    2.dp,
                                    if (wrongAttempt) ClayError else (token?.color ?: CocoaSecondary.copy(alpha = 0.3f)),
                                    RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (token != null) {
                                Icon(
                                    imageVector = token.icon,
                                    contentDescription = stringResource(token.labelRes),
                                    tint = token.color,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                if (wrongAttempt) {
                    Text(
                        text = stringResource(R.string.hub_try_pin_again),
                        style = MaterialTheme.typography.labelMedium,
                        color = ClayError
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    pinShapeTokens.forEach { token ->
                        val tokLabel = stringResource(token.labelRes)
                        Surface(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                wrongAttempt = false
                                if (entered.size < 4) {
                                    entered.add(token.index)
                                    if (entered.size == 4) {
                                        val code = entered.joinToString(",")
                                        if (code == profile.pinSequence) {
                                            onUnlocked()
                                        } else {
                                            wrongAttempt = true
                                            entered.clear()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .testTag("pin_shape_${token.index}"),
                            shape = RoundedCornerShape(20.dp),
                            color = token.color.copy(alpha = 0.16f),
                            border = BorderStroke(2.dp, token.color.copy(alpha = 0.6f))
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Icon(
                                    imageVector = token.icon,
                                    contentDescription = tokLabel,
                                    tint = token.color,
                                    modifier = Modifier.size(26.dp)
                                )
                                Text(
                                    text = tokLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EspressoInk,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (entered.isNotEmpty()) {
                    entered.removeAt(entered.lastIndex)
                }
            }) {
                Icon(Icons.AutoMirrored.Rounded.Backspace, contentDescription = stringResource(R.string.hub_cd_delete_shape))
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.common_undo))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
        }
    )
}
