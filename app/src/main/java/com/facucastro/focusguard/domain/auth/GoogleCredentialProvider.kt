package com.facucastro.focusguard.domain.auth

import android.content.Context

interface GoogleCredentialProvider {
    suspend fun getGoogleIdToken(context: Context): Result<String>
}
