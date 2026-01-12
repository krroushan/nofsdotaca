package com.prashantpizza.nofsdotaca.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

// Request/Response data classes
data class LoginRequest(
    val phone: String,
    val password: String
)

data class RbacRoleResponse(
    val id: String,
    val slug: String,
    val name: String
)

data class UserResponse(
    val id: String,
    val name: String,
    val phone: String,
    val rbacRole: RbacRoleResponse?,
    val isPhoneVerified: Boolean,
    val createdAt: String
)

data class TokensResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val tokenType: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val user: UserResponse?,
    val tokens: TokensResponse?,
    val error: String?
)

// Retrofit API interface
interface AuthApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}

class AuthRepository {
    
    companion object {
        private const val TAG = "AuthRepository"
        private const val BASE_URL = "https://pos.prashantpizza.in/api/mobile/"
        
        @Volatile
        private var instance: AuthRepository? = null
        
        fun getInstance(): AuthRepository {
            return instance ?: synchronized(this) {
                instance ?: AuthRepository().also { instance = it }
            }
        }
    }
    
    private val apiService: AuthApiService by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
        
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApiService::class.java)
    }
    
    suspend fun login(phone: String, password: String): Result<LoginResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Attempting login for phone: ${phone.take(5)}...")
            
            val request = LoginRequest(phone, password)
            val response = apiService.login(request)
            
            if (response.success && response.tokens != null && response.user != null) {
                Log.d(TAG, "Login successful for user: ${response.user.name}")
                Result.success(response)
            } else {
                val errorMessage = response.error ?: "Login failed"
                Log.e(TAG, "Login failed: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during login: ${e.message}", e)
            Result.failure(e)
        }
    }
}
