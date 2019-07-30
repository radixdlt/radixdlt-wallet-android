package com.radixdlt.android.apps.wallet.ui.fragment.transactions

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.radixdlt.android.apps.wallet.data.model.newtransaction.TransactionEntity2
import com.radixdlt.android.apps.wallet.data.model.newtransaction.TransactionsDao2
import com.radixdlt.android.apps.wallet.data.model.newtransaction.TransactionsRepository2
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.math.BigDecimal
import javax.inject.Inject

class AssetTransactionsViewModel @Inject constructor(
    context: Context,
    private val transactionsDao2: TransactionsDao2
) : ViewModel() {

    private var transactionsRepository = TransactionsRepository2(context, transactionsDao2)
    val transactionList = MediatorLiveData<MutableList<TransactionEntity2>>()

    init {
        this.transactionList.addSource(transactionsRepository) {
            transactionList.value = it
        }
    }

    private val compositeDisposable = CompositeDisposable()

    private val _assetTransactionsState: MutableLiveData<AssetTransactionsState> = MutableLiveData()

    val assetTransactionsState: LiveData<AssetTransactionsState>
        get() = _assetTransactionsState

    private val _assetBalance: MutableLiveData<String> = MutableLiveData()

    val assetBalance: LiveData<String>
        get() = _assetBalance

    fun getAllTransactionsAsset(asset: String) {
        Timber.d("getAllTransactionsAsset")
        transactionsDao2.getAllTransactionsByTokenTypeFlowable(asset)
            .subscribeOn(Schedulers.io())
            .subscribe({

                // Get and sum transactions
                val total = sumStoredTransactions(it).toPlainString()

                setAssetBalance(total)
                setAssetTransactionState(
                    AssetTransactionsState.ShowAssetTransactions(
                        it.sortedByDescending(TransactionEntity2::timestamp)
                    )
                )

            }, {
                Timber.e(it)
            })
            .addTo(compositeDisposable)
    }

    private fun sumStoredTransactions(transactionEntities: List<TransactionEntity2>): BigDecimal {
        val sumSent = transactionEntities.asSequence().filter { transactions ->
            transactions.sent
        }.map { transactionEntity ->
            transactionEntity.amount
        }.fold(BigDecimal.ZERO, BigDecimal::add)

        val sumReceived = transactionEntities.asSequence().filterNot { transactions ->
            transactions.sent
        }.map { transactionEntity ->
            transactionEntity.amount
        }.fold(BigDecimal.ZERO, BigDecimal::add)

        return sumReceived - sumSent
    }

    private fun setAssetBalance(balance: String) {
        _assetBalance.postValue(balance)
    }

    private fun setAssetTransactionState(state: AssetTransactionsState) {
        _assetTransactionsState.postValue(state)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
