# Feature Implementation Pipeline

## Overview

Each feature goes through a sequential pipeline of specialized agents. Agents communicate through files in `docs/features-claude/<feature-name>/`. All decisions are made autonomously — no user input required during the pipeline.

## Module Structure

**IMPORTANT**: The project uses a multi-module KMP structure:
- `shared/` — KMP library (`androidMultiplatformLibrary` plugin)
- `androidApp/` — Android app entry point
- `iosApp/` — iOS app (Xcode project)

All build commands use `:shared:` and `:androidApp:` prefixes. **Never use `:composeApp:`**.

## Agents

| Step | Agent | Model | Can Write | Input | Output |
|------|-------|-------|-----------|-------|--------|
| 1 | **preparer** | opus | yes | docs/, codebase | `prep.md` + doc updates |
| 2 | **doc-validator** | sonnet | no | `prep.md`, docs/ | pass/fail report |
| 3 | **coder** | opus | yes | `prep.md` | code + `code-report.md` |
| 4 | **tester** | opus | yes | `prep.md`, code | tests + `test-report.md` |
| 5 | **linter** | sonnet | yes | code + tests | formatted code |
| 6 | **mobile-tester** | opus | yes | spec, app APK | `e2e-report.md` |
| 7 | **validator** | sonnet | no | all artifacts | approved / rework |

## File Structure

```
docs/features-claude/
└── <feature-name>/
    ├── prep.md          # Step 1: implementation plan
    ├── code-report.md   # Step 3: what was coded
    ├── test-report.md   # Step 4: test results + security
    ├── e2e-report.md    # Step 6: mobile E2E test results
    └── validation.md    # Step 7: final verdict
```

Documentation updates go to `docs/` (never to `docs/features-claude/`).

## Flow

```
START
  │
  ▼
[preparer] ── writes prep.md, updates docs/ (marks pending features with [!NOTE])
  │
  ▼
[doc-validator] ── reads prep.md + docs/
  │
  ├── CRITICAL issues → back to [preparer]
  │
  ▼ PASS or warnings only
[coder] ── reads prep.md, writes code, builds + runs tests
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
[mobile-tester] ── builds APK, installs, tests on emulator (skip if no emulator)
  │
  ▼
[validator] ── reads everything, compares plan vs result
  │
  ├── REWORK → back to failing step with instructions
  │
  ▼ APPROVED
DONE ── merge into master
```

## Build Commands

```bash
# Build Android app
./gradlew :androidApp:assembleDebug

# Run unit tests (shared module)
./gradlew :shared:testAndroidHostTest

# Build + test (standard verification)
./gradlew :shared:testAndroidHostTest :androidApp:assembleDebug

# Build iOS framework
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64

# Lint
./gradlew formatAll && ./gradlew ktlintCheck && ./gradlew detekt
```

## Concurrency Rules

### Parallel Agents — SAFE
These steps CAN run in parallel for DIFFERENT features:
- Multiple **preparers** (read-only analysis, write to separate feature dirs)
- Multiple **doc-validators** (read-only)
- Multiple **coders** in separate worktrees (isolated filesystems)

### Parallel Agents — UNSAFE
**NEVER** run these simultaneously on the same repo:
- Two **test** runs (`./gradlew :shared:testAndroidHostTest`) — Gradle shares `build/` directory, causes `NoSuchFileException`
- Two **linter** runs — both modify the same files
- Two **coders** without worktree isolation — will conflict on files

### Workaround for Test Conflicts
If test run fails with `NoSuchFileException` on binary files:
1. Wait 30 seconds
2. `rm -rf shared/build/test-results`
3. Retry

## Wave Execution Strategy

Group features into waves based on dependencies. Within a wave, parallelize independent features.

### Wave Planning
```
Wave 1 (parallel — no dependencies):
  ├── Feature A ─── preparer → doc-validator → coder → tester → ...
  ├── Feature B ─── preparer → doc-validator → coder → tester → ...
  └── Feature C ─── preparer → doc-validator → coder → tester → ...
       ↓ all merged into master

Wave 2 (sequential — depends on Wave 1):
  ├── Feature D (depends on A) ─── pipeline
  └── Feature E (depends on B) ─── pipeline
       ↓ merged

Wave 3 ...
```

### Within a Wave
- Launch ALL preparers in parallel (`run_in_background: true`)
- As each preparer completes → immediately launch doc-validator
- As each doc-validator passes → immediately launch coder (in worktree)
- **DO NOT wait for all features** to finish one step before starting the next
- Merge completed features one at a time, resolve conflicts

## Rework Loop

When validator returns REWORK:

1. Validator writes `validation.md` with specific issues and who should fix them
2. Orchestrator re-runs the specified agent with instructions from `validation.md`
3. Pipeline resumes from that step forward
4. Maximum 2 rework cycles per step — after that, escalate to user

## Known Shared Files (Merge Conflict Risks)

- `shared/src/commonMain/.../di/KoinModule.kt` — all features add registrations
- `shared/src/commonMain/.../navigation/Route.kt` — features adding routes
- `shared/src/commonMain/.../navigation/NavGraph.kt` — features adding screens
- `gradle/libs.versions.toml` — new dependencies
- `shared/build.gradle.kts` — new dependency references

Merge order matters. Merge one, rebase/re-merge others, continue.

## Orchestrator Responsibilities

The orchestrator (main Claude session) manages:

1. **Plan waves** — group features by dependencies
2. **Launch agents** — with correct prompts, worktree isolation for coders
3. **Chain steps** — as each step completes, launch the next immediately
4. **Handle rework** — re-run agents when validator rejects
5. **Merge branches** — resolve conflicts, verify build after merge
6. **Track progress** — report status table to user

## Starting a Feature

### Preparer
```
Feature: <name>
Spec: docs/features/<name>.md
Create plan at: docs/features-claude/<name>/prep.md
```

### Doc-Validator
```
Validate docs for feature: <name>
Plan: docs/features-claude/<name>/prep.md
Report issues or confirm pass.
```

### Coder (with worktree isolation)
```
Implement feature: <name>
Plan: docs/features-claude/<name>/prep.md
Write report to: docs/features-claude/<name>/code-report.md
Build with: ./gradlew :shared:testAndroidHostTest :androidApp:assembleDebug
```

### Tester
```
Test feature: <name>
Plan: docs/features-claude/<name>/prep.md
Code report: docs/features-claude/<name>/code-report.md
Write report to: docs/features-claude/<name>/test-report.md
Run tests: ./gradlew :shared:testAndroidHostTest
```

### Linter
```
Lint and format all code.
Run: ./gradlew formatAll && ./gradlew ktlintCheck && ./gradlew detekt
Build: ./gradlew :androidApp:assembleDebug
Fix all issues.
```

### Mobile Tester
```
E2E test feature: <name>
Spec: docs/features/<name>.md
Build: ./gradlew :androidApp:assembleDebug
Package: com.simplevideo.whiteiptv
Write report to: docs/features-claude/<name>/e2e-report.md
Requires: running Android emulator + claude-in-mobile MCP
```

### Validator
```
Validate feature: <name>
Plan: docs/features-claude/<name>/prep.md
Code report: docs/features-claude/<name>/code-report.md
Test report: docs/features-claude/<name>/test-report.md
Write verdict to: docs/features-claude/<name>/validation.md
```
