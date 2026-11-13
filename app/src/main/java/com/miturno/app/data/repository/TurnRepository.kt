package com.miturno.app.data.repository

import com.miturno.app.data.model.Point
import com.miturno.app.data.model.Progress
import com.miturno.app.data.model.ServiceItem
import com.miturno.app.data.model.Turn
import com.miturno.app.data.model.TurnStatus
import com.miturno.app.data.model.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

interface TurnRepository {
    // Autenticación (RF-01, RF-10)
    suspend fun login(identifier: String, password: String, role: String): Result<User>
    suspend fun register(
        fullName: String, document: String, email: String,
        affiliationNumber: String, password: String
    ): Result<User>

    // Afiliado
    fun observePoints(): Flow<List<Point>>                                   // RF-02
    fun observeServices(pointId: Long): Flow<List<ServiceItem>>              // RF-02
    suspend fun requestTurn(userId: Long, serviceId: Long, pointId: Long): Result<Turn>  // RF-03, RF-04
    fun observeActiveTurn(userId: Long): Flow<Turn?>
    fun observeCurrentTurn(pointId: Long): Flow<Turn?>                       // RF-05
    fun observeQueue(pointId: Long): Flow<List<Turn>>                        // RF-06, RF-11
    fun observeHistory(userId: Long): Flow<List<Turn>>                       // RF-09
    suspend fun cancelTurn(turnId: Long): Result<Unit>                       // RF-08

    // Funcionario
    suspend fun callNext(pointId: Long): Result<Turn>                        // RF-12
    suspend fun finishTurn(turnId: Long): Result<Unit>                       // RF-13
    suspend fun skipTurn(turnId: Long): Result<Unit>                         // RF-14
}
@OptIn(ExperimentalCoroutinesApi::class)
fun TurnRepository.observeProgress(userId: Long): Flow<Progress> =
    observeActiveTurn(userId).flatMapLatest { turn ->
        if (turn == null) {
            flowOf(Progress())
        } else {
            combine(observeCurrentTurn(turn.pointId), observeQueue(turn.pointId)) { current, queue ->
                val ahead = if (turn.status == TurnStatus.PENDING) {
                    queue.indexOfFirst { it.id == turn.id }.coerceAtLeast(0)
                } else 0
                Progress(turn = turn, current = current, ahead = ahead)
            }
        }
    }
