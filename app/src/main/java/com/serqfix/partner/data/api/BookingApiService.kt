package com.serqfix.partner.data.api

import retrofit2.http.*

// Request models
data class AcceptBookingRequest(
    val bookingId: String,
    val providerId: String,
    val lat: Double? = null,
    val lng: Double? = null
)

data class RejectBookingRequest(
    val bookingId: String,
    val providerId: String
)

// Response models
data class BookingResponse(
    val success: Boolean,
    val message: String? = null,
    val data: BookingData? = null
)

data class CartItem(
    val _id: String? = null,
    val title: String? = null,
    val quantity: Int? = null,
    val icon: IconData? = null,
    val price: Double? = null
)

data class IconData(
    val url: String? = null
)

data class BookingData(
    val _id: String? = null,
    val bookingId: String? = null,
    val serviceName: String? = null,
    val customerName: String? = null,
    val customerPhone: String? = null,
    val customerLocation: String? = null,
    val serviceDate: String? = null,
    val serviceTime: String? = null,
    val date: String? = null,
    val time: String? = null,
    val status: String? = null,
    val amount: Double? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    // Additional fields for service details
    val cartItems: List<CartItem>? = null,
    val canceledByCustomer: Boolean? = null,
    val completed: Boolean? = null,
    val expired: Boolean? = null,
    val otpVerified: Boolean? = null,
    val reachedAtLocation: Boolean? = null,
    val outForService: Boolean? = null,
    val acceptedByServiceProvider: Boolean? = null,
    val location: LocationData? = null,
    val address: String? = null,
    val user: com.serqfix.partner.data.api.UserData? = null,
    val fullname: String? = null,
    val phoneNumber: String? = null
)

data class LocationData(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String? = null
)

data class BookingsListResponse(
    val success: Boolean,
    val message: String? = null,
    val data: List<BookingData>? = null,
    val meta: com.serqfix.partner.data.api.MetaData? = null
)

data class UpdateBookingStatusRequest(
    val bookingId: String,
    val providerId: String,
    val statusType: String,
    val value: Boolean = true,
    val lat: Double? = null,
    val lng: Double? = null
)

data class VerifyReachedOtpRequest(
    val bookingId: String,
    val otp: String,
    val lat: Double? = null,
    val lng: Double? = null
)

data class VerifyCompletionOtpRequest(
    val bookingId: String,
    val otp: String,
    val lat: Double? = null,
    val lng: Double? = null
)

interface BookingApiService {
    @GET("mobile-app/bookings/id/{bookingId}")
    suspend fun getBookingById(@Path("bookingId") bookingId: String): BookingResponse
    
    @GET("mobile-app/bookings/{userId}")
    suspend fun getBookings(
        @Path("userId") userId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("status") status: String? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): BookingsListResponse
    
    @POST("accept-booking")
    suspend fun acceptBooking(@Body request: AcceptBookingRequest): BookingResponse
    
    @POST("bookings/reject-booking")
    suspend fun rejectBooking(@Body request: RejectBookingRequest): BookingResponse
    
    @POST("mobile-app/mark-booking-status")
    suspend fun updateBookingStatus(@Body request: UpdateBookingStatusRequest): BookingResponse
    
    @POST("mobile-app/bookings/reaching-otp-verify")
    suspend fun verifyReachedOtp(@Body request: VerifyReachedOtpRequest): BookingResponse
    
    @POST("mobile-app/bookings/complete/send-otp")
    suspend fun sendCompletionOtp(@Body request: com.serqfix.partner.data.api.SendCompletionOtpRequest): BookingResponse
    
    @POST("mobile-app/bookings/complete/{bookingId}")
    suspend fun verifyCompletionOtp(
        @Path("bookingId") bookingId: String,
        @Body request: VerifyCompletionOtpRequest
    ): BookingResponse
}
