package com.serqfix.partner.data.api

import okhttp3.MultipartBody
import retrofit2.http.*

// Request models
data class UpdateProfileRequest(
    val name: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val address: String? = null
)

// Response models
data class ProfileResponse(
    val success: Boolean,
    val message: String? = null,
    val data: ProfileData? = null
)

data class ProfileData(
    val _id: String? = null,
    val id: String? = null,
    val name: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val profileImage: String? = null,
    val address: String? = null,
    val isVerified: Boolean? = null,
    val active: Boolean? = null,
    val available: Boolean? = null,
    val provider_payment_type: String? = null
)

interface ProfileApiService {
    @GET("mobile-app/profile/{providerId}")
    suspend fun getProfile(@Path("providerId") providerId: String): ProfileResponse
    
    @PUT("mobile-app/profile/{providerId}")
    suspend fun updateProfile(
        @Path("providerId") providerId: String,
        @Body request: UpdateProfileRequest
    ): ProfileResponse
    
    @Multipart
    @POST("mobile-app/profile/upload-image")
    suspend fun uploadProfileImage(
        @Part("providerId") providerId: String,
        @Part image: MultipartBody.Part
    ): ProfileResponse
}
