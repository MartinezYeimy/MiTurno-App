package com.miturno.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Roles de la aplicación (RF-01 y RF-10). */
object Role {
    const val AFFILIATE = "AFILIADO"
    const val STAFF = "FUNCIONARIO"
}

/** Estados posibles de un turno. */
object TurnStatus {
    const val PENDING = "PENDIENTE"
    const val IN_SERVICE = "EN_ATENCION"
    const val DONE = "ATENDIDO"
    const val CANCELLED = "CANCELADO"
    const val SKIPPED = "OMITIDO"
}

/** Error con mensaje listo para mostrar al usuario. */
class AppException(message: String) : Exception(message)

/** Punto de atención de la EPS (sede). */
@Entity(tableName = "points")
data class Point(
    @PrimaryKey val id: Long = 0,
    val name: String = "",
    val address: String = ""
)

/** Servicio ofrecido en un punto de atención (RF-02). [prefix] es la letra del turno (A-015). */
@Entity(tableName = "services")
data class ServiceItem(
    @PrimaryKey val id: Long = 0,
    val pointId: Long = 0,
    val name: String = "",
    val description: String = "",
    val prefix: String = ""
)

/** Afiliado o funcionario. Los funcionarios usan [username] y tienen un [pointId] asignado. */
@Entity(
    tableName = "users",
    indices = [Index("document"), Index("email"), Index("affiliationNumber"), Index("username")]
)
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String = "",
    val document: String = "",
    val email: String = "",
    val affiliationNumber: String = "",
    val username: String = "",
    val passwordHash: String = "",
    val role: String = Role.AFFILIATE,
    val pointId: Long = 0,
    /** Solo funcionarios: módulo o ventanilla donde atiende (se le muestra al afiliado al ser llamado). */
    val module: String = "",
    val active: Boolean = true
)

/** Turno de atención (entidad TURNO del documento). */
@Entity(tableName = "turns", indices = [Index("userId"), Index("pointId", "status")])
data class Turn(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val number: String = "",
    val consecutive: Int = 0,
    val serviceId: Long = 0,
    val serviceName: String = "",
    val pointId: Long = 0,
    val userId: Long = 0,
    /** Datos del afiliado, para que el funcionario sepa a quién atiende. */
    val userName: String = "",
    val userDocument: String = "",
    val status: String = TurnStatus.PENDING,
    val requestedAt: Long = 0,
    val calledAt: Long? = null,
    val finishedAt: Long? = null,
    /** Módulo o ventanilla que llamó el turno (se llena al pasar a EN_ATENCION). */
    val module: String = ""
)

/**
 * Progreso del turno activo del afiliado:
 * [turn] su turno, [current] el turno en atención en ese punto y [ahead] cuántos turnos pendientes tiene antes (RF-05/RF-06).
 */
data class Progress(
    val turn: Turn? = null,
    val current: Turn? = null,
    val ahead: Int = 0
)
