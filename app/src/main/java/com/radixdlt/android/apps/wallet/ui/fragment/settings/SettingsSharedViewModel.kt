package com.radixdlt.android.apps.wallet.ui.fragment.settings

import androidx.lifecycle.ViewModel
import com.hadilq.liveevent.LiveEvent

class SettingsSharedViewModel : ViewModel() {

    private val _deleteWallet = LiveEvent<Boolean>()
    val deleteWallet: LiveEvent<Boolean> get() = _deleteWallet

    private val _showChangedPinSnackbar = LiveEvent<Unit>()
    val showChangedPinSnackbar: LiveEvent<Unit>
        get() = _showChangedPinSnackbar

    private val _popAuthenticationSetupBackStack = LiveEvent<Unit>()
    val popAuthenticationSetupBackStack: LiveEvent<Unit>
        get() = _popAuthenticationSetupBackStack

    private val _authenticateAction = LiveEvent<AuthenticateAction>()
    val authenticateAction: LiveEvent<AuthenticateAction>
        get() = _authenticateAction

    fun cancel() {
        _authenticateAction.value = AuthenticateAction.Cancel
    }

    fun backup() {
        _authenticateAction.value = AuthenticateAction.Backup
    }

    fun useBiometrics() {
        _authenticateAction.value = AuthenticateAction.UseBiometrics
    }

    fun usePin(functionality: AuthenticateFunctionality) {
        _authenticateAction.value = AuthenticateAction.UsePin(functionality)
    }

    fun setDeleteWallet(delete: Boolean) {
        _deleteWallet.value = delete
    }

    fun showChangedPinSnackbar() {
        _showChangedPinSnackbar.value = Unit
    }

    fun popAuthenticationSetupBackStack() {
        _popAuthenticationSetupBackStack.value = Unit
    }
}

sealed class AuthenticateAction {
    object Cancel : AuthenticateAction()
    object Backup : AuthenticateAction()
    object UseBiometrics : AuthenticateAction()
    class UsePin(val functionality: AuthenticateFunctionality) : AuthenticateAction()
}
