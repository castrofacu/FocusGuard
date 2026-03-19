package com.facucastro.focusguard.domain.usecase

import com.facucastro.focusguard.domain.auth.GoogleCredentialProvider
import javax.inject.Inject

class GetGoogleIdTokenUseCase @Inject constructor(
    private val googleCredentialProvider: GoogleCredentialProvider
) {
    suspend operator fun invoke(): Result<String> =
        googleCredentialProvider.getGoogleIdToken()
}
