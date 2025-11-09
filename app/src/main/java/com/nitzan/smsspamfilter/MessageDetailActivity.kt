package com.nitzan.smsspamfilter

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class MessageDetailActivity : AppCompatActivity() {

    private lateinit var messageStorage: MessageStorage
    private lateinit var blockedSendersManager: BlockedSendersManager
    private lateinit var currentMessage: SMSMessage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_detail)

        messageStorage = MessageStorage(this)
        blockedSendersManager = BlockedSendersManager(this)

        // קבל את ההודעה מה-Intent
        val messageId = intent.getLongExtra("MESSAGE_ID", -1)
        if (messageId == -1L) {
            finish()
            return
        }

        currentMessage = messageStorage.getAllMessages().find { it.id == messageId } ?: run {
            finish()
            return
        }

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        val tvSenderDetail = findViewById<TextView>(R.id.tvSenderDetail)
        val tvTimeDetail = findViewById<TextView>(R.id.tvTimeDetail)
        val tvContentDetail = findViewById<TextView>(R.id.tvContentDetail)
        val tvCurrentStatus = findViewById<TextView>(R.id.tvCurrentStatus)
        val cardCurrentStatus = findViewById<CardView>(R.id.cardCurrentStatus)

        // מלא נתונים
        tvSenderDetail.text = if (currentMessage.sender.isNotEmpty()) currentMessage.sender else "מספר לא ידוע"

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        tvTimeDetail.text = dateFormat.format(Date(currentMessage.timestamp))

        tvContentDetail.text = currentMessage.content

        // הצג סטטוס נוכחי
        if (currentMessage.isSpam) {
            tvCurrentStatus.text = "🚫 הודעת ספאם"
            tvCurrentStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
            cardCurrentStatus.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_light))
        } else {
            tvCurrentStatus.text = "✅ הודעה רגילה"
            tvCurrentStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
            cardCurrentStatus.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_light))
        }
    }

    private fun setupClickListeners() {
        val btnBack = findViewById<Button>(R.id.btnBack)
        val btnMarkSpam = findViewById<MaterialButton>(R.id.btnMarkSpamDetail)
        val btnMarkLegit = findViewById<MaterialButton>(R.id.btnMarkLegitDetail)
        val btnBlockSender = findViewById<MaterialButton>(R.id.btnBlockSender)
        val btnMarkSenderLegit = findViewById<MaterialButton>(R.id.btnMarkSenderLegit)

        btnBack.setOnClickListener {
            finish()
        }

        btnMarkSpam.setOnClickListener {
            updateMessageStatus(true)
        }

        btnMarkLegit.setOnClickListener {
            updateMessageStatus(false)
        }

        btnBlockSender.setOnClickListener {
            showBlockSenderDialog()
        }

        btnMarkSenderLegit.setOnClickListener {
            showMarkSenderLegitDialog()
        }

        updateBlockButtonState()
    }

    private fun showMarkSenderLegitDialog() {
        val messagesFromSender = messageStorage.getMessagesFromSender(currentMessage.sender)
        val spamCount = messagesFromSender.count { it.isSpam }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("השולח אינו ספאם")
            .setMessage("""
                האם אתה בטוח שהשולח ${currentMessage.sender} אינו ספאם?
                
                פעולה זו תסמן את כל $spamCount ההודעות ממספר זה כ"לא ספאם" ותלמד את המערכת שהשולח הזה לגיטימי.
            """.trimIndent())
            .setPositiveButton("כן, השולח לגיטימי") { _, _ ->
                markAllSenderMessagesAsLegit()
            }
            .setNegativeButton("ביטול", null)
            .show()
    }

    private fun markAllSenderMessagesAsLegit() {
        val spamDetector = SpamDetectorML(this)
        val messagesFromSender = messageStorage.getMessagesFromSender(currentMessage.sender)

        var updatedCount = 0

        messagesFromSender.forEach { message ->
            if (message.isSpam) {
                // לימד את המודל שזה לא ספאם
                spamDetector.learnFromUser(message.content, message.sender, false)
                updatedCount++
            }
        }

        // הסר מרשימת החסומים אם קיים
        if (blockedSendersManager.isBlocked(currentMessage.sender)) {
            blockedSendersManager.unblockSender(currentMessage.sender)
        }

        Toast.makeText(
            this,
            "✅ $updatedCount הודעות מ-${currentMessage.sender} סומנו כלגיטימיות",
            Toast.LENGTH_LONG
        ).show()

        // עדכן את התצוגה
        currentMessage = currentMessage.copy(isSpam = false, isManuallyModified = true)
        initViews()
        updateBlockButtonState()

        // חזור למסך הקודם אחרי רגע
        findViewById<TextView>(R.id.tvCurrentStatus).postDelayed({
            finish()
        }, 1500)
    }

    private fun updateBlockButtonState() {
        val btnBlockSender = findViewById<MaterialButton>(R.id.btnBlockSender)
        val isBlocked = blockedSendersManager.isBlocked(currentMessage.sender)

        if (isBlocked) {
            btnBlockSender.text = "🔓 בטל חסימת שולח"
            btnBlockSender.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.holo_orange_dark)
        } else {
            btnBlockSender.text = "🔒 חסום שולח זה לתמיד"
            btnBlockSender.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.holo_orange_light)
        }
    }

    private fun showBlockSenderDialog() {
        val isBlocked = blockedSendersManager.isBlocked(currentMessage.sender)
        val message = if (isBlocked) {
            "האם אתה בטוח שברצונך לבטל את חסימת השולח ${currentMessage.sender}?"
        } else {
            "האם אתה בטוח שברצונך לחסום את השולח ${currentMessage.sender}? כל הודעות עתידיות ממספר זה יחסמו אוטומטית."
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(if (isBlocked) "ביטול חסימה" else "חסימת שולח")
            .setMessage(message)
            .setPositiveButton(if (isBlocked) "בטל חסימה" else "חסום") { _, _ ->
                toggleBlockSender()
            }
            .setNegativeButton("ביטול", null)
            .show()
    }

    private fun toggleBlockSender() {
        val wasBlocked = blockedSendersManager.isBlocked(currentMessage.sender)

        if (wasBlocked) {
            blockedSendersManager.unblockSender(currentMessage.sender)
            Toast.makeText(this, "חסימת השולח בוטלה", Toast.LENGTH_SHORT).show()
        } else {
            blockedSendersManager.blockSender(currentMessage.sender)
            Toast.makeText(this, "השולח נחסם בהצלחה", Toast.LENGTH_SHORT).show()
            updateMessageStatus(true)
        }

        updateBlockButtonState()
    }

    private fun updateMessageStatus(isSpam: Boolean) {
        val spamDetector = SpamDetectorML(this)
        spamDetector.learnFromUser(currentMessage.content, currentMessage.sender, isSpam)

        currentMessage = currentMessage.copy(
            isSpam = isSpam,
            isManuallyModified = true
        )

        initViews()

        val statusText = if (isSpam) "ספאם" else "רגיל"
        Toast.makeText(this, "ההודעה עודכנה כ$statusText", Toast.LENGTH_SHORT).show()

        findViewById<TextView>(R.id.tvCurrentStatus).postDelayed({
            finish()
        }, 1000)
    }
}