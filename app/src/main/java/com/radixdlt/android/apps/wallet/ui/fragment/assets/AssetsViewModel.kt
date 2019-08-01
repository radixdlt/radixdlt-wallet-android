package com.radixdlt.android.apps.wallet.ui.fragment.assets

import android.content.Context
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.radixdlt.android.apps.wallet.data.model.newtransaction.TransactionEntity2
import com.radixdlt.android.apps.wallet.data.model.newtransaction.TransactionsDao2
import com.radixdlt.android.apps.wallet.data.model.newtransaction.TransactionsRepository2
import javax.inject.Inject
import javax.inject.Named

class AssetsViewModel @Inject constructor(
    private val context: Context,
    @Named("assets") val assetsLiveData: AssetsLiveData,
    private val transactionsDao2: TransactionsDao2
) : ViewModel() {

    private var transactionsRepository = TransactionsRepository2(context, transactionsDao2)
    val transactionList = MediatorLiveData<MutableList<TransactionEntity2>>()

    init {
        this.transactionList.addSource(transactionsRepository) {
            transactionList.value = it
        }
    }

    fun refresh() {
        transactionList.removeSource(transactionsRepository)
        transactionsRepository = TransactionsRepository2(context, transactionsDao2)
        transactionList.addSource(transactionsRepository, transactionList::setValue)
    }
}
