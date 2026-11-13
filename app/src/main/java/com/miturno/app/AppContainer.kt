package com.miturno.app

import android.content.Context
import com.miturno.app.data.SessionManager
import com.miturno.app.data.local.AppDatabase
import com.miturno.app.data.local.DemoData
import com.miturno.app.data.model.Role
import com.miturno.app.data.remote.AppConfig
import com.miturno.app.data.remote.RetrofitProvider
import com.miturno.app.data.repository.RemoteTurnRepository
import com.miturno.app.data.repository.RoomTurnRepository
import com.miturno.app.data.repository.TurnRepository
import com.miturno.app.data.repository.observeProgress
import com.miturno.app.notifications.TurnNotifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class AppContainer(context: Context) {

    val session = SessionManager(context)
    val notifier = TurnNotifier(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val database: AppDatabase? =
        if (AppConfig.USE_REMOTE_API) null else AppDatabase.build(context)

    val repository: TurnRepository =
        database?.let { RoomTurnRepository(it) } ?: RemoteTurnRepository(RetrofitProvider.api)

    init {
        notifier.createChannel()
        database?.let { db -> scope.launch { DemoData.seedIfEmpty(db) } }

        // Vigila el turno del afiliado con sesión activa y lanza las notificaciones (RF-07)
        scope.launch {
            session.state
                .flatMapLatest { s ->
                    if (s != null && s.role == Role.AFFILIATE) repository.observeProgress(s.userId) else emptyFlow()
                }
                .collect { notifier.onProgress(it) }
        }
    }
}
