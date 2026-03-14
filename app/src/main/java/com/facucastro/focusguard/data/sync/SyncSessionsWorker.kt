package com.facucastro.focusguard.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.facucastro.focusguard.data.local.LocalSessionDataSource
import com.facucastro.focusguard.data.remote.FocusApiService
import com.facucastro.focusguard.data.remote.dto.toDto
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncSessionsWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val dataSource: LocalSessionDataSource,
    private val apiService: FocusApiService
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val pendingSessions = try {
            dataSource.getPendingSessions()
        } catch (_: Exception) {
            return Result.retry()
        }

        var anyFailed = false

        pendingSessions.forEach { session ->
            try {
                apiService.createSession(session.toDto())
                    .onSuccess {
                        dataSource.markAsSynced(session.id)
                    }
                    .onFailure {
                        anyFailed = true
                    }
            } catch (_: Exception) {
                anyFailed = true
            }
        }

        return if (anyFailed) Result.retry() else Result.success()
    }
}