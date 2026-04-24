package com.facucastro.focusguard.domain.model

data class CommunityRanking(
    val rank: Int,
    val user: LeaderboardUser,
    val focusMinutesThisWeek: Int,
    val isCurrentlyInSession: Boolean,
)
