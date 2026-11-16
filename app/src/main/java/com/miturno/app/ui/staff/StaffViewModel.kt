package com.miturno.app.ui.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miturno.app.data.SessionManager
import com.miturno.app.data.model.Turn
import com.miturno.app.data.repository.TurnRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StaffViewModel(
    private val repo: TurnRepository,
    session: SessionManager
) : ViewModel() {

    private val pointId: Long = session.state.value?.pointId ?: 0L
    val staffName: String = session.state.value?.name.orEmpty()
    /** Módulo o ventanilla de este funcionario; se le informa al afiliado cuando lo llaman. */
    val module: String = session.state.value?.module.orEmpty().ifBlank { "Módulo 1" }

    val pointName: StateFlow<String> = repo.observePoints()
        .map { list -> list.firstOrNull { it.id == pointId }?.name ?: "Punto de atención" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "Punto de atención")

    /** Turno que se está atendiendo ahora. */
    val current: StateFlow<Turn?> = repo.observeCurrentTurn(pointId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** RF-11: fila de turnos pendientes en orden de llegada. */
    val queue: StateFlow<List<Turn>> = repo.observeQueue(pointId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun clearMessage() {
        _message.value = null
    }

    /** RF-12 */
    fun callNext() {
        viewModelScope.launch {
            repo.callNext(pointId, module)
                .onSuccess { _message.value = "Llamando al turno ${it.number} a $module" }
                .onFailure { _message.value = it.message ?: "No se pudo llamar el siguiente turno" }
        }
    }

    /** RF-13 */
    fun finish(turn: Turn) {
        viewModelScope.launch {
            repo.finishTurn(turn.id)
                .onSuccess { _message.value = "Turno marcado como atendido" }
                .onFailure { _message.value = it.message ?: "No se pudo marcar el turno" }
        }
    }

    /** RF-14 */
    fun skip(turn: Turn) {
        viewModelScope.launch {
            repo.skipTurn(turn.id)
                .onSuccess { _message.value = "Turno ${turn.number} omitido" }
                .onFailure { _message.value = it.message ?: "No se pudo omitir el turno" }
        }
    }
}
