package com.leetcodeautomation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.foundation.rememberScrollState
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leetcodeautomation.data.AiProvider
import com.leetcodeautomation.data.Settings
import com.leetcodeautomation.data.SolveLanguage

@Composable
fun SettingsScreen(initial: Settings, onSave: (Settings) -> Unit) {
    var session by remember { mutableStateOf(initial.leetcodeSession) }
    var csrf by remember { mutableStateOf(initial.csrfToken) }
    var apiKey by remember { mutableStateOf(initial.nvidiaApiKey) }
    var model by remember { mutableStateOf(initial.aiModel) }
    var aiProvider by remember { mutableStateOf(initial.aiProvider) }
    var openaiApiKey by remember { mutableStateOf(initial.openaiApiKey) }
    var groqApiKey by remember { mutableStateOf(initial.groqApiKey) }
    var geminiApiKey by remember { mutableStateOf(initial.geminiApiKey) }
    var anthropicApiKey by remember { mutableStateOf(initial.anthropicApiKey) }
    var openrouterApiKey by remember { mutableStateOf(initial.openrouterApiKey) }
    var customBaseUrl by remember { mutableStateOf(initial.customApiBaseUrl) }
    var customApiKey by remember { mutableStateOf(initial.customApiKey) }
    var maxAttempts by remember { mutableStateOf(initial.maxFixAttempts) }
    var submissionLanguage by remember { mutableStateOf(initial.submissionLanguage) }
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
        aiProvider = aiProvider,
        openaiApiKey = openaiApiKey,
        groqApiKey = groqApiKey,
        geminiApiKey = geminiApiKey,
        anthropicApiKey = anthropicApiKey,
        openrouterApiKey = openrouterApiKey,
        customApiBaseUrl = customBaseUrl,
        customApiKey = customApiKey,
        maxFixAttempts = maxAttempts,
        submissionLanguage = submissionLanguage,
        streakEnabled = streakEnabled,
        solveHour = solveHour,
        solveMinute = solveMinute,
        problemsPerRun = problemsPerRun,
        repeatEveryDays = repeatEveryDays,
        backupSlugs = backupSlugs,
    )

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = CanvasBlack,
        topBar = { AppTopBar(onSettingsClick = {}) },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    containerColor = NvidiaGreenBright,
                    contentColor = SurfaceContainerLowest,
                ) {
                    Text(data.visuals.message, fontFamily = JetBrainsMono, fontWeight = FontWeight.Bold)
                }
            }
        },
    ) { padding ->
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Dot(NvidiaGreenBright)
                        Text(
                            "NVIDIA's free AI works with nothing to pick — switch below only if you'd rather use a provider you already pay for.",
                            color = OnSurfaceVariant,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    FieldLabel("AI provider")
                    Spacer(Modifier.height(6.dp))
                    ProviderPicker(
                        selected = AiProvider.fromId(aiProvider),
                        onSelect = { provider ->
                            aiProvider = provider.id
                            if (provider != AiProvider.CUSTOM) model = provider.defaultModel
                        },
                    )

                    Spacer(Modifier.height(12.dp))
                    when (val provider = AiProvider.fromId(aiProvider)) {
                        AiProvider.NVIDIA -> HelpField(
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
                        AiProvider.CUSTOM -> AdvancedProviderFields(
                            baseUrl = customBaseUrl,
                            apiKey = customApiKey,
                            onBaseUrlChange = { customBaseUrl = it },
                            onApiKeyChange = { customApiKey = it },
                        )
                        else -> ProviderKeyField(
                            provider = provider,
                            value = when (provider) {
                                AiProvider.OPENAI -> openaiApiKey
                                AiProvider.GROQ -> groqApiKey
                                AiProvider.GEMINI -> geminiApiKey
                                AiProvider.ANTHROPIC -> anthropicApiKey
                                AiProvider.OPENROUTER -> openrouterApiKey
                                else -> ""
                            },
                            onChange = { value ->
                                when (provider) {
                                    AiProvider.OPENAI -> openaiApiKey = value
                                    AiProvider.GROQ -> groqApiKey = value
                                    AiProvider.GEMINI -> geminiApiKey = value
                                    AiProvider.ANTHROPIC -> anthropicApiKey = value
                                    AiProvider.OPENROUTER -> openrouterApiKey = value
                                    else -> {}
                                }
                            },
                        )
                    }

                    AdvancedModelField(model) { model = it }

                    Spacer(Modifier.height(16.dp))
                    FieldLabel("Which language should it write?")
                    Spacer(Modifier.height(6.dp))
                    LanguagePicker(
                        selected = SolveLanguage.fromSlug(submissionLanguage),
                        onSelect = { submissionLanguage = it.langSlug },
                    )

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
                                    onClick = {
                                        onSave(currentSettings())
                                        scope.launch { snackbarHostState.showSnackbar("Settings saved") }
                                    },
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

/**
 * A small fixed set of AI providers the app knows how to talk to — unlike the model list within
 * a provider, provider identities/endpoints are stable, so a chip picker is fine here.
 */
@Composable
private fun ProviderPicker(selected: AiProvider, onSelect: (AiProvider) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AiProvider.entries.forEach { provider ->
            val active = provider == selected
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (active) NvidiaGreenBright else SurfaceContainerLowest)
                    .border(1.dp, if (active) NvidiaGreenBright else OutlineVariant, RoundedCornerShape(8.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onSelect(provider) },
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Text(
                    provider.label,
                    color = if (active) SurfaceContainerLowest else OnSurface,
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

/** Masked API key field for a specific non-NVIDIA provider, with a short "where to get it" hint. */
@Composable
private fun ProviderKeyField(provider: AiProvider, value: String, onChange: (String) -> Unit) {
    var visible by remember { mutableStateOf(false) }
    Column {
        FieldLabel("${provider.label} API key")
        Spacer(Modifier.height(2.dp))
        Text(provider.keyHint, color = OnSurfaceVariant, fontSize = 11.sp)
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
    }
}

/**
 * A small fixed set of languages the app itself supports end-to-end (starter code fetch, AI
 * prompt, and LeetCode's submission API) — unlike the AI model list, this isn't an external
 * catalog that goes stale, so a simple toggle is fine here.
 */
@Composable
private fun LanguagePicker(selected: SolveLanguage, onSelect: (SolveLanguage) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SolveLanguage.entries.forEach { lang ->
            val active = lang == selected
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (active) NvidiaGreenBright else SurfaceContainerLowest)
                    .border(1.dp, if (active) NvidiaGreenBright else OutlineVariant, RoundedCornerShape(8.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onSelect(lang) },
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                Text(
                    lang.displayName,
                    color = if (active) SurfaceContainerLowest else OnSurface,
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                )
            }
        }
    }
}

/**
 * Hidden by default — most people should never need this. Free-text rather than a fixed
 * dropdown, since NVIDIA models get renamed/deprecated over time and a hardcoded list goes
 * stale (this app shipped with one that was already scheduled for deprecation).
 */
@Composable
private fun AdvancedModelField(value: String, onChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { expanded = !expanded },
                )
                .padding(top = 8.dp, bottom = 4.dp),
        ) {
            Text("Advanced: change AI model", color = NvidiaGreenBright, fontSize = 12.sp)
            Icon(
                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = NvidiaGreenBright,
            )
        }
        if (expanded) {
            Text(
                "Only change this if NVIDIA discontinues the current model. Find valid IDs at " +
                    "build.nvidia.com — open a model page and copy the id shown in its code sample.",
                color = OnSurfaceVariant,
                fontSize = 11.sp,
                modifier = Modifier.padding(bottom = 6.dp),
            )
            OutlinedTextField(
                value = value,
                onValueChange = onChange,
                singleLine = true,
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
}

/**
 * Hidden by default. Lets someone swap NVIDIA out entirely for a different OpenAI-compatible
 * AI service (OpenAI, Groq, Together AI, etc.) by supplying that provider's endpoint + key.
 * When both are blank, the app keeps using NVIDIA with the API key above.
 */
@Composable
private fun AdvancedProviderFields(
    baseUrl: String,
    apiKey: String,
    onBaseUrlChange: (String) -> Unit,
    onApiKeyChange: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var keyVisible by remember { mutableStateOf(false) }
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { expanded = !expanded },
                )
                .padding(top = 8.dp, bottom = 4.dp),
        ) {
            Text("Advanced: use a different AI provider", color = NvidiaGreenBright, fontSize = 12.sp)
            Icon(
                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = NvidiaGreenBright,
            )
        }
        if (expanded) {
            Text(
                "Only needed if you'd rather use a different AI service instead of NVIDIA. " +
                    "Must be an OpenAI-compatible chat completions endpoint. Leave both blank to " +
                    "keep using NVIDIA with the key above.",
                color = OnSurfaceVariant,
                fontSize = 11.sp,
                modifier = Modifier.padding(bottom = 6.dp),
            )
            FieldLabel("Provider endpoint URL")
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = baseUrl,
                onValueChange = onBaseUrlChange,
                singleLine = true,
                placeholder = { Text("https://api.openai.com/v1/chat/completions", color = OnSurfaceVariant.copy(alpha = 0.5f), fontSize = 12.sp) },
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

            Spacer(Modifier.height(12.dp))
            FieldLabel("Provider API key")
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = apiKey,
                onValueChange = onApiKeyChange,
                singleLine = true,
                placeholder = { Text("Paste it here", color = OnSurfaceVariant.copy(alpha = 0.6f), fontSize = 13.sp) },
                visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { keyVisible = !keyVisible }) {
                        Icon(
                            if (keyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
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
