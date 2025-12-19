package se.techlisbon.mrwhite

import android.content.Context
import androidx.core.content.edit

object PrefsManager {
    private const val PREFS_NAME = "MrWhitePrefs"
    private const val KEY_WORD_URL = "word_url"
    private const val KEY_PLAYERS = "players"
    private const val KEY_PLAYED_PAIRS = "played_pairs"
    private const val KEY_WORD_SOURCE_HASH = "word_source_hash"

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

    // Word pair playlist tracking
    fun savePlayedPairs(context: Context, indices: Set<Int>) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit {
                    putString(KEY_PLAYED_PAIRS, indices.joinToString(","))
                }
    }

    fun getPlayedPairs(context: Context): Set<Int> {
        val stored = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_PLAYED_PAIRS, "") ?: ""
        return if (stored.isBlank()) {
            emptySet()
        } else {
            stored.split(",").mapNotNull { it.toIntOrNull() }.toSet()
        }
    }

    fun saveWordSourceHash(context: Context, hash: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit {
                    putString(KEY_WORD_SOURCE_HASH, hash)
                }
    }

    fun getWordSourceHash(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_WORD_SOURCE_HASH, "") ?: ""
    }

    fun clearPlayedPairs(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit {
                    remove(KEY_PLAYED_PAIRS)
                }
    }
}