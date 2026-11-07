package com.gec.emergency.data.remote

import com.gec.emergency.data.remote.dto.AgencyResponseDto
import com.gec.emergency.data.remote.dto.CreateEmergencyCallAttemptRequestDto
import com.gec.emergency.data.remote.dto.CreateLocationSessionRequestDto
import com.gec.emergency.data.remote.dto.EmergencyCallAttemptResponseDto
import com.gec.emergency.data.remote.dto.IncidentCategoryResponseDto
import com.gec.emergency.data.remote.dto.IncidentReportRequestDto
import com.gec.emergency.data.remote.dto.IncidentReportResponseDto
import com.gec.emergency.data.remote.dto.LiveTrackingRequestDto
import com.gec.emergency.data.remote.dto.LocationPointRequestDto
import com.gec.emergency.data.remote.dto.LocationSessionResponseDto
import com.gec.emergency.data.remote.dto.RegionRefResponseDto
import com.gec.emergency.data.remote.dto.SafetyInformationResponseDto
import com.gec.emergency.data.remote.dto.StationContactResponseDto
import com.gec.emergency.data.remote.dto.StationDetailsResponseDto
import com.gec.emergency.data.remote.dto.StationResponseDto
import com.gec.emergency.data.remote.dto.SubmitResponseReviewRequestDto
import com.gec.emergency.data.remote.dto.TrackedReportResponseDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface EmergencyDirectoryApi {

    @GET("api/v1/public/agencies")
    suspend fun getAgencies(): Response<List<AgencyResponseDto>>

    @GET("api/v1/public/agencies/{agencyId}")
    suspend fun getAgency(@Path("agencyId") agencyId: String): Response<AgencyResponseDto>

    @GET("api/v1/public/stations")
    suspend fun getAllPublicStations(): Response<List<StationResponseDto>>

    @GET("api/v1/public/agencies/{agencyId}/stations")
    suspend fun getStationsByAgency(@Path("agencyId") agencyId: String): Response<List<StationResponseDto>>

    @GET("api/v1/public/stations/{stationId}")
    suspend fun getStationDetails(@Path("stationId") stationId: String): Response<StationDetailsResponseDto>

    @GET("api/v1/public/stations/{stationId}/contacts")
    suspend fun getStationContacts(@Path("stationId") stationId: String): Response<List<StationContactResponseDto>>

    @GET("api/v1/public/stations/headquarters")
    suspend fun getPublicHeadquarters(): Response<List<StationDetailsResponseDto>>

    @GET("api/v1/public/stations/headquarters/agencies/{agencyId}/regions/{regionId}")
    suspend fun getPublicRegionalHeadquarters(
        @Path("agencyId") agencyId: String,
        @Path("regionId") regionId: String,
    ): Response<StationDetailsResponseDto>

    @GET("api/v1/public/regions")
    suspend fun getPublicRegions(): Response<List<RegionRefResponseDto>>

    @GET("api/v1/public/incident-categories")
    suspend fun getIncidentCategories(@Query("mode") mode: String? = null): Response<List<IncidentCategoryResponseDto>>

    @GET("api/v1/public/safety-information")
    suspend fun getPublicSafetyInformation(
        @Query("category") category: String? = null,
        @Query("severity") severity: String? = null
    ): Response<List<SafetyInformationResponseDto>>

    @GET("api/v1/public/safety-information/{id}")
    suspend fun getPublicSafetyInformationById(@Path("id") id: String): Response<SafetyInformationResponseDto>

    @GET("api/v1/public/account/notifications/unread-count")
    suspend fun getUnreadNotificationCount(): Response<Map<String, Long>>

    @POST("api/v1/public/account/notifications/{id}/read")
    suspend fun markNotificationAsRead(@Path("id") id: String): Response<Void>

    @POST("api/v1/public/emergency-call-attempts")
    suspend fun createEmergencyCallAttempt(
        @Header("X-Installation-ID") installationId: String?,
        @Header("Authorization") bearerToken: String?,
        @Body request: CreateEmergencyCallAttemptRequestDto
    ): Response<EmergencyCallAttemptResponseDto>

    @PATCH("api/v1/public/emergency-call-attempts/{id}/launched")
    suspend fun markCallLaunched(
        @Path("id") callAttemptId: String
    ): Response<EmergencyCallAttemptResponseDto>

    @GET("api/v1/public/live-tracking-requests/reports/{reportId}")
    suspend fun getRequestsForReport(
        @Path("reportId") reportId: String
    ): Response<List<LiveTrackingRequestDto>>

    @POST("api/v1/public/live-tracking-requests/{requestId}/respond")
    suspend fun respondToLiveTrackingRequest(
        @Path("requestId") requestId: String,
        @Query("action") action: String
    ): Response<LiveTrackingRequestDto>

    @POST("api/v1/public/location-sessions")
    suspend fun createLocationSession(
        @Header("X-Installation-ID") installationId: String?,
        @Body request: CreateLocationSessionRequestDto
    ): Response<LocationSessionResponseDto>

    @POST("api/v1/public/location-sessions/{sessionId}/points")
    suspend fun recordLocationPoint(
        @Path("sessionId") sessionId: String,
        @Body request: LocationPointRequestDto
    ): Response<Void>

    @POST("api/v1/public/location-sessions/{sessionId}/stop")
    suspend fun stopLocationSession(
        @Path("sessionId") sessionId: String,
        @Query("reason") reason: String? = null
    ): Response<LocationSessionResponseDto>

    @POST("api/v1/public/reports")
    suspend fun submitReport(
        @Header("X-Installation-ID") installationId: String?,
        @Header("Authorization") bearerToken: String?,
        @Body request: IncidentReportRequestDto
    ): Response<IncidentReportResponseDto>

    @Multipart
    @POST("api/v1/public/reports/{reportId}/attachments")
    suspend fun uploadReportAttachment(
        @Path("reportId") reportId: String,
        @Header("X-Report-Token") trackingToken: String,
        @Part("mediaType") mediaType: RequestBody,
        @Part file: MultipartBody.Part,
    ): Response<ResponseBody>

    @GET("api/v1/public/reports/track/{publicReference}")
    suspend fun trackReport(
        @Path("publicReference") publicReference: String,
        @Header("X-Report-Token") trackingToken: String?,
    ): Response<TrackedReportResponseDto>

    @POST("api/v1/public/auth/claim-report")
    suspend fun claimReport(
        @Header("Authorization") bearerToken: String?,
        @Body body: Map<String, String>
    ): Response<TrackedReportResponseDto>

    @POST("api/v1/public/auth/reports/{reportId}/assignments/{assignmentId}/review")
    suspend fun submitResponseReview(
        @Header("Authorization") bearerToken: String?,
        @Path("reportId") reportId: String,
        @Path("assignmentId") assignmentId: String,
        @Body request: SubmitResponseReviewRequestDto
    ): Response<TrackedReportResponseDto>

    @POST("api/v1/public/auth/forgot-password/request-otp")
    suspend fun requestForgotPasswordOtp(@Body body: Map<String, String>): Response<Map<String, String>>

    @POST("api/v1/public/auth/forgot-password/verify-otp")
    suspend fun verifyPublicForgotPasswordOtp(@Body body: Map<String, String>): Response<Map<String, String>>

    @POST("api/v1/public/auth/forgot-password/reset-with-token")
    suspend fun resetPublicPasswordWithToken(@Body body: Map<String, String>): Response<Map<String, Any>>

    @POST("api/v1/public/auth/forgot-password/reset")
    suspend fun resetPasswordWithOtp(@Body body: Map<String, String>): Response<Map<String, Any>>

    @POST("api/v1/public/auth/register/request-otp")
    suspend fun requestRegisterOtp(@Body body: Map<String, String>): Response<Map<String, String>>

    @POST("api/v1/public/auth/register/verify-otp")
    suspend fun verifyRegisterOtp(
        @Header("X-Installation-ID") installationId: String?,
        @Body body: Map<String, String>
    ): Response<Map<String, Any>>

    @POST("api/v1/public/auth/register/with-password")
    suspend fun registerWithPassword(
        @Header("X-Installation-ID") installationId: String?,
        @Body body: Map<String, String>
    ): Response<Map<String, Any>>

    @POST("api/v1/public/auth/login/request-otp")
    suspend fun requestLoginOtp(@Body body: Map<String, String>): Response<Map<String, String>>

    @POST("api/v1/public/auth/login/with-password")
    suspend fun loginWithPassword(
        @Header("X-Installation-ID") installationId: String?,
        @Body body: Map<String, String>
    ): Response<Map<String, Any>>

    @POST("api/v1/public/auth/login/request-sms-fallback")
    suspend fun requestSmsFallbackOtp(@Body body: Map<String, String>): Response<Map<String, String>>

    @POST("api/v1/public/auth/login/verify-otp")
    suspend fun verifyLoginOtp(
        @Header("X-Installation-ID") installationId: String?,
        @Body body: Map<String, String>
    ): Response<Map<String, Any>>

    @POST("api/v1/public/auth/login/verify-totp")
    suspend fun verifyPublicTotpLogin(
        @Header("X-Installation-ID") installationId: String?,
        @Body body: Map<String, String>
    ): Response<Map<String, Any>>

    @GET("api/v1/public/auth/me")
    suspend fun getCurrentPublicUser(@Header("Authorization") bearerToken: String): Response<Map<String, Any>>

    @PUT("api/v1/public/auth/profile")
    suspend fun updatePublicProfile(
        @Header("Authorization") bearerToken: String,
        @Body body: Map<String, String>
    ): Response<Map<String, Any>>

    @POST("api/v1/public/auth/change-password")
    suspend fun changePublicPassword(
        @Header("Authorization") bearerToken: String,
        @Body body: Map<String, String>
    ): Response<Void>

    @POST("api/v1/public/account/mfa/recovery/generate")
    suspend fun generatePublicRecoveryCodes(
        @Header("Authorization") bearerToken: String,
        @Body body: Map<String, String>
    ): Response<List<String>>

    @POST("api/v1/public/auth/mfa/recovery/request")
    suspend fun submitPublicMfaRecoveryRequest(
        @Header("Authorization") bearerToken: String?,
        @Body body: Map<String, String>
    ): Response<Map<String, Any>>

    @POST("api/v1/public/auth/mfa/recovery/collect")
    suspend fun collectPublicRecoveryCodes(
        @Header("Authorization") bearerToken: String,
        @Body body: Map<String, String>
    ): Response<List<String>>

    @POST("api/v1/public/auth/phone-change-request")
    suspend fun submitPublicPhoneChangeRequest(
        @Header("Authorization") bearerToken: String,
        @Body body: Map<String, String>
    ): Response<Map<String, Any>>

    @GET("api/v1/public/auth/my-requests")
    suspend fun getPublicMyRequests(
        @Header("Authorization") bearerToken: String
    ): Response<List<Map<String, Any>>>

    @GET("api/v1/public/auth/my-reports")
    suspend fun getMyReports(
        @Header("X-Installation-ID") installationId: String?,
        @Header("Authorization") bearerToken: String?
    ): Response<List<TrackedReportResponseDto>>

    @POST("api/v1/public/account/mfa/totp/setup")
    suspend fun startPublicTotpSetup(
        @Header("Authorization") bearerToken: String
    ): Response<Map<String, String>>

    @POST("api/v1/public/account/mfa/totp/confirm")
    suspend fun confirmPublicTotpSetup(
        @Header("Authorization") bearerToken: String,
        @Body body: Map<String, String>
    ): Response<Map<String, String>>

    @POST("api/v1/public/account/mfa/totp/reconfigure/start")
    suspend fun startTotpReconfiguration(
        @Header("Authorization") bearerToken: String,
        @Body body: Map<String, String>
    ): Response<Map<String, String>>

    @POST("api/v1/public/account/mfa/totp/reconfigure/confirm")
    suspend fun confirmTotpReconfiguration(
        @Header("Authorization") bearerToken: String,
        @Body body: Map<String, String>
    ): Response<Map<String, String>>

    @POST("api/v1/public/account/mfa/totp/disable")
    suspend fun disablePublicTotp(
        @Header("Authorization") bearerToken: String
    ): Response<Map<String, String>>
}
