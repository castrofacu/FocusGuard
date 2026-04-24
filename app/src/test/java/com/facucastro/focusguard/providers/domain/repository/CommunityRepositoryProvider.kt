package com.facucastro.focusguard.providers.domain.repository

import com.facucastro.focusguard.domain.model.CommunityRanking
import com.facucastro.focusguard.domain.model.LeaderboardUser
import com.facucastro.focusguard.domain.repository.CommunityRepository

class FakeCommunityRepository(
    var rankingResult: Result<List<CommunityRanking>> = Result.success(fakeCommunityRankings),
) : CommunityRepository {
    override suspend fun getWeeklyRanking(): Result<List<CommunityRanking>> = rankingResult
}

val fakeCommunityRankings: List<CommunityRanking> = listOf(
    CommunityRanking(
        rank = 1,
        user = LeaderboardUser(id = "u1", displayName = "Alice", avatarUrl = null),
        focusMinutesThisWeek = 300,
        isCurrentlyInSession = true,
    ),
    CommunityRanking(
        rank = 2,
        user = LeaderboardUser(id = "u2", displayName = "Bob", avatarUrl = null),
        focusMinutesThisWeek = 240,
        isCurrentlyInSession = false,
    ),
    CommunityRanking(
        rank = 3,
        user = LeaderboardUser(id = "u3", displayName = "Carol", avatarUrl = null),
        focusMinutesThisWeek = 180,
        isCurrentlyInSession = false,
    ),
)

fun providesFakeCommunityRepository(
    rankingResult: Result<List<CommunityRanking>> = Result.success(fakeCommunityRankings),
): FakeCommunityRepository = FakeCommunityRepository(rankingResult)
