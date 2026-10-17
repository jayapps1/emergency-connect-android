package com.gec.emergency.feature.reports.silent

import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SilentReportRoute(
    onBackClick: () -> Unit,
    onViewReportDetails: (String) -> Unit,
    onNavigateToEmergencyCall: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SilentReportViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            val contentResolver = context.contentResolver
            var fileName = "photo.jpg"
            var fileSize = 0L

            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (nameIndex != -1) fileName = cursor.getString(nameIndex) ?: fileName
                    if (sizeIndex != -1) fileSize = cursor.getLong(sizeIndex)
                }
            }

            viewModel.addAttachment(
                SelectedMediaAttachment(
                    uri = uri,
                    mediaType = "IMAGE",
                    fileName = fileName,
                    mimeType = contentResolver.getType(uri) ?: "image/jpeg",
                    sizeBytes = fileSize,
                )
            )
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            val contentResolver = context.contentResolver
            var fileName = "video.mp4"
            var fileSize = 0L

            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (nameIndex != -1) fileName = cursor.getString(nameIndex) ?: fileName
                    if (sizeIndex != -1) fileSize = cursor.getLong(sizeIndex)
                }
            }

            viewModel.addAttachment(
                SelectedMediaAttachment(
                    uri = uri,
                    mediaType = "VIDEO",
                    fileName = fileName,
                    mimeType = contentResolver.getType(uri) ?: "video/mp4",
                    sizeBytes = fileSize,
                )
            )
        }
    }

    SilentReportScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onCategorySelected = { category ->
            viewModel.selectCategory(category)
        },
        onDescriptionChange = { text ->
            viewModel.updateDescription(text)
        },
        onVehicleRegistrationChange = { text ->
            viewModel.updateVehicleRegistrationNumber(text)
        },
        onVehicleTypeChange = { text ->
            viewModel.updateVehicleType(text)
        },
        onVehicleColourChange = { text ->
            viewModel.updateVehicleColour(text)
        },
        onDirectionOfTravelChange = { text ->
            viewModel.updateDirectionOfTravel(text)
        },
        onAddPhotoClick = {
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        onAddVideoClick = {
            videoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
            )
        },
        onRemoveAttachment = { attachment ->
            viewModel.removeAttachment(attachment)
        },
        onRefreshLocation = {
            viewModel.refreshLocation()
        },
        onSubmitReport = {
            viewModel.submitSilentReport(context)
        },
        onViewReportDetails = onViewReportDetails,
        onNavigateToEmergencyCall = onNavigateToEmergencyCall,
        modifier = modifier,
    )
}
