package com.facucastro.focusguard

import androidx.lifecycle.ViewModel
import com.facucastro.focusguard.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    authRepository: AuthRepository
) : ViewModel() {
    val isUserLoggedIn = authRepository.isUserLoggedIn
}
