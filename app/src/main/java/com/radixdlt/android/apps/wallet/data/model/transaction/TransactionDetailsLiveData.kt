package com.radixdlt.android.apps.wallet.data.model.transaction

import androidx.lifecycle.LiveData
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class TransactionDetailsLiveData @Inject constructor(
    private val transactionsDao: TransactionsDao
) : LiveData<TransactionDetails>() {

    lateinit var address: String
    lateinit var tokenType: String

    private val compositeDisposable = CompositeDisposable()

    override fun onActive() {
        super.onActive()
        retrieveStoredTransactionsByAddress()
    }

    private fun retrieveStoredTransactionsByAddress() {
        transactionsDao.getAllTransactionsByAddressAndToken(address, tokenType)
            .subscribeOn(Schedulers.io())
            .subscribe {
                val sent = calculateTotalSent(it)
                val received = calculateTotalReceived(it)

                val transactionDetails = TransactionDetails(
                    it, sent.first, sent.second, received.first, received.second
                )

                postValue(transactionDetails)
            }
            .addTo(compositeDisposable)
    }

    private fun calculateTotalSent(
        transactions: MutableList<TransactionEntity>
    ): Pair<Int, String> {
        val sentTransactionEntities = transactions.filter {
            it.sent
        }

        val total = sentTransactionEntities.map {
            it.formattedAmount.toBigDecimal()
        }.fold(BigDecimal.ZERO, BigDecimal::add)

        return Pair(
            sentTransactionEntities.size,
            total.setScale(5, RoundingMode.HALF_UP).toPlainString()
        )
    }

    private fun calculateTotalReceived(
        transactions: MutableList<TransactionEntity>
    ): Pair<Int, String> {

        val receivedTransactionEntities = transactions.filterNot {
            it.sent
        }

        val total = receivedTransactionEntities.map {
            it.formattedAmount.substring(1).toBigDecimal()
        }.fold(BigDecimal.ZERO, BigDecimal::add)

        return Pair(
            receivedTransactionEntities.size,
            total.setScale(5, RoundingMode.HALF_UP).toPlainString()
        )
    }

    override fun onInactive() {
        super.onInactive()
        compositeDisposable.clear()
    }
}
