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
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

// Request/Response data classes
data class FcmTokenRequest(
    val fcmToken: String,
    val platform: String = "android"
)

data class FcmTokenResponse(
    val success: Boolean,
    val message: String?,
    val error: String?,
    val data: FcmTokenData?
)

data class FcmTokenData(
    val message: String
)

// Retrofit API interface
interface FcmTokenApiService {
    @POST("fcm-token")
    suspend fun saveToken(@Body request: FcmTokenRequest): FcmTokenResponse
    
    @DELETE("fcm-token")
    suspend fun removeToken(): FcmTokenResponse
}

class FcmTokenRepository private constructor(context: Context) {
    
    companion object {
        private const val TAG = "FcmTokenRepository"
        private const val BASE_URL = "https://pos.prashantpizza.in/api/mobile/"
        
        @Volatile
        private var instance: FcmTokenRepository? = null
        
        fun getInstance(context: Context? = null): FcmTokenRepository {
            return instance ?: synchronized(this) {
                if (context == null) {
                    throw IllegalStateException("Context is required for first initialization")
                }
                instance ?: FcmTokenRepository(context.applicationContext).also { instance = it }
            }
        }
    }
    
    private val tokenManager: TokenManager = TokenManager.getInstance(context)
    private val authErrorHandler: AuthErrorHandler = AuthErrorHandler.getInstance(context)
    
    private val apiService: FcmTokenApiService by lazy {
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
            .create(FcmTokenApiService::class.java)
    }
    
    /**
     * Save or update FCM token for authenticated user
     */
    suspend fun saveToken(fcmToken: String, platform: String = "android"): Result<FcmTokenResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Saving FCM token: ${fcmToken.take(20)}...")
            
            val request = FcmTokenRequest(fcmToken, platform)
            val response = apiService.saveToken(request)
            
            if (response.success) {
                Log.d(TAG, "FCM token saved successfully: ${response.message}")
                Result.success(response)
            } else {
                val errorMessage = response.error ?: "Failed to save FCM token"
                Log.e(TAG, "Failed to save FCM token: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error saving FCM token: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Remove FCM token (on logout)
     */
    suspend fun removeToken(): Result<FcmTokenResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Removing FCM token")
            
            val response = apiService.removeToken()
            
            if (response.success) {
                Log.d(TAG, "FCM token removed successfully: ${response.message}")
                Result.success(response)
            } else {
                val errorMessage = response.error ?: "Failed to remove FCM token"
                Log.e(TAG, "Failed to remove FCM token: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error removing FCM token: ${e.message}", e)
            Result.failure(e)
        }
    }
}
