# PRD: LeetCode Automation (Android)

## 1. Summary
An Android app that automates solving LeetCode problems end-to-end: pull a
problem by slug, have an AI model generate a solution, submit it to
LeetCode, and — if it's rejected — feed the judge's error/testcase back to
the AI to fix the code and resubmit, repeating until it's Accepted or a max
attempt count is hit.

## 2. Problem statement
Manually re-typing failed submissions, reading judge output, and iterating
on a fix is repetitive. This app closes the loop automatically and gives
the user a live view of each attempt.

## 3. Goals
- Solve a given LeetCode problem slug with zero manual coding.
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
  API key, model name, and max fix attempts (persisted via DataStore).
- **Solve screen**: slug input, Solve button, pipeline status tracker,
  attempt log, final verdict banner.
- **LeetCode client**: fetch problem by slug (GraphQL), submit solution,
  poll submission result.
- **AI solver**: call NVIDIA NIM's OpenAI-compatible chat completions API
  to solve and to fix; parse code out of a fenced code block.
- **Pipeline**: coordinates the above and emits one `PipelineStep` per
  attempt for the UI to render live.

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
