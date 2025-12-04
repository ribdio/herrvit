package se.techlisbon.mrwhite

import android.content.Context
import androidx.core.content.edit

object PrefsManager {
    private const val PREFS_NAME = "MrWhitePrefs"
    private const val KEY_WORD_URL = "word_url"
    private const val KEY_PLAYERS = "players"

    fun saveWordUrl(context: Context, url: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit {
                    putString(KEY_WORD_URL, url)
                }
    }

    fun getWordUrl(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_WORD_URL, "") ?: ""
    }

    fun savePlayers(context: Context, players: List<String>) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit {
                    putString(KEY_PLAYERS, players.joinToString(","))
                }
    }

    fun getPlayers(context: Context): List<String> {
        val stored = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_PLAYERS, "") ?: ""
        return if (stored.isBlank()) emptyList() else stored.split(",")
    }
}