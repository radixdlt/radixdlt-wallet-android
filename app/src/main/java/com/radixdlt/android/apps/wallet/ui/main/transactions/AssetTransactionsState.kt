package com.radixdlt.android.apps.wallet.ui.main.transactions

import com.radixdlt.android.apps.wallet.data.model.transaction.TransactionEntity

sealed class AssetTransactionsState {
    class ShowAssetTransactions(val assets: List<TransactionEntity>) : AssetTransactionsState()
}
