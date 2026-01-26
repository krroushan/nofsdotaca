package com.serqfix.partner.data.api

import retrofit2.http.*

// Response models
data class AssetResponse(
    val success: Boolean,
    val message: String? = null,
    val data: List<AssetData>? = null
)

data class AssetData(
    val _id: String? = null,
    val name: String? = null,
    val description: String? = null,
    val assignedAt: String? = null,
    val assetType: String? = null,
    val status: String? = null
)

interface AssetApiService {
    @GET("mobile-app/assets/assignments")
    suspend fun getAssetAssignments(@Query("providerId") providerId: String): AssetResponse
}
