package com.serqfix.partner.data.api

import retrofit2.http.*

// Request models
data class CreateTicketRequest(
    val userId: String,
    val subject: String,
    val description: String,
    val category: String? = null,
    val priority: String? = null
)

data class TicketMessageRequest(
    val ticketId: String,
    val userId: String,
    val message: String
)

// Response models
data class TicketResponse(
    val success: Boolean,
    val message: String? = null,
    val data: TicketData? = null
)

data class TicketData(
    val _id: String? = null,
    val ticketId: String? = null,
    val subject: String? = null,
    val description: String? = null,
    val status: String? = null,
    val category: String? = null,
    val priority: String? = null,
    val messages: List<TicketMessageData>? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class TicketMessageData(
    val _id: String? = null,
    val message: String? = null,
    val senderId: String? = null,
    val senderType: String? = null,
    val createdAt: String? = null
)

data class TicketsListResponse(
    val success: Boolean,
    val message: String? = null,
    val data: List<TicketData>? = null,
    val meta: MetaData? = null
)

interface TicketApiService {
    @POST("tickets")
    suspend fun createTicket(@Body request: CreateTicketRequest): TicketResponse
    
    @GET("tickets")
    suspend fun getTicketsByUser(@Query("user") userId: String): TicketsListResponse
    
    @GET("tickets/{ticketId}")
    suspend fun getTicketById(@Path("ticketId") ticketId: String): TicketResponse
    
    @POST("tickets/{ticketId}/message")
    suspend fun sendMessage(
        @Path("ticketId") ticketId: String,
        @Body request: TicketMessageRequest
    ): TicketResponse
}
