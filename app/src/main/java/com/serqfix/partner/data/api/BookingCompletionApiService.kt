package com.serqfix.partner.data.api

import okhttp3.MultipartBody
import retrofit2.http.*

// Request models
data class ReachedOtpVerificationRequest(
    val bookingId: String,
    val providerId: String,
    val otp: String
)

data class SendCompletionOtpRequest(
    val bookingId: String,
    val providerId: String
)

data class CompleteBookingRequest(
    val bookingId: String,
    val providerId: String,
    val completionOtp: String
)

// Response models
data class BookingCompletionResponse(
    val success: Boolean,
    val message: String? = null,
    val data: BookingData? = null
)

interface BookingCompletionApiService {
    @POST("mobile-app/bookings/{bookingId}/reached-otp")
    suspend fun verifyReachedOtp(
        @Path("bookingId") bookingId: String,
        @Body request: ReachedOtpVerificationRequest
    ): BookingCompletionResponse
    
    @POST("mobile-app/bookings/send-completion-otp")
    suspend fun sendCompletionOtp(@Body request: SendCompletionOtpRequest): BookingCompletionResponse
    
    @POST("mobile-app/bookings/complete")
    suspend fun completeBooking(@Body request: CompleteBookingRequest): BookingCompletionResponse
    
    @Multipart
    @POST("mobile-app/bookings/upload-proof")
    suspend fun uploadProofOfWork(
        @Part("bookingId") bookingId: String,
        @Part("providerId") providerId: String,
        @Part image: MultipartBody.Part
    ): BookingCompletionResponse
}
