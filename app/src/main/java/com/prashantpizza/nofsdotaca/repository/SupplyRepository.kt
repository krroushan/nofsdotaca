package com.prashantpizza.nofsdotaca.repository

import android.content.Context
import android.util.Log
import com.prashantpizza.nofsdotaca.BuildConfig
import com.prashantpizza.nofsdotaca.utils.AuthErrorHandler
import com.prashantpizza.nofsdotaca.utils.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

/** Catalog item — never includes HQ buy cost (costPerUnit). */
data class SupplyCatalogItem(
    val _id: String,
    val slugId: String?,
    val name: String?,
    val unit: String?,
    val hsnCode: String?,
    val sellPrice: Double?,
    val brandName: String?,
    val gstRate: Double?,
    val minOrderQty: Double?,
    val description: String?
)

data class SupplyCatalogResponse(
    val success: Boolean,
    val data: List<SupplyCatalogItem>?,
    val error: String?
)

data class SupplyOrderLinePayload(
    val inventoryId: String,
    val qty: Double,
    val packCount: Double? = null,
    val packSize: Double? = null,
    val packUnit: String? = null
)

data class SupplyCreateOrderRequest(
    val lines: List<SupplyOrderLinePayload>,
    val notes: String? = null,
    val submit: Boolean = true,
    val source: String = "restaurant_app"
)

data class SupplyOrderSummary(
    val _id: String,
    val orderNumber: String?,
    val status: String?,
    val restaurantName: String?,
    val total: Double?,
    val taxable: Double?,
    val tax: Double?,
    val notes: String?,
    val createdAt: String?,
    val lines: List<SupplyOrderLine>?
)

data class SupplyOrderLine(
    val inventorySlugId: String?,
    val name: String?,
    val unit: String?,
    val orderedQty: Double?,
    val approvedQty: Double?,
    val dispatchedQty: Double?,
    val receivedQty: Double?,
    val sellPrice: Double?,
    val gstRate: Double?,
    val hsnCode: String?
)

data class SupplyOrderResponse(
    val success: Boolean,
    val data: SupplyOrderSummary?,
    val error: String?
)

data class SupplyOrdersListResponse(
    val success: Boolean,
    val data: List<SupplyOrderSummary>?,
    val error: String?
)

data class UpdateGstRequest(val gstNumber: String?)

interface SupplyApiService {
    @GET("supply/catalog")
    suspend fun getCatalog(
        @Query("q") q: String? = null,
        @Query("limit") limit: Int? = 30
    ): SupplyCatalogResponse

    @GET("supply/orders")
    suspend fun listOrders(
        @Query("status") status: String? = null,
        @Query("limit") limit: Int? = 30
    ): SupplyOrdersListResponse

    @POST("supply/orders")
    suspend fun createOrder(@Body body: SupplyCreateOrderRequest): SupplyOrderResponse

    @GET("supply/orders/{id}")
    suspend fun getOrder(@Path("id") id: String): SupplyOrderResponse

    @POST("supply/orders/{id}/receive")
    suspend fun receiveOrder(
        @Path("id") id: String,
        @Header("Idempotency-Key") idempotencyKey: String
    ): SupplyOrderResponse

    @PATCH("pos/restaurant/profile")
    suspend fun updateGst(@Body body: UpdateGstRequest): RestaurantProfileResponse
}

/**
 * SupplyRepository — franchise RPO APIs (catalog / orders / receive / GSTIN).
 * Separate from pizza OrdersRepository.
 */
class SupplyRepository private constructor(context: Context) {

    companion object {
        private const val TAG = "SupplyRepository"

        @Volatile
        private var INSTANCE: SupplyRepository? = null

        fun getInstance(context: Context): SupplyRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SupplyRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val appContext = context.applicationContext
    private val tokenManager = TokenManager.getInstance(context)
    private val authErrorHandler = AuthErrorHandler.getInstance(context)
    private val receiveKeys = mutableMapOf<String, String>()

    private val api: SupplyApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val authInterceptor = Interceptor { chain ->
            val token = tokenManager.getAccessToken()
            val req = if (token != null) {
                chain.request().newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
            } else {
                chain.request()
            }
            val response = chain.proceed(req)
            if (response.code == 401) {
                Log.w(TAG, "401 on supply API")
                authErrorHandler.handleAuthError()
            }
            response
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SupplyApiService::class.java)
    }

    suspend fun getCatalog(q: String? = null): Result<List<SupplyCatalogItem>> =
        withContext(Dispatchers.IO) {
            try {
                val res = api.getCatalog(q = q)
                if (res.success) Result.success(res.data ?: emptyList())
                else Result.failure(Exception(res.error ?: "Catalog failed"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun listOrders(status: String? = null): Result<List<SupplyOrderSummary>> =
        withContext(Dispatchers.IO) {
            try {
                val res = api.listOrders(status = status)
                if (res.success) Result.success(res.data ?: emptyList())
                else Result.failure(Exception(res.error ?: "List failed"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun createOrder(lines: List<SupplyOrderLinePayload>, notes: String? = null): Result<SupplyOrderSummary> =
        withContext(Dispatchers.IO) {
            try {
                val res = api.createOrder(SupplyCreateOrderRequest(lines = lines, notes = notes))
                if (res.success && res.data != null) Result.success(res.data)
                else Result.failure(Exception(res.error ?: "Create failed"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getOrder(id: String): Result<SupplyOrderSummary> =
        withContext(Dispatchers.IO) {
            try {
                val res = api.getOrder(id)
                if (res.success && res.data != null) Result.success(res.data)
                else Result.failure(Exception(res.error ?: "Not found"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun receiveOrder(id: String): Result<SupplyOrderSummary> =
        withContext(Dispatchers.IO) {
            try {
                val key = receiveKeys.getOrPut(id) { "recv-$id" }
                val res = api.receiveOrder(id, key)
                if (res.success && res.data != null) {
                    receiveKeys.remove(id)
                    Result.success(res.data)
                } else Result.failure(Exception(res.error ?: "Receive failed"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Download PO / invoice PDF with the JWT. Caller shares the file via FileProvider.
     */
    suspend fun downloadPdf(relativePath: String, filename: String): Result<File> =
        withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getAccessToken()
                    ?: return@withContext Result.failure(Exception("Not signed in"))
                val url = if (relativePath.startsWith("http")) {
                    relativePath
                } else {
                    "${BuildConfig.API_BASE_URL.trimEnd('/')}/${relativePath.trimStart('/')}"
                }
                val client = OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build()
                val request = Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer $token")
                    .get()
                    .build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(
                            Exception("PDF download failed (${response.code})")
                        )
                    }
                    val bytes = response.body?.bytes()
                        ?: return@withContext Result.failure(Exception("Empty PDF"))
                    val file = File(appContext.cacheDir, filename)
                    file.writeBytes(bytes)
                    Result.success(file)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun updateGstin(gstNumber: String?): Result<RestaurantProfile> =
        withContext(Dispatchers.IO) {
            try {
                val res = api.updateGst(UpdateGstRequest(gstNumber))
                if (res.success && res.data != null) Result.success(res.data)
                else Result.failure(Exception(res.error ?: "GSTIN update failed"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
