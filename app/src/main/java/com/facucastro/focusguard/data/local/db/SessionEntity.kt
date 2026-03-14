package com.facucastro.focusguard.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_sessions")
data class SessionEntity(
    @PrimaryKey val id: Long,
    val startTime: Long,
    val durationSeconds: Int,
    val distractionCount: Int,
    val isSynced: Boolean = false
)
