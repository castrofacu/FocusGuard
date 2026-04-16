package com.facucastro.focusguard.di

import com.facucastro.focusguard.data.service.FocusSessionRunner
import com.facucastro.focusguard.domain.session.FocusSessionController
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SessionModule {

    @Binds
    @Singleton
    abstract fun bindFocusSessionController(
        impl: FocusSessionRunner,
    ): FocusSessionController
}
