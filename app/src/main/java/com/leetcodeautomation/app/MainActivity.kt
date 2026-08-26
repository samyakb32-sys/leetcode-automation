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
import com.leetcodeautomation.ui.LeetCodeLoginScreen
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
                var showLogin by remember { mutableStateOf(false) }
                val state by viewModel.uiState.collectAsState()
                val context = LocalContext.current

                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { /* streak automation still runs; the user just won't see result notifications */ }
                val storagePermissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { /* saving a proof image just won't work until granted */ }

                LaunchedEffect(Unit) {
                    StreakNotifier.ensureChannel(context)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    // Needed to save proof images pre-scoped-storage; Q+ doesn't require it.
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                }

                if (showLogin) {
                    LeetCodeLoginScreen(
                        onCaptured = { session, csrf ->
                            viewModel.saveSettings(state.settings.copy(leetcodeSession = session, csrfToken = csrf))
                            showLogin = false
                        },
                        onClose = { showLogin = false },
                    )
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.weight(1f, fill = true)) {
                            when (tab) {
                                AppTab.SOLVE -> SolverScreen(viewModel = viewModel, onOpenSettings = { tab = AppTab.SETTINGS })
                                AppTab.HISTORY -> HistoryScreen()
                                AppTab.STATS -> StatsScreen()
                                AppTab.SETTINGS -> SettingsScreen(
                                    initial = state.settings,
                                    onSave = viewModel::saveSettings,
                                    onLaunchLogin = { showLogin = true },
                                )
                            }
                        }
                        BottomNav(selected = tab, onSelect = { tab = it })
                    }
                }
            }
        }
    }
}
