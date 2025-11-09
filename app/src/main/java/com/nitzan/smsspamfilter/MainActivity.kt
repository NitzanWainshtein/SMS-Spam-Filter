package com.nitzan.smsspamfilter

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    companion object {
        private const val SMS_PERMISSION_REQUEST = 100
    }

    private lateinit var messageStorage: MessageStorage
    private lateinit var blockedSendersManager: BlockedSendersManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        messageStorage = MessageStorage(this)
        blockedSendersManager = BlockedSendersManager(this)

        val btnPermissions = findViewById<Button>(R.id.btnPermissions)
        val btnViewMessages = findViewById<Button>(R.id.btnViewMessages)
        val btnBlockedSenders = findViewById<Button>(R.id.btnBlockedSenders)
        val tvSpamCount = findViewById<TextView>(R.id.tvSpamCount)
        val tvBlockedCount = findViewById<TextView>(R.id.tvBlockedCount)

        btnPermissions.setOnClickListener {
            requestSmsPermissions()
        }

        btnViewMessages.setOnClickListener {
            val intent = Intent(this, MessagesActivity::class.java)
            startActivity(intent)
        }

        btnBlockedSenders.setOnClickListener {
            val intent = Intent(this, BlockedSendersActivity::class.java)
            startActivity(intent)
        }

        // הצג סטטיסטיקות
        updateStats(tvSpamCount, tvBlockedCount)
    }

    private fun updateStats(tvSpamCount: TextView, tvBlockedCount: TextView) {
        val spamCount = messageStorage.getSpamCount()
        val blockedCount = blockedSendersManager.getBlockedCount()

        tvSpamCount.text = "🚫 הודעות ספאם שנחסמו: $spamCount"
        tvBlockedCount.text = "🔒 שולחים חסומים: $blockedCount"
    }

    override fun onResume() {
        super.onResume()
        val tvSpamCount = findViewById<TextView>(R.id.tvSpamCount)
        val tvBlockedCount = findViewById<TextView>(R.id.tvBlockedCount)
        updateStats(tvSpamCount, tvBlockedCount)
    }

    private fun requestSmsPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS
        )

        val permissionsNeeded = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions, SMS_PERMISSION_REQUEST)
        } else {
            openNotificationSettings()
        }
    }

    private fun openNotificationSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        startActivity(intent)
        Toast.makeText(this, "מצא את SMS Spam Filter והפעל", Toast.LENGTH_LONG).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == SMS_PERMISSION_REQUEST) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "הרשאות SMS ניתנו!", Toast.LENGTH_SHORT).show()
                openNotificationSettings()
            } else {
                Toast.makeText(this, "צריך הרשאות SMS כדי שהאפליקציה תעבוד", Toast.LENGTH_LONG).show()
            }
        }
    }
}