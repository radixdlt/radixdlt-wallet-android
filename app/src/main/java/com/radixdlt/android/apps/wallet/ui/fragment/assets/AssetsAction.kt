package com.radixdlt.android.apps.wallet.ui.fragment.assets

import java.math.BigDecimal

sealed class AssetsAction {
    object CloseBackUpMnemonicWarning : AssetsAction()
    object NavigateTo : AssetsAction()
    object ShowLoading : AssetsAction()
    object ShowError : AssetsAction()
    class ShowAssets(val assets: List<Asset>) : AssetsAction()
}

data class Asset(
    var name: String?,
    val iso: String,
    val address: String,
    var urlIcon: String?,
    var total: String
)

data class AssetPayment(
    var name: String?,
    val iso: String,
    val address: String,
    var urlIcon: String?,
    var total: String,
    val granularity: BigDecimal?,
    var selected: Boolean
)
