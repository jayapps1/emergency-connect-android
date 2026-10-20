package com.gec.emergency.feature.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gec.emergency.data.local.ReportTrackingStore
import com.gec.emergency.data.remote.EmergencyDirectoryApi
import com.gec.emergency.data.remote.dto.TrackedReportResponseDto
import com.gec.emergency.feature.auth.normalizeGhanaPhoneNumber
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountUiState(
    val isLoggedIn: Boolean = false,
    val phoneNumberInput: String = "",
    val phoneNumber: String? = null,
    val isTotpEnabled: Boolean = false,
    val isTotpSetupDialogOpen: Boolean = false,
    val totpSecret: String? = null,
    val totpProvisioningUri: String? = null,
    val isRequestingOtp: Boolean = false,
    val isVerifyingOtp: Boolean = false,
    val otpRequested: Boolean = false,
    val otpVerified: Boolean = false,
    val nextStep: String = "SMS_OTP_REQUIRED",
    val resendTimerSeconds: Int = 45,
    val errorMessage: String? = null,
    val myReports: List<TrackedReportResponseDto> = emptyList(),
    val isLoadingReports: Boolean = false,
)

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val api: EmergencyDirectoryApi,
    private val trackingStore: ReportTrackingStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        checkAuthStatus()
    }

    fun checkAuthStatus() {
        viewModelScope.launch {
            val token = trackingStore.getPublicAuthToken()
            val phone = trackingStore.getPublicUserPhone()
            if (!token.isNullOrBlank()) {
                _uiState.value = _uiState.value.copy(
                    isLoggedIn = true,
                    phoneNumber = phone,
                )
                loadUserInfo()
                loadMyReports()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoggedIn = false,
                    phoneNumber = null,
                )
            }
        }
    }

    private fun loadUserInfo() {
        viewModelScope.launch {
            try {
                val token = trackingStore.getPublicAuthToken()
                if (!token.isNullOrBlank()) {
                    val bearer = "Bearer $token"
                    val response = api.getCurrentPublicUser(bearer)
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        val totp = body["totpEnabled"] as? Boolean ?: false
                        _uiState.value = _uiState.value.copy(isTotpEnabled = totp)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun onPhoneNumberInputChanged(input: String) {
        _uiState.value = _uiState.value.copy(
            phoneNumberInput = input,
            errorMessage = null,
        )
    }

    fun startTotpSetup() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(errorMessage = null)
            try {
                val token = trackingStore.getPublicAuthToken()
                if (!token.isNullOrBlank()) {
                    val bearer = "Bearer $token"
                    val response = api.startPublicTotpSetup(bearer)
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        val secret = body["manualSetupKey"] ?: body["secret"] ?: ""
                        val uri = body["provisioningUri"] ?: ""
                        _uiState.value = _uiState.value.copy(
                            totpSecret = secret,
                            totpProvisioningUri = uri,
                            isTotpSetupDialogOpen = true,
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.localizedMessage ?: "Failed to start TOTP setup")
            }
        }
    }

    fun confirmTotpSetup(code: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(errorMessage = null)
            try {
                val token = trackingStore.getPublicAuthToken()
                if (!token.isNullOrBlank()) {
                    val bearer = "Bearer $token"
                    val response = api.confirmPublicTotpSetup(bearer, mapOf("code" to code))
                    if (response.isSuccessful) {
                        _uiState.value = _uiState.value.copy(
                            isTotpEnabled = true,
                            isTotpSetupDialogOpen = false,
                            totpSecret = null,
                            totpProvisioningUri = null,
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(errorMessage = "Invalid 6-digit TOTP code. Please try again.")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.localizedMessage ?: "Failed to confirm TOTP setup")
            }
        }
    }

    fun disableTotp() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(errorMessage = null)
            try {
                val token = trackingStore.getPublicAuthToken()
                if (!token.isNullOrBlank()) {
                    val bearer = "Bearer $token"
                    val response = api.disablePublicTotp(bearer)
                    if (response.isSuccessful) {
                        _uiState.value = _uiState.value.copy(isTotpEnabled = false)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.localizedMessage ?: "Failed to disable TOTP")
            }
        }
    }

    fun dismissTotpSetupDialog() {
        _uiState.value = _uiState.value.copy(
            isTotpSetupDialogOpen = false,
            totpSecret = null,
            totpProvisioningUri = null,
        )
    }

    fun requestRegisterOtp(phone: String) {
        val canonicalPhone = normalizeGhanaPhoneNumber(phone)
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRequestingOtp = true, errorMessage = null)
            try {
                val response = api.requestRegisterOtp(mapOf("phoneNumber" to canonicalPhone))
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val nextStep = body["nextStep"] ?: "SMS_OTP_REQUIRED"
                    _uiState.value = _uiState.value.copy(
                        isRequestingOtp = false,
                        otpRequested = true,
                        nextStep = nextStep,
                        phoneNumber = canonicalPhone,
                    )
                    if (nextStep == "SMS_OTP_REQUIRED") {
                        startResendTimer()
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isRequestingOtp = false,
                        errorMessage = "Unable to send verification code right now. Please try again.",
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRequestingOtp = false,
                    errorMessage = e.localizedMessage ?: "Failed to connect to verification service.",
                )
            }
        }
    }

    fun requestSmsFallbackOtp(phone: String) {
        val canonicalPhone = normalizeGhanaPhoneNumber(phone)
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRequestingOtp = true, errorMessage = null)
            try {
                val response = api.requestSmsFallbackOtp(mapOf("phoneNumber" to canonicalPhone))
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isRequestingOtp = false,
                        nextStep = "SMS_OTP_REQUIRED",
                        phoneNumber = canonicalPhone,
                    )
                    startResendTimer()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRequestingOtp = false,
                    errorMessage = e.localizedMessage ?: "SMS fallback failed.",
                )
            }
        }
    }

    fun verifyRegisterOtp(phone: String, code: String) {
        val canonicalPhone = normalizeGhanaPhoneNumber(phone)
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isVerifyingOtp = true, errorMessage = null)
            try {
                val installationId = trackingStore.getOrCreateInstallationId()
                val response = if (_uiState.value.nextStep == "TOTP_REQUIRED") {
                    api.verifyPublicTotpLogin(installationId, mapOf("phoneNumber" to canonicalPhone, "code" to code))
                } else {
                    api.verifyRegisterOtp(installationId, mapOf("phoneNumber" to canonicalPhone, "code" to code))
                }

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val token = body["accessToken"] as? String
                    val normPhone = body["phoneNumber"] as? String ?: canonicalPhone

                    if (!token.isNullOrBlank()) {
                        trackingStore.savePublicAuthToken(token, normPhone)
                        _uiState.value = _uiState.value.copy(
                            isVerifyingOtp = false,
                            otpRequested = false,
                            otpVerified = true,
                            isLoggedIn = true,
                            phoneNumber = normPhone,
                        )
                        loadMyReports()
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isVerifyingOtp = false,
                            errorMessage = "Verification failed. Invalid token received.",
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isVerifyingOtp = false,
                        errorMessage = "Invalid or expired verification code.",
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isVerifyingOtp = false,
                    errorMessage = e.localizedMessage ?: "Verification failed.",
                )
            }
        }
    }

    fun claimReport(reference: String, trackingToken: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingReports = true, errorMessage = null)
            try {
                val token = trackingStore.getPublicAuthToken()
                val bearer = if (!token.isNullOrBlank()) "Bearer $token" else null

                val response = api.claimReport(bearer, mapOf("reference" to reference, "trackingToken" to trackingToken))
                if (response.isSuccessful) {
                    loadMyReports()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoadingReports = false,
                        errorMessage = "Unable to add this report. Check the reference and tracking code.",
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingReports = false,
                    errorMessage = e.localizedMessage ?: "Failed to claim report.",
                )
            }
        }
    }

    fun loadMyReports() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingReports = true)
            try {
                val installationId = trackingStore.getOrCreateInstallationId()
                val token = trackingStore.getPublicAuthToken()
                val bearer = if (!token.isNullOrBlank()) "Bearer $token" else null

                val response = api.getMyReports(installationId, bearer)
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoadingReports = false,
                        myReports = response.body()!!,
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoadingReports = false)
                }
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoadingReports = false)
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            trackingStore.clearPublicAuthToken()
            _uiState.value = AccountUiState()
        }
    }

    private fun startResendTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            for (sec in 45 downTo 0) {
                _uiState.value = _uiState.value.copy(resendTimerSeconds = sec)
                delay(1000L)
            }
        }
    }

    fun resetState() {
        _uiState.value = _uiState.value.copy(
            phoneNumberInput = "",
            otpRequested = false,
            otpVerified = false,
            nextStep = "SMS_OTP_REQUIRED",
            errorMessage = null,
        )
    }
}
