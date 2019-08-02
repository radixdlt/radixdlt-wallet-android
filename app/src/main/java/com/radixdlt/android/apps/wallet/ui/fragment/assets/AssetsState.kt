package com.radixdlt.android.apps.wallet.ui.fragment.assets

sealed class AssetsState {
    object Loading : AssetsState()
    object Error : AssetsState()
    class ShowAssets(val assets: List<Asset>) : AssetsState()
}

data class Asset(var name: String?, val iso: String, val address: String, var urlIcon: String?, var total: String)
