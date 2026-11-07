package com.gec.emergency.feature.account

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
    onCheckAuthStatus: () -> Unit = {},
    onClaimReportSubmit: (String, String) -> Unit = { _, _ -> },
    onStartTotpSetup: () -> Unit = {},
    onConfirmTotpSetup: (String) -> Unit = {},
    onStartTotpReconfiguration: (String, (String, String) -> Unit, (String) -> Unit) -> Unit = { _, _, _ -> },
    onConfirmTotpReconfiguration: (String, String, () -> Unit, (String) -> Unit) -> Unit = { _, _, _, _ -> },
    onDisableTotp: () -> Unit = {},
    onDismissTotpDialog: () -> Unit = {},
    onUpdateProfile: (String, String, () -> Unit) -> Unit = { _, _, _ -> },
    onChangePassword: (String, String, () -> Unit, (String) -> Unit) -> Unit = { _, _, _, _ -> },
    onGenerateRecoveryCodes: (String, (List<String>) -> Unit, (String) -> Unit) -> Unit = { _, _, _ -> },
    onSubmitPublicMfaRecoveryRequest: (String, () -> Unit, (String) -> Unit) -> Unit = { _, _, _ -> },
    onCollectPublicRecoveryCodes: (String, (List<String>) -> Unit, (String) -> Unit) -> Unit = { _, _, _ -> },
    onSubmitPhoneChangeRequest: (String, String, () -> Unit, (String) -> Unit) -> Unit = { _, _, _, _ -> },
    onLoadMyRequests: () -> Unit = {},
) {
    var claimDialogOpen by remember { mutableStateOf(false) }
    var totpConfirmCode by remember { mutableStateOf("") }
    var showManualKey by remember { mutableStateOf(false) }
    var copyKeyFeedback by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    // Dialog States
    var editProfileOpen by remember { mutableStateOf(false) }
    var editFirstName by remember { mutableStateOf("") }
    var editLastName by remember { mutableStateOf("") }

    var changePasswordOpen by remember { mutableStateOf(false) }
    var currentPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmNewPass by remember { mutableStateOf("") }
    var changePasswordError by remember { mutableStateOf<String?>(null) }

    var reconfigurePasswordOpen by remember { mutableStateOf(false) }
    var reconfigurePasswordInput by remember { mutableStateOf("") }
    var reconfigureError by remember { mutableStateOf<String?>(null) }
    var reconfigureSecret by remember { mutableStateOf<String?>(null) }
    var reconfigureUri by remember { mutableStateOf<String?>(null) }
    var reconfigureDialogOpen by remember { mutableStateOf(false) }
    var reconfigureCodeInput by remember { mutableStateOf("") }

    var recoveryManagementOpen by remember { mutableStateOf(false) }
    var confirmGenerateOpen by remember { mutableStateOf(false) }
    var verifyPasswordOpen by remember { mutableStateOf(false) }
    var verifyPasswordInput by remember { mutableStateOf("") }
    var verifyPasswordError by remember { mutableStateOf<String?>(null) }

    var recoveryCodesOpen by remember { mutableStateOf(false) }
    var displayedCodes by remember { mutableStateOf<List<String>?>(null) }
    var isGeneratingCodes by remember { mutableStateOf(false) }

    var assistedRecoveryOpen by remember { mutableStateOf(false) }
    var assistedReason by remember { mutableStateOf("") }
    var assistedError by remember { mutableStateOf<String?>(null) }

    var phoneChangeOpen by remember { mutableStateOf(false) }
    var newPhoneInput by remember { mutableStateOf("") }
    var phoneChangeReason by remember { mutableStateOf("") }
    var phoneChangeError by remember { mutableStateOf<String?>(null) }

    var requestsHistoryOpen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        onCheckAuthStatus()
    }

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
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Section 1: Personal Information Card
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

                        if (uiState.isLoggedIn && uiState.phoneNumber != null) {
                            Text(
                                text = if (!uiState.firstName.isNullOrBlank() || !uiState.lastName.isNullOrBlank())
                                    "${uiState.firstName ?: ""} ${uiState.lastName ?: ""}".trim()
                                else "Citizen Account",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = maskPhoneNumber(uiState.phoneNumber),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        editFirstName = uiState.firstName ?: ""
                                        editLastName = uiState.lastName ?: ""
                                        editProfileOpen = true
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                                    Text("Edit Profile")
                                }

                                OutlinedButton(
                                    onClick = {
                                        newPhoneInput = ""
                                        phoneChangeReason = ""
                                        phoneChangeError = null
                                        phoneChangeOpen = true
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                                    Text("Change Phone")
                                }
                            }
                        } else {
                            Text(
                                text = "Guest User",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Register or sign in to track reports and manage 2-step verification.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (uiState.isLoggedIn) {
                    // Section 2: Account Security Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
                                    text = "Account Security",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(12.dp))

                            // Change Password Action
                            OutlinedButton(
                                onClick = {
                                    currentPass = ""
                                    newPass = ""
                                    confirmNewPass = ""
                                    changePasswordError = null
                                    changePasswordOpen = true
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                                Text("Change Password")
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // 2-Step Verification Section
                            if (uiState.isTotpEnabled) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp),
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Authenticator App Enabled",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        recoveryManagementOpen = true
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Icon(imageVector = Icons.Default.Key, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                                    Text("Manage 2FA Recovery Codes")
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedButton(
                                    onClick = {
                                        reconfigurePasswordInput = ""
                                        reconfigureError = null
                                        reconfigurePasswordOpen = true
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                                    Text("Reconfigure Authenticator")
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedButton(
                                    onClick = onDisableTotp,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text("Disable 2-Step Verification")
                                }
                            } else {
                                Text(
                                    text = "Enhance your account security using an authenticator app (Google Authenticator, Aegis, Authy) for 6-digit login codes.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = onStartTotpSetup,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCode,
                                        contentDescription = null,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text("Set Up Authenticator App")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Section 3: My Activity Card
                    Button(
                        onClick = onMyReportsClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ListAlt,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "My Reports (${uiState.myReports.size})",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { claimDialogOpen = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = MaterialTheme.shapes.medium,
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Claim Report")
                        }

                        OutlinedButton(
                            onClick = {
                                onLoadMyRequests()
                                requestsHistoryOpen = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = MaterialTheme.shapes.medium,
                        ) {
                            Icon(imageVector = Icons.Default.History, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Request History")
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Section 4: Account Actions
                    OutlinedButton(
                        onClick = onSignOutClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sign Out",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onRegisterClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = MaterialTheme.shapes.medium,
                        ) {
                            Text("Create Account", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onLoginClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = MaterialTheme.shapes.medium,
                        ) {
                            Text("Sign In", fontWeight = FontWeight.Bold)
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

                Spacer(modifier = Modifier.height(128.dp))
            }
        }
    }

    // Reconfigure Authenticator Password Dialog
    if (reconfigurePasswordOpen) {
        AlertDialog(
            onDismissRequest = { reconfigurePasswordOpen = false },
            title = { Text("Verify Identity to Reconfigure", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter your current password to start reconfiguring your authenticator app.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = reconfigurePasswordInput,
                        onValueChange = { reconfigurePasswordInput = it },
                        label = { Text("Current Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (reconfigureError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = reconfigureError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = reconfigurePasswordInput.isNotBlank(),
                    onClick = {
                        reconfigureError = null
                        onStartTotpReconfiguration(reconfigurePasswordInput, { secret, uri ->
                            reconfigureSecret = secret
                            reconfigureUri = uri
                            reconfigurePasswordOpen = false
                            reconfigureCodeInput = ""
                            reconfigureDialogOpen = true
                        }, { err ->
                            reconfigureError = err
                        })
                    }
                ) {
                    Text("Verify & Setup New Authenticator")
                }
            },
            dismissButton = {
                TextButton(onClick = { reconfigurePasswordOpen = false }) { Text("Cancel") }
            }
        )
    }

    // Reconfigure Authenticator QR & Confirmation Dialog
    if (reconfigureDialogOpen && reconfigureSecret != null) {
        AlertDialog(
            onDismissRequest = { reconfigureDialogOpen = false },
            title = { Text("Scan New Authenticator QR Code", fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Scan this new QR code in your authenticator app (Google Authenticator, Authy, Aegis).", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(12.dp))

                    if (!reconfigureUri.isNullOrBlank()) {
                        TotpQrCode(provisioningUri = reconfigureUri!!)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = reconfigureCodeInput,
                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) reconfigureCodeInput = it },
                        label = { Text("6-Digit Code from New App") },
                        placeholder = { Text("123456") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    if (reconfigureError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = reconfigureError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = reconfigureCodeInput.length == 6,
                    onClick = {
                        reconfigureError = null
                        onConfirmTotpReconfiguration(reconfigureSecret!!, reconfigureCodeInput, {
                            reconfigureDialogOpen = false
                        }, { err ->
                            reconfigureError = err
                        })
                    }
                ) {
                    Text("Verify & Activate New Authenticator")
                }
            },
            dismissButton = {
                TextButton(onClick = { reconfigureDialogOpen = false }) { Text("Cancel") }
            }
        )
    }

    // Read-Only 2FA Recovery Management Landing Dialog
    if (recoveryManagementOpen) {
        AlertDialog(
            onDismissRequest = { recoveryManagementOpen = false },
            title = { Text("2FA Recovery Management", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Authenticator App Enabled", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    }

                    Text("Manage your 2FA backup recovery codes or submit an assisted recovery request if you lose access to your authenticator.", style = MaterialTheme.typography.bodySmall)

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            confirmGenerateOpen = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                        Text("Regenerate Backup Codes")
                    }

                    OutlinedButton(
                        onClick = {
                            assistedReason = ""
                            assistedError = null
                            assistedRecoveryOpen = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Request Assisted 2FA Recovery")
                    }

                    OutlinedButton(
                        onClick = {
                            onLoadMyRequests()
                            requestsHistoryOpen = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.History, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                        Text("My Recovery Requests")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { recoveryManagementOpen = false }) { Text("Close") }
            }
        )
    }

    // Confirmation Dialog before Generating/Replacing Codes
    if (confirmGenerateOpen) {
        AlertDialog(
            onDismissRequest = { confirmGenerateOpen = false },
            title = { Text("Regenerate Backup Codes?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Generating a new set of 10 backup recovery codes will immediately invalidate any previously generated codes. Password verification is required.", style = MaterialTheme.typography.bodyMedium)
            },
            confirmButton = {
                Button(
                    onClick = {
                        confirmGenerateOpen = false
                        verifyPasswordInput = ""
                        verifyPasswordError = null
                        verifyPasswordOpen = true
                    }
                ) {
                    Text("Proceed to Password Verification")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmGenerateOpen = false }) { Text("Cancel") }
            }
        )
    }

    // Password Verification Prompt Dialog before Code Generation
    if (verifyPasswordOpen) {
        AlertDialog(
            onDismissRequest = { verifyPasswordOpen = false },
            title = { Text("Verify Your Identity", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter your current account password to verify your identity before generating new backup recovery codes.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = verifyPasswordInput,
                        onValueChange = { verifyPasswordInput = it },
                        label = { Text("Current Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (verifyPasswordError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = verifyPasswordError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = verifyPasswordInput.isNotBlank() && !isGeneratingCodes,
                    onClick = {
                        isGeneratingCodes = true
                        verifyPasswordError = null
                        onGenerateRecoveryCodes(verifyPasswordInput, { codes ->
                            displayedCodes = codes
                            isGeneratingCodes = false
                            verifyPasswordOpen = false
                            recoveryManagementOpen = false
                            recoveryCodesOpen = true
                        }, { err ->
                            verifyPasswordError = err
                            isGeneratingCodes = false
                        })
                    }
                ) {
                    if (isGeneratingCodes) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Verify & Generate Codes")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { verifyPasswordOpen = false }) { Text("Cancel") }
            }
        )
    }

    // 2FA Recovery Codes One-Time Display Dialog
    if (recoveryCodesOpen && displayedCodes != null) {
        AlertDialog(
            onDismissRequest = {
                recoveryCodesOpen = false
                isGeneratingCodes = false
            },
            title = { Text("New Recovery Codes Generated", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("These single-use recovery codes allow logging in if you lose access to your authenticator app. Store them offline securely. Previously generated backup codes no longer work.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            displayedCodes!!.chunked(2).forEach { pair ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    pair.forEach { code ->
                                        Text(text = code, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(displayedCodes!!.joinToString("\n")))
                        recoveryCodesOpen = false
                    }
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                    Text("Copy Codes & Close")
                }
            }
        )
    }

    // Edit Profile Dialog
    if (editProfileOpen) {
        AlertDialog(
            onDismissRequest = { editProfileOpen = false },
            title = { Text("Edit Citizen Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editFirstName,
                        onValueChange = { editFirstName = it },
                        label = { Text("First Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editLastName,
                        onValueChange = { editLastName = it },
                        label = { Text("Last Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateProfile(editFirstName, editLastName) {
                            editProfileOpen = false
                        }
                    }
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { editProfileOpen = false }) { Text("Cancel") }
            }
        )
    }

    // Change Password Dialog
    if (changePasswordOpen) {
        AlertDialog(
            onDismissRequest = { changePasswordOpen = false },
            title = { Text("Change Password", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = currentPass,
                        onValueChange = { currentPass = it },
                        label = { Text("Current Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        label = { Text("New Password (min 6 chars)") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = confirmNewPass,
                        onValueChange = { confirmNewPass = it },
                        label = { Text("Confirm New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (changePasswordError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = changePasswordError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = newPass.length >= 6 && newPass == confirmNewPass,
                    onClick = {
                        onChangePassword(currentPass, newPass, {
                            changePasswordOpen = false
                        }, { err ->
                            changePasswordError = err
                        })
                    }
                ) {
                    Text("Save Password")
                }
            },
            dismissButton = {
                TextButton(onClick = { changePasswordOpen = false }) { Text("Cancel") }
            }
        )
    }

    // Assisted 2FA Recovery Request Dialog
    if (assistedRecoveryOpen) {
        AlertDialog(
            onDismissRequest = { assistedRecoveryOpen = false },
            title = { Text("Request Assisted 2FA Recovery", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("If you lost your authenticator device and have no backup recovery codes, submit a recovery request for System Administration review.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = assistedReason,
                        onValueChange = { assistedReason = it },
                        label = { Text("Reason / Identity Details") },
                        placeholder = { Text("Lost phone containing authenticator app") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (assistedError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = assistedError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = assistedReason.isNotBlank(),
                    onClick = {
                        onSubmitPublicMfaRecoveryRequest(assistedReason, {
                            assistedRecoveryOpen = false
                        }, { err ->
                            assistedError = err
                        })
                    }
                ) {
                    Text("Submit Recovery Request")
                }
            },
            dismissButton = {
                TextButton(onClick = { assistedRecoveryOpen = false }) { Text("Cancel") }
            }
        )
    }

    // Change Phone Number Request Dialog
    if (phoneChangeOpen) {
        AlertDialog(
            onDismissRequest = { phoneChangeOpen = false },
            title = { Text("Request Phone Number Change", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Phone number changes require System Administration approval for security. Enter your new phone number below.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newPhoneInput,
                        onValueChange = { newPhoneInput = it },
                        label = { Text("New Phone Number (e.g. 0241234567)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = phoneChangeReason,
                        onValueChange = { phoneChangeReason = it },
                        label = { Text("Reason for Change") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (phoneChangeError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = phoneChangeError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = newPhoneInput.isNotBlank(),
                    onClick = {
                        onSubmitPhoneChangeRequest(newPhoneInput, phoneChangeReason, {
                            phoneChangeOpen = false
                        }, { err ->
                            phoneChangeError = err
                        })
                    }
                ) {
                    Text("Submit Request")
                }
            },
            dismissButton = {
                TextButton(onClick = { phoneChangeOpen = false }) { Text("Cancel") }
            }
        )
    }

    // Request History Dialog
    if (requestsHistoryOpen) {
        AlertDialog(
            onDismissRequest = { requestsHistoryOpen = false },
            title = { Text("My Request History", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (uiState.myRequests.isEmpty()) {
                        Text("No account requests submitted yet.", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        uiState.myRequests.forEach { req ->
                            val reqId = req["id"]?.toString() ?: ""
                            val rType = req["requestType"]?.toString() ?: "REQUEST"
                            val rStatus = req["status"]?.toString() ?: "PENDING"
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(text = rType.replace("_", " "), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        AssistChip(
                                            onClick = {},
                                            label = { Text(rStatus, fontWeight = FontWeight.Bold) }
                                        )
                                    }
                                    Text(text = req["reason"]?.toString() ?: "", style = MaterialTheme.typography.bodySmall)

                                    if ("APPROVED_AWAITING_COLLECTION".equals(rStatus, ignoreCase = true) && "PUBLIC_MFA_RECOVERY".equals(rType, ignoreCase = true)) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = {
                                                onCollectPublicRecoveryCodes(reqId, { codes ->
                                                    displayedCodes = codes
                                                    recoveryCodesOpen = true
                                                    requestsHistoryOpen = false
                                                }, {})
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Collect Approved Recovery Codes")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { requestsHistoryOpen = false }) { Text("Close") }
            }
        )
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

                    Spacer(modifier = Modifier.height(12.dp))

                    if (!uiState.totpSecret.isNullOrBlank()) {
                        TextButton(
                            onClick = { showManualKey = !showManualKey },
                        ) {
                            Text(if (showManualKey) "Hide Manual Key" else "Can't scan? Show setup key")
                        }

                        if (showManualKey) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                ),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Key,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = uiState.totpSecret,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f),
                                    )
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(uiState.totpSecret))
                                            copyKeyFeedback = true
                                        },
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy Secret",
                                        )
                                    }
                                }
                            }
                            if (copyKeyFeedback) {
                                Text(
                                    text = "Secret copied to clipboard!",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = totpConfirmCode,
                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) totpConfirmCode = it },
                        label = { Text("6-Digit Authenticator Code") },
                        placeholder = { Text("123456") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    if (uiState.errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirmTotpSetup(totpConfirmCode)
                        totpConfirmCode = ""
                        showManualKey = false
                    },
                    enabled = totpConfirmCode.length == 6,
                ) {
                    Text("Verify & Enable")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showManualKey = false
                        totpConfirmCode = ""
                        onDismissTotpDialog()
                    },
                ) {
                    Text("Cancel")
                }
            },
        )
    }
}

private fun maskPhoneNumber(phone: String): String {
    if (phone.length < 7) return phone
    val prefix = phone.take(4)
    val suffix = phone.takeLast(3)
    return "$prefix *** $suffix"
}
