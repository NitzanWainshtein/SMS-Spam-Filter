package com.nitzan.smsspamfilter

import android.content.Intent
import android.os.Bundle
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MessagesActivity : AppCompatActivity() {

    private lateinit var messageStorage: MessageStorage
    private lateinit var messagesAdapter: MessagesCompactAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)

        messageStorage = MessageStorage(this)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewMessages)
        messagesAdapter = MessagesCompactAdapter(
            onItemClick = { message -> openMessageDetail(message) },
            onItemLongClick = { message -> showQuickActionMenu(message) }
        )

        recyclerView.adapter = messagesAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadMessages()
    }

    private fun openMessageDetail(message: SMSMessage) {
        val intent = Intent(this, MessageDetailActivity::class.java)
        intent.putExtra("MESSAGE_ID", message.id)
        startActivity(intent)
    }

    private fun showQuickActionMenu(message: SMSMessage) {
        val popup = PopupMenu(this, null)
        popup.menu.add(0, 1, 0, if (message.isSpam) "סמן כהודעה רגילה" else "סמן כספאם")
        popup.menu.add(0, 2, 0, "פתח פרטים מלאים")

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> {
                    updateMessageStatus(message, !message.isSpam)
                    true
                }
                2 -> {
                    openMessageDetail(message)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun updateMessageStatus(message: SMSMessage, isSpam: Boolean) {
        val updatedMessage = message.copy(
            isSpam = isSpam,
            isManuallyModified = true
        )
        messageStorage.updateMessage(updatedMessage)

        // עדכן את המודל ML
        val spamDetector = SpamDetectorML(this)
        spamDetector.learnFromUser(message.content, message.sender, isSpam)

        // רענן את הרשימה
        loadMessages()

        val statusText = if (isSpam) "ספאם" else "רגיל"
        Toast.makeText(this, "ההודעה עודכנה כ$statusText", Toast.LENGTH_SHORT).show()
    }

    private fun loadMessages() {
        val messages = messageStorage.getAllMessages()
        messagesAdapter.submitList(messages)
    }

    override fun onResume() {
        super.onResume()
        loadMessages()
    }
}