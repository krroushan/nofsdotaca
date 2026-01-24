package com.serqfix.partner.util

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URISyntaxException

object SocketManager {
    private const val TAG = "SocketManager"
    private const val SOCKET_SERVER_URL = "https://app.serq.in"
    private const val SOCKET_PATH = "/api/socket"
    private var socket: Socket? = null
    private var isConnected = false
    
    fun initializeSocket(): Socket? {
        return initializeSocket(SOCKET_SERVER_URL)
    }
    
    fun initializeSocket(serverUrl: String): Socket? {
        return try {
            val options = IO.Options().apply {
                path = SOCKET_PATH
                reconnection = true
                reconnectionAttempts = Int.MAX_VALUE
                reconnectionDelay = 1000
                transports = arrayOf("websocket", "polling")
            }
            
            socket = IO.socket(serverUrl, options)
            socket?.connect()
            
            socket?.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "Socket connected")
                isConnected = true
            }
            
            socket?.on(Socket.EVENT_DISCONNECT) {
                Log.d(TAG, "Socket disconnected")
                isConnected = false
            }
            
            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                Log.e(TAG, "Socket connection error: ${args?.getOrNull(0)}")
                isConnected = false
            }
            
            socket
        } catch (e: URISyntaxException) {
            Log.e(TAG, "Invalid socket URL: $serverUrl", e)
            null
        }
    }
    
    fun getSocket(): Socket? = socket
    fun isConnected(): Boolean = isConnected
    
    fun disconnect() {
        socket?.disconnect()
        socket = null
        isConnected = false
    }
    
    fun joinRoom(roomId: String) {
        socket?.emit("join-user-room", roomId, io.socket.client.Ack { args ->
            val response = if (args.isNotEmpty()) args[0] else null
            Log.d(TAG, "Join room response: $response")
        })
    }
    
    fun leaveRoom(roomId: String) {
        socket?.emit("leave_room", roomId)
    }
    
    fun subscribeToCollection(
        collection: String,
        callback: (data: JSONObject) -> Unit
    ): () -> Unit {
        val dataRefreshHandler: (Array<out Any>) -> Unit = { args ->
            try {
                val data = args.getOrNull(0) as? JSONObject
                data?.let { callback(it) }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling data-refresh", e)
            }
        }
        
        val bookingEventHandler: (String) -> ((Array<out Any>) -> Unit) = { eventName ->
            { args ->
                try {
                    val data = args.getOrNull(0) as? JSONObject
                    data?.let { callback(it) }
                } catch (e: Exception) {
                    Log.e(TAG, "Error handling $eventName", e)
                }
            }
        }
        
        // Subscribe to various booking events
        socket?.on("data-refresh", dataRefreshHandler)
        socket?.on("booking-status", bookingEventHandler("booking-status"))
        socket?.on("new-booking", bookingEventHandler("new-booking"))
        socket?.on("booking-cancelled", bookingEventHandler("booking-cancelled"))
        socket?.on("booking-expired", bookingEventHandler("booking-expired"))
        socket?.on("booking-abandon", bookingEventHandler("booking-abandon"))
        socket?.on("booking-update", bookingEventHandler("booking-update"))
        socket?.on("invoice-accepted", bookingEventHandler("invoice-accepted"))
        socket?.on("invoice-updated", bookingEventHandler("invoice-updated"))
        
        socket?.emit("verify-subscription", JSONObject().apply {
            put("event", "data-refresh")
            put("collection", collection)
        })
        
        // Return cleanup function
        return {
            socket?.off("data-refresh", dataRefreshHandler)
            socket?.off("booking-status")
            socket?.off("new-booking")
            socket?.off("booking-cancelled")
            socket?.off("booking-expired")
            socket?.off("booking-abandon")
            socket?.off("booking-update")
            socket?.off("invoice-accepted")
            socket?.off("invoice-updated")
        }
    }
}
