package com.radixdlt.android.ui.adapter

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.radixdlt.android.R
import com.radixdlt.android.data.model.message.MessageEntity
import com.radixdlt.android.util.FAUCET_ADDRESS
import com.radixdlt.android.util.QueryPreferences
import com.radixdlt.android.util.setAddressWithColors
import kotlinx.android.synthetic.main.item_contact.view.*

class ContactsAdapter(
    var list: MutableList<MessageEntity>,
    private val itemClick: (String, Boolean) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    lateinit var ctx: Context

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        ctx = viewGroup.context
        return ContactsViewHolder(
            LayoutInflater
                .from(ctx).inflate(R.layout.item_contact, viewGroup, false), itemClick
        )
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ContactsViewHolder).bindList(list[position])
    }

    inner class ContactsViewHolder(
        itemView: View,
        private val itemClick: (String, Boolean) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        fun bindList(messageEntity: MessageEntity) {

            val contactAddress = getContactAddress(messageEntity)

            displayContacts(contactAddress, messageEntity, itemView)

            setClickListener(contactAddress)
            setLongClickListener(contactAddress)
        }

        private fun getContactAddress(messageEntity: MessageEntity): String {
            return if (messageEntity.fromAddress == QueryPreferences.getPrefAddress(ctx)) {
                messageEntity.toAddress
            } else {
                messageEntity.fromAddress
            }
        }

        private fun setLongClickListener(contactAddress: String) {
            itemView.setOnLongClickListener {
                itemClick(contactAddress, true)
                return@setOnLongClickListener true
            }
        }

        private fun setClickListener(contactAddress: String) {
            itemView.setOnClickListener { itemClick(contactAddress, false) }
        }
    }

    private fun displayContacts(address: String, messageEntities: MessageEntity, itemView: View) {

        itemView.addressTextView.text = if (address == FAUCET_ADDRESS) {
            ctx.getString(R.string.contacts_fragment_user_faucet)
        } else {
            setAddressWithColors(ctx, address, R.color.materialDarkGrey)
        }

        itemView.messageTextView.text = messageEntities.content

        // This is to add a more dynamic date/time
        if (messageEntities.timestamp + 3000 >= System.currentTimeMillis()) {
            itemView.timeTextView.text = ctx.getString(R.string.contacts_fragment_now)
        } else {
            itemView.timeTextView.text = DateUtils.getRelativeDateTimeString(
                ctx,
                messageEntities.timestamp, DateUtils.SECOND_IN_MILLIS,
                DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL
            )
        }
    }
}
