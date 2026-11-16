package com.miturno.app.data.repository

import androidx.room.withTransaction
import com.miturno.app.data.local.AppDatabase
import com.miturno.app.data.model.AppException
import com.miturno.app.data.model.Point
import com.miturno.app.data.model.Role
import com.miturno.app.data.model.ServiceItem
import com.miturno.app.data.model.Turn
import com.miturno.app.data.model.TurnStatus
import com.miturno.app.data.model.User
import com.miturno.app.util.PasswordHasher
import com.miturno.app.util.TimeUtils
import kotlinx.coroutines.flow.Flow
import java.util.Locale

/**
 * Implementación con Room: toda la lógica de negocio vive aquí.
 * Como Room emite cambios automáticamente, cualquier actualización del funcionario
 * se refleja de inmediato en las pantallas de los afiliados (RF-15).
 */
class RoomTurnRepository(private val db: AppDatabase) : TurnRepository {

    private val dao = db.dao()

    // ---------- Autenticación ----------
    override suspend fun login(identifier: String, password: String, role: String): Result<User> {
        val user = dao.findUser(identifier.trim(), role)
        return when {
            user == null || user.passwordHash != PasswordHasher.hash(password) ->
                Result.failure(AppException("Credenciales incorrectas"))
            !user.active ->
                Result.failure(AppException("Tu usuario está inactivo. Comunícate con la EPS."))
            else -> Result.success(user)
        }
    }

    override suspend fun register(
        fullName: String, document: String, email: String,
        affiliationNumber: String, password: String
    ): Result<User> {
        if (dao.countDuplicates(document.trim(), email.trim(), affiliationNumber.trim()) > 0) {
            return Result.failure(AppException("Ya existe una cuenta con ese documento, correo o número de afiliación"))
        }
        val user = User(
            fullName = fullName.trim(), document = document.trim(), email = email.trim(),
            affiliationNumber = affiliationNumber.trim(),
            passwordHash = PasswordHasher.hash(password), role = Role.AFFILIATE
        )
        val id = dao.insertUser(user)
        return Result.success(user.copy(id = id))
    }

    // ---------- Consultas ----------
    override fun observePoints(): Flow<List<Point>> = dao.observePoints()
    override fun observeServices(pointId: Long): Flow<List<ServiceItem>> = dao.observeServices(pointId)
    override fun observeActiveTurn(userId: Long): Flow<Turn?> = dao.observeActiveTurn(userId)
    override fun observeCurrentTurn(pointId: Long): Flow<Turn?> = dao.observeCurrent(pointId)
    override fun observeQueue(pointId: Long): Flow<List<Turn>> = dao.observeQueue(pointId)
    override fun observeHistory(userId: Long): Flow<List<Turn>> = dao.observeHistory(userId)

    // ---------- Afiliado ----------
    override suspend fun requestTurn(userId: Long, serviceId: Long, pointId: Long): Result<Turn> =
        db.withTransaction {
            val service = dao.getService(serviceId)
                ?: return@withTransaction Result.failure(AppException("El servicio seleccionado no está disponible"))
            if (dao.activeCount(userId) > 0) {
                return@withTransaction Result.failure(
                    AppException("Ya tienes un turno activo. Cancélalo o espera a que sea atendido.")
                )
            }
            // RF-04: prefijo del servicio + consecutivo del día (ej. A-015)
            val next = dao.lastConsecutive(serviceId, TimeUtils.startOfToday()) + 1
            val user = dao.getUser(userId)
            val turn = Turn(
                number = String.format(Locale.US, "%s-%03d", service.prefix, next),
                consecutive = next,
                serviceId = service.id,
                serviceName = service.name,
                pointId = pointId,
                userId = userId,
                userName = user?.fullName.orEmpty(),
                userDocument = user?.document.orEmpty(),
                status = TurnStatus.PENDING,
                requestedAt = System.currentTimeMillis()
            )
            val id = dao.insertTurn(turn)
            Result.success(turn.copy(id = id))
        }

    override suspend fun cancelTurn(turnId: Long): Result<Unit> = db.withTransaction {
        val turn = dao.getTurn(turnId)
        when {
            turn == null -> Result.failure(AppException("No se encontró el turno"))
            turn.status != TurnStatus.PENDING ->
                Result.failure(AppException("Tu turno ya fue llamado y no se puede cancelar"))
            else -> {
                dao.updateTurn(turn.copy(status = TurnStatus.CANCELLED, finishedAt = System.currentTimeMillis()))
                Result.success(Unit)
            }
        }
    }

    // ---------- Funcionario ----------
    override suspend fun callNext(pointId: Long, module: String): Result<Turn> = db.withTransaction {
        val next = dao.getNextPending(pointId)
            ?: return@withTransaction Result.failure(AppException("No hay turnos pendientes en la fila"))
        val now = System.currentTimeMillis()
        // El turno que estaba en atención se cierra como ATENDIDO
        dao.getCurrent(pointId)?.let { dao.updateTurn(it.copy(status = TurnStatus.DONE, finishedAt = now)) }
        val called = next.copy(status = TurnStatus.IN_SERVICE, calledAt = now, module = module)
        dao.updateTurn(called)
        Result.success(called)
    }

    override suspend fun finishTurn(turnId: Long): Result<Unit> = db.withTransaction {
        val turn = dao.getTurn(turnId)
        if (turn == null || turn.status != TurnStatus.IN_SERVICE) {
            Result.failure(AppException("El turno no está en atención"))
        } else {
            dao.updateTurn(turn.copy(status = TurnStatus.DONE, finishedAt = System.currentTimeMillis()))
            Result.success(Unit)
        }
    }

    override suspend fun skipTurn(turnId: Long): Result<Unit> = db.withTransaction {
        val turn = dao.getTurn(turnId)
        if (turn == null || !(turn.status == TurnStatus.PENDING || turn.status == TurnStatus.IN_SERVICE)) {
            Result.failure(AppException("Ese turno ya no está activo"))
        } else {
            dao.updateTurn(turn.copy(status = TurnStatus.SKIPPED, finishedAt = System.currentTimeMillis()))
            Result.success(Unit)
        }
    }
}
