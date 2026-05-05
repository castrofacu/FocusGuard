package com.facucastro.focusguard.di

import com.facucastro.focusguard.data.repository.CommunityRepositoryImpl
import com.facucastro.focusguard.domain.repository.CommunityRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CommunityModule {

    @Binds
    @Singleton
    abstract fun bindCommunityRepository(
        impl: CommunityRepositoryImpl,
    ): CommunityRepository
}
