package com.facucastro.focusguard.tests.domain.usecase

import com.facucastro.focusguard.domain.usecase.GetGoogleIdTokenUseCase
import com.facucastro.focusguard.providers.domain.auth.providesMockGoogleCredentialProvider
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetGoogleIdTokenUseCaseTest {

    @Test
    fun `GIVEN context AND provider returns success WHEN invoke THEN returns success with token`() = runTest {
        // GIVEN
        val token = "token"
        val provider = providesMockGoogleCredentialProvider(idTokenResult = Result.success(token))
        val useCase = GetGoogleIdTokenUseCase(provider)

        // WHEN
        val result = useCase()

        // THEN
        assertTrue(result.isSuccess)
        assertEquals(token, result.getOrNull())
        coVerify { provider.getGoogleIdToken() }
    }

    @Test
    fun `GIVEN context AND provider returns failure WHEN invoke THEN returns failure`() = runTest {
        // GIVEN
        val exception = RuntimeException("Credential error")
        val provider = providesMockGoogleCredentialProvider(idTokenResult = Result.failure(exception))
        val useCase = GetGoogleIdTokenUseCase(provider)

        // WHEN
        val result = useCase()

        // THEN
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify { provider.getGoogleIdToken() }
    }
}
