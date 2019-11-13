package com.radixdlt.android.apps.wallet.ui.fragment.payment.assetselection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.radixdlt.android.apps.wallet.data.model.AssetDao
import com.radixdlt.android.apps.wallet.data.model.AssetEntity
import com.radixdlt.android.apps.wallet.ui.fragment.assets.AssetPayment
import com.radixdlt.client.core.atoms.particles.RRI
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class PaymentAssetSelectionViewModel @Inject constructor(
    private val assetDao: AssetDao
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val assets = mutableListOf<AssetPayment>()

    private val _assetsOwned = MutableLiveData<List<AssetPayment>>()
    val assetsOwned: LiveData<List<AssetPayment>> get() = _assetsOwned

    var selectedAsset = ""

    fun retrieveAssets() {
        assetDao.getAllAssets()
            .subscribeOn(Schedulers.io())
            .subscribe(::mapToAssetPaymentAndPost, Timber::e)
            .addTo(compositeDisposable)
    }

    private fun mapToAssetPaymentAndPost(assets: MutableList<AssetEntity>) {
        assets.forEach {
            val rri = RRI.fromString(it.rri)

            this.assets.add(AssetPayment(
                it.tokenName,
                rri.name,
                rri.address.toString(),
                it.tokenIconUrl,
                it.amount.toString(),
                it.tokenGranularity,
                it.rri == selectedAsset
            ))
        }

        _assetsOwned.postValue(this.assets)
    }

    override fun onCleared() {
        assets.clear()
        compositeDisposable.clear()
        super.onCleared()
    }
}
