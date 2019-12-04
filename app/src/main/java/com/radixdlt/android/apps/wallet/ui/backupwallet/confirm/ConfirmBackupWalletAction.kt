package com.radixdlt.android.apps.wallet.ui.backupwallet.confirm

sealed class ConfirmBackupWalletAction {
    object ShowMnemonicError : ConfirmBackupWalletAction()
    object Navigate : ConfirmBackupWalletAction()
}
