package com.radixdlt.android.apps.wallet.ui.activity.main

import androidx.lifecycle.ViewModel
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
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val transactionsDao2: TransactionsDao2
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val myAddress = Identity.api!!.address.toString()

    init {
        retrieveAllTransactions()
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
                insertTransactionIntoDB(transactionEntitiy2)  // insert in DB since token type isn't owned
            })
            .addTo(compositeDisposable)
    }

    private fun checkForExistingTokenDefinitionData(
        lastExistingTransaction: TransactionEntity2,
        transactionEntitiy2: TransactionEntity2
    )
    {
        if (lastExistingTransaction.tokenName != null) {
            val transaction = TransactionEntity2(
                transactionEntitiy2.accountAddress,
                transactionEntitiy2.accountName,
                transactionEntitiy2.address,
                transactionEntitiy2.amount,
                transactionEntitiy2.message,
                transactionEntitiy2.sent,
                transactionEntitiy2.timestamp,
                transactionEntitiy2.rri,
                lastExistingTransaction.tokenName,
                lastExistingTransaction.tokenDescription,
                lastExistingTransaction.tokenIconUrl,
                lastExistingTransaction.tokenTotalSupply,
                lastExistingTransaction.tokenSupplyType
            )

            insertTransactionIntoDB(transaction)   // insert in DB with all info
        } else {
            insertTransactionIntoDB(transactionEntitiy2)  // insert in DB with what we have
        }
    }

    private fun insertTransactionIntoDB(transaction: TransactionEntity2) {
        transactionsDao2.insertTransaction(transaction)
    }

    private fun retrieveListOfOldTransactions(
        allTransactions: Observable<TokenTransfer>
    ): Single<ArrayList<TokenTransfer>> {

        // Retrieve existing transactions from DB first
        getStoredTransactions()

        val oldTransactionsList: Single<ArrayList<TokenTransfer>> = allTransactions
            .scan(ArrayList<TokenTransfer>()) { list, transaction ->
                list.add(transaction)
                return@scan list
            }
            .debounce(3, TimeUnit.SECONDS)
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
                getStoredTransactions()
            }, Throwable::printStackTrace)
            .addTo(compositeDisposable)

        return oldTransactionsList
    }

    private fun getStoredTransactions() {
        transactionsDao2.getAllTransactions()
            .subscribeOn(Schedulers.io())
            .subscribe {
                Timber.tag("TransactionsLooking").d(it.toString())
//                postValue(it) TODO: MAYBE I DUNNO RIGHT NOW
            }
            .addTo(compositeDisposable)
    }

}