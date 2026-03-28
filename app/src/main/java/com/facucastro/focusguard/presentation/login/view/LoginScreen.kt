package com.facucastro.focusguard.presentation.login.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.facucastro.focusguard.presentation.core.component.FocusGuardButton
import com.facucastro.focusguard.presentation.core.component.FocusGuardButtonVariant
import com.facucastro.focusguard.presentation.core.component.LoadingComponent
import com.facucastro.focusguard.presentation.login.contract.LoginContract
import com.facucastro.focusguard.presentation.login.viewModel.LoginViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = viewModel(),
) {
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val errorMessage = (viewState as? LoginContract.State.Error)?.message
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                LoginContract.Effect.NavigateToHome -> {
                    // Handled in MainActivity
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            LoginContent(
                isLoading = viewState is LoginContract.State.Loading,
                onGoogleClick = { viewModel.handleIntent(LoginContract.Intent.SignInWithGoogleClicked) },
                onAnonymousClick = { viewModel.handleIntent(LoginContract.Intent.SignInAnonymously) },
            )

            if (viewState is LoginContract.State.Loading) {
                LoadingComponent(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
internal fun LoginContent(
    isLoading: Boolean,
    onGoogleClick: () -> Unit,
    onAnonymousClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(120.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = CircleShape,
                ),
        ) {
            Icon(
                imageVector = Icons.Filled.Shield,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp),
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "FocusGuard",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Your sanctuary for distraction-free work.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(48.dp))

        FeatureCard(
            icon = Icons.Filled.Shield,
            title = "Obsidian Shield",
            description = "Block distractions and stay in your flow state.",
        )
        Spacer(Modifier.height(12.dp))
        FeatureCard(
            icon = Icons.Filled.AutoAwesome,
            title = "Heroic Progress",
            description = "Track your focus sessions and level up your productivity.",
        )

        Spacer(Modifier.height(48.dp))

        FocusGuardButton(
            text = if (isLoading) "Loading..." else "Continue with Google",
            onClick = onGoogleClick,
            enabled = !isLoading,
            icon = Icons.Filled.AccountCircle,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        FocusGuardButton(
            text = if (isLoading) "Loading..." else "Continue as Guest",
            onClick = onAnonymousClick,
            enabled = !isLoading,
            variant = FocusGuardButtonVariant.Outlined,
            icon = Icons.Filled.Person,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
