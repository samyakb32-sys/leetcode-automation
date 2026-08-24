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

    fun solve() {
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
        )

        viewModelScope.launch {
            try {
                val leetcode = LeetCodeClient(s.leetcodeSession, s.csrfToken)
                val solver = s.toSolver()
                val pipeline = Pipeline(leetcode, solver)

                val slug = ProblemPicker.pickOne(leetcode, s.backupSlugs)
                    ?: throw IllegalStateException(
                        "Couldn't find a problem to solve — LeetCode's Daily Challenge wasn't reachable and " +
                            "no backup problems are set in Settings."
                    )
                _uiState.value = _uiState.value.copy(stage = Stage.EXTRACTING, titleSlug = slug)

                _uiState.value = _uiState.value.copy(stage = Stage.SOLVING)

                val language = SolveLanguage.fromSlug(s.submissionLanguage)
                val lastStep = pipeline.run(slug, s.maxFixAttempts, language) { step ->
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
                    errorMessage = e.message ?: "Unknown error",
                )
            }
        }
    }
}
