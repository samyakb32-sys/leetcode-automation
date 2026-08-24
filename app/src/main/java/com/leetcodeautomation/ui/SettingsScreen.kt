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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
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

// Confirmed live on build.nvidia.com's catalog (free endpoint available) as of this writing.
private val MODELS = listOf(
    "meta/llama-3.1-70b-instruct",
    "meta/llama-3.1-8b-instruct",
    "meta/llama-3.2-1b-instruct",
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
                        "Settings",
                        color = OnSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                    )
                    Text(
                        "A one-time setup, then the app solves problems on its own.",
                        color = OnSurfaceVariant,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(NvidiaGreenBright.copy(alpha = 0.08f))
                        .border(1.dp, NvidiaGreenBright.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                ) {
                    Text("3 things to set up", color = NvidiaGreenBright, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "1. Log in to LeetCode below, so the app can submit as you.\n" +
                            "2. Get a free NVIDIA key, so the app can write solutions.\n" +
                            "3. Pick a time and turn on Streak Automation.",
                        color = OnSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                    )
                }
            }

            item {
                SettingsCard(title = "Log in to LeetCode", icon = Icons.Default.VpnKey) {
                    Text(
                        "The app needs to be logged in as you to submit solutions. You'll copy two values from your browser, once.",
                        color = OnSurfaceVariant,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                    HelpField(
                        friendlyLabel = "Your LeetCode login",
                        technicalLabel = "LEETCODE_SESSION",
                        value = session,
                        onChange = { session = it },
                        steps = listOf(
                            "On your phone or computer, open leetcode.com and make sure you're logged in.",
                            "Open your browser's Developer Tools (on Chrome: menu ⋮ → More tools → Developer tools, or press F12).",
                            "Go to the \"Application\" tab (Chrome) or \"Storage\" tab (Firefox), then Cookies → leetcode.com.",
                            "Find the row named LEETCODE_SESSION, copy its long Value, and paste it here.",
                        ),
                    )
                    Spacer(Modifier.height(16.dp))
                    HelpField(
                        friendlyLabel = "Security token",
                        technicalLabel = "CSRF_TOKEN",
                        value = csrf,
                        onChange = { csrf = it },
                        steps = listOf(
                            "In that same Cookies list for leetcode.com...",
                            "Find the row named csrftoken, copy its Value, and paste it here.",
                        ),
                    )
                }
            }

            item {
                SettingsCard(title = "Connect the AI", icon = Icons.Default.Memory) {
                    Text(
                        "This app uses NVIDIA's free AI service to write solutions. You need a personal key, once.",
                        color = OnSurfaceVariant,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                    HelpField(
                        friendlyLabel = "AI key",
                        technicalLabel = "NVIDIA_API_KEY",
                        value = apiKey,
                        onChange = { apiKey = it },
                        steps = listOf(
                            "Go to build.nvidia.com and sign in (a free account works).",
                            "Open any model page and tap \"Get API Key\".",
                            "Copy the key — it starts with nvapi- — and paste it here.",
                        ),
                    )
                }
            }

            item {
                SettingsCard(title = "Protect My Streak", icon = Icons.Default.Bolt) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Turn on automation", color = OnSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text(
                                "Solves problems for you in the background, even if you never open the app.",
                                color = OnSurfaceVariant,
                                fontSize = 12.sp,
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
                    FieldLabel("What time should it run?")
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Stepper(solveHour, min = 0, max = 23) { solveHour = it }
                        Text(":", color = OnSurface, fontFamily = JetBrainsMono, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 8.dp))
                        Stepper(solveMinute, min = 0, max = 59) { solveMinute = it }
                        Text("(24-hour clock, your phone's time zone)", color = OnSurfaceVariant, fontSize = 11.sp, modifier = Modifier.padding(start = 12.dp))
                    }

                    Spacer(Modifier.height(16.dp))
                    FieldLabel("How many problems each time?")
                    Spacer(Modifier.height(6.dp))
                    Stepper(problemsPerRun, min = 1, max = 10) { problemsPerRun = it }

                    Spacer(Modifier.height(16.dp))
                    FieldLabel("How often?")
                    Spacer(Modifier.height(6.dp))
                    Stepper(repeatEveryDays, min = 1, max = 30) { repeatEveryDays = it }
                    Text(
                        "1 = every day (recommended for keeping a streak). It solves today's Daily Challenge first, then the extra problems below if it needs more.",
                        color = OnSurfaceVariant,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )

                    Spacer(Modifier.height(16.dp))
                    FieldLabel("Extra problems (optional)")
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
                    Text(
                        "Only used if the Daily Challenge alone isn't enough to reach the number above. Paste the end of the problem's URL (leetcode.com/problems/two-sum → \"two-sum\"), separated by commas.",
                        color = OnSurfaceVariant,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }

            item {
                SettingsCard(title = "AI Settings", icon = Icons.Default.Memory) {
                    FieldLabel("AI model")
                    Spacer(Modifier.height(6.dp))
                    ModelDropdown(model) { model = it }

                    Spacer(Modifier.height(16.dp))

                    FieldLabel("How many tries before giving up?")
                    Spacer(Modifier.height(6.dp))
                    Stepper(maxAttempts, onChange = { maxAttempts = it })
                    Text(
                        "If a solution is rejected, the AI reads the error and tries again, up to this many times.",
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
                            Text("Ready", color = OnSurfaceVariant, fontFamily = JetBrainsMono, fontSize = 13.sp)
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
                                "SAVE",
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

/** Small caption above a control — plain-language, not the technical/env-var name. */
@Composable
private fun FieldLabel(text: String) {
    Text(text, color = OnSurface, fontSize = 13.sp, fontWeight = FontWeight.Medium)
}

/**
 * A masked credential field with a plain-language label, the technical name in small print
 * (for anyone who already knows it), and an expandable "How do I find this?" step list.
 */
@Composable
private fun HelpField(
    friendlyLabel: String,
    technicalLabel: String,
    value: String,
    onChange: (String) -> Unit,
    steps: List<String>,
) {
    var visible by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }
    Column {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                FieldLabel(friendlyLabel)
                Text(technicalLabel, color = OnSurfaceVariant, fontFamily = JetBrainsMono, fontSize = 10.sp)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { showHelp = !showHelp },
                    )
                    .padding(4.dp),
            ) {
                Icon(Icons.Default.HelpOutline, contentDescription = null, tint = NvidiaGreenBright)
                Text(
                    "How do I find this?",
                    color = NvidiaGreenBright,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp),
                )
                Icon(
                    if (showHelp) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = NvidiaGreenBright,
                )
            }
        }

        if (showHelp) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 8.dp)
                    .background(SurfaceContainerLowest, RoundedCornerShape(8.dp))
                    .padding(12.dp),
            ) {
                steps.forEachIndexed { i, step ->
                    Row(modifier = Modifier.padding(bottom = if (i < steps.lastIndex) 6.dp else 0.dp)) {
                        Text("${i + 1}.", color = NvidiaGreenBright, fontSize = 12.sp, modifier = Modifier.padding(end = 6.dp))
                        Text(step, color = OnSurfaceVariant, fontSize = 12.sp, lineHeight = 17.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            singleLine = true,
            placeholder = { Text("Paste it here", color = OnSurfaceVariant.copy(alpha = 0.6f), fontSize = 13.sp) },
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
        if (value.isNotBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                Dot(NvidiaGreenBright)
                Text("Filled in", color = OnSurfaceVariant, fontSize = 11.sp, modifier = Modifier.padding(start = 6.dp))
            }
        }
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
