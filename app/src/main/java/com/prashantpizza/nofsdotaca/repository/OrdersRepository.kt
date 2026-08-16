package com.prashantpizza.nofsdotaca.repository

import android.content.Context
import android.util.Log
import com.prashantpizza.nofsdotaca.utils.TokenManager
import com.prashantpizza.nofsdotaca.utils.AuthErrorHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import retrofit2.http.Body
import java.util.concurrent.TimeUnit
import com.google.gson.*
import com.google.gson.reflect.TypeToken

// Order data models
data class OrderItemSize(
    val sizeId: String?,
    val size: String?,
    val price: Double?,
    val serves: Int?,
    val inventoryMultiplier: Int?,
    val hasSpecificInventory: Boolean?
)

data class OrderItemCrust(
    val crustId: String?,
    val crust: String?,
    val crustSize: String?,
    val price: Double?,
    val serves: Int?
)

data class OrderItemTopping(
    val toppingId: String?,
    val topping: String?,
    val toppingSize: String?,
    val image: String?,
    val price: Double?
)

data class OrderItemAddOn(
    val addOnId: String?,
    val addOn: String?,
    val image: String?,
    val price: Double?
)

data class OrderItemOffer(
    val offerId: String?,
    val offerType: String?,
    val isFreeItem: Boolean?,
    val originalPrice: Double?,
    val offerDiscount: Double?,
    val offerTitle: String?,
    val badgeColor: String?,
    val offerPrice: Double?
)

data class OrderCoupon(
    val couponId: String?,
    val code: String?,
    val name: String?,
    val discount: Double?,
    val type: String?,
    val value: Double?
)

data class OrderHistoryPerformer(
    val userId: String?,
    val userName: String?,
    val image: String?,
    val role: String?
)

data class OrderHistoryEntry(
    val status: String?,
    val paymentStatus: String?,
    val paymentMethod: String?,
    val action: String?,
    val description: String?,
    val source: String?,
    val performedBy: OrderHistoryPerformer?,
    val timestamp: String?,
    val notes: String?
)

// Product ID can be either a string or a populated object
data class PopulatedProduct(
    val _id: String?,
    val name: String?,
    val image: String?
)

private fun flattenMongoId(parent: JsonObject, field: String) {
    val element = parent.get(field)
    val id = when {
        element == null || element.isJsonNull -> null
        element.isJsonPrimitive && element.asJsonPrimitive.isString -> element.asString
        element.isJsonObject -> {
            val idElement = element.asJsonObject.get("_id")
            if (idElement != null && idElement.isJsonPrimitive) idElement.asString else null
        }
        else -> null
    }
    if (id != null) {
        parent.addProperty(field, id)
    } else if (element != null && !element.isJsonPrimitive) {
        parent.remove(field)
    }
}

// Custom deserializer for OrderItem to handle productId as string or object
class OrderItemDeserializer : JsonDeserializer<OrderItem> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: java.lang.reflect.Type?,
        context: JsonDeserializationContext?
    ): OrderItem {
        if (json == null || !json.isJsonObject) {
            throw JsonParseException("OrderItem must be a JSON object")
        }
        
        val jsonObject = json.asJsonObject
        val gson = Gson()
        val modifiedJson = jsonObject.deepCopy()
        flattenMongoId(modifiedJson, "productId")
        val offerElement = modifiedJson.get("offer")
        if (offerElement != null && offerElement.isJsonObject) {
            val offerObj = offerElement.asJsonObject
            flattenMongoId(offerObj, "offerId")
            modifiedJson.add("offer", offerObj)
        }
        return gson.fromJson(modifiedJson, OrderItem::class.java)
    }
}

// Custom deserializer for Order to handle restaurantId as string or object and items array
class OrderDeserializer : JsonDeserializer<Order> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: java.lang.reflect.Type?,
        context: JsonDeserializationContext?
    ): Order {
        if (json == null || !json.isJsonObject) {
            throw JsonParseException("Order must be a JSON object")
        }
        
        val jsonObject = json.asJsonObject
        val gson = GsonBuilder()
            .registerTypeAdapter(OrderItem::class.java, OrderItemDeserializer())
            .create()
        
        // Handle restaurantId - can be string or object
        val restaurantIdElement = jsonObject.get("restaurantId")
        val restaurantId: String? = when {
            restaurantIdElement == null || restaurantIdElement.isJsonNull -> null
            restaurantIdElement.isJsonPrimitive && restaurantIdElement.asJsonPrimitive.isString -> {
                restaurantIdElement.asString
            }
            restaurantIdElement.isJsonObject -> {
                // Extract _id from populated object
                val restaurantObj = restaurantIdElement.asJsonObject
                restaurantObj.get("_id")?.asString
            }
            else -> null
        }
        
        // Create a modified JSON object with restaurantId as string
        val modifiedJson = jsonObject.deepCopy()
        if (restaurantId != null) {
            modifiedJson.addProperty("restaurantId", restaurantId)
        } else {
            modifiedJson.remove("restaurantId")
        }
        
        // Handle items array - process each item to convert productId from object to string
        val itemsElement = modifiedJson.get("items")
        if (itemsElement != null && itemsElement.isJsonArray) {
            val itemsArray = itemsElement.asJsonArray
            val processedItems = JsonArray()
            
            for (itemElement in itemsArray) {
                if (itemElement.isJsonObject) {
                    val modifiedItem = itemElement.asJsonObject.deepCopy()
                    flattenMongoId(modifiedItem, "productId")
                    val offerElement = modifiedItem.get("offer")
                    if (offerElement != null && offerElement.isJsonObject) {
                        flattenMongoId(offerElement.asJsonObject, "offerId")
                    }
                    processedItems.add(modifiedItem)
                } else {
                    processedItems.add(itemElement)
                }
            }
            
            modifiedJson.add("items", processedItems)
        }

        val couponElement = modifiedJson.get("coupon")
        if (couponElement != null && couponElement.isJsonObject) {
            val couponObj = couponElement.asJsonObject
            flattenMongoId(couponObj, "couponId")
            modifiedJson.add("coupon", couponObj)
        }
        
        // Deserialize the rest normally with the custom gson that has OrderItemDeserializer
        return gson.fromJson(modifiedJson, Order::class.java)
    }
}

data class OrderItem(
    val productId: String?,
    val productName: String?,
    val productImage: String?,
    val selectedSize: OrderItemSize?,
    val selectedCrust: OrderItemCrust?,
    val selectedToppings: List<OrderItemTopping>?,
    val selectedAddOns: List<OrderItemAddOn>?,
    val quantity: Int?,
    val basePrice: Double?,
    val customizationPrice: Double?,
    val itemTotal: Double?,
    val gstType: String?,
    val offer: OrderItemOffer?,
    val specialInstructions: String?,
    val _id: String?
)

data class OrderCustomer(
    val customerId: String?,
    val name: String?,
    val phone: String?,
    val email: String?,
    val gstNumber: String?
)

data class OrderPayment(
    val status: String?,
    val method: String?,
    val amount: Double?,
    val transactionId: String?
)

data class DeliveryAddress(
    val label: String?,
    val flatNo: String?,
    val buildingName: String?,
    val addressLine: String?,
    val landmark: String?,
    val city: String?,
    val state: String?,
    val pincode: String?,
    val lat: Double?,
    val lng: Double?
)

data class PickupAddress(
    val lat: Double?,
    val lng: Double?
)

data class Order(
    val _id: String,
    val orderNumber: String,
    val orderType: String?,
    val source: String?,
    val customer: OrderCustomer?,
    val items: List<OrderItem>?,
    val subtotal: Double?,
    val tax: Double?,
    val gstRate: Double?,
    val gstEnabled: Boolean?,
    val gstExempt: Boolean?,
    val convenienceFee: Double?,
    val convenienceFeeName: String?,
    val deliveryFee: Double?,
    val deliveryFeeName: String?,
    val discount: Double?,
    val total: Double?,
    val coupon: OrderCoupon?,
    val status: String?,
    val payment: OrderPayment?,
    val deliveryAddress: DeliveryAddress?,
    val pickupAddress: PickupAddress?,
    val estimatedTime: String?,
    val actualTime: String?,
    val deliveryBoy: Any?,
    val restaurantId: String?,
    val notes: String?,
    val orderHistory: List<OrderHistoryEntry>?,
    val createdAt: String?,
    val updatedAt: String?
)

data class OrdersResponse(
    val success: Boolean,
    val data: List<Order>?,
    val stats: OrdersStats?,
    val pagination: OrdersPagination?,
    val error: String?
)

data class OrdersStats(
    val total: Int?,
    val totalRevenue: Double?,
    val averageOrderValue: Double?,
    val statusCounts: Map<String, Int>?,
    val paymentStatusCounts: Map<String, Int>?
)

data class OrdersPagination(
    val page: Int?,
    val limit: Int?,
    val total: Int?,
    val pages: Int?,
    val hasNext: Boolean?,
    val hasPrev: Boolean?
)

data class OrderDetailResponse(
    val success: Boolean,
    val data: Order?,
    val error: String?
)

data class UpdateStatusRequest(
    val status: String,
    val notes: String? = null
)

data class UpdatePaymentStatusRequest(
    val paymentStatus: String,
    val paymentMethod: String? = null
)

data class UpdateStatusResponse(
    val success: Boolean,
    val data: OrderUpdateData?,
    val message: String?,
    val error: String?
)

data class OrderUpdateData(
    val _id: String?,
    val orderNumber: String?,
    val status: String?,
    val payment: OrderPayment?
)

// Retrofit API interface
interface OrdersApiService {
    @GET("pos/orders")
    suspend fun getOrders(
        @Query("status") status: String? = null,
        @Query("orderType") orderType: String? = null,
        @Query("paymentStatus") paymentStatus: String? = null,
        @Query("source") source: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("page") page: Int? = null,
        @Query("search") search: String? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): OrdersResponse
    
    @GET("pos/orders/{id}")
    suspend fun getOrderById(@Path("id") id: String): OrderDetailResponse
    
    @PUT("pos/orders/{id}/status")
    suspend fun updateOrderStatus(
        @Path("id") id: String,
        @Body request: UpdateStatusRequest
    ): UpdateStatusResponse
    
    @PATCH("pos/orders/{id}/payment-status")
    suspend fun updatePaymentStatus(
        @Path("id") id: String,
        @Body request: UpdatePaymentStatusRequest
    ): UpdateStatusResponse
}

class OrdersRepository private constructor(context: Context) {
    
    companion object {
        private const val TAG = "OrdersRepository"
        private const val BASE_URL = "https://pos.prashantpizza.in/api/mobile/"
        
        @Volatile
        private var instance: OrdersRepository? = null
        
        fun getInstance(context: Context? = null): OrdersRepository {
            return instance ?: synchronized(this) {
                if (context == null) {
                    throw IllegalStateException("Context is required for first initialization")
                }
                instance ?: OrdersRepository(context.applicationContext).also { instance = it }
            }
        }
    }
    
    private val tokenManager: TokenManager = TokenManager.getInstance(context)
    private val authErrorHandler: AuthErrorHandler = AuthErrorHandler.getInstance(context)
    
    private val apiService: OrdersApiService by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        // Auth interceptor to add Authorization header
        val authInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val accessToken = tokenManager.getAccessToken()
            
            val newRequest = if (accessToken != null) {
                originalRequest.newBuilder()
                    .header("Authorization", "Bearer $accessToken")
                    .build()
            } else {
                originalRequest
            }
            
            val response = chain.proceed(newRequest)
            
            // Handle 401 Unauthorized - token expired or invalid
            if (response.code == 401) {
                Log.w(TAG, "Received 401 Unauthorized - token expired or invalid")
                authErrorHandler.handleAuthError()
            }
            
            response
        }
        
        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
        
        // Create Gson with custom deserializers for OrderItem and Order
        val gson = GsonBuilder()
            .registerTypeAdapter(OrderItem::class.java, OrderItemDeserializer())
            .registerTypeAdapter(Order::class.java, OrderDeserializer())
            .create()
        
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(OrdersApiService::class.java)
    }
    
    /**
     * Get orders with optional filters
     */
    suspend fun getOrders(
        status: String? = null,
        orderType: String? = null,
        paymentStatus: String? = null,
        source: String? = null,
        limit: Int = 50,
        page: Int = 1,
        search: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ): Result<OrdersResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching orders: status=$status, page=$page, limit=$limit")
            
            val response = apiService.getOrders(
                status = status,
                orderType = orderType,
                paymentStatus = paymentStatus,
                source = source,
                limit = limit,
                page = page,
                search = search,
                startDate = startDate,
                endDate = endDate
            )
            
            if (response.success && response.data != null) {
                Log.d(TAG, "Orders fetched successfully: ${response.data.size} orders")
                Result.success(response)
            } else {
                val errorMessage = response.error ?: "Failed to fetch orders"
                Log.e(TAG, "Failed to fetch orders: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching orders: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get order by ID
     */
    suspend fun getOrderById(orderId: String): Result<Order> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching order: $orderId")
            
            val response = apiService.getOrderById(orderId)
            
            if (response.success && response.data != null) {
                Log.d(TAG, "Order fetched successfully: ${response.data.orderNumber}")
                Result.success(response.data)
            } else {
                val errorMessage = response.error ?: "Order not found"
                Log.e(TAG, "Failed to fetch order: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching order: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Update order status
     */
    suspend fun updateOrderStatus(
        orderId: String,
        status: String,
        notes: String? = null
    ): Result<OrderUpdateData> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Updating order status: $orderId -> $status")
            
            val request = UpdateStatusRequest(status = status, notes = notes)
            val response = apiService.updateOrderStatus(orderId, request)
            
            if (response.success && response.data != null) {
                Log.d(TAG, "Order status updated successfully: ${response.message}")
                Result.success(response.data)
            } else {
                val errorMessage = response.error ?: "Failed to update order status"
                Log.e(TAG, "Failed to update order status: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating order status: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Update payment status
     */
    suspend fun updatePaymentStatus(
        orderId: String,
        paymentStatus: String,
        paymentMethod: String? = null
    ): Result<OrderUpdateData> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Updating payment status: $orderId -> $paymentStatus")
            
            val request = UpdatePaymentStatusRequest(
                paymentStatus = paymentStatus,
                paymentMethod = paymentMethod
            )
            val response = apiService.updatePaymentStatus(orderId, request)
            
            if (response.success && response.data != null) {
                Log.d(TAG, "Payment status updated successfully: ${response.message}")
                Result.success(response.data)
            } else {
                val errorMessage = response.error ?: "Failed to update payment status"
                Log.e(TAG, "Failed to update payment status: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating payment status: ${e.message}", e)
            Result.failure(e)
        }
    }
}
