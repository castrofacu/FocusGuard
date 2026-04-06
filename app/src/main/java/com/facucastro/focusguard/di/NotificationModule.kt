package com.facucastro.focusguard.di

import com.facucastro.focusguard.data.notification.FocusNotificationManager
import com.facucastro.focusguard.domain.notification.DistractionNotifier
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {

    @Binds
    @Singleton
    abstract fun bindDistractionNotifier(impl: FocusNotificationManager): DistractionNotifier
}
