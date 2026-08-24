# LeetCode Automation

An Android app that automates the loop: **extract problem → AI solves →
save → submit → accepted?** If a submission fails, the judge's error and
failing testcase are fed back to the AI to fix the code, and it resubmits
— repeating until it's accepted or a max attempt count is reached.

See [PRD.md](PRD.md) for the full product spec.

## Stack
- Kotlin, Jetpack Compose (Material 3), coroutines
- OkHttp + kotlinx.serialization for LeetCode's GraphQL/REST API
- NVIDIA NIM (OpenAI-compatible chat completions API) as the AI solver
- Jetpack DataStore for on-device credential storage

## Project structure
```
app/src/main/java/com/leetcodeautomation/
  app/    MainActivity
  data/   LeetCodeClient, NvidiaSolver, Pipeline, SettingsRepository, Models
  ui/     SolverScreen, SettingsScreen, SolverViewModel, Theme
```

## Setup
1. Open the project root in Android Studio (Koala+) and let it sync.
2. In the app's Settings screen, provide:
   - **LeetCode session cookie** and **CSRF token** — copy these from your
     browser's cookies while logged in to leetcode.com
     (`LEETCODE_SESSION`, `csrftoken`).
   - **NVIDIA API key** — from [build.nvidia.com](https://build.nvidia.com).
   - Model name (defaults to `nvidia/llama-3.1-nemotron-70b-instruct`) and
     max fix attempts (defaults to 5).
3. Enter a problem slug (e.g. `two-sum`) on the home screen and tap Solve.

## Notes
- Only Python 3 submissions are supported in v1.
- This project wasn't built/verified in this sandbox — outbound access to
  Google's Maven repo is blocked here. Open it in Android Studio (or CI
  with normal internet access) to sync dependencies and build.
