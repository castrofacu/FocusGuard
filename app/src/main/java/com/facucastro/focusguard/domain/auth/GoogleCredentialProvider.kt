package com.facucastro.focusguard.domain.auth

interface GoogleCredentialProvider {
    suspend fun getGoogleIdToken(): Result<String>
}
