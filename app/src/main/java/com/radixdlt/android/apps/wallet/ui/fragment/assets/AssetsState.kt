package com.radixdlt.android.apps.wallet.ui.fragment.assets

sealed class AssetsState {
    class Assets(val assets: List<Asset>) : AssetsState()
}

data class Asset(val name: String, val iso: String, val address: String, val urlIcon: String, val total: String)
