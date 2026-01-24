package com.serqfix.partner.data.repository

import com.serqfix.partner.data.api.ApiModule
import com.serqfix.partner.data.api.WalletApiService
import com.serqfix.partner.data.api.WalletRechargeRequest
import com.serqfix.partner.data.api.WalletPayoutRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WalletRepository {
    
    private val apiService: WalletApiService = ApiModule.createService()
    
    suspend fun getWalletData(
        userId: String,
        page: Int? = null,
        limit: Int? = null,
        type: String? = null,
        category: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ): Result<com.serqfix.partner.data.api.WalletResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getWalletData(userId, page, limit, type, category, startDate, endDate)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getBalance(userId: String): Result<com.serqfix.partner.data.api.WalletResponse> = withContext(Dispatchers.IO) {
        try {
            val request = mapOf("action" to "get_balance", "userId" to userId)
            val response = apiService.getBalance(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun rechargeWallet(
        userId: String,
        amount: Double,
        paymentMethod: String,
        transactionId: String? = null
    ): Result<com.serqfix.partner.data.api.WalletResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.rechargeWallet(WalletRechargeRequest(userId, amount, paymentMethod, transactionId))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun requestPayout(
        userId: String,
        amount: Double,
        accountDetails: Map<String, String>? = null
    ): Result<com.serqfix.partner.data.api.WalletResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.requestPayout(WalletPayoutRequest(userId, amount, accountDetails))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
