package com.miturno.app.ui.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miturno.app.data.SessionManager
import com.miturno.app.data.model.User
import com.miturno.app.data.repository.TurnRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(val loading: Boolean = false, val error: String? = null)

class AuthViewModel(
    private val repo: TurnRepository,
    private val session: SessionManager
) : ViewModel() {

    private val _ui = MutableStateFlow(AuthUiState())
    val ui: StateFlow<AuthUiState> = _ui

    fun clearError() {
        _ui.value = _ui.value.copy(error = null)
    }

    /** RF-01 (afiliado) y RF-10 (funcionario). */
    fun login(identifier: String, password: String, role: String, onSuccess: (User) -> Unit) {
        if (identifier.isBlank() || password.isBlank()) {
            _ui.value = AuthUiState(error = "Completa todos los campos")
            return
        }
        viewModelScope.launch {
            _ui.value = AuthUiState(loading = true)
            repo.login(identifier, password, role)
                .onSuccess { user ->
                    session.save(user)
                    _ui.value = AuthUiState()
                    onSuccess(user)
                }
                .onFailure { _ui.value = AuthUiState(error = it.message ?: "No se pudo iniciar sesión") }
        }
    }

    fun register(
        fullName: String, document: String, email: String, affiliation: String,
        password: String, confirm: String, acceptedData: Boolean,
        onSuccess: (User) -> Unit
    ) {
        val error = when {
            listOf(fullName, document, email, affiliation, password).any { it.isBlank() } ->
                "Completa todos los campos"
            document.trim().length < 5 -> "El documento no es válido"
            !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> "El correo no es válido"
            password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            password != confirm -> "Las contraseñas no coinciden"
            !acceptedData -> "Debes autorizar el tratamiento de tus datos para crear la cuenta"
            else -> null
        }
        if (error != null) {
            _ui.value = AuthUiState(error = error)
            return
        }
        viewModelScope.launch {
            _ui.value = AuthUiState(loading = true)
            repo.register(fullName, document, email, affiliation, password)
                .onSuccess { user ->
                    session.save(user)
                    _ui.value = AuthUiState()
                    onSuccess(user)
                }
                .onFailure { _ui.value = AuthUiState(error = it.message ?: "No se pudo crear la cuenta") }
        }
    }
}
