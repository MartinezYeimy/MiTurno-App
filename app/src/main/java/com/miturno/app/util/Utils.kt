package com.miturno.app.util

import com.miturno.app.data.remote.AppConfig
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Hash SHA-256 para no guardar contraseñas en texto plano. */
object PasswordHasher {
    fun hash(plain: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(("miturno::$plain").toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

/** Tiempo estimado de espera: turnos por delante × minutos por turno (AppConfig.MINUTES_PER_TURN). */
object WaitEstimate {
    fun minutes(ahead: Int): Int = ahead.coerceAtLeast(0) * AppConfig.MINUTES_PER_TURN

    fun text(ahead: Int): String {
        if (ahead <= 0) return "Muy pronto, eres el siguiente"
        val m = minutes(ahead)
        if (m < 60) return "Aprox. $m minutos"
        val h = m / 60
        val r = m % 60
        return if (r == 0) "Aprox. $h h" else "Aprox. $h h $r min"
    }
}

object TimeUtils {
    private val locale = Locale("es", "CO")

    fun startOfToday(): Long {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    fun time(millis: Long): String = SimpleDateFormat("h:mm a", locale).format(Date(millis))

    fun dateTime(millis: Long): String =
        SimpleDateFormat("d 'de' MMM yyyy, h:mm a", locale).format(Date(millis))
}
