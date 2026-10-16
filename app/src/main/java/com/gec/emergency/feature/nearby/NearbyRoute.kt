package com.gec.emergency.feature.nearby

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun NearbyRoute(
    onBackClick: () -> Unit,
    onBrowseAllServicesClick: () -> Unit,
    onStationClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NearbyViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (fineGranted || coarseGranted) {
            viewModel.onPermissionGranted()
        } else {
            viewModel.onPermissionDenied(permanentlyDenied = false)
        }
    }

    LaunchedEffect(Unit) {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            viewModel.onPermissionGranted()
        }
    }

    NearbyScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onGrantPermissionClick = {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                )
            )
        },
        onBrowseAllServicesClick = onBrowseAllServicesClick,
        onStationClick = onStationClick,
        onRefreshLocationClick = { viewModel.refreshLocation() },
        onAgencySelected = { agencyId -> viewModel.setSelectedAgency(agencyId) },
        onRadiusSelected = { radius -> viewModel.setSearchRadius(radius) },
        modifier = modifier,
    )
}
