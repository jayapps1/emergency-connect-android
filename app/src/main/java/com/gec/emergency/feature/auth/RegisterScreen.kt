package com.gec.emergency.feature.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gec.emergency.feature.account.AccountUiState

/**
 * Partial phone-number input must always be preserved while typing.
 * Full Ghana-number validation is performed separately from onValueChange
 * so incomplete intermediate values are not discarded.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    uiState: AccountUiState,
    onPhoneNumberChanged: (String) -> Unit,
    onBackClick: () -> Unit,
    onRequestOtp: (String) -> Unit,
    onContinueAsGuest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var validationError by rememberSaveable { mutableStateOf<String?>(null) }
    val keyboardController = LocalSoftwareKeyboardController.current

    val phoneInput = uiState.phoneNumberInput
    val digitsOnly = phoneInput.filter { it.isDigit() }
    val canSubmit = digitsOnly.length >= 9 && !uiState.isRequestingOtp

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Create GEC Account") },
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
                modifier = Modifier.fillMaxWidth(),
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
                    text = "Verify Your Phone Number",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Verify your mobile phone to access your report history and track emergency response status.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { newValue ->
                        onPhoneNumberChanged(newValue)
                        validationError = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                keyboardController?.show()
                            }
                        },
                    label = { Text("Phone Number") },
                    placeholder = { Text("0541553337") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = null)
                    },
                    prefix = {
                        Text("+233 ", fontWeight = FontWeight.Bold)
                    },
                    singleLine = true,
                    enabled = true,
                    readOnly = false,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            if (canSubmit) {
                                onRequestOtp(normalizeGhanaPhoneNumber(phoneInput))
                            }
                        }
                    ),
                )

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
                        keyboardController?.hide()
                        if (!canSubmit) {
                            validationError = "Enter a valid Ghana phone number."
                        } else {
                            validationError = null
                            onRequestOtp(normalizeGhanaPhoneNumber(phoneInput))
                        }
                    },
                    enabled = canSubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                ) {
                    if (uiState.isRequestingOtp) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .height(24.dp)
                                .width(24.dp),
                        )
                    } else {
                        Text("Send Verification Code", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onContinueAsGuest,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Continue Without Account (Emergency Bypass)")
                }

                Spacer(modifier = Modifier.height(24.dp))

                TextButton(onClick = onContinueAsGuest) {
                    Text("Emergency reporting and calling do not require an account.")
                }
            }
        }
    }
}

/**
 * Normalizes user-entered Ghanaian phone number strings into the canonical E.164 format (+233XXXXXXXXX).
 * Accepts 05XXXXXXXX, 5XXXXXXXX, 2335XXXXXXXX, +2335XXXXXXXX, or +23305XXXXXXXX naturally.
 */
fun normalizeGhanaPhoneNumber(rawPhone: String): String {
    val trimmed = rawPhone.trim()
    val digitsOnly = trimmed.replace(Regex("[^0-9]"), "")

    if (digitsOnly.length <= 5) return digitsOnly

    if (digitsOnly.startsWith("2330") && digitsOnly.length == 13) {
        return "+233" + digitsOnly.substring(4)
    }
    if (digitsOnly.startsWith("233") && digitsOnly.length == 12) {
        return "+$digitsOnly"
    }
    if (digitsOnly.startsWith("0") && digitsOnly.length == 10) {
        return "+233" + digitsOnly.substring(1)
    }
    if (digitsOnly.length == 9 && (digitsOnly.startsWith("2") || digitsOnly.startsWith("3") || digitsOnly.startsWith("5"))) {
        return "+233$digitsOnly"
    }
    if (trimmed.startsWith("+")) {
        return "+$digitsOnly"
    }
    return "+233" + digitsOnly.removePrefix("0")
}
