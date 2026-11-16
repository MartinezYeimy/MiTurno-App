package com.miturno.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.miturno.app.data.model.Point
import com.miturno.app.data.model.ServiceItem
import com.miturno.app.data.model.Turn
import com.miturno.app.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // ---------- Puntos y servicios ----------
    @Query("SELECT * FROM points ORDER BY name")
    fun observePoints(): Flow<List<Point>>

    @Query("SELECT COUNT(*) FROM points")
    suspend fun countPoints(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoints(items: List<Point>)

    @Query("SELECT * FROM services WHERE pointId = :pointId ORDER BY name")
    fun observeServices(pointId: Long): Flow<List<ServiceItem>>

    @Query("SELECT * FROM services WHERE id = :id")
    suspend fun getService(id: Long): ServiceItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServices(items: List<ServiceItem>)

    // ---------- Usuarios ----------
    @Insert
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUser(id: Long): User?

    @Query(
        """SELECT * FROM users
           WHERE role = :role
             AND (document = :identifier OR affiliationNumber = :identifier
                  OR username = :identifier OR LOWER(email) = LOWER(:identifier))
           LIMIT 1"""
    )
    suspend fun findUser(identifier: String, role: String): User?

    @Query(
        """SELECT COUNT(*) FROM users
           WHERE role = 'AFILIADO'
             AND (document = :document OR LOWER(email) = LOWER(:email)
                  OR affiliationNumber = :affiliation)"""
    )
    suspend fun countDuplicates(document: String, email: String, affiliation: String): Int

    // ---------- Turnos ----------
    @Insert
    suspend fun insertTurn(turn: Turn): Long

    @Update
    suspend fun updateTurn(turn: Turn)

    @Query("SELECT * FROM turns WHERE id = :id")
    suspend fun getTurn(id: Long): Turn?

    @Query("SELECT COALESCE(MAX(consecutive), 0) FROM turns WHERE serviceId = :serviceId AND requestedAt >= :since")
    suspend fun lastConsecutive(serviceId: Long, since: Long): Int

    @Query("SELECT COUNT(*) FROM turns WHERE userId = :userId AND status IN ('PENDIENTE', 'EN_ATENCION')")
    suspend fun activeCount(userId: Long): Int

    @Query(
        """SELECT * FROM turns WHERE userId = :userId AND status IN ('PENDIENTE', 'EN_ATENCION')
           ORDER BY requestedAt DESC LIMIT 1"""
    )
    fun observeActiveTurn(userId: Long): Flow<Turn?>

    @Query("SELECT * FROM turns WHERE pointId = :pointId AND status = 'EN_ATENCION' ORDER BY calledAt DESC LIMIT 1")
    fun observeCurrent(pointId: Long): Flow<Turn?>

    @Query("SELECT * FROM turns WHERE pointId = :pointId AND status = 'EN_ATENCION' ORDER BY calledAt DESC LIMIT 1")
    suspend fun getCurrent(pointId: Long): Turn?

    @Query("SELECT * FROM turns WHERE pointId = :pointId AND status = 'PENDIENTE' ORDER BY requestedAt ASC, id ASC")
    fun observeQueue(pointId: Long): Flow<List<Turn>>

    @Query("SELECT * FROM turns WHERE pointId = :pointId AND status = 'PENDIENTE' ORDER BY requestedAt ASC, id ASC LIMIT 1")
    suspend fun getNextPending(pointId: Long): Turn?

    @Query("SELECT * FROM turns WHERE userId = :userId ORDER BY requestedAt DESC")
    fun observeHistory(userId: Long): Flow<List<Turn>>
}
