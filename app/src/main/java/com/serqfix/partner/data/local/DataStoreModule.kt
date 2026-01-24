package com.serqfix.partner.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "partner_app_preferences")

object DataStoreKeys {
    val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    val USER_STATUS = booleanPreferencesKey("user_status")
    val USER_DATA = stringPreferencesKey("user_data_local")
    val TOKEN = stringPreferencesKey("token")
    val AVAILABLE = booleanPreferencesKey("available")
    val USER_ID = stringPreferencesKey("user_id")
    val FCM_TOKEN = stringPreferencesKey("fcm_token")
    val PERMISSIONS_COMPLETED = booleanPreferencesKey("permissions_completed")
}

class UserPreferencesDataStore(private val context: Context) {
    
    private val dataStore = context.dataStore
    
    suspend fun setIsLoggedIn(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[DataStoreKeys.IS_LOGGED_IN] = value
        }
    }
    
    val isLoggedIn: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[DataStoreKeys.IS_LOGGED_IN] ?: false
    }
    
    suspend fun setUserStatus(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[DataStoreKeys.USER_STATUS] = value
        }
    }
    
    suspend fun setUserData(value: String) {
        dataStore.edit { preferences ->
            preferences[DataStoreKeys.USER_DATA] = value
        }
    }
    
    val userData: Flow<String?> = dataStore.data.map { preferences ->
        preferences[DataStoreKeys.USER_DATA]
    }
    
    suspend fun setToken(value: String) {
        dataStore.edit { preferences ->
            preferences[DataStoreKeys.TOKEN] = value
        }
    }
    
    val token: Flow<String?> = dataStore.data.map { preferences ->
        preferences[DataStoreKeys.TOKEN]
    }
    
    suspend fun setAvailable(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[DataStoreKeys.AVAILABLE] = value
        }
    }
    
    val available: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[DataStoreKeys.AVAILABLE] ?: false
    }
    
    suspend fun setUserId(value: String) {
        dataStore.edit { preferences ->
            preferences[DataStoreKeys.USER_ID] = value
        }
    }
    
    val userId: Flow<String?> = dataStore.data.map { preferences ->
        preferences[DataStoreKeys.USER_ID]
    }
    
    suspend fun setFcmToken(value: String) {
        dataStore.edit { preferences ->
            preferences[DataStoreKeys.FCM_TOKEN] = value
        }
    }
    
    val fcmToken: Flow<String?> = dataStore.data.map { preferences ->
        preferences[DataStoreKeys.FCM_TOKEN]
    }

    suspend fun setPermissionsCompleted(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[DataStoreKeys.PERMISSIONS_COMPLETED] = value
        }
    }

    val permissionsCompleted: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[DataStoreKeys.PERMISSIONS_COMPLETED] ?: false
    }

    suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
