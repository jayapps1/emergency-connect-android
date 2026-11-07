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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gec.emergency.feature.account.AccountUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    uiState: AccountUiState,
    onPhoneNumberChanged: (String) -> Unit,
    onBackClick: () -> Unit,
    onRequestResetOtp: (phone: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
    onVerifyResetOtp: (phone: String, code: String, onSuccess: (recoveryToken: String) -> Unit, onError: (String) -> Unit) -> Unit,
    onResetPassword: (phone: String, recoveryToken: String, newPass: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var step by rememberSaveable { mutableStateOf(1) } // 1: Request, 2: 6-Box OTP, 3: New Password, 4: Success
    var otpDigits by rememberSaveable { mutableStateOf(List(6) { "" }) }
    var recoveryToken by rememberSaveable { mutableStateOf<String?>(null) }
    var newPasswordInput by rememberSaveable { mutableStateOf("") }
    var confirmPasswordInput by rememberSaveable { mutableStateOf("") }
    var validationError by rememberSaveable { mutableStateOf<String?>(null) }
    var isSubmitting by rememberSaveable { mutableStateOf(false) }

    val focusRequesters = remember { List(6) { FocusRequester() } }

    val phoneInput = uiState.phoneNumberInput
    val digitsOnly = phoneInput.filter { it.isDigit() }
    val isPhoneValid = digitsOnly.length >= 9

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Reset Your Password") },
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
                    imageVector = if (step == 4) Icons.Default.CheckCircle else Icons.Default.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .height(64.dp)
                        .width(64.dp),
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = when (step) {
                        1 -> "Forgot Password"
                        2 -> "Verify Your Identity"
                        3 -> "Create New Password"
                        else -> "Password Reset Complete"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = when (step) {
                        1 -> "Enter the phone number registered with your GEC citizen account to receive a password reset verification code."
                        2 -> "A 6-digit verification code has been sent to your registered mobile phone number. Enter digits below:"
                        3 -> "Your identity has been verified. Create a new secure password for your account."
                        else -> "Your password has been successfully reset. You can now sign in with your new password."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(24.dp))

                // STAGE 1: Phone Entry
                if (step == 1) {
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
                }

                // STAGE 2: 6 Independent Digits OTP Boxes
                if (step == 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        otpDigits.forEachIndexed { index, digit ->
                            OutlinedTextField(
                                value = digit,
                                onValueChange = { newValue ->
                                    val filtered = newValue.filter { it.isDigit() }
                                    if (filtered.length > 1) {
                                        // Pasting 6 digits
                                        val pasted = filtered.take(6)
                                        val newList = otpDigits.toMutableList()
                                        pasted.forEachIndexed { idx, char ->
                                            if (idx < 6) newList[idx] = char.toString()
                                        }
                                        otpDigits = newList
                                        if (pasted.length == 6) {
                                            isSubmitting = true
                                            onVerifyResetOtp(normalizeGhanaPhoneNumber(phoneInput), pasted, { token ->
                                                isSubmitting = false
                                                recoveryToken = token
                                                step = 3
                                            }, { err ->
                                                isSubmitting = false
                                                validationError = err
                                                otpDigits = List(6) { "" }
                                            })
                                        }
                                    } else {
                                        val newList = otpDigits.toMutableList()
                                        newList[index] = filtered
                                        otpDigits = newList
                                        validationError = null

                                        if (filtered.isNotEmpty() && index < 5) {
                                            focusRequesters[index + 1].requestFocus()
                                        }

                                        // Auto-Submit on 6th digit
                                        if (newList.all { it.isNotEmpty() }) {
                                            val codeStr = newList.joinToString("")
                                            isSubmitting = true
                                            onVerifyResetOtp(normalizeGhanaPhoneNumber(phoneInput), codeStr, { token ->
                                                isSubmitting = false
                                                recoveryToken = token
                                                step = 3
                                            }, { err ->
                                                isSubmitting = false
                                                validationError = err
                                                otpDigits = List(6) { "" }
                                            })
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .width(48.dp)
                                    .focusRequester(focusRequesters[index])
                                    .onKeyEvent { keyEvent ->
                                        if (keyEvent.key == Key.Backspace && digit.isEmpty() && index > 0) {
                                            focusRequesters[index - 1].requestFocus()
                                            true
                                        } else {
                                            false
                                        }
                                    },
                                textStyle = MaterialTheme.typography.titleLarge.copy(
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = if (index == 5) ImeAction.Done else ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (otpDigits.all { it.isNotEmpty() }) {
                                            val codeStr = otpDigits.joinToString("")
                                            isSubmitting = true
                                            onVerifyResetOtp(normalizeGhanaPhoneNumber(phoneInput), codeStr, { token ->
                                                isSubmitting = false
                                                recoveryToken = token
                                                step = 3
                                            }, { err ->
                                                isSubmitting = false
                                                validationError = err
                                            })
                                        }
                                    }
                                )
                            )
                        }
                    }
                }

                // STAGE 3: Create New Password
                if (step == 3) {
                    OutlinedTextField(
                        value = newPasswordInput,
                        onValueChange = {
                            newPasswordInput = it
                            validationError = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("New Password (min 6 chars)") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = confirmPasswordInput,
                        onValueChange = {
                            confirmPasswordInput = it
                            validationError = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Confirm New Password") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    )
                }

                if (validationError != null || uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = validationError ?: uiState.errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ACTIONS
                when (step) {
                    1 -> {
                        Button(
                            onClick = {
                                if (!isPhoneValid) {
                                    validationError = "Enter a valid phone number."
                                } else {
                                    validationError = null
                                    isSubmitting = true
                                    onRequestResetOtp(normalizeGhanaPhoneNumber(phoneInput), {
                                        isSubmitting = false
                                        step = 2
                                    }, { err ->
                                        isSubmitting = false
                                        validationError = err
                                    })
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                        ) {
                            if (isSubmitting || uiState.isRequestingOtp) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.height(24.dp).width(24.dp),
                                )
                            } else {
                                Text("Send Reset Code", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    2 -> {
                        if (isSubmitting || uiState.isVerifyingOtp) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Verifying OTP...", style = MaterialTheme.typography.bodyMedium)
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                TextButton(onClick = { step = 1 }) {
                                    Text("Change Number")
                                }
                                TextButton(onClick = {
                                    isSubmitting = true
                                    onRequestResetOtp(normalizeGhanaPhoneNumber(phoneInput), {
                                        isSubmitting = false
                                    }, { err ->
                                        isSubmitting = false
                                        validationError = err
                                    })
                                }) {
                                    Text("Resend Code")
                                }
                            }
                        }
                    }
                    3 -> {
                        Button(
                            onClick = {
                                if (newPasswordInput.length < 6) {
                                    validationError = "Password must be at least 6 characters long."
                                } else if (newPasswordInput != confirmPasswordInput) {
                                    validationError = "Passwords do not match."
                                } else if (recoveryToken == null) {
                                    validationError = "Verification expired. Request a new code."
                                } else {
                                    validationError = null
                                    isSubmitting = true
                                    onResetPassword(normalizeGhanaPhoneNumber(phoneInput), recoveryToken!!, newPasswordInput, {
                                        isSubmitting = false
                                        step = 4
                                    }, { err ->
                                        isSubmitting = false
                                        validationError = err
                                    })
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                        ) {
                            if (isSubmitting || uiState.isVerifyingOtp) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.height(24.dp).width(24.dp),
                                )
                            } else {
                                Text("Reset Password", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    else -> {
                        Button(
                            onClick = onNavigateToLogin,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                        ) {
                            Text("Sign In Now", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onNavigateToLogin) {
                    Text("Back to Sign In")
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
