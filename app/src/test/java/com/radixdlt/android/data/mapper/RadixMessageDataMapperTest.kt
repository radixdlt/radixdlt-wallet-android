package com.radixdlt.android.data.mapper

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import com.radixdlt.client.application.translate.data.DecryptedMessage
import com.radixdlt.client.atommodel.accounts.RadixAddress
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class RadixMessageDataMapperTest {

    @Test
    @DisplayName("Given DecryptedMessage When transformed Then MessageEntity is returned")
    fun transformDecryptedMessage() {
        val data = "Hello".toByteArray()
        val state = DecryptedMessage.EncryptionState.NOT_ENCRYPTED
        val timestamp = 1546529559L

        val decryptedMessage = mock<DecryptedMessage>()
        val radixAddressFrom = mock<RadixAddress>()
        val radixAddressTo = mock<RadixAddress>()

        whenever(decryptedMessage.data).thenReturn("Hello".toByteArray())
        whenever(decryptedMessage.from).thenReturn(radixAddressFrom)
        whenever(decryptedMessage.to).thenReturn(radixAddressTo)
        whenever(decryptedMessage.encryptionState).thenReturn(state)
        whenever(decryptedMessage.timestamp).thenReturn(timestamp)

        val messageEntity = RadixMessageDataMapper.transform(decryptedMessage)
        assertEquals(String(data), messageEntity.content)
        assertEquals(timestamp, messageEntity.timestamp)
        assertEquals(radixAddressFrom.toString(), messageEntity.fromAddress)
        assertEquals(radixAddressTo.toString(), messageEntity.toAddress)
    }
}
