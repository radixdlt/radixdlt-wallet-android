package com.radixdlt.android.apps.wallet.ui.fragment.details

sealed class AssetTransactionDetailsAction {
    class CopyToClipboard(val message: String) : AssetTransactionDetailsAction()
    object Authenticate : AssetTransactionDetailsAction()
}
