package com.leetcodeautomation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.leetcodeautomation.data.PipelineStep

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolverScreen(viewModel: SolverViewModel, onOpenSettings: () -> Unit) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LeetCode Automation") },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
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
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                OutlinedTextField(
                    value = state.titleSlug,
                    onValueChange = viewModel::updateSlug,
                    label = { Text("Problem slug, e.g. two-sum") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                androidx.compose.foundation.layout.Spacer(Modifier.padding(4.dp))
                Button(onClick = viewModel::solve, enabled = state.stage != Stage.EXTRACTING && state.stage != Stage.SOLVING && state.stage != Stage.SUBMITTING) {
                    Text("Solve")
                }
            }

            androidx.compose.foundation.layout.Spacer(Modifier.padding(8.dp))

            PipelineTracker(state.stage)

            androidx.compose.foundation.layout.Spacer(Modifier.padding(8.dp))

            state.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            if (state.stage == Stage.DONE) {
                Text(
                    if (state.accepted) "✅ Accepted" else "❌ Not accepted after max attempts",
                    color = if (state.accepted) AcceptedGreen else ErrorRed,
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 12.dp),
            ) {
                items(state.steps) { step -> AttemptCard(step) }
            }
        }
    }
}

@Composable
private fun PipelineTracker(stage: Stage) {
    val steps = listOf("Extract", "Solve", "Save", "Submit", "Judge")
    val activeIndex = when (stage) {
        Stage.IDLE -> -1
        Stage.EXTRACTING -> 0
        Stage.SOLVING -> 1
        Stage.SUBMITTING -> 3
        Stage.DONE, Stage.ERROR -> 4
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        steps.forEachIndexed { i, label ->
            val active = i <= activeIndex
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (active) NvidiaGreen.copy(alpha = 0.25f) else SurfaceDark,
                ),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(
                    label,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
        if (stage == Stage.EXTRACTING || stage == Stage.SOLVING || stage == Stage.SUBMITTING) {
            CircularProgressIndicator(modifier = Modifier.padding(start = 4.dp))
        }
    }
}

@Composable
private fun AttemptCard(step: PipelineStep) {
    val verdictColor = if (step.result.accepted) AcceptedGreen else ErrorRed
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Attempt ${step.attempt}", style = MaterialTheme.typography.titleSmall)
                Text(step.result.statusMsg, color = verdictColor, style = MaterialTheme.typography.labelLarge)
            }
            androidx.compose.foundation.layout.Spacer(Modifier.padding(4.dp))
            Text(
                step.solution.code.lines().take(6).joinToString("\n"),
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
            )
        }
    }
}
