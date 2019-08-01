package com.radixdlt.android.apps.wallet.ui.fragment.assets

sealed class AssetsState {
    class Assets(val assets: List<Asset>) : AssetsState()
}

data class Asset(var name: String?, val iso: String, val address: String, var urlIcon: String?, var total: String)
