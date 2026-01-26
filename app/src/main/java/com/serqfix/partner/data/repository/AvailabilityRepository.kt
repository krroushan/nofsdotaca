package com.serqfix.partner.data.repository

import com.serqfix.partner.data.api.ApiModule
import com.serqfix.partner.data.api.AvailabilityApiService
import com.serqfix.partner.data.api.UpdateAvailabilityRequest
import com.serqfix.partner.data.api.OnOffDutyRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AvailabilityRepository {
    
    private val apiService: AvailabilityApiService = ApiModule.createService()
    
    suspend fun toggleAvailability(providerId: String, available: Boolean): Result<com.serqfix.partner.data.api.AvailabilityResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.toggleAvailability(UpdateAvailabilityRequest(providerId, available))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAvailabilityStatus(providerId: String): Result<com.serqfix.partner.data.api.AvailabilityResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAvailabilityStatus(providerId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // On-Off Duty methods (matching React Native)
    suspend fun onOffDuty(providerId: String, isOnDuty: Boolean): Result<com.serqfix.partner.data.api.DutyStatusResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.onOffDuty(OnOffDutyRequest(providerId, isOnDuty))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getDutyStatus(providerId: String): Result<com.serqfix.partner.data.api.DutyStatusResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getDutyStatus(providerId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
