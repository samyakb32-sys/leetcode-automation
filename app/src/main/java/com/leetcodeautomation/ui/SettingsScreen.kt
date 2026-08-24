package com.leetcodeautomation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leetcodeautomation.data.Settings

private val MODELS = listOf(
    "nvidia/llama-3.1-nemotron-70b-instruct",
    "meta/llama3-70b-instruct",
    "mistralai/mixtral-8x22b-instruct",
    "nvidia/nemotron-4-340b-instruct",
)

@Composable
fun SettingsScreen(initial: Settings, onSave: (Settings) -> Unit) {
    var session by remember { mutableStateOf(initial.leetcodeSession) }
    var csrf by remember { mutableStateOf(initial.csrfToken) }
    var apiKey by remember { mutableStateOf(initial.nvidiaApiKey) }
    var model by remember { mutableStateOf(initial.aiModel) }
    var maxAttempts by remember { mutableStateOf(initial.maxFixAttempts) }
    var streakEnabled by remember { mutableStateOf(initial.streakEnabled) }
    var solveHour by remember { mutableStateOf(initial.solveHour) }
    var solveMinute by remember { mutableStateOf(initial.solveMinute) }
    var problemsPerRun by remember { mutableStateOf(initial.problemsPerRun) }
    var repeatEveryDays by remember { mutableStateOf(initial.repeatEveryDays) }
    var backupSlugs by remember { mutableStateOf(initial.backupSlugs) }

    fun currentSettings() = Settings(
        leetcodeSession = session,
        csrfToken = csrf,
        nvidiaApiKey = apiKey,
        aiModel = model,
        maxFixAttempts = maxAttempts,
        streakEnabled = streakEnabled,
        solveHour = solveHour,
        solveMinute = solveMinute,
        problemsPerRun = problemsPerRun,
        repeatEveryDays = repeatEveryDays,
        backupSlugs = backupSlugs,
    )

    Scaffold(containerColor = CanvasBlack, topBar = { AppTopBar(onSettingsClick = {}) }) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            item {
                Column {
                    Text(
                        "Configuration",
                        color = OnSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                    )
                    Text(
                        "Manage automation credentials and solver parameters.",
                        color = OnSurfaceVariant,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }

            item {
                SettingsCard(title = "Authentication", icon = Icons.Default.VpnKey) {
                    MaskedField("LEETCODE_SESSION", session) { session = it }
                    Spacer(Modifier.height(12.dp))
                    MaskedField("CSRF_TOKEN", csrf) { csrf = it }
                    Spacer(Modifier.height(12.dp))
                    MaskedField("NVIDIA_API_KEY", apiKey) { apiKey = it }
                    Text(
                        "Required for utilizing NIM inference endpoints.",
                        color = OnSurfaceVariant,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }

            item {
                SettingsCard(title = "Streak Automation", icon = Icons.Default.Bolt) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("AUTO-RUN DAILY", color = OnSurfaceVariant, fontFamily = JetBrainsMono, fontSize = 12.sp)
                            Text(
                                "Runs in the background to protect your streak, even if you never open the app.",
                                color = OnSurfaceVariant,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                        Switch(
                            checked = streakEnabled,
                            onCheckedChange = { streakEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = NvidiaGreenBright, checkedTrackColor = NvidiaGreen.copy(alpha = 0.5f)),
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    Text("SOLVE_TIME", color = OnSurfaceVariant, fontFamily = JetBrainsMono, fontSize = 12.sp)
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Stepper(solveHour, min = 0, max = 23) { solveHour = it }
                        Text(":", color = OnSurface, fontFamily = JetBrainsMono, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 8.dp))
                        Stepper(solveMinute, min = 0, max = 59) { solveMinute = it }
                    }

                    Spacer(Modifier.height(16.dp))
                    Text("PROBLEMS_PER_RUN", color = OnSurfaceVariant, fontFamily = JetBrainsMono, fontSize = 12.sp)
                    Spacer(Modifier.height(6.dp))
                    Stepper(problemsPerRun, min = 1, max = 10) { problemsPerRun = it }

                    Spacer(Modifier.height(16.dp))
                    Text("REPEAT_EVERY_N_DAYS", color = OnSurfaceVariant, fontFamily = JetBrainsMono, fontSize = 12.sp)
                    Spacer(Modifier.height(6.dp))
                    Stepper(repeatEveryDays, min = 1, max = 30) { repeatEveryDays = it }
                    Text(
                        "1 = every day. Solves today's Daily Challenge first, then backup slugs below if more are needed.",
                        color = OnSurfaceVariant,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )

                    Spacer(Modifier.height(16.dp))
                    Text("BACKUP_SLUGS (comma-separated)", color = OnSurfaceVariant, fontFamily = JetBrainsMono, fontSize = 12.sp)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = backupSlugs,
                        onValueChange = { backupSlugs = it },
                        placeholder = { Text("two-sum, valid-parentheses", fontFamily = JetBrainsMono, fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceContainerLowest,
                            unfocusedContainerColor = SurfaceContainerLowest,
                            focusedBorderColor = NvidiaGreenBright,
                            unfocusedBorderColor = OutlineVariant,
                            focusedTextColor = OnSurface,
                            unfocusedTextColor = OnSurface,
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = JetBrainsMono, fontSize = 13.sp),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            item {
                SettingsCard(title = "Solver Engine", icon = Icons.Default.Memory) {
                    Text("LLM_MODEL", color = OnSurfaceVariant, fontFamily = JetBrainsMono, fontSize = 12.sp)
                    Spacer(Modifier.height(6.dp))
                    ModelDropdown(model) { model = it }

                    Spacer(Modifier.height(16.dp))

                    Text("MAX_FIX_ATTEMPTS", color = OnSurfaceVariant, fontFamily = JetBrainsMono, fontSize = 12.sp)
                    Spacer(Modifier.height(6.dp))
                    Stepper(maxAttempts, onChange = { maxAttempts = it })
                    Text(
                        "Number of iterations to attempt fixing compilation or logical errors.",
                        color = OnSurfaceVariant,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = OutlineVariant.copy(alpha = 0.4f))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Dot(NvidiaGreenBright)
                            Spacer(Modifier.width(8.dp))
                            Text("Engine Ready", color = OnSurfaceVariant, fontFamily = JetBrainsMono, fontSize = 13.sp)
                        }
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(NvidiaGreenBright)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { onSave(currentSettings()) },
                                )
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                        ) {
                            Text(
                                "SAVE CONFIG",
                                color = SurfaceContainerLowest,
                                fontFamily = JetBrainsMono,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Dot(color: androidx.compose.ui.graphics.Color) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color),
    )
}

@Composable
private fun SettingsCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .padding(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
        ) {
            Icon(icon, contentDescription = null, tint = NvidiaGreenBright)
            Spacer(Modifier.width(8.dp))
            Text(title, color = OnSurface, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        }
        HorizontalDivider(color = OutlineVariant.copy(alpha = 0.4f))
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun MaskedField(label: String, value: String, onChange: (String) -> Unit) {
    var visible by remember { mutableStateOf(false) }
    Column {
        Text(label, color = OnSurfaceVariant, fontFamily = JetBrainsMono, fontSize = 12.sp)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            singleLine = true,
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { visible = !visible }) {
                    Icon(
                        if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle visibility",
                        tint = OnSurfaceVariant,
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceContainerLowest,
                unfocusedContainerColor = SurfaceContainerLowest,
                focusedBorderColor = NvidiaGreenBright,
                unfocusedBorderColor = OutlineVariant,
                focusedTextColor = OnSurface,
                unfocusedTextColor = OnSurface,
            ),
            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = JetBrainsMono, fontSize = 13.sp),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ModelDropdown(selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceContainerLowest)
                .border(1.dp, OutlineVariant, RoundedCornerShape(8.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { expanded = true },
                )
                .padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(selected, color = OnSurface, fontFamily = JetBrainsMono, fontSize = 13.sp)
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = OnSurfaceVariant)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            MODELS.forEach { m ->
                DropdownMenuItem(text = { Text(m, fontFamily = JetBrainsMono, fontSize = 13.sp) }, onClick = {
                    onSelect(m)
                    expanded = false
                })
            }
        }
    }
}

@Composable
private fun Stepper(value: Int, min: Int = 1, max: Int = 10, onChange: (Int) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceContainerLowest)
            .border(1.dp, OutlineVariant, RoundedCornerShape(8.dp))
            .padding(8.dp),
    ) {
        StepperButton(Icons.Default.Remove) { if (value > min) onChange(value - 1) }
        Text(
            value.toString().padStart(2, '0'),
            color = OnSurface,
            fontFamily = JetBrainsMono,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        StepperButton(Icons.Default.Add) { if (value < max) onChange(value + 1) }
    }
}

@Composable
private fun StepperButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(SurfaceOverlay)
            .border(1.dp, OutlineVariant, RoundedCornerShape(4.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(6.dp),
    ) {
        Icon(icon, contentDescription = null, tint = OnSurface)
    }
}
