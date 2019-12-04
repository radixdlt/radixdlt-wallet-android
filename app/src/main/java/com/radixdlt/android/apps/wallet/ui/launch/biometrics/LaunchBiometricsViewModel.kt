package com.radixdlt.android.apps.wallet.ui.launch.biometrics

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hadilq.liveevent.LiveEvent
import com.radixdlt.android.apps.wallet.biometrics.BiometricsAuthenticationResult

class LaunchBiometricsViewModel : ViewModel() {

    private val _paymentBiometricsAction = LiveEvent<LaunchBiometricsAction>()
    val paymentBiometricsAction: LiveEvent<LaunchBiometricsAction> get() = _paymentBiometricsAction

    private val _biometricsAuthenticationResult = MutableLiveData<BiometricsAuthenticationResult>()
    val biometricsAuthenticationResult: LiveData<BiometricsAuthenticationResult>
        get() = _biometricsAuthenticationResult

    fun setBiometricsAuthenticationResult(result: BiometricsAuthenticationResult) {
        _biometricsAuthenticationResult.value = result
    }

    fun usePinClickListener() {
        _paymentBiometricsAction.value = LaunchBiometricsAction.USE_PIN
    }

    fun cancelClickListener() {
        _paymentBiometricsAction.value = LaunchBiometricsAction.LOGOUT
    }
}
