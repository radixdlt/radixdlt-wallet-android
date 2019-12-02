package com.radixdlt.android.apps.wallet.ui.fragment.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hadilq.liveevent.LiveEvent
import com.radixdlt.android.apps.wallet.util.multiClickingPrevention

class SettingsViewModel : ViewModel() {

    private val _settingsAction = LiveEvent<SettingsAction>()
    val settingsAction: LiveEvent<SettingsAction> get() = _settingsAction

    private val _useBiometrics = MutableLiveData<Boolean>()
    val useBiometrics: LiveData<Boolean> get() = _useBiometrics

    private val _authenticateOnLaunch = MutableLiveData<Boolean>()
    val authenticateOnLaunch: LiveData<Boolean> get() = _authenticateOnLaunch

    private val _hideBiometrics = MutableLiveData<Boolean>()
    val hideBiometrics: LiveData<Boolean> get() = _hideBiometrics

    private val _pinSet = MutableLiveData<Boolean>()
    val pinSet: LiveData<Boolean> get() = _pinSet

    fun showSecurityOptions(
        isUsingBiometrics: Boolean,
        useBiometrics: Boolean,
        authenticateOnLaunch: Boolean,
        pinSet: Boolean
    ) {
        _pinSet.value = pinSet
        _authenticateOnLaunch.value = authenticateOnLaunch

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
        if (multiClickingPrevention(300)) return
        _settingsAction.value = SettingsAction.BACKUP_WALLET
    }

    fun changePinClickListener() {
        if (multiClickingPrevention(300)) return
        _settingsAction.value = SettingsAction.CHANGE_PIN
    }

    fun deleteWalletClickListener() {
        if (multiClickingPrevention(300)) return
        _settingsAction.value = SettingsAction.DELETE_WALLET
    }

    fun reportAnIssueClickListener() {
        _settingsAction.value = SettingsAction.REPORT_ISSUE
    }

    fun useBiometricsClickListener() {
        if (multiClickingPrevention(300)) return
        _settingsAction.value = SettingsAction.USE_BIOMETRICS
    }

    fun toggleBiometrics() {
        _useBiometrics.value = !(_useBiometrics.value as Boolean)
    }

    fun useBiometricsCheckedListener(checked: Boolean) {
        if (_useBiometrics.value != checked) {
            _settingsAction.value = SettingsAction.USE_BIOMETRICS
        }
    }

    fun authenticateOnLaunchClickListener() {
        _authenticateOnLaunch.value = !(_authenticateOnLaunch.value as Boolean)
    }

    fun authenticateOnLaunchCheckedListener(checked: Boolean) {
        if (_authenticateOnLaunch.value != checked) {
            _authenticateOnLaunch.value = checked
        }
    }
}
