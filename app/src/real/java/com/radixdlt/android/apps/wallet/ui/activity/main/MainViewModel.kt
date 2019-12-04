package com.radixdlt.android.apps.wallet.ui.activity.main

import androidx.annotation.IdRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.EmptyResultSetException
import com.hadilq.liveevent.LiveEvent
import com.radixdlt.android.apps.wallet.connectivity.ConnectivityLiveData
import com.radixdlt.android.apps.wallet.data.mapper.TokenTransferDataMapperOM
import com.radixdlt.android.apps.wallet.data.model.AssetDao
import com.radixdlt.android.apps.wallet.data.model.AssetEntity
import com.radixdlt.android.apps.wallet.data.model.TransactionsDaoOM
import com.radixdlt.android.apps.wallet.data.model.TransactionsEntityOM
import com.radixdlt.android.apps.wallet.identity.Identity
import com.radixdlt.android.apps.wallet.util.sumStoredTransactionsOM
import com.radixdlt.client.application.translate.tokens.TokenTransfer
import com.radixdlt.client.core.atoms.particles.RRI
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val assetDao: AssetDao,
    private val transactionsDaoOM: TransactionsDaoOM,
    val connectivityLiveData: ConnectivityLiveData
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val myAddress = Identity.api!!.address.toString()

    private val _mainLoadingState: MutableLiveData<MainLoadingState> = MutableLiveData()

    val mainLoadingState: LiveData<MainLoadingState>
        get() = _mainLoadingState

    private val _navigationCheckedItem = MutableLiveData<Int>()
    val navigationCheckedItem: LiveData<Int> get() = _navigationCheckedItem

    private val _showBackUpWalletNotification = MutableLiveData<Boolean>()
    val showBackUpWalletNotification: LiveData<Boolean>
        get() = _showBackUpWalletNotification

    private val _launchAuthenticationAction = LiveEvent<LaunchAuthenticationAction>()
    val launchAuthenticationAction: LiveEvent<LaunchAuthenticationAction>
        get() = _launchAuthenticationAction

    init {
        _showBackUpWalletNotification.value = true
        _mainLoadingState.value = MainLoadingState.LOADING
        retrieveAllTransactions()
    }

    fun unlock() {
        _launchAuthenticationAction.value = LaunchAuthenticationAction.UNLOCK
    }

    fun usePin() {
        _launchAuthenticationAction.value = LaunchAuthenticationAction.USE_PIN
    }

    fun logout() {
        _launchAuthenticationAction.value = LaunchAuthenticationAction.LOGOUT
    }

    fun setBottomNavigationCheckedItem(@IdRes item: Int) {
        _navigationCheckedItem.value = item
    }

    fun showBackUpWalletNotification(show: Boolean) {
        _showBackUpWalletNotification.value = show
    }

    private fun retrieveAllTransactions() {

        val radixWalletTransactionsObservable = Identity.api!!.observeTokenTransfers()

        val allTransactions: Observable<TokenTransfer> = radixWalletTransactionsObservable
            .publish()
            .refCount(2)

        val oldTransactionsList = retrieveListOfOldTransactions(allTransactions)

        listenToNewTransactions(oldTransactionsList, allTransactions)
    }

    private fun listenToNewTransactions(
        oldTransactionsList: Single<ArrayList<TokenTransfer>>,
        allTransactions: Observable<TokenTransfer>
    ) {
        Observables.combineLatest(
            oldTransactionsList.toObservable(),
            allTransactions
        ) { listOfOld, transaction ->
            if (listOfOld.contains(transaction)) {
                Maybe.empty()
            } else {
                Maybe.just(transaction)
            }
        }.flatMapMaybe { it }
            .map {
                //                TokenTransferDataMapper2.transform(it, myAddress)
                TokenTransferDataMapperOM.transform(it, myAddress)
            }
            .subscribeOn(Schedulers.io())
//            .subscribe(::attemptToRetrieveLastTransactionForAsset, Throwable::printStackTrace)
            .subscribe(::insertTransaction, Throwable::printStackTrace)
            .addTo(compositeDisposable)
    }

    private fun insertTransaction(transactionsEntityOM: TransactionsEntityOM) {
        Timber.tag("ASSETSOM").d("New Transaction")
        // check asset exists
        assetDao.getAsset(transactionsEntityOM.rri)
            .subscribeOn(Schedulers.io())
            .subscribe({
                transactionsDaoOM.insertTransaction(transactionsEntityOM)
                if (transactionsEntityOM.sent) {
                    assetDao.updateEntityAmount(transactionsEntityOM.rri, -transactionsEntityOM.amount)
                } else {
                    assetDao.updateEntityAmount(transactionsEntityOM.rri, transactionsEntityOM.amount)
                }
                if (it.tokenIconUrl == null) {
                    pullAtoms(it.rri)
                }
            }, {
                Timber.e(it)
                if (it is EmptyResultSetException) {
                    Timber.e(it)
                    insertAsset(transactionsEntityOM)
                }
            })
            .addTo(compositeDisposable)
    }

    private fun retrieveListOfOldTransactions(
        allTransactions: Observable<TokenTransfer>
    ): Single<ArrayList<TokenTransfer>> {

        val oldTransactionsList: Single<ArrayList<TokenTransfer>> = allTransactions
            .scan(ArrayList<TokenTransfer>()) { list, transaction ->
                list.add(transaction)
                return@scan list
            }
            .debounce(5, TimeUnit.SECONDS)
            .firstOrError()
            .cache()

        oldTransactionsList
            .toObservable()
            .flatMapIterable { list -> list }
            .map {
                //                TokenTransferDataMapper2.transform(it, myAddress)
                TokenTransferDataMapperOM.transform(it, myAddress)
            }
            .toList()
            .subscribeOn(Schedulers.io())
            .subscribe({
                //                transactionsDao2.insertTransactions(it) // insert in DB

                insertAssets(it)

                setFinishedStateAfterDelay()

                Timber.tag("newOM").d("The transactions $it")
            }, Throwable::printStackTrace)
            .addTo(compositeDisposable)

        return oldTransactionsList
    }

    private fun insertAssets(transactionEntityOMList: MutableList<TransactionsEntityOM>) {
        Timber.tag("ASSETSOM").d("Old Transactions List")

        val rriList = transactionEntityOMList.distinctBy { it.rri }.map { it.rri }

        rriList.forEach { rri ->

            val transactions = transactionEntityOMList.filter { it.rri == rri }

            // CHECK ASSET EXISTS
            assetDao.getAsset(rri)
                .subscribeOn(Schedulers.io())
                .subscribe({
                    transactionsDaoOM.insertTransactions(transactions)
                    if (it.tokenIconUrl == null) {
                        pullAtoms(it.rri)
                    }
                }, {
                    // DB hasn't got the asset
                    if (it is EmptyResultSetException) {
                        val transactionsByRRI = transactionEntityOMList.filter { list ->
                            list.rri == rri
                        }

                        val sum = sumStoredTransactionsOM(transactionsByRRI)

                        val assetEntity = AssetEntity(
                            Identity.api!!.address.toString(),
                            rri,
                            RRI.fromString(rri).name,
                            null,
                            null,
                            null,
                            null,
                            null,
                            sum
                        )

                        assetDao.insertAsset(assetEntity)
                        transactionsDaoOM.insertTransactions(transactions)

                        // pull atoms to get token definition data
                        pullAtoms(rri)
                    }

                })
                .addTo(compositeDisposable)
        }
    }

    private fun insertAsset(transactionEntityOM: TransactionsEntityOM) {

        val assetEntity = AssetEntity(
            Identity.api!!.address.toString(),
            transactionEntityOM.rri,
            RRI.fromString(transactionEntityOM.rri).name,
            null,
            null,
            null,
            null,
            null,
            transactionEntityOM.amount
        )

        assetDao.insertAsset(assetEntity)
        transactionsDaoOM.insertTransaction(transactionEntityOM)

        // pull atoms to get token definition data
        pullAtoms(transactionEntityOM.rri)
    }

    private fun pullAtoms(rri: String) {
        val address = RRI.fromString(rri).address
        Identity.api!!
            .pullOnce(address)
            .subscribeOn(Schedulers.io())
            .subscribe {
                retrieveTokenDefinition(rri)
            }
            .addTo(compositeDisposable)
    }

    private fun retrieveTokenDefinition(rri: String) {
        Identity.api!!.observeTokenDef(RRI.fromString(rri))
            .firstOrError() // Converts Observable to Single
            .subscribeOn(Schedulers.io())
            .subscribe({ tokenState ->

                // Update entities in DB
                assetDao.updateEntities(
                    rri,
                    tokenState.name,
                    tokenState.description,
                    tokenState.iconUrl ?: "",
                    tokenState.totalSupply,
                    tokenState.granularity,
                    tokenState.tokenSupplyType.name
                )
            }, {
                Timber.e(it)
            }).addTo(compositeDisposable)

    }

    /**
     * Delay is added here before posting the finished state in
     * the event that token definition loading is being done and
     * hence avoids view flickering between states.
     * */
    private fun setFinishedStateAfterDelay() {
        viewModelScope.launch {
            delay(500)
            _mainLoadingState.postValue(MainLoadingState.FINISHED)
        }
    }
}

enum class LaunchAuthenticationAction {
    UNLOCK, USE_PIN, LOGOUT
}
