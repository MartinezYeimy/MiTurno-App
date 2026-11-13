package com.miturno.app.data.repository

import com.google.gson.Gson
import com.miturno.app.data.model.AppException
import com.miturno.app.data.model.Point
import com.miturno.app.data.model.ServiceItem
import com.miturno.app.data.model.Turn
import com.miturno.app.data.model.User
import com.miturno.app.data.remote.ApiError
import com.miturno.app.data.remote.AppConfig
import com.miturno.app.data.remote.LoginRequest
import com.miturno.app.data.remote.RegisterRequest
import com.miturno.app.data.remote.TurnRequest
import com.miturno.app.data.remote.TurnosApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException

/**
 * Implementación con Retrofit. Para simular "tiempo real" consulta el servidor cada
 * [AppConfig.POLL_INTERVAL_MS] ms y solo emite cuando el dato cambia (RNF-03: máx. 5 s).
 * Más adelante se puede cambiar el polling por WebSockets o Firebase sin tocar la interfaz gráfica.
 */
class RemoteTurnRepository(private val api: TurnosApi) : TurnRepository {

    private suspend fun <T> call(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpException) {
        Result.failure(AppException(parseError(e)))
    } catch (e: IOException) {
        Result.failure(AppException("Sin conexión. Revisa tu Internet e inténtalo de nuevo."))
    } catch (e: Exception) {
        Result.failure(AppException(e.message ?: "Ocurrió un error inesperado"))
    }

    private fun parseError(e: HttpException): String {
        val body = try { e.response()?.errorBody()?.string() } catch (_: Exception) { null }
        val serverMessage = try { Gson().fromJson(body, ApiError::class.java)?.message } catch (_: Exception) { null }
        return serverMessage ?: when (e.code()) {
            401 -> "Credenciales incorrectas"
            409 -> "La operación no es posible en el estado actual del turno"
            else -> "Error del servidor (${e.code()})"
        }
    }

    private fun <T> poll(intervalMs: Long = AppConfig.POLL_INTERVAL_MS, fetch: suspend () -> T): Flow<T> = flow {
        while (true) {
            val result = runCatching { fetch() }
            if (result.isSuccess) emit(result.getOrThrow())
            delay(intervalMs)
        }
    }.distinctUntilChanged()

    // ---------- Autenticación ----------
    override suspend fun login(identifier: String, password: String, role: String): Result<User> =
        call { api.login(LoginRequest(identifier.trim(), password, role)) }

    override suspend fun register(
        fullName: String, document: String, email: String,
        affiliationNumber: String, password: String
    ): Result<User> = call {
        api.register(RegisterRequest(fullName.trim(), document.trim(), email.trim(), affiliationNumber.trim(), password))
    }

    // ---------- Consultas ----------
    override fun observePoints(): Flow<List<Point>> = poll(30_000) { api.points() }
    override fun observeServices(pointId: Long): Flow<List<ServiceItem>> = poll(30_000) { api.services(pointId) }
    override fun observeActiveTurn(userId: Long): Flow<Turn?> = poll { api.activeTurn(userId) }
    override fun observeCurrentTurn(pointId: Long): Flow<Turn?> = poll { api.current(pointId) }
    override fun observeQueue(pointId: Long): Flow<List<Turn>> = poll { api.queue(pointId) }
    override fun observeHistory(userId: Long): Flow<List<Turn>> = poll { api.history(userId) }

    // ---------- Acciones ----------
    override suspend fun requestTurn(userId: Long, serviceId: Long, pointId: Long): Result<Turn> =
        call { api.requestTurn(TurnRequest(userId, serviceId, pointId)) }

    override suspend fun cancelTurn(turnId: Long): Result<Unit> = call { api.cancel(turnId) }.map { }
    override suspend fun callNext(pointId: Long): Result<Turn> = call { api.callNext(pointId) }
    override suspend fun finishTurn(turnId: Long): Result<Unit> = call { api.finish(turnId) }.map { }
    override suspend fun skipTurn(turnId: Long): Result<Unit> = call { api.skip(turnId) }.map { }
}
