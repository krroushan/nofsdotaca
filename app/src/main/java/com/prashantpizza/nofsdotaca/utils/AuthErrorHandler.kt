package com.prashantpizza.nofsdotaca.utils

import android.content.Context
import android.util.Log

/**
 * Global authentication error handler
 * Handles 401 Unauthorized responses by clearing tokens and triggering logout
 */
class AuthErrorHandler private constructor(context: Context) {
    
    companion object {
        private const val TAG = "AuthErrorHandler"
        
        @Volatile
        private var instance: AuthErrorHandler? = null
        
        fun getInstance(context: Context): AuthErrorHandler {
            return instance ?: synchronized(this) {
                instance ?: AuthErrorHandler(context.applicationContext).also { instance = it }
            }
        }
    }
    
    private val tokenManager: TokenManager = TokenManager.getInstance(context)
    private var onLogoutCallback: (() -> Unit)? = null
    
    /**
     * Set callback to be invoked when authentication error occurs
     * This should be set in MainActivity to handle logout
     */
    fun setLogoutCallback(callback: () -> Unit) {
        onLogoutCallback = callback
    }
    
    /**
     * Handle authentication error (401 Unauthorized)
     * Clears tokens and triggers logout callback
     */
    fun handleAuthError() {
        Log.w(TAG, "Authentication error detected - clearing tokens and logging out")
        
        // Clear tokens
        tokenManager.clearTokens()
        
        // Trigger logout callback if set
        onLogoutCallback?.invoke()
    }
    
    /**
     * Check if error is an authentication error (401)
     */
    fun isAuthError(statusCode: Int): Boolean {
        return statusCode == 401
    }
}
