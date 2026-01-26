package com.prashantpizza.nofsdotaca.repository

import android.content.Context
import android.util.Log
import com.prashantpizza.nofsdotaca.utils.TokenManager
import com.prashantpizza.nofsdotaca.utils.AuthErrorHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// Payment data models
data class PaymentRecord(
    val _id: String,
    val orderNumber: String,
    val payment: PaymentInfo?,
    val customer: PaymentCustomer?,
    val orderType: String?,
    val source: String?,
    val subtotal: Double?,
    val tax: Double?,
    val total: Double?,
    val createdAt: String?,
    val updatedAt: String?,
    val restaurantDetails: PaymentRestaurant?
)

data class PaymentInfo(
    val status: String?,
    val method: String?,
    val amount: Double?,
    val transactionId: String?
)

data class PaymentCustomer(
    val name: String?,
    val phone: String?,
    val email: String?
)

data class PaymentRestaurant(
    val _id: String?,
    val name: String?,
    val branchCode: String?
)

data class PaymentsResponse(
    val success: Boolean,
    val data: List<PaymentRecord>?,
    val stats: PaymentsStats?,
    val pagination: PaymentsPagination?,
    val error: String?
)

data class PaymentsStats(
    val totalPayments: Int?,
    val totalAmount: Double?,
    val totalTax: Double?,
    val averagePayment: Double?,
    val paymentMethodCounts: List<String>?,
    val paymentStatusCounts: List<String>?,
    val methodTotals: Map<String, MethodTotal>?
)

data class MethodTotal(
    val count: Int?,
    val totalAmount: Double?
)

data class PaymentsPagination(
    val page: Int?,
    val limit: Int?,
    val total: Int?,
    val pages: Int?,
    val hasNext: Boolean?,
    val hasPrev: Boolean?
)

// Retrofit API interface
interface PaymentsApiService {
    @GET("pos/payments")
    suspend fun getPayments(
        @Query("paymentStatus") paymentStatus: String? = null,
        @Query("paymentMethod") paymentMethod: String? = null,
        @Query("orderType") orderType: String? = null,
        @Query("source") source: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("page") page: Int? = null,
        @Query("search") search: String? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): PaymentsResponse
}

class PaymentsRepository private constructor(context: Context) {
    
    companion object {
        private const val TAG = "PaymentsRepository"
        private const val BASE_URL = "https://pos.prashantpizza.in/api/mobile/"
        
        @Volatile
        private var instance: PaymentsRepository? = null
        
        fun getInstance(context: Context? = null): PaymentsRepository {
            return instance ?: synchronized(this) {
                if (context == null) {
                    throw IllegalStateException("Context is required for first initialization")
                }
                instance ?: PaymentsRepository(context.applicationContext).also { instance = it }
            }
        }
    }
    
    private val tokenManager: TokenManager = TokenManager.getInstance(context)
    private val authErrorHandler: AuthErrorHandler = AuthErrorHandler.getInstance(context)
    
    private val apiService: PaymentsApiService by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        // Auth interceptor to add Authorization header
        val authInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val accessToken = tokenManager.getAccessToken()
            
            val newRequest = if (accessToken != null) {
                originalRequest.newBuilder()
                    .header("Authorization", "Bearer $accessToken")
                    .build()
            } else {
                originalRequest
            }
            
            val response = chain.proceed(newRequest)
            
            // Handle 401 Unauthorized - token expired or invalid
            if (response.code == 401) {
                Log.w(TAG, "Received 401 Unauthorized - token expired or invalid")
                authErrorHandler.handleAuthError()
            }
            
            response
        }
        
        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
        
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PaymentsApiService::class.java)
    }
    
    /**
     * Get payments with optional filters
     */
    suspend fun getPayments(
        paymentStatus: String? = null,
        paymentMethod: String? = null,
        orderType: String? = null,
        source: String? = null,
        limit: Int = 50,
        page: Int = 1,
        search: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ): Result<PaymentsResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching payments: paymentStatus=$paymentStatus, page=$page, limit=$limit")
            
            val response = apiService.getPayments(
                paymentStatus = paymentStatus,
                paymentMethod = paymentMethod,
                orderType = orderType,
                source = source,
                limit = limit,
                page = page,
                search = search,
                startDate = startDate,
                endDate = endDate
            )
            
            if (response.success && response.data != null) {
                Log.d(TAG, "Payments fetched successfully: ${response.data.size} payments")
                Result.success(response)
            } else {
                val errorMessage = response.error ?: "Failed to fetch payments"
                Log.e(TAG, "Failed to fetch payments: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching payments: ${e.message}", e)
            Result.failure(e)
        }
    }
}
