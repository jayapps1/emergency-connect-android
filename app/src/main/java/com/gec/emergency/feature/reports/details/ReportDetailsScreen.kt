package com.gec.emergency.feature.reports.details

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gec.emergency.R
import com.gec.emergency.data.remote.dto.TrackedReportResponseDto
import com.gec.emergency.domain.model.AgencyType
import com.gec.emergency.domain.model.GeoLocation
import com.gec.emergency.domain.model.IncidentReport
import com.gec.emergency.domain.model.IncidentSeverity
import com.gec.emergency.domain.model.ReportLocation
import com.gec.emergency.domain.model.ReportMode
import com.gec.emergency.domain.model.ReportStatus
import com.gec.emergency.domain.model.ReportStatusHistory
import com.gec.emergency.ui.theme.GhanaEmergencyConnectTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailsScreen(
    uiState: ReportDetailsUiState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenRatingDialog: (TrackedReportResponseDto.AgencyAssignmentDetailDto) -> Unit = {},
    onDismissRatingDialog: () -> Unit = {},
    onSubmitReview: (Int, Int?, Int?, Int?, String) -> Unit = { _, _, _, _, _ -> },
) {
    var ratingValue by remember { mutableIntStateOf(5) }
    var reviewComment by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Report Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.content_desc_back),
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
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.report != null || uiState.trackedDetail != null -> {
                    val report = uiState.report
                    val tracked = uiState.trackedDetail
                    val publicRef = tracked?.publicReference ?: report?.publicReference ?: report?.id ?: ""
                    val currentStatus = tracked?.status ?: report?.status?.name ?: "SUBMITTED"

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Report Reference Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "Reference Number",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = publicRef,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AssistChip(
                                        onClick = { },
                                        label = { Text("Status: $currentStatus") },
                                    )
                                    if (report != null) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        AssistChip(
                                            onClick = { },
                                            label = { Text("Mode: ${report.mode.name}") },
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Multi-Agency Assignment Response Cards
                        if (tracked != null && tracked.agencyAssignments.isNotEmpty()) {
                            Text(
                                text = "Responding Agencies & Status",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            tracked.agencyAssignments.forEach { assignment ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    ),
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Shield,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = assignment.agencyName,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                )
                                            }
                                            AssistChip(
                                                onClick = { },
                                                label = { Text(assignment.status) },
                                            )
                                        }

                                        if (!assignment.stationName.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Station: ${assignment.stationName}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        if (assignment.status == "RESOLVED") {
                                            if (assignment.review != null) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "Your Rating: ",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold,
                                                    )
                                                    repeat(assignment.review.rating) {
                                                        Icon(
                                                            imageVector = Icons.Default.Star,
                                                            contentDescription = null,
                                                            tint = Color(0xFFFFB300),
                                                        )
                                                    }
                                                }
                                                if (!assignment.review.comment.isNullOrBlank()) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "\"${assignment.review.comment}\"",
                                                        style = MaterialTheme.typography.bodySmall,
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))
                                                OutlinedButton(onClick = { onOpenRatingDialog(assignment) }) {
                                                    Text("Edit Review")
                                                }
                                            } else {
                                                Button(onClick = { onOpenRatingDialog(assignment) }) {
                                                    Icon(imageVector = Icons.Default.Star, contentDescription = null)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Rate Response")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Status History Timeline Card
                        if (uiState.statusHistory.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text(
                                        text = "Status History Timeline",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    uiState.statusHistory.forEachIndexed { index, history ->
                                        if (index > 0) {
                                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                        }
                                        val histTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                            .format(Date(history.timestampMillis))
                                        Column {
                                            Text(
                                                text = history.status.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                            )
                                            Text(
                                                text = histTime,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                            if (!history.note.isNullOrBlank()) {
                                                Text(
                                                    text = history.note,
                                                    style = MaterialTheme.typography.bodySmall,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {
                    Text(
                        text = uiState.errorMessage ?: "Report not found.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            }
        }
    }

    if (uiState.isRatingDialogOpen && uiState.ratingTargetAssignment != null) {
        val target = uiState.ratingTargetAssignment
        AlertDialog(
            onDismissRequest = onDismissRatingDialog,
            title = {
                Text(
                    text = "Rate ${target.agencyName}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Column {
                    Text(
                        text = "Overall Response Rating:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        (1..5).forEach { star ->
                            Icon(
                                imageVector = if (star <= ratingValue) Icons.Default.Star else Icons.Default.StarOutline,
                                contentDescription = null,
                                tint = if (star <= ratingValue) Color(0xFFFFB300) else Color.Gray,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable { ratingValue = star },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = reviewComment,
                        onValueChange = { reviewComment = it },
                        label = { Text("Comments / Feedback (Optional)") },
                        placeholder = { Text("The emergency response was fast and professional...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4,
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSubmitReview(ratingValue, ratingValue, ratingValue, ratingValue, reviewComment)
                        reviewComment = ""
                    },
                    enabled = !uiState.isSubmittingReview,
                ) {
                    Text("Submit Review")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRatingDialog) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ReportDetailsScreenPreview() {
    val sampleReport = IncidentReport(
        id = "rpt_01",
        publicReference = "RPT-20261014-ABC123",
        categoryId = "cat_robbery",
        mode = ReportMode.SILENT,
        createdAtMillis = System.currentTimeMillis(),
        severity = IncidentSeverity.CRITICAL,
        status = ReportStatus.SUBMITTED,
        location = ReportLocation(geoLocation = GeoLocation(5.5500, -0.2000, 10.0)),
    )
    val sampleHistory = listOf(
        ReportStatusHistory(
            id = "hist_01",
            reportId = "rpt_01",
            status = ReportStatus.SUBMITTED,
            timestampMillis = System.currentTimeMillis(),
            note = "Submitted via GEC Mobile API.",
        )
    )
    GhanaEmergencyConnectTheme {
        ReportDetailsScreen(
            uiState = ReportDetailsUiState(
                report = sampleReport,
                targetAgencyTypes = setOf(AgencyType.POLICE),
                statusHistory = sampleHistory,
            ),
            onBackClick = {},
        )
    }
}
