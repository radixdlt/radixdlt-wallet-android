package com.radixdlt.android.apps.wallet.ui.fragment.payment.assetselection

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.annotation.MainThread
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.radixdlt.android.R
import com.radixdlt.android.apps.wallet.ui.fragment.assets.Asset
import com.radixdlt.android.apps.wallet.util.QueryPreferences
import com.radixdlt.client.atommodel.accounts.RadixAddress
import com.radixdlt.client.core.atoms.particles.RRI
import kotlinx.android.synthetic.main.item_asset_selection.view.*
import timber.log.Timber
import java.util.Locale

class PaymentSelectionAdapter(
    private val itemClick: (String, String, String) -> Unit,
    private val items: MutableList<Asset> = mutableListOf()
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private lateinit var ctx: Context
    private lateinit var myAddress: String
    private var itemsFiltered = items
    private val itemsAll = mutableListOf<Asset>()

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        ctx = viewGroup.context
        myAddress = QueryPreferences.getPrefAddress(ctx)

        return PaymentSelectionViewHolder(
            LayoutInflater.from(ctx).inflate(R.layout.item_asset_selection, viewGroup, false),
            itemClick
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as PaymentSelectionViewHolder).bindList(itemsFiltered[position])
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            Timber.tag("payloads").d("empty")
            onBindViewHolder(holder, position)
        } else {
            val asset = itemsFiltered[position]
            for (data in payloads) {
                Timber.tag("payloads").d("$data")
                when (data as Int) {
                    UPDATE_NAME -> (holder as PaymentSelectionViewHolder).setName(asset)
                    UPDATE_ICON -> (holder as PaymentSelectionViewHolder).setIcon(asset)
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
                    val filteredList = arrayListOf<Asset>()
                    itemsAll.forEach {
                        if (it.name != null) {
                            if (it.name!!.toLowerCase(Locale.ROOT).contains(charString) ||
                                it.iso.toLowerCase(Locale.ROOT).contains(charString)
                            ) {
                                filteredList.add(it)
                            }
                        } else {
                            if (it.iso.toLowerCase(Locale.ROOT).contains(charString)) {
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
                itemsFiltered = filterResults?.values as? MutableList<Asset> ?: itemsAll
                replace(itemsFiltered)
            }
        }
    }

    inner class PaymentSelectionViewHolder(
        itemView: View,
        private val itemClick: (String, String, String) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        fun bindList(item: Asset) {

            setName(item)
            setIcon(item)

            itemView.assetSelectionIsoTextView.text = "(${item.iso})"
            itemView.assetSelectionRRITextView.setText(
                ctx.getString(R.string.common_rri_address_string, item.address, item.iso),
                TextView.BufferType.EDITABLE
            )

            // Item click listeners
            val rri = RRI.of(RadixAddress.from(item.address), item.iso).toString()
            setClickListener(rri, item.name ?: item.iso, item.total)
        }

        fun setName(item: Asset) {
            val name = item.name?.let { it } ?: item.iso
            itemView.assetSelectionNameTextView.text = name
        }

        // Set Iso
        // Set RRI

        // Set correct resources and color depending if sent or received
        fun setIcon(item: Asset) {
            val urlIcon = if (item.urlIcon.isNullOrBlank()) null else item.urlIcon
            Glide.with(ctx)
                .load(urlIcon)
                .fallback(R.drawable.no_token_icon)
                .into(itemView.assetSelectionCircleImageView)
        }

        private fun setClickListener(rri: String, name: String, balance: String) {
            itemView.setOnClickListener {
                itemClick(rri, name, balance)
            }
        }
    }

    @MainThread
    fun replace(assets: List<Asset>) {
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

    fun originalList(assets: List<Asset>) {
        itemsAll.clear()
        itemsAll.addAll(assets)
    }

    private class AssetsDiffUtil(
        private val oldList: List<Asset>,
        private val newList: List<Asset>
    ) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            Timber.tag("diffUtil").d("areItemsTheSame")
            return oldList[oldItemPosition] === newList[newItemPosition]
        }

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            Timber.tag("diffUtil").d("areContentsTheSame")
            return oldList[oldItemPosition].name == newList[newItemPosition].name &&
                oldList[oldItemPosition].urlIcon == newList[newItemPosition].urlIcon
        }

        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
            Timber.tag("diffUtil").d("getChangePayload")
            return when {
                oldList[oldItemPosition].name != newList[newItemPosition].name -> UPDATE_NAME
                else -> UPDATE_ICON
            }
        }
    }

    companion object {
        const val UPDATE_NAME = 0
        const val UPDATE_ICON = 1
    }
}
