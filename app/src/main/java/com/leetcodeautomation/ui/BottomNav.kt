package com.leetcodeautomation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.RowScope

enum class AppTab(val label: String, val icon: ImageVector) {
    SOLVE("Solve", Icons.Default.PlayCircleFilled),
    HISTORY("History", Icons.Default.History),
    STATS("Stats", Icons.Default.BarChart),
    SETTINGS("Settings", Icons.Default.Tune),
}

@Composable
fun BottomNav(selected: AppTab, onSelect: (AppTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceContainerLow)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        AppTab.entries.forEach { tab -> NavItem(tab, tab == selected, onSelect) }
    }
}

@Composable
private fun RowScope.NavItem(tab: AppTab, active: Boolean, onSelect: (AppTab) -> Unit) {
    val bg = if (active) NvidiaGreenBright.copy(alpha = 0.15f) else androidx.compose.ui.graphics.Color.Transparent
    val tint = if (active) NvidiaGreenBright else OnSurfaceVariant
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onSelect(tab) }
            .background(bg, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 6.dp),
    ) {
        Icon(tab.icon, contentDescription = tab.label, tint = tint)
        Text(
            tab.label,
            color = tint,
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMono),
        )
    }
}
