package com.leetcodeautomation.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "settings")

data class Settings(
    val leetcodeSession: String = "",
    val csrfToken: String = "",
    val nvidiaApiKey: String = "",
    val aiModel: String = "nvidia/llama-3.1-nemotron-70b-instruct",
    val maxFixAttempts: Int = 5,
)

/** Persists LeetCode/NVIDIA credentials on-device via DataStore. */
class SettingsRepository(private val context: Context) {
    private object Keys {
        val SESSION = stringPreferencesKey("leetcode_session")
        val CSRF = stringPreferencesKey("csrf_token")
        val NVIDIA_KEY = stringPreferencesKey("nvidia_api_key")
        val MODEL = stringPreferencesKey("ai_model")
        val MAX_ATTEMPTS = stringPreferencesKey("max_fix_attempts")
    }

    suspend fun load(): Settings {
        val prefs = context.dataStore.data.first()
        return Settings(
            leetcodeSession = prefs[Keys.SESSION].orEmpty(),
            csrfToken = prefs[Keys.CSRF].orEmpty(),
            nvidiaApiKey = prefs[Keys.NVIDIA_KEY].orEmpty(),
            aiModel = prefs[Keys.MODEL] ?: "nvidia/llama-3.1-nemotron-70b-instruct",
            maxFixAttempts = prefs[Keys.MAX_ATTEMPTS]?.toIntOrNull() ?: 5,
        )
    }

    suspend fun save(settings: Settings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SESSION] = settings.leetcodeSession
            prefs[Keys.CSRF] = settings.csrfToken
            prefs[Keys.NVIDIA_KEY] = settings.nvidiaApiKey
            prefs[Keys.MODEL] = settings.aiModel
            prefs[Keys.MAX_ATTEMPTS] = settings.maxFixAttempts.toString()
        }
    }
}
