package com.radixdlt.android.apps.wallet.ui.adapter

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
import com.radixdlt.android.R
import com.radixdlt.android.apps.wallet.ui.fragment.assets.Asset
import com.radixdlt.android.apps.wallet.util.QueryPreferences
import kotlinx.android.synthetic.main.item_asset.view.*
import timber.log.Timber
import java.math.BigDecimal
import java.math.RoundingMode

class AssetsAdapter(
    private val items: MutableList<Asset> = mutableListOf(),
    private val itemClick: (String, Boolean) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private lateinit var ctx: Context
    private lateinit var myAddress: String
    private var listFiltered = items
    private val fullList = mutableListOf<Asset>()

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
        (holder as WalletViewHolder).bindList(listFiltered[position])
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            Timber.tag("payloads").d("empty")
            onBindViewHolder(holder, position)
        } else {
            val asset = listFiltered[position]
            for (data in payloads) {
                Timber.tag("payloads").d("$data")
                when (data as Int) {
                    UPDATE_TOTAL -> (holder as WalletViewHolder).setTotalAssetHoldings(asset)
                    UPDATE_NAME -> (holder as WalletViewHolder).setName(asset)
                    UPDATE_ICON -> (holder as WalletViewHolder).setIcon(asset)
                }
            }
        }
    }

    override fun getItemCount(): Int = listFiltered.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    listFiltered = fullList
                } else {
                    val filteredList = arrayListOf<Asset>()
                    fullList.forEach {
                        if (it.name != null) {
                            if (it.name!!.toLowerCase().contains(charString) || it.iso.toLowerCase().contains(charString)) {
                                filteredList.add(it)
                            }
                        } else {
                            if (it.iso.toLowerCase().contains(charString)) {
                                filteredList.add(it)
                            }
                        }
                    }

                    listFiltered = filteredList
                }

                val filterResults = FilterResults()
                filterResults.values = listFiltered
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults?) {
                listFiltered = filterResults?.values as MutableList<Asset>
                replace(listFiltered)
            }
        }
    }

    inner class WalletViewHolder(
        itemView: View,
        private val itemClick: (String, Boolean) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        fun bindList(item: Asset) {

            setName(item)
            setIcon(item)
            setTotalAssetHoldings(item)

            // Item click listeners
            setClickListener(item.iso)
            setLongClickListener(item.iso)
        }

        fun setName(item: Asset) {
            val name = item.name?.let { it } ?: item.iso
            itemView.addressTextView.text = name
        }

        // Set correct resources and colors depending if sent or received
        fun setIcon(item: Asset) {
            val urlIcon = item.urlIcon
            Glide.with(ctx)
                .load(urlIcon)
                .fallback(R.drawable.no_token_icon)
                .into(itemView.circleImageView)
        }

        // Detect if whole number or decimal toAddress change character size
        fun setTotalAssetHoldings(item: Asset) {
            val total = BigDecimal(item.total)
                .setScale(2, RoundingMode.HALF_UP)
                .toPlainString()
            val holdings = "$total ${item.iso}"
            itemView.transactionAmount.text = holdings
        }

        private fun setClickListener(item: String) {
            itemView.setOnClickListener {
                itemClick(item, false)
            }
        }

        private fun setLongClickListener(item: String) {
            itemView.setOnLongClickListener {
                //                itemClick(item, true)
                return@setOnLongClickListener true
            }
        }
    }

    @MainThread
    fun replace(assets: List<Asset>) {
        val difference = DiffUtil.calculateDiff(AssetsDiffUtil(items, assets), false)
        difference.dispatchUpdatesTo(this)
        items.clear()
        items.addAll(assets)
    }

    fun originalList(assets: List<Asset>) {
        fullList.clear()
        fullList.addAll(assets)
    }

    private class AssetsDiffUtil(
        private val oldList: List<Asset>,
        private val newList: List<Asset>
    ) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] === newList[newItemPosition]
        }

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].total == newList[newItemPosition].total &&
                oldList[oldItemPosition].name == newList[newItemPosition].name &&
                oldList[oldItemPosition].urlIcon == newList[newItemPosition].urlIcon
        }

        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
            return when {
                oldList[oldItemPosition].total != newList[newItemPosition].total -> UPDATE_TOTAL
                oldList[oldItemPosition].name != newList[newItemPosition].name -> UPDATE_NAME
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
