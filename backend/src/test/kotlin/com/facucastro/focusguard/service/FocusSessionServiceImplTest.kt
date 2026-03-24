package com.facucastro.focusguard.service

import com.facucastro.focusguard.exception.SessionNotFoundException
import com.facucastro.focusguard.fixtures.FocusSessionFixtures
import com.facucastro.focusguard.repository.FocusSessionRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Optional
import kotlin.test.assertEquals

class FocusSessionServiceImplTest {

    private val repository: FocusSessionRepository = mockk()
    private val service = FocusSessionServiceImpl(repository)

    private val entity = FocusSessionFixtures.defaultEntity()
    private val dto = FocusSessionFixtures.defaultDto()

    @Test
    fun `given a valid dto, when createSession is called, then it saves the entity and returns the dto`() {
        // Given
        every { repository.save(any()) } returns entity

        // When
        val result = service.createSession(dto)

        // Then
        assertEquals(dto.id, result.id)
        assertEquals(dto.startTime, result.startTime)
        assertEquals(dto.durationSeconds, result.durationSeconds)
        assertEquals(dto.distractionCount, result.distractionCount)
        verify(exactly = 1) { repository.save(any()) }
    }

    @Test
    fun `given existing sessions in the repository, when getSessions is called, then it returns a list of dtos`() {
        // Given
        every { repository.findAll() } returns listOf(entity)

        // When
        val result = service.getSessions()

        // Then
        assertEquals(1, result.size)
        assertEquals(entity.id, result[0].id)
        verify(exactly = 1) { repository.findAll() }
    }

    @Test
    fun `given no sessions in the repository, when getSessions is called, then it returns an empty list`() {
        // Given
        every { repository.findAll() } returns emptyList()

        // When
        val result = service.getSessions()

        // Then
        assertEquals(0, result.size)
    }

    @Test
    fun `given an existing session, when getSessionById is called, then it returns the corresponding dto`() {
        // Given
        every { repository.findById(1L) } returns Optional.of(entity)

        // When
        val result = service.getSessionById(1L)

        // Then
        assertEquals(entity.id, result.id)
        verify(exactly = 1) { repository.findById(1L) }
    }

    @Test
    fun `given no session with the given id, when getSessionById is called, then it throws SessionNotFoundException`() {
        // Given
        every { repository.findById(99L) } returns Optional.empty()

        // When / Then
        assertThrows<SessionNotFoundException> {
            service.getSessionById(99L)
        }
    }
}
