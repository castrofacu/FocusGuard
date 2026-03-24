package com.facucastro.focusguard.repository

import com.facucastro.focusguard.fixtures.FocusSessionFixtures
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@DataJpaTest
class FocusSessionRepositoryTest {

    @Autowired
    lateinit var repository: FocusSessionRepository

    private fun buildSession(id: Long) = FocusSessionFixtures.defaultEntity(
        id = id,
        startTime = 1700000000L + id,
        distractionCount = 0
    )

    @Test
    fun `given a valid session, when save and findById are called, then the session is persisted and retrieved`() {
        // Given
        val session = buildSession(1L)

        // When
        val saved = repository.save(session)
        val found = repository.findById(1L).orElse(null)

        // Then
        assertNotNull(found)
        assertEquals(saved.id, found.id)
        assertEquals(saved.startTime, found.startTime)
        assertEquals(saved.durationSeconds, found.durationSeconds)
        assertEquals(saved.distractionCount, found.distractionCount)
    }

    @Test
    fun `given no session with the given id, when findById is called, then it returns empty`() {
        // Given - empty repository

        // When
        val result = repository.findById(999L)

        // Then
        assertTrue(result.isEmpty)
    }

    @Test
    fun `given multiple saved sessions, when findAll is called, then all sessions are returned`() {
        // Given
        repository.save(buildSession(1L))
        repository.save(buildSession(2L))

        // When
        val all = repository.findAll()

        // Then
        assertEquals(2, all.size)
    }

    @Test
    fun `given no sessions in the repository, when findAll is called, then it returns an empty list`() {
        // Given - empty repository

        // When
        val all = repository.findAll()

        // Then
        assertEquals(0, all.size)
    }
}
