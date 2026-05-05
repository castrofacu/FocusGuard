package com.facucastro.focusguard.presentation.community.contract

import com.facucastro.focusguard.domain.model.CommunityRanking

data class CommunityState(
    val isLoading: Boolean = false,
    val rankings: List<CommunityRanking> = emptyList(),
    val errorMessage: String? = null,
)
