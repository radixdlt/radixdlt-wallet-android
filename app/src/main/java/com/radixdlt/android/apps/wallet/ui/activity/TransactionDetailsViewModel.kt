package com.radixdlt.android.apps.wallet.ui.activity

import androidx.lifecycle.ViewModel
import com.radixdlt.android.apps.wallet.data.model.transaction.TransactionDetailsLiveData
import javax.inject.Inject

class TransactionDetailsViewModel @Inject constructor(
    val transactions: TransactionDetailsLiveData
) : ViewModel() {

    fun transactionDetailsAddress(address: String, tokenType: String) {
        transactions.address = address
        transactions.tokenType = tokenType
    }
}
