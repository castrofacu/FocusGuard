package com.facucastro.focusguard.data.local

import com.facucastro.focusguard.domain.model.FocusSession
import kotlinx.coroutines.flow.Flow

interface LocalSessionDataSource {
    fun getSessions(): Flow<List<FocusSession>>
    suspend fun addSession(session: FocusSession)
    suspend fun getPendingSessions(): List<FocusSession>
    suspend fun markAsSynced(id: Long)
}
