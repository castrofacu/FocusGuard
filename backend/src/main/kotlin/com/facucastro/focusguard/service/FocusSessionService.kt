package com.facucastro.focusguard.service

import com.facucastro.focusguard.dto.FocusSessionDto

interface FocusSessionService {
    fun createSession(dto: FocusSessionDto): FocusSessionDto
}
