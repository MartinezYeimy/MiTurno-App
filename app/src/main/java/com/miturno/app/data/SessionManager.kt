package com.miturno.app.data

import android.content.Context
import com.miturno.app.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class Session(val userId: Long, val role: String, val name: String, val pointId: Long)

/** Guarda la sesión activa para que el usuario no tenga que iniciar sesión cada vez que abre la app. */
class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)
    private val _state = MutableStateFlow(read())
    val state: StateFlow<Session?> = _state

    fun save(user: User) {
        prefs.edit()
            .putLong("userId", user.id)
            .putString("role", user.role)
            .putString("name", user.fullName)
            .putLong("pointId", user.pointId)
            .apply()
        _state.value = read()
    }

    fun clear() {
        prefs.edit().clear().apply()
        _state.value = null
    }

    private fun read(): Session? {
        val id = prefs.getLong("userId", -1L)
        val role = prefs.getString("role", null)
        if (id < 0 || role == null) return null
        return Session(id, role, prefs.getString("name", "") ?: "", prefs.getLong("pointId", 0L))
    }
}
