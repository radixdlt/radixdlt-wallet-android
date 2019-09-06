package com.radixdlt.android.apps.wallet.ui.fragment.payment.assetselection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.radixdlt.android.apps.wallet.data.model.newtransaction.TransactionsDao2
import com.radixdlt.android.apps.wallet.ui.fragment.assets.Asset
import com.radixdlt.android.apps.wallet.util.sumStoredTransactions
import com.radixdlt.client.core.atoms.particles.RRI
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class PaymentAssetSelectionViewModel @Inject constructor(
    private val transactionsDao2: TransactionsDao2
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val assets = mutableListOf<Asset>()

    private val tokenDefRequested: MutableList<String> = mutableListOf()

    private var numberOfAssets = 0

    private val _assetsOwned = MutableLiveData<List<Asset>>()
    val assetsOwned: LiveData<List<Asset>> get() = _assetsOwned

    init {
        retrieveAssets()
    }

    private fun retrieveAssets() {
        transactionsDao2.getAllAssets()
            .subscribeOn(Schedulers.io())
            .subscribe({
                assets.clear()
                numberOfAssets = it.size
                getAllTransactionsFromEachAsset(it)
            }, {
//                setAssetsState(AssetsState.Error)
                Timber.e(it)
            })
            .addTo(compositeDisposable)
    }

    private fun getAllTransactionsFromEachAsset(assets: MutableList<String>) {
//        val tokenDefRequired: MutableList<String> = mutableListOf()
        assets.forEach { tokenType ->
            val rri = RRI.fromString(tokenType)
            transactionsDao2.getAllTransactionsByTokenType(tokenType)
                .subscribeOn(Schedulers.io())
                .subscribe({

                    val tokenName = it.first().tokenName
                    val tokenUrlIcon = it.first().tokenIconUrl

//                    if (tokenName == null && !tokenDefRequested.contains(it.first().rri)) {
//                        tokenDefRequired.add(it.first().rri)
//                        tokenDefRequested.add(it.first().rri)
//                    }

                    // Get and sum transactions
                    val total = sumStoredTransactions(it).toPlainString()

                    this.assets.add(Asset(tokenName, rri.name, rri.address.toString(), tokenUrlIcon, total))

                    // POST only when we have processed all tokens
                    if (this.assets.isNotEmpty() && this.assets.size == numberOfAssets) {

//                        if (tokenDefRequired.isNotEmpty()) {
//                            setAssetsState(AssetsState.Loading)
//                            pullAtoms(tokenDefRequired)
//                        }
//
//                        if (tokenDefRequested.isEmpty()) {
//                            setAssetsState(AssetsState.ShowAssets(this.assets.sortedBy(Asset::name)))
//                        }

                        _assetsOwned.postValue(this.assets.sortedBy(Asset::name))
                    }
                }, {
                    Timber.e(it)
                })
                .addTo(compositeDisposable)
        }
    }

    override fun onCleared() {
        assets.clear()
        compositeDisposable.clear()
        super.onCleared()
    }
}
