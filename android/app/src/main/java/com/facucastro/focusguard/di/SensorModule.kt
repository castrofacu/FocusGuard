package com.facucastro.focusguard.di

import com.facucastro.focusguard.data.sensor.CompositeDistractionMonitor
import com.facucastro.focusguard.domain.sensor.DistractionMonitor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SensorModule {

    @Binds
    @Singleton
    abstract fun bindDistractionMonitor(impl: CompositeDistractionMonitor): DistractionMonitor
}
