package com.miturno.app.ui.affiliate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miturno.app.data.SessionManager
import com.miturno.app.data.model.Turn
import com.miturno.app.data.repository.TurnRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

class HistoryViewModel(
    repo: TurnRepository,
    session: SessionManager
) : ViewModel() {

    /** RF-09. null = cargando. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val turns: StateFlow<List<Turn>?> = session.state
        .flatMapLatest { s -> if (s == null) flowOf(emptyList()) else repo.observeHistory(s.userId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
