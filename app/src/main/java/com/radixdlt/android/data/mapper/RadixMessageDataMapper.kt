package com.radixdlt.android.data.mapper

import com.radixdlt.android.data.model.message.MessageEntity
import com.radixdlt.client.application.translate.data.DecryptedMessage

object RadixMessageDataMapper {

    /**
     * Transform a [RadixMessage] into a [MessageEntity].
     *
     * @param radixMessage Object to be transformed.
     * @return [MessageEntity]
     */
    fun transform(radixMessage: DecryptedMessage): MessageEntity {

        return MessageEntity(
            radixMessage.from.toString(),
            radixMessage.to.toString(),
            String(radixMessage.data),
            radixMessage.timestamp
        )
    }
}
