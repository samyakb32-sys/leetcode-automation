package com.leetcodeautomation.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.leetcodeautomation.ui.LeetCodeAutomationTheme
import com.leetcodeautomation.ui.SettingsScreen
import com.leetcodeautomation.ui.SolverScreen
import com.leetcodeautomation.ui.SolverViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: SolverViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LeetCodeAutomationTheme {
                var showSettings by remember { mutableStateOf(false) }
                val state by viewModel.uiState.collectAsState()

                if (showSettings) {
                    SettingsScreen(
                        initial = state.settings,
                        onBack = { showSettings = false },
                        onSave = viewModel::saveSettings,
                    )
                } else {
                    SolverScreen(viewModel = viewModel, onOpenSettings = { showSettings = true })
                }
            }
        }
    }
}
