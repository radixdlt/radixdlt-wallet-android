package com.radixdlt.android.apps.wallet.ui.fragment.payment.input

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.radixdlt.android.apps.wallet.data.model.newtransaction.TransactionsDao2
import com.radixdlt.android.apps.wallet.ui.fragment.assets.AssetPayment
import com.radixdlt.android.apps.wallet.util.GENESIS_XRD
import com.radixdlt.android.apps.wallet.util.sumStoredTransactions
import com.radixdlt.client.core.atoms.particles.RRI
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class PaymentInputViewModel @Inject constructor(
    private val transactionsDao: TransactionsDao2
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val availableTokens = mutableListOf<String>()

    private val _asset = MutableLiveData<AssetPayment?>()
    val asset: LiveData<AssetPayment?> get() = _asset

    init {
        retrieveTokenTypes()
    }

    private fun retrieveTokenTypes() {
        transactionsDao.getAllAssets()
            .subscribeOn(Schedulers.io())
            .subscribe({
                availableTokens.clear()
                availableTokens.addAll(it)

                if (it.size > 0 && it.contains(GENESIS_XRD)) {
                    getTransactionsFromAsset(GENESIS_XRD)
                } else {
                    getTransactionsFromAsset(it.first())
                }
            }, Timber::e)
            .addTo(compositeDisposable)
    }

    fun getTransactionsFromAsset(asset: String) {
        val rri = RRI.fromString(asset)
        transactionsDao.getAllTransactionsByTokenType(asset)
            .subscribeOn(Schedulers.io())
            .subscribe {

                if (it.isEmpty()) return@subscribe

                val tokenName = it.first().tokenName
                val tokenUrlIcon = it.first().tokenIconUrl
                val tokenGranularity = it.first().tokenGranularity
                val total = sumStoredTransactions(it).toPlainString()

                val assetData = AssetPayment(
                    tokenName,
                    rri.name,
                    rri.address.toString(),
                    tokenUrlIcon,
                    total,
                    tokenGranularity,
                    false
                )

                _asset.postValue(assetData)
            }
            .addTo(compositeDisposable)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}