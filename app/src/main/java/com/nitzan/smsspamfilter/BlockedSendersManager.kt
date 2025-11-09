package com.nitzan.smsspamfilter

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class BlockedSendersManager(context: Context) {

    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences("blocked_senders", Context.MODE_PRIVATE)
    private val gson = Gson()

    private var blockedSenders = mutableSetOf<String>()

    init {
        loadBlockedSenders()
    }

    fun blockSender(sender: String): Boolean {
        val normalizedSender = normalizeSender(sender)
        if (normalizedSender.isNotEmpty()) {
            blockedSenders.add(normalizedSender)
            saveBlockedSenders()
            Log.d("BLOCKED_SENDERS", "שולח נחסם: $normalizedSender")
            return true
        }
        return false
    }

    fun unblockSender(sender: String): Boolean {
        val normalizedSender = normalizeSender(sender)
        val wasBlocked = blockedSenders.remove(normalizedSender)
        if (wasBlocked) {
            saveBlockedSenders()
            Log.d("BLOCKED_SENDERS", "חסימת שולח בוטלה: $normalizedSender")
        }
        return wasBlocked
    }

    fun isBlocked(sender: String): Boolean {
        val normalizedSender = normalizeSender(sender)
        val blocked = blockedSenders.contains(normalizedSender)
        if (blocked) {
            Log.d("BLOCKED_SENDERS", "שולח חסום זוהה: $normalizedSender")
        }
        return blocked
    }

    fun getAllBlockedSenders(): Set<String> {
        return blockedSenders.toSet()
    }

    fun getBlockedCount(): Int {
        return blockedSenders.size
    }

    // נרמול מספר טלפון - הסרת רווחים, מקפים וכו'
    private fun normalizeSender(sender: String): String {
        return sender.replace(Regex("[\\s\\-\\(\\)\\+]"), "")
            .replace("^972".toRegex(), "0") // המר +972 ל-0
            .trim()
    }

    private fun saveBlockedSenders() {
        val json = gson.toJson(blockedSenders.toList())
        sharedPrefs.edit().putString("blocked_list", json).apply()
        Log.d("BLOCKED_SENDERS", "רשימה שחורה נשמרה: ${blockedSenders.size} שולחים")
    }

    private fun loadBlockedSenders() {
        val json = sharedPrefs.getString("blocked_list", "[]") ?: "[]"
        val type = object : TypeToken<List<String>>() {}.type
        val loadedList: List<String> = gson.fromJson(json, type) ?: emptyList()
        blockedSenders = loadedList.toMutableSet()
        Log.d("BLOCKED_SENDERS", "רשימה שחורה נטענה: ${blockedSenders.size} שולחים")
    }
}