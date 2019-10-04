package com.radixdlt.android.apps.wallet.ui.fragment.importwallet.invalidchecksum

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.radixdlt.android.apps.wallet.ui.fragment.importwallet.ImportWalletAction

class ImportWalletInvalidChecksumViewModel : ViewModel() {

    private val _importWalletAction = MutableLiveData<ImportWalletAction>()
    val importWalletAction: LiveData<ImportWalletAction> get() = _importWalletAction

    fun cancelDialogClickListener() {
        _importWalletAction.value = ImportWalletAction.CloseDialog
    }

    fun continueClickListener() {
        _importWalletAction.value = ImportWalletAction.OpenWallet
    }
}
