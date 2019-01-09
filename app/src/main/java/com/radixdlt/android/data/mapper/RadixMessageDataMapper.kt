package com.radixdlt.android.data.mapper

import com.radixdlt.android.data.model.message.MessageEntity
import com.radixdlt.client.application.translate.data.DecryptedMessage

object RadixMessageDataMapper {

    /**
     * Transform a [DecryptedMessage] into a [MessageEntity].
     *
     * @param decryptedMessage Object to be transformed.
     * @return [MessageEntity]
     */
    fun transform(decryptedMessage: DecryptedMessage): MessageEntity {

        return MessageEntity(
            decryptedMessage.from.toString(),
            decryptedMessage.to.toString(),
            String(decryptedMessage.data),
            decryptedMessage.timestamp
        )
    }
}
