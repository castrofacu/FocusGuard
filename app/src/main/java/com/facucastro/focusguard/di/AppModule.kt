package com.facucastro.focusguard.di

import android.content.Context
import androidx.credentials.CredentialManager
import com.facucastro.focusguard.BuildConfig
import com.facucastro.focusguard.data.auth.GoogleCredentialDataSource
import com.facucastro.focusguard.data.local.LocalSessionDataSource
import com.facucastro.focusguard.data.local.RoomSessionDataSource
import com.facucastro.focusguard.data.remote.FakeFocusApiServiceImpl
import com.facucastro.focusguard.data.remote.FocusApiService
import com.facucastro.focusguard.data.repository.AuthRepositoryImpl
import com.facucastro.focusguard.data.repository.FocusRepositoryImpl
import com.facucastro.focusguard.data.time.SystemTimeProvider
import com.facucastro.focusguard.domain.auth.GoogleCredentialProvider
import com.facucastro.focusguard.domain.repository.AuthRepository
import com.facucastro.focusguard.domain.repository.FocusRepository
import com.facucastro.focusguard.domain.time.TimeProvider
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.auth.FirebaseAuth
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindLocalSessionDataSource(impl: RoomSessionDataSource): LocalSessionDataSource

    @Binds
    @Singleton
    abstract fun bindFocusRepository(impl: FocusRepositoryImpl): FocusRepository

    @Binds
    @Singleton
    abstract fun bindFocusApiService(impl: FakeFocusApiServiceImpl): FocusApiService

    @Binds
    @Singleton
    abstract fun bindTimeProvider(impl: SystemTimeProvider): TimeProvider

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindGoogleCredentialProvider(impl: GoogleCredentialDataSource): GoogleCredentialProvider

    companion object {
        @Provides
        @Singleton
        fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

        @Provides
        @Singleton
        fun provideCredentialManager(@ApplicationContext context: Context): CredentialManager =
            CredentialManager.create(context)

        @Provides
        @Singleton
        fun provideGoogleIdOption(): GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .build()
    }
}
