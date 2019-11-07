package com.radixdlt.android.apps.wallet.ui.fragment.transactions

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.radixdlt.android.apps.wallet.R
import com.radixdlt.android.apps.wallet.data.model.newtransaction.TransactionEntity2
import com.radixdlt.android.apps.wallet.ui.adapter.StickyHeaderItemDecoration
import com.radixdlt.android.apps.wallet.util.formatDateDay
import com.radixdlt.android.apps.wallet.util.formatDateMonthYear
import com.radixdlt.android.apps.wallet.util.getStartOfDay
import com.radixdlt.client.core.atoms.particles.RRI
import kotlinx.android.synthetic.main.item_asset_transaction.view.*
import kotlinx.android.synthetic.main.item_asset_transaction.view.transactionConstraintLayout
import kotlinx.android.synthetic.main.item_asset_transaction_date.view.*
import timber.log.Timber
import java.math.RoundingMode

class AssetTransactionsAdapter(
    private val itemClick: (TransactionEntity2, Boolean) -> Unit,
    private val items: MutableList<TransactionEntity2> = mutableListOf()
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    StickyHeaderItemDecoration.StickyHeaderInterface {

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

    /**
     * StickyHeader interface methods
     * */
    override fun getHeaderPositionForItem(itemPosition: Int): Int {
        var itemPos = itemPosition
        var headerPosition = 0
        do {
            if (this.isHeader(itemPos)) {
                headerPosition = itemPos
                break
            }
            itemPos -= 1
        } while (itemPos >= 0)
        return headerPosition
    }

    override fun getHeaderLayout(headerPosition: Int): Int {
        return R.layout.item_asset_transaction_date
    }

    override fun bindHeaderData(header: View, headerPosition: Int) {
        val timestamp = items[headerPosition].timestamp
        header.assetTransactionsDateDayTextView.text = formatDateDay(timestamp)
        header.assetTransactionsDateMonthYearTextView.text = formatDateMonthYear(timestamp)
        // Make this part of the layout invisible since even though no data
        // is being displayed as this is the sticky which overlays the list.
        header.transactionConstraintLayout.visibility = View.INVISIBLE
    }

    override fun isHeader(itemPosition: Int): Boolean {
        return getItemViewType(itemPosition) == TYPE_DATE
    }

    inner class TransactionsViewHolder(
        itemView: View,
        private val itemClick: (TransactionEntity2, Boolean) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        fun bindTransaction(transactionEntity: TransactionEntity2) {

            setAccount(transactionEntity)
            setResources(transactionEntity)
            setTransactionAmount(transactionEntity)
            setTransactionFiatValue(transactionEntity)
            setAddress(transactionEntity)

            // Item click listeners
            setClickListener(transactionEntity)
        }

        fun bindTransactionAndDate(transactionEntity: TransactionEntity2) {
            bindTransaction(transactionEntity)
            val timestamp = transactionEntity.timestamp
            itemView.assetTransactionsDateDayTextView.text = formatDateDay(timestamp)
            itemView.assetTransactionsDateMonthYearTextView.text = formatDateMonthYear(timestamp)
        }

        private fun setAccount(transactionEntity2: TransactionEntity2) {
            itemView.accountTextView.text = transactionEntity2.accountName
        }

        // Set correct resources and color depending if sent or received
        private fun setResources(transactionEntity: TransactionEntity2) {
            if (transactionEntity.sent) {
                itemView.circleImageView.setImageResource(R.drawable.new_send_image_item_wallet)
                itemView.transactionAmount.setTextColor(
                    ContextCompat.getColor(ctx, R.color.radixRed)
                )
            } else {
                itemView.circleImageView.setImageResource(R.drawable.new_receive_image_item_wallet)
                itemView.transactionAmount.setTextColor(
                    ContextCompat.getColor(ctx, R.color.colorAccent)
                )
            }
        }

        private fun setTransactionAmount(transactionEntity: TransactionEntity2) {
            var amount = transactionEntity.amount
                .setScale(2, RoundingMode.HALF_UP)
                .toPlainString()

            val iso = RRI.fromString(transactionEntity.rri).name

            amount = if (transactionEntity.sent) "-$amount $iso" else "+$amount $iso"

            itemView.transactionAmount.text = amount
        }

        private fun setTransactionFiatValue(transactionEntity: TransactionEntity2) {
            var amount = transactionEntity.amount
                .setScale(2, RoundingMode.HALF_UP)
                .toPlainString()

            amount = "$$amount"

            itemView.fiatValueTextView.text = amount
        }

        private fun setAddress(transactionEntity: TransactionEntity2) {
            val address = transactionEntity.address
            val message = if (transactionEntity.sent) "To: $address" else "From: $address"
            itemView.addressTextView.text = message
        }

        private fun setClickListener(transactionEntity: TransactionEntity2) {
            itemView.transactionConstraintLayout.setOnClickListener {
                itemClick(transactionEntity, false)
            }

            itemView.transactionConstraintLayout.setOnLongClickListener {
                itemClick(transactionEntity, true)
                return@setOnLongClickListener true
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
