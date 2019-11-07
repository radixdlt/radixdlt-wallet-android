package com.radixdlt.android.apps.wallet.ui.dialog.authentication.pin.setup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radixdlt.android.apps.wallet.util.VAULT_PIN
import com.radixdlt.android.apps.wallet.util.Vault
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SetupPinAuthenticationViewModel : ViewModel() {

    enum class SetupPinState {
        SET, SETTING, CONFIRM, ERROR
    }

    private val _pinAction = MutableLiveData<SetupPinAuthenticationAction?>()
    val setupPinAuthenticationAction: LiveData<SetupPinAuthenticationAction?> get() = _pinAction

    private val _pinSetupState = MutableLiveData<SetupPinState>()
    val pinSetupState: LiveData<SetupPinState> get() = _pinSetupState

    private val _pinLength = MutableLiveData<Int>()
    val pinLength: LiveData<Int> get() = _pinLength

    private lateinit var pinSet: String

    init {
        _pinSetupState.value =
            SetupPinState.SET
    }

    fun pinTextChange(text: String) {
        when (pinSetupState.value) {
            SetupPinState.SET -> setPin(text)
            SetupPinState.SETTING -> return
            SetupPinState.CONFIRM -> confirmPin(text)
            SetupPinState.ERROR -> return
        }
    }

    private fun setPin(text: String) {
        _pinLength.value = text.length
        if (text.length == 4) {
            _pinSetupState.value =
                SetupPinState.SETTING
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
            _pinAction.value = SetupPinAuthenticationAction.NAVIGATE
            _pinAction.value = null
        } else {
            _pinSetupState.value =
                SetupPinState.ERROR
            delayLastCheckedCheckBoxStateChange(600)
        }
    }

    private fun delayLastCheckedCheckBoxStateChange(delayLength: Long) {
        // Add a slight delay to the last check box when checked so that
        // it is visible before resetting when needing to confirm pin
        viewModelScope.launch {
            delay(delayLength)
            _pinLength.value = 0
            _pinSetupState.value =
                SetupPinState.CONFIRM
        }
    }
}
