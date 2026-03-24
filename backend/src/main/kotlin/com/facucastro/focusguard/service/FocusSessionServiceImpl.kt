package com.facucastro.focusguard.service

import com.facucastro.focusguard.dto.FocusSessionDto
import com.facucastro.focusguard.dto.toDto
import com.facucastro.focusguard.dto.toEntity
import com.facucastro.focusguard.exception.SessionNotFoundException
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

    override fun getSessions(): List<FocusSessionDto> =
        repository.findAll().map { it.toDto() }

    override fun getSessionById(id: Long): FocusSessionDto =
        repository.findById(id)
            .orElseThrow { SessionNotFoundException(id) }
            .toDto()
}
