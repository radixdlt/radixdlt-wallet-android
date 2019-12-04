package com.radixdlt.android.apps.wallet.ui.start.createwallet

sealed class CreateWalletAction {
    class OpenWallet(val address: String) : CreateWalletAction()
    object ImportWallet : CreateWalletAction()
}
