package com.leetcodeautomation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leetcodeautomation.data.PipelineStep

/** Attempts from the current app session. Persisted history across restarts is a future addition. */
@Composable
fun HistoryScreen(steps: List<PipelineStep>) {
    Scaffold(containerColor = CanvasBlack, topBar = { AppTopBar(onSettingsClick = {}) }) { padding ->
        if (steps.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            ) {
                Text("No attempts yet this session.", color = OnSurfaceVariant, fontFamily = JetBrainsMono, fontSize = 13.sp)
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(steps.reversed()) { step -> HistoryRow(step) }
        }
    }
}

@Composable
private fun HistoryRow(step: PipelineStep) {
    val accepted = step.result.accepted
    val color = if (accepted) AcceptedGreen else ErrorRed
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .border(1.dp, OutlineVariant, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(if (accepted) Icons.Default.CheckCircle else Icons.Default.Error, contentDescription = null, tint = color)
            Text(
                step.result.statusMsg,
                color = color,
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        Text("attempt ${step.attempt}", color = OnSurfaceVariant, fontFamily = JetBrainsMono, fontSize = 12.sp)
    }
}
