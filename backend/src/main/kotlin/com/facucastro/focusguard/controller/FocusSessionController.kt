package com.facucastro.focusguard.controller

import com.facucastro.focusguard.dto.FocusSessionDto
import com.facucastro.focusguard.service.FocusSessionService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/sessions")
class FocusSessionController(
    private val service: FocusSessionService
) {

    @PostMapping
    fun createSession(
        @Valid @RequestBody dto: FocusSessionDto
    ): ResponseEntity<FocusSessionDto> {
        val created = service.createSession(dto)
        return ResponseEntity
            .created(URI.create("/sessions/${created.id}"))
            .body(created)
    }

    @GetMapping
    fun getSessions(): ResponseEntity<List<FocusSessionDto>> =
        ResponseEntity.ok(service.getSessions())

    @GetMapping("{id}")
    fun getSessionById(@PathVariable id: Long): ResponseEntity<FocusSessionDto> =
        ResponseEntity.ok(service.getSessionById(id))
}
