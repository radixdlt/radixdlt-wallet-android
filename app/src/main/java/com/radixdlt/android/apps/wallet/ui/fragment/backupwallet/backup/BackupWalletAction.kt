package com.radixdlt.android.apps.wallet.ui.fragment.backupwallet.backup

sealed class BackupWalletAction {
    class ConfirmBackup(val mnemonic: String) : BackupWalletAction()
    object CopyMnemonic : BackupWalletAction()
}
