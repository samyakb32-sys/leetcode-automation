package com.leetcodeautomation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.leetcodeautomation.data.Settings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(initial: Settings, onBack: () -> Unit, onSave: (Settings) -> Unit) {
    var session by remember { mutableStateOf(initial.leetcodeSession) }
    var csrf by remember { mutableStateOf(initial.csrfToken) }
    var apiKey by remember { mutableStateOf(initial.nvidiaApiKey) }
    var model by remember { mutableStateOf(initial.aiModel) }
    var maxAttempts by remember { mutableStateOf(initial.maxFixAttempts.toString()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
        ) {
            OutlinedTextField(
                value = session,
                onValueChange = { session = it },
                label = { Text("LeetCode session cookie") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = csrf,
                onValueChange = { csrf = it },
                label = { Text("LeetCode CSRF token") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text("NVIDIA API key") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
            OutlinedTextField(
                value = model,
                onValueChange = { model = it },
                label = { Text("Model") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
            OutlinedTextField(
                value = maxAttempts,
                onValueChange = { maxAttempts = it.filter(Char::isDigit) },
                label = { Text("Max fix attempts") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
            Button(
                onClick = {
                    onSave(
                        Settings(
                            leetcodeSession = session,
                            csrfToken = csrf,
                            nvidiaApiKey = apiKey,
                            aiModel = model,
                            maxFixAttempts = maxAttempts.toIntOrNull() ?: 5,
                        )
                    )
                    onBack()
                },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            ) {
                Text("Save")
            }
        }
    }
}
