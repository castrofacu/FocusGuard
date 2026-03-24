package com.facucastro.focusguard.tests.domain.usecase

import com.facucastro.focusguard.domain.usecase.SignInWithGoogleUseCase
import com.facucastro.focusguard.providers.domain.repository.providesMockAuthRepository
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SignInWithGoogleUseCaseTest {

    @Test
    fun `GIVEN idToken AND repository returns success WHEN invoke THEN returns success`() = runTest {
        // GIVEN
        val idToken = "token"
        val repository = providesMockAuthRepository(signInWithGoogleResult = Result.success(Unit))
        val useCase = SignInWithGoogleUseCase(repository)

        // WHEN
        val result = useCase(idToken)

        // THEN
        assertTrue(result.isSuccess)
        coVerify { repository.signInWithGoogle(idToken) }
    }

    @Test
    fun `GIVEN idToken AND repository returns failure WHEN invoke THEN returns failure`() = runTest {
        // GIVEN
        val idToken = "token"
        val exception = RuntimeException("Google sign in failed")
        val repository = providesMockAuthRepository(signInWithGoogleResult = Result.failure(exception))
        val useCase = SignInWithGoogleUseCase(repository)

        // WHEN
        val result = useCase(idToken)

        // THEN
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify { repository.signInWithGoogle(idToken) }
    }
}
