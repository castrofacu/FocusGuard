package com.facucastro.focusguard.data.local

import com.facucastro.focusguard.data.local.db.SessionDao
import com.facucastro.focusguard.data.local.db.toDomain
import com.facucastro.focusguard.data.local.db.toEntity
import com.facucastro.focusguard.domain.model.FocusSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomSessionDataSource @Inject constructor(
    private val dao: SessionDao
) : LocalSessionDataSource {

    override fun getSessions(): Flow<List<FocusSession>> =
        dao.getAllSessions().map { entities -> entities.map { it.toDomain() } }

    override suspend fun addSession(session: FocusSession) {
        dao.insert(session.toEntity())
    }

    override suspend fun getPendingSessions(): List<FocusSession> =
        dao.getPendingSessions().map { it.toDomain() }

    override suspend fun markAsSynced(id: Long) {
        dao.markAsSynced(id)
    }
}
