package com.facucastro.focusguard.providers.presentation.community.viewModel

import com.facucastro.focusguard.domain.model.CommunityRanking
import com.facucastro.focusguard.presentation.community.viewModel.CommunityViewModel
import com.facucastro.focusguard.providers.domain.repository.FakeCommunityRepository
import com.facucastro.focusguard.providers.domain.repository.fakeCommunityRankings
import com.facucastro.focusguard.providers.domain.repository.providesFakeCommunityRepository

fun providesCommunityViewModel(
    rankingResult: Result<List<CommunityRanking>> = Result.success(fakeCommunityRankings),
): CommunityViewModel {
    return CommunityViewModel(
        communityRepository = providesFakeCommunityRepository(rankingResult),
    )
}
