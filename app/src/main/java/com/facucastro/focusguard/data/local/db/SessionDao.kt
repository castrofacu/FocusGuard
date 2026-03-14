package com.facucastro.focusguard.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SessionEntity)

    @Query("SELECT * FROM focus_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE isSynced = 0")
    suspend fun getPendingSessions(): List<SessionEntity>

    @Query("UPDATE focus_sessions SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: Long)
}
