package com.serqfix.partner.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST

// Request models
data class RequestOtpRequest(
    val phoneNumber: String,
    val loginType: String = "phone-otp"
)

data class VerifyOtpRequest(
    val phoneNumber: String,
    val loginType: String = "phone-otp",
    val otp: String
)

data class PhonePasswordLoginRequest(
    val phoneNumber: String,
    val loginType: String = "phone-password",
    val password: String,
    val otp: String? = null
)

data class EmailPasswordLoginRequest(
    val email: String,
    val loginType: String = "email-password",
    val password: String
)

// Response models
data class AuthResponse(
    val success: Boolean,
    val message: String? = null,
    @SerializedName("user")
    val data: UserData? = null,  // API returns "user" but we map it to "data" for consistency
    val token: String? = null
)

data class UserData(
    val _id: String? = null,
    val id: String? = null,
    val phoneNumber: String? = null,
    val email: String? = null,
    val name: String? = null,
    val available: Boolean? = null,
    val token: String? = null,
    val active: Boolean? = null,
    val isVerified: Boolean? = null,
    val agreementOpen: Boolean? = null,
    val agreementSigned: Boolean? = null,
    val role: String? = null,
    val providerRole: String? = null,
    val phoneNumberVerified: Boolean? = null,
    val aadharVerified: Boolean? = null,
    val panVerified: Boolean? = null,
    val bankDetailsVerified: Boolean? = null,
    val profileImageUploaded: Boolean? = null,
    val relativeFillup: Boolean? = null,
    val addressFillup: Boolean? = null,
    val expertiseFillup: Boolean? = null,
    val gender: String? = null,
    val city: String? = null,
    val state: String? = null,
    val databaseName: String? = null,
    val provider_payment_type: String? = null,
    val work_days: List<String>? = null,
    val shift_start: String? = null,
    val shift_end: String? = null,
    val base_salary: Double? = null,
    val salary_type: String? = null,
    val image: ImageData? = null
)

data class ImageData(
    val url: String? = null,
    val name: String? = null
)

interface AuthApiService {
    @POST("mobile-app/login")
    suspend fun requestPhoneOtp(@Body request: RequestOtpRequest): AuthResponse
    
    @POST("mobile-app/login")
    suspend fun verifyPhoneOtp(@Body request: VerifyOtpRequest): AuthResponse
    
    @POST("mobile-app/login")
    suspend fun loginWithPhonePassword(@Body request: PhonePasswordLoginRequest): AuthResponse
    
    @POST("mobile-app/login")
    suspend fun loginWithEmailPassword(@Body request: EmailPasswordLoginRequest): AuthResponse
}
