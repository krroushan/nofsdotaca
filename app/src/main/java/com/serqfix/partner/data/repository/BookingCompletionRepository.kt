package com.serqfix.partner.data.repository

import com.serqfix.partner.data.api.ApiModule
import com.serqfix.partner.data.api.BookingCompletionApiService
import com.serqfix.partner.data.api.ReachedOtpVerificationRequest
import com.serqfix.partner.data.api.SendCompletionOtpRequest
import com.serqfix.partner.data.api.CompleteBookingRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody

class BookingCompletionRepository {
    
    private val apiService: BookingCompletionApiService = ApiModule.createService()
    
    suspend fun verifyReachedOtp(
        bookingId: String,
        providerId: String,
        otp: String
    ): Result<com.serqfix.partner.data.api.BookingCompletionResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.verifyReachedOtp(bookingId, ReachedOtpVerificationRequest(bookingId, providerId, otp))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun sendCompletionOtp(bookingId: String, providerId: String): Result<com.serqfix.partner.data.api.BookingCompletionResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.sendCompletionOtp(SendCompletionOtpRequest(bookingId, providerId))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun completeBooking(
        bookingId: String,
        providerId: String,
        completionOtp: String
    ): Result<com.serqfix.partner.data.api.BookingCompletionResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.completeBooking(CompleteBookingRequest(bookingId, providerId, completionOtp))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun uploadProofOfWork(
        bookingId: String,
        providerId: String,
        imagePart: MultipartBody.Part
    ): Result<com.serqfix.partner.data.api.BookingCompletionResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.uploadProofOfWork(bookingId, providerId, imagePart)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
