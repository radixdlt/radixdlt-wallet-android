package com.radixdlt.android.apps.wallet.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.radixdlt.android.R
import com.radixdlt.android.apps.wallet.ui.fragment.assets.Asset
import com.radixdlt.android.apps.wallet.util.QueryPreferences
import kotlinx.android.synthetic.main.item_asset.view.*

class AssetsAdapter(
    private val list: MutableList<Asset>,
    private val itemClick: (String, Boolean) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private lateinit var ctx: Context
    private lateinit var myAddress: String
    private var listFiltered = list

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

    override fun getItemCount(): Int = listFiltered.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    listFiltered = list
                } else {
                    val filteredList = arrayListOf<Asset>()
                    list.forEach {
                        if (it.name.toLowerCase().contains(charString) || it.iso.toLowerCase().contains(charString)) {
                            filteredList.add(it)
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
                notifyDataSetChanged()
            }
        }
    }

    inner class WalletViewHolder(
        itemView: View,
        private val itemClick: (String, Boolean) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        fun bindList(item: Asset) {

            setName(item.name)
            setResources(item.urlIcon)
            setTotalAssetHoldings(item.total)

            // Item click listeners
            setClickListener(item.iso)
            setLongClickListener(item.iso)
        }

        private fun setName(item: String) {
            itemView.addressTextView.text = item
        }

        // Set correct resources and colors depending if sent or received
        private fun setResources(item: String) {
            itemView.circleImageView.setImageResource(R.drawable.receive_image_item_wallet)
        }

        // Detect if whole number or decimal toAddress change character size
        private fun setTotalAssetHoldings(item: String) {
            itemView.transactionAmount.text = item
        }

        private fun setClickListener(item: String) {
            itemView.setOnClickListener {
                //                itemClick(item, false)
            }
        }

        private fun setLongClickListener(item: String) {
            itemView.setOnLongClickListener {
                //                itemClick(item, true)
                return@setOnLongClickListener true
            }
        }
    }
}
