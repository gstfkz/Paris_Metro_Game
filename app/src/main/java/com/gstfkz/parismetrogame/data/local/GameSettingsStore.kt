package com.gstfkz.parismetrogame.data.local

import android.content.Context

enum class GameLanguage { ENGLISH, FRENCH }

data class GamePreferences(
    val language: GameLanguage = GameLanguage.ENGLISH,
    val walkEnabled: Boolean = true,
    val rerEnabled: Boolean = true
)

class GameSettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("game_settings", Context.MODE_PRIVATE)
    fun get() = GamePreferences(
        language = if (prefs.getString("language", "en") == "fr") GameLanguage.FRENCH else GameLanguage.ENGLISH,
        walkEnabled = prefs.getBoolean("walk_enabled", true),
        rerEnabled = prefs.getBoolean("rer_enabled", true)
    )
    fun setLanguage(value: GameLanguage) = prefs.edit().putString("language", if (value == GameLanguage.FRENCH) "fr" else "en").apply()
    fun setWalkEnabled(value: Boolean) = prefs.edit().putBoolean("walk_enabled", value).apply()
    fun setRerEnabled(value: Boolean) = prefs.edit().putBoolean("rer_enabled", value).apply()
}
