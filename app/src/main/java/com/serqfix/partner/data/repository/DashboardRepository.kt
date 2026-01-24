package com.serqfix.partner.data.repository

import com.serqfix.partner.data.api.ApiModule
import com.serqfix.partner.data.api.DashboardApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DashboardRepository {
    
    private val apiService: DashboardApiService = ApiModule.createService()
    
    suspend fun getDashboardData(providerId: String): Result<com.serqfix.partner.data.api.DashboardResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getDashboardData(providerId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
