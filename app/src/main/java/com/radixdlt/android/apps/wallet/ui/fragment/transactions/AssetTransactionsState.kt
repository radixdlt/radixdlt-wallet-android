package com.radixdlt.android.apps.wallet.ui.fragment.transactions

import com.radixdlt.android.apps.wallet.data.model.newtransaction.TransactionEntity2

sealed class AssetTransactionsState {
    class ShowAssetTransactions(val assets: List<TransactionEntity2>) : AssetTransactionsState()
    object Error : AssetTransactionsState()
}
