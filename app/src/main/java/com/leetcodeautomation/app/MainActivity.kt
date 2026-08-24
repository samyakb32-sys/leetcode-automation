package com.leetcodeautomation.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.leetcodeautomation.data.StreakNotifier
import com.leetcodeautomation.ui.AppTab
import com.leetcodeautomation.ui.BottomNav
import com.leetcodeautomation.ui.HistoryScreen
import com.leetcodeautomation.ui.LeetCodeAutomationTheme
import com.leetcodeautomation.ui.SettingsScreen
import com.leetcodeautomation.ui.SolverScreen
import com.leetcodeautomation.ui.SolverViewModel
import com.leetcodeautomation.ui.StatsScreen

class MainActivity : ComponentActivity() {
    private val viewModel: SolverViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LeetCodeAutomationTheme {
                var tab by remember { mutableStateOf(AppTab.SOLVE) }
                val state by viewModel.uiState.collectAsState()
                val context = LocalContext.current

                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { /* streak automation still runs; the user just won't see result notifications */ }

                LaunchedEffect(Unit) {
                    StreakNotifier.ensureChannel(context)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f, fill = true)) {
                        when (tab) {
                            AppTab.SOLVE -> SolverScreen(viewModel = viewModel, onOpenSettings = { tab = AppTab.SETTINGS })
                            AppTab.HISTORY -> HistoryScreen(state.steps)
                            AppTab.STATS -> StatsScreen(state.steps)
                            AppTab.SETTINGS -> SettingsScreen(initial = state.settings, onSave = viewModel::saveSettings)
                        }
                    }
                    BottomNav(selected = tab, onSelect = { tab = it })
                }
            }
        }
    }
}
