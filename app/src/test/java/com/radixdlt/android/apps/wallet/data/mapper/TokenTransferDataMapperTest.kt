package com.radixdlt.android.apps.wallet.data.mapper

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import com.radixdlt.client.application.translate.tokens.TokenTransfer
import com.radixdlt.client.atommodel.accounts.RadixAddress
import com.radixdlt.client.atommodel.tokens.TokenClassReference
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.Optional

class TokenTransferDataMapperTest {

    private val timestamp = 1546529559L
    private val amount = BigDecimal(100)
    private val address = "9he94tVfQGAVr4xoUpG3uJfB2exURExzFV6E7dq4bxUWRbM5Edd"
    private val tokenTransfer = mock<TokenTransfer>()
    private val radixAddressFrom = RadixAddress.from(address)
    private val radixAddressTo = mock<RadixAddress>()
    private val tokenClassReference = TokenClassReference.of(radixAddressFrom, "XRD")

    @BeforeEach
    fun setup() {
        whenever(tokenTransfer.from).thenReturn(radixAddressFrom)
        whenever(tokenTransfer.to).thenReturn(radixAddressTo)
        whenever(tokenTransfer.timestamp).thenReturn(timestamp)
        whenever(tokenTransfer.tokenClass).thenReturn(tokenClassReference)
        whenever(tokenTransfer.amount).thenReturn(amount)
    }

    @Nested
    @DisplayName("Given TokenTransfer with attachment")
    inner class TokenTransferWithAttachment {
        private val attachmentAsString = Optional.of("Hello")

        @BeforeEach
        fun setup() {
            whenever(tokenTransfer.from).thenReturn(radixAddressFrom)
            whenever(tokenTransfer.to).thenReturn(radixAddressTo)
            whenever(tokenTransfer.attachmentAsString).thenReturn(attachmentAsString)
            whenever(tokenTransfer.timestamp).thenReturn(timestamp)
            whenever(tokenTransfer.tokenClass).thenReturn(tokenClassReference)
            whenever(tokenTransfer.amount).thenReturn(amount)
        }

        @Test
        @DisplayName("When sent and transformed Then TransactionEntity is returned")
        fun transformSentTokenTransfer() {
            val myAddress = "9he94tVfQGAVr4xoUpG3uJfB2exURExzFV6E7dq4bxUWRbM5Edd"

            val transactionEntity =
                TokenTransferDataMapper.transform(tokenTransfer, myAddress)

            assertNotEquals(myAddress, transactionEntity.address)
            assertEquals(amount.toLong(), transactionEntity.subUnitAmount)
            assertEquals(timestamp, transactionEntity.dateUnix)
            assertEquals("100.00000", transactionEntity.formattedAmount)
            assertEquals(true, transactionEntity.sent)
            assertEquals(attachmentAsString.get(), transactionEntity.message)
            assertEquals(
                "9he94tVfQGAVr4xoUpG3uJfB2exURExzFV6E7dq4bxUWRbM5Edd/@XRD",
                transactionEntity.tokenClassISO
            )
            assertEquals(TokenClassReference.getSubunits(), transactionEntity.tokenClassSubUnits)
        }

        @Test
        @DisplayName("When received and transformed Then TransactionEntity is returned")
        fun transformReceivedTokenTransfer() {
            val myAddress = "JH1P8f3znbyrDj8F4RWpix7hRkgxqHjdW2fNnKpR3v6ufXnknor"

            val transactionEntity =
                TokenTransferDataMapper.transform(tokenTransfer, myAddress)

            assertNotEquals(myAddress, transactionEntity.address)
            assertEquals(amount.toLong(), transactionEntity.subUnitAmount)
            assertEquals(timestamp, transactionEntity.dateUnix)
            assertEquals("+100.00000", transactionEntity.formattedAmount)
            assertEquals(false, transactionEntity.sent)
            assertEquals(attachmentAsString.get(), transactionEntity.message)
            assertEquals(
                "9he94tVfQGAVr4xoUpG3uJfB2exURExzFV6E7dq4bxUWRbM5Edd/@XRD",
                transactionEntity.tokenClassISO
            )
            assertEquals(TokenClassReference.getSubunits(), transactionEntity.tokenClassSubUnits)
        }
    }

    @Nested
    @DisplayName("Given TokenTransfer without attachment")
    inner class TokenTransferWithoutAttachment {
        private val attachmentAsString = Optional.empty<String>()

        @BeforeEach
        fun setup() {
            whenever(tokenTransfer.attachmentAsString).thenReturn(attachmentAsString)
        }

        @Test
        @DisplayName("When sent and transformed Then TransactionEntity is returned")
        fun transformSentTokenTransfer() {
            val myAddress = "9he94tVfQGAVr4xoUpG3uJfB2exURExzFV6E7dq4bxUWRbM5Edd"

            val transactionEntity =
                TokenTransferDataMapper.transform(tokenTransfer, myAddress)

            assertNotEquals(myAddress, transactionEntity.address)
            assertEquals(amount.toLong(), transactionEntity.subUnitAmount)
            assertEquals(timestamp, transactionEntity.dateUnix)
            assertEquals("100.00000", transactionEntity.formattedAmount)
            assertEquals(true, transactionEntity.sent)
            assertEquals(null, transactionEntity.message)
            assertEquals(
                "9he94tVfQGAVr4xoUpG3uJfB2exURExzFV6E7dq4bxUWRbM5Edd/@XRD",
                transactionEntity.tokenClassISO
            )
            assertEquals(TokenClassReference.getSubunits(), transactionEntity.tokenClassSubUnits)
        }

        @Test
        @DisplayName("When received and transformed Then TransactionEntity is returned")
        fun transformReceivedTokenTransfer() {
            val myAddress = "JH1P8f3znbyrDj8F4RWpix7hRkgxqHjdW2fNnKpR3v6ufXnknor"

            val transactionEntity =
                TokenTransferDataMapper.transform(tokenTransfer, myAddress)

            assertNotEquals(myAddress, transactionEntity.address)
            assertEquals(amount.toLong(), transactionEntity.subUnitAmount)
            assertEquals(timestamp, transactionEntity.dateUnix)
            assertEquals("+100.00000", transactionEntity.formattedAmount)
            assertEquals(false, transactionEntity.sent)
            assertEquals(null, transactionEntity.message)
            assertEquals(
                "9he94tVfQGAVr4xoUpG3uJfB2exURExzFV6E7dq4bxUWRbM5Edd/@XRD",
                transactionEntity.tokenClassISO
            )
            assertEquals(TokenClassReference.getSubunits(), transactionEntity.tokenClassSubUnits)
        }
    }
}
