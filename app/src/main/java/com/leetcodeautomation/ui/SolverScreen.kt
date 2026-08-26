package com.leetcodeautomation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leetcodeautomation.data.PipelineStep
import com.leetcodeautomation.data.ProofImageGenerator
import com.leetcodeautomation.data.RunHistoryEntry
import com.leetcodeautomation.data.hasAiCredential
import kotlinx.coroutines.launch

@Composable
fun SolverScreen(viewModel: SolverViewModel, onOpenSettings: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val needsSetup = state.settings.leetcodeSession.isBlank() ||
        state.settings.csrfToken.isBlank() ||
        !state.settings.hasAiCredential

    Scaffold(containerColor = CanvasBlack, topBar = { AppTopBar(onSettingsClick = onOpenSettings) }) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            if (needsSetup) {
                item { SetupNeededBanner(onOpenSettings) }
            }
            item { SolveButtonCard(titleSlug = state.titleSlug, stage = state.stage, onExecute = viewModel::solve) }
            item {
                ActivePipelineCard(
                    stage = state.stage,
                    attempt = state.steps.size,
                    maxAttempts = state.settings.maxFixAttempts,
                )
            }
            item {
                state.errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontFamily = JetBrainsMono)
                }
                if (state.stage == Stage.DONE) {
                    Text(
                        if (state.accepted) "Done — it got accepted! 🎉" else "Done — it didn't pass. Check the attempts below.",
                        color = if (state.accepted) AcceptedGreen else ErrorRed,
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (state.accepted) {
                        val lastStep = state.steps.lastOrNull()
                        if (lastStep != null) {
                            Spacer(Modifier.height(12.dp))
                            ProofButton(state.titleSlug, lastStep)
                        }
                    }
                }
            }
            item {
                Text(
                    "RECENT TRIES",
                    color = OnSurfaceVariant,
                    fontFamily = JetBrainsMono,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
                )
            }
            items(state.steps.reversed()) { step -> AttemptCard(step) }
        }
    }
}

@Composable
private fun ProofButton(titleSlug: String, lastStep: PipelineStep) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(NvidiaGreenBright.copy(alpha = 0.15f))
            .border(1.dp, NvidiaGreenBright.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    scope.launch {
                        val entry = RunHistoryEntry(
                            titleSlug = titleSlug,
                            accepted = lastStep.result.accepted,
                            statusMsg = lastStep.result.statusMsg,
                            attempts = lastStep.attempt,
                            timestampMillis = System.currentTimeMillis(),
                            fromBackground = false,
                        )
                        val uri = ProofImageGenerator.saveToGallery(context, entry)
                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "image/png"
                            putExtra(android.content.Intent.EXTRA_STREAM, uri)
                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(android.content.Intent.createChooser(intent, "Share your proof"))
                    }
                },
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.Share, contentDescription = null, tint = NvidiaGreenBright)
        Text(
            "SAVE & SHARE PROOF",
            color = NvidiaGreenBright,
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Composable
private fun SetupNeededBanner(onOpenSettings: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SecondaryContainer.copy(alpha = 0.12f))
            .border(1.dp, SecondaryContainer.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Text("One-time setup needed", color = SecondaryContainer, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            "Before it can solve problems, log in to LeetCode and connect the AI in Settings.",
            color = OnSurfaceVariant,
            fontSize = 13.sp,
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(SecondaryContainer)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onOpenSettings,
                )
                .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Text("GO TO SETTINGS", color = SurfaceContainerLowest, fontFamily = JetBrainsMono, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
private fun SolveButtonCard(titleSlug: String, stage: Stage, onExecute: () -> Unit) {
    val running = stage == Stage.PICKING || stage == Stage.EXTRACTING || stage == Stage.SOLVING || stage == Stage.SUBMITTING
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .border(1.dp, NvidiaGreenBright.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Text(
            "Ready when you are",
            color = NvidiaGreenBright,
            fontFamily = JetBrainsMono,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            if (running && titleSlug.isNotBlank()) "Working on: $titleSlug"
            else "It picks a problem for you — today's Daily Challenge, or a backup from Settings.",
            color = OnSurfaceVariant,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 2.dp),
        )
        Spacer(Modifier.height(12.dp))
        val gradient = Brush.horizontalGradient(listOf(NvidiaGreen, SecondaryContainer))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(50))
                .background(gradient)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = !running,
                    onClick = onExecute,
                )
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (running) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = SurfaceContainerLowest,
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = SurfaceContainerLowest)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                if (running) "WORKING…" else "SOLVE",
                color = SurfaceContainerLowest,
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
            )
        }
    }
}

private data class PipelineStageInfo(val label: String, val icon: ImageVector)

@Composable
private fun ActivePipelineCard(stage: Stage, attempt: Int = 0, maxAttempts: Int = 1) {
    // steps.size counts completed attempts, so the attempt currently in flight is one more.
    val attemptLabel = if (stage == Stage.SUBMITTING && maxAttempts > 1) " (try ${attempt + 1} of $maxAttempts)" else ""
    val stages = listOf(
        PipelineStageInfo("Picking a problem", Icons.Default.Shuffle),
        PipelineStageInfo("Reading the problem", Icons.Default.Download),
        PipelineStageInfo("AI is writing a solution", Icons.Default.Memory),
        PipelineStageInfo("Submitting to LeetCode$attemptLabel", Icons.Default.CloudUpload),
        PipelineStageInfo("Waiting for the verdict$attemptLabel", Icons.Default.Gavel),
    )
    val activeIndex = when (stage) {
        Stage.IDLE -> -1
        Stage.PICKING -> 0
        Stage.EXTRACTING -> 1
        Stage.SOLVING -> 2
        Stage.SUBMITTING -> 3
        Stage.DONE, Stage.ERROR -> 4
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .padding(16.dp),
    ) {
        Text(
            "WHAT'S HAPPENING",
            color = OnSurfaceVariant,
            fontFamily = JetBrainsMono,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
        )
        stages.forEachIndexed { i, info ->
            val complete = i < activeIndex
            val active = i == activeIndex
            val pending = i > activeIndex
            val color = when {
                complete -> NvidiaGreen
                active -> SecondaryContainer
                else -> OutlineVariant
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(color),
                )
                Spacer(Modifier.width(12.dp))
                if (active) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = color, strokeWidth = 2.dp)
                } else {
                    Icon(info.icon, contentDescription = null, tint = color)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        info.label,
                        color = if (pending) OnSurfaceVariant else if (active) SecondaryContainer else OnSurface,
                        fontFamily = JetBrainsMono,
                        fontSize = 13.sp,
                    )
                    if (active) {
                        Text("Processing…", color = OnSurfaceVariant, fontFamily = JetBrainsMono, fontSize = 10.sp)
                    } else if (complete) {
                        Text("Complete", color = OnSurfaceVariant, fontFamily = JetBrainsMono, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun AttemptCard(step: PipelineStep) {
    val accepted = step.result.accepted
    val chipColor = if (accepted) NvidiaGreen else ErrorRed
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TerminalBlack)
            .border(1.dp, OutlineVariant, RoundedCornerShape(12.dp))
            .padding(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(chipColor.copy(alpha = 0.1f))
                        .border(1.dp, chipColor, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Icon(
                        if (accepted) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = null,
                        tint = chipColor,
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        step.result.statusMsg.uppercase(),
                        color = chipColor,
                        fontFamily = JetBrainsMono,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text("attempt ${step.attempt}", color = OnSurfaceVariant, fontFamily = JetBrainsMono, fontSize = 12.sp)
            }
        }
        if (!accepted) {
            Spacer(Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CanvasBlack, RoundedCornerShape(8.dp))
                    .border(1.dp, OutlineVariant, RoundedCornerShape(8.dp))
                    .padding(8.dp),
            ) {
                Text(
                    step.result.errorSummary,
                    color = OnSurfaceVariant,
                    fontFamily = JetBrainsMono,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                )
            }
        }
    }
}
