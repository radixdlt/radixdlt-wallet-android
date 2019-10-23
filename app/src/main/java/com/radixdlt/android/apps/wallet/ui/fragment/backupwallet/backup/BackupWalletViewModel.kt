package com.radixdlt.android.apps.wallet.ui.fragment.backupwallet.backup

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.radixdlt.android.apps.wallet.util.VAULT_MNEMONIC
import com.radixdlt.android.apps.wallet.util.Vault
import com.radixdlt.android.apps.wallet.util.copyToClipboard

class BackupWalletViewModel : ViewModel() {

    private val _backupWalletAction = MutableLiveData<BackupWalletAction?>()
    val backupWalletAction: LiveData<BackupWalletAction?> get() = _backupWalletAction

    private val _pastedMnemonic = MutableLiveData<Array<String>>()
    val mnemonicArray: LiveData<Array<String>> get() = _pastedMnemonic

    private val mnemonic: String = Vault.getVault().getString(VAULT_MNEMONIC, null)
        ?: throw RuntimeException()

    init {
        val mnemonicArray = mnemonic.trim().split(" ").toTypedArray()
        _pastedMnemonic.value = mnemonicArray
    }

    fun copyButtonClick(context: Context) {
        copyToClipboard(context, mnemonic)
        _backupWalletAction.value = BackupWalletAction.CopyMnemonic
    }

    fun nextButtonClick() {
        confirmBackup()
    }

    private fun confirmBackup() {
        _backupWalletAction.value = BackupWalletAction.ConfirmBackup(mnemonic)
        _backupWalletAction.value = null
    }
}
