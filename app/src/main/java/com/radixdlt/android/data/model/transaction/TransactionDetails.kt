package com.radixdlt.android.data.model.transaction

data class TransactionDetails(
    val allTransactions: List<TransactionEntity>,
    val sentSize: Int,
    val sentTotal: String,
    val receivedSize: Int,
    val receivedTotal: String
)
