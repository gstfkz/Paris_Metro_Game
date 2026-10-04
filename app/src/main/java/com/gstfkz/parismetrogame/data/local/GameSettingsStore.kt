package com.gstfkz.parismetrogame.data.local

import android.content.Context

enum class GameLanguage { ENGLISH, FRENCH }

data class GamePreferences(
    val language: GameLanguage = GameLanguage.ENGLISH,
    val walkEnabled: Boolean = true,
    val rerEnabled: Boolean = true,
    val countdownEnabled: Boolean = false,
    val countdownMinutes: Int = 1,
    val countdownSeconds: Int = 0
) {
    val countdownTotalSeconds: Int get() = (countdownMinutes * 60 + countdownSeconds).coerceAtLeast(1)
}

class GameSettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("game_settings", Context.MODE_PRIVATE)
    fun get() = GamePreferences(
        language = if (prefs.getString("language", "en") == "fr") GameLanguage.FRENCH else GameLanguage.ENGLISH,
        walkEnabled = prefs.getBoolean("walk_enabled", true),
        rerEnabled = prefs.getBoolean("rer_enabled", true),
        countdownEnabled = prefs.getBoolean("countdown_enabled", false),
        countdownMinutes = prefs.getInt("countdown_minutes", 1).coerceIn(0, 99),
        countdownSeconds = prefs.getInt("countdown_seconds", 0).coerceIn(0, 59)
    )
    fun setLanguage(value: GameLanguage) = prefs.edit().putString("language", if (value == GameLanguage.FRENCH) "fr" else "en").apply()
    fun setWalkEnabled(value: Boolean) = prefs.edit().putBoolean("walk_enabled", value).apply()
    fun setRerEnabled(value: Boolean) = prefs.edit().putBoolean("rer_enabled", value).apply()
    fun setCountdownEnabled(value: Boolean) = prefs.edit().putBoolean("countdown_enabled", value).apply()
    fun setCountdown(minutes: Int, seconds: Int) = prefs.edit()
        .putInt("countdown_minutes", minutes.coerceIn(0, 99))
        .putInt("countdown_seconds", seconds.coerceIn(0, 59))
        .apply()
}
