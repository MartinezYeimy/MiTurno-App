package com.miturno.app.ui.affiliate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miturno.app.data.SessionManager
import com.miturno.app.data.model.Progress
import com.miturno.app.data.repository.TurnRepository
import com.miturno.app.data.repository.observeProgress
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repo: TurnRepository,
    private val session: SessionManager
) : ViewModel() {

    val firstName: String = session.state.value?.name?.trim()?.substringBefore(' ').orEmpty()

    /** null = todavía cargando. Se actualiza solo cuando el funcionario avanza la fila (RF-05, RF-06, RF-15). */
    @OptIn(ExperimentalCoroutinesApi::class)
    val progress: StateFlow<Progress?> = session.state
        .flatMapLatest { s -> if (s == null) flowOf(Progress()) else repo.observeProgress(s.userId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun clearMessage() {
        _message.value = null
    }

    /** RF-08 */
    fun cancelTurn(turnId: Long) {
        viewModelScope.launch {
            repo.cancelTurn(turnId)
                .onSuccess { _message.value = "Tu turno fue cancelado" }
                .onFailure { _message.value = it.message ?: "No se pudo cancelar el turno" }
        }
    }
}
