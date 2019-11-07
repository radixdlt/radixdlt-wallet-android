package com.radixdlt.android.apps.wallet.ui.activity.main

import androidx.annotation.IdRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radixdlt.android.apps.wallet.data.mapper.TokenTransferDataMapper2
import com.radixdlt.android.apps.wallet.data.model.newtransaction.TransactionEntity2
import com.radixdlt.android.apps.wallet.data.model.newtransaction.TransactionsDao2
import com.radixdlt.android.apps.wallet.identity.Identity
import com.radixdlt.client.application.translate.tokens.TokenTransfer
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
    private val transactionsDao2: TransactionsDao2
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

    init {
        _showBackUpWalletNotification.value = true
        _mainLoadingState.value =
            MainLoadingState.LOADING
        checkTransactionsTable()
        retrieveAllTransactions()
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
                TokenTransferDataMapper2.transform(it, myAddress)
            }
            .subscribeOn(Schedulers.io())
            .subscribe(::attemptToRetrieveLastTransactionForAsset, Throwable::printStackTrace)
            .addTo(compositeDisposable)
    }

    private fun attemptToRetrieveLastTransactionForAsset(transactionEntitiy2: TransactionEntity2) {
        transactionsDao2.getLastTransactionByTokenType(transactionEntitiy2.rri)
            .subscribe({ lastTransactionForAsset ->
                checkForExistingTokenDefinitionData(lastTransactionForAsset, transactionEntitiy2)
            }, {
                insertTransactionIntoDB(transactionEntitiy2) // insert in DB since token type isn't owned
            })
            .addTo(compositeDisposable)
    }

    private fun checkForExistingTokenDefinitionData(
        lastExistingTransaction: TransactionEntity2,
        transactionEntity2: TransactionEntity2
    ) {
        if (lastExistingTransaction.tokenName != null) {
            val transaction = TransactionEntity2(
                transactionEntity2.accountAddress,
                transactionEntity2.accountName,
                transactionEntity2.address,
                transactionEntity2.amount,
                transactionEntity2.message,
                transactionEntity2.sent,
                transactionEntity2.timestamp,
                transactionEntity2.rri,
                lastExistingTransaction.tokenName,
                lastExistingTransaction.tokenDescription,
                lastExistingTransaction.tokenIconUrl,
                lastExistingTransaction.tokenTotalSupply,
                lastExistingTransaction.tokenGranularity,
                lastExistingTransaction.tokenSupplyType
            )
            insertTransactionIntoDB(transaction) // insert in DB with all info
        } else {
            insertTransactionIntoDB(transactionEntity2) // insert in DB with what we have
        }
    }

    private fun insertTransactionIntoDB(transaction: TransactionEntity2) {
        transactionsDao2.insertTransaction(transaction)
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
                TokenTransferDataMapper2.transform(it, myAddress)
            }
            .toList()
            .subscribeOn(Schedulers.io())
            .subscribe({
                transactionsDao2.insertTransactions(it) // insert in DB
                setFinishedStateAfterDelay()
            }, Throwable::printStackTrace)
            .addTo(compositeDisposable)

        return oldTransactionsList
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

    @Suppress("unused")
    private fun getStoredTransactions() {
        transactionsDao2.getAllTransactions()
            .subscribeOn(Schedulers.io())
            .subscribe {
                Timber.tag("TransactionsLooking").d(it.toString())
                _mainLoadingState.postValue(MainLoadingState.FINISHED)
            }
            .addTo(compositeDisposable)
    }

    private fun checkTransactionsTable() {
        viewModelScope.launch {
            // Check to see if there are existing transactions and make
            // loading state to finish as soon as possible.
            if (transactionsDao2.transactionsCount() != 0) {
                _mainLoadingState.postValue(MainLoadingState.EXISTING)
            }
        }
    }
}
