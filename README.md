# Reverse LeetCode

An Android app whose job is to keep your LeetCode streak alive on its own.
It runs in the background on a schedule you set (time of day, how many
problems, how often) and, for each run, drives the loop: **extract problem
→ AI solves → save → submit → accepted?** If a submission fails, the
judge's error and failing testcase are fed back to the AI to fix the code,
and it resubmits — repeating until it's accepted or a max attempt count is
reached. You can also trigger a one-off solve for a specific problem slug
from the Solve tab.

See [PRD.md](PRD.md) for the full product spec.

## Stack
- Kotlin, Jetpack Compose (Material 3), coroutines
- OkHttp + kotlinx.serialization for LeetCode's GraphQL/REST API
- NVIDIA NIM (OpenAI-compatible chat completions API) as the AI solver
- Jetpack DataStore for on-device credential/config storage
- WorkManager for the recurring background streak-automation job

## Project structure
```
app/src/main/java/com/leetcodeautomation/
  app/    MainActivity
  data/   LeetCodeClient, NvidiaSolver, Pipeline, StreakWorker/StreakScheduler,
          SettingsRepository, Models
  ui/     SolverScreen, SettingsScreen, HistoryScreen, StatsScreen,
          SolverViewModel, BottomNav, TopBar, Theme
```

## Setup
1. Open the project root in Android Studio (Koala+) and let it sync.
2. In the app's Settings tab, provide:
   - **LeetCode session cookie** and **CSRF token** — copy these from your
     browser's cookies while logged in to leetcode.com
     (`LEETCODE_SESSION`, `csrftoken`).
   - **NVIDIA API key** — from [build.nvidia.com](https://build.nvidia.com).
   - Model name (defaults to `meta/llama-3.3-70b-instruct`) and
     max fix attempts (defaults to 5).
3. Turn on **Streak Automation** and set:
   - **Solve time** — hour:minute the automation fires each day.
   - **Problems per run** — how many problems to solve each time (today's
     Daily Challenge first, then `Backup slugs` if more are needed).
   - **Repeat every N days** — 1 for daily.
   Tap **Save Config** — this schedules (or reschedules) the background
   job via WorkManager, which persists across app restarts and reboots.
4. For an on-demand solve, enter a problem slug (e.g. `two-sum`) on the
   Solve tab and tap Execute.

## Notes
- Only Python 3 submissions are supported in v1.
- Background runs are headless — you'll get a notification summarizing
  what was solved/failed. The History/Stats tabs only reflect attempts
  made in the current app session (on-demand Solve runs), not background
  runs; persisting run history across restarts is a future addition.
- Builds via GitHub Actions (`.github/workflows/build.yml`) since this
  sandbox can't reach Google's Maven repo to build locally — every push
  produces a debug APK as a downloadable workflow artifact.
- **Security**: LeetCode session cookie, CSRF token, and the NVIDIA API
  key are stored in a plaintext Jetpack DataStore file in app-private
  storage. `android:allowBackup` is set to `false` so they can't leave
  the device via `adb backup`/cloud backup, but they're still plaintext
  at rest — recoverable from a rooted device or a backup-bypassing
  exploit. Encrypting them (e.g. `androidx.security` `EncryptedSharedPreferences`
  or Keystore-wrapped values) is a reasonable follow-up hardening step,
  not yet done here.
