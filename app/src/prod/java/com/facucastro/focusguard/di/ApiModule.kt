package com.facucastro.focusguard.di

import com.facucastro.focusguard.data.remote.FocusApiService
import com.facucastro.focusguard.data.remote.RetrofitFocusApiServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ApiModule {

    @Binds
    @Singleton
    abstract fun bindFocusApiService(impl: RetrofitFocusApiServiceImpl): FocusApiService
}
