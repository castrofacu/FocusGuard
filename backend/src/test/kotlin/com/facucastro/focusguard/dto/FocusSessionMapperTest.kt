package com.facucastro.focusguard.dto

import com.facucastro.focusguard.fixtures.FocusSessionFixtures
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class FocusSessionMapperTest {

    private val entity = FocusSessionFixtures.defaultEntity()
    private val dto = FocusSessionFixtures.defaultDto()

    @Test
    fun `given a FocusSession entity, when toDto is called, then all fields are mapped correctly`() {
        // Given
        val session = entity

        // When
        val result = session.toDto()

        // Then
        assertEquals(session.id, result.id)
        assertEquals(session.startTime, result.startTime)
        assertEquals(session.durationSeconds, result.durationSeconds)
        assertEquals(session.distractionCount, result.distractionCount)
    }

    @Test
    fun `given a FocusSessionDto, when toEntity is called, then all fields are mapped correctly`() {
        // Given
        val sessionDto = dto

        // When
        val result = sessionDto.toEntity()

        // Then
        assertEquals(sessionDto.id, result.id)
        assertEquals(sessionDto.startTime, result.startTime)
        assertEquals(sessionDto.durationSeconds, result.durationSeconds)
        assertEquals(sessionDto.distractionCount, result.distractionCount)
    }

    @Test
    fun `given a FocusSession entity, when toDto and toEntity are chained, then original values are preserved`() {
        // Given
        val session = entity

        // When
        val result = session.toDto().toEntity()

        // Then
        assertEquals(session.id, result.id)
        assertEquals(session.startTime, result.startTime)
        assertEquals(session.durationSeconds, result.durationSeconds)
        assertEquals(session.distractionCount, result.distractionCount)
    }
}
