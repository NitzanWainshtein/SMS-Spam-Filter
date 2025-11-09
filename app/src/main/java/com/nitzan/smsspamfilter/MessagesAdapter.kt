package com.nitzan.smsspamfilter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class MessagesAdapter(
    private val onStatusChange: (SMSMessage, Boolean) -> Unit
) : ListAdapter<SMSMessage, MessagesAdapter.MessageViewHolder>(MessageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSender: TextView = itemView.findViewById(R.id.tvSender)
        private val tvContent: TextView = itemView.findViewById(R.id.tvContent)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val btnMarkSpam: Button = itemView.findViewById(R.id.btnMarkSpam)
        private val btnMarkLegit: Button = itemView.findViewById(R.id.btnMarkLegit)

        fun bind(message: SMSMessage) {
            tvSender.text = "מאת: ${message.sender}"
            tvContent.text = message.content

            val dateFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
            tvTime.text = dateFormat.format(Date(message.timestamp))

            if (message.isSpam) {
                tvStatus.text = "🚫 ספאם"
                tvStatus.setTextColor(itemView.context.getColor(android.R.color.holo_red_dark))
                itemView.setBackgroundColor(itemView.context.getColor(android.R.color.holo_red_light))
            } else {
                tvStatus.text = "✅ רגיל"
                tvStatus.setTextColor(itemView.context.getColor(android.R.color.holo_green_dark))
                itemView.setBackgroundColor(itemView.context.getColor(android.R.color.white))
            }

            btnMarkSpam.setOnClickListener {
                onStatusChange(message, true)
            }

            btnMarkLegit.setOnClickListener {
                onStatusChange(message, false)
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