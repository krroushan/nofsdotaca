package com.serqfix.partner.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serqfix.partner.data.api.UserData
import com.serqfix.partner.data.api.BookingData
import com.serqfix.partner.data.api.AMCOrderData
import com.serqfix.partner.data.api.AssetData
import com.serqfix.partner.data.api.AttendanceRecord
import com.serqfix.partner.data.local.UserPreferencesDataStore
import com.serqfix.partner.data.repository.BookingRepository
import com.serqfix.partner.data.repository.AvailabilityRepository
import com.serqfix.partner.data.repository.AttendanceRepository
import com.serqfix.partner.data.repository.AssetRepository
import com.serqfix.partner.data.repository.AMCRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val userData: UserData? = null,
    val bookings: List<BookingData> = emptyList(),
    val currentBooking: BookingData? = null,
    val isAvailable: Boolean = false,
    val isConnected: Boolean = true,
    val todayAttendance: AttendanceRecord? = null,
    val isWorkingDay: Boolean = true,
    val isHoliday: Boolean = false,
    val pendingAMCAssignments: List<AMCOrderData> = emptyList(),
    val assetNotifications: List<AssetData> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val availabilityRepository: AvailabilityRepository,
    private val attendanceRepository: AttendanceRepository,
    private val assetRepository: AssetRepository,
    private val amcRepository: AMCRepository,
    private val userPreferences: UserPreferencesDataStore
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadInitialData()
    }
    
    fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // Load user data
                val userDataJson = userPreferences.userData.first()
                val userData = if (userDataJson != null) {
                    com.google.gson.Gson().fromJson(userDataJson, UserData::class.java)
                } else {
                    null
                }
                
                _uiState.value = _uiState.value.copy(userData = userData)
                
                val userId = userData?.id ?: userData?._id
                if (userId == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "User ID not found"
                    )
                    return@launch
                }
                
                // Check duty status
                checkDutyStatus(userId)
                
                // Load bookings
                fetchBookings(userId)
                
                // Load current booking
                fetchCurrentBooking(userId)
                
                // Load attendance if salary-only
                if (userData?.provider_payment_type == "salary-only") {
                    loadTodayAttendance(userId)
                    checkWorkDayAndHoliday(userData)
                }
                
                // Load AMC assignments
                fetchAMCAssignments(userId)
                
                // Load asset assignments
                fetchAssetAssignments(userId)
                
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load data"
                )
            }
        }
    }
    
    fun refreshData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
            
            try {
                val userData = _uiState.value.userData
                val userId = userData?.id ?: userData?._id
                
                if (userId == null) {
                    // Reload user data first
                    val userDataJson = userPreferences.userData.first()
                    val loadedUserData = if (userDataJson != null) {
                        com.google.gson.Gson().fromJson(userDataJson, UserData::class.java)
                    } else {
                        null
                    }
                    
                    if (loadedUserData == null) {
                        _uiState.value = _uiState.value.copy(isRefreshing = false)
                        return@launch
                    }
                    
                    _uiState.value = _uiState.value.copy(userData = loadedUserData)
                    val newUserId = loadedUserData.id ?: loadedUserData._id
                    if (newUserId == null) {
                        _uiState.value = _uiState.value.copy(isRefreshing = false)
                        return@launch
                    }
                    
                    // Check duty status
                    checkDutyStatus(newUserId)
                    
                    // Load bookings
                    fetchBookings(newUserId)
                    
                    // Load current booking
                    fetchCurrentBooking(newUserId)
                    
                    // Load attendance if salary-only
                    if (loadedUserData.provider_payment_type == "salary-only") {
                        loadTodayAttendance(newUserId)
                        checkWorkDayAndHoliday(loadedUserData)
                    }
                    
                    // Load AMC assignments
                    fetchAMCAssignments(newUserId)
                    
                    // Load asset assignments
                    fetchAssetAssignments(newUserId)
                } else {
                    // Check duty status
                    checkDutyStatus(userId)
                    
                    // Load bookings
                    fetchBookings(userId)
                    
                    // Load current booking
                    fetchCurrentBooking(userId)
                    
                    // Load attendance if salary-only
                    if (userData?.provider_payment_type == "salary-only") {
                        loadTodayAttendance(userId)
                        checkWorkDayAndHoliday(userData)
                    }
                    
                    // Load AMC assignments
                    fetchAMCAssignments(userId)
                    
                    // Load asset assignments
                    fetchAssetAssignments(userId)
                }
                
                _uiState.value = _uiState.value.copy(isRefreshing = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    error = e.message ?: "Failed to refresh data"
                )
            }
        }
    }
    
    fun toggleAvailability() {
        viewModelScope.launch {
            try {
                val userData = _uiState.value.userData
                val userId = userData?.id ?: userData?._id
                
                if (userId == null) {
                    _uiState.value = _uiState.value.copy(error = "User ID not found")
                    return@launch
                }
                
                val newStatus = !_uiState.value.isAvailable
                
                availabilityRepository.onOffDuty(userId, newStatus)
                    .onSuccess { response ->
                        if (response.success) {
                            _uiState.value = _uiState.value.copy(isAvailable = newStatus)
                            // Save to local storage
                            userPreferences.setAvailable(newStatus)
                        } else {
                            _uiState.value = _uiState.value.copy(error = response.message ?: "Failed to update status")
                        }
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(error = error.message ?: "Failed to update status")
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to update status")
            }
        }
    }
    
    fun fetchBookings(userId: String) {
        viewModelScope.launch {
            try {
                bookingRepository.getBookings(userId, page = 1, limit = 10, status = "ongoing")
                    .onSuccess { response ->
                        if (response.success) {
                            val bookings = response.data ?: emptyList()
                            // Filter bookings: only show accepted, not completed, not canceled, not expired
                            val filteredBookings = bookings.filter { booking ->
                                booking.acceptedByServiceProvider == true &&
                                booking.completed != true &&
                                booking.canceledByCustomer != true &&
                                booking.expired != true &&
                                booking.status != "Cancelled by Admin"
                            }
                            _uiState.value = _uiState.value.copy(bookings = filteredBookings)
                        } else {
                            _uiState.value = _uiState.value.copy(error = response.message ?: "Failed to fetch bookings")
                        }
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(error = error.message ?: "Failed to fetch bookings")
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to fetch bookings")
            }
        }
    }
    
    fun fetchCurrentBooking(userId: String) {
        viewModelScope.launch {
            try {
                bookingRepository.getCurrentBooking(userId)
                    .onSuccess { response ->
                        if (response.success) {
                            _uiState.value = _uiState.value.copy(currentBooking = response.data)
                        } else {
                            _uiState.value = _uiState.value.copy(currentBooking = null)
                        }
                    }
                    .onFailure {
                        _uiState.value = _uiState.value.copy(currentBooking = null)
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(currentBooking = null)
            }
        }
    }
    
    fun checkDutyStatus(userId: String) {
        viewModelScope.launch {
            try {
                // First check local storage
                val localAvailable = userPreferences.available.first()
                _uiState.value = _uiState.value.copy(isAvailable = localAvailable)
                
                // Then check API
                availabilityRepository.getDutyStatus(userId)
                    .onSuccess { response ->
                        if (response.success) {
                            val isOnDuty = response.data?.isOnDuty ?: false
                            _uiState.value = _uiState.value.copy(isAvailable = isOnDuty)
                            userPreferences.setAvailable(isOnDuty)
                        }
                    }
                    .onFailure {
                        // Keep local value if API fails
                    }
            } catch (e: Exception) {
                // Keep local value if error
            }
        }
    }
    
    fun loadTodayAttendance(providerId: String) {
        viewModelScope.launch {
            try {
                val today = Date()
                val startDate = Date(today.time - 24 * 60 * 60 * 1000) // Yesterday
                
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                dateFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
                
                attendanceRepository.fetchProviderAttendance(
                    providerId,
                    startDate = dateFormat.format(startDate),
                    endDate = dateFormat.format(today)
                )
                    .onSuccess { response ->
                        if (response.success && response.data != null) {
                            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(today)
                            val todayRecord = response.data.find { record ->
                                record.date?.startsWith(todayStr) == true
                            }
                            _uiState.value = _uiState.value.copy(todayAttendance = todayRecord)
                        }
                    }
                    .onFailure {
                        // Silently fail - attendance is optional
                    }
            } catch (e: Exception) {
                // Silently fail - attendance is optional
            }
        }
    }
    
    fun punchAttendance(action: String, location: com.serqfix.partner.data.api.LocationData? = null) {
        viewModelScope.launch {
            try {
                val userData = _uiState.value.userData
                val providerId = userData?.id ?: userData?._id
                
                if (providerId == null) {
                    _uiState.value = _uiState.value.copy(error = "User ID not found")
                    return@launch
                }
                
                // Frontend safety checks
                val todayAttendance = _uiState.value.todayAttendance
                if (action == "login" && todayAttendance?.login_time != null) {
                    _uiState.value = _uiState.value.copy(error = "Already punched in for today")
                    return@launch
                }
                if (action == "logout") {
                    if (todayAttendance?.login_time == null) {
                        _uiState.value = _uiState.value.copy(error = "Please punch in first")
                        return@launch
                    }
                    if (todayAttendance.logout_time != null) {
                        _uiState.value = _uiState.value.copy(error = "Already punched out for today")
                        return@launch
                    }
                }
                
                attendanceRepository.punchAttendance(providerId, action, location)
                    .onSuccess { response ->
                        if (response.success) {
                            // Reload today's attendance
                            loadTodayAttendance(providerId)
                        } else {
                            _uiState.value = _uiState.value.copy(error = response.message ?: "Failed to update attendance")
                        }
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(error = error.message ?: "Failed to update attendance")
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to update attendance")
            }
        }
    }
    
    fun checkWorkDayAndHoliday(userData: UserData) {
        viewModelScope.launch {
            try {
                val today = Date()
                val dayName = SimpleDateFormat("EEEE", Locale.US).format(today)
                
                val workDays = userData.work_days ?: emptyList()
                val isTodayWorkDay = workDays.isEmpty() || workDays.contains(dayName)
                
                _uiState.value = _uiState.value.copy(isWorkingDay = isTodayWorkDay)
                
                // Check if today is a holiday
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                dateFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
                val dateStr = dateFormat.format(today)
                
                attendanceRepository.checkIsHoliday(dateStr)
                    .onSuccess { response ->
                        if (response.success) {
                            _uiState.value = _uiState.value.copy(isHoliday = response.isHoliday ?: false)
                        }
                    }
                    .onFailure {
                        // Default to not holiday on error
                        _uiState.value = _uiState.value.copy(isHoliday = false)
                    }
            } catch (e: Exception) {
                // Default to working day on error
                _uiState.value = _uiState.value.copy(
                    isWorkingDay = true,
                    isHoliday = false
                )
            }
        }
    }
    
    fun fetchAMCAssignments(userId: String) {
        viewModelScope.launch {
            try {
                amcRepository.getPendingAMCAssignments(userId)
                    .onSuccess { response ->
                        if (response.success) {
                            _uiState.value = _uiState.value.copy(
                                pendingAMCAssignments = response.data ?: emptyList()
                            )
                        }
                    }
                    .onFailure {
                        // Silently fail - AMC assignments are optional
                    }
            } catch (e: Exception) {
                // Silently fail - AMC assignments are optional
            }
        }
    }
    
    fun handleAMCAccept(assignmentId: String) {
        viewModelScope.launch {
            try {
                val userData = _uiState.value.userData
                val userId = userData?.id ?: userData?._id
                
                if (userId == null) {
                    _uiState.value = _uiState.value.copy(error = "User ID not found")
                    return@launch
                }
                
                amcRepository.acceptAMCAssignment(assignmentId, userId)
                    .onSuccess { response ->
                        if (response.success) {
                            // Remove from pending assignments
                            _uiState.value = _uiState.value.copy(
                                pendingAMCAssignments = _uiState.value.pendingAMCAssignments.filter { it._id != assignmentId }
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(error = response.message ?: "Failed to accept assignment")
                        }
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(error = error.message ?: "Failed to accept assignment")
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to accept assignment")
            }
        }
    }
    
    fun handleAMCReject(assignmentId: String) {
        viewModelScope.launch {
            try {
                val userData = _uiState.value.userData
                val userId = userData?.id ?: userData?._id
                
                if (userId == null) {
                    _uiState.value = _uiState.value.copy(error = "User ID not found")
                    return@launch
                }
                
                amcRepository.rejectAMCAssignment(assignmentId, userId, "Not interested")
                    .onSuccess { response ->
                        if (response.success) {
                            // Remove from pending assignments
                            _uiState.value = _uiState.value.copy(
                                pendingAMCAssignments = _uiState.value.pendingAMCAssignments.filter { it._id != assignmentId }
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(error = response.message ?: "Failed to reject assignment")
                        }
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(error = error.message ?: "Failed to reject assignment")
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to reject assignment")
            }
        }
    }
    
    fun fetchAssetAssignments(userId: String) {
        viewModelScope.launch {
            try {
                assetRepository.getAssetAssignments(userId)
                    .onSuccess { response ->
                        if (response.success) {
                            _uiState.value = _uiState.value.copy(
                                assetNotifications = response.data ?: emptyList()
                            )
                        }
                    }
                    .onFailure {
                        // Silently fail - asset assignments are optional
                    }
            } catch (e: Exception) {
                // Silently fail - asset assignments are optional
            }
        }
    }
    
    fun handleAssetDismiss(assetId: String) {
        _uiState.value = _uiState.value.copy(
            assetNotifications = _uiState.value.assetNotifications.filter { it._id != assetId }
        )
    }
    
    fun handleBookingAccepted(bookingId: String) {
        // Remove current booking after acceptance
        _uiState.value = _uiState.value.copy(currentBooking = null)
        // Refresh bookings list
        val userId = _uiState.value.userData?.id ?: _uiState.value.userData?._id
        userId?.let { fetchBookings(it) }
    }
    
    fun setNetworkConnected(connected: Boolean) {
        _uiState.value = _uiState.value.copy(isConnected = connected)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
