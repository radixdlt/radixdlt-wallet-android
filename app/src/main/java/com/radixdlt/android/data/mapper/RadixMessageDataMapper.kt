package com.radixdlt.android.data.mapper

import com.radixdlt.android.data.model.message.MessageEntity
import com.radixdlt.client.dapps.messaging.RadixMessage

object RadixMessageDataMapper {

    /**
     * Transform a [RadixMessage] into a [MessageEntity].
     *
     * @param radixMessage Object to be transformed.
     * @return [MessageEntity]
     */
    fun transform(radixMessage: RadixMessage): MessageEntity {

        return MessageEntity(
            radixMessage.from.toString(),
            radixMessage.to.toString(),
            radixMessage.content,
            radixMessage.timestamp
        )
    }
}
