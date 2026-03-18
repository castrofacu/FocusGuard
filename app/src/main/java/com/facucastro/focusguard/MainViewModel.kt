package com.facucastro.focusguard

import androidx.lifecycle.ViewModel
import com.facucastro.focusguard.domain.usecase.ObserveLoginStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    observeLoginState: ObserveLoginStateUseCase
) : ViewModel() {

    val isUserLoggedIn = observeLoginState()
}
