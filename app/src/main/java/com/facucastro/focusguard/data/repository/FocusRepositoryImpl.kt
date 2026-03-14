package com.facucastro.focusguard.data.repository

import com.facucastro.focusguard.data.local.LocalSessionDataSource
import com.facucastro.focusguard.data.sync.SyncWorkScheduler
import com.facucastro.focusguard.domain.model.FocusSession
import com.facucastro.focusguard.domain.repository.FocusRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FocusRepositoryImpl @Inject constructor(
    private val localDataSource: LocalSessionDataSource,
    private val syncWorkScheduler: SyncWorkScheduler
) : FocusRepository {

    override suspend fun saveSession(session: FocusSession): Result<Unit> {
        return runCatching {
            localDataSource.addSession(session)
        }.onSuccess {
            syncWorkScheduler.enqueueSync()
        }
    }

    override fun getHistory(): Flow<List<FocusSession>> = localDataSource.getSessions()
}
