package com.facucastro.focusguard.service

import com.facucastro.focusguard.dto.FocusSessionDto

interface FocusSessionService {
    fun createSession(dto: FocusSessionDto): FocusSessionDto
    fun getSessions(): List<FocusSessionDto>
    fun getSessionById(id: Long): FocusSessionDto
}
