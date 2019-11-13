package com.radixdlt.android.apps.wallet.ui.fragment.payment.input

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.radixdlt.android.apps.wallet.data.model.AssetDao
import com.radixdlt.android.apps.wallet.data.model.AssetEntity
import com.radixdlt.android.apps.wallet.ui.fragment.assets.AssetPayment
import com.radixdlt.android.apps.wallet.util.GENESIS_XRD
import com.radixdlt.client.core.atoms.particles.RRI
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class PaymentInputViewModel @Inject constructor(
    private val assetDao: AssetDao
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val _asset = MutableLiveData<AssetPayment?>()
    val asset: LiveData<AssetPayment?> get() = _asset

    init {
        retrieveTokenTypes()
    }

    private fun retrieveTokenTypes() {
        assetDao.getAllAssets()
            .subscribeOn(Schedulers.io())
            .subscribe(::checkAvailableAssets, Timber::e)
            .addTo(compositeDisposable)
    }

    fun getTransactionsFromAsset(asset: String) {
        assetDao.getAsset(asset)
            .subscribeOn(Schedulers.io())
            .subscribe(::mapToAssetPaymentAndPost, Timber::e)
            .addTo(compositeDisposable)
    }

    private fun checkAvailableAssets(assets: MutableList<AssetEntity>) {
        val asset: AssetEntity =
            if (assets.size > 0 && assets.any { it.rri == GENESIS_XRD }) {
                assets.find { it.rri == GENESIS_XRD } ?: assets.first()
            } else {
                assets.first()
            }

        mapToAssetPaymentAndPost(asset)
    }

    private fun mapToAssetPaymentAndPost(asset: AssetEntity) {
        val rri = RRI.fromString(asset.rri)

        val assetData = AssetPayment(
            asset.tokenName,
            rri.name,
            rri.address.toString(),
            asset.tokenIconUrl,
            asset.amount.toString(),
            asset.tokenGranularity,
            false
        )

        _asset.postValue(assetData)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
