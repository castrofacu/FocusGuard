package com.facucastro.focusguard.data.repository

import com.facucastro.focusguard.data.local.LocalSessionDataSource
import com.facucastro.focusguard.data.sync.SyncWorkScheduler
import com.facucastro.focusguard.domain.model.FocusSession
import com.facucastro.focusguard.domain.repository.FocusRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FocusRepositoryImpl @Inject constructor(
    private val localDataSource: LocalSessionDataSource,
    private val syncWorkScheduler: SyncWorkScheduler
) : FocusRepository {

    override suspend fun saveSession(session: FocusSession): Result<Unit> {
        return try {
            localDataSource.addSession(session)
            syncWorkScheduler.enqueueSync()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getHistory(): Flow<List<FocusSession>> = localDataSource.getSessions()
}
