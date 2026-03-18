package com.facucastro.focusguard.providers.presentation.login

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import com.facucastro.focusguard.presentation.login.view.LoginContent

fun ComposeContentTestRule.providesLoginContent(
    isLoading: Boolean = false,
    onGoogleClick: () -> Unit = {},
    onAnonymousClick: () -> Unit = {}
) {
    setContent {
        LoginContent(
            isLoading = isLoading,
            onGoogleClick = onGoogleClick,
            onAnonymousClick = onAnonymousClick
        )
    }
}
