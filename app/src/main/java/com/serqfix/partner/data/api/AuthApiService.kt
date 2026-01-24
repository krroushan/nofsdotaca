package com.serqfix.partner.data.api

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
    val data: UserData? = null,
    val token: String? = null
)

data class UserData(
    val _id: String? = null,
    val id: String? = null,
    val phoneNumber: String? = null,
    val email: String? = null,
    val name: String? = null,
    val available: Boolean? = null,
    val token: String? = null
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
