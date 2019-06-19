package com.radixdlt.android.apps.wallet.data.model.transaction

import androidx.lifecycle.LiveData
import com.radixdlt.android.apps.wallet.util.TOTAL
import com.radixdlt.android.apps.wallet.util.fixedStripTrailingZeros
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

    private var tokenType = TOTAL

    override fun onActive() {
        super.onActive()
        retrieveWalletBalance(tokenType)
    }

    fun retrieveWalletBalance(tokenType: String) {
        this.tokenType = tokenType

        total = BigDecimal(0)
        lastTransaction = null

        retrieveSumOfStoredTransactions()
        listenToNewTransaction()
    }

    private fun retrieveSumOfStoredTransactions() {
        if (tokenType == TOTAL) {
            transactionsDao.getAllTransactions()
                .subscribeOn(Schedulers.io())
                .subscribe(::calculateBalanceAndPostValue)
                .addTo(compositeDisposable)
        } else {
            transactionsDao.getAllTransactionsByTokenType(tokenType)
                .subscribeOn(Schedulers.io())
                .subscribe(::calculateBalanceAndPostValue)
                .addTo(compositeDisposable)
        }
    }

    private fun calculateBalanceAndPostValue(transactionEntities: List<TransactionEntity>) {
        sumStoredTransactions(transactionEntities)
        lastTransaction = if (transactionEntities.isNotEmpty()) {
            transactionEntities.last()
        } else return
        postValue(total.fixedStripTrailingZeros().toPlainString())
    }

    private fun listenToNewTransaction() {
        if (tokenType == TOTAL) {
            transactionsDao.getLatestTransaction()
                .subscribeOn(Schedulers.io())
                .subscribe(::calculateNewBalanceAndPostValue)
                .addTo(compositeDisposable)
        } else {
            transactionsDao.getLatestTransactionByTokenType(tokenType)
                .subscribeOn(Schedulers.io())
                .subscribe(::calculateNewBalanceAndPostValue)
                .addTo(compositeDisposable)
        }
    }

    private fun calculateNewBalanceAndPostValue(transactionEntity: TransactionEntity) {
        when {
            transactionEntity == lastTransaction -> return
            lastTransaction == null -> retrieveSumOfStoredTransactions()
            else -> calculateNewBalance(transactionEntity)
        }
        postValue(total.fixedStripTrailingZeros().toPlainString())
    }

    private fun sumStoredTransactions(transactionEntities: List<TransactionEntity>) {
        val sumSent = transactionEntities.asSequence().filter { transactions ->
            transactions.sent
        }.map { transactionEntity ->
            BigDecimal(transactionEntity.formattedAmount)
        }.fold(BigDecimal.ZERO, BigDecimal::add)

        val sumReceived = transactionEntities.asSequence().filterNot { transactions ->
            transactions.sent
        }.map { transactionEntity ->
            BigDecimal(transactionEntity.formattedAmount)
        }.fold(BigDecimal.ZERO, BigDecimal::add)

        total = sumReceived - sumSent
    }

    private fun calculateNewBalance(transactionEntity: TransactionEntity) {
        if (transactionEntity.sent) {
            total -= BigDecimal(transactionEntity.formattedAmount)
        } else {
            total += BigDecimal(transactionEntity.formattedAmount)
        }
    }

    override fun onInactive() {
        super.onInactive()
        compositeDisposable.clear()
    }
}
