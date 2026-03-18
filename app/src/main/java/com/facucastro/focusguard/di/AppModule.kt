package com.facucastro.focusguard.di

import com.facucastro.focusguard.data.local.LocalSessionDataSource
import com.facucastro.focusguard.data.local.RoomSessionDataSource
import com.facucastro.focusguard.data.remote.FakeFocusApiServiceImpl
import com.facucastro.focusguard.data.remote.FocusApiService
import com.facucastro.focusguard.data.repository.AuthRepositoryImpl
import com.facucastro.focusguard.data.repository.FocusRepositoryImpl
import com.facucastro.focusguard.data.time.SystemTimeProvider
import com.facucastro.focusguard.domain.repository.AuthRepository
import com.facucastro.focusguard.domain.repository.FocusRepository
import com.facucastro.focusguard.domain.time.TimeProvider
import com.google.firebase.auth.FirebaseAuth
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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

    // To switch to the real Retrofit backend (RetrofitFocusApiServiceImpl)
    @Binds
    @Singleton
    abstract fun bindFocusApiService(impl: FakeFocusApiServiceImpl): FocusApiService

    @Binds
    @Singleton
    abstract fun bindTimeProvider(impl: SystemTimeProvider): TimeProvider

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    companion object {
        @Provides
        @Singleton
        fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
    }
}
