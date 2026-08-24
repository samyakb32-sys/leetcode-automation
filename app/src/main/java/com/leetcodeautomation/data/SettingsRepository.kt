package com.leetcodeautomation.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "settings")

data class Settings(
    val leetcodeSession: String = "",
    val csrfToken: String = "",
    val nvidiaApiKey: String = "",
    val aiModel: String = "meta/llama-3.3-70b-instruct",
    val maxFixAttempts: Int = 5,
    // Streak automation: run automatically at solveHour:solveMinute, every repeatEveryDays
    // day(s), solving problemsPerRun problems (today's daily challenge first, then backupSlugs).
    val streakEnabled: Boolean = false,
    val solveHour: Int = 8,
    val solveMinute: Int = 0,
    val problemsPerRun: Int = 1,
    val repeatEveryDays: Int = 1,
    val backupSlugs: String = "",
)

/** Persists LeetCode/NVIDIA credentials and streak-automation settings on-device. */
class SettingsRepository(private val context: Context) {
    private object Keys {
        val SESSION = stringPreferencesKey("leetcode_session")
        val CSRF = stringPreferencesKey("csrf_token")
        val NVIDIA_KEY = stringPreferencesKey("nvidia_api_key")
        val MODEL = stringPreferencesKey("ai_model")
        val MAX_ATTEMPTS = intPreferencesKey("max_fix_attempts")
        val STREAK_ENABLED = booleanPreferencesKey("streak_enabled")
        val SOLVE_HOUR = intPreferencesKey("solve_hour")
        val SOLVE_MINUTE = intPreferencesKey("solve_minute")
        val PROBLEMS_PER_RUN = intPreferencesKey("problems_per_run")
        val REPEAT_EVERY_DAYS = intPreferencesKey("repeat_every_days")
        val BACKUP_SLUGS = stringPreferencesKey("backup_slugs")
    }

    suspend fun load(): Settings {
        val prefs = context.dataStore.data.first()
        return Settings(
            leetcodeSession = prefs[Keys.SESSION].orEmpty(),
            csrfToken = prefs[Keys.CSRF].orEmpty(),
            nvidiaApiKey = prefs[Keys.NVIDIA_KEY].orEmpty(),
            aiModel = prefs[Keys.MODEL] ?: "meta/llama-3.3-70b-instruct",
            maxFixAttempts = prefs[Keys.MAX_ATTEMPTS] ?: 5,
            streakEnabled = prefs[Keys.STREAK_ENABLED] ?: false,
            solveHour = prefs[Keys.SOLVE_HOUR] ?: 8,
            solveMinute = prefs[Keys.SOLVE_MINUTE] ?: 0,
            problemsPerRun = prefs[Keys.PROBLEMS_PER_RUN] ?: 1,
            repeatEveryDays = prefs[Keys.REPEAT_EVERY_DAYS] ?: 1,
            backupSlugs = prefs[Keys.BACKUP_SLUGS].orEmpty(),
        )
    }

    suspend fun save(settings: Settings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SESSION] = settings.leetcodeSession
            prefs[Keys.CSRF] = settings.csrfToken
            prefs[Keys.NVIDIA_KEY] = settings.nvidiaApiKey
            prefs[Keys.MODEL] = settings.aiModel
            prefs[Keys.MAX_ATTEMPTS] = settings.maxFixAttempts
            prefs[Keys.STREAK_ENABLED] = settings.streakEnabled
            prefs[Keys.SOLVE_HOUR] = settings.solveHour
            prefs[Keys.SOLVE_MINUTE] = settings.solveMinute
            prefs[Keys.PROBLEMS_PER_RUN] = settings.problemsPerRun
            prefs[Keys.REPEAT_EVERY_DAYS] = settings.repeatEveryDays
            prefs[Keys.BACKUP_SLUGS] = settings.backupSlugs
        }
    }
}
