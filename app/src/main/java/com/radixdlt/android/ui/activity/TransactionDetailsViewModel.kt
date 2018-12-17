package com.radixdlt.android.ui.activity

import androidx.lifecycle.ViewModel
import com.radixdlt.android.data.model.transaction.TransactionDetailsLiveData
import javax.inject.Inject

class TransactionDetailsViewModel @Inject constructor(
    val transactions: TransactionDetailsLiveData
) : ViewModel() {

    fun transactionDetailsAddress(address: String, tokenType: String) {
        transactions.address = address
        transactions.tokenType = tokenType
    }
}
