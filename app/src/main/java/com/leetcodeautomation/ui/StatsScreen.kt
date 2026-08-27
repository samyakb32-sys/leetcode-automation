package com.leetcodeautomation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leetcodeautomation.data.RunHistoryEntry
import com.leetcodeautomation.data.RunHistoryStore

/** Counters over the persisted run history (Solve tab + background streak runs). */
@Composable
fun StatsScreen() {
    val context = LocalContext.current
    var entries by remember { mutableStateOf<List<RunHistoryEntry>>(emptyList()) }

    LaunchedEffect(Unit) {
        entries = RunHistoryStore(context).load()
    }

    val accepted = entries.count { it.accepted }
    val total = entries.size
    val currentStreak = run {
        val acceptedDays = entries.filter { it.accepted }
            .map { java.time.Instant.ofEpochMilli(it.timestampMillis).atZone(java.time.ZoneId.systemDefault()).toLocalDate() }
            .toSortedSet()
        // A streak stays alive until a whole day is missed, so if today isn't solved *yet* we
        // count back from yesterday — otherwise every streak would read 0 until the day's solve.
        val today = java.time.LocalDate.now()
        var day = if (acceptedDays.contains(today)) today else today.minusDays(1)
        var streak = 0
        while (acceptedDays.contains(day)) {
            streak++
            day = day.minusDays(1)
        }
        streak
    }

    Scaffold(containerColor = CanvasBlack, topBar = { AppTopBar(onSettingsClick = {}) }) { padding ->
        Column(modifier = Modifier.fillMaxWidth().padding(padding).padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("STREAK (DAYS)", currentStreak.toString(), Modifier.weight(1f))
                StatCard("ACCEPTED", accepted.toString(), Modifier.weight(1f))
                StatCard("ATTEMPTS", total.toString(), Modifier.weight(1f))
            }
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
        Text(value, color = NvidiaGreenBright, fontFamily = JetBrainsMono, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Text(label, color = OnSurfaceVariant, fontFamily = JetBrainsMono, fontSize = 11.sp)
    }
}
