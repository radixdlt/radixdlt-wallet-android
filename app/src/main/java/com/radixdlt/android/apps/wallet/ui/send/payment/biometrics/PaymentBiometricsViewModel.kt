package com.radixdlt.android.apps.wallet.ui.send.payment.biometrics

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.radixdlt.android.apps.wallet.biometrics.BiometricsAuthenticationResult

class PaymentBiometricsViewModel : ViewModel() {

    private val _paymentBiometricsAction = MutableLiveData<PaymentBiometricsAction?>()
    val paymentBiometricsAction: LiveData<PaymentBiometricsAction?> get() = _paymentBiometricsAction

    private val _biometricsAuthenticationResult = MutableLiveData<BiometricsAuthenticationResult>()
    val biometricsAuthenticationResult: LiveData<BiometricsAuthenticationResult>
        get() = _biometricsAuthenticationResult

    fun setBiometricsAuthenticationResult(result: BiometricsAuthenticationResult) {
        _biometricsAuthenticationResult.value = result
    }

    fun usePinClickListener() {
        _paymentBiometricsAction.value = PaymentBiometricsAction.USE_PIN
    }

    fun cancelClickListener() {
        _paymentBiometricsAction.value = PaymentBiometricsAction.CANCEL
    }
}
