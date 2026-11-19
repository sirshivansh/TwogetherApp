package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Park
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.local.SessionStore
import com.example.data.repository.TwogetherRepository
import com.example.ui.components.ClayLanguageSelectorButton
import com.example.ui.components.ForgotPasswordDialog
import com.example.ui.components.TwogetherBrandHeader
import com.example.ui.components.localizedForCurrentLocale
import com.example.ui.components.twogetherAutofill
import com.example.ui.theme.AliveAmbientBackground
import com.example.ui.theme.AliveStaggeredEntry
import com.example.ui.theme.ClayButton
import com.example.ui.theme.ClayCard
import com.example.ui.theme.ClayErrorContainer
import com.example.ui.theme.CocoaSecondary
import com.example.ui.theme.EspressoInk
import com.example.ui.theme.ListenButton
import com.example.ui.theme.SoftOatSurface
import com.example.ui.theme.WarmCreamBg
import com.example.ui.viewmodel.AppRoleMode
import com.example.util.NetworkMonitor
import com.example.util.TtsSpeaker
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class LocalAuthLaunch(
    val workspaceUid: String,
    val roleMode: AppRoleMode,
    val entityId: String?
)

fun attemptAutoSignIn(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onUnauthenticated: () -> Unit,
    scope: CoroutineScope
) {
    if (Firebase.auth.currentUser != null) {
        onAuthSuccess()
        return
    }
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (_: Exception) {
        onUnauthenticated()
        return
    }
    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(clientId)
        .setAutoSelectEnabled(true)
        .build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()
    scope.launch {
        try {
            val cred = credentialManager.getCredential(context, request).credential
            if (cred is CustomCredential && cred.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val token = GoogleIdTokenCredential.createFrom(cred.data).idToken
                Firebase.auth.signInWithCredential(GoogleAuthProvider.getCredential(token, null)).await()
                onAuthSuccess()
            } else {
                onUnauthenticated()
            }
        } catch (_: Exception) {
            onUnauthenticated()
        }
    }
}

fun onGoogleSignInClicked(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    scope: CoroutineScope,
    onAuthCancelled: () -> Unit = {}
) {
    val locCtx = context.localizedForCurrentLocale()
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (_: Exception) {
        onAuthError(locCtx.getString(R.string.auth_err_google_config))
        return
    }
    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(signInOption)
        .build()
    scope.launch {
        try {
            val activity = context as? Activity
                ?: throw IllegalStateException(locCtx.getString(R.string.auth_err_google_activity))
            val cred = credentialManager.getCredential(activity, request).credential
            if (cred is CustomCredential && cred.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val token = GoogleIdTokenCredential.createFrom(cred.data).idToken
                Firebase.auth.signInWithCredential(GoogleAuthProvider.getCredential(token, null)).await()
                onAuthSuccess()
            } else {
                onAuthError(locCtx.getString(R.string.auth_err_google_cred_type))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
            onAuthCancelled()
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign-In failed", e)
            onAuthError(e.localizedMessage ?: locCtx.getString(R.string.auth_err_google_failed))
        }
    }
}

fun signOutTwogether(
    credentialManager: CredentialManager,
    scope: CoroutineScope,
    onSignOutComplete: () -> Unit = {}
) {
    scope.launch {
        withContext(NonCancellable) {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.w("Auth", "Failed to clear credential state: ${e.message}")
            } finally {
                Firebase.auth.signOut()
                onSignOutComplete()
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalComposeUiApi::class)
@Composable
fun AuthScreen(
    ttsSpeaker: TtsSpeaker,
    speakingId: String?,
    sessionStore: SessionStore,
    onAuthSuccess: (String?) -> Unit,
    onEmailAuthSuccess: (LocalAuthLaunch) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val repository = remember(context) { TwogetherRepository(context) }

    var selectedRole by remember { mutableStateOf(PortalRoleChoice.STUDENT) }
    var isSignUpMode by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    // Login inputs
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var showLoginPassword by remember { mutableStateOf(false) }

    // Sign Up inputs (users enter their own demo/real details)
    var signUpName by remember { mutableStateOf("") }
    var signUpEmail by remember { mutableStateOf("") }
    var signUpPassword by remember { mutableStateOf("") }
    var showSignUpPassword by remember { mutableStateOf(false) }
    var studentGrade by remember { mutableIntStateOf(5) }
    var studentCenter by remember { mutableStateOf("Asha Kiran NGO Center") }
    var studentLanguage by remember { mutableStateOf("Bilingual") }
    var studentConsent by remember { mutableStateOf(true) }
    var educatorSubject by remember { mutableStateOf("Mathematics") }
    var educatorTier by remember { mutableStateOf("TIER_1_PAID") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val startGoogleSignIn: () -> Unit = {
        errorMessage = null
        isLoading = true
        onGoogleSignInClicked(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {
                isLoading = false
                onAuthSuccess(selectedRole.roleKey)
            },
            onAuthError = { msg ->
                isLoading = false
                errorMessage = msg
            },
            scope = scope,
            onAuthCancelled = {
                isLoading = false
            }
        )
    }

    LaunchedEffect(Unit) {
        repository.ensureLocalSeedData(TwogetherRepository.LOCAL_WORKSPACE_UID)
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = { onAuthSuccess(null) },
            onUnauthenticated = {},
            scope = scope
        )
    }

    Scaffold(
        containerColor = WarmCreamBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            AliveAmbientBackground()

            Column(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top-right Animated Language Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ClayLanguageSelectorButton()
                }

                AliveStaggeredEntry(index = 0) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        TwogetherBrandHeader(
                            modifier = Modifier.fillMaxWidth(),
                            compact = false
                        )
                        val authWelcomeSpeech = stringResource(R.string.auth_welcome_speech)
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                        ) {
                            ListenButton(
                                isSpeaking = speakingId == "auth_welcome",
                                onClick = {
                                    ttsSpeaker.toggleSpeak("auth_welcome", authWelcomeSpeech)
                                },
                                testTag = "auth_listen_button"
                            )
                        }
                    }
                }

                AliveStaggeredEntry(index = 1) {
                    val selectedRoleLabel = stringResource(selectedRole.labelRes)
                    ClayCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.surface,
                        cornerRadius = 28.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // 1. Role Prompt ("Login as: 1. Student, 2. Educator, 3. Admin")
                            Text(
                                text = stringResource(R.string.auth_login_as),
                                style = MaterialTheme.typography.titleLarge,
                                color = EspressoInk,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PortalRoleChoice.entries.forEach { role ->
                                    val isSelected = selectedRole == role
                                    val roleLabel = stringResource(role.labelRes)
                                    Surface(
                                        onClick = {
                                            selectedRole = role
                                            errorMessage = null
                                        },
                                        shape = RoundedCornerShape(18.dp),
                                        color = if (isSelected) role.accent else role.bgTint,
                                        modifier = Modifier
                                            .weight(1f)
                                            .minimumInteractiveComponentSize()
                                            .testTag("auth_role_${role.roleKey.lowercase()}")
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = role.icon,
                                                contentDescription = roleLabel,
                                                tint = if (isSelected) Color.White else role.accent,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = "${role.number}. $roleLabel",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = if (isSelected) Color.White else EspressoInk,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center,
                                                maxLines = 2,
                                                softWrap = true,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }

                            // 2. Normal Website-Style Login | Sign Up Tabs
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(SoftOatSurface)
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    false to stringResource(R.string.auth_login),
                                    true to stringResource(R.string.auth_sign_up)
                                ).forEach { (signUpTab, label) ->
                                    val active = isSignUpMode == signUpTab
                                    Surface(
                                        onClick = {
                                            if (signUpTab && signUpEmail.isBlank() && loginEmail.isNotBlank()) {
                                                signUpEmail = loginEmail
                                            }
                                            isSignUpMode = signUpTab
                                            errorMessage = null
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (active) Color.White else Color.Transparent,
                                        shadowElevation = if (active) 2.dp else 0.dp,
                                        modifier = Modifier
                                            .weight(1f)
                                            .minimumInteractiveComponentSize()
                                            .testTag(if (signUpTab) "auth_tab_signup" else "auth_tab_login")
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = if (active) selectedRole.accent else CocoaSecondary,
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
                                // ---------- EMAIL & PASSWORD LOGIN ----------
                                OutlinedTextField(
                                    value = loginEmail,
                                    onValueChange = {
                                        loginEmail = it
                                        errorMessage = null
                                    },
                                    label = { Text(stringResource(R.string.auth_role_email, selectedRoleLabel)) },
                                    placeholder = { Text(stringResource(R.string.auth_email_placeholder)) },
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
                                            errorMessage = null
                                        }
                                        .testTag("auth_email_input")
                                )

                                OutlinedTextField(
                                    value = loginPassword,
                                    onValueChange = {
                                        loginPassword = it
                                        errorMessage = null
                                    },
                                    label = { Text(stringResource(R.string.auth_password)) },
                                    placeholder = { Text(stringResource(R.string.auth_password_placeholder)) },
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
                                            errorMessage = null
                                        }
                                        .testTag("auth_password_input")
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = { showForgotPasswordDialog = true },
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .testTag("auth_forgot_password_button")
                                    ) {
                                        Text(
                                            text = stringResource(R.string.auth_forgot_password),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = selectedRole.accent,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                if (errorMessage != null) {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = ClayErrorContainer,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = errorMessage.orEmpty(),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = EspressoInk,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }

                                if (isLoading) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = selectedRole.accent)
                                    }
                                } else {
                                    ClayButton(
                                        text = stringResource(R.string.auth_login_as_role, selectedRoleLabel),
                                        containerColor = selectedRole.accent,
                                        onClick = {
                                            errorMessage = null
                                            isLoading = true
                                            scope.launch {
                                                val res = repository.authenticateOnLaunch(
                                                    expectedRole = selectedRole.roleKey,
                                                    rawEmail = loginEmail,
                                                    rawPassword = loginPassword
                                                )
                                                isLoading = false
                                                res.onSuccess { account ->
                                                    val mode = when (account.role) {
                                                        "ADMIN" -> AppRoleMode.ADMIN
                                                        "EDUCATOR" -> AppRoleMode.EDUCATOR
                                                        else -> AppRoleMode.STUDENT
                                                    }
                                                    sessionStore.saveSession(
                                                        workspaceUid = account.ownerUid,
                                                        roleMode = account.role,
                                                        entityId = account.linkedEntityId,
                                                        userEmail = account.email,
                                                        userName = account.displayName
                                                    )
                                                    onEmailAuthSuccess(
                                                        LocalAuthLaunch(
                                                            workspaceUid = account.ownerUid,
                                                            roleMode = mode,
                                                            entityId = account.linkedEntityId
                                                        )
                                                    )
                                                }.onFailure { err ->
                                                    errorMessage = err.localizedMessage
                                                        ?: context.localizedForCurrentLocale().getString(R.string.auth_err_login_failed)
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("auth_login_button")
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    TextButton(
                                        onClick = {
                                            if (signUpEmail.isBlank() && loginEmail.isNotBlank()) {
                                                signUpEmail = loginEmail
                                            }
                                            isSignUpMode = true
                                            errorMessage = null
                                        },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Text(
                                            text = stringResource(R.string.auth_no_account_sign_up),
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            } else {
                                // ---------- EMAIL & PASSWORD SIGN UP ----------
                                OutlinedTextField(
                                    value = signUpName,
                                    onValueChange = {
                                        signUpName = it
                                        errorMessage = null
                                    },
                                    label = { Text(stringResource(R.string.auth_full_name)) },
                                    placeholder = { Text(stringResource(R.string.auth_enter_name)) },
                                    leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(18.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("auth_signup_name_input")
                                )

                                OutlinedTextField(
                                    value = signUpEmail,
                                    onValueChange = {
                                        signUpEmail = it
                                        errorMessage = null
                                    },
                                    label = { Text(stringResource(R.string.auth_email_address)) },
                                    placeholder = { Text(stringResource(R.string.auth_email_example)) },
                                    leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    shape = RoundedCornerShape(18.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .twogetherAutofill(listOf(AutofillType.EmailAddress)) { filled ->
                                            signUpEmail = filled
                                            errorMessage = null
                                        }
                                        .testTag("auth_signup_email_input")
                                )

                                OutlinedTextField(
                                    value = signUpPassword,
                                    onValueChange = {
                                        signUpPassword = it
                                        errorMessage = null
                                    },
                                    label = { Text(stringResource(R.string.auth_create_password)) },
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
                                            errorMessage = null
                                        }
                                        .testTag("auth_signup_password_input")
                                )

                                when (selectedRole) {
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
                                                val active = studentGrade == c
                                                Surface(
                                                    onClick = { studentGrade = c },
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = if (active) selectedRole.accent else SoftOatSurface,
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
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.clickable { studentConsent = !studentConsent }
                                        ) {
                                            Checkbox(
                                                checked = studentConsent,
                                                onCheckedChange = { studentConsent = it }
                                            )
                                            Text(
                                                text = stringResource(R.string.auth_guardian_consent),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = EspressoInk
                                            )
                                        }
                                    }

                                    PortalRoleChoice.EDUCATOR -> {
                                        OutlinedTextField(
                                            value = educatorSubject,
                                            onValueChange = { educatorSubject = it },
                                            label = { Text(stringResource(R.string.auth_subject_specialty)) },
                                            singleLine = true,
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        )
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
                                            text = stringResource(R.string.auth_admin_signup_note),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = CocoaSecondary
                                        )
                                    }
                                }

                                if (errorMessage != null) {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = ClayErrorContainer,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = errorMessage.orEmpty(),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = EspressoInk,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }

                                if (isLoading) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = selectedRole.accent)
                                    }
                                } else {
                                    ClayButton(
                                        text = stringResource(R.string.auth_sign_up_as_role, selectedRoleLabel),
                                        containerColor = selectedRole.accent,
                                        onClick = {
                                            val locCtx = context.localizedForCurrentLocale()
                                            val cleanName = signUpName.trim()
                                            val cleanEmail = signUpEmail.trim()
                                            if (cleanName.isBlank()) {
                                                errorMessage = locCtx.getString(R.string.auth_err_full_name)
                                                return@ClayButton
                                            }
                                            if (!cleanEmail.contains("@")) {
                                                errorMessage = locCtx.getString(R.string.auth_err_valid_email)
                                                return@ClayButton
                                            }
                                            if (signUpPassword.length < 6) {
                                                errorMessage = locCtx.getString(R.string.auth_err_password_len)
                                                return@ClayButton
                                            }
                                            if (selectedRole == PortalRoleChoice.STUDENT && !studentConsent) {
                                                errorMessage = locCtx.getString(R.string.auth_err_consent)
                                                return@ClayButton
                                            }

                                            errorMessage = null
                                            isLoading = true
                                            val workspaceUid = TwogetherRepository.LOCAL_WORKSPACE_UID
                                            scope.launch {
                                                when (selectedRole) {
                                                    PortalRoleChoice.STUDENT -> {
                                                        val res = repository.createStudentProfile(
                                                            uid = workspaceUid,
                                                            name = cleanName,
                                                            email = cleanEmail,
                                                            password = signUpPassword,
                                                            gradeClass = studentGrade,
                                                            avatarId = "lion",
                                                            themeColorKey = "terracotta",
                                                            preferredLanguage = studentLanguage,
                                                            parentalConsentGiven = studentConsent,
                                                            centerName = studentCenter,
                                                            pinSequence = "0,1,2,3"
                                                        )
                                                        isLoading = false
                                                        res.onSuccess { created ->
                                                            sessionStore.saveSession(
                                                                workspaceUid = workspaceUid,
                                                                roleMode = "STUDENT",
                                                                entityId = created.id,
                                                                userEmail = created.email,
                                                                userName = created.name
                                                            )
                                                            onEmailAuthSuccess(
                                                                LocalAuthLaunch(
                                                                    workspaceUid = workspaceUid,
                                                                    roleMode = AppRoleMode.STUDENT,
                                                                    entityId = created.id
                                                                )
                                                            )
                                                        }.onFailure { err ->
                                                            errorMessage = err.localizedMessage
                                                                ?: locCtx.getString(R.string.auth_err_signup_failed)
                                                        }
                                                    }

                                                    PortalRoleChoice.EDUCATOR -> {
                                                        val res = repository.registerEducatorWithEmail(
                                                            uid = workspaceUid,
                                                            fullName = cleanName,
                                                            email = cleanEmail,
                                                            password = signUpPassword,
                                                            tier = educatorTier,
                                                            subjectSpecialty = educatorSubject
                                                        )
                                                        isLoading = false
                                                        res.onSuccess { created ->
                                                            sessionStore.saveSession(
                                                                workspaceUid = workspaceUid,
                                                                roleMode = "EDUCATOR",
                                                                entityId = created.id,
                                                                userEmail = created.email,
                                                                userName = created.fullName
                                                            )
                                                            onEmailAuthSuccess(
                                                                LocalAuthLaunch(
                                                                    workspaceUid = workspaceUid,
                                                                    roleMode = AppRoleMode.EDUCATOR,
                                                                    entityId = created.id
                                                                )
                                                            )
                                                        }.onFailure { err ->
                                                            errorMessage = err.localizedMessage
                                                                ?: locCtx.getString(R.string.auth_err_signup_failed)
                                                        }
                                                    }

                                                    PortalRoleChoice.ADMIN -> {
                                                        val res = repository.registerAdminAccount(
                                                            uid = workspaceUid,
                                                            fullName = cleanName,
                                                            email = cleanEmail,
                                                            password = signUpPassword
                                                        )
                                                        isLoading = false
                                                        res.onSuccess { created ->
                                                            sessionStore.saveSession(
                                                                workspaceUid = workspaceUid,
                                                                roleMode = "ADMIN",
                                                                entityId = created.linkedEntityId,
                                                                userEmail = created.email,
                                                                userName = created.displayName
                                                            )
                                                            onEmailAuthSuccess(
                                                                LocalAuthLaunch(
                                                                    workspaceUid = workspaceUid,
                                                                    roleMode = AppRoleMode.ADMIN,
                                                                    entityId = created.linkedEntityId
                                                                )
                                                            )
                                                        }.onFailure { err ->
                                                            errorMessage = err.localizedMessage
                                                                ?: locCtx.getString(R.string.auth_err_signup_failed)
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("auth_signup_button")
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    TextButton(
                                        onClick = {
                                            isSignUpMode = false
                                            errorMessage = null
                                        },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Text(
                                            text = stringResource(R.string.auth_have_account_login),
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                            // 3. OR Divider + Google OAuth Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                HorizontalDivider(modifier = Modifier.weight(1f))
                                Text(
                                    text = stringResource(R.string.common_or),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = CocoaSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                                HorizontalDivider(modifier = Modifier.weight(1f))
                            }

                            OutlinedButton(
                                onClick = startGoogleSignIn,
                                enabled = !isLoading,
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("google_sign_in_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AccountCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.auth_continue_google),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = EspressoInk,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Text(
                                text = stringResource(R.string.auth_footer_note),
                                style = MaterialTheme.typography.labelMedium,
                                color = CocoaSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }

    if (showForgotPasswordDialog) {
        val roleLabel = stringResource(selectedRole.labelRes)
        ForgotPasswordDialog(
            roleLabel = roleLabel,
            initialEmail = loginEmail,
            accentColor = selectedRole.accent,
            onDismiss = { showForgotPasswordDialog = false },
            onSubmitReset = { resetEmail, newPw, onDone ->
                scope.launch {
                    val monitor = NetworkMonitor(context)
                    val currentlyOnline = monitor.isOnline.value
                    monitor.release()
                    val res = repository.resetPortalPassword(
                        expectedRole = selectedRole.roleKey,
                        rawEmail = resetEmail,
                        newPassword = newPw,
                        isOnline = currentlyOnline
                    )
                    res.onSuccess {
                        loginEmail = resetEmail
                        loginPassword = newPw
                    }
                    onDone(res)
                }
            }
        )
    }
}
