package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.LockReset
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillNode
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalAutofillTree
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.ClayButton
import com.example.ui.theme.ClayErrorContainer
import com.example.ui.theme.CocoaSecondary
import com.example.ui.theme.EspressoInk
import com.example.ui.theme.LeafGreenSoft
import com.example.ui.theme.SoftOatSurface
import com.example.ui.theme.WarmCreamBg

/**
 * Attaches Android system Autofill / Password Manager support to Compose TextFields
 * using Compose UI 1.7's AutofillNode & LocalAutofillTree APIs.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.twogetherAutofill(
    autofillTypes: List<AutofillType>,
    onFill: (String) -> Unit
): Modifier = composed {
    val autofill = LocalAutofill.current
    val autofillTree = LocalAutofillTree.current
    val autofillNode = remember(autofillTypes) {
        AutofillNode(
            autofillTypes = autofillTypes,
            onFill = onFill
        )
    }

    DisposableEffect(autofillTree, autofillNode) {
        autofillTree += autofillNode
        onDispose {
            autofill?.cancelAutofillForNode(autofillNode)
        }
    }

    this
        .onGloballyPositioned { coordinates ->
            autofillNode.boundingBox = coordinates.boundsInWindow()
        }
        .onFocusChanged { focusState ->
            autofill?.let { manager ->
                if (focusState.isFocused) {
                    manager.requestAutofillForNode(autofillNode)
                } else {
                    manager.cancelAutofillForNode(autofillNode)
                }
            }
        }
}

/**
 * Offline-first Forgot Password Claymorphic dialog.
 * Verifies the user's registered email + role, updates their password hash locally in Room DB
 * (100% offline-capable), and sends a best-effort Firebase password reset email when online.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ForgotPasswordDialog(
    roleLabel: String,
    initialEmail: String,
    accentColor: Color,
    onDismiss: () -> Unit,
    onSubmitReset: (email: String, newPassword: String, onDone: (Result<String>) -> Unit) -> Unit
) {
    var email by remember(initialEmail) { mutableStateOf(initialEmail) }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var successMsg by remember { mutableStateOf<String?>(null) }
    val mismatchMsg = stringResource(R.string.forgot_pw_mismatch)
    val shortPwMsg = stringResource(R.string.auth_err_password_len)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = WarmCreamBg,
        shape = RoundedCornerShape(28.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.LockReset,
                    contentDescription = null,
                    tint = accentColor
                )
                Text(
                    text = stringResource(R.string.forgot_pw_dialog_title, roleLabel),
                    style = MaterialTheme.typography.titleLarge,
                    color = EspressoInk,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.forgot_pw_dialog_sub),
                    style = MaterialTheme.typography.bodyMedium,
                    color = CocoaSecondary
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMsg = null
                    },
                    label = { Text(stringResource(R.string.auth_email_address)) },
                    leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SoftOatSurface.copy(alpha = 0.45f),
                        unfocusedContainerColor = SoftOatSurface.copy(alpha = 0.45f),
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .twogetherAutofill(listOf(AutofillType.EmailAddress)) { email = it }
                        .testTag("forgot_pw_email_input")
                )

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        errorMsg = null
                    },
                    label = { Text(stringResource(R.string.forgot_pw_new_label)) },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                contentDescription = stringResource(R.string.auth_toggle_password_cd)
                            )
                        }
                    },
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SoftOatSurface.copy(alpha = 0.45f),
                        unfocusedContainerColor = SoftOatSurface.copy(alpha = 0.45f),
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .twogetherAutofill(listOf(AutofillType.NewPassword, AutofillType.Password)) { newPassword = it }
                        .testTag("forgot_pw_new_input")
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        errorMsg = null
                    },
                    label = { Text(stringResource(R.string.forgot_pw_confirm_label)) },
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SoftOatSurface.copy(alpha = 0.45f),
                        unfocusedContainerColor = SoftOatSurface.copy(alpha = 0.45f),
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("forgot_pw_confirm_input")
                )

                if (errorMsg != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ClayErrorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMsg.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = EspressoInk,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                if (successMsg != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = LeafGreenSoft,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = successMsg.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = EspressoInk,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (successMsg != null) {
                ClayButton(
                    text = stringResource(R.string.common_done),
                    containerColor = accentColor,
                    onClick = onDismiss,
                    modifier = Modifier.testTag("forgot_pw_done_button")
                )
            } else {
                ClayButton(
                    text = stringResource(R.string.forgot_pw_submit_btn),
                    containerColor = accentColor,
                    enabled = !isSubmitting && email.isNotBlank() && newPassword.isNotBlank(),
                    onClick = {
                        when {
                            newPassword.length < 6 -> {
                                errorMsg = shortPwMsg
                            }
                            newPassword != confirmPassword -> {
                                errorMsg = mismatchMsg
                            }
                            else -> {
                                isSubmitting = true
                                errorMsg = null
                                onSubmitReset(email.trim(), newPassword) { result ->
                                    isSubmitting = false
                                    result.onSuccess { msg ->
                                        successMsg = msg
                                    }.onFailure { err ->
                                        errorMsg = err.localizedMessage ?: "Could not reset password."
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.testTag("forgot_pw_submit_button")
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}
