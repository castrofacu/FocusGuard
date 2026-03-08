package com.facucastro.focusguard.data.remote

import com.facucastro.focusguard.data.remote.dto.FocusSessionDto

interface FocusApiService {
    suspend fun createSession(dto: FocusSessionDto): Result<FocusSessionDto>
    suspend fun getSessions(): Result<List<FocusSessionDto>>
    suspend fun getSessionById(id: Long): Result<FocusSessionDto>
}
