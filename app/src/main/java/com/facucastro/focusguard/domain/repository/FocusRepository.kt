package com.facucastro.focusguard.domain.repository

import com.facucastro.focusguard.domain.model.FocusSession
import kotlinx.coroutines.flow.Flow

interface FocusRepository {
    suspend fun startSession(): FocusSession
    suspend fun stopSession(id: Long, distractionCount: Int)
    fun getHistory(): Flow<List<FocusSession>>
    suspend fun getSessionById(id: Long): FocusSession?
}
