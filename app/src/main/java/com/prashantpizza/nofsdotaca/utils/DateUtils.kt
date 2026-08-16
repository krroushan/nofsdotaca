package com.prashantpizza.nofsdotaca.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    fun formatDate(dateString: String): String {
        return format(dateString, "MMM dd, yyyy 'at' hh:mm a")
    }

    fun formatDateTime(dateString: String?): String {
        if (dateString.isNullOrBlank()) return ""
        return format(dateString, "d MMM yyyy, hh:mm a")
    }

    private fun format(dateString: String, pattern: String): String {
        val parsers = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSSX",
            "yyyy-MM-dd'T'HH:mm:ssX"
        )
        for (parser in parsers) {
            try {
                val inputFormat = SimpleDateFormat(parser, Locale.US)
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                val date = inputFormat.parse(dateString) ?: continue
                val outputFormat = SimpleDateFormat(pattern, Locale.ENGLISH)
                outputFormat.timeZone = TimeZone.getDefault()
                return outputFormat.format(date)
            } catch (_: Exception) {
            }
        }
        return dateString
    }
}
