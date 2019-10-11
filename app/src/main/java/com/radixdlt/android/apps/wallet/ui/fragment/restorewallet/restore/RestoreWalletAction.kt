package com.radixdlt.android.apps.wallet.ui.fragment.restorewallet.restore

sealed class RestoreWalletAction {
    class OpenWallet(val address: String = "") : RestoreWalletAction()
    object CloseDialog : RestoreWalletAction()
    object ShowMnemonicError : RestoreWalletAction()
    object ShowDialog : RestoreWalletAction()
    object PasteMnemonic : RestoreWalletAction()
}
