package com.facucastro.focusguard.presentation.community

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facucastro.focusguard.presentation.community.contract.CommunityEffect
import com.facucastro.focusguard.presentation.community.view.CommunityContent
import com.facucastro.focusguard.presentation.community.viewModel.CommunityViewModel

@Composable
fun CommunityScreen(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: CommunityViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is CommunityEffect.ShowErrorSnackbar -> {
                    snackbarHostState.showSnackbar(message = effect.message)
                }
            }
        }
    }

    CommunityContent(
        state = state,
        onIntent = viewModel::handleIntent,
        modifier = modifier,
    )
}
