package com.radixdlt.android.apps.wallet.data.mapper

import com.radixdlt.android.apps.wallet.data.model.TransactionsEntityOM
import com.radixdlt.android.apps.wallet.data.model.newtransaction.TransactionEntity2
import com.radixdlt.client.application.translate.tokens.TokenTransfer
import java.math.BigDecimal

object TokenTransferDataMapperOM {

    /**
     * Transform a [TokenTransfer] into an [TransactionEntity2].
     *
     * @param tokenTransfer Object to be transformed.
     * @param myAddress String used to deduce the correct data to display
     * @return [TransactionEntity2]
     */
    fun transform(tokenTransfer: TokenTransfer, myAddress: String): TransactionsEntityOM {

        val accountAddress: String = myAddress
        val accountName = "Personal" // Temporary until we allow multiple accounts
        val address: String = getAddress(tokenTransfer, myAddress)
        val amount: BigDecimal = tokenTransfer.amount
        val message: String? = if (tokenTransfer.attachmentAsString.isPresent) tokenTransfer.attachmentAsString.get() else null
        val sent: Boolean = tokenTransfer.from.toString() == myAddress
        val timestamp: Long = tokenTransfer.timestamp
        val rri: String = tokenTransfer.tokenClass.toString()

        return TransactionsEntityOM(
            accountAddress,
            accountName,
            address,
            amount,
            message,
            sent,
            timestamp,
            rri
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
}
