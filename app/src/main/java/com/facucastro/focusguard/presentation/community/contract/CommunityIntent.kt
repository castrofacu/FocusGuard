package com.facucastro.focusguard.presentation.community.contract

sealed interface CommunityIntent {

    data object LoadRanking : CommunityIntent

    data object RetryClicked : CommunityIntent
}
