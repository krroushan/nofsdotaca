package com.serqfix.partner.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serqfix.partner.data.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WalletUiState(
    val isLoading: Boolean = false,
    val balance: Double = 0.0,
    val transactions: List<com.serqfix.partner.data.api.WalletTransactionData> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()
    
    fun getWalletData(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            walletRepository.getWalletData(userId)
                .onSuccess { response ->
                    if (response.success && response.data != null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            balance = response.data.balance ?: 0.0,
                            transactions = response.data.transactions ?: emptyList()
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = response.message ?: "Failed to fetch wallet data"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to fetch wallet data"
                    )
                }
        }
    }
    
    fun rechargeWallet(userId: String, amount: Double, paymentMethod: String, transactionId: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            walletRepository.rechargeWallet(userId, amount, paymentMethod, transactionId)
                .onSuccess { response ->
                    if (response.success) {
                        // Refresh wallet data
                        getWalletData(userId)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = response.message ?: "Failed to recharge wallet"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to recharge wallet"
                    )
                }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
