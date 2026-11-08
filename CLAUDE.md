# Expense Tracker (SMS-based) — Project Guide for Claude

## What this app is
An Android expense tracker that automatically reads bank/card SMS alerts,
parses them into transactions (amount, currency, merchant, date, account,
debit/credit), and stores them ON THE DEVICE ONLY. Target market: Sri Lanka first.

## Non-negotiable rules
- Raw SMS never leaves the device. No network calls with SMS content. No analytics on message text.
- Only process messages from known bank sender IDs (allowlist in `parser/senders`). Ignore all other SMS — never read, log, or store them.
- Never log full SMS bodies or account numbers in Logcat. Mask account numbers (show last 4 only).
- Do not add new dependencies without asking first.
- Do not change the database schema without writing a Room migration.

## Tech stack
- Kotlin, Jetpack Compose, Material 3
- Architecture: MVVM + Repository, single-activity
- Room (SQLite) for local storage; DataStore for settings
- Hilt for DI; Coroutines + Flow
- Min SDK 26, target latest stable

## Module layout
- `:parser` — PURE Kotlin/JVM module. No Android imports. All SMS parsing logic lives here.
- `:data` — Room DB, DAOs, repositories, SMS reader (ContentResolver) and BroadcastReceiver.
- `:app` — Compose UI, ViewModels, navigation.

## Parser design
- One parser per bank: `parser/banks/<BankName>Parser.kt`, implementing `BankSmsParser`.
- Return `ParseResult.Success(transaction)` / `ParseResult.Unrecognized(reason)` — never throw, never guess silently.
- Unrecognized messages from allowlisted senders go to a "Needs review" inbox in the UI.
- Test fixtures: `parser/src/test/resources/samples/<bank>/*.txt` (anonymized real messages)
  with expected output in matching `.json` files.

## Commands
- Parser unit tests (fast, run these constantly): `./gradlew :parser:test`
- All tests: `./gradlew test`
- Lint: `./gradlew lint`
- Debug build: `./gradlew assembleDebug`
- Install on device/emulator: `./gradlew installDebug`
- Simulate incoming SMS on emulator: `adb emu sms send <SENDER> "<message text>"`

## Workflow rules for Claude
- For any non-trivial change, propose a plan first and wait for approval.
- Write/update tests before or alongside parser changes. A parser change is done only when `./gradlew :parser:test` passes.
- Keep changes small and focused on the task asked. Don't refactor unrelated code.
- After finishing a task, summarize what changed and what to test manually.

## Current status
<!-- Update this section as the project progresses -->
- [x] Project skeleton with 3 modules
- [ ] SMS permission flow + read inbox filtered by sender allowlist
- [ ] Parser for first bank + fixtures
- [ ] Transaction list + monthly total screen
- [ ] Live SMS receiver
- [ ] Categories + user corrections
- [ ] "Needs review" inbox
