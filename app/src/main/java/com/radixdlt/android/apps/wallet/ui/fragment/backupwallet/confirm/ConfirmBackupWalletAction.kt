package com.radixdlt.android.apps.wallet.ui.fragment.backupwallet.confirm

sealed class ConfirmBackupWalletAction {
    object ShowMnemonicError : ConfirmBackupWalletAction()
    object Navigate : ConfirmBackupWalletAction()
}
