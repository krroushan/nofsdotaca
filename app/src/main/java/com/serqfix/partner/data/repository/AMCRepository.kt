package com.serqfix.partner.data.repository

import com.serqfix.partner.data.api.ApiModule
import com.serqfix.partner.data.api.AMCApiService
import com.serqfix.partner.data.api.AcceptAMCAssignmentRequest
import com.serqfix.partner.data.api.RejectAMCAssignmentRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AMCRepository {
    
    private val apiService: AMCApiService = ApiModule.createService()
    
    suspend fun getCurrentAMCOrder(providerId: String): Result<com.serqfix.partner.data.api.AMCOrderResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getCurrentAMCOrder(providerId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAMCDashboard(providerId: String): Result<com.serqfix.partner.data.api.AMCDashboardResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAMCDashboard(providerId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAssignedAMCOrders(
        providerId: String,
        page: Int = 1,
        limit: Int = 10,
        includeAll: Boolean = false
    ): Result<com.serqfix.partner.data.api.AMCOrdersListResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAssignedAMCOrders(providerId, page, limit, includeAll)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAMCOrderById(orderId: String): Result<com.serqfix.partner.data.api.AMCOrderResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAMCOrderById(orderId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun acceptAMCAssignment(orderId: String, providerId: String): Result<com.serqfix.partner.data.api.AMCOrderResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.acceptAMCAssignment(AcceptAMCAssignmentRequest(orderId, providerId))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun rejectAMCAssignment(orderId: String, providerId: String, reason: String? = null): Result<com.serqfix.partner.data.api.AMCOrderResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.rejectAMCAssignment(RejectAMCAssignmentRequest(orderId, providerId, reason))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
