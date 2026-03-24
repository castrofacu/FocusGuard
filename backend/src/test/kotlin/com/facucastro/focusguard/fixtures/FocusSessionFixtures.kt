package com.facucastro.focusguard.fixtures

import com.facucastro.focusguard.domain.FocusSession
import com.facucastro.focusguard.dto.FocusSessionDto

object FocusSessionFixtures {

    fun defaultEntity(
        id: Long = 1L,
        startTime: Long = 1700000000L,
        durationSeconds: Int = 1500,
        distractionCount: Int = 2
    ) = FocusSession(
        id = id,
        startTime = startTime,
        durationSeconds = durationSeconds,
        distractionCount = distractionCount
    )

    fun defaultDto(
        id: Long = 1L,
        startTime: Long = 1700000000L,
        durationSeconds: Int = 1500,
        distractionCount: Int = 2
    ) = FocusSessionDto(
        id = id,
        startTime = startTime,
        durationSeconds = durationSeconds,
        distractionCount = distractionCount
    )

    val defaultDtoJson = """{"id":1,"startTime":1700000000,"durationSeconds":1500,"distractionCount":2}"""

    val invalidDtoJson = """{"id":-1,"startTime":-1,"durationSeconds":0,"distractionCount":-1}"""
}
