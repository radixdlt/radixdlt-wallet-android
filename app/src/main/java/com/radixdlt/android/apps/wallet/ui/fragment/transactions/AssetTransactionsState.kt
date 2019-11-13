package com.radixdlt.android.apps.wallet.ui.fragment.transactions

import com.radixdlt.android.apps.wallet.data.model.TransactionsEntityOM

sealed class AssetTransactionsState {
    class ShowAssetTransactions(val assets: List<TransactionsEntityOM>) : AssetTransactionsState()
}
