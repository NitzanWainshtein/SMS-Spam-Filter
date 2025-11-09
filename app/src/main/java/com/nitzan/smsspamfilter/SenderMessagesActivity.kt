package com.nitzan.smsspamfilter

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SenderMessagesActivity : AppCompatActivity() {

    private lateinit var messageStorage: MessageStorage
    private lateinit var messagesAdapter: MessagesCompactAdapter
    private lateinit var senderNumber: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sender_messages)

        messageStorage = MessageStorage(this)

        // קבל את מספר השולח
        senderNumber = intent.getStringExtra("SENDER_NUMBER") ?: ""
        if (senderNumber.isEmpty()) {
            finish()
            return
        }

        setupViews()
        setupRecyclerView()
        loadMessages()
    }

    private fun setupViews() {
        val btnBack = findViewById<Button>(R.id.btnBackSenderMessages)
        val tvSenderNumber = findViewById<TextView>(R.id.tvSenderNumber)

        btnBack.setOnClickListener {
            finish()
        }

        tvSenderNumber.text = "🚫 $senderNumber"
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewSenderMessages)

        messagesAdapter = MessagesCompactAdapter(
            onItemClick = { message -> openMessageDetail(message) },
            onItemLongClick = { message -> /* ללא תפריט כאן */ }
        )

        recyclerView.adapter = messagesAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadMessages() {
        // טען רק הודעות מהשולח הספציפי מהיסטוריית SMS האמיתית
        val messagesFromSender = messageStorage.getMessagesFromSender(senderNumber)

        messagesAdapter.submitList(messagesFromSender)

        Log.d("SenderMessages", "נטענו ${messagesFromSender.size} הודעות מהשולח $senderNumber")
    }

    private fun openMessageDetail(message: SMSMessage) {
        val intent = Intent(this, MessageDetailActivity::class.java)
        intent.putExtra("MESSAGE_ID", message.id)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        loadMessages()
    }
}