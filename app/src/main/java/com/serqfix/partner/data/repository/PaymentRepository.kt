package com.serqfix.partner.data.repository

import com.serqfix.partner.data.api.ApiModule
import com.serqfix.partner.data.api.PaymentApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PaymentRepository {
    
    private val apiService: PaymentApiService = ApiModule.createService()
    
    suspend fun getPayments(
        userId: String,
        page: Int = 1,
        limit: Int = 10,
        status: String? = null
    ): Result<com.serqfix.partner.data.api.PaymentsListResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPayments(userId, page, limit, status)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getPaymentById(paymentId: String): Result<com.serqfix.partner.data.api.PaymentResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPaymentById(paymentId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
