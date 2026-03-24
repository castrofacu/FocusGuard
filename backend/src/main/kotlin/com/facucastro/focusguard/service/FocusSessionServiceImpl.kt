package com.facucastro.focusguard.service

import com.facucastro.focusguard.dto.FocusSessionDto
import com.facucastro.focusguard.dto.toDto
import com.facucastro.focusguard.dto.toEntity
import com.facucastro.focusguard.repository.FocusSessionRepository
import org.springframework.stereotype.Service

@Service
class FocusSessionServiceImpl(
    private val repository: FocusSessionRepository

) : FocusSessionService {

    override fun createSession(dto: FocusSessionDto): FocusSessionDto {
        val entity = dto.toEntity()
        val saved = repository.save(entity)
        return saved.toDto()
    }
}
