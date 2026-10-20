package com.gec.emergency.feature.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LockClock
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gec.emergency.feature.account.AccountUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpVerifyScreen(
    uiState: AccountUiState,
    phoneNumber: String,
    onBackClick: () -> Unit,
    onVerifyOtp: (String) -> Unit,
    onResendOtp: () -> Unit,
    onRequestSmsFallback: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var otpCode by remember { mutableStateOf("") }
    val isTotpRequired = uiState.nextStep == "TOTP_REQUIRED"

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (isTotpRequired) "Authenticator Verification" else "Verify Phone") },
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
                    imageVector = Icons.Default.LockClock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .height(64.dp)
                        .width(64.dp),
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isTotpRequired) "Enter Authenticator Code" else "Enter 6-Digit Code",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isTotpRequired) "Open your authenticator app (Google Authenticator, Microsoft Authenticator, Authy) and enter the 6-digit code for your account." else "We sent an SMS verification code to $phoneNumber.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = otpCode,
                    onValueChange = { if (it.length <= 6) otpCode = it.filter { char -> char.isDigit() } },
                    label = { Text(if (isTotpRequired) "6-Digit Authenticator Code" else "6-Digit Verification Code") },
                    placeholder = { Text("123456") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                if (uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = uiState.errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { onVerifyOtp(otpCode) },
                    enabled = !uiState.isVerifyingOtp && otpCode.length == 6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                ) {
                    if (uiState.isVerifyingOtp) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .height(24.dp)
                                .width(24.dp),
                        )
                    } else {
                        Text(if (isTotpRequired) "Verify & Sign In" else "Verify & Continue", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isTotpRequired) {
                    TextButton(onClick = onRequestSmsFallback) {
                        Text("Use SMS Verification Instead", fontWeight = FontWeight.Bold)
                    }
                } else {
                    if (uiState.resendTimerSeconds > 0) {
                        Text(
                            text = "Resend code in 00:${if (uiState.resendTimerSeconds < 10) "0" else ""}${uiState.resendTimerSeconds}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        TextButton(onClick = onResendOtp) {
                            Text("Resend Verification Code", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
