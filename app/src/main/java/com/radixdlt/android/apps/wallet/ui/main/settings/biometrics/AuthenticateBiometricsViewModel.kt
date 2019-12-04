package com.radixdlt.android.apps.wallet.ui.main.settings.biometrics

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.radixdlt.android.apps.wallet.biometrics.BiometricsAuthenticationResult

class AuthenticateBiometricsViewModel : ViewModel() {

    private val _paymentBiometricsAction = MutableLiveData<AuthenticateBiometricsAction?>()
    val paymentBiometricsAction: LiveData<AuthenticateBiometricsAction?> get() = _paymentBiometricsAction

    private val _biometricsAuthenticationResult = MutableLiveData<BiometricsAuthenticationResult>()
    val biometricsAuthenticationResult: LiveData<BiometricsAuthenticationResult>
        get() = _biometricsAuthenticationResult

    fun setBiometricsAuthenticationResult(result: BiometricsAuthenticationResult) {
        _biometricsAuthenticationResult.value = result
    }

    fun usePinClickListener() {
        _paymentBiometricsAction.value = AuthenticateBiometricsAction.USE_PIN
        _paymentBiometricsAction.value = null
    }

    fun cancelClickListener() {
        _paymentBiometricsAction.value = AuthenticateBiometricsAction.CANCEL
        _paymentBiometricsAction.value = null
    }
}
