# Feature Implementation Pipeline

## Overview

Each feature goes through a sequential pipeline of specialized agents. Agents communicate through files in `.claude/features/<feature-name>/`. All decisions are made autonomously — no user input required during the pipeline.

## Agents

| Step | Agent | Model | Can Write | Input | Output |
|------|-------|-------|-----------|-------|--------|
| 1 | **preparer** | opus | yes | docs/, codebase | `prep.md` + doc updates |
| 2 | **doc-validator** | sonnet | no | `prep.md`, docs/ | pass/fail report |
| 3 | **coder** | opus | yes | `prep.md` | code + `code-report.md` |
| 4 | **tester** | opus | yes | `prep.md`, code | tests + `test-report.md` |
| 5 | **linter** | sonnet | yes | code + tests | formatted code |
| 6 | **mobile-tester** | opus | yes | spec, app APK | `e2e-report.md` |
| 7 | **validator** | sonnet | no | all artifacts | ✅ approved / 🔄 rework |

## File Structure

```
.claude/features/
└── <feature-name>/
    ├── prep.md          # Step 1: implementation plan
    ├── code-report.md   # Step 3: what was coded
    ├── test-report.md   # Step 4: test results + security
    ├── validation.md    # Step 6: final verdict
    └── e2e-report.md    # Step 7: mobile E2E test results
```

## Flow

```
START
  │
  ▼
[preparer] ── writes prep.md, updates docs/
  │
  ▼
[doc-validator] ── reads prep.md + docs/
  │
  ├── FAIL → back to [preparer] with issues
  │
  ▼ PASS
[coder] ── reads prep.md, writes code, builds
  │         runs in worktree (isolation: "worktree")
  │
  ▼
[tester] ── reads prep.md + code, writes tests
  │          runs in same worktree branch
  │
  ▼
[linter] ── runs formatAll + detekt, fixes issues
  │          runs in same worktree branch
  │
  ▼
[mobile-tester] ── builds APK, installs, tests on emulator
  │                 uses claude-in-mobile MCP (screenshot, tap, swipe, etc.)
  │                 requires running Android emulator
  │
  ▼
[validator] ── reads everything (incl. e2e-report), compares plan vs result
  │
  ├── REWORK → back to failing step with instructions
  │            validator specifies: who (coder/tester/linter/mobile-tester), what, why
  │
  ▼ APPROVED
DONE ── ready to merge into master
```

## Rework Loop

When validator returns 🔄 REWORK:

1. Validator writes `validation.md` with specific issues and who should fix them
2. Orchestrator re-runs the specified agent with instructions from `validation.md`
3. Pipeline resumes from that step forward
4. Maximum 2 rework cycles per step — after that, escalate to user

## Parallel Execution

Features CAN run in parallel when they don't share files:

```
master
  ├── worktree: feat/settings    ← pipeline running
  ├── worktree: feat/search      ← pipeline running (parallel)
  └── worktree: feat/theme       ← pipeline running (parallel)
```

**Known shared files** (will cause merge conflicts):
- `di/KoinModule.kt` — all features add registrations
- `navigation/Route.kt` — features adding routes
- `navigation/NavGraph.kt` — features adding screens
- `designsystem/Theme.kt` — theme-related features

Merge order matters. Merge one, rebase others, continue.

## Orchestrator Responsibilities

The orchestrator (main Claude session) manages:

1. **Decide feature order** — based on dependencies and priority
2. **Launch agents** — with correct prompts and worktree isolation
3. **Pass state** — tell each agent where to find previous step's output
4. **Handle rework** — re-run agents when validator rejects
5. **Merge branches** — help user resolve conflicts
6. **Track progress** — update TaskList with feature statuses

## Starting a Feature

Orchestrator prompt template for each agent:

### Preparer
```
Feature: <name>
Spec: docs/features/<name>.md
Create plan at: .claude/features/<name>/prep.md
```

### Doc-Validator
```
Validate docs for feature: <name>
Plan: .claude/features/<name>/prep.md
Report issues or confirm pass.
```

### Coder
```
Implement feature: <name>
Plan: .claude/features/<name>/prep.md
Write report to: .claude/features/<name>/code-report.md
Build with: ./gradlew :composeApp:assembleDebug
```

### Tester
```
Test feature: <name>
Plan: .claude/features/<name>/prep.md
Code report: .claude/features/<name>/code-report.md
Write report to: .claude/features/<name>/test-report.md
Run tests: ./gradlew :composeApp:testDebugUnitTest
```

### Linter
```
Lint and format all code.
Run: ./gradlew formatAll && ./gradlew ktlintCheck && ./gradlew detekt
Fix all issues.
```

### Validator
```
Validate feature: <name>
Plan: .claude/features/<name>/prep.md
Code report: .claude/features/<name>/code-report.md
Test report: .claude/features/<name>/test-report.md
Write verdict to: .claude/features/<name>/validation.md
```

### Mobile Tester
```
E2E test feature: <name>
Spec: docs/features/<name>.md
Build: ./gradlew :composeApp:assembleDebug
Package: com.simplevideo.whiteiptv
Write report to: .claude/features/<name>/e2e-report.md
Requires: running Android emulator + claude-in-mobile MCP
```

## Feature Priority Order

Based on dependencies (implement in this order for sequential, or parallelize independent ones):

1. **Light Theme** — no dependencies, unlocks Settings appearance
2. **Settings Screen** — depends on theme support
3. **Continue Watching** — independent (DB + Home UI)
4. **Playlist Management** — independent (Home UI + new screens)
5. **Search Enhancement** — independent (DB + all screens)
6. **Playlist Auto-Refresh** — depends on Settings (auto-update toggle)
7. **EPG** — large, independent but many open questions
8. **PiP / Sleep Timer / Cast** — depends on player architecture
9. **iOS Player** — last, after all features stable
