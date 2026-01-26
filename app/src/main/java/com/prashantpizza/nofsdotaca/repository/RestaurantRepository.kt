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
import java.util.concurrent.TimeUnit

// Restaurant Profile Data Models
data class RestaurantContact(
    val primaryPhone: String,
    val secondaryPhone: String?,
    val email: String
)

data class RestaurantManager(
    val name: String,
    val phone: String,
    val email: String
)

data class RestaurantAddress(
    val line1: String,
    val line2: String,
    val landmark: String,
    val city: String,
    val state: String,
    val pincode: String,
    val country: String,
    val lat: Double?,
    val lng: Double?
)

data class RestaurantLocation(
    val type: String,
    val coordinates: List<Double>
)

data class RestaurantOpeningHours(
    val openingTime: String,
    val closingTime: String
)

data class RestaurantProfile(
    val _id: String,
    val name: String,
    val branchCode: String,
    val primaryImage: String?,
    val images: List<String>,
    val gstNumber: String?,
    val contact: RestaurantContact,
    val manager: RestaurantManager,
    val address: RestaurantAddress,
    val location: RestaurantLocation?,
    val openingHours: RestaurantOpeningHours,
    val features: List<String>,
    val createdAt: String,
    val updatedAt: String
)

data class RestaurantProfileResponse(
    val success: Boolean,
    val data: RestaurantProfile?,
    val error: String?
)

// Retrofit API interface
interface RestaurantApiService {
    @GET("pos/restaurant/profile")
    suspend fun getProfile(): RestaurantProfileResponse
}

class RestaurantRepository private constructor(context: Context) {
    
    companion object {
        private const val TAG = "RestaurantRepository"
        private const val BASE_URL = "https://pos.prashantpizza.in/api/mobile/"
        
        @Volatile
        private var INSTANCE: RestaurantRepository? = null
        
        fun getInstance(context: Context): RestaurantRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RestaurantRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val tokenManager: TokenManager = TokenManager.getInstance(context)
    private val authErrorHandler: AuthErrorHandler = AuthErrorHandler.getInstance(context)
    
    private val apiService: RestaurantApiService by lazy {
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
            .create(RestaurantApiService::class.java)
    }
    
    suspend fun getProfile(): Result<RestaurantProfile> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching restaurant profile...")
            val response = apiService.getProfile()
            
            if (response.success && response.data != null) {
                Log.d(TAG, "Restaurant profile fetched successfully: ${response.data.name}")
                Result.success(response.data)
            } else {
                val errorMessage = response.error ?: "Failed to fetch restaurant profile"
                Log.e(TAG, "Failed to fetch restaurant profile: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching restaurant profile: ${e.message}", e)
            Result.failure(e)
        }
    }
}
