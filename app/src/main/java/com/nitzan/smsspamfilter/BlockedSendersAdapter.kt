package com.nitzan.smsspamfilter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

data class BlockedSenderInfo(
    val senderNumber: String,
    val blockedDate: Long = System.currentTimeMillis(),
    val messagesCount: Int = 0
)

class BlockedSendersAdapter(
    private val onViewMessagesClick: (String) -> Unit,
    private val onUnblockClick: (String) -> Unit
) : ListAdapter<BlockedSenderInfo, BlockedSendersAdapter.BlockedSenderViewHolder>(BlockedSenderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockedSenderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_blocked_sender, parent, false)
        return BlockedSenderViewHolder(view)
    }

    override fun onBindViewHolder(holder: BlockedSenderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BlockedSenderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardBlockedSender: CardView = itemView.findViewById(R.id.cardBlockedSender)
        private val tvBlockedSender: TextView = itemView.findViewById(R.id.tvBlockedSender)
        private val tvBlockedDate: TextView = itemView.findViewById(R.id.tvBlockedDate)
        private val tvMessagesCount: TextView = itemView.findViewById(R.id.tvMessagesCount)
        private val btnViewMessages: MaterialButton = itemView.findViewById(R.id.btnViewMessages)
        private val btnUnblock: MaterialButton = itemView.findViewById(R.id.btnUnblock)

        fun bind(blockedSender: BlockedSenderInfo) {
            tvBlockedSender.text = blockedSender.senderNumber

            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            tvBlockedDate.text = "נחסם ב: ${dateFormat.format(Date(blockedSender.blockedDate))}"

            tvMessagesCount.text = "${blockedSender.messagesCount} הודעות נחסמו"

            btnViewMessages.setOnClickListener {
                onViewMessagesClick(blockedSender.senderNumber)
            }

            btnUnblock.setOnClickListener {
                onUnblockClick(blockedSender.senderNumber)
            }
        }
    }

    class BlockedSenderDiffCallback : DiffUtil.ItemCallback<BlockedSenderInfo>() {
        override fun areItemsTheSame(oldItem: BlockedSenderInfo, newItem: BlockedSenderInfo): Boolean {
            return oldItem.senderNumber == newItem.senderNumber
        }

        override fun areContentsTheSame(oldItem: BlockedSenderInfo, newItem: BlockedSenderInfo): Boolean {
            return oldItem == newItem
        }
    }
}