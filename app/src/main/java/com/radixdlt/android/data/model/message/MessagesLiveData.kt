package com.radixdlt.android.data.model.message

import androidx.lifecycle.LiveData
import com.radixdlt.android.data.mapper.RadixMessageDataMapper
import com.radixdlt.android.identity.Identity
import com.radixdlt.client.application.translate.data.DecryptedMessage
import com.radixdlt.client.dapps.messaging.RadixMessaging
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

abstract class MessagesLiveData(
    private val messagesDao: MessagesDao
) : LiveData<MutableList<MessageEntity>>() {

    var compositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun onActive() {
        super.onActive()
        Timber.d("MessagesLiveData onActive")
        retrieveMessages()
    }

    private fun retrieveMessages() {
        val radixWalletMessagesObservable = RadixMessaging(Identity.api!!)
            .allMessages

        val allMessages = radixWalletMessagesObservable
            .publish()
            .refCount(2)

        val oldMessagesList = retrieveListOfOldMessages(allMessages)

        listenToNewMessages(oldMessagesList, allMessages)
    }

    private fun listenToNewMessages(
        oldMessageList: Single<ArrayList<DecryptedMessage>>,
        allMessages: Observable<DecryptedMessage>
    ) {
        Observables.combineLatest(
            oldMessageList.toObservable(),
            allMessages
        ) { listOfOld, radixMessage ->
            if (listOfOld.contains(radixMessage)) {
                Maybe.empty()
            } else {
                Maybe.just(radixMessage)
            }
        }.flatMapMaybe { it }
            .map { mutableListOf(RadixMessageDataMapper.transform(it)) }
            .subscribeOn(Schedulers.io())
            .subscribe({
                messagesDao.insertMessage(it.first()) // insert in DB
            }, Throwable::printStackTrace)
            .addTo(compositeDisposable)
    }

    private fun retrieveListOfOldMessages(
        allMessages: Observable<DecryptedMessage>
    ): Single<ArrayList<DecryptedMessage>> {

        val oldMessageList: Single<ArrayList<DecryptedMessage>> = allMessages
            .scan(ArrayList<DecryptedMessage>()) { list, radixMessage ->
                list.add(radixMessage)
                return@scan list
            }
            .debounce(3, TimeUnit.SECONDS)
            .firstOrError()
            .cache()

        oldMessageList
            .toObservable()
            .flatMapIterable { list -> list }
            .map(RadixMessageDataMapper::transform)
            .toList()
            .subscribeOn(Schedulers.io())
            .subscribe({
                insertExistingListOfMessages(it)
            }, Throwable::printStackTrace)
            .addTo(compositeDisposable)

        return oldMessageList
    }

    abstract fun insertExistingListOfMessages(existingList: MutableList<MessageEntity>)

    override fun onInactive() {
        super.onInactive()
        Timber.d("MessagesLiveData onInactive")
        compositeDisposable.clear()
    }
}
