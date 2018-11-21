package com.radixdlt.android.data.model.message

import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class ConversationRepository @Inject constructor(
    private val messagesDao: MessagesDao
) : MessagesLiveData(messagesDao) {

    lateinit var myAddress: String
    lateinit var otherAddress: String

    private var lastMessage: MessageEntity? = null

    override fun onActive() {
        getConversation()
        super.onActive()
    }

    private fun getConversation() {
        messagesDao.getAllMessagesByAddresses(myAddress, otherAddress)
            .subscribeOn(Schedulers.io())
            .subscribe {
                getAndListenToLatestMessage()
                if (it.isEmpty()) return@subscribe
                lastMessage = it.last()
                postValue(it)
                Timber.tag("CoversationDao").d(it.toString())
            }.addTo(compositeDisposable)
    }

    private fun getAndListenToLatestMessage() {
        messagesDao.getLatestMessageByAddresses(myAddress, otherAddress)
            .subscribeOn(Schedulers.io())
            .subscribe {
                if (it.isEmpty() || lastMessage == it.first()) return@subscribe
                postValue(it)
            }.addTo(compositeDisposable)
    }

    override fun insertExistingListOfMessages(existingList: MutableList<MessageEntity>) {
        messagesDao.insertMessages(existingList)
    }
}
