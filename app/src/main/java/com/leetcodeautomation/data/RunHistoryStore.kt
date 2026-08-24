package com.leetcodeautomation.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.historyDataStore by preferencesDataStore(name = "run_history")
private val HISTORY_KEY = stringPreferencesKey("entries")
private const val MAX_ENTRIES = 200

@Serializable
data class RunHistoryEntry(
    val titleSlug: String,
    val accepted: Boolean,
    val statusMsg: String,
    val attempts: Int,
    val timestampMillis: Long,
    val fromBackground: Boolean,
)

/**
 * Persists solved-problem history across app restarts and background streak runs, so
 * History/Stats show real data instead of only whatever happened in the current session.
 */
class RunHistoryStore(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun load(): List<RunHistoryEntry> {
        val raw = context.historyDataStore.data.first()[HISTORY_KEY] ?: return emptyList()
        return runCatching { json.decodeFromString<List<RunHistoryEntry>>(raw) }.getOrDefault(emptyList())
    }

    suspend fun append(entry: RunHistoryEntry) {
        context.historyDataStore.edit { prefs ->
            val current = prefs[HISTORY_KEY]?.let {
                runCatching { json.decodeFromString<List<RunHistoryEntry>>(it) }.getOrDefault(emptyList())
            } ?: emptyList()
            val updated = (current + entry).takeLast(MAX_ENTRIES)
            prefs[HISTORY_KEY] = json.encodeToString(updated)
        }
    }
}
