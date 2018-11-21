package com.radixdlt.android.data.model.transaction

import androidx.lifecycle.LiveData
import com.radixdlt.android.util.fixedStripTrailingZeros
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import java.math.BigDecimal
import javax.inject.Inject

class BalanceLiveData @Inject constructor(
    private val transactionsDao: TransactionsDao
) : LiveData<String>() {

    private val compositeDisposable = CompositeDisposable()

    private var total: BigDecimal = BigDecimal(0)

    private var lastTransaction: TransactionEntity? = null

    override fun onActive() {
        super.onActive()
        retrieveWalletBalance()
    }

    private fun retrieveWalletBalance() {
        retrieveSumOfAllStoredTransactions()
        listenToNewTransaction()
    }

    private fun retrieveSumOfAllStoredTransactions() {
        transactionsDao.getAllTransactions()
            .subscribeOn(Schedulers.io())
            .subscribe {
                sumStoredTransactions(it)
                lastTransaction = if (it.isNotEmpty()) it.last() else return@subscribe
                postValue(total.fixedStripTrailingZeros().toPlainString())
            }.addTo(compositeDisposable)
    }

    private fun listenToNewTransaction() {
        transactionsDao.getLatestTransaction()
            .subscribeOn(Schedulers.io())
            .subscribe {
                when {
                    it == lastTransaction -> return@subscribe
                    lastTransaction == null -> retrieveSumOfAllStoredTransactions()
                    else -> calculateNewBalance(it)
                }
                postValue(total.fixedStripTrailingZeros().toPlainString())
            }.addTo(compositeDisposable)
    }

    private fun sumStoredTransactions(it: MutableList<TransactionEntity>) {
        val sumSent = it.asSequence().filter { transactions ->
            transactions.sent
        }.map { transactionEntity ->
            BigDecimal(transactionEntity.formattedAmount)
        }.fold(BigDecimal.ZERO, BigDecimal::add)

        val sumReceived = it.asSequence().filterNot { transactions ->
            transactions.sent
        }.map { transactionEntity ->
            BigDecimal(transactionEntity.formattedAmount)
        }.fold(BigDecimal.ZERO, BigDecimal::add)

        total = sumReceived - sumSent
    }

    private fun calculateNewBalance(it: TransactionEntity) {
        if (it.sent) {
            total -= BigDecimal(it.formattedAmount)
        } else {
            total += BigDecimal(it.formattedAmount)
        }
    }

    override fun onInactive() {
        super.onInactive()
        compositeDisposable.clear()
    }
}
