package com.leetcodeautomation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leetcodeautomation.data.PipelineStep

/** Simple in-session counters. Persisted long-term stats are a future addition. */
@Composable
fun StatsScreen(steps: List<PipelineStep>) {
    val accepted = steps.count { it.result.accepted }
    val total = steps.size

    Scaffold(containerColor = CanvasBlack, topBar = { AppTopBar(onSettingsClick = {}) }) { padding ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatCard("ACCEPTED", accepted.toString(), Modifier.weight(1f))
            StatCard("ATTEMPTS", total.toString(), Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .border(1.dp, OutlineVariant, RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Text(value, color = NvidiaGreenBright, fontFamily = JetBrainsMono, fontWeight = FontWeight.Bold, fontSize = 28.sp)
        Text(label, color = OnSurfaceVariant, fontFamily = JetBrainsMono, fontSize = 12.sp)
    }
}
