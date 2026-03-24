package com.facucastro.focusguard.controller

import com.facucastro.focusguard.exception.SessionNotFoundException
import com.facucastro.focusguard.fixtures.FocusSessionFixtures
import com.facucastro.focusguard.service.FocusSessionService
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@WebMvcTest(FocusSessionController::class)
class FocusSessionControllerTest {

    @TestConfiguration
    class MockConfig {
        @Bean
        fun focusSessionService(): FocusSessionService = mockk()
    }

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var service: FocusSessionService

    private val dto = FocusSessionFixtures.defaultDto()

    @BeforeEach
    fun setUp() {
        clearMocks(service)
    }

    @Test
    fun `given a valid session dto, when POST sessions is called, then it returns 201 with location header and body`() {
        // Given
        every { service.createSession(any()) } returns dto

        // When / Then
        mockMvc.post("/sessions") {
            contentType = MediaType.APPLICATION_JSON
            content = FocusSessionFixtures.defaultDtoJson
        }.andExpect {
            status { isCreated() }
            header { string("Location", "/sessions/1") }
            jsonPath("$.id") { value(1) }
            jsonPath("$.durationSeconds") { value(1500) }
        }
    }

    @Test
    fun `given an invalid session dto, when POST sessions is called, then it returns 400 with error details`() {
        // Given - invalid dto (negative values)

        // When / Then
        mockMvc.post("/sessions") {
            contentType = MediaType.APPLICATION_JSON
            content = FocusSessionFixtures.invalidDtoJson
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.status") { value(400) }
            jsonPath("$.message") { isString() }
        }
    }

    @Test
    fun `given existing sessions, when GET sessions is called, then it returns 200 with the list`() {
        // Given
        every { service.getSessions() } returns listOf(dto)

        // When / Then
        mockMvc.get("/sessions").andExpect {
            status { isOk() }
            jsonPath("$[0].id") { value(1) }
        }
    }

    @Test
    fun `given an existing session, when GET sessions by id is called, then it returns 200 with the session`() {
        // Given
        every { service.getSessionById(1L) } returns dto

        // When / Then
        mockMvc.get("/sessions/1").andExpect {
            status { isOk() }
            jsonPath("$.id") { value(1) }
            jsonPath("$.durationSeconds") { value(1500) }
        }
    }

    @Test
    fun `given no session with the given id, when GET sessions by id is called, then it returns 404 with error details`() {
        // Given
        every { service.getSessionById(99L) } throws SessionNotFoundException(99L)

        // When / Then
        mockMvc.get("/sessions/99").andExpect {
            status { isNotFound() }
            jsonPath("$.status") { value(404) }
            jsonPath("$.message") { isString() }
        }
    }
}
