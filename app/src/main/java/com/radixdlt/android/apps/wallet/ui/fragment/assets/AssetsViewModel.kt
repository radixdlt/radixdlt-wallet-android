package com.radixdlt.android.apps.wallet.ui.fragment.assets

import android.content.Context
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.radixdlt.android.apps.wallet.data.model.transaction.TransactionEntity
import com.radixdlt.android.apps.wallet.data.model.transaction.TransactionsDao
import com.radixdlt.android.apps.wallet.data.model.transaction.TransactionsRepository
import javax.inject.Inject
import javax.inject.Named

class AssetsViewModel @Inject constructor(
    private val context: Context,
    @Named("assets") val assetsLiveData: AssetsLiveData,
    private val transactionsDao: TransactionsDao
) : ViewModel() {

    private var transactionsRepository = TransactionsRepository(context, transactionsDao)
    val transactionList = MediatorLiveData<MutableList<TransactionEntity>>()

    init {
        this.transactionList.addSource(transactionsRepository) {
            transactionList.value = it
        }
    }

    fun refresh() {
        transactionList.removeSource(transactionsRepository)
        transactionsRepository = TransactionsRepository(context, transactionsDao)
        transactionList.addSource(transactionsRepository, transactionList::setValue)
    }
}
