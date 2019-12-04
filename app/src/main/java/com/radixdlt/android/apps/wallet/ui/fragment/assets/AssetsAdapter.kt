package com.radixdlt.android.apps.wallet.ui.fragment.assets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.annotation.MainThread
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.radixdlt.android.apps.wallet.R
import com.radixdlt.android.apps.wallet.data.model.AssetEntity
import com.radixdlt.android.apps.wallet.util.QueryPreferences
import com.radixdlt.client.core.atoms.particles.RRI
import kotlinx.android.synthetic.main.item_asset.view.*
import timber.log.Timber
import java.math.RoundingMode
import java.util.Locale

class AssetsAdapter(
    private val itemClick: (String, String, String) -> Unit,
    private val items: MutableList<AssetEntity> = mutableListOf()
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private lateinit var ctx: Context
    private lateinit var myAddress: String
    private var itemsFiltered = items
    private val itemsAll = mutableListOf<AssetEntity>()

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        ctx = viewGroup.context
        myAddress = QueryPreferences.getPrefAddress(ctx)

        return WalletViewHolder(
            LayoutInflater.from(viewGroup.context).inflate(
                R.layout.item_asset,
                viewGroup, false
            ), itemClick
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as WalletViewHolder).bindList(itemsFiltered[position])
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            val asset = itemsFiltered[position]
            for (data in payloads) {
                when (data as Int) {
                    UPDATE_TOTAL -> (holder as WalletViewHolder).setTotalAssetHoldings(asset)
                    UPDATE_NAME -> (holder as WalletViewHolder).setName(asset)
                    UPDATE_ICON -> (holder as WalletViewHolder).setIcon(asset)
                }
            }
        }
    }

    override fun getItemCount(): Int = itemsFiltered.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    itemsFiltered = itemsAll
                } else {
                    val filteredList = arrayListOf<AssetEntity>()
                    itemsAll.forEach {
                        if (it.tokenName != null) {
                            if (it.tokenName.toLowerCase(Locale.ROOT).contains(charString) ||
                                RRI.fromString(it.rri).name.toLowerCase(Locale.ROOT).contains(charString)
                            ) {
                                filteredList.add(it)
                            }
                        } else {
                            if (RRI.fromString(it.rri).name.toLowerCase(Locale.ROOT).contains(charString)) {
                                filteredList.add(it)
                            }
                        }
                    }

                    itemsFiltered = filteredList
                }

                val filterResults = FilterResults()
                filterResults.values = itemsFiltered
                return filterResults
            }

            override fun publishResults(
                charSequence: CharSequence?,
                filterResults: FilterResults?
            ) {
                itemsFiltered = filterResults?.values as? MutableList<AssetEntity> ?: itemsAll
                replace(itemsFiltered)
            }
        }
    }

    inner class WalletViewHolder(
        itemView: View,
        private val itemClick: (String, String, String) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        fun bindList(item: AssetEntity) {

            setName(item)
            setIcon(item)
            setTotalAssetHoldings(item)

            // Item click listeners
            setClickListener(item.rri, item.tokenName!!, item.amount.toPlainString())
        }

        fun setName(item: AssetEntity) {
            val name = item.tokenName
            itemView.addressTextView.text = name
        }

        // Set correct resources and color depending if sent or received
        fun setIcon(item: AssetEntity) {
            val urlIcon = if (item.tokenIconUrl.isNullOrBlank()) null else item.tokenIconUrl
            Glide.with(ctx)
                .load(urlIcon)
                .fallback(R.drawable.no_token_icon)
                .into(itemView.circleImageView)
        }

        // Detect if whole number or decimal to change character size
        fun setTotalAssetHoldings(item: AssetEntity) {
            val total = item.amount
                .setScale(2, RoundingMode.HALF_UP)
                .toPlainString()
            val holdings = "$total ${item.tokenName}"
            itemView.transactionAmount.text = holdings
        }

        private fun setClickListener(rri: String, name: String, balance: String) {
            itemView.setOnClickListener {
                itemClick(rri, name, balance)
            }
        }
    }

    @MainThread
    fun replace(assets: List<AssetEntity>) {
        val difference = DiffUtil.calculateDiff(
            AssetsDiffUtil(
                items,
                assets
            ), false
        )
        difference.dispatchUpdatesTo(this)
        items.clear()
        items.addAll(assets)
    }

    fun originalList(assets: List<AssetEntity>) {
        itemsAll.clear()
        itemsAll.addAll(assets)
    }

    private class AssetsDiffUtil(
        private val oldList: List<AssetEntity>,
        private val newList: List<AssetEntity>
    ) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            Timber.tag("diffUtil").d("areItemsTheSame")
            return oldList[oldItemPosition] === newList[newItemPosition]
        }

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            Timber.tag("diffUtil").d("areContentsTheSame")
            return oldList[oldItemPosition].amount == newList[newItemPosition].amount &&
                oldList[oldItemPosition].tokenName == newList[newItemPosition].tokenName &&
                oldList[oldItemPosition].tokenIconUrl == newList[newItemPosition].tokenIconUrl
        }

        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
            Timber.tag("diffUtil").d("getChangePayload")
            return when {
                oldList[oldItemPosition].amount != newList[newItemPosition].amount -> UPDATE_TOTAL
                oldList[oldItemPosition].tokenName != newList[newItemPosition].tokenName -> UPDATE_NAME
                else -> UPDATE_ICON
            }
        }
    }

    companion object {
        const val UPDATE_TOTAL = 0
        const val UPDATE_NAME = 1
        const val UPDATE_ICON = 2
    }
}
