package com.serqfix.partner.data.repository

import com.serqfix.partner.data.api.ApiModule
import com.serqfix.partner.data.api.TicketApiService
import com.serqfix.partner.data.api.CreateTicketRequest
import com.serqfix.partner.data.api.TicketMessageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TicketRepository {
    
    private val apiService: TicketApiService = ApiModule.createService()
    
    suspend fun createTicket(
        userId: String,
        subject: String,
        description: String,
        category: String? = null,
        priority: String? = null
    ): Result<com.serqfix.partner.data.api.TicketResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createTicket(CreateTicketRequest(userId, subject, description, category, priority))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getTicketsByUser(userId: String): Result<com.serqfix.partner.data.api.TicketsListResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTicketsByUser(userId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getTicketById(ticketId: String): Result<com.serqfix.partner.data.api.TicketResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTicketById(ticketId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun sendMessage(ticketId: String, userId: String, message: String): Result<com.serqfix.partner.data.api.TicketResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.sendMessage(ticketId, TicketMessageRequest(ticketId, userId, message))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
