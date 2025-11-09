package com.nitzan.smsspamfilter

// Data class פשוט ללא Room annotations
data class SMSMessage(
    val id: Long = System.currentTimeMillis(),
    val sender: String,
    val content: String,
    val timestamp: Long,
    val isSpam: Boolean = false,
    val spamScore: Float = 0.0f,
    val isManuallyModified: Boolean = false
)