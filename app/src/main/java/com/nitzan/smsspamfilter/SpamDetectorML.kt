package com.nitzan.smsspamfilter

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SpamDetectorML(private val context: Context) {

    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences("ml_training", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val blockedSendersManager = BlockedSendersManager(context)

    private var spamSamples = mutableListOf<String>()
    private var legitimateSamples = mutableListOf<String>()

    init {
        loadTrainingData()

        // דוגמאות ספאם ראשוניות אם אין נתונים
        if (spamSamples.isEmpty()) {
            spamSamples.addAll(listOf(
                "טורינר כניסה חינם",
                "בונוס מהרו",
                "הרוויח באתר",
                "שלם מיידית",
                "החזרי מס"
            ))
        }

        if (legitimateSamples.isEmpty()) {
            legitimateSamples.addAll(listOf(
                "היי איך אתה",
                "נפגש מחר",
                "תודה על העזרה"
            ))
        }
    }

    fun detectSpam(message: String, sender: String): Boolean {
        Log.d("ML_SPAM", "=== בודק הודעה: '$message' ===")

        // בדיקה ראשונה - אם השולח חסום
        if (blockedSendersManager.isBlocked(sender)) {
            Log.d("ML_SPAM", "🚫 שולח חסום זוהה: $sender")
            return true
        }

        val cleanMessage = message.lowercase().trim()

        // בדוק התאמה מדויקת לדוגמאות שנלמדו
        for (spamSample in spamSamples) {
            val cleanSpam = spamSample.lowercase().trim()

            if (similarity(cleanMessage, cleanSpam) > 0.8) {
                Log.d("ML_SPAM", "🔴 נמצא דמיון גבוה לספאם: '$cleanSpam' (${similarity(cleanMessage, cleanSpam)})")
                return true
            }
        }

        // שלב 2: זיהוי דפוסי ספאם מההודעות שנלמדו
        val spamScore = calculateLearnedSpamScore(cleanMessage)
        if (spamScore > 0.6) {
            Log.d("ML_SPAM", "🟠 דפוס ספאם נלמד זוהה: ניקוד $spamScore")
            return true
        }

        for (legitSample in legitimateSamples) {
            val cleanLegit = legitSample.lowercase().trim()

            if (similarity(cleanMessage, cleanLegit) > 0.85) {
                Log.d("ML_SPAM", "🟢 נמצא דמיון גבוה לרגיל: '$cleanLegit' (${similarity(cleanMessage, cleanLegit)})")
                return false
            }
        }

        // אם לא נמצא דמיון - השתמש בפיצ'רים
        val features = extractFeatures(message, sender)
        val score = calculateSpamScore(features)

        Log.d("ML_SPAM", "🔵 ניקוד פיצ'רים: $score (רף: 0.5)")
        Log.d("ML_SPAM", "דוגמאות ספאם שנלמדו: ${spamSamples.size}, דוגמאות רגילות: ${legitimateSamples.size}")

        return score > 0.5
    }

    // פונקציה לזיהוי דפוסים מהודעות ספאם שנלמדו
    private fun calculateLearnedSpamScore(message: String): Double {
        if (spamSamples.isEmpty()) return 0.0

        val messageWords = message.split("\\s+".toRegex()).filter { it.length > 2 }

        // חשב כמה מילים מופיעות בהודעות ספאם שנלמדו
        var spamWordMatches = 0
        val spamWordFreq = mutableMapOf<String, Int>()

        // בנה מילון של מילים מהודעות ספאם
        spamSamples.forEach { spam ->
            spam.lowercase().split("\\s+".toRegex()).filter { it.length > 2 }.forEach { word ->
                spamWordFreq[word] = (spamWordFreq[word] ?: 0) + 1
            }
        }

        // בדוק כמה מהמילים בהודעה החדשה מופיעות בספאם
        messageWords.forEach { word ->
            if (spamWordFreq.containsKey(word.lowercase())) {
                spamWordMatches++
            }
        }

        val score = if (messageWords.isNotEmpty()) {
            spamWordMatches.toDouble() / messageWords.size
        } else 0.0

        Log.d("ML_SPAM", "דפוס ספאם נלמד: $spamWordMatches מתוך ${messageWords.size} מילים = $score")

        return score
    }

    // פונקציה לחישוב דמיון בין שתי הודעות
    private fun similarity(message1: String, message2: String): Double {
        val words1 = message1.split("\\s+".toRegex()).filter { it.length > 2 }.toSet()
        val words2 = message2.split("\\s+".toRegex()).filter { it.length > 2 }.toSet()

        if (words1.isEmpty() || words2.isEmpty()) return 0.0

        val intersection = words1.intersect(words2)
        val union = words1.union(words2)

        return intersection.size.toDouble() / union.size.toDouble()
    }

    private fun extractFeatures(message: String, sender: String): Map<String, Double> {
        return mapOf(
            "hasNumbers" to if (message.contains(Regex("\\d+"))) 1.0 else 0.0,
            "hasExclamation" to minOf(message.count { it == '!' } / 3.0, 1.0),
            "hasMoneyWords" to if (listOf("שח", "דולר", "מיליון", "אלף", "₪", "בונוס", "הרוויח").any {
                    message.contains(it, ignoreCase = true) }) 1.0 else 0.0,
            "hasUrgentWords" to if (listOf("דחוף", "מהר", "עכשיו", "מיידית", "מוגבל").any {
                    message.contains(it, ignoreCase = true) }) 1.0 else 0.0,
            "hasGamblingWords" to if (listOf("טורינר", "פוקר", "הימור", "קזינו", "רולטה").any {
                    message.contains(it, ignoreCase = true) }) 1.0 else 0.0,
            "hasEmojis" to if (message.contains(Regex("[\\p{So}\\p{Sk}]"))) 1.0 else 0.0,
            "isUnknownSender" to if (sender.matches(Regex("\\d+"))) 1.0 else 0.0,
            "messageLength" to if (message.length > 50) 1.0 else 0.0,
            "hasLinks" to if (message.contains("http") || message.contains("wa.link") ||
                message.contains("לחץ")) 1.0 else 0.0,
            "hasTimeLimit" to if (listOf("עוד", "שעתיים", "דקות", "לשעה הקרובה").any {
                    message.contains(it, ignoreCase = true) }) 1.0 else 0.0
        )
    }

    private fun calculateSpamScore(features: Map<String, Double>): Double {
        val weights = mapOf(
            "hasNumbers" to 0.05,
            "hasExclamation" to 0.1,
            "hasMoneyWords" to 0.25,
            "hasUrgentWords" to 0.15,
            "hasGamblingWords" to 0.4,
            "hasEmojis" to 0.05,
            "isUnknownSender" to 0.2,
            "messageLength" to 0.05,
            "hasLinks" to 0.2,
            "hasTimeLimit" to 0.25
        )

        val score = minOf(features.map { (feature, value) ->
            (weights[feature] ?: 0.0) * value
        }.sum(), 1.0)

        Log.d("ML_SPAM", "פיצ'רים: $features")
        Log.d("ML_SPAM", "ניקוד פיצ'רים: $score")

        return score
    }

    fun learnFromUser(message: String, sender: String, isSpam: Boolean) {
        val cleanMessage = message.trim()

        // הסר מכל הרשימות קודם
        spamSamples.removeAll { it.trim().equals(cleanMessage, ignoreCase = true) }
        legitimateSamples.removeAll { it.trim().equals(cleanMessage, ignoreCase = true) }

        // הוסף לרשימה הנכונה
        if (isSpam) {
            spamSamples.add(cleanMessage)
            Log.d("ML_SPAM", "✅ נוסף לספאם: '$cleanMessage'")
        } else {
            legitimateSamples.add(cleanMessage)
            Log.d("ML_SPAM", "✅ נוסף לרגיל: '$cleanMessage'")
        }

        // שמור מיד
        saveTrainingData()

        Log.d("ML_SPAM", "📊 סה\"כ: ${spamSamples.size} ספאם, ${legitimateSamples.size} רגיל")
    }

    private fun saveTrainingData() {
        val editor = sharedPrefs.edit()
        editor.putString("spam_samples", gson.toJson(spamSamples))
        editor.putString("legit_samples", gson.toJson(legitimateSamples))
        editor.apply()
        Log.d("ML_SPAM", "💾 נתונים נשמרו")
    }

    private fun loadTrainingData() {
        val spamJson = sharedPrefs.getString("spam_samples", "[]") ?: "[]"
        val legitJson = sharedPrefs.getString("legit_samples", "[]") ?: "[]"

        val type = object : TypeToken<MutableList<String>>() {}.type
        spamSamples = gson.fromJson(spamJson, type) ?: mutableListOf()
        legitimateSamples = gson.fromJson(legitJson, type) ?: mutableListOf()

        Log.d("ML_SPAM", "📂 נטענו: ${spamSamples.size} ספאם, ${legitimateSamples.size} רגיל")
    }
}