package com.gec.emergency.feature.account

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ClaimReportDialog(
    onDismiss: () -> Unit,
    onClaimSubmit: (reference: String, trackingToken: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var reference by remember { mutableStateOf("") }
    var trackingToken by remember { mutableStateOf("") }

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Existing Guest Report",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Enter the report reference and tracking code given to you when submitting your guest report to link it to your account.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = reference,
                    onValueChange = { reference = it.uppercase() },
                    label = { Text("Report Reference") },
                    placeholder = { Text("e.g. RPT-20261017-ABC123") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = trackingToken,
                    onValueChange = { trackingToken = it },
                    label = { Text("Tracking Code") },
                    placeholder = { Text("Enter tracking token") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onClaimSubmit(reference, trackingToken) },
                enabled = reference.isNotBlank() && trackingToken.isNotBlank(),
            ) {
                Text("Add Report")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
