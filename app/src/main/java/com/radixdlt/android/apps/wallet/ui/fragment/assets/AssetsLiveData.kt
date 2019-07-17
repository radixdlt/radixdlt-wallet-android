package com.radixdlt.android.apps.wallet.ui.fragment.assets

import androidx.lifecycle.LiveData
import com.radixdlt.android.apps.wallet.data.model.transaction.TransactionEntity
import com.radixdlt.android.apps.wallet.data.model.transaction.TransactionsDao
import com.radixdlt.android.apps.wallet.identity.Identity
import com.radixdlt.client.core.atoms.particles.RRI
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.math.BigDecimal
import javax.inject.Inject

class AssetsLiveData @Inject constructor(
    private val transactionsDao: TransactionsDao
) : LiveData<AssetsState>() {

    private val compositeDisposable = CompositeDisposable()

    private val assets = mutableListOf<Asset>()

    private var numberOfOwnedTokens = 0

    override fun onActive() {
        super.onActive()
        retrieveTokenTypes()
    }

    private fun retrieveTokenTypes() {
        transactionsDao.getAllTokenTypes()
            .subscribeOn(Schedulers.io())
            .subscribe({
                Timber.d("retrieveTokenTypes ${it.size}")
                assets.clear() // if a new transaction is received it will call the DB again
                numberOfOwnedTokens = it.size
                pullAtoms(it)
            }, { Timber.e(it) })
            .addTo(compositeDisposable)
    }

    private fun pullAtoms(tokenTypes: MutableList<String>) {
        var counter = 0
        val addresses = tokenTypes.map { RRI.fromString(it).address }.distinct()

        Timber.tag("ASSETS").d("Number of assets: ${addresses.size}")

        addresses.forEach { address ->
            Identity.api!!
                .pullOnce(address)
                .subscribeOn(Schedulers.io())
                .subscribe {
                    counter++
                    if (counter == addresses.size) {
                        getAllTransactionsFromEachToken(tokenTypes)
                    }
                }
                .addTo(compositeDisposable)
        }
    }

    private fun getAllTransactionsFromEachToken(tokenTypes: MutableList<String>) {
        tokenTypes.forEach { tokenType ->
            val rri = RRI.fromString(tokenType)
            transactionsDao.getAllTransactionsByTokenType(tokenType)
                .subscribeOn(Schedulers.io())
                .subscribe({

                    // Get and sum transactions
                    val total = sumStoredTransactions(it).toPlainString()
                    Timber.d("total ${it.first().tokenClassISO}")

                    retrieveTokenDefinition(rri, total)
                }, {
                    Timber.e(it)
                })
                .addTo(compositeDisposable)
        }
    }

    private fun retrieveTokenDefinition(rri: RRI, total: String) {
        Identity.api!!.observeTokenDef(rri)
            .firstOrError() // Converting Observable to Single
            .subscribeOn(Schedulers.io())
            .subscribe({
                Timber.d(it.toString())

                assets.add(Asset(it.name, it.iso, rri.address.toString(), "url", total))
                Timber.tag("ASSETS").d("${it.name} COMPLETED")

                // POST only when we have processed all tokens
                if (assets.size == numberOfOwnedTokens) {
                    postValue(AssetsState.Assets(assets.sortedBy(Asset::name)))
                    Timber.tag("ASSETS").d("${it.name} POST")
                }
            }, {
                Timber.e(it)
            }).addTo(compositeDisposable)
    }

    private fun retrieveTokenDefinition(rri: RRI) {
        Identity.api!!.observeTokenDef(rri)
            .firstOrError() // Converting Observable to Single
            .subscribeOn(Schedulers.io())
            .subscribe({
                Timber.d(it.toString())
            }, {
                Timber.e(it)
            }).addTo(compositeDisposable)
    }

    private fun addDummyTokens() {
        assets.clear()
        assets.add(Asset("Rads", "XRD", "afakeaddress", "url", "5000"))
        assets.add(Asset("True USD", "TRU", "afakeaddress", "url", "200"))
        assets.add(Asset("Paxos Standard", "PAX", "afakeaddress", "url", "10000"))
        assets.add(Asset("Bitcoin Rads", "BTCR", "afakeaddress", "url", "15000"))

        postValue(AssetsState.Assets(assets))
    }

    private fun sumStoredTransactions(transactionEntities: List<TransactionEntity>): BigDecimal {
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

        return sumReceived - sumSent
    }

    override fun onInactive() {
        super.onInactive()
        assets.clear()
        numberOfOwnedTokens = 0
        compositeDisposable.clear()
    }
}
