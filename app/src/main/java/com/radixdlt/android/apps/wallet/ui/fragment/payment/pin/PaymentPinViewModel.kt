package com.radixdlt.android.apps.wallet.ui.fragment.payment.pin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radixdlt.android.apps.wallet.util.VAULT_PIN
import com.radixdlt.android.apps.wallet.util.Vault
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PaymentPinViewModel : ViewModel() {

    enum class PaymentPinState {
        ENTER, ERROR
    }

    private val _paymentPinAction = MutableLiveData<PaymentPinAction?>()
    val paymentPinAction: LiveData<PaymentPinAction?> get() = _paymentPinAction

    private val _paymentPinState = MutableLiveData<PaymentPinState>()
    val paymentPinState: LiveData<PaymentPinState> get() = _paymentPinState

    private val _pinLength = MutableLiveData<Int>()
    val pinLength: LiveData<Int> get() = _pinLength

    init {
        _paymentPinState.value = PaymentPinState.ENTER
    }

    fun pinTextChange(text: String) {
        when (paymentPinState.value) {
            PaymentPinState.ENTER -> enterPin(text)
            PaymentPinState.ERROR -> return
        }
    }

    private fun enterPin(text: String) {
        _pinLength.value = text.length
        if (text.length == 4) {
            viewModelScope.launch {
                delay(200)
                verifyConfirmationPinSameAsSetup(text)
            }
        }
    }

    private fun verifyConfirmationPinSameAsSetup(text: String) {
        if (text == Vault.getVault().getString(VAULT_PIN, null)) {
            _paymentPinAction.value = PaymentPinAction.SUCCESS
            _paymentPinAction.value = null
        } else {
            _paymentPinState.value = PaymentPinState.ERROR
            delayLastCheckedCheckBoxStateChange()
        }
    }

    private fun delayLastCheckedCheckBoxStateChange() {
        // Add a slight delay to the last check box when checked so that
        // it is visible before resetting when needing to confirm pin
        viewModelScope.launch {
            delay(600)
            _pinLength.value = 0
            _paymentPinState.value = PaymentPinState.ENTER
        }
    }
}
