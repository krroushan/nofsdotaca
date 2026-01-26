package com.serqfix.partner.data.repository

import com.serqfix.partner.data.api.ApiModule
import com.serqfix.partner.data.api.AttendanceApiService
import com.serqfix.partner.data.api.PunchAttendanceRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AttendanceRepository {
    
    private val apiService: AttendanceApiService = ApiModule.createService()
    
    suspend fun fetchProviderAttendance(
        providerId: String,
        startDate: String? = null,
        endDate: String? = null
    ): Result<com.serqfix.partner.data.api.AttendanceResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.fetchProviderAttendance(providerId, startDate, endDate)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun punchAttendance(
        providerId: String,
        action: String,
        location: com.serqfix.partner.data.api.LocationData? = null
    ): Result<com.serqfix.partner.data.api.AttendanceResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.punchAttendance(PunchAttendanceRequest(providerId, action, location))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun checkIsHoliday(date: String): Result<com.serqfix.partner.data.api.HolidayCheckResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.checkIsHoliday(date)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
