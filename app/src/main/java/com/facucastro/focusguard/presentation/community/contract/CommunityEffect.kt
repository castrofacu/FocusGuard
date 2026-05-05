package com.facucastro.focusguard.presentation.community.contract

sealed interface CommunityEffect {

    data class ShowErrorSnackbar(val message: String) : CommunityEffect
}
