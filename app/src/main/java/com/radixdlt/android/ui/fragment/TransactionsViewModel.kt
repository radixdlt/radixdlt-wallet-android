package com.radixdlt.android.ui.fragment

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.radixdlt.android.data.model.transaction.TransactionEntity
import com.radixdlt.android.data.model.transaction.TransactionsDao
import com.radixdlt.android.data.model.transaction.TransactionsRepository
import javax.inject.Inject

class TransactionsViewModel @Inject constructor(
    private val context: Context,
    val balance: LiveData<String>,
    private val transactionsDao: TransactionsDao
) : ViewModel() {

    private var transactionListLiveData = TransactionsRepository(context, transactionsDao)
    val transactionList = MediatorLiveData<MutableList<TransactionEntity>>()

    init {
        this.transactionList.addSource(transactionListLiveData) {
            transactionList.value = it
        }
    }

    fun refresh() {
        transactionList.removeSource(transactionListLiveData)
        transactionListLiveData = TransactionsRepository(context, transactionsDao)
        transactionList.addSource(transactionListLiveData, transactionList::setValue)
    }

    /**
     * Temporary method to [TransactionsRepository]
     * which disposes of disposable according to lifecycle.
     * */
    fun requestTokensFromFaucet() {
        transactionListLiveData.requestTestTokenFromFaucet()
    }
}
