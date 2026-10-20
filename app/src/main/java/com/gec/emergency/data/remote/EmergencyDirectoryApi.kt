package com.gec.emergency.data.remote

import com.gec.emergency.data.remote.dto.AgencyResponseDto
import com.gec.emergency.data.remote.dto.IncidentCategoryResponseDto
import com.gec.emergency.data.remote.dto.IncidentReportRequestDto
import com.gec.emergency.data.remote.dto.IncidentReportResponseDto
import com.gec.emergency.data.remote.dto.RegionRefResponseDto
import com.gec.emergency.data.remote.dto.StationContactResponseDto
import com.gec.emergency.data.remote.dto.StationDetailsResponseDto
import com.gec.emergency.data.remote.dto.StationResponseDto
import com.gec.emergency.data.remote.dto.TrackedReportResponseDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface EmergencyDirectoryApi {

    @GET("api/v1/public/agencies")
    suspend fun getAgencies(): Response<List<AgencyResponseDto>>

    @GET("api/v1/public/agencies/{agencyId}")
    suspend fun getAgency(@Path("agencyId") agencyId: String): Response<AgencyResponseDto>

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
        @Body body: Map<String, Any?>
    ): Response<TrackedReportResponseDto>

    @POST("api/v1/public/auth/register/request-otp")
    suspend fun requestRegisterOtp(@Body body: Map<String, String>): Response<Map<String, String>>

    @POST("api/v1/public/auth/register/verify-otp")
    suspend fun verifyRegisterOtp(
        @Header("X-Installation-ID") installationId: String?,
        @Body body: Map<String, String>
    ): Response<Map<String, Any>>

    @POST("api/v1/public/auth/login/request-otp")
    suspend fun requestLoginOtp(@Body body: Map<String, String>): Response<Map<String, String>>

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

    @POST("api/v1/public/account/mfa/totp/disable")
    suspend fun disablePublicTotp(
        @Header("Authorization") bearerToken: String
    ): Response<Map<String, String>>
}
