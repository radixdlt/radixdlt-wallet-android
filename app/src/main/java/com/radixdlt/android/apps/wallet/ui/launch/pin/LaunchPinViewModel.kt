package com.radixdlt.android.apps.wallet.ui.launch.pin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hadilq.liveevent.LiveEvent
import com.radixdlt.android.apps.wallet.util.VAULT_PIN
import com.radixdlt.android.apps.wallet.util.Vault
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LaunchPinViewModel : ViewModel() {

    enum class LaunchPinState {
        ENTER, ERROR
    }

    private val _paymentPinAction = LiveEvent<LaunchPinAction>()
    val paymentPinAction: LiveEvent<LaunchPinAction> get() = _paymentPinAction

    private val _paymentPinState = MutableLiveData<LaunchPinState>()
    val paymentPinState: LiveData<LaunchPinState> get() = _paymentPinState

    private val _pinLength = MutableLiveData<Int>()
    val pinLength: LiveData<Int> get() = _pinLength

    init {
        _paymentPinState.value = LaunchPinState.ENTER
    }

    fun logout() {
        _paymentPinAction.value = LaunchPinAction.LOGOUT
    }

    fun pinTextChange(text: String) {
        when (paymentPinState.value) {
            LaunchPinState.ENTER -> enterPin(text)
            LaunchPinState.ERROR -> return
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
            _paymentPinAction.value = LaunchPinAction.SUCCESS
        } else {
            _paymentPinState.value = LaunchPinState.ERROR
            delayLastCheckedCheckBoxStateChange()
        }
    }

    private fun delayLastCheckedCheckBoxStateChange() {
        // Add a slight delay to the last check box when checked so that
        // it is visible before resetting when needing to confirm pin
        viewModelScope.launch {
            delay(600)
            _pinLength.value = 0
            _paymentPinState.value = LaunchPinState.ENTER
        }
    }
}
