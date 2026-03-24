package com.facucastro.focusguard.repository

import com.facucastro.focusguard.domain.FocusSession
import org.springframework.data.jpa.repository.JpaRepository

interface FocusSessionRepository : JpaRepository<FocusSession, Long>
