package com.nitzan.smsspamfilter

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class SMSNotificationListener : NotificationListenerService() {

    private lateinit var spamDetector: SpamDetectorML

    override fun onCreate() {
        super.onCreate()
        spamDetector = SpamDetectorML(applicationContext)
        Log.d("SMS_FILTER", "🔄 NotificationListener נוצר - טוען נתוני למידה")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        if (sbn?.packageName == "com.google.android.apps.messaging" ||
            sbn?.packageName == "com.android.mms") {

            val notification = sbn.notification
            val extras = notification.extras

            val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

            // התעלם מהודעות מערכת
            if (text.contains("Messages is doing work in the background")) {
                return
            }

            // יצירה חדשה של המודל כדי לוודא שהוא טוען נתונים עדכניים
            val freshSpamDetector = SpamDetectorML(applicationContext)
            val isSpam = freshSpamDetector.detectSpam(text, title)

            if (isSpam) {
                Log.d("SMS_FILTER", "🤖 ML זיהה ספאם - מבטל התראה: $text")
                cancelNotification(sbn.key)
            } else {
                Log.d("SMS_FILTER", "✅ ML: הודעה תקינה: $text")
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
    }
}