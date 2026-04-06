package com.facucastro.focusguard.providers.domain.notification

import com.facucastro.focusguard.domain.notification.DistractionNotifier
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs

fun providesNotifierMock(): DistractionNotifier = mockk {
    every { notifyDistraction(any()) } just runs
}
