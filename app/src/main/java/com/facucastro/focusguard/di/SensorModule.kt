package com.facucastro.focusguard.di

import com.facucastro.focusguard.data.sensor.AccelerometerDistractionMonitor
import com.facucastro.focusguard.data.sensor.MicrophoneDistractionMonitor
import com.facucastro.focusguard.domain.sensor.CompositeDistractionMonitor
import com.facucastro.focusguard.domain.sensor.DistractionMonitor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SensorModule {

    @Provides
    @Singleton
    fun provideDistractionMonitor(
        accelerometerMonitor: AccelerometerDistractionMonitor,
        microphoneMonitor: MicrophoneDistractionMonitor,
    ): DistractionMonitor = CompositeDistractionMonitor(
        monitors = listOf(accelerometerMonitor, microphoneMonitor),
    )
}
