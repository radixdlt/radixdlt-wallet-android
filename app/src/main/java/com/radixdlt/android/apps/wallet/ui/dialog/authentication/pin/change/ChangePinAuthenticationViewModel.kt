package com.radixdlt.android.apps.wallet.ui.dialog.authentication.pin.change

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hadilq.liveevent.LiveEvent
import com.radixdlt.android.apps.wallet.util.VAULT_PIN
import com.radixdlt.android.apps.wallet.util.Vault
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChangePinAuthenticationViewModel : ViewModel() {

    enum class ChangePinState {
        CURRENT, CURRENT_DELAY, NEW, NEW_DELAY, CONFIRM, ERROR
    }

    private val _changePinAuthenticationAction = LiveEvent<Unit>()
    val changePinAuthenticationAction: LiveEvent<Unit>
        get() = _changePinAuthenticationAction

    private val _changePinState = MutableLiveData<ChangePinState>()
    val changePinState: LiveData<ChangePinState>
        get() = _changePinState

    private val _pinLength = MutableLiveData<Int>()
    val pinLength: LiveData<Int>
        get() = _pinLength

    private lateinit var pinSet: String

    init {
        _changePinState.value = ChangePinState.CURRENT
    }

    fun pinTextChange(text: String) {
        when (changePinState.value) {
            ChangePinState.CURRENT -> oldPin(text)
            ChangePinState.NEW -> newPin(text)
            ChangePinState.CONFIRM -> confirmPin(text)
            ChangePinState.CURRENT_DELAY, ChangePinState.NEW_DELAY, ChangePinState.ERROR -> return
        }
    }

    private fun oldPin(text: String) {
        _pinLength.value = text.length
        if (text.length == 4) {
            verifyCurrentPin(text)
        }
    }

    private fun verifyCurrentPin(text: String) {
        if (text == Vault.getVault().getString(VAULT_PIN, null)) {
            _changePinState.value = ChangePinState.CURRENT_DELAY
            delayLastCheckedCheckBoxStateChange(200)
            pinSet = text
        } else {
            _changePinState.value = ChangePinState.ERROR
            delayLastCheckedCheckBoxStateChangeError()
        }
    }

    private fun newPin(text: String) {
        _pinLength.value = text.length
        if (text.length == 4) {
            _changePinState.value = ChangePinState.NEW_DELAY
            delayLastCheckedCheckBoxStateChange(200)
            pinSet = text
        }
    }

    private fun confirmPin(text: String) {
        _pinLength.value = text.length
        if (text.length == 4) {
            viewModelScope.launch {
                delay(200)
                verifyConfirmationPinSameAsSetup(text)
            }
        }
    }

    private fun verifyConfirmationPinSameAsSetup(text: String) {
        if (text == pinSet) {
            Vault.getVault().edit().putString(VAULT_PIN, text).apply()
            _changePinAuthenticationAction.value = Unit
        } else {
            _changePinState.value = ChangePinState.ERROR
            delayLastCheckedCheckBoxStateChange(600)
        }
    }

    private fun delayLastCheckedCheckBoxStateChangeError() {
        // Add a slight delay to the last check box when checked so that
        // it is visible before resetting when needing to confirm pin
        viewModelScope.launch {
            delay(600)
            _pinLength.value = 0
            _changePinState.value = ChangePinState.CURRENT
        }
    }

    private fun delayLastCheckedCheckBoxStateChange(delayLength: Long) {
        // Add a slight delay to the last check box when checked so that
        // it is visible before resetting when needing to confirm pin
        viewModelScope.launch {
            delay(delayLength)
            _pinLength.value = 0
            _changePinState.value = if (_changePinState.value == ChangePinState.CURRENT_DELAY) {
                ChangePinState.NEW
            } else {
                ChangePinState.CONFIRM
            }
        }
    }
}
