package com.radixdlt.android.apps.wallet.data.mapper

import com.radixdlt.android.apps.wallet.data.model.transaction.TransactionEntity
import com.radixdlt.client.application.translate.data.receipt.Receipt
import com.radixdlt.client.application.translate.tokens.TokenTransfer
import com.radixdlt.client.application.translate.tokens.TokenUnitConversions
import java.math.RoundingMode

object TokenTransferDataMapper {

    /**
     * Transform a [TokenTransfer] into an [TransactionEntity].
     *
     * @param tokenTransfer Object to be transformed.
     * @param myAddress String used to deduce the correct data to display
     * @return [TransactionEntity]
     */
    fun transform(tokenTransfer: TokenTransfer, myAddress: String): TransactionEntity {

        val address: String =
            getAddress(tokenTransfer, myAddress)
        val subUnitAmount: Long = tokenTransfer.amount.toLong()
        val formattedAmount: String =
            getAmount(tokenTransfer, myAddress)

        var message: String? = null
        var receiptBytes: ByteArray? = null

        if (tokenTransfer.hasReceipt()) {
            val receipt: Receipt = tokenTransfer.receiptFromAttachment.get()
            receiptBytes = receipt.serializedJsonBytes
        } else if (tokenTransfer.attachmentAsString.isPresent) {
            message = tokenTransfer.attachmentAsString.get()
        }

        val sent: Boolean = tokenTransfer.from.toString() == myAddress
        val dateUnix: Long = tokenTransfer.timestamp
        val tokenClassISO: String = tokenTransfer.tokenClass.toString()
        val tokenClassSubUnits = TokenUnitConversions.getSubunits()

        return TransactionEntity(
            address,
            subUnitAmount,
            formattedAmount,
            message,
            receiptBytes,
            sent,
            dateUnix,
            tokenClassISO,
            tokenClassSubUnits
        )
    }

    /**
     * Compare address against locally stored users address and use the one that is not.
     *
     * @param transaction
     * @param myAddress
     * @return The getAddress to display
     * */
    private fun getAddress(transaction: TokenTransfer, myAddress: String): String {
        return if (transaction.from.toString() == myAddress) {
            transaction.to.toString()
        } else {
            transaction.from.toString()
        }
    }

    /**
     * Extract transactionList formattedAmount, format and check if it is being sent or received.
     * If received, prepend '+' plus sign.
     *
     * @param transaction
     * @param myAddress
     * @return Formatted formattedAmount to display
     * */
    private fun getAmount(transaction: TokenTransfer, myAddress: String): String {
        val amountFormatted = transaction.amount.setScale(
                5, RoundingMode.HALF_UP
        ).toPlainString()

        return if (transaction.from.toString() == myAddress) {
            amountFormatted
        } else {
            "+$amountFormatted"
        }
    }
}
