package com.leetcodeautomation.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.leetcodeautomation.data.LeetCodeClient
import com.leetcodeautomation.data.NvidiaSolver
import com.leetcodeautomation.data.Pipeline
import com.leetcodeautomation.data.PipelineStep
import com.leetcodeautomation.data.Settings
import com.leetcodeautomation.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class Stage { IDLE, EXTRACTING, SOLVING, SUBMITTING, DONE, ERROR }

data class SolverUiState(
    val titleSlug: String = "",
    val stage: Stage = Stage.IDLE,
    val steps: List<PipelineStep> = emptyList(),
    val accepted: Boolean = false,
    val errorMessage: String? = null,
    val settings: Settings = Settings(),
)

class SolverViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsRepo = SettingsRepository(application)

    private val _uiState = MutableStateFlow(SolverUiState())
    val uiState: StateFlow<SolverUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(settings = settingsRepo.load())
        }
    }

    fun updateSlug(slug: String) {
        _uiState.value = _uiState.value.copy(titleSlug = slug)
    }

    fun saveSettings(settings: Settings) {
        viewModelScope.launch {
            settingsRepo.save(settings)
            _uiState.value = _uiState.value.copy(settings = settings)
        }
    }

    fun solve() {
        val state = _uiState.value
        val s = state.settings
        if (state.titleSlug.isBlank()) return
        if (s.leetcodeSession.isBlank() || s.csrfToken.isBlank() || s.nvidiaApiKey.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Set your LeetCode + NVIDIA credentials in Settings first.")
            return
        }

        _uiState.value = state.copy(
            stage = Stage.EXTRACTING,
            steps = emptyList(),
            accepted = false,
            errorMessage = null,
        )

        viewModelScope.launch {
            try {
                val leetcode = LeetCodeClient(s.leetcodeSession, s.csrfToken)
                val solver = NvidiaSolver(s.nvidiaApiKey, s.aiModel)
                val pipeline = Pipeline(leetcode, solver)

                _uiState.value = _uiState.value.copy(stage = Stage.SOLVING)

                val lastStep = pipeline.run(state.titleSlug, s.maxFixAttempts) { step ->
                    _uiState.value = _uiState.value.copy(
                        stage = Stage.SUBMITTING,
                        steps = _uiState.value.steps + step,
                    )
                }

                _uiState.value = _uiState.value.copy(
                    stage = Stage.DONE,
                    accepted = lastStep.result.accepted,
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
