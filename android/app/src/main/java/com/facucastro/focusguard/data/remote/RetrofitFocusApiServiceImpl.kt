package com.facucastro.focusguard.data.remote

import com.facucastro.focusguard.data.remote.api.FocusRetrofitApi
import com.facucastro.focusguard.data.remote.dto.FocusSessionDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RetrofitFocusApiServiceImpl @Inject constructor(
    private val api: FocusRetrofitApi
) : FocusApiService {

    override suspend fun createSession(dto: FocusSessionDto): Result<FocusSessionDto> =
        runCatching { api.createSession(dto) }

    override suspend fun getSessions(): Result<List<FocusSessionDto>> =
        runCatching { api.getSessions() }

    override suspend fun getSessionById(id: Long): Result<FocusSessionDto> =
        runCatching { api.getSessionById(id) }
}
