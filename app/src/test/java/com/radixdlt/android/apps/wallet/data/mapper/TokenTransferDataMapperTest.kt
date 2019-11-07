package com.radixdlt.android.apps.wallet.data.mapper

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import com.radixdlt.client.application.translate.tokens.TokenTransfer
import com.radixdlt.client.application.translate.tokens.TokenUnitConversions
import com.radixdlt.client.atommodel.accounts.RadixAddress
import com.radixdlt.client.core.atoms.particles.RRI
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
    private val tokenClassReference = RRI.of(radixAddressFrom, "XRD")

    @BeforeEach
    fun setup() {
        whenever(tokenTransfer.from).doReturn(radixAddressFrom)
        whenever(tokenTransfer.to).doReturn(radixAddressTo)
        whenever(tokenTransfer.timestamp).doReturn(timestamp)
        whenever(tokenTransfer.tokenClass).doReturn(tokenClassReference)
        whenever(tokenTransfer.amount).doReturn(amount)
    }

    @Nested
    @DisplayName("Given TokenTransfer with attachment")
    inner class TokenTransferWithAttachment {
        private val attachmentAsString = Optional.of("Hello")

        @BeforeEach
        fun setup() {
            whenever(tokenTransfer.from).doReturn(radixAddressFrom)
            whenever(tokenTransfer.to).doReturn(radixAddressTo)
            whenever(tokenTransfer.attachmentAsString).doReturn(attachmentAsString)
            whenever(tokenTransfer.timestamp).doReturn(timestamp)
            whenever(tokenTransfer.tokenClass).doReturn(tokenClassReference)
            whenever(tokenTransfer.amount).doReturn(amount)
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
                "/9he94tVfQGAVr4xoUpG3uJfB2exURExzFV6E7dq4bxUWRbM5Edd/XRD",
                transactionEntity.rri
            )
            assertEquals(TokenUnitConversions.getSubunits(), transactionEntity.tokenClassSubUnits)
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
                "/9he94tVfQGAVr4xoUpG3uJfB2exURExzFV6E7dq4bxUWRbM5Edd/XRD",
                transactionEntity.rri
            )
            assertEquals(TokenUnitConversions.getSubunits(), transactionEntity.tokenClassSubUnits)
        }
    }

    @Nested
    @DisplayName("Given TokenTransfer without attachment")
    inner class TokenTransferWithoutAttachment {
        private val attachmentAsString = Optional.empty<String>()

        @BeforeEach
        fun setup() {
            whenever(tokenTransfer.attachmentAsString).doReturn(attachmentAsString)
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
                "/9he94tVfQGAVr4xoUpG3uJfB2exURExzFV6E7dq4bxUWRbM5Edd/XRD",
                transactionEntity.rri
            )
            assertEquals(TokenUnitConversions.getSubunits(), transactionEntity.tokenClassSubUnits)
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
                "/9he94tVfQGAVr4xoUpG3uJfB2exURExzFV6E7dq4bxUWRbM5Edd/XRD",
                transactionEntity.rri
            )
            assertEquals(TokenUnitConversions.getSubunits(), transactionEntity.tokenClassSubUnits)
        }
    }
}
