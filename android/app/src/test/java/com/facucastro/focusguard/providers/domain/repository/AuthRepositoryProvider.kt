package com.facucastro.focusguard.providers.domain.repository

import com.facucastro.focusguard.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

fun providesMockAuthRepository(
    currentUserEmail: String? = null,
    isAnonymous: Boolean = false,
    isUserLoggedIn: Flow<Boolean> = flowOf(false),
    signInAnonymouslyResult: Result<Unit> = Result.success(Unit),
    signInWithGoogleResult: Result<Unit> = Result.success(Unit),
    signOutResult: Result<Unit> = Result.success(Unit)
): AuthRepository {
    return mockk<AuthRepository> {
        every { this@mockk.currentUserEmail } returns currentUserEmail
        every { this@mockk.isAnonymous } returns isAnonymous
        every { this@mockk.isUserLoggedIn } returns isUserLoggedIn
        coEvery { signInAnonymously() } returns signInAnonymouslyResult
        coEvery { signInWithGoogle(any()) } returns signInWithGoogleResult
        coEvery { signOut() } returns signOutResult
    }
}
