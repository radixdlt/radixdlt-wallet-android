package com.radixdlt.android.apps.wallet.ui.start.restorewallet.invalidchecksum

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.radixdlt.android.apps.wallet.ui.start.restorewallet.restore.RestoreWalletAction

class RestoreWalletInvalidChecksumViewModel : ViewModel() {

    private val _restoreWalletAction = MutableLiveData<RestoreWalletAction>()
    val restoreWalletAction: LiveData<RestoreWalletAction> get() = _restoreWalletAction

    fun cancelDialogClickListener() {
        _restoreWalletAction.value = RestoreWalletAction.CloseDialog
    }

    fun continueClickListener() {
        _restoreWalletAction.value = RestoreWalletAction.ShowSetupPinDialog
    }
}
