package com.radixdlt.android.data.model.transaction

import androidx.lifecycle.LiveData
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.math.BigDecimal
import javax.inject.Inject

class TokenTypesLiveData @Inject constructor(
    private val transactionsDao: TransactionsDao
) : LiveData<List<String>>() {

    private val compositeDisposable = CompositeDisposable()

    var sendingTokens: Boolean = false

    override fun onActive() {
        super.onActive()
        retrieveTokenTypes()
    }

    private fun retrieveTokenTypes() {
        transactionsDao.getAllTokenTypes()
            .subscribeOn(Schedulers.io())
            .subscribe {
                getAllTransactionsFromEachToken(it)
                Timber.tag("TokenTypes").d(it.toString())
            }
            .addTo(compositeDisposable)
    }

    private fun getAllTransactionsFromEachToken(tokenTypes: MutableList<String>) {
        if (sendingTokens) {
            tokenTypes.forEach {
                transactionsDao.getAllTransactionsByTokenType(it)
                    .subscribeOn(Schedulers.io())
                    .subscribe { transactionEntity ->
                        if (!checkTokenIsPositiveBalance(transactionEntity)) {
                            tokenTypes.remove(it)
                        }
                    }
                    .addTo(compositeDisposable)
            }
        }

        postValue(tokenTypes)
    }

    private fun checkTokenIsPositiveBalance(
        transactionEntity: MutableList<TransactionEntity>
    ): Boolean {
        val sumSent = transactionEntity.asSequence().filter {
            it.sent
        }.map {
            BigDecimal(it.formattedAmount)
        }.fold(BigDecimal.ZERO, BigDecimal::add)

        val sumReceived = transactionEntity.asSequence().filterNot {
            it.sent
        }.map {
            BigDecimal(it.formattedAmount)
        }.fold(BigDecimal.ZERO, BigDecimal::add)

        return ((sumReceived - sumSent) > BigDecimal.ZERO)
    }

    override fun onInactive() {
        super.onInactive()
        compositeDisposable.clear()
    }
}
