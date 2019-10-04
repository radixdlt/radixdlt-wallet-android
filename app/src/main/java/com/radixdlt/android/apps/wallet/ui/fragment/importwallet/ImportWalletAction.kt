package com.radixdlt.android.apps.wallet.ui.fragment.importwallet

sealed class ImportWalletAction {
    object ShowMnemonicError : ImportWalletAction()
    object OpenWallet : ImportWalletAction()
    object ShowDialog : ImportWalletAction()
    object CloseDialog : ImportWalletAction()
}
