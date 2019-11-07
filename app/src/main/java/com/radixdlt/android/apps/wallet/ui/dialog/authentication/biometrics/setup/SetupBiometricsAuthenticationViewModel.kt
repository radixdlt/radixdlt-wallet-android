package com.radixdlt.android.apps.wallet.ui.dialog.authentication.biometrics.setup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SetupBiometricsAuthenticationViewModel : ViewModel() {

    private val _setupBiometricsAction = MutableLiveData<SetupBiometricsAuthenticationAction?>()
    val setupBiometricsAuthenticationAction: LiveData<SetupBiometricsAuthenticationAction?> get() = _setupBiometricsAction

    fun useBiometricsClickListener() {
        _setupBiometricsAction.value = SetupBiometricsAuthenticationAction.USE_BIOMETRICS
    }

    fun notRightNowClickListener() {
        _setupBiometricsAction.value = SetupBiometricsAuthenticationAction.CANCEL
    }
}
