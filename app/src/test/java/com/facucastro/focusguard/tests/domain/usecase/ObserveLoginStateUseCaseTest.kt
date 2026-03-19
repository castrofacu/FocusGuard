package com.facucastro.focusguard.tests.domain.usecase

import com.facucastro.focusguard.domain.usecase.ObserveLoginStateUseCase
import com.facucastro.focusguard.providers.domain.repository.providesMockAuthRepository
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class ObserveLoginStateUseCaseTest {

    @Test
    fun `GIVEN repository returns true WHEN invoke THEN returns true flow`() = runTest {
        // GIVEN
        val repository = providesMockAuthRepository(isUserLoggedIn = flowOf(true))
        val useCase = ObserveLoginStateUseCase(repository)

        // WHEN
        val result = useCase().first()

        // THEN
        assertTrue(result)
        verify { repository.isUserLoggedIn }
    }

    @Test
    fun `GIVEN repository returns false WHEN invoke THEN returns false flow`() = runTest {
        // GIVEN
        val repository = providesMockAuthRepository(isUserLoggedIn = flowOf(false))
        val useCase = ObserveLoginStateUseCase(repository)

        // WHEN
        val result = useCase().first()

        // THEN
        assertTrue(!result)
        verify { repository.isUserLoggedIn }
    }
}
