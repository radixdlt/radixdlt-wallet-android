package com.radixdlt.android.apps.wallet.ui.main.assets

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.hadilq.liveevent.LiveEvent
import com.radixdlt.android.apps.wallet.data.model.asset.AssetDao
import com.radixdlt.android.apps.wallet.data.model.asset.AssetEntity
import com.radixdlt.android.apps.wallet.data.model.transaction.TransactionDao
import com.radixdlt.android.apps.wallet.data.model.transaction.TransactionEntity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStreamReader
import javax.inject.Inject

class AssetsViewModel @Inject constructor(
    private val context: Context,
    private val assetDao: AssetDao,
    private val transactionDao: TransactionDao
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val assets = mutableListOf<Asset>()

    private val _assetsAction: MutableLiveData<AssetsAction> = MutableLiveData()
    val assetsAction: LiveData<AssetsAction>
        get() = _assetsAction

    private val _singleAction = LiveEvent<AssetsAction>()
    val singleAction: LiveEvent<AssetsAction>
        get() = _singleAction

    init {
        insertTransactionsIntoDB()
        retrieveAssets()
    }

    private fun retrieveAssets() {
        assetDao.getAllAssets()
            .subscribeOn(Schedulers.io())
            .subscribe {
                setAssetsState(AssetsAction.ShowAssets(it))
            }.addTo(compositeDisposable)
    }

    fun navigateTo() {
        _singleAction.value = AssetsAction.NavigateTo
    }

    fun closeBackUpMnemonicWarningSign() {
        _singleAction.value = AssetsAction.CloseBackUpMnemonicWarning
    }

    private fun setAssetsState(action: AssetsAction) {
        _assetsAction.postValue(action)
    }

    override fun onCleared() {
        assets.clear()
        compositeDisposable.clear()
        super.onCleared()
    }

    private fun insertTransactionsIntoDB() {
        val assets = context.assets.open("dummy_assets.json")
        val transactions = context.assets.open("dummy_transactions_om.json")
        val assetEntityList = Gson().fromJson(
            InputStreamReader(assets),
            Array<AssetEntity>::class.java
        ).toList()
        val transactionsEntityOMList = Gson().fromJson(
            InputStreamReader(transactions),
            Array<TransactionEntity>::class.java
        ).toList()

        viewModelScope.launch(Dispatchers.IO) {
            assetDao.insertAssets(assetEntityList)
            transactionDao.insertTransactions(transactionsEntityOMList)
        }
    }
}
