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
    val firstName: String? = null,
    val lastName: String? = null,
    val isTotpEnabled: Boolean = false,
    val isTotpSetupDialogOpen: Boolean = false,
    val totpSecret: String? = null,
    val totpProvisioningUri: String? = null,
    val generatedRecoveryCodes: List<String>? = null,
    val myRequests: List<Map<String, Any>> = emptyList(),
    val isRequestingOtp: Boolean = false,
    val isVerifyingOtp: Boolean = false,
    val otpRequested: Boolean = false,
    val otpVerified: Boolean = false,
    val nextStep: String = "SMS_OTP_REQUIRED",
    val resendTimerSeconds: Int = 45,
    val errorMessage: String? = null,
    val successMessage: String? = null,
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
                loadMyRequests()
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
                        val fn = body["firstName"] as? String
                        val ln = body["lastName"] as? String
                        _uiState.value = _uiState.value.copy(
                            isTotpEnabled = totp,
                            firstName = fn,
                            lastName = ln
                        )
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun updateProfile(firstName: String, lastName: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val token = trackingStore.getPublicAuthToken()
                if (!token.isNullOrBlank()) {
                    val bearer = "Bearer $token"
                    val res = api.updatePublicProfile(bearer, mapOf("firstName" to firstName, "lastName" to lastName))
                    if (res.isSuccessful) {
                        _uiState.value = _uiState.value.copy(firstName = firstName, lastName = lastName)
                        onSuccess()
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun changePassword(currentPass: String, newPass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val token = trackingStore.getPublicAuthToken()
                if (!token.isNullOrBlank()) {
                    val bearer = "Bearer $token"
                    val res = api.changePublicPassword(bearer, mapOf("currentPassword" to currentPass, "newPassword" to newPass))
                    if (res.isSuccessful) {
                        onSuccess()
                    } else {
                        onError("Failed to change password. Ensure current password is correct.")
                    }
                }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Failed to change password.")
            }
        }
    }

    fun requestForgotPasswordOtp(phone: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRequestingOtp = true, errorMessage = null)
            try {
                val norm = normalizeGhanaPhoneNumber(phone)
                val response = api.requestForgotPasswordOtp(mapOf("phoneNumber" to norm))
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isRequestingOtp = false)
                    onSuccess()
                } else {
                    _uiState.value = _uiState.value.copy(isRequestingOtp = false, errorMessage = "Failed to send reset code. Verify phone number.")
                    onError("Failed to send reset code. Verify phone number.")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isRequestingOtp = false, errorMessage = e.localizedMessage ?: "Failed to send reset code.")
                onError(e.localizedMessage ?: "Failed to send reset code.")
            }
        }
    }

    fun verifyForgotPasswordOtp(phone: String, code: String, onSuccess: (recoveryToken: String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isVerifyingOtp = true, errorMessage = null)
            try {
                val norm = normalizeGhanaPhoneNumber(phone)
                val response = api.verifyPublicForgotPasswordOtp(mapOf("phoneNumber" to norm, "code" to code))
                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()!!["recoveryToken"] ?: ""
                    _uiState.value = _uiState.value.copy(isVerifyingOtp = false)
                    onSuccess(token)
                } else {
                    _uiState.value = _uiState.value.copy(isVerifyingOtp = false, errorMessage = "Invalid or expired verification code.")
                    onError("Invalid or expired verification code.")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isVerifyingOtp = false, errorMessage = e.localizedMessage ?: "Verification failed.")
                onError(e.localizedMessage ?: "Verification failed.")
            }
        }
    }

    fun resetPasswordWithToken(phone: String, recoveryToken: String, newPass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isVerifyingOtp = true, errorMessage = null)
            try {
                val norm = normalizeGhanaPhoneNumber(phone)
                val response = api.resetPublicPasswordWithToken(mapOf("phoneNumber" to norm, "recoveryToken" to recoveryToken, "newPassword" to newPass))
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isVerifyingOtp = false)
                    onSuccess()
                } else {
                    _uiState.value = _uiState.value.copy(isVerifyingOtp = false, errorMessage = "Password reset failed.")
                    onError("Password reset failed.")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isVerifyingOtp = false, errorMessage = e.localizedMessage ?: "Password reset failed.")
                onError(e.localizedMessage ?: "Password reset failed.")
            }
        }
    }

    fun resetPasswordWithOtp(phone: String, code: String, newPass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isVerifyingOtp = true, errorMessage = null)
            try {
                val norm = normalizeGhanaPhoneNumber(phone)
                val response = api.resetPasswordWithOtp(mapOf("phoneNumber" to norm, "code" to code, "newPassword" to newPass))
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isVerifyingOtp = false)
                    onSuccess()
                } else {
                    _uiState.value = _uiState.value.copy(isVerifyingOtp = false, errorMessage = "Invalid verification code or password reset failed.")
                    onError("Invalid verification code or password reset failed.")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isVerifyingOtp = false, errorMessage = e.localizedMessage ?: "Password reset failed.")
                onError(e.localizedMessage ?: "Password reset failed.")
            }
        }
    }

    fun generateRecoveryCodes(password: String, onSuccess: (List<String>) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val token = trackingStore.getPublicAuthToken()
                if (!token.isNullOrBlank()) {
                    val bearer = "Bearer $token"
                    val res = api.generatePublicRecoveryCodes(bearer, mapOf("password" to password))
                    if (res.isSuccessful && res.body() != null) {
                        val codes = res.body()!!
                        _uiState.value = _uiState.value.copy(generatedRecoveryCodes = codes)
                        onSuccess(codes)
                    } else {
                        val errMessage = res.errorBody()?.string() ?: "Failed to generate recovery codes."
                        if (errMessage.contains("password", ignoreCase = true)) {
                            onError("Incorrect password. Please try again.")
                        } else {
                            onError("Failed to generate recovery codes.")
                        }
                    }
                } else {
                    onError("Unauthenticated citizen session. Please sign in.")
                }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Failed to generate recovery codes.")
            }
        }
    }

    fun submitPublicMfaRecoveryRequest(reason: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val token = trackingStore.getPublicAuthToken()
                val bearer = if (!token.isNullOrBlank()) "Bearer $token" else null
                val currentPhone = _uiState.value.phoneNumber ?: _uiState.value.phoneNumberInput
                val reqBody = mutableMapOf("reason" to reason)
                if (!currentPhone.isBlank()) {
                    reqBody["phoneNumber"] = normalizeGhanaPhoneNumber(currentPhone)
                }
                val res = api.submitPublicMfaRecoveryRequest(bearer, reqBody)
                if (res.isSuccessful) {
                    onSuccess()
                    loadMyRequests()
                } else {
                    val errBody = res.errorBody()?.string() ?: ""
                    if (errBody.contains("ALREADY_PENDING")) {
                        onError("You already have an active recovery request awaiting review.")
                    } else if (!errBody.isBlank()) {
                        onError("Failed to submit recovery request: Please ensure valid phone number.")
                    } else {
                        onError("Failed to submit recovery request.")
                    }
                }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Failed to submit recovery request.")
            }
        }
    }

    fun collectPublicRecoveryCodes(requestId: String, onSuccess: (List<String>) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val token = trackingStore.getPublicAuthToken()
                if (!token.isNullOrBlank()) {
                    val bearer = "Bearer $token"
                    val res = api.collectPublicRecoveryCodes(bearer, mapOf("requestId" to requestId))
                    if (res.isSuccessful && res.body() != null) {
                        val codes = res.body()!!
                        onSuccess(codes)
                        loadMyRequests()
                    } else {
                        onError("Unable to collect recovery codes. Ensure request is approved.")
                    }
                }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Failed to collect recovery codes.")
            }
        }
    }

    fun submitPhoneChangeRequest(requestedPhone: String, reason: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val token = trackingStore.getPublicAuthToken()
                if (!token.isNullOrBlank()) {
                    val bearer = "Bearer $token"
                    val res = api.submitPublicPhoneChangeRequest(bearer, mapOf("requestedPhone" to requestedPhone, "reason" to reason))
                    if (res.isSuccessful) {
                        onSuccess()
                        loadMyRequests()
                    } else {
                        onError("Failed to submit request. Check phone number format.")
                    }
                }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Failed to submit phone change request.")
            }
        }
    }

    fun loadMyRequests() {
        viewModelScope.launch {
            try {
                val token = trackingStore.getPublicAuthToken()
                if (!token.isNullOrBlank()) {
                    val bearer = "Bearer $token"
                    val res = api.getPublicMyRequests(bearer)
                    if (res.isSuccessful && res.body() != null) {
                        _uiState.value = _uiState.value.copy(myRequests = res.body()!!)
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
                        _uiState.value = _uiState.value.copy(errorMessage = "Invalid authenticator code. Please try again.")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.localizedMessage ?: "Failed to confirm TOTP setup")
            }
        }
    }

    fun startTotpReconfiguration(password: String, onSuccess: (secret: String, provisioningUri: String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val token = trackingStore.getPublicAuthToken()
                if (!token.isNullOrBlank()) {
                    val bearer = "Bearer $token"
                    val response = api.startTotpReconfiguration(bearer, mapOf("password" to password))
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        val secret = body["manualSetupKey"] ?: body["secret"] ?: ""
                        val uri = body["provisioningUri"] ?: ""
                        onSuccess(secret, uri)
                    } else {
                        onError("Incorrect password. Unable to reconfigure authenticator.")
                    }
                } else {
                    onError("Unauthenticated citizen session. Please sign in.")
                }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Failed to reconfigure authenticator.")
            }
        }
    }

    fun confirmTotpReconfiguration(newSecret: String, code: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val token = trackingStore.getPublicAuthToken()
                if (!token.isNullOrBlank()) {
                    val bearer = "Bearer $token"
                    val response = api.confirmTotpReconfiguration(bearer, mapOf("secret" to newSecret, "code" to code))
                    if (response.isSuccessful) {
                        _uiState.value = _uiState.value.copy(isTotpEnabled = true)
                        onSuccess()
                    } else {
                        onError("Invalid 6-digit code from new authenticator app.")
                    }
                } else {
                    onError("Unauthenticated citizen session. Please sign in.")
                }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Failed to confirm new authenticator.")
            }
        }
    }

    fun disableTotp() {
        viewModelScope.launch {
            try {
                val token = trackingStore.getPublicAuthToken()
                if (!token.isNullOrBlank()) {
                    val bearer = "Bearer $token"
                    val response = api.disablePublicTotp(bearer)
                    if (response.isSuccessful) {
                        _uiState.value = _uiState.value.copy(isTotpEnabled = false)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun dismissTotpSetupDialog() {
        _uiState.value = _uiState.value.copy(
            isTotpSetupDialogOpen = false,
            totpSecret = null,
            totpProvisioningUri = null,
            errorMessage = null,
        )
    }

    fun requestRegisterOtp(phone: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isRequestingOtp = true,
                errorMessage = null,
            )
            try {
                val norm = normalizeGhanaPhoneNumber(phone)
                val response = api.requestRegisterOtp(mapOf("phoneNumber" to norm))
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = _uiState.value.copy(
                        isRequestingOtp = false,
                        otpRequested = true,
                    )
                    startResendTimer()
                    onSuccess()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isRequestingOtp = false,
                        errorMessage = "Failed to send verification code. Check phone number.",
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRequestingOtp = false,
                    errorMessage = e.localizedMessage ?: "Failed to send OTP",
                )
            }
        }
    }

    fun requestSmsFallbackOtp(phone: String) {
        viewModelScope.launch {
            try {
                val norm = normalizeGhanaPhoneNumber(phone)
                api.requestSmsFallbackOtp(mapOf("phoneNumber" to norm))
            } catch (_: Exception) {}
        }
    }

    fun registerWithPassword(phone: String, password: String, firstName: String, lastName: String, code: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isVerifyingOtp = true, errorMessage = null)
            try {
                val installId = trackingStore.getOrCreateInstallationId()
                val response = api.registerWithPassword(
                    installId,
                    mapOf(
                        "phoneNumber" to phone,
                        "password" to password,
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "code" to code
                    )
                )
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val token = body["accessToken"] as? String
                    if (!token.isNullOrBlank()) {
                        trackingStore.savePublicAuthToken(token, phone)
                        _uiState.value = _uiState.value.copy(
                            isVerifyingOtp = false,
                            isLoggedIn = true,
                            phoneNumber = phone,
                            firstName = firstName,
                            lastName = lastName,
                        )
                        onSuccess()
                    } else {
                        _uiState.value = _uiState.value.copy(isVerifyingOtp = false, errorMessage = "Authentication failed.")
                        onError("Authentication failed.")
                    }
                } else {
                    _uiState.value = _uiState.value.copy(isVerifyingOtp = false, errorMessage = "Invalid verification code.")
                    onError("Invalid verification code.")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isVerifyingOtp = false, errorMessage = e.localizedMessage ?: "Registration failed.")
                onError(e.localizedMessage ?: "Registration failed.")
            }
        }
    }

    fun loginWithPassword(phone: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRequestingOtp = true, errorMessage = null)
            try {
                val installId = trackingStore.getOrCreateInstallationId()
                val response = api.loginWithPassword(installId, mapOf("phoneNumber" to phone, "password" to password))
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val token = body["accessToken"] as? String
                    val fn = body["firstName"] as? String
                    val ln = body["lastName"] as? String
                    if (!token.isNullOrBlank()) {
                        trackingStore.savePublicAuthToken(token, phone)
                        _uiState.value = _uiState.value.copy(
                            isRequestingOtp = false,
                            isLoggedIn = true,
                            phoneNumber = phone,
                            firstName = fn,
                            lastName = ln,
                        )
                        onSuccess()
                    } else {
                        _uiState.value = _uiState.value.copy(isRequestingOtp = false, errorMessage = "Login failed.")
                        onError("Login failed.")
                    }
                } else {
                    _uiState.value = _uiState.value.copy(isRequestingOtp = false, errorMessage = "Invalid phone or password.")
                    onError("Invalid phone or password.")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isRequestingOtp = false, errorMessage = e.localizedMessage ?: "Login failed.")
                onError(e.localizedMessage ?: "Login failed.")
            }
        }
    }

    fun loginWithTotp(phone: String, totpCode: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRequestingOtp = true, errorMessage = null)
            try {
                val installId = trackingStore.getOrCreateInstallationId()
                val response = api.verifyPublicTotpLogin(installId, mapOf("phoneNumber" to phone, "code" to totpCode))
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val token = body["accessToken"] as? String
                    val fn = body["firstName"] as? String
                    val ln = body["lastName"] as? String
                    if (!token.isNullOrBlank()) {
                        trackingStore.savePublicAuthToken(token, phone)
                        _uiState.value = _uiState.value.copy(
                            isRequestingOtp = false,
                            isLoggedIn = true,
                            phoneNumber = phone,
                            firstName = fn,
                            lastName = ln,
                            isTotpEnabled = true,
                        )
                        onSuccess()
                    } else {
                        _uiState.value = _uiState.value.copy(isRequestingOtp = false, errorMessage = "TOTP authentication failed.")
                        onError("TOTP authentication failed.")
                    }
                } else {
                    _uiState.value = _uiState.value.copy(isRequestingOtp = false, errorMessage = "Invalid TOTP code.")
                    onError("Invalid TOTP code.")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isRequestingOtp = false, errorMessage = e.localizedMessage ?: "TOTP login failed.")
                onError(e.localizedMessage ?: "TOTP login failed.")
            }
        }
    }

    fun verifyRegisterOtp(phone: String, code: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isVerifyingOtp = true,
                errorMessage = null,
            )
            try {
                val norm = normalizeGhanaPhoneNumber(phone)
                val installId = trackingStore.getOrCreateInstallationId()
                val response = api.verifyRegisterOtp(installId, mapOf("phoneNumber" to norm, "code" to code))
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val token = body["accessToken"] as? String
                    if (!token.isNullOrBlank()) {
                        trackingStore.savePublicAuthToken(token, norm)
                        _uiState.value = _uiState.value.copy(
                            isVerifyingOtp = false,
                            otpVerified = true,
                            isLoggedIn = true,
                            phoneNumber = norm,
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isVerifyingOtp = false,
                            errorMessage = "Authentication failed.",
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isVerifyingOtp = false,
                        errorMessage = "Invalid verification code.",
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isVerifyingOtp = false,
                    errorMessage = e.localizedMessage ?: "Verification failed",
                )
            }
        }
    }

    private fun startResendTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            for (i in 45 downTo 0) {
                _uiState.value = _uiState.value.copy(resendTimerSeconds = i)
                delay(1000)
            }
        }
    }

    private fun loadMyReports() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingReports = true)
            try {
                val installId = trackingStore.getOrCreateInstallationId()
                val token = trackingStore.getPublicAuthToken()
                val bearer = if (!token.isNullOrBlank()) "Bearer $token" else null
                val response = api.getMyReports(installId, bearer)
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = _uiState.value.copy(
                        myReports = response.body()!!,
                        isLoadingReports = false,
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoadingReports = false)
                }
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoadingReports = false)
            }
        }
    }

    fun claimReport(reference: String, trackingToken: String) {
        viewModelScope.launch {
            try {
                val token = trackingStore.getPublicAuthToken()
                if (!token.isNullOrBlank()) {
                    val bearer = "Bearer $token"
                    val response = api.claimReport(
                        bearer,
                        mapOf("publicReference" to reference, "trackingToken" to trackingToken)
                    )
                    if (response.isSuccessful) {
                        loadMyReports()
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun signOut() {
        viewModelScope.launch {
            trackingStore.clearPublicAuthToken()
            _uiState.value = AccountUiState(isLoggedIn = false)
        }
    }
}
