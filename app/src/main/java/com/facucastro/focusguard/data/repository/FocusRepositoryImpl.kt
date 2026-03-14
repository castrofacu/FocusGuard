package com.facucastro.focusguard.data.repository

import com.facucastro.focusguard.data.local.LocalSessionDataSource
import com.facucastro.focusguard.data.remote.FocusApiService
import com.facucastro.focusguard.data.remote.dto.toDto
import com.facucastro.focusguard.domain.model.FocusSession
import com.facucastro.focusguard.domain.repository.FocusRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FocusRepositoryImpl @Inject constructor(
    private val dataStore: LocalSessionDataSource,
    private val apiService: FocusApiService
) : FocusRepository {

    override suspend fun saveSession(session: FocusSession): Result<Unit> {
        return runCatching {
            dataStore.addSession(session)
        }.onSuccess {
            apiService.createSession(session.toDto())
                .onSuccess {
                    dataStore.markAsSynced(session.id)
                }
        }
    }

    override fun getHistory(): Flow<List<FocusSession>> = dataStore.getSessions()
}
