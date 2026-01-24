package com.serqfix.partner.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

sealed class AppError(
    open val message: String,
    val userFriendlyMessage: String
) {
    data class NetworkError(override val message: String) : AppError(
        message = message,
        userFriendlyMessage = "Network connection failed. Please check your internet connection and try again."
    )
    
    data class PermissionError(override val message: String) : AppError(
        message = message,
        userFriendlyMessage = "Permission denied. Please grant the required permission in settings."
    )
    
    data class LocationError(override val message: String) : AppError(
        message = message,
        userFriendlyMessage = "Location unavailable. Please enable location services and try again."
    )
    
    data class UploadError(override val message: String) : AppError(
        message = message,
        userFriendlyMessage = "Failed to upload. Please check your connection and try again."
    )
    
    data class ApiError(override val message: String, val code: Int? = null) : AppError(
        message = message,
        userFriendlyMessage = message
    )
    
    data class UnknownError(override val message: String) : AppError(
        message = message,
        userFriendlyMessage = "An unexpected error occurred. Please try again."
    )
}

object ErrorHandler {
    
    fun handleError(
        error: Throwable,
        context: Context? = null,
        snackbarHostState: SnackbarHostState? = null,
        scope: CoroutineScope? = null
    ): AppError {
        val appError = when {
            error is java.net.UnknownHostException || 
            error is java.net.SocketTimeoutException ||
            error is java.io.IOException -> {
                AppError.NetworkError(error.message ?: "Network error")
            }
            error is SecurityException -> {
                AppError.PermissionError(error.message ?: "Permission denied")
            }
            error.message?.contains("location", ignoreCase = true) == true ||
            error.message?.contains("Location", ignoreCase = true) == true -> {
                AppError.LocationError(error.message ?: "Location error")
            }
            else -> {
                AppError.UnknownError(error.message ?: "Unknown error")
            }
        }
        
        // Show error to user
        context?.let {
            Toast.makeText(it, appError.userFriendlyMessage, Toast.LENGTH_LONG).show()
        }
        
        snackbarHostState?.let { snackbar ->
            scope?.launch {
                snackbar.showSnackbar(appError.userFriendlyMessage)
            }
        }
        
        return appError
    }
    
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    fun checkNetworkAndThrow(context: Context) {
        if (!isNetworkAvailable(context)) {
            throw java.net.UnknownHostException("No internet connection")
        }
    }
}
