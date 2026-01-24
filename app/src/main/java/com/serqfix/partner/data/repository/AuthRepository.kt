package com.serqfix.partner.data.repository

import com.serqfix.partner.data.api.ApiModule
import com.serqfix.partner.data.api.AuthApiService
import com.serqfix.partner.data.api.RequestOtpRequest
import com.serqfix.partner.data.api.VerifyOtpRequest
import com.serqfix.partner.data.api.PhonePasswordLoginRequest
import com.serqfix.partner.data.api.EmailPasswordLoginRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository {
    
    private val apiService: AuthApiService = ApiModule.createService()
    
    suspend fun requestPhoneOtp(phoneNumber: String): Result<com.serqfix.partner.data.api.AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.requestPhoneOtp(RequestOtpRequest(phoneNumber))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun verifyPhoneOtp(phoneNumber: String, otp: String): Result<com.serqfix.partner.data.api.AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.verifyPhoneOtp(VerifyOtpRequest(phoneNumber, "phone-otp", otp))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun loginWithPhonePassword(phoneNumber: String, password: String, otp: String? = null): Result<com.serqfix.partner.data.api.AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.loginWithPhonePassword(PhonePasswordLoginRequest(phoneNumber, "phone-password", password, otp))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun loginWithEmailPassword(email: String, password: String): Result<com.serqfix.partner.data.api.AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.loginWithEmailPassword(EmailPasswordLoginRequest(email, "email-password", password))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
