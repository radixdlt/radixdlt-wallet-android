package com.radixdlt.android.apps.wallet.ui.fragment.assets

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.radixdlt.android.apps.wallet.data.model.newtransaction.TransactionsDao2
import com.radixdlt.android.apps.wallet.identity.Identity
import com.radixdlt.android.apps.wallet.util.sumStoredTransactions
import com.radixdlt.client.core.atoms.particles.RRI
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class AssetsViewModel @Inject constructor(
    private val transactionsDao2: TransactionsDao2
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val assets = mutableListOf<Asset>()

    private val tokenDefRequested: MutableList<String> = mutableListOf()

    private var numberOfAssets = 0

    private val _assetsAction: MutableLiveData<AssetsAction?> = MutableLiveData()
    val assetsAction: LiveData<AssetsAction?>
        get() = _assetsAction

    init {
        retrieveAssets()
    }

    fun navigateTo() {
        _assetsAction.value = AssetsAction.NavigateTo
        resetActionValue()
    }

    fun closeBackUpMnemonicWarningSign() {
        _assetsAction.value = AssetsAction.CloseBackUpMnemonicWarning
        resetActionValue()
    }

    private fun resetActionValue() {
        _assetsAction.value = null
    }

    private fun retrieveAssets() {
        transactionsDao2.getAllAssets()
            .subscribeOn(Schedulers.io())
            .subscribe({
                assets.clear()
                numberOfAssets = it.size
                getAllTransactionsFromEachAsset(it)
            }, {
                setAssetsState(AssetsAction.ShowError)
                Timber.e(it)
            })
            .addTo(compositeDisposable)
    }

    private fun pullAtoms(assets: MutableList<String>) {
        var counter = 0
        val addresses = assets.map { RRI.fromString(it).address }.distinct()

        addresses.forEach { address ->
            if (Identity.api == null) return@forEach
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
                    if (this.assets.isNotEmpty() && this.assets.size == numberOfAssets) {

                        if (tokenDefRequired.isNotEmpty()) {
                            setAssetsState(AssetsAction.ShowLoading)
                            pullAtoms(tokenDefRequired)
                        }

                        if (tokenDefRequested.isEmpty()) {
                            setAssetsState(AssetsAction.ShowAssets(this.assets.sortedBy(Asset::name)))
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
                    transactionsDao2.updateEntities(
                        tokenState.name,
                        tokenState.description,
                        tokenState.iconUrl ?: "",
                        rriString,
                        tokenState.totalSupply,
                        tokenState.granularity,
                        tokenState.tokenSupplyType.name
                    )
                }, {
                    Timber.e(it)
                }).addTo(compositeDisposable)
        }
    }

    private fun setAssetsState(action: AssetsAction) {
        _assetsAction.postValue(action)
    }

    override fun onCleared() {
        assets.clear()
        compositeDisposable.clear()
        super.onCleared()
    }
}
