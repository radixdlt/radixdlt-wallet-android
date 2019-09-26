package com.radixdlt.android.apps.wallet.ui.fragment.payment.assetselection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.radixdlt.android.apps.wallet.data.model.newtransaction.TransactionsDao2
import com.radixdlt.android.apps.wallet.ui.fragment.assets.AssetPayment
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

    private val assets = mutableListOf<AssetPayment>()

    private var numberOfAssets = 0

    private val _assetsOwned = MutableLiveData<List<AssetPayment>>()
    val assetsOwned: LiveData<List<AssetPayment>> get() = _assetsOwned

    var selectedAsset = ""

    fun retrieveAssets() {
        transactionsDao2.getAllAssets()
            .subscribeOn(Schedulers.io())
            .subscribe({
                assets.clear()
                numberOfAssets = it.size
                getAllTransactionsFromEachAsset(it)
            }, Timber::e)
            .addTo(compositeDisposable)
    }

    private fun getAllTransactionsFromEachAsset(assets: MutableList<String>) {
        assets.forEach { tokenType ->
            val rri = RRI.fromString(tokenType)
            transactionsDao2.getAllTransactionsByTokenType(tokenType)
                .subscribeOn(Schedulers.io())
                .subscribe({

                    val tokenName = it.first().tokenName
                    val tokenUrlIcon = it.first().tokenIconUrl
                    val tokenGranularity = it.first().tokenGranularity

                    // Get and sum transactions
                    val total = sumStoredTransactions(it).toPlainString()

                    this.assets.add(
                        AssetPayment(
                            tokenName,
                            rri.name,
                            rri.address.toString(),
                            tokenUrlIcon,
                            total,
                            tokenGranularity,
                            tokenType == selectedAsset
                        )
                    )

                    // POST only when we have processed all tokens
                    if (this.assets.isNotEmpty() && this.assets.size == numberOfAssets) {
                        _assetsOwned.postValue(this.assets.sortedBy(AssetPayment::name))
                    }
                }, Timber::e)
                .addTo(compositeDisposable)
        }
    }

    override fun onCleared() {
        assets.clear()
        compositeDisposable.clear()
        super.onCleared()
    }
}
