package com.radixdlt.android.apps.wallet.data.mapper

import com.radixdlt.android.apps.wallet.data.model.newtransaction.TransactionEntity2
import com.radixdlt.client.application.translate.tokens.TokenTransfer
import java.math.BigDecimal
import java.math.RoundingMode

object TokenTransferDataMapper2 {

    /**
     * Transform a [TokenTransfer] into an [TransactionEntity2].
     *
     * @param tokenTransfer Object to be transformed.
     * @param myAddress String used to deduce the correct data to display
     * @return [TransactionEntity2]
     */
    fun transform(tokenTransfer: TokenTransfer, myAddress: String): TransactionEntity2 {

        val accountAddress: String = myAddress
        val accountName = "Personal" // Temporary until we allow multiple accounts
        val address: String = getAddress(tokenTransfer, myAddress)
        val amount: BigDecimal = tokenTransfer.amount
        val message: String? = if (tokenTransfer.attachmentAsString.isPresent) tokenTransfer.attachmentAsString.get() else null
        val sent: Boolean = tokenTransfer.from.toString() == myAddress
        val timestamp: Long = tokenTransfer.timestamp
        val rri: String = tokenTransfer.tokenClass.toString()
        val tokenName: String? = null
        val tokenDescription: String? = null
        val tokenIconUrl: String? = null
        val tokenTotalSupply: BigDecimal? = null
        val tokenGranularity: BigDecimal? = null
        val tokenSupplyType: String? = null

        return TransactionEntity2(
            accountAddress,
            accountName,
            address,
            amount,
            message,
            sent,
            timestamp,
            rri,
            tokenName,
            tokenDescription,
            tokenIconUrl,
            tokenTotalSupply,
            tokenGranularity,
            tokenSupplyType
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
