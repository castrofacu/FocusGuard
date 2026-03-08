package com.facucastro.focusguard.data.remote

import com.facucastro.focusguard.data.remote.dto.FocusSessionDto
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

// Fake implementation of FocusApiService for development purposes
@Singleton
class FakeFocusApiServiceImpl @Inject constructor() : FocusApiService {

    override suspend fun createSession(dto: FocusSessionDto): Result<FocusSessionDto> {
        delay(500)
        return Result.success(dto)
    }

    override suspend fun getSessions(): Result<List<FocusSessionDto>> {
        delay(500)
        return Result.success(emptyList())
    }

    override suspend fun getSessionById(id: Long): Result<FocusSessionDto> {
        delay(500)
        return Result.success(FocusSessionDto(id = id, startTime = 0, durationSeconds = 0, distractionCount = 0))
    }
}
