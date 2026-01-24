package com.serqfix.partner.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serqfix.partner.data.local.UserPreferencesDataStore
import com.serqfix.partner.data.repository.AMCRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AMCUiState(
    val isLoading: Boolean = false,
    val orders: List<com.serqfix.partner.data.api.AMCOrderData> = emptyList(),
    val currentOrder: com.serqfix.partner.data.api.AMCOrderData? = null,
    val dashboardData: com.serqfix.partner.data.api.AMCDashboardData? = null,
    val error: String? = null
)

@HiltViewModel
class AMCViewModel @Inject constructor(
    private val amcRepository: AMCRepository,
    private val userPreferences: UserPreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AMCUiState())
    val uiState: StateFlow<AMCUiState> = _uiState.asStateFlow()

    fun getAssignedAMCOrders(page: Int = 1, limit: Int = 10, includeAll: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val userId = userPreferences.userId.first()
            if (userId != null) {
                amcRepository.getAssignedAMCOrders(userId, page, limit, includeAll)
                    .onSuccess { response ->
                        if (response.success) {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                orders = response.data ?: emptyList()
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = response.message ?: "Failed to fetch AMC orders"
                            )
                        }
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to fetch AMC orders"
                        )
                    }
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "User ID not found"
                )
            }
        }
    }

    fun getAMCOrderById(orderId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            amcRepository.getAMCOrderById(orderId)
                .onSuccess { response ->
                    if (response.success && response.data != null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            currentOrder = response.data
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = response.message ?: "Failed to fetch AMC order"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to fetch AMC order"
                    )
                }
        }
    }

    fun acceptAMCAssignment(orderId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val providerId = userPreferences.userId.first()
            if (providerId != null) {
                amcRepository.acceptAMCAssignment(orderId, providerId)
                    .onSuccess { response ->
                        if (response.success) {
                            // Refresh orders list
                            getAssignedAMCOrders()
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = response.message ?: "Failed to accept AMC assignment"
                            )
                        }
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to accept AMC assignment"
                        )
                    }
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "User ID not found"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
