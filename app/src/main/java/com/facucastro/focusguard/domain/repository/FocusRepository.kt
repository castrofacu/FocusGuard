package com.facucastro.focusguard.domain.repository

import com.facucastro.focusguard.domain.model.FocusSession
import kotlinx.coroutines.flow.Flow

interface FocusRepository {
    suspend fun saveSession(session: FocusSession): Result<Unit>
    fun getHistory(): Flow<List<FocusSession>>
}
