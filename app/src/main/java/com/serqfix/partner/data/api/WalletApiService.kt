package com.serqfix.partner.data.api

import retrofit2.http.*

// Request models
data class WalletRechargeRequest(
    val userId: String,
    val amount: Double,
    val paymentMethod: String, // "cash" or "online"
    val transactionId: String? = null
)

data class WalletPayoutRequest(
    val userId: String,
    val amount: Double,
    val accountDetails: Map<String, String>? = null
)

// Response models
data class WalletResponse(
    val success: Boolean,
    val message: String? = null,
    val data: WalletData? = null
)

data class WalletData(
    val balance: Double? = null,
    val transactions: List<WalletTransactionData>? = null,
    val pendingTransactions: List<WalletTransactionData>? = null
)

data class WalletTransactionData(
    val _id: String? = null,
    val transactionId: String? = null,
    val type: String? = null, // "credit" or "debit"
    val amount: Double? = null,
    val status: String? = null, // "pending", "completed", "failed"
    val description: String? = null,
    val createdAt: String? = null
)

data class WalletTransactionsListResponse(
    val success: Boolean,
    val message: String? = null,
    val data: List<WalletTransactionData>? = null,
    val meta: MetaData? = null
)

interface WalletApiService {
    @GET("mobile-app/wallet")
    suspend fun getWalletData(
        @Query("userId") userId: String,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("type") type: String? = null,
        @Query("category") category: String? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): WalletResponse
    
    @POST("mobile-app/wallet")
    suspend fun getBalance(@Body request: Map<String, String>): WalletResponse
    
    @POST("mobile-app/wallet/recharge")
    suspend fun rechargeWallet(@Body request: WalletRechargeRequest): WalletResponse
    
    @POST("mobile-app/wallet/payout")
    suspend fun requestPayout(@Body request: WalletPayoutRequest): WalletResponse
}
