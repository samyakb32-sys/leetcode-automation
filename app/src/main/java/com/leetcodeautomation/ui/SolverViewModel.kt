package com.leetcodeautomation.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.leetcodeautomation.data.LeetCodeClient
import com.leetcodeautomation.data.Pipeline
import com.leetcodeautomation.data.PipelineStep
import com.leetcodeautomation.data.ProblemPicker
import com.leetcodeautomation.data.RunHistoryEntry
import com.leetcodeautomation.data.RunHistoryStore
import com.leetcodeautomation.data.Settings
import com.leetcodeautomation.data.SettingsRepository
import com.leetcodeautomation.data.SolveLanguage
import com.leetcodeautomation.data.StreakScheduler
import com.leetcodeautomation.data.hasAiCredential
import com.leetcodeautomation.data.toSolver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class Stage { IDLE, PICKING, EXTRACTING, SOLVING, SUBMITTING, DONE, ERROR }

data class SolverUiState(
    val titleSlug: String = "",
    val stage: Stage = Stage.IDLE,
    val steps: List<PipelineStep> = emptyList(),
    val accepted: Boolean = false,
    val errorMessage: String? = null,
    // Which stage was in flight when a run failed, so the progress list can show where it
    // actually stopped instead of claiming every earlier stage completed.
    val failedStage: Stage? = null,
    val settings: Settings = Settings(),
    val settingsSavedTick: Int = 0,
)

class SolverViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsRepo = SettingsRepository(application)
    private val historyStore = RunHistoryStore(application)

    private val _uiState = MutableStateFlow(SolverUiState())
    val uiState: StateFlow<SolverUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(settings = settingsRepo.load())
        }
    }

    fun saveSettings(settings: Settings) {
        viewModelScope.launch {
            settingsRepo.save(settings)
            _uiState.value = _uiState.value.copy(
                settings = settings,
                settingsSavedTick = _uiState.value.settingsSavedTick + 1,
            )
            StreakScheduler.schedule(getApplication(), settings)
        }
    }

    /**
     * Solves today's Daily Challenge specifically — refuses if it's already been Accepted, or if
     * it isn't Easy difficulty (this app is built to reliably solve Easy problems only).
     */
    fun solveDaily() = solve { leetcode, alreadySolved ->
        val daily = ProblemPicker.pickDaily(leetcode)
            ?: throw IllegalStateException("Couldn't reach LeetCode's Daily Challenge right now.")
        if (daily in alreadySolved) {
            throw IllegalStateException(
                "You've already solved today's Daily Challenge — check History, or try Practice Another."
            )
        }
        when (val difficulty = leetcode.fetchDifficulty(daily)) {
            "Easy" -> Unit
            // null means the lookup itself failed (network/rate limit) — don't claim it's too hard.
            null -> throw IllegalStateException(
                "Couldn't check today's Daily Challenge difficulty — check your connection and try again."
            )
            else -> throw IllegalStateException(
                "Today's Daily Challenge is $difficulty — this app only solves Easy problems. Try Practice Another instead."
            )
        }
        daily
    }

    /** Solves the next unsolved Easy problem from the backup slugs configured in Settings — never the Daily Challenge. */
    fun solvePractice() = solve { leetcode, alreadySolved ->
        ProblemPicker.pickFromBackups(leetcode, _uiState.value.settings.backupSlugs, alreadySolved)
            ?: throw IllegalStateException(
                "No unsolved Easy backup problems left — add more comma-separated Easy LeetCode slugs in Settings."
            )
    }

    private fun solve(pickSlug: suspend (LeetCodeClient, Set<String>) -> String) {
        val state = _uiState.value
        val s = state.settings
        if (s.leetcodeSession.isBlank() || s.csrfToken.isBlank() || !s.hasAiCredential) {
            _uiState.value = state.copy(errorMessage = "Set your LeetCode + AI credentials in Settings first.")
            return
        }

        _uiState.value = state.copy(
            stage = Stage.PICKING,
            titleSlug = "",
            steps = emptyList(),
            accepted = false,
            errorMessage = null,
            failedStage = null,
        )

        viewModelScope.launch {
            try {
                val leetcode = LeetCodeClient(s.leetcodeSession, s.csrfToken)
                val solver = s.toSolver()
                val pipeline = Pipeline(leetcode, solver)

                val alreadySolved = historyStore.load().filter { it.accepted }.map { it.titleSlug }.toSet()
                val slug = pickSlug(leetcode, alreadySolved)
                _uiState.value = _uiState.value.copy(stage = Stage.EXTRACTING, titleSlug = slug)

                val language = SolveLanguage.fromSlug(s.submissionLanguage)
                // Pipeline.run fetches the problem first, then asks the AI — advance to SOLVING
                // only once fetching is actually done, so a failed fetch isn't blamed on the AI.
                val lastStep = pipeline.run(
                    titleSlug = slug,
                    maxFixAttempts = s.maxFixAttempts,
                    language = language,
                    onFetched = { _uiState.value = _uiState.value.copy(stage = Stage.SOLVING) },
                ) { step ->
                    _uiState.value = _uiState.value.copy(
                        stage = Stage.SUBMITTING,
                        steps = _uiState.value.steps + step,
                    )
                }

                _uiState.value = _uiState.value.copy(
                    stage = Stage.DONE,
                    accepted = lastStep.result.accepted,
                )
                historyStore.append(
                    RunHistoryEntry(
                        titleSlug = slug,
                        accepted = lastStep.result.accepted,
                        statusMsg = lastStep.result.statusMsg,
                        attempts = lastStep.attempt,
                        timestampMillis = System.currentTimeMillis(),
                        fromBackground = false,
                    )
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    stage = Stage.ERROR,
                    failedStage = _uiState.value.stage,
                    errorMessage = e.message ?: "Unknown error",
                )
            }
        }
    }
}
