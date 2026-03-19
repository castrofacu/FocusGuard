package com.facucastro.focusguard.tests.presentation.login.viewModel

import com.facucastro.focusguard.presentation.login.contract.LoginContract
import com.facucastro.focusguard.providers.presentation.login.viewModel.providesLoginViewModel
import com.facucastro.focusguard.utils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    @Test
    fun `GIVEN viewModel WHEN initialized THEN state is Idle`() = runTest {
        val viewModel = providesLoginViewModel()
        Assert.assertEquals(LoginContract.State.Idle, viewModel.viewState.value)
    }

    @Test
    fun `GIVEN success result WHEN SignInAnonymously intent THEN state becomes Idle and NavigateToHome effect is sent`() =
        runTest {
            // GIVEN
            val viewModel = providesLoginViewModel(
                signInAnonymouslyResult = Result.success(Unit)
            )
            val effects = mutableListOf<LoginContract.Effect>()
            val job = launch { viewModel.effect.toList(effects) }

            // WHEN
            viewModel.handleIntent(LoginContract.Intent.SignInAnonymously)
            runCurrent()

            // THEN
            Assert.assertEquals(LoginContract.State.Idle, viewModel.viewState.value)
            Assert.assertEquals(listOf(LoginContract.Effect.NavigateToHome), effects)
            job.cancel()
        }

    @Test
    fun `GIVEN failure result WHEN SignInAnonymously intent THEN state becomes Error`() =
        runTest {
            // GIVEN
            val errorMessage = "Anonymous sign in failed"
            val viewModel = providesLoginViewModel(
                signInAnonymouslyResult = Result.failure(Exception(errorMessage))
            )

            // WHEN
            viewModel.handleIntent(LoginContract.Intent.SignInAnonymously)
            runCurrent()

            // THEN
            Assert.assertTrue(viewModel.viewState.value is LoginContract.State.Error)
            Assert.assertEquals(
                errorMessage,
                (viewModel.viewState.value as LoginContract.State.Error).message
            )
        }

    @Test
    fun `GIVEN success results WHEN SignInWithGoogleClicked intent THEN state becomes Idle and NavigateToHome effect is sent`() =
        runTest {
            // GIVEN
            val viewModel = providesLoginViewModel(
                idTokenResult = Result.success("valid_token"),
                signInWithGoogleResult = Result.success(Unit)
            )
            val effects = mutableListOf<LoginContract.Effect>()
            val job = launch { viewModel.effect.toList(effects) }

            // WHEN
            viewModel.handleIntent(LoginContract.Intent.SignInWithGoogleClicked)
            runCurrent()

            // THEN
            Assert.assertEquals(LoginContract.State.Idle, viewModel.viewState.value)
            Assert.assertEquals(listOf(LoginContract.Effect.NavigateToHome), effects)
            job.cancel()
        }

    @Test
    fun `GIVEN id token failure WHEN SignInWithGoogleClicked intent THEN state becomes Error`() =
        runTest {
            // GIVEN
            val errorMessage = "Google ID token error"
            val viewModel = providesLoginViewModel(
                idTokenResult = Result.failure(Exception(errorMessage))
            )

            // WHEN
            viewModel.handleIntent(LoginContract.Intent.SignInWithGoogleClicked)
            runCurrent()

            // THEN
            Assert.assertTrue(viewModel.viewState.value is LoginContract.State.Error)
            Assert.assertEquals(
                errorMessage,
                (viewModel.viewState.value as LoginContract.State.Error).message
            )
        }

    @Test
    fun `GIVEN sign in failure WHEN SignInWithGoogleClicked intent THEN state becomes Error`() =
        runTest {
            // GIVEN
            val errorMessage = "Google sign in error"
            val viewModel = providesLoginViewModel(
                signInWithGoogleResult = Result.failure(Exception(errorMessage))
            )

            // WHEN
            viewModel.handleIntent(LoginContract.Intent.SignInWithGoogleClicked)
            runCurrent()

            // THEN
            Assert.assertTrue(viewModel.viewState.value is LoginContract.State.Error)
            Assert.assertEquals(
                errorMessage,
                (viewModel.viewState.value as LoginContract.State.Error).message
            )
        }
}
