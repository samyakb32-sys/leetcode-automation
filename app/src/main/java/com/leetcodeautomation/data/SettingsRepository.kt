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
    // Which AI provider is active (see AiProvider). Defaults to NVIDIA's free endpoint.
    val aiProvider: String = AiProvider.NVIDIA.id,
    val openaiApiKey: String = "",
    val groqApiKey: String = "",
    val geminiApiKey: String = "",
    val anthropicApiKey: String = "",
    val openrouterApiKey: String = "",
    // Only used when aiProvider is "custom": any OpenAI-compatible chat completions endpoint.
    val customApiBaseUrl: String = "",
    val customApiKey: String = "",
    val maxFixAttempts: Int = 5,
    // Which language the AI writes solutions in and submits to LeetCode.
    val submissionLanguage: String = SolveLanguage.PYTHON3.langSlug,
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
        val AI_PROVIDER = stringPreferencesKey("ai_provider")
        val OPENAI_KEY = stringPreferencesKey("openai_api_key")
        val GROQ_KEY = stringPreferencesKey("groq_api_key")
        val GEMINI_KEY = stringPreferencesKey("gemini_api_key")
        val ANTHROPIC_KEY = stringPreferencesKey("anthropic_api_key")
        val OPENROUTER_KEY = stringPreferencesKey("openrouter_api_key")
        val CUSTOM_BASE_URL = stringPreferencesKey("custom_api_base_url")
        val CUSTOM_API_KEY = stringPreferencesKey("custom_api_key")
        val MAX_ATTEMPTS = intPreferencesKey("max_fix_attempts")
        val SUBMISSION_LANGUAGE = stringPreferencesKey("submission_language")
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
            aiProvider = prefs[Keys.AI_PROVIDER] ?: AiProvider.NVIDIA.id,
            openaiApiKey = prefs[Keys.OPENAI_KEY].orEmpty(),
            groqApiKey = prefs[Keys.GROQ_KEY].orEmpty(),
            geminiApiKey = prefs[Keys.GEMINI_KEY].orEmpty(),
            anthropicApiKey = prefs[Keys.ANTHROPIC_KEY].orEmpty(),
            openrouterApiKey = prefs[Keys.OPENROUTER_KEY].orEmpty(),
            customApiBaseUrl = prefs[Keys.CUSTOM_BASE_URL].orEmpty(),
            customApiKey = prefs[Keys.CUSTOM_API_KEY].orEmpty(),
            maxFixAttempts = prefs[Keys.MAX_ATTEMPTS] ?: 5,
            submissionLanguage = prefs[Keys.SUBMISSION_LANGUAGE] ?: SolveLanguage.PYTHON3.langSlug,
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
            prefs[Keys.AI_PROVIDER] = settings.aiProvider
            prefs[Keys.OPENAI_KEY] = settings.openaiApiKey
            prefs[Keys.GROQ_KEY] = settings.groqApiKey
            prefs[Keys.GEMINI_KEY] = settings.geminiApiKey
            prefs[Keys.ANTHROPIC_KEY] = settings.anthropicApiKey
            prefs[Keys.OPENROUTER_KEY] = settings.openrouterApiKey
            prefs[Keys.CUSTOM_BASE_URL] = settings.customApiBaseUrl
            prefs[Keys.CUSTOM_API_KEY] = settings.customApiKey
            prefs[Keys.MAX_ATTEMPTS] = settings.maxFixAttempts
            prefs[Keys.SUBMISSION_LANGUAGE] = settings.submissionLanguage
            prefs[Keys.STREAK_ENABLED] = settings.streakEnabled
            prefs[Keys.SOLVE_HOUR] = settings.solveHour
            prefs[Keys.SOLVE_MINUTE] = settings.solveMinute
            prefs[Keys.PROBLEMS_PER_RUN] = settings.problemsPerRun
            prefs[Keys.REPEAT_EVERY_DAYS] = settings.repeatEveryDays
            prefs[Keys.BACKUP_SLUGS] = settings.backupSlugs
        }
    }
}
