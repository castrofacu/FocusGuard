package com.facucastro.focusguard.presentation.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.facucastro.focusguard.presentation.core.LoadingComponent
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    googleAuthUiClient: GoogleAuthUiClient,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = viewModel(),
) {
    val viewState by viewModel.viewState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Centralizamos el disparador de Google
    val onGoogleClick = {
        scope.launch {
            googleAuthUiClient.signIn(context)
                .onSuccess { token ->
                    viewModel.handleIntent(LoginContract.Intent.SignInWithGoogle(token))
                }
                .onFailure {
                    viewModel.handleIntent(LoginContract.Intent.ErrorDisplayed)
                }
        }
        Unit
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is LoginContract.Effect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
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
        // UI Única con overlays
        Box(modifier = Modifier.padding(padding)) {
            LoginContent(
                isLoading = viewState is LoginContract.State.Loading,
                onGoogleClick = onGoogleClick,
                onAnonymousClick = { viewModel.handleIntent(LoginContract.Intent.SignInAnonymously) },
                // Si el estado es Error, pasamos el mensaje, sino null
                errorMessage = (viewState as? LoginContract.State.Error)?.message
            )

            if (viewState is LoginContract.State.Loading) {
                LoadingComponent() // Un overlay transparente con el indicator
            }
        }
    }
}

@Composable
private fun LoginContent(
    isLoading: Boolean,
    onGoogleClick: () -> Unit,
    onAnonymousClick: () -> Unit,
    modifier: Modifier = Modifier,
    errorMessage: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "FocusGuard",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Protege tu enfoque",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onGoogleClick,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                Text("Cargando...")
            } else {
                Text("Iniciar sesión con Google")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onAnonymousClick,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continuar como invitado")
        }

        // El error lo mostramos solo si existe
        errorMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
