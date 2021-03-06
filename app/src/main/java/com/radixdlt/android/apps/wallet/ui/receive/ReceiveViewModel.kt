package com.radixdlt.android.apps.wallet.ui.receive

import androidx.lifecycle.ViewModel
import com.hadilq.liveevent.LiveEvent
import com.radixdlt.android.apps.wallet.ui.main.LaunchAuthenticationAction

class ReceiveViewModel : ViewModel() {

    private val _launchAuthenticationAction = LiveEvent<LaunchAuthenticationAction>()
    val launchAuthenticationAction: LiveEvent<LaunchAuthenticationAction>
        get() = _launchAuthenticationAction

    fun unlock() {
        _launchAuthenticationAction.value = LaunchAuthenticationAction.UNLOCK
    }

    fun usePin() {
        _launchAuthenticationAction.value = LaunchAuthenticationAction.USE_PIN
    }

    fun logout() {
        _launchAuthenticationAction.value = LaunchAuthenticationAction.LOGOUT
    }
}
