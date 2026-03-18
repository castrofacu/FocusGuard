package com.facucastro.focusguard.data.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.facucastro.focusguard.domain.auth.GoogleCredentialProvider
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleCredentialDataSource @Inject constructor(
    private val credentialManager: CredentialManager,
    private val googleIdOption: GetGoogleIdOption
) : GoogleCredentialProvider {
    override suspend fun getGoogleIdToken(context: Context): Result<String> {
        return try {
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            val result = credentialManager.getCredential(context, request)
            val credential = GoogleIdTokenCredential.createFrom(result.credential.data)
            Result.success(credential.idToken)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
