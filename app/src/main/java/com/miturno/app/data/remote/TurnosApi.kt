package com.miturno.app.data.remote

import com.miturno.app.data.model.Point
import com.miturno.app.data.model.ServiceItem
import com.miturno.app.data.model.Turn
import com.miturno.app.data.model.User
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

/** Configuración general. Cambia USE_REMOTE_API a true cuando tengas el servidor listo. */
object AppConfig {
    const val USE_REMOTE_API = false
    // 10.0.2.2 = el "localhost" de tu computador visto desde el emulador de Android Studio.
    const val BASE_URL = "http://10.0.2.2:8080/api/"
    const val POLL_INTERVAL_MS = 3_000L   // RNF-03: la actualización no debe tardar más de 5 s
    const val NOTIFY_THRESHOLD = 3        // RF-07: avisar cuando falten 3 turnos o menos
    const val MINUTES_PER_TURN = 10       // Tiempo estimado de atención por turno (cámbialo si lo necesitas)
}

data class LoginRequest(val identifier: String, val password: String, val role: String)

data class RegisterRequest(
    val fullName: String,
    val document: String,
    val email: String,
    val affiliationNumber: String,
    val password: String
)

data class TurnRequest(val userId: Long, val serviceId: Long, val pointId: Long)

data class CallNextRequest(val module: String)

data class ApiError(val message: String? = null)

/**
 * Contrato REST que debe cumplir el servidor.
 * Los endpoints que devuelven "Turn?" deben responder 204 (sin contenido) cuando no hay turno.
 */
interface TurnosApi {
    @POST("auth/login") suspend fun login(@Body body: LoginRequest): User
    @POST("auth/register") suspend fun register(@Body body: RegisterRequest): User

    @GET("points") suspend fun points(): List<Point>
    @GET("points/{id}/services") suspend fun services(@Path("id") pointId: Long): List<ServiceItem>
    @GET("points/{id}/current") suspend fun current(@Path("id") pointId: Long): Turn?
    @GET("points/{id}/queue") suspend fun queue(@Path("id") pointId: Long): List<Turn>
    @POST("points/{id}/call-next") suspend fun callNext(@Path("id") pointId: Long, @Body body: CallNextRequest): Turn

    @POST("turns") suspend fun requestTurn(@Body body: TurnRequest): Turn
    @POST("turns/{id}/cancel") suspend fun cancel(@Path("id") turnId: Long): Turn
    @POST("turns/{id}/finish") suspend fun finish(@Path("id") turnId: Long): Turn
    @POST("turns/{id}/skip") suspend fun skip(@Path("id") turnId: Long): Turn

    @GET("users/{id}/active-turn") suspend fun activeTurn(@Path("id") userId: Long): Turn?
    @GET("users/{id}/turns") suspend fun history(@Path("id") userId: Long): List<Turn>
}

object RetrofitProvider {
    val api: TurnosApi by lazy {
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
        Retrofit.Builder()
            .baseUrl(AppConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TurnosApi::class.java)
    }
}
