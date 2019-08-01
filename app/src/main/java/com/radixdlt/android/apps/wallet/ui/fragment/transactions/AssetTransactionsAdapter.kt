package com.radixdlt.android.apps.wallet.ui.fragment.transactions

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.MainThread
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.radixdlt.android.R
import com.radixdlt.android.apps.wallet.data.model.newtransaction.TransactionEntity2
import com.radixdlt.android.apps.wallet.util.getOrdinal
import com.radixdlt.android.apps.wallet.util.getStartOfDay
import de.hdodenhof.circleimageview.CircleImageView
import org.jetbrains.anko.find
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import timber.log.Timber
import java.math.RoundingMode
import java.util.Locale

class AssetTransactionsAdapter(
    private val items: MutableList<TransactionEntity2> = mutableListOf(),
    private val itemClick: (String, Boolean) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var ctx: Context

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        ctx = viewGroup.context
        val inflater = LayoutInflater.from(viewGroup.context)
        val view: View

        view = when (viewType) {
            TYPE_TRANSACTION -> inflater.inflate(
                R.layout.item_asset_transaction,
                viewGroup, false
            )
            TYPE_DATE -> inflater.inflate(
                R.layout.item_asset_transaction_date,
                viewGroup, false
            )
            else -> throw IllegalStateException("Unexpected viewType (= $viewType)")
        }

        return TransactionsViewHolder(view, itemClick)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            TYPE_DATE -> (holder as TransactionsViewHolder).bindTransactionAndDate(items[position])
            TYPE_TRANSACTION -> (holder as TransactionsViewHolder).bindTransaction(items[position])
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        val transaction = items[position]
        val startOfDay = getStartOfDay(transaction.timestamp)
        var startOfPreviousDay: Long = 0
        if (position != 0) {
            startOfPreviousDay = getDatePreviousPosition(position)
        }

        return if (position == 0 || startOfDay != startOfPreviousDay) {
            TYPE_DATE
        } else {
            TYPE_TRANSACTION
        }
    }

    private fun getDatePreviousPosition(position: Int): Long {
        return getStartOfDay((items[position - 1].timestamp))
    }

    inner class TransactionsViewHolder(
        itemView: View,
        private val itemClick: (String, Boolean) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val transactionView: View = itemView.find(R.id.transactionConstraintLayout)
        private val circleImageView: CircleImageView = itemView.find(R.id.circleImageView)
        private val accountTextView: TextView = itemView.find(R.id.accountTextView)
        private val addressTextView: TextView = itemView.find(R.id.addressTextView)
        private val transactionAmountTextView: TextView = itemView.find(R.id.transactionAmount)
        private val fiatValueTextView: TextView = itemView.find(R.id.fiatValueTextView)

        fun bindTransaction(transactionEntity: TransactionEntity2) {

            setAccount(accountTextView, transactionEntity)
            setResources(circleImageView, transactionAmountTextView, transactionEntity)
            setTransactionAmount(transactionEntity, transactionAmountTextView)
            setTransactionFiatValue(transactionEntity, fiatValueTextView)
            setAddress(transactionEntity, addressTextView)

            // Item click listeners
            setClickListener(transactionEntity, transactionView)
        }

        fun bindTransactionAndDate(transactionEntity: TransactionEntity2) {
            bindTransaction(transactionEntity)
            val date: TextView = itemView.find(R.id.assetTransactionsDateTextView)
            date.text = formatDateYear(transactionEntity.timestamp)
        }

        private fun formatDateYear(dateUnix: Long): String {
            val localDateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(dateUnix), ZoneId.systemDefault()
            )
            var displayValue = localDateTime.format(
                DateTimeFormatter.ofPattern("EEEE, ? MMMM, yyyy", Locale.getDefault())
            )
            val day = localDateTime.format(
                DateTimeFormatter.ofPattern("dd", Locale.getDefault())
            )
            val dayOrdinal = day.toInt().getOrdinal()
            displayValue = displayValue.replace("?", dayOrdinal!!)

            return displayValue
        }

        private fun setAccount(textView: TextView, transactionEntity2: TransactionEntity2) {
            textView.text = transactionEntity2.accountName
        }

        // Set correct resources and color depending if sent or received
        private fun setResources(
            circle: CircleImageView,
            transactionView: TextView,
            transactionEntity: TransactionEntity2
        ) {
            if (transactionEntity.sent) {
                circle.setImageResource(R.drawable.new_send_image_item_wallet)
                transactionView.setTextColor(ContextCompat.getColor(ctx, R.color.radixRed))
            } else {
                circle.setImageResource(R.drawable.new_receive_image_item_wallet)
                transactionView.setTextColor(ContextCompat.getColor(ctx, R.color.colorAccent))
            }
        }

        // Detect if whole number or decimal to change character size
        private fun setTransactionAmount(
            transactionEntity: TransactionEntity2,
            textView: TextView
        ) {
            var amount = transactionEntity.amount
                .setScale(2, RoundingMode.HALF_UP)
                .toPlainString()

            amount = if (transactionEntity.sent) "-$amount" else "+$amount"

            textView.text = amount
        }

        private fun setTransactionFiatValue(
            transactionEntity: TransactionEntity2,
            textView: TextView
        ) {
            var amount = transactionEntity.amount
                .setScale(2, RoundingMode.HALF_UP)
                .toPlainString()

            amount = "$$amount"

            textView.text = amount
        }

        private fun setAddress(transactionEntity: TransactionEntity2, textView: TextView) {
            val address = transactionEntity.address
            val message = if (transactionEntity.sent) "To: $address" else "From: $address"
            textView.text = message
        }

        private fun setClickListener(transactionEntity: TransactionEntity2, view: View) {
            view.setOnClickListener {
                itemClick(transactionEntity.address, false)
            }
        }
    }

    @MainThread
    fun replace(assets: List<TransactionEntity2>) {
        val difference = DiffUtil.calculateDiff(
            AssetTransactionsDiffUtil(items, assets),
            true
        )
        difference.dispatchUpdatesTo(this)
        items.clear()
        items.addAll(assets)
    }

    private class AssetTransactionsDiffUtil(
        private val oldList: List<TransactionEntity2>,
        private val newList: List<TransactionEntity2>
    ) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            Timber.tag("diffUtil")
                .d("areItemsTheSame ${oldList[oldItemPosition] === newList[newItemPosition]}")
            return oldList[oldItemPosition] === newList[newItemPosition]
        }

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return true
        }
    }

    companion object {
        const val TYPE_TRANSACTION = 0
        const val TYPE_DATE = 1
    }
}