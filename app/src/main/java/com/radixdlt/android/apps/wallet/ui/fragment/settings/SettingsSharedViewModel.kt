package com.radixdlt.android.apps.wallet.ui.fragment.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hadilq.liveevent.LiveEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SettingsSharedViewModel : ViewModel() {

    private val _deleteWallet = LiveEvent<Boolean>()
    val deleteWallet: LiveEvent<Boolean> get() = _deleteWallet

    private val _showChangedPinSnackbar = LiveEvent<Unit>()
    val showChangedPinSnackbar: LiveEvent<Unit>
        get() = _showChangedPinSnackbar

    private val _popAuthenticationSetupBackStack = LiveEvent<Unit>()
    val popAuthenticationSetupBackStack: LiveEvent<Unit>
        get() = _popAuthenticationSetupBackStack

    fun setDeleteWallet(delete: Boolean) {
        _deleteWallet.value = delete
    }

    fun showChangedPinSnackbar() {
        _showChangedPinSnackbar.value = Unit
    }

    fun popAuthenticationSetupBackStack() {
        viewModelScope.launch {
            delay(25)
            _popAuthenticationSetupBackStack.value = Unit
        }
    }
}
