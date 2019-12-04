package com.radixdlt.android.apps.wallet.ui.start.restorewallet.shared

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RestoreWalletSharedViewModel : ViewModel() {

    private val _restoreWalletSharedAction = MutableLiveData<RestoreWalletSharedAction>()
    val restoreWalletSharedAction: LiveData<RestoreWalletSharedAction>
        get() = _restoreWalletSharedAction

    fun continueClickListener() {
        _restoreWalletSharedAction.value = RestoreWalletSharedAction.OpenWallet
    }
}
