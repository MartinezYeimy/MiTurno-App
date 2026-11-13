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
 */
object DemoData {

    // Credenciales de prueba (se muestran en la pantalla de inicio de sesión)
    const val DEMO_AFFILIATE = "1000000001"
    const val DEMO_AFFILIATE_PASSWORD = "123456"
    const val DEMO_STAFF = "func.centro"
    const val DEMO_STAFF_PASSWORD = "Func1234"

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

        // Funcionarios
        dao.insertUser(
            User(
                fullName = "Laura Gómez", username = DEMO_STAFF,
                passwordHash = PasswordHasher.hash(DEMO_STAFF_PASSWORD),
                role = Role.STAFF, pointId = 1
            )
        )
        dao.insertUser(
            User(
                fullName = "Carlos Pérez", username = "func.norte",
                passwordHash = PasswordHasher.hash(DEMO_STAFF_PASSWORD),
                role = Role.STAFF, pointId = 2
            )
        )

        // Afiliado de prueba
        dao.insertUser(
            User(
                fullName = "María Rodríguez", document = DEMO_AFFILIATE,
                email = "maria@correo.com", affiliationNumber = "AF-0001",
                passwordHash = PasswordHasher.hash(DEMO_AFFILIATE_PASSWORD)
            )
        )

        // Otros afiliados con turnos ya en la fila de la Sede Centro
        val others = (1..4).map { i ->
            dao.insertUser(
                User(
                    fullName = "Afiliado ejemplo $i", document = "200000000$i",
                    email = "ejemplo$i@correo.com", affiliationNumber = "AF-100$i",
                    passwordHash = PasswordHasher.hash("123456")
                )
            )
        }
        val now = System.currentTimeMillis()
        val min = 60_000L
        fun turn(number: String, consecutive: Int, serviceId: Long, serviceName: String, user: Long, status: String, ago: Long, called: Long? = null) =
            Turn(
                number = number, consecutive = consecutive, serviceId = serviceId, serviceName = serviceName,
                pointId = 1, userId = user, status = status, requestedAt = now - ago * min,
                calledAt = called?.let { now - it * min }
            )

        dao.insertTurn(turn("A-001", 1, 1, "Autorizaciones", others[0], TurnStatus.IN_SERVICE, 30, 5))
        dao.insertTurn(turn("B-001", 1, 2, "Afiliación y actualización de datos", others[1], TurnStatus.PENDING, 20))
        dao.insertTurn(turn("A-002", 2, 1, "Autorizaciones", others[2], TurnStatus.PENDING, 12))
        dao.insertTurn(turn("C-001", 1, 3, "Información y orientación", others[3], TurnStatus.PENDING, 6))
    }
}
