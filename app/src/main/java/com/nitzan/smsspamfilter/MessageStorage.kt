package com.nitzan.smsspamfilter

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony
import android.util.Log

class MessageStorage(private val context: Context) {

    fun getAllMessages(): List<SMSMessage> {
        val messages = mutableListOf<SMSMessage>()

        try {
            val cursor = context.contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                arrayOf(
                    Telephony.Sms._ID,
                    Telephony.Sms.ADDRESS,
                    Telephony.Sms.BODY,
                    Telephony.Sms.DATE,
                    Telephony.Sms.TYPE
                ),
                "${Telephony.Sms.TYPE} = ?", // רק הודעות נכנסות
                arrayOf(Telephony.Sms.MESSAGE_TYPE_INBOX.toString()),
                "${Telephony.Sms.DATE} DESC LIMIT 500" // הגבל ל-500 הודעות אחרונות
            )

            cursor?.use {
                val spamDetector = SpamDetectorML(context)

                while (it.moveToNext()) {
                    val id = it.getLong(it.getColumnIndexOrThrow(Telephony.Sms._ID))
                    val address = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)) ?: ""
                    val body = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.BODY)) ?: ""
                    val date = it.getLong(it.getColumnIndexOrThrow(Telephony.Sms.DATE))

                    // בדוק אם זה ספאם לפי המודל הנוכחי
                    val isSpam = spamDetector.detectSpam(body, address)

                    messages.add(SMSMessage(
                        id = id,
                        sender = address,
                        content = body,
                        timestamp = date,
                        isSpam = isSpam
                    ))
                }
            }

            Log.d("MessageStorage", "נטענו ${messages.size} הודעות מהיסטוריית SMS")

        } catch (e: Exception) {
            Log.e("MessageStorage", "שגיאה בקריאת SMS: ${e.message}")
        }

        return messages
    }

    fun getMessagesFromSender(sender: String): List<SMSMessage> {
        return getAllMessages().filter {
            normalizeSender(it.sender) == normalizeSender(sender)
        }
    }

    fun getSpamCount(): Int {
        return getAllMessages().count { it.isSpam }
    }

    // נרמול מספר טלפון
    private fun normalizeSender(sender: String): String {
        return sender.replace(Regex("[\\s\\-\\(\\)\\+]"), "")
            .replace("^972".toRegex(), "0")
            .trim()
    }

    // הפונקציות האלה נשארות לתאימות אחורה
    fun saveMessage(message: SMSMessage) {
        // כבר לא צריך - קוראים ישירות מהמערכת
        Log.d("MessageStorage", "saveMessage מיושן - קוראים ישירות מהמערכת")
    }

    fun updateMessage(updatedMessage: SMSMessage) {
        // עדכון ייעשה רק במודל ML, לא במסד הנתונים
        Log.d("MessageStorage", "updateMessage מיושן - השינוי נשמר במודל ML")
    }
}