package com.serqfix.partner.data.repository

import com.serqfix.partner.data.api.ApiModule
import com.serqfix.partner.data.api.AssetApiService
import com.serqfix.partner.data.api.AssetResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class AssetRepository {
    
    private val apiService: AssetApiService = ApiModule.createService()
    
    suspend fun getAssetAssignments(providerId: String): Result<AssetResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAssetAssignments(providerId)
            Result.success(response)
        } catch (e: HttpException) {
            // Handle 404 as empty list (endpoint might not exist or user has no assets)
            if (e.code() == 404) {
                Result.success(AssetResponse(success = true, data = emptyList(), message = "No assets found"))
            } else {
                Result.failure(e)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
