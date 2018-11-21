package com.radixdlt.android.data.model.message

import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class ContactsRepository @Inject constructor(
    private val messagesDao: MessagesDao
) : MessagesLiveData(messagesDao) {

    override fun onActive() {
        getStoredMessages()
        super.onActive()
    }

    private fun getStoredMessages() {
        messagesDao.getLastMessageFromEachConversation()
            .subscribeOn(Schedulers.io())
            .subscribe {
                postValue(it)
            }
            .addTo(compositeDisposable)
    }

    override fun insertExistingListOfMessages(existingList: MutableList<MessageEntity>) {
        messagesDao.insertMessages(existingList)
        getStoredMessages()
    }
}
