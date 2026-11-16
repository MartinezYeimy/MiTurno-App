package com.miturno.app.data.local

import com.miturno.app.data.model.Point
import com.miturno.app.data.model.Role
import com.miturno.app.data.model.ServiceItem
import com.miturno.app.data.model.Turn
import com.miturno.app.data.model.TurnStatus
import com.miturno.app.data.model.User
import com.miturno.app.util.PasswordHasher

/**
 * Datos de ejemplo para poder probar la app sin servidor.
 * Solo se insertan la primera vez (cuando la base de datos está vacía).
 *
 * CUENTAS DE PRUEBA
 *   Afiliados (documento / contraseña "123456"):
 *     1000000001 María Rodríguez   (sin turno: puede pedir uno)
 *     1000000002 Juan Pérez        (EN ATENCIÓN ahora, Sede Centro)
 *     1000000003 Ana Martínez      (siguiente en la fila)
 *     1000000004 Carlos Gómez      (1 turno por delante)
 *     1000000005 Luisa Fernández   (2 turnos por delante)
 *     1000000006 Pedro Ramírez     (sin turno: puede pedir uno)
 *   Funcionarios (contraseña "Func1234"):
 *     func.centro -> Sede Centro, "Módulo 1"
 *     func.norte  -> Sede Norte,  "Ventanilla 2"
 */
object DemoData {

    const val DEMO_AFFILIATE = "1000000001"
    const val DEMO_AFFILIATE_PASSWORD = "123456"
    const val DEMO_STAFF = "func.centro"
    const val DEMO_STAFF_PASSWORD = "Func1234"

    private const val AFFILIATE_PASSWORD = "123456"

    suspend fun seedIfEmpty(db: AppDatabase) {
        val dao = db.dao()
        if (dao.countPoints() > 0) return

        val points = listOf(
            Point(1, "Sede Centro", "Calle 10 # 5-20"),
            Point(2, "Sede Norte", "Carrera 15 # 100-30")
        )
        dao.insertPoints(points)

        val templates = listOf(
            Triple("Autorizaciones", "Solicita o consulta autorizaciones de servicios y procedimientos.", "A"),
            Triple("Afiliación y actualización de datos", "Resuelve problemas con tu afiliación o actualiza tus datos personales.", "B"),
            Triple("Información y orientación", "Recibe orientación sobre los servicios de la EPS.", "C"),
            Triple("Peticiones, quejas y reclamos", "Presenta una petición, queja, reclamo o sugerencia.", "D")
        )
        var serviceId = 1L
        val services = points.flatMap { point ->
            templates.map { (name, description, prefix) ->
                ServiceItem(serviceId++, point.id, name, description, prefix)
            }
        }
        dao.insertServices(services)

        // Funcionarios (cada uno atiende en un módulo o ventanilla distinto)
        dao.insertUser(
            User(
                fullName = "Laura Gómez", username = DEMO_STAFF,
                passwordHash = PasswordHasher.hash(DEMO_STAFF_PASSWORD),
                role = Role.STAFF, pointId = 1, module = "Módulo 1"
            )
        )
        dao.insertUser(
            User(
                fullName = "Carlos Pérez", username = "func.norte",
                passwordHash = PasswordHasher.hash(DEMO_STAFF_PASSWORD),
                role = Role.STAFF, pointId = 2, module = "Ventanilla 2"
            )
        )

        // Afiliados de prueba (documento 1000000001 a 1000000006, contraseña 123456)
        val affiliates = listOf(
            Triple("María Rodríguez", "maria@correo.com", "AF-0001"),
            Triple("Juan Pérez", "juan@correo.com", "AF-0002"),
            Triple("Ana Martínez", "ana@correo.com", "AF-0003"),
            Triple("Carlos Gómez", "carlos@correo.com", "AF-0004"),
            Triple("Luisa Fernández", "luisa@correo.com", "AF-0005"),
            Triple("Pedro Ramírez", "pedro@correo.com", "AF-0006")
        )
        val users = affiliates.mapIndexed { i, (name, email, affiliation) ->
            val document = "100000000${i + 1}"
            val id = dao.insertUser(
                User(
                    fullName = name, document = document, email = email,
                    affiliationNumber = affiliation,
                    passwordHash = PasswordHasher.hash(AFFILIATE_PASSWORD)
                )
            )
            Triple(id, name, document)
        }

        // Fila ya armada en la Sede Centro: Juan en atención, Ana, Carlos y Luisa esperando
        val now = System.currentTimeMillis()
        val min = 60_000L
        fun turn(
            number: String, consecutive: Int, serviceId: Long, serviceName: String,
            user: Triple<Long, String, String>, status: String, ago: Long,
            called: Long? = null, module: String = ""
        ) = Turn(
            number = number, consecutive = consecutive, serviceId = serviceId, serviceName = serviceName,
            pointId = 1, userId = user.first, userName = user.second, userDocument = user.third,
            status = status, requestedAt = now - ago * min,
            calledAt = called?.let { now - it * min }, module = module
        )

        dao.insertTurn(turn("A-001", 1, 1, "Autorizaciones", users[1], TurnStatus.IN_SERVICE, 30, 5, "Módulo 1"))
        dao.insertTurn(turn("B-001", 1, 2, "Afiliación y actualización de datos", users[2], TurnStatus.PENDING, 20))
        dao.insertTurn(turn("A-002", 2, 1, "Autorizaciones", users[3], TurnStatus.PENDING, 12))
        dao.insertTurn(turn("C-001", 1, 3, "Información y orientación", users[4], TurnStatus.PENDING, 6))
    }
}
