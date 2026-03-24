package com.facucastro.focusguard.domain

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "focus_sessions")
class FocusSession(

    @Id
    val id: Long,

    val startTime: Long,

    val durationSeconds: Int,

    val distractionCount: Int
)
