package com.radixdlt.android.apps.wallet.ui.fragment.transactions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.radixdlt.android.apps.wallet.data.model.newtransaction.TransactionEntity2
import com.radixdlt.android.apps.wallet.data.model.newtransaction.TransactionsDao2
import com.radixdlt.android.apps.wallet.util.sumStoredTransactions
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class AssetTransactionsViewModel @Inject constructor(
    private val transactionsDao2: TransactionsDao2
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val _assetTransactionsState: MutableLiveData<AssetTransactionsState> = MutableLiveData()

    val assetTransactionsState: LiveData<AssetTransactionsState>
        get() = _assetTransactionsState

    private val _assetBalance: MutableLiveData<String> = MutableLiveData()

    val assetBalance: LiveData<String>
        get() = _assetBalance

    fun getAllTransactionsAsset(asset: String) {
        Timber.d("getAllTransactionsAsset")
        transactionsDao2.getAllTransactionsByTokenTypeFlowable(asset)
            .subscribeOn(Schedulers.io())
            .subscribe({

                // Get and sum transactions
                val total = sumStoredTransactions(it).toPlainString()

                setAssetBalance(total)
                setAssetTransactionState(
                    AssetTransactionsState.ShowAssetTransactions(
                        it.sortedByDescending(TransactionEntity2::timestamp)
                    )
                )

            }, {
                Timber.e(it)
            })
            .addTo(compositeDisposable)
    }

    private fun setAssetBalance(balance: String) {
        _assetBalance.postValue(balance)
    }

    private fun setAssetTransactionState(state: AssetTransactionsState) {
        _assetTransactionsState.postValue(state)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
