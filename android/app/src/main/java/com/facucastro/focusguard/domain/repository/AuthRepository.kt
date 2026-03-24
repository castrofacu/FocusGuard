package com.facucastro.focusguard.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUserEmail: String?
    val isAnonymous: Boolean
    val isUserLoggedIn: Flow<Boolean>

    suspend fun signInAnonymously(): Result<Unit>
    suspend fun signInWithGoogle(idToken: String): Result<Unit>
    suspend fun signOut(): Result<Unit>
}
