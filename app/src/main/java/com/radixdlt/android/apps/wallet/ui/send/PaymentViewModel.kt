package com.radixdlt.android.apps.wallet.ui.send

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hadilq.liveevent.LiveEvent
import com.radixdlt.android.apps.wallet.ui.main.LaunchAuthenticationAction

class PaymentViewModel : ViewModel() {

    var selectedAsset = ""

    var noteInputShown = false

    private val _paymentAction = MutableLiveData<PaymentAction?>()
    val paymentAction: LiveData<PaymentAction?> get() = _paymentAction

    private val _launchAuthenticationAction = LiveEvent<LaunchAuthenticationAction>()
    val launchAuthenticationAction: LiveEvent<LaunchAuthenticationAction>
        get() = _launchAuthenticationAction

    fun unlock() {
        _launchAuthenticationAction.value = LaunchAuthenticationAction.UNLOCK
    }

    fun usePinAuthentication() {
        _launchAuthenticationAction.value = LaunchAuthenticationAction.USE_PIN
    }

    fun logout() {
        _launchAuthenticationAction.value = LaunchAuthenticationAction.LOGOUT
    }

    fun pay() {
        _paymentAction.value =
            PaymentAction.PAY
        clearPaymentAction()
    }

    fun usePin() {
        _paymentAction.value =
            PaymentAction.USE_PIN
        clearPaymentAction()
    }

    private fun clearPaymentAction() {
        _paymentAction.value = null
    }
}

enum class PaymentAction {
    PAY, USE_PIN
}
