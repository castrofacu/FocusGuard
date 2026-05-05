package com.facucastro.focusguard.domain.repository

import com.facucastro.focusguard.domain.model.CommunityRanking

interface CommunityRepository {
    suspend fun getWeeklyRanking(): Result<List<CommunityRanking>>
}
