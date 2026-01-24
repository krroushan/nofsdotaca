package com.serqfix.partner.data.repository

import com.serqfix.partner.data.api.ApiModule
import com.serqfix.partner.data.api.ProfileApiService
import com.serqfix.partner.data.api.UpdateProfileRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody

class ProfileRepository {
    
    private val apiService: ProfileApiService = ApiModule.createService()
    
    suspend fun getProfile(providerId: String): Result<com.serqfix.partner.data.api.ProfileResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getProfile(providerId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateProfile(
        providerId: String,
        name: String? = null,
        email: String? = null,
        phoneNumber: String? = null,
        address: String? = null
    ): Result<com.serqfix.partner.data.api.ProfileResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateProfile(
                providerId,
                UpdateProfileRequest(name, email, phoneNumber, address)
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun uploadProfileImage(providerId: String, imagePart: MultipartBody.Part): Result<com.serqfix.partner.data.api.ProfileResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.uploadProfileImage(providerId, imagePart)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
