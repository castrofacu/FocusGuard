package com.facucastro.focusguard.presentation.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface MainTab : NavKey {

    @Serializable
    data object Home : MainTab

    @Serializable
    data object History : MainTab

    @Serializable
    data object Community : MainTab
}
