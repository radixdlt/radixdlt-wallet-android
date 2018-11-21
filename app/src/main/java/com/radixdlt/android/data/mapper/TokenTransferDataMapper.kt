package com.radixdlt.android.data.mapper

import com.radixdlt.android.data.model.transaction.TransactionEntity
import com.radixdlt.client.application.objects.TokenTransfer
import com.radixdlt.client.assets.Asset
import java.math.BigDecimal
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
        val subUnitAmount: Long = tokenTransfer.subUnitAmount
        val formattedAmount: String = getAmount(tokenTransfer, myAddress)
        val message: String? = tokenTransfer.attachmentAsString
        val sent: Boolean = tokenTransfer.from.toString() == myAddress
        val dateUnix: Long = tokenTransfer.timestamp
        val tokenClassISO: String = tokenTransfer.tokenClass.iso
        val tokenClassSubUnits: Int = tokenTransfer.tokenClass.subUnits

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
        val amount: Double = transaction.subUnitAmount.toDouble() / Asset.TEST.subUnits
        val amountFormatted = BigDecimal(amount.toString()).setScale(
                5, RoundingMode.HALF_UP
        ).toPlainString()

        return if (transaction.from.toString() == myAddress) {
            amountFormatted
        } else {
            "+$amountFormatted"
        }
    }
}
