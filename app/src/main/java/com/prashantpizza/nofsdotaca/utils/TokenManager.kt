package com.prashantpizza.nofsdotaca.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class TokenManager private constructor(context: Context) {
    
    companion object {
        private const val TAG = "TokenManager"
        private const val PREFS_NAME = "auth_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        
        @Volatile
        private var instance: TokenManager? = null
        
        fun getInstance(context: Context): TokenManager {
            return instance ?: synchronized(this) {
                instance ?: TokenManager(context.applicationContext).also { instance = it }
            }
        }
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    /**
     * Save access and refresh tokens
     */
    fun saveTokens(accessToken: String, refreshToken: String) {
        try {
            prefs.edit()
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .apply()
            Log.d(TAG, "Tokens saved successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving tokens: ${e.message}", e)
        }
    }
    
    /**
     * Get access token
     */
    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }
    
    /**
     * Get refresh token
     */
    fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }
    
    /**
     * Clear all tokens (logout)
     */
    fun clearTokens() {
        try {
            prefs.edit()
                .remove(KEY_ACCESS_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
                .apply()
            Log.d(TAG, "Tokens cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing tokens: ${e.message}", e)
        }
    }
    
    /**
     * Check if user is authenticated (has access token)
     */
    fun isAuthenticated(): Boolean {
        return getAccessToken() != null
    }
}
