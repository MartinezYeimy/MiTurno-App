package com.miturno.app.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.miturno.app.MainActivity
import com.miturno.app.R
import com.miturno.app.data.model.Progress
import com.miturno.app.data.model.TurnStatus
import com.miturno.app.data.remote.AppConfig
import com.miturno.app.util.WaitEstimate

/**
 * RF-07: avisa al afiliado cuando su turno está próximo (faltan 3 o menos) y cuando es llamado.
 * Son notificaciones locales; para recibirlas con la app cerrada se necesita un servidor + Firebase (FCM).
 */
class TurnNotifier(private val context: Context) {

    private val prefs = context.getSharedPreferences("notified_turns", Context.MODE_PRIVATE)

    fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Avance de tu turno", NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Avisos cuando tu turno está próximo o es llamado" }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    fun onProgress(progress: Progress) {
        val turn = progress.turn ?: return
        // Nivel 2 = ya te llamaron, nivel 1 = próximo, 0 = todavía falta
        val level = when {
            turn.status == TurnStatus.IN_SERVICE -> 2
            progress.ahead <= AppConfig.NOTIFY_THRESHOLD -> 1
            else -> 0
        }
        if (level == 0) return
        val key = "turn_${turn.id}"
        if (level <= prefs.getInt(key, 0)) return
        prefs.edit().putInt(key, level).apply()

        if (level == 2) {
            val where = if (turn.module.isBlank()) "al módulo de atención" else "a: ${turn.module}"
            show(turn.id.toInt(), "¡Es tu turno!", "Turno ${turn.number}: dirígete $where.")
        } else {
            val text = if (progress.ahead == 0) "Eres el siguiente. Prepárate."
            else {
                val faltan = if (progress.ahead == 1) "falta 1 turno" else "faltan ${progress.ahead} turnos"
                "Tu turno está próximo, $faltan. Tiempo estimado: ${WaitEstimate.text(progress.ahead).removePrefix("Aprox. ")}."
            }
            show(turn.id.toInt(), "Turno ${turn.number}", text)
        }
    }

    @SuppressLint("MissingPermission")
    private fun show(id: Int, title: String, text: String) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val intent = Intent(context, MainActivity::class.java)
            .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP }
        val pending = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(id, notification)
    }

    companion object {
        const val CHANNEL_ID = "turnos"
    }
}
