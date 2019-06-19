package com.radixdlt.android.apps.wallet.ui.activity

import androidx.lifecycle.ViewModel
import com.radixdlt.android.apps.wallet.data.model.message.ConversationRepository
import com.radixdlt.android.apps.wallet.data.model.message.SendMessageLiveData
import javax.inject.Inject
import javax.inject.Named

class ConversationViewModel @Inject constructor(
    @Named("conversation") val conversationRepository: ConversationRepository,
    @Named("sendMessage") val sendMessageLiveData: SendMessageLiveData
) : ViewModel() {

    fun conversationTwoParticipants(myAddress: String, otherAddress: String) {
        conversationRepository.myAddress = myAddress
        conversationRepository.otherAddress = otherAddress
    }

    fun sendMessage(otherAddress: String, message: String) {
        sendMessageLiveData.sendMessage(otherAddress, message)
    }
}
