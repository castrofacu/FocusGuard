package com.facucastro.focusguard.domain

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "focus_sessions")
class FocusSession(
    @Id
    var id: Long,
    var startTime: Long,
    var durationSeconds: Int,
    var distractionCount: Int
)
