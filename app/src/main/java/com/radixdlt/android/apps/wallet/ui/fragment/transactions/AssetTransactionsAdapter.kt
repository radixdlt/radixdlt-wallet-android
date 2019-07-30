package com.radixdlt.android.apps.wallet.ui.fragment.transactions

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.radixdlt.android.R
import com.radixdlt.android.apps.wallet.data.model.newtransaction.TransactionEntity2
import com.radixdlt.android.apps.wallet.util.QueryPreferences
import com.radixdlt.android.apps.wallet.util.formatCharactersForAmount
import com.radixdlt.android.apps.wallet.util.setAddressWithColors
import kotlinx.android.synthetic.main.item_wallet.view.*
import timber.log.Timber
import java.math.RoundingMode

class AssetTransactionsAdapter(
    private val items: MutableList<TransactionEntity2> = mutableListOf(),
    private val itemClick: (String, Boolean) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var ctx: Context
    private lateinit var myAddress: String

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        ctx = viewGroup.context
        myAddress = QueryPreferences.getPrefAddress(ctx)

        return WalletViewHolder(
            LayoutInflater.from(viewGroup.context).inflate(
                R.layout.item_wallet,
                viewGroup, false
            ), itemClick
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as WalletViewHolder).bindList(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class WalletViewHolder(
        itemView: View,
        private val itemClick: (String, Boolean) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        fun bindList(transactionEntity: TransactionEntity2) {

            setAddress(transactionEntity)
            setResources(transactionEntity)
            setTransactionAmount(transactionEntity)
            setTokenType(transactionEntity)
            setMessage(transactionEntity)

            // Item click listeners
            setClickListener(transactionEntity)
            setLongClickListener(transactionEntity)
        }

        private fun setAddress(transactionEntity: TransactionEntity2) {
            itemView.addressTextView.text = setAddressWithColors(
                ctx, transactionEntity.address, R.color.materialDarkGrey
            )
        }

        // Set correct resources and colors depending if sent or received
        private fun setResources(transactionEntity: TransactionEntity2) {
            if (transactionEntity.sent) {
                itemView.circleImageView.setImageResource(R.drawable.send_image_item_wallet)
                itemView.transactionAmount.setTextColor(
                    ContextCompat.getColor(ctx, R.color.materialGrey700)
                )
            } else {
                itemView.circleImageView.setImageResource(R.drawable.receive_image_item_wallet)
                itemView.transactionAmount.setTextColor(ContextCompat.getColor(ctx, R.color.green))
            }
        }

        // Detect if whole number or decimal to change character size
        private fun setTransactionAmount(transactionEntity: TransactionEntity2) {
            var amount = transactionEntity.amount
                .setScale(2, RoundingMode.HALF_UP)
                .toPlainString()

            amount = if (transactionEntity.sent) "-$amount" else "+$amount"

            if (!amount.contains(".")) {
                itemView.transactionAmount.text = amount
            } else {
                itemView.transactionAmount.text = formatCharactersForAmount(
                    amount.split(".")[0],
                    amount.split(".")[1]
                )
            }
        }

        private fun setTokenType(transactionEntity: TransactionEntity2) {
//            if (transactionEntity.tokenClassISO != GENESIS_XRD) {
                itemView.testTokensTextView.text = transactionEntity.rri.split("/")[2]
                itemView.testTokensTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
//            } else {
//                itemView.testTokensTextView.text = ctx.getString(R.string.wallet_fragment_item_xml_test_tokens)
//                itemView.testTokensTextView.setCompoundDrawablesWithIntrinsicBounds(
//                    null,
//                    null,
//                    ContextCompat.getDrawable(ctx, R.drawable.ic_warning_line_small),
//                    null
//                )
//            }
        }

        // Detect if message attached to transactionList, set and make VISIBLE or set as GONE
        private fun setMessage(transactionEntity: TransactionEntity2) {
            if (transactionEntity.message != null) {
                itemView.messageTextView.visibility = View.VISIBLE
                itemView.messageTextView.text = transactionEntity.message
            } else {
                itemView.messageTextView.visibility = View.GONE
            }
        }

        private fun setClickListener(transactionEntity: TransactionEntity2) {
            itemView.setOnClickListener {
                itemClick(transactionEntity.address, false)
            }
        }

        private fun setLongClickListener(transactionEntity: TransactionEntity2) {
            itemView.setOnLongClickListener {
//                itemClick(transactionEntity, true)
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
            Timber.tag("diffUtil").d("areItemsTheSame ${oldList[oldItemPosition] === newList[newItemPosition]}")
            return oldList[oldItemPosition] === newList[newItemPosition]
        }

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return true
        }
    }
}
