package com.serqfix.partner.data.repository

import com.serqfix.partner.data.api.ApiModule
import com.serqfix.partner.data.api.BookingApiService
import com.serqfix.partner.data.api.AcceptBookingRequest
import com.serqfix.partner.data.api.RejectBookingRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BookingRepository {
    
    private val apiService: BookingApiService = ApiModule.createService()
    
    suspend fun getBookingById(bookingId: String): Result<com.serqfix.partner.data.api.BookingResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getBookingById(bookingId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getBookings(
        userId: String,
        page: Int = 1,
        limit: Int = 10,
        status: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ): Result<com.serqfix.partner.data.api.BookingsListResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getBookings(userId, page, limit, status, startDate, endDate)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun acceptBooking(bookingId: String, providerId: String, lat: Double? = null, lng: Double? = null): Result<com.serqfix.partner.data.api.BookingResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.acceptBooking(AcceptBookingRequest(bookingId, providerId, lat, lng))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun rejectBooking(bookingId: String, providerId: String): Result<com.serqfix.partner.data.api.BookingResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.rejectBooking(RejectBookingRequest(bookingId, providerId))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateBookingStatus(
        bookingId: String,
        providerId: String,
        statusType: String,
        value: Boolean = true,
        lat: Double? = null,
        lng: Double? = null
    ): Result<com.serqfix.partner.data.api.BookingResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateBookingStatus(
                com.serqfix.partner.data.api.UpdateBookingStatusRequest(
                    bookingId, providerId, statusType, value, lat, lng
                )
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun verifyReachedOtp(
        bookingId: String,
        otp: String,
        lat: Double? = null,
        lng: Double? = null
    ): Result<com.serqfix.partner.data.api.BookingResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.verifyReachedOtp(
                com.serqfix.partner.data.api.VerifyReachedOtpRequest(bookingId, otp, lat, lng)
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun sendCompletionOtp(
        bookingId: String,
        providerId: String
    ): Result<com.serqfix.partner.data.api.BookingResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.sendCompletionOtp(
                com.serqfix.partner.data.api.SendCompletionOtpRequest(bookingId, providerId)
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun verifyCompletionOtp(
        bookingId: String,
        otp: String,
        lat: Double? = null,
        lng: Double? = null
    ): Result<com.serqfix.partner.data.api.BookingResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.verifyCompletionOtp(
                bookingId,
                com.serqfix.partner.data.api.VerifyCompletionOtpRequest(bookingId, otp, lat, lng)
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
