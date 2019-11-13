package com.radixdlt.android.apps.wallet.ui.fragment.assets

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hadilq.liveevent.LiveEvent
import com.radixdlt.android.apps.wallet.data.model.AssetDao
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class AssetsViewModel @Inject constructor(
    private val assetDao: AssetDao
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
}
