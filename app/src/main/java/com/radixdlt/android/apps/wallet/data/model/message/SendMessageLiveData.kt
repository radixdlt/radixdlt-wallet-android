package com.radixdlt.android.apps.wallet.data.model.message

import androidx.lifecycle.LiveData
import com.radixdlt.android.apps.wallet.identity.Identity
import com.radixdlt.client.atommodel.accounts.RadixAddress
import com.radixdlt.client.core.network.actions.SubmitAtomStatusAction
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class SendMessageLiveData @Inject constructor(
    // TODO: Use in future where message will be inserted immediately and have a pending state until
    // confirmed by the network.
    private val messagesDao: MessagesDao
) : LiveData<String>() {

    private val compositeDisposable = CompositeDisposable()

    fun sendMessage(otherAddress: String, message: String) {
        // Send a message!
        if (otherAddress.isBlank()) return
        Timber.d(otherAddress)
        try {
            Identity.api!!
                .sendMessage(RadixAddress.from(otherAddress), message.toByteArray(), true)
                .toObservable()
                .subscribeOn(Schedulers.io())
                .subscribe {
                    if (it is SubmitAtomStatusAction) {
                        postValue(it.statusNotification.atomStatus.name)
                    }
                    Timber.d("Network status is... $it")
                }.addTo(compositeDisposable)
        } catch (e: Exception) {
            postValue("Wrong address format! Try a new conversation again")
        }
    }

    override fun onInactive() {
        super.onInactive()
        compositeDisposable.clear()
    }
}
