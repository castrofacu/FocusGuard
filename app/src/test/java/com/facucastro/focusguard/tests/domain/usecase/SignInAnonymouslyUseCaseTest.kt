package com.facucastro.focusguard.tests.domain.usecase

import com.facucastro.focusguard.domain.usecase.SignInAnonymouslyUseCase
import com.facucastro.focusguard.providers.domain.repository.providesMockAuthRepository
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SignInAnonymouslyUseCaseTest {

    @Test
    fun `GIVEN repository returns success WHEN invoke THEN returns success`() = runTest {
        // GIVEN
        val repository = providesMockAuthRepository(signInAnonymouslyResult = Result.success(Unit))
        val useCase = SignInAnonymouslyUseCase(repository)

        // WHEN
        val result = useCase()

        // THEN
        assertTrue(result.isSuccess)
        coVerify { repository.signInAnonymously() }
    }

    @Test
    fun `GIVEN repository returns failure WHEN invoke THEN returns failure`() = runTest {
        // GIVEN
        val exception = RuntimeException("Login failed")
        val repository = providesMockAuthRepository(signInAnonymouslyResult = Result.failure(exception))
        val useCase = SignInAnonymouslyUseCase(repository)

        // WHEN
        val result = useCase()

        // THEN
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify { repository.signInAnonymously() }
    }
}
