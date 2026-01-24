package com.serqfix.partner.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serqfix.partner.data.local.UserPreferencesDataStore
import com.serqfix.partner.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null,
    val userData: com.serqfix.partner.data.api.UserData? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPreferences: UserPreferencesDataStore
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    init {
        // Check if user is already logged in
        viewModelScope.launch {
            userPreferences.isLoggedIn.collect { isLoggedIn ->
                _uiState.value = _uiState.value.copy(isLoggedIn = isLoggedIn)
            }
        }
    }
    
    fun requestPhoneOtp(phoneNumber: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            authRepository.requestPhoneOtp(phoneNumber)
                .onSuccess { response ->
                    if (response.success) {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = response.message ?: "Failed to request OTP"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to request OTP"
                    )
                }
        }
    }
    
    fun verifyPhoneOtp(phoneNumber: String, otp: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            authRepository.verifyPhoneOtp(phoneNumber, otp)
                .onSuccess { response ->
                    if (response.success && response.data != null) {
                        // Store user data
                        response.data.let { userData ->
                            userPreferences.setUserData(com.google.gson.Gson().toJson(userData))
                            userPreferences.setToken(response.token ?: userData.token ?: "")
                            userPreferences.setIsLoggedIn(true)
                            userPreferences.setUserStatus(true)
                            userData.available?.let { userPreferences.setAvailable(it) }
                            userData._id?.let { userPreferences.setUserId(it) }
                        }
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            userData = response.data
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
    
    fun loginWithPhonePassword(phoneNumber: String, password: String, otp: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            authRepository.loginWithPhonePassword(phoneNumber, password, otp)
                .onSuccess { response ->
                    if (response.success && response.data != null) {
                        // Store user data
                        response.data.let { userData ->
                            userPreferences.setUserData(com.google.gson.Gson().toJson(userData))
                            userPreferences.setToken(response.token ?: userData.token ?: "")
                            userPreferences.setIsLoggedIn(true)
                            userPreferences.setUserStatus(true)
                            userData.available?.let { userPreferences.setAvailable(it) }
                            userData._id?.let { userPreferences.setUserId(it) }
                        }
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            userData = response.data
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = response.message ?: "Login failed"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Login failed"
                    )
                }
        }
    }
    
    fun loginWithEmailPassword(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            authRepository.loginWithEmailPassword(email, password)
                .onSuccess { response ->
                    if (response.success && response.data != null) {
                        // Store user data
                        response.data.let { userData ->
                            userPreferences.setUserData(com.google.gson.Gson().toJson(userData))
                            userPreferences.setToken(response.token ?: userData.token ?: "")
                            userPreferences.setIsLoggedIn(true)
                            userPreferences.setUserStatus(true)
                            userData.available?.let { userPreferences.setAvailable(it) }
                            userData._id?.let { userPreferences.setUserId(it) }
                        }
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            userData = response.data
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = response.message ?: "Login failed"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Login failed"
                    )
                }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            userPreferences.clearAll()
            _uiState.value = AuthUiState()
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
