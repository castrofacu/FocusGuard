package com.facucastro.focusguard.data.remote.graphql.mapper

import com.facucastro.focusguard.domain.model.CommunityRanking
import com.facucastro.focusguard.domain.model.LeaderboardUser
import com.facucastro.focusguard.graphql.GetWeeklyRankingQuery

fun GetWeeklyRankingQuery.GetWeeklyRanking.toDomain(): CommunityRanking {
    return CommunityRanking(
        rank = rank,
        user = user.toDomain(),
        focusMinutesThisWeek = focusMinutesThisWeek,
        isCurrentlyInSession = isCurrentlyInSession,
    )
}

private fun GetWeeklyRankingQuery.User.toDomain(): LeaderboardUser {
    return LeaderboardUser(
        id = id,
        displayName = displayName,
        avatarUrl = avatarUrl,
    )
}
