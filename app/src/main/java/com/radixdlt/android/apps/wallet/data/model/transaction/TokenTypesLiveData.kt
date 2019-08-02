package com.radixdlt.android.apps.wallet.data.model.transaction

import androidx.lifecycle.LiveData
import com.radixdlt.android.apps.wallet.data.model.newtransaction.TransactionEntity2
import com.radixdlt.android.apps.wallet.data.model.newtransaction.TransactionsDao2
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.math.BigDecimal
import javax.inject.Inject

class TokenTypesLiveData @Inject constructor(
    private val transactionsDao: TransactionsDao2
) : LiveData<List<String>>() {

    private val compositeDisposable = CompositeDisposable()

    private val availableTokens = mutableListOf<String>()
    var sendingTokens: Boolean = false

    override fun onActive() {
        super.onActive()
        retrieveTokenTypes()
    }

    private fun retrieveTokenTypes() {
        transactionsDao.getAllAssets()
            .subscribeOn(Schedulers.io())
            .subscribe({
                availableTokens.clear()
                availableTokens.addAll(it)
                getAllTransactionsFromEachToken(it)
            }, { Timber.e(it) })
            .addTo(compositeDisposable)
    }

    private fun getAllTransactionsFromEachToken(tokenTypes: MutableList<String>) {
        if (sendingTokens) {
            tokenTypes.forEach {
                transactionsDao.getAllTransactionsByTokenType(it)
                    .subscribeOn(Schedulers.io())
                    .subscribe { transactionEntity ->
                        if (!checkTokenIsPositiveBalance(transactionEntity)) {
                            availableTokens.remove(it)
                        }
                    }
                    .addTo(compositeDisposable)
            }
        }

        postValue(availableTokens)
    }

    private fun checkTokenIsPositiveBalance(
        transactionEntity: List<TransactionEntity2>
    ): Boolean {
        val sumSent = transactionEntity.asSequence().filter {
            it.sent
        }.map {
            it.amount
        }.fold(BigDecimal.ZERO, BigDecimal::add)

        val sumReceived = transactionEntity.asSequence().filterNot {
            it.sent
        }.map {
            it.amount
        }.fold(BigDecimal.ZERO, BigDecimal::add)

        return ((sumReceived - sumSent) > BigDecimal.ZERO)
    }

    override fun onInactive() {
        super.onInactive()
        Timber.d("TokenTypesLiveData onInactive $sendingTokens")
        availableTokens.clear()
        compositeDisposable.clear()
    }
}
