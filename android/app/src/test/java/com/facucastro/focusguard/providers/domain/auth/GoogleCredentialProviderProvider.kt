package com.facucastro.focusguard.providers.domain.auth

import com.facucastro.focusguard.domain.auth.GoogleCredentialProvider
import io.mockk.coEvery
import io.mockk.mockk

fun providesMockGoogleCredentialProvider(
    idTokenResult: Result<String> = Result.success("mock_id_token")
): GoogleCredentialProvider {
    return mockk<GoogleCredentialProvider> {
        coEvery { getGoogleIdToken() } returns idTokenResult
    }
}
