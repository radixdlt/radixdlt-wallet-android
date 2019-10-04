package com.radixdlt.android.apps.wallet.ui.fragment.importwallet.shared

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ImportWalletSharedViewModel : ViewModel() {

    private val _importWalletAction = MutableLiveData<ImportWalletSharedAction>()
    val importWalletAction: LiveData<ImportWalletSharedAction> get() = _importWalletAction

    fun continueClickListener() {
        _importWalletAction.value = ImportWalletSharedAction.OpenWallet
    }
}
