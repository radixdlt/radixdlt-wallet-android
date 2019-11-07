package com.radixdlt.android.apps.wallet.ui.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PaymentViewModel : ViewModel() {

    var selectedAsset = ""

    var noteInputShown = false

    private val _paymentAction = MutableLiveData<PaymentAction?>()
    val paymentAction: LiveData<PaymentAction?> get() = _paymentAction

    fun pay() {
        _paymentAction.value = PaymentAction.PAY
        clearPaymentAction()
    }

    fun usePin() {
        _paymentAction.value = PaymentAction.USE_PIN
        clearPaymentAction()
    }

    private fun clearPaymentAction() {
        _paymentAction.value = null
    }
}

enum class PaymentAction {
    PAY, USE_PIN
}
