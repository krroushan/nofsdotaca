package com.serqfix.partner.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serqfix.partner.data.repository.BookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BookingUiState(
    val isLoading: Boolean = false,
    val bookings: List<com.serqfix.partner.data.api.BookingData> = emptyList(),
    val currentBooking: com.serqfix.partner.data.api.BookingData? = null,
    val error: String? = null,
    val page: Int = 1,
    val hasMore: Boolean = true
)

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()
    
    fun getBookingById(bookingId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            bookingRepository.getBookingById(bookingId)
                .onSuccess { response ->
                    if (response.success && response.data != null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            currentBooking = response.data
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = response.message ?: "Failed to fetch booking"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to fetch booking"
                    )
                }
        }
    }
    
    fun getBookings(
        userId: String,
        page: Int = 1,
        limit: Int = 10,
        status: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        loadMore: Boolean = false
    ) {
        viewModelScope.launch {
            if (!loadMore) {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null, page = page)
            }
            
            bookingRepository.getBookings(userId, page, limit, status, startDate, endDate)
                .onSuccess { response ->
                    if (response.success) {
                        val newBookings = response.data ?: emptyList()
                        val updatedBookings = if (loadMore) {
                            _uiState.value.bookings + newBookings
                        } else {
                            newBookings
                        }
                        
                        val hasMore = response.meta?.let {
                            (it.currentPage ?: 1) < (it.totalPages ?: 1)
                        } ?: false
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            bookings = updatedBookings,
                            page = page,
                            hasMore = hasMore
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = response.message ?: "Failed to fetch bookings"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to fetch bookings"
                    )
                }
        }
    }
    
    fun acceptBooking(bookingId: String, providerId: String, lat: Double? = null, lng: Double? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            bookingRepository.acceptBooking(bookingId, providerId, lat, lng)
                .onSuccess { response ->
                    if (response.success) {
                        // Update the booking in the list
                        val updatedBookings = _uiState.value.bookings.map { booking ->
                            if (booking._id == bookingId || booking.bookingId == bookingId) {
                                response.data ?: booking
                            } else {
                                booking
                            }
                        }
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            bookings = updatedBookings,
                            currentBooking = response.data ?: _uiState.value.currentBooking
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = response.message ?: "Failed to accept booking"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to accept booking"
                    )
                }
        }
    }
    
    fun rejectBooking(bookingId: String, providerId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            bookingRepository.rejectBooking(bookingId, providerId)
                .onSuccess { response ->
                    if (response.success) {
                        // Remove the booking from the list
                        val updatedBookings = _uiState.value.bookings.filter { booking ->
                            booking._id != bookingId && booking.bookingId != bookingId
                        }
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            bookings = updatedBookings,
                            currentBooking = null
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = response.message ?: "Failed to reject booking"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to reject booking"
                    )
                }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearCurrentBooking() {
        _uiState.value = _uiState.value.copy(currentBooking = null)
    }
    
    fun updateBookingStatus(
        bookingId: String,
        providerId: String,
        statusType: String,
        value: Boolean = true,
        lat: Double? = null,
        lng: Double? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            bookingRepository.updateBookingStatus(bookingId, providerId, statusType, value, lat, lng)
                .onSuccess { response ->
                    if (response.success) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            currentBooking = response.data ?: _uiState.value.currentBooking
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = response.message ?: "Failed to update status"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to update status"
                    )
                }
        }
    }
    
    fun verifyReachedOtp(
        bookingId: String,
        otp: String,
        lat: Double? = null,
        lng: Double? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            bookingRepository.verifyReachedOtp(bookingId, otp, lat, lng)
                .onSuccess { response ->
                    if (response.success) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            currentBooking = response.data ?: _uiState.value.currentBooking
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = response.message ?: "Failed to verify OTP"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to verify OTP"
                    )
                }
        }
    }
    
    fun sendCompletionOtp(
        bookingId: String,
        providerId: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            bookingRepository.sendCompletionOtp(bookingId, providerId)
                .onSuccess { response ->
                    if (response.success) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            currentBooking = response.data ?: _uiState.value.currentBooking
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = response.message ?: "Failed to send completion OTP"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to send completion OTP"
                    )
                }
        }
    }
    
    fun verifyCompletionOtp(
        bookingId: String,
        otp: String,
        lat: Double? = null,
        lng: Double? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            bookingRepository.verifyCompletionOtp(bookingId, otp, lat, lng)
                .onSuccess { response ->
                    if (response.success) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            currentBooking = response.data ?: _uiState.value.currentBooking
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = response.message ?: "Failed to verify completion OTP"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to verify completion OTP"
                    )
                }
        }
    }
}
