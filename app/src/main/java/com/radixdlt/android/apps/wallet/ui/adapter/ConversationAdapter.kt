package com.radixdlt.android.apps.wallet.ui.adapter

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.radixdlt.android.R
import com.radixdlt.android.apps.wallet.data.model.message.MessageEntity
import com.radixdlt.android.apps.wallet.util.formatDateDayMonthYear
import com.radixdlt.android.apps.wallet.util.getStartOfDay
import com.radixdlt.android.apps.wallet.util.isYesterday
import org.jetbrains.anko.find
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import timber.log.Timber
import java.util.Locale

class ConversationAdapter(
    val myAddress: String,
    var list: MutableList<MessageEntity>
) : RecyclerView.Adapter<ConversationAdapter.MessageListHolder>() {

    lateinit var ctx: Context

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int):
        MessageListHolder {
        ctx = viewGroup.context
        val inflater = LayoutInflater.from(viewGroup.context)
        val view: View

        view = when (viewType) {
            TYPE_RIGHT -> inflater.inflate(
                R.layout.list_item_message_right_triangle,
                viewGroup, false
            )
            TYPE_LEFT -> inflater.inflate(
                R.layout.list_item_message_left_triangle,
                viewGroup, false
            )
            else -> throw IllegalStateException("Unexpected viewType (= $viewType)")
        }

        return MessageListHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: MessageListHolder, position: Int) {
        when (holder.itemViewType) {
            TYPE_RIGHT -> onBindItemsViewHolder(holder, position)
            TYPE_LEFT -> onBindItemsViewHolder(holder, position)
        }
    }

    private fun onBindItemsViewHolder(messageListHolder: MessageListHolder, position: Int) {
        val conversation = list[position]
        val startOfDay = getStartOfDay(conversation.timestamp)
        var startOfPreviousDay: Long = 0
        if (position != 0) {
            startOfPreviousDay = getDatePreviousPosition(position)
            Timber.d("Start of previous day %d", startOfPreviousDay)
        }

        Timber.d("Start of day %d, %d", startOfDay, conversation.timestamp)

        if (position == 0 || conversation.fromAddress != list[position - 1].fromAddress &&
            startOfDay != startOfPreviousDay
        ) {
            messageListHolder.bindMessageListItemTriangleAndDate(conversation)
        } else if (conversation.fromAddress != list[position - 1].fromAddress &&
            startOfDay == startOfPreviousDay
        ) {
            messageListHolder.bindMessageListItemTriangleAndUser(conversation)
        } else if (startOfDay == startOfPreviousDay) {
            messageListHolder.bindMessageListItem(conversation)
        } else {
            messageListHolder.bindMessageListItemTriangleAndDate(conversation)
        }
    }

    private fun getDatePreviousPosition(position: Int): Long {
        return getStartOfDay((list[position - 1].timestamp))
    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position].fromAddress == myAddress) TYPE_RIGHT else TYPE_LEFT
    }

    inner class MessageListHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var constLayout: ConstraintLayout =
            itemView.find(R.id.constraintLayout_speech_bubble)
        private var lblFrom: TextView = itemView.find(R.id.lblMsgFrom)
        private var lblDate: TextView = itemView.find(R.id.lblMsgDate)
        private var txtMsg: TextView = itemView.find(R.id.txtMsg)
        private var txtTime: TextView = itemView.find(R.id.txtTime)

        fun bindMessageListItemTriangleAndDate(conversation: MessageEntity) {
            Timber.d("bindMessageListItemTriangleAndDate")
            lblFrom.visibility = View.VISIBLE
            lblDate.visibility = View.VISIBLE

            constLayout.background = if (conversation.fromAddress == myAddress) {
                ContextCompat.getDrawable(ctx, R.drawable.speech_bubble)
            } else {
                ContextCompat.getDrawable(ctx, R.drawable.speech_bubble_recipient)
            }

            lblFrom.text = if (conversation.fromAddress == myAddress) {
                ctx.getString(R.string.conversation_activity_me_label)
            } else {
                conversation.fromAddress
            }

            val isToday = DateUtils.isToday(conversation.timestamp)

            val isYesterday = isYesterday(conversation.timestamp)

            if (isToday) {
                lblDate.text = ctx.getString(R.string.conversation_activity_today_label)
            } else {
                lblDate.text = if (isYesterday) {
                    ctx.getString(R.string.conversation_activity_yesterday_label)
                } else {
                    formatDateDayMonthYear(conversation.timestamp)
                }
            }

            val textMessage =
                String.format(
                    ctx.getString(R.string.conversation_activity_message_bubble),
                    conversation.content
                )
            txtMsg.text = textMessage

            val localDateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(conversation.timestamp),
                ZoneId.systemDefault()
            )

            val displayValue = localDateTime.format(
                DateTimeFormatter.ofPattern(TIME_FORMAT, Locale.getDefault())
            )

            txtTime.text = displayValue
        }

        fun bindMessageListItemTriangleAndUser(conversation: MessageEntity) {
            Timber.d("bindMessageListItemTriangleAndUser")
            lblFrom.visibility = View.VISIBLE

            lblFrom.text = if (conversation.fromAddress == myAddress) {
                ctx.getString(R.string.conversation_activity_me_label)
            } else {
                conversation.fromAddress
            }

            lblDate.visibility = View.GONE

            constLayout.background = if (conversation.fromAddress == myAddress) {
                ContextCompat.getDrawable(ctx, R.drawable.speech_bubble)
            } else {
                ContextCompat.getDrawable(ctx, R.drawable.speech_bubble_recipient)
            }

            val textMessage = String.format(
                ctx.getString(R.string.conversation_activity_message_bubble),
                conversation.content
            )
            txtMsg.text = textMessage

            val localDateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(conversation.timestamp),
                ZoneId.systemDefault()
            )

            val displayValue = localDateTime.format(
                DateTimeFormatter.ofPattern(TIME_FORMAT, Locale.getDefault())
            )
            txtTime.text = displayValue
        }

        fun bindMessageListItem(conversation: MessageEntity) {
            Timber.d("bindMessageListItem")
            lblFrom.visibility = View.GONE
            lblDate.visibility = View.GONE

            constLayout.background = if (conversation.fromAddress == myAddress) {
                ContextCompat.getDrawable(ctx, R.drawable.speech_bubble_default)
            } else {
                ContextCompat.getDrawable(ctx, R.drawable.speech_bubble_default_recipient)
            }

            val textMessage = String.format(
                ctx.getString(R.string.conversation_activity_message_bubble),
                conversation.content
            )
            txtMsg.text = textMessage

            val localDateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(conversation.timestamp),
                ZoneId.systemDefault()
            )

            val displayValue = localDateTime.format(
                DateTimeFormatter.ofPattern(TIME_FORMAT, Locale.getDefault())
            )
            txtTime.text = displayValue
        }
    }

    companion object {
        const val TYPE_RIGHT = 0
        const val TYPE_LEFT = 1
        const val TIME_FORMAT = "HH:mm"
    }
}
