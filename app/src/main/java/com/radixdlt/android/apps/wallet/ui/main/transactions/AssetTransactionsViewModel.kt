package com.radixdlt.android.apps.wallet.ui.main.transactions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.radixdlt.android.apps.wallet.data.model.asset.AssetDao
import com.radixdlt.android.apps.wallet.data.model.asset.AssetEntity
import com.radixdlt.android.apps.wallet.data.model.transaction.TransactionDao
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class AssetTransactionsViewModel @AssistedInject constructor(
    private val assetDao: AssetDao,
    private val transactionDao: TransactionDao,
    @Assisted private val asset: String
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val _assetTransactionsState: MutableLiveData<AssetTransactionsState> = MutableLiveData()
    val assetTransactionsState: LiveData<AssetTransactionsState>
        get() = _assetTransactionsState

    private val _assetDetails = MutableLiveData<AssetEntity>()
    val assetDetails: LiveData<AssetEntity>
        get() = _assetDetails

    init {
        getAssetDetailsAndTransactions(asset)
    }

    private fun getAssetDetailsAndTransactions(asset: String) {
        getAssetDetails(asset)
        getAssetTransactions(asset)
    }

    private fun getAssetDetails(asset: String) {
        assetDao.getAssetFlowable(asset)
            .subscribeOn(Schedulers.io())
            .subscribe({
                setAssetDetails(it)
            }, Timber::e)
            .addTo(compositeDisposable)
    }

    private fun getAssetTransactions(asset: String) {
        transactionDao.getAllTransactionsFromAsset(asset)
            .subscribeOn(Schedulers.io())
            .subscribe {
                setAssetTransactionState(AssetTransactionsState.ShowAssetTransactions(it))
            }
            .addTo(compositeDisposable)
    }

    private fun setAssetDetails(assetEntity: AssetEntity) {
        _assetDetails.postValue(assetEntity)
    }

    private fun setAssetTransactionState(state: AssetTransactionsState) {
        _assetTransactionsState.postValue(state)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

    @AssistedInject.Factory
    interface Factory {
        fun create(asset: String): AssetTransactionsViewModel
    }
}
