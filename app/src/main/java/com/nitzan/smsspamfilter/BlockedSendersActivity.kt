package com.nitzan.smsspamfilter

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class BlockedSendersActivity : AppCompatActivity() {

    private lateinit var blockedSendersManager: BlockedSendersManager
    private lateinit var messageStorage: MessageStorage
    private lateinit var blockedSendersAdapter: BlockedSendersAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blocked_senders)

        blockedSendersManager = BlockedSendersManager(this)
        messageStorage = MessageStorage(this)

        setupViews()
        setupRecyclerView()
        loadBlockedSenders()
    }

    private fun setupViews() {
        val btnBack = findViewById<Button>(R.id.btnBackBlocked)
        recyclerView = findViewById(R.id.recyclerViewBlockedSenders)
        emptyLayout = findViewById(R.id.layoutEmptyBlocked)

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        blockedSendersAdapter = BlockedSendersAdapter(
            onViewMessagesClick = { senderNumber ->
                viewMessagesFromSender(senderNumber)
            },
            onUnblockClick = { senderNumber ->
                showUnblockDialog(senderNumber)
            }
        )

        recyclerView.adapter = blockedSendersAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadBlockedSenders() {
        val blockedSenders = blockedSendersManager.getAllBlockedSenders()

        if (blockedSenders.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyLayout.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyLayout.visibility = View.GONE

            // יצירת רשימה עם מידע מלא על כל שולח חסום
            val blockedSendersInfo = blockedSenders.map { sender ->
                // עכשיו נקבל את ההודעות האמיתיות מהיסטוריית SMS
                val messagesFromSender = messageStorage.getMessagesFromSender(sender)

                BlockedSenderInfo(
                    senderNumber = sender,
                    blockedDate = System.currentTimeMillis(),
                    messagesCount = messagesFromSender.size
                )
            }.sortedByDescending { it.messagesCount }

            blockedSendersAdapter.submitList(blockedSendersInfo)
        }
    }

    private fun viewMessagesFromSender(senderNumber: String) {
        val intent = Intent(this, SenderMessagesActivity::class.java)
        intent.putExtra("SENDER_NUMBER", senderNumber)
        startActivity(intent)
    }

    private fun showUnblockDialog(senderNumber: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("ביטול חסימת שולח")
            .setMessage("האם אתה בטוח שברצונך לבטל את חסימת השולח $senderNumber?")
            .setPositiveButton("בטל חסימה") { _, _ ->
                unblockSender(senderNumber)
            }
            .setNegativeButton("ביטול", null)
            .show()
    }

    private fun unblockSender(senderNumber: String) {
        val success = blockedSendersManager.unblockSender(senderNumber)

        if (success) {
            Toast.makeText(this, "השולח $senderNumber הוסר מהרשימה השחורה", Toast.LENGTH_SHORT).show()
            loadBlockedSenders()
        } else {
            Toast.makeText(this, "שגיאה בביטול חסימת השולח", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        loadBlockedSenders()
    }
}