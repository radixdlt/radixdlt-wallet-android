package com.radixdlt.android.apps.wallet.ui.fragment.payment.assetselection

import android.content.Context
import android.text.method.ReplacementTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.radixdlt.android.R
import com.radixdlt.android.apps.wallet.ui.fragment.assets.AssetPayment
import com.radixdlt.android.apps.wallet.util.GENESIS_XRD
import com.radixdlt.android.apps.wallet.util.QueryPreferences
import com.radixdlt.client.atommodel.accounts.RadixAddress
import com.radixdlt.client.core.atoms.particles.RRI
import kotlinx.android.synthetic.main.item_asset_selection.view.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Locale

class PaymentSelectionAdapter(
    private val itemClick: (String) -> Unit,
    private val items: MutableList<AssetPayment> = mutableListOf()
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private lateinit var ctx: Context
    private lateinit var myAddress: String
    private var itemsFiltered = items
    private val itemsAll = mutableListOf<AssetPayment>()

    var selectedItem = -1
    var selectedAssetRRI = ""

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
            onBindViewHolder(holder, position)
        } else {
            val asset = itemsFiltered[position]
            for (data in payloads) {
                when (data as Int) {
                    UPDATE_SELECTED -> (holder as PaymentSelectionViewHolder).setRadioButton(asset)
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
                    val filteredList = arrayListOf<AssetPayment>()
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
                @Suppress("UNCHECKED_CAST")
                itemsFiltered = filterResults?.values as? MutableList<AssetPayment> ?: itemsAll
                replace(itemsFiltered)
            }
        }
    }

    object WordBreakTransformationMethod : ReplacementTransformationMethod() {
        private val dash = charArrayOf('-', '\u2011')
        private val space = charArrayOf(' ', '\u00A0')
        private val slash = charArrayOf('/', '\u2215')

        private val original = charArrayOf(dash[0], space[0], slash[0])
        private val replacement = charArrayOf(dash[1], space[1], slash[1])

        override fun getOriginal() = original
        override fun getReplacement() = replacement
    }

    inner class PaymentSelectionViewHolder(
        itemView: View,
        private val itemClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        fun bindList(item: AssetPayment) {
            setName(item)
            setIcon(item)
            setRadioButton(item)
            setIso(item)
            setRRI(item)

            setClickListener(item)
        }

        // Set name checking that it has been retrieved form token definition else set iso
        fun setName(item: AssetPayment) {
            val name = item.name?.let { it } ?: item.iso
            itemView.assetSelectionNameTextView.text = name
        }

        // Set correct resources and color depending if sent or received
        fun setIcon(item: AssetPayment) {
            val urlIcon = if (item.urlIcon.isNullOrBlank()) null else item.urlIcon
            Glide.with(ctx)
                .load(urlIcon)
                .fallback(R.drawable.no_token_icon)
                .into(itemView.assetSelectionCircleImageView)

            val rri = RRI.of(RadixAddress.from(item.address), item.iso).toString()

            Timber.tag("RRICheck").d(rri)
            if (rri == GENESIS_XRD) {
                itemView.assetSelectionRadixVerifiedCircleImageView.visibility = View.VISIBLE
            } else {
                itemView.assetSelectionRadixVerifiedCircleImageView.visibility = View.GONE
            }
        }

        // Set checked and unchecked radio buttons
        fun setRadioButton(item: AssetPayment) {
            if (item.selected) {
                itemView.assetSelectionRadioButton.isChecked = true
                selectedItem = adapterPosition
                val rri = RRI.of(RadixAddress.from(item.address), item.iso).toString()
                selectedAssetRRI = rri
            }

            // Necessary to see the animation of radio button... Android things
            itemView.assetSelectionRadioButton.post {
                if (!item.selected) {
                    itemView.assetSelectionRadioButton.isChecked = false
                }
            }
        }

        private fun setIso(item: AssetPayment) {
            val iso = "(${item.iso})"
            itemView.assetSelectionIsoTextView.text = iso
        }

        // Set RRI using WordBreakTransformationMethod due to forward slash wrapping issues
        private fun setRRI(item: AssetPayment) {
            itemView.assetSelectionRRITextView.apply {
                transformationMethod = WordBreakTransformationMethod
                text = ctx.getString(R.string.common_rri_address_string, item.address, item.iso)
            }
        }

        private fun setClickListener(item: AssetPayment) {
            itemView.setOnClickListener {
                if (!itemView.assetSelectionRadioButton.isChecked) {
                    itemView.assetSelectionRadioButton.isChecked = true
                } else {
                    return@setOnClickListener
                }

                // Find item that is selected and deselect it
                itemsFiltered.filter {
                    it.selected
                }.map { it.selected = false }

                item.selected = true

                // Add a delay so radio checks  and transition appear to be smooth
                (ctx as AppCompatActivity).lifecycleScope.launch {
                    val rri = RRI.of(RadixAddress.from(item.address), item.iso).toString()
                    itemClick(rri)
                    delay(250)
                    notifyDataSetChanged()
                }
            }
        }
    }

    @MainThread
    fun replace(assets: List<AssetPayment>) {
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

    fun originalList(assets: List<AssetPayment>) {
        itemsAll.clear()
        itemsAll.addAll(assets)
    }

    private class AssetsDiffUtil(
        private val oldList: List<AssetPayment>,
        private val newList: List<AssetPayment>
    ) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] === newList[newItemPosition]
        }

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].name == newList[newItemPosition].name &&
                oldList[oldItemPosition].urlIcon == newList[newItemPosition].urlIcon &&
                oldList[oldItemPosition].selected == newList[newItemPosition].selected
        }

        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
            return when {
                oldList[oldItemPosition].selected != newList[newItemPosition].selected -> UPDATE_SELECTED
                oldList[oldItemPosition].name != newList[newItemPosition].name -> UPDATE_NAME
                else -> UPDATE_ICON
            }
        }
    }

    companion object {
        const val UPDATE_SELECTED = 0
        const val UPDATE_NAME = 1
        const val UPDATE_ICON = 2
    }
}
