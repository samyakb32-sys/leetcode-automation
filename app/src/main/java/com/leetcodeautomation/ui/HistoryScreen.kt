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
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leetcodeautomation.data.RunHistoryEntry
import com.leetcodeautomation.data.RunHistoryStore
import java.text.DateFormat
import java.util.Date

/** Every solve attempt — from the Solve tab or a background streak run — persisted on-device. */
@Composable
fun HistoryScreen() {
    val context = LocalContext.current
    var entries by remember { mutableStateOf<List<RunHistoryEntry>>(emptyList()) }

    LaunchedEffect(Unit) {
        entries = RunHistoryStore(context).load()
    }

    Scaffold(containerColor = CanvasBlack, topBar = { AppTopBar(onSettingsClick = {}) }) { padding ->
        if (entries.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            ) {
                Text("No attempts yet. Tap Solve or turn on Streak Automation.", color = OnSurfaceVariant, fontFamily = JetBrainsMono, fontSize = 13.sp)
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(entries.reversed()) { entry -> HistoryRow(entry) }
        }
    }
}

@Composable
private fun HistoryRow(entry: RunHistoryEntry) {
    val color = if (entry.accepted) AcceptedGreen else ErrorRed
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .border(1.dp, OutlineVariant, RoundedCornerShape(12.dp))
            .padding(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(if (entry.accepted) Icons.Default.CheckCircle else Icons.Default.Error, contentDescription = null, tint = color)
                Text(
                    entry.statusMsg,
                    color = color,
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(start = 8.dp),
                )
                if (entry.fromBackground) {
                    Icon(
                        Icons.Default.CloudSync,
                        contentDescription = "Background run",
                        tint = OnSurfaceVariant,
                        modifier = Modifier.padding(start = 6.dp),
                    )
                }
            }
            Text("attempt ${entry.attempts}", color = OnSurfaceVariant, fontFamily = JetBrainsMono, fontSize = 12.sp)
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(entry.titleSlug, color = OnSurface, fontFamily = JetBrainsMono, fontSize = 12.sp)
            Text(
                DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(entry.timestampMillis)),
                color = OnSurfaceVariant,
                fontSize = 11.sp,
            )
        }
    }
}
