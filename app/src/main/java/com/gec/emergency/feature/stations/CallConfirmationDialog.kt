package com.gec.emergency.feature.stations

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gec.emergency.R
import com.gec.emergency.core.calling.CallRequest
import com.gec.emergency.core.calling.CallType
import com.gec.emergency.ui.theme.GhanaEmergencyConnectTheme

@Composable
fun CallConfirmationDialog(
    state: CallConfirmationState,
    onConfirmCall: (CallRequest) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!state.isVisible) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.call_confirmation_title, state.stationName),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column {
                Text(
                    text = state.contactTypeLabel,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = state.phoneNumber,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    state.callRequest?.let(onConfirmCall)
                },
            ) {
                Text(stringResource(R.string.confirm_call))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_call))
            }
        },
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
fun CallConfirmationDialogPreview() {
    GhanaEmergencyConnectTheme {
        CallConfirmationDialog(
            state = CallConfirmationState(
                isVisible = true,
                stationName = "Komenda Fire Station",
                phoneNumber = "+233 302 000 001",
                contactTypeLabel = "Watchroom",
                callRequest = CallRequest(
                    phoneNumber = "+233 302 000 001",
                    callType = CallType.STATION_WATCHROOM,
                    stationId = "st_01",
                ),
            ),
            onConfirmCall = {},
            onDismiss = {},
        )
    }
}
