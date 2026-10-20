package com.gec.emergency.feature.account

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

private const val QR_BITMAP_SIZE = 512

/**
 * The QR encodes the full otpauth provisioning URI rather than only
 * the Base32 secret so standard authenticator applications can import
 * the account directly.
 */
@Composable
fun TotpQrCode(
    provisioningUri: String,
    modifier: Modifier = Modifier,
) {
    val bitmap = remember(provisioningUri) {
        if (provisioningUri.isBlank()) null else generateQrCodeBitmap(provisioningUri)
    }

    if (bitmap != null) {
        Box(
            modifier = modifier
                .background(Color.White, shape = RoundedCornerShape(12.dp))
                .padding(12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Authenticator setup QR code",
                modifier = Modifier.size(180.dp),
            )
        }
    }
}

private fun generateQrCodeBitmap(contents: String): Bitmap? {
    if (contents.isBlank()) return null

    return runCatching {
        val matrix = QRCodeWriter().encode(
            contents,
            BarcodeFormat.QR_CODE,
            QR_BITMAP_SIZE,
            QR_BITMAP_SIZE,
        )

        createBitmap(
            matrix.width,
            matrix.height,
            Bitmap.Config.RGB_565,
        ).apply {
            for (x in 0 until matrix.width) {
                for (y in 0 until matrix.height) {
                    this[x, y] = if (matrix[x, y]) {
                        android.graphics.Color.BLACK
                    } else {
                        android.graphics.Color.WHITE
                    }
                }
            }
        }
    }.getOrNull()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    uiState: AccountUiState,
    onBackClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit,
    onMyReportsClick: () -> Unit,
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier,
    onClaimReportSubmit: (String, String) -> Unit = { _, _ -> },
    onStartTotpSetup: () -> Unit = {},
    onConfirmTotpSetup: (String) -> Unit = {},
    onDisableTotp: () -> Unit = {},
    onDismissTotpDialog: () -> Unit = {},
) {
    var claimDialogOpen by remember { mutableStateOf(false) }
    var totpConfirmCode by remember { mutableStateOf("") }
    var showManualKey by remember { mutableStateOf(false) }
    var copyKeyFeedback by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Account & Profile") },
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
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .height(64.dp)
                                .width(64.dp),
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (uiState.isLoggedIn) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Verified Public User",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }

                            if (!uiState.phoneNumber.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = maskPhoneNumber(uiState.phoneNumber),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        } else {
                            Text(
                                text = "Guest User",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Sign in to save and access your report history across sessions.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (uiState.isLoggedIn) {
                    Button(
                        onClick = onMyReportsClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ListAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("My Reports & Tracked Incidents", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { claimDialogOpen = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Existing Guest Report")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Account Security & MFA",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (uiState.isTotpEnabled) "Authenticator App (TOTP): Enabled" else "Authenticator App (TOTP): Optional (Not Enabled)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            if (!uiState.isTotpEnabled) {
                                Button(
                                    onClick = onStartTotpSetup,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Icon(imageVector = Icons.Default.QrCode, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Set Up Authenticator App")
                                }
                            } else {
                                OutlinedButton(
                                    onClick = onDisableTotp,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text("Disable Authenticator App")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = onSignOutClick,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign Out")
                    }
                } else {
                    Button(
                        onClick = onRegisterClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                    ) {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verify Phone / Create Account", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onLoginClick,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Sign In With Phone Number")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Emergency Services, Calling, Silent Reports, and General Reporting are always available to guests without registration.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    if (claimDialogOpen) {
        ClaimReportDialog(
            onDismiss = { claimDialogOpen = false },
            onClaimSubmit = { ref, token ->
                claimDialogOpen = false
                onClaimReportSubmit(ref, token)
            },
        )
    }

    if (uiState.isTotpSetupDialogOpen) {
        AlertDialog(
            onDismissRequest = {
                showManualKey = false
                onDismissTotpDialog()
            },
            title = {
                Text(
                    text = "Set Up Authenticator App",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Scan this QR code with Google Authenticator, Microsoft Authenticator, Authy, or 1Password.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // QR Code Image
                    if (!uiState.totpProvisioningUri.isNullOrBlank()) {
                        TotpQrCode(provisioningUri = uiState.totpProvisioningUri)
                    } else {
                        Text(
                            text = "Unable to display QR code. Use manual setup key below.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(onClick = { showManualKey = !showManualKey }) {
                        Icon(imageVector = Icons.Default.Key, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (showManualKey) "Hide Setup Key" else "Reveal Manual Setup Key")
                    }

                    if (showManualKey && !uiState.totpSecret.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = uiState.totpSecret,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            TextButton(onClick = {
                                clipboardManager.setText(AnnotatedString(uiState.totpSecret))
                                copyKeyFeedback = true
                            }) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (copyKeyFeedback) "Copied!" else "Copy Key")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = totpConfirmCode,
                        onValueChange = { input ->
                            totpConfirmCode = input.filter(Char::isDigit).take(6)
                        },
                        label = { Text("6-Digit Authenticator Code") },
                        placeholder = { Text("123456") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirmTotpSetup(totpConfirmCode)
                        totpConfirmCode = ""
                        showManualKey = false
                    },
                    enabled = totpConfirmCode.length == 6 && totpConfirmCode.all(Char::isDigit),
                ) {
                    Text("Confirm & Enable")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showManualKey = false
                    onDismissTotpDialog()
                }) {
                    Text("Cancel")
                }
            },
        )
    }
}

private fun maskPhoneNumber(phone: String): String {
    if (phone.length < 8) return phone
    return phone.take(4) + " *** " + phone.takeLast(3)
}
