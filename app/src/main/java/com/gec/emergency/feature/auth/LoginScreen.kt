package com.gec.emergency.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.gec.emergency.feature.account.AccountUiState

enum class LoginMethod {
    PASSWORD,
    AUTHENTICATOR
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    uiState: AccountUiState,
    onPhoneNumberChanged: (String) -> Unit,
    onBackClick: () -> Unit,
    onPasswordLogin: (phone: String, password: String) -> Unit,
    onTotpLogin: (phone: String, totpCode: String) -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit = {},
    onContinueAsGuest: () -> Unit,
    modifier: Modifier = Modifier,
    onSubmitPublicMfaRecoveryRequest: (reason: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit = { _, _, _ -> },
) {
    var loginMethod by rememberSaveable { mutableStateOf(LoginMethod.PASSWORD) }
    var passwordInput by rememberSaveable { mutableStateOf("") }
    var totpInput by rememberSaveable { mutableStateOf("") }
    var validationError by rememberSaveable { mutableStateOf<String?>(null) }

    var assistedRecoveryOpen by rememberSaveable { mutableStateOf(false) }
    var assistedReasonInput by rememberSaveable { mutableStateOf("") }
    var assistedError by rememberSaveable { mutableStateOf<String?>(null) }
    var assistedSuccessMessage by rememberSaveable { mutableStateOf<String?>(null) }

    val phoneInput = uiState.phoneNumberInput
    val digitsOnly = phoneInput.filter { it.isDigit() }
    val isPhoneValid = digitsOnly.length >= 9

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Sign In to GEC") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .height(64.dp)
                        .width(64.dp),
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Welcome Back",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Sign in to access your incident reports, claim offline reports, or manage account security.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Login Method Selector Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = loginMethod == LoginMethod.PASSWORD,
                        onClick = {
                            loginMethod = LoginMethod.PASSWORD
                            validationError = null
                        },
                        label = { Text("Password Login") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) },
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = loginMethod == LoginMethod.AUTHENTICATOR,
                        onClick = {
                            loginMethod = LoginMethod.AUTHENTICATOR
                            validationError = null
                        },
                        label = { Text("Authenticator") },
                        leadingIcon = { Icon(imageVector = Icons.Default.QrCode, contentDescription = null) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { newValue ->
                        onPhoneNumberChanged(newValue)
                        validationError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Phone Number") },
                    placeholder = { Text("0541553337") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Phone, contentDescription = null) },
                    prefix = { Text("+233 ", fontWeight = FontWeight.Bold) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (loginMethod == LoginMethod.PASSWORD) {
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = {
                            passwordInput = it
                            validationError = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Password") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onNavigateToForgotPassword) {
                            Text("Forgot Password?", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = totpInput,
                        onValueChange = {
                            if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                                totpInput = it
                                validationError = null
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("6-Digit Authenticator Code") },
                        placeholder = { Text("123456") },
                        leadingIcon = { Icon(imageVector = Icons.Default.QrCode, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    TextButton(
                        onClick = {
                            assistedReasonInput = "Lost phone containing authenticator app"
                            assistedError = null
                            assistedSuccessMessage = null
                            assistedRecoveryOpen = true
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Can't Access Your Authenticator?", style = MaterialTheme.typography.bodySmall)
                    }
                }

                if (validationError != null || uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = validationError ?: uiState.errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (!isPhoneValid) {
                            validationError = "Enter a valid phone number."
                        } else if (loginMethod == LoginMethod.PASSWORD && passwordInput.isBlank()) {
                            validationError = "Enter your password."
                        } else if (loginMethod == LoginMethod.AUTHENTICATOR && totpInput.length != 6) {
                            validationError = "Enter a 6-digit authenticator code."
                        } else {
                            validationError = null
                            val normalized = normalizeGhanaPhoneNumber(phoneInput)
                            if (loginMethod == LoginMethod.PASSWORD) {
                                onPasswordLogin(normalized, passwordInput)
                            } else {
                                onTotpLogin(normalized, totpInput)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                ) {
                    if (uiState.isRequestingOtp || uiState.isVerifyingOtp) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .height(24.dp)
                                .width(24.dp),
                        )
                    } else {
                        Text(
                            text = if (loginMethod == LoginMethod.PASSWORD) "Sign In" else "Sign In with Authenticator",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onNavigateToRegister) {
                    Text("Don't have an account? Create Account")
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onContinueAsGuest,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Continue Without Account (Emergency Bypass)")
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Pre-Login Assisted 2FA Recovery Dialog
    if (assistedRecoveryOpen) {
        AlertDialog(
            onDismissRequest = { assistedRecoveryOpen = false },
            title = { Text("Request Account Recovery", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("If you lost your authenticator device and have no backup recovery codes, submit a recovery request for System Administration review.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = assistedReasonInput,
                        onValueChange = { assistedReasonInput = it },
                        label = { Text("Reason / Identity Explanation") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (assistedError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = assistedError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    if (assistedSuccessMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = assistedSuccessMessage!!, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = assistedReasonInput.isNotBlank() && assistedSuccessMessage == null,
                    onClick = {
                        onSubmitPublicMfaRecoveryRequest(assistedReasonInput, {
                            assistedSuccessMessage = "Recovery request submitted for System Administration approval. Track status in Profile -> Request History."
                        }, { err ->
                            assistedError = err
                        })
                    }
                ) {
                    Text("Submit Recovery Request")
                }
            },
            dismissButton = {
                TextButton(onClick = { assistedRecoveryOpen = false }) { Text("Close") }
            }
        )
    }
}
