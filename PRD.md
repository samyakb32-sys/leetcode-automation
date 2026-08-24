# PRD: LeetCode Automation (Android)

## 1. Summary
An Android app whose sole purpose is protecting the user's LeetCode streak.
It runs on a schedule the user sets — a time of day, how many problems to
solve, and how often to repeat — and for each run it pulls a problem
(today's Daily Challenge by default), has an AI model generate a solution,
submits it, and — if it's rejected — feeds the judge's error/testcase back
to the AI to fix the code and resubmit, repeating until it's Accepted or a
max attempt count is hit. It also supports solving a specific slug on
demand from the Solve tab.

## 2. Problem statement
Missing a day breaks a LeetCode streak, and manually re-typing failed
submissions, reading judge output, and iterating on a fix is repetitive.
This app closes the loop automatically in the background — the user sets
it once and it keeps the streak alive without them opening the app.

## 3. Goals
- **Primary: never miss a streak day.** Run automatically in the
  background at a user-configured time, without the app being open.
- Let the user configure: solve time (hour:minute), problems to solve per
  run, and how often the automation repeats (every N days).
- Solve a given LeetCode problem slug on demand with zero manual coding
  (Solve tab), for ad-hoc use outside the schedule.
- Show the pipeline state in real time: Extract → Solve → Save → Submit →
  Judge.
- On failure, automatically retry with AI-generated fixes up to a
  configurable attempt limit.
- Let the user store their own LeetCode session + NVIDIA API credentials
  on-device.

### Non-goals
- Multi-language submissions beyond Python 3 (v1 is Python-only).
- Browsing/searching the LeetCode problem catalog (slug must be known).
- Account creation or LeetCode login flow inside the app (user supplies an
  existing session cookie).

## 4. Users
Individuals practicing LeetCode who want an AI-assisted, automated
solve → submit → fix loop, primarily for learning/benchmarking model
performance rather than for contest/interview use.

## 5. Core user flow
1. User opens the app, enters a problem slug (e.g. `two-sum`), taps Solve.
2. App fetches the problem via LeetCode's GraphQL API.
3. App sends the problem statement + starter code to an NVIDIA-hosted
   model, which returns an explanation, complexity analysis, and code.
4. App saves the solution and submits it to LeetCode.
5. App polls for a verdict.
   - **Accepted** → pipeline ends, success state shown with runtime/memory.
   - **Rejected** → judge feedback (failing testcase, expected/actual
     output, compile/runtime error) is sent back to the model for a fix,
     and the app resubmits.
6. Loop repeats until Accepted or `maxFixAttempts` is reached.
7. Every attempt (code + verdict) is shown as a card in a scrolling log.

## 6. Functional requirements
- **Settings screen**: store LeetCode session cookie, CSRF token, NVIDIA
  API key, model name, max fix attempts, and streak-automation config
  (enabled, solve time, problems per run, repeat-every-N-days, backup
  slugs) — persisted via DataStore.
- **Streak automation**: a background job (WorkManager) scheduled per the
  user's config. Each run solves today's Daily Challenge first, then
  configured backup slugs, up to `problemsPerRun`, using the same
  solve→submit→fix pipeline as the Solve tab. Reschedules itself on the
  configured interval and survives reboots.
- **Solve tab**: slug input, Execute button, pipeline status tracker,
  attempt log, final verdict banner — for on-demand runs outside the
  schedule.
- **History / Stats tabs**: lightweight in-session view of past attempts
  and accepted/attempted counts.
- **LeetCode client**: fetch problem by slug (GraphQL), fetch today's Daily
  Challenge slug, submit solution, poll submission result.
- **AI solver**: call NVIDIA NIM's OpenAI-compatible chat completions API
  to solve and to fix; parse code out of a fenced code block.
- **Pipeline**: coordinates the above and emits one `PipelineStep` per
  attempt for the UI to render live (skipped when running headless in the
  background).

## 7. Non-functional requirements
- Credentials stored locally only (DataStore), never transmitted anywhere
  except their respective APIs (LeetCode, NVIDIA).
- Network calls run off the main thread (Dispatchers.IO / coroutines).
- Judge polling capped at 60s per submission to avoid hanging the UI.

## 8. Out of scope / future work
- OAuth-based LeetCode login instead of manual cookie entry.
- Support for additional languages (Java, C++, JS).
- Local history/persistence of past solved problems across sessions.
- Streaming AI responses into the UI instead of waiting for the full
  completion.

## 9. Success metrics
- % of attempted problems reaching Accepted within `maxFixAttempts`.
- Average attempts-to-accept per difficulty tier (Easy/Medium/Hard).
