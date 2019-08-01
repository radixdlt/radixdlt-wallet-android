package com.radixdlt.android.apps.wallet.ui.fragment.assets

import androidx.lifecycle.LiveData
import com.radixdlt.android.apps.wallet.data.model.newtransaction.TransactionEntity2
import com.radixdlt.android.apps.wallet.data.model.newtransaction.TransactionsDao2
import com.radixdlt.android.apps.wallet.identity.Identity
import com.radixdlt.client.core.atoms.particles.RRI
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.math.BigDecimal
import javax.inject.Inject

class AssetsLiveData @Inject constructor(
    private val transactionsDao2: TransactionsDao2
) : LiveData<AssetsState>() {

    private val compositeDisposable = CompositeDisposable()

    private val assets = mutableListOf<Asset>()

    private val tokenDefRequested: MutableList<String> = mutableListOf()

    private var numberOfAssets = 0

    override fun onActive() {
        super.onActive()
        retrieveAssets()
    }

    private fun retrieveAssets() {
        transactionsDao2.getAllAssets()
            .subscribeOn(Schedulers.io())
            .subscribe({
                assets.clear()
                numberOfAssets = it.size
                getAllTransactionsFromEachAsset(it)
            }, { Timber.e(it) })
            .addTo(compositeDisposable)
    }

    private fun pullAtoms(assets: MutableList<String>) {
        var counter = 0
        val addresses = assets.map { RRI.fromString(it).address }.distinct()

        addresses.forEach { address ->
            Identity.api!!
                .pullOnce(address)
                .subscribeOn(Schedulers.io())
                .subscribe {
                    counter++
                    if (counter == addresses.size) {
                        retrieveTokenDefinition(assets)
                    }
                }
                .addTo(compositeDisposable)
        }
    }

    private fun getAllTransactionsFromEachAsset(assets: MutableList<String>) {
        val tokenDefRequired: MutableList<String> = mutableListOf()
        assets.forEach { tokenType ->
            val rri = RRI.fromString(tokenType)
            transactionsDao2.getAllTransactionsByTokenType(tokenType)
                .subscribeOn(Schedulers.io())
                .subscribe({

                    val tokenName = it.first().tokenName
                    val tokenUrlIcon = it.first().tokenIconUrl

                    if (tokenName == null && !tokenDefRequested.contains(it.first().rri)) {
                        tokenDefRequired.add(it.first().rri)
                        tokenDefRequested.add(it.first().rri)
                    }

                    // Get and sum transactions
                    val total = sumStoredTransactions(it).toPlainString()

                    this.assets.add(Asset(tokenName, rri.name, rri.address.toString(), tokenUrlIcon, total))

                    // POST only when we have processed all tokens
                    if (this.assets.size == numberOfAssets) {

                        if (tokenDefRequired.isNotEmpty()) {
                            pullAtoms(tokenDefRequired)
                        }

                        if (tokenDefRequested.isEmpty()) {
                            postValue(AssetsState.Assets(this.assets.sortedBy(Asset::name)))
                        }
                    }

                }, {
                    Timber.e(it)
                })
                .addTo(compositeDisposable)
        }
    }

    private fun retrieveTokenDefinition(assets: MutableList<String>) {

        assets.forEach { rriString ->
            Identity.api!!.observeTokenDef(RRI.fromString(rriString))
                .firstOrError() // Converts Observable to Single
                .subscribeOn(Schedulers.io())
                .subscribe({ tokenState ->

                    // Remove from requested list as obtained
                    tokenDefRequested.remove(rriString)

                    // Update entities in DB
                    // FIXME: use urlIcon in next release of beta library for now use below to test
                    val tokenIconUrl = "https://styles.redditmedia.com/t5_2qjzo/styles/communityIcon_o0xuar6bbpo21.png"
                    transactionsDao2.updateEntities(
                        tokenState.name,
                        tokenState.description,
                        tokenIconUrl,
                        rriString,
                        tokenState.totalSupply,
                        tokenState.tokenSupplyType.name
                    )
                }, {
                    Timber.e(it)
                }).addTo(compositeDisposable)
        }
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

    override fun onInactive() {
        super.onInactive()
        assets.clear()
        compositeDisposable.clear()
    }
}
