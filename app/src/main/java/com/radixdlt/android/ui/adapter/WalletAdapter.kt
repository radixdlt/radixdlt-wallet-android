package com.radixdlt.android.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.radixdlt.android.R
import com.radixdlt.android.data.model.transaction.TransactionEntity
import com.radixdlt.android.util.GENESIS_XRD
import com.radixdlt.android.util.QueryPreferences
import com.radixdlt.android.util.formatCharactersForAmount
import com.radixdlt.android.util.setAddressWithColors
import kotlinx.android.synthetic.main.item_wallet.view.*

class WalletAdapter(
    private val list: MutableList<TransactionEntity>,
    private val itemClick: (TransactionEntity, Boolean) -> Unit
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
        (holder as WalletViewHolder).bindList(list[position])
    }

    override fun getItemCount(): Int = list.size

    inner class WalletViewHolder(
        itemView: View,
        private val itemClick: (TransactionEntity, Boolean) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        fun bindList(transactionEntity: TransactionEntity) {

            setAddress(transactionEntity)
            setResources(transactionEntity)
            setTransactionAmount(transactionEntity)
            setTokenType(transactionEntity)
            setMessage(transactionEntity)

            // Item click listeners
            setClickListener(transactionEntity)
            setLongClickListener(transactionEntity)
        }

        private fun setAddress(transactionEntity: TransactionEntity) {
            itemView.addressTextView.text = setAddressWithColors(
                ctx, transactionEntity.address, R.color.materialDarkGrey
            )
        }

        // Set correct resources and colors depending if sent or received
        private fun setResources(transactionEntity: TransactionEntity) {
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

        // Detect if whole number or decimal toAddress change character size
        private fun setTransactionAmount(transactionEntity: TransactionEntity) {
            if (!transactionEntity.formattedAmount.contains(".")) {
                itemView.transactionAmount.text = transactionEntity.formattedAmount
            } else {
                itemView.transactionAmount.text = formatCharactersForAmount(
                    transactionEntity.formattedAmount.split(".")[0],
                    transactionEntity.formattedAmount.split(".")[1]
                )
            }
        }

        private fun setTokenType(transactionEntity: TransactionEntity) {
//            if (transactionEntity.tokenClassISO != GENESIS_XRD) {
                itemView.testTokensTextView.text = transactionEntity.tokenClassISO.split("/@")[1]
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

        // Detect if message attached toAddress transactionList, set and make VISIBLE or set as GONE
        private fun setMessage(transactionEntity: TransactionEntity) {
            if (transactionEntity.message != null) {
                itemView.messageTextView.visibility = View.VISIBLE
                itemView.messageTextView.text = transactionEntity.message
            } else {
                itemView.messageTextView.visibility = View.GONE
            }
        }

        private fun setClickListener(transactionEntity: TransactionEntity) {
            itemView.setOnClickListener {
                itemClick(transactionEntity, false)
            }
        }

        private fun setLongClickListener(transactionEntity: TransactionEntity) {
            itemView.setOnLongClickListener {
                itemClick(transactionEntity, true)
                return@setOnLongClickListener true
            }
        }
    }
}
