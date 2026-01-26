package com.serqfix.partner.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateTimeUtils {
    
    /**
     * Format time string to AM/PM format
     * Example: "14:30" -> "2:30 PM"
     */
    fun formatTimeToAMPM(time: String?): String {
        if (time == null || time.isEmpty()) return ""
        
        return try {
            // Try parsing as HH:mm format
            val inputFormat = SimpleDateFormat("HH:mm", Locale.US)
            val outputFormat = SimpleDateFormat("h:mm a", Locale.US)
            val date = inputFormat.parse(time)
            if (date != null) {
                outputFormat.format(date)
            } else {
                time // Return original if parsing fails
            }
        } catch (e: Exception) {
            time // Return original if parsing fails
        }
    }
    
    /**
     * Check if a date is today
     */
    fun isToday(date: Date): Boolean {
        val today = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return dateFormat.format(date) == dateFormat.format(today)
    }
    
    /**
     * Get day name from date (e.g., "Monday", "Tuesday")
     */
    fun getDayName(date: Date): String {
        val dateFormat = SimpleDateFormat("EEEE", Locale.US)
        return dateFormat.format(date)
    }
    
    /**
     * Format date to locale time string
     */
    fun formatToLocaleTime(dateString: String?): String {
        if (dateString == null || dateString.isEmpty()) return "--"
        
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            inputFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(dateString)
            if (date != null) {
                val outputFormat = SimpleDateFormat("h:mm a", Locale.US)
                outputFormat.format(date)
            } else {
                "--"
            }
        } catch (e: Exception) {
            "--"
        }
    }
}
