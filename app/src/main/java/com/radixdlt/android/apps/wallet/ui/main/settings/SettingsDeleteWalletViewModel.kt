package com.radixdlt.android.apps.wallet.ui.main.settings

import androidx.lifecycle.ViewModel
import com.hadilq.liveevent.LiveEvent

class SettingsDeleteWalletViewModel : ViewModel() {

    private val _deleteWallet = LiveEvent<Boolean>()
    val deleteWallet: LiveEvent<Boolean> get() = _deleteWallet

    fun deleteWalletClickListener() {
        _deleteWallet.value = true
    }

    fun cancelClickListener() {
        _deleteWallet.value = false
    }
}
