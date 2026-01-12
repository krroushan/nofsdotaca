package com.prashantpizza.nofsdotaca.repository

import android.content.Context
import android.util.Log
import com.prashantpizza.nofsdotaca.model.OrderNotification
import com.prashantpizza.nofsdotaca.utils.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

// API Response models
data class OrderActionRequest(
    val orderId: String,
    val action: String, // "accept" or "reject"
    val timestamp: Long = System.currentTimeMillis()
)

data class OrderActionResponse(
    val success: Boolean,
    val message: String
)

// Retrofit API interface
interface OrderApiService {
    @POST("api/orders/action")
    suspend fun updateOrderStatus(@Body request: OrderActionRequest): OrderActionResponse
}

class OrderRepository private constructor(context: Context) {
    
    companion object {
        private const val TAG = "OrderRepository"
        private const val BASE_URL = "https://your-api-endpoint.com/" // Replace with actual endpoint
        
        @Volatile
        private var instance: OrderRepository? = null
        
        fun getInstance(context: Context? = null): OrderRepository {
            return instance ?: synchronized(this) {
                if (context == null) {
                    throw IllegalStateException("Context is required for first initialization")
                }
                instance ?: OrderRepository(context.applicationContext).also { instance = it }
            }
        }
    }
    
    private val tokenManager: TokenManager = TokenManager.getInstance(context)
    
    private val apiService: OrderApiService by lazy {
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
                Log.w(TAG, "Received 401 Unauthorized - token may be expired")
                // Token will be cleared on next login attempt
                // Could trigger logout flow here if needed
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
            .create(OrderApiService::class.java)
    }
    
    suspend fun acceptOrder(order: OrderNotification): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Accepting order: ${order.orderId}")
            
            // TODO: Replace with actual API call when backend is ready
            // val response = apiService.updateOrderStatus(
            //     OrderActionRequest(order.orderId, "accept")
            // )
            
            // Simulate API call for now
            kotlinx.coroutines.delay(500)
            
            // Dummy success response
            Log.d(TAG, "Order ${order.orderId} accepted successfully")
            Result.success("Order accepted successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error accepting order: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun rejectOrder(order: OrderNotification): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Rejecting order: ${order.orderId}")
            
            // TODO: Replace with actual API call when backend is ready
            // val response = apiService.updateOrderStatus(
            //     OrderActionRequest(order.orderId, "reject")
            // )
            
            // Simulate API call for now
            kotlinx.coroutines.delay(500)
            
            // Dummy success response
            Log.d(TAG, "Order ${order.orderId} rejected successfully")
            Result.success("Order rejected successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error rejecting order: ${e.message}", e)
            Result.failure(e)
        }
    }
}
