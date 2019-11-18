package com.radixdlt.android.apps.wallet.ui.fragment.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hadilq.liveevent.LiveEvent

class SettingsViewModel : ViewModel() {

    private val _settingsAction = LiveEvent<SettingsAction>()
    val settingsAction: LiveEvent<SettingsAction> get() = _settingsAction

    private val _useBiometrics = MutableLiveData<Boolean>()
    val useBiometrics: LiveData<Boolean> get() = _useBiometrics

    private val _hideBiometrics = MutableLiveData<Boolean>()
    val hideBiometrics: LiveData<Boolean> get() = _hideBiometrics

    private val _pinSet = MutableLiveData<Boolean>()
    val pinSet: LiveData<Boolean> get() = _pinSet

    fun showSecurityOptions(isUsingBiometrics: Boolean, useBiometrics: Boolean, pinSet: Boolean) {
        _pinSet.value = pinSet

        if (!pinSet) {
            hideBiometrics()
        } else if (isUsingBiometrics) {
            _hideBiometrics.value = false
            _useBiometrics.value = useBiometrics
        } else {
            hideBiometrics()
        }
    }

    private fun hideBiometrics() {
        _hideBiometrics.value = true
        _useBiometrics.value = false
    }

    fun backupWalletClickListener() {
        _settingsAction.value = SettingsAction.BACKUP_WALLET
    }

    fun changePinClickListener() {
        _settingsAction.value = SettingsAction.CHANGE_PIN
    }

    fun deleteWalletClickListener() {
        _settingsAction.value = SettingsAction.DELETE_WALLET
    }

    fun reportAnIssueClickListener() {
        _settingsAction.value = SettingsAction.REPORT_ISSUE
    }

    fun useBiometricsClickListener() {
        _useBiometrics.value = !(_useBiometrics.value as Boolean)
    }

    fun useBiometricsCheckedListener(checked: Boolean) {
        if (_useBiometrics.value != checked) {
            _useBiometrics.value = checked
        }
    }
}
