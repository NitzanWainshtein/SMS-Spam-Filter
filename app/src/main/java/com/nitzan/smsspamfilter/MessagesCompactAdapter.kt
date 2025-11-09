package com.nitzan.smsspamfilter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class MessagesCompactAdapter(
    private val onItemClick: (SMSMessage) -> Unit,
    private val onItemLongClick: (SMSMessage) -> Unit
) : ListAdapter<SMSMessage, MessagesCompactAdapter.CompactViewHolder>(MessageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message_compact, parent, false)
        return CompactViewHolder(view)
    }

    override fun onBindViewHolder(holder: CompactViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CompactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardMessage: CardView = itemView.findViewById(R.id.cardMessage)
        private val tvStatusIcon: TextView = itemView.findViewById(R.id.tvStatusIcon)
        private val tvSenderCompact: TextView = itemView.findViewById(R.id.tvSenderCompact)
        private val tvTimeCompact: TextView = itemView.findViewById(R.id.tvTimeCompact)
        private val tvContentCompact: TextView = itemView.findViewById(R.id.tvContentCompact)

        fun bind(message: SMSMessage) {
            // הגדרת מראה לפי סטטוס ספאם
            if (message.isSpam) {
                tvStatusIcon.text = "🚫"
                cardMessage.setCardBackgroundColor(
                    ContextCompat.getColor(itemView.context, android.R.color.holo_red_light)
                )
            } else {
                tvStatusIcon.text = "✅"
                cardMessage.setCardBackgroundColor(
                    ContextCompat.getColor(itemView.context, android.R.color.white)
                )
            }

            // נתונים בסיסיים
            tvSenderCompact.text = if (message.sender.isNotEmpty()) message.sender else "לא ידוע"

            val dateFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
            tvTimeCompact.text = dateFormat.format(Date(message.timestamp))

            tvContentCompact.text = message.content

            // אירועי לחיצה
            cardMessage.setOnClickListener {
                onItemClick(message)
            }

            cardMessage.setOnLongClickListener {
                onItemLongClick(message)
                true
            }
        }
    }

    class MessageDiffCallback : DiffUtil.ItemCallback<SMSMessage>() {
        override fun areItemsTheSame(oldItem: SMSMessage, newItem: SMSMessage): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SMSMessage, newItem: SMSMessage): Boolean {
            return oldItem == newItem
        }
    }
}