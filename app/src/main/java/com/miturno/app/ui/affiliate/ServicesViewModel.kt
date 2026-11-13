package com.miturno.app.ui.affiliate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miturno.app.data.SessionManager
import com.miturno.app.data.model.Point
import com.miturno.app.data.model.ServiceItem
import com.miturno.app.data.repository.TurnRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ServicesUiState(
    val points: List<Point> = emptyList(),
    val selectedPointId: Long? = null,
    val services: List<ServiceItem> = emptyList(),
    val hasActiveTurn: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class ServicesViewModel(
    private val repo: TurnRepository,
    private val session: SessionManager
) : ViewModel() {

    private val userId: Long get() = session.state.value?.userId ?: -1L
    private val selected = MutableStateFlow<Long?>(null)

    private val points = repo.observePoints()
    private val effectivePoint = combine(points, selected) { list, sel -> sel ?: list.firstOrNull()?.id }

    private val services = effectivePoint.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repo.observeServices(id)
    }
    private val hasActive = repo.observeActiveTurn(userId).map { it != null }

    /** RF-02: servicios del punto seleccionado. */
    val ui: StateFlow<ServicesUiState> =
        combine(points, effectivePoint, services, hasActive) { pts, point, srv, active ->
            ServicesUiState(pts, point, srv, active)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ServicesUiState())

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun selectPoint(id: Long) {
        selected.value = id
    }

    fun clearError() {
        _error.value = null
    }

    /** RF-03 y RF-04: solicita el turno y entrega el número asignado. */
    fun requestTurn(service: ServiceItem, onSuccess: (String) -> Unit) {
        val uid = userId
        if (uid < 0) return
        viewModelScope.launch {
            _busy.value = true
            repo.requestTurn(uid, service.id, service.pointId)
                .onSuccess { onSuccess(it.number) }
                .onFailure { _error.value = it.message ?: "No se pudo solicitar el turno" }
            _busy.value = false
        }
    }
}
