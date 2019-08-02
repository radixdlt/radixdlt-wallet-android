package com.radixdlt.android.apps.wallet.data.model.transaction

import com.radixdlt.android.apps.wallet.data.model.newtransaction.TransactionEntity2

data class TransactionDetails(
    val allTransactions: List<TransactionEntity2>,
    val sentSize: Int,
    val sentTotal: String,
    val receivedSize: Int,
    val receivedTotal: String
)
