package com.radixdlt.android.data.mapper

import com.radixdlt.android.data.model.transaction.TransactionEntity
import com.radixdlt.client.application.translate.tokens.TokenClassReference
import com.radixdlt.client.application.translate.tokens.TokenTransfer
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

        val address: String = getAddress(tokenTransfer, myAddress)
        val subUnitAmount: Long = tokenTransfer.amount.toLong()
        val formattedAmount: String = getAmount(tokenTransfer, myAddress)
        val message: String? = if (tokenTransfer.attachmentAsString.isPresent) tokenTransfer.attachmentAsString.get() else null
        val sent: Boolean = tokenTransfer.from.toString() == myAddress
        val dateUnix: Long = tokenTransfer.timestamp
//        val tokenClassISO: String = tokenTransfer.tokenClass.symbol
        val tokenClassISO: String = tokenTransfer.tokenClass.toString()
        // TODO: Currently it is fixed and the plan is for all tokens to have the same subunits
        // TODO: Recent update has changed this to 10^18
        val tokenClassSubUnits = TokenClassReference.getSubunits()

        return TransactionEntity(
                address,
                subUnitAmount,
                formattedAmount,
                message,
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
