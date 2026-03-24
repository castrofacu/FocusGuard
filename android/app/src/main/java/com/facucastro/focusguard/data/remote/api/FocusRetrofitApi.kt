package com.facucastro.focusguard.data.remote.api

import com.facucastro.focusguard.data.remote.dto.FocusSessionDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface FocusRetrofitApi {

    @POST("sessions")
    suspend fun createSession(@Body dto: FocusSessionDto): FocusSessionDto

    @GET("sessions")
    suspend fun getSessions(): List<FocusSessionDto>

    @GET("sessions/{id}")
    suspend fun getSessionById(@Path("id") id: Long): FocusSessionDto
}
