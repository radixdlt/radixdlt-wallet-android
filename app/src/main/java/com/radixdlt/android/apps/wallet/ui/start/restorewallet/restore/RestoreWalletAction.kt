package com.radixdlt.android.apps.wallet.ui.start.restorewallet.restore

sealed class RestoreWalletAction {
    object ShowSetupPinDialog : RestoreWalletAction()
    object CloseDialog : RestoreWalletAction()
    object ShowMnemonicError : RestoreWalletAction()
    object ShowInvalidChecksumDialog : RestoreWalletAction()
    object PasteMnemonic : RestoreWalletAction()
}
