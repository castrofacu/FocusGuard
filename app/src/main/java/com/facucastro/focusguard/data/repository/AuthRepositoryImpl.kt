package com.facucastro.focusguard.data.repository

import com.facucastro.focusguard.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override val currentUserEmail: String?
        get() = firebaseAuth.currentUser?.email

    override val isAnonymous: Boolean
        get() = firebaseAuth.currentUser?.isAnonymous ?: false

    override val isUserLoggedIn: Flow<Boolean> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser != null)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }.onStart {
        emit(firebaseAuth.currentUser != null)
    }

    override suspend fun signInAnonymously(): Result<Unit> {
        return try {
            firebaseAuth.signInAnonymously().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<Unit> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val currentUser = firebaseAuth.currentUser

            if (currentUser != null && currentUser.isAnonymous) {
                try {
                    currentUser.linkWithCredential(credential).await()
                } catch (e: FirebaseAuthUserCollisionException) {
                    firebaseAuth.signInWithCredential(credential).await()
                }
            } else {
                firebaseAuth.signInWithCredential(credential).await()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
