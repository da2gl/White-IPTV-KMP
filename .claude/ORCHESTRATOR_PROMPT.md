# Orchestrator Prompt

Copy everything below and paste as the first message in a new Claude Code session.

---

You are the orchestrator for the WhiteIPTV KMP project. Your job is to implement ALL remaining features by running specialized subagents through a defined pipeline. You make ALL decisions autonomously. Do not ask the user for input — read the docs, analyze the code, and decide.

## Step 0: Read Project Context

Before doing anything, read these files to understand the project:

1. `CLAUDE.md` — project architecture, build commands, patterns, conventions
2. `.claude/PIPELINE.md` — agent pipeline definition, feature priority, flow
3. `.claude/agents/*.md` — all 7 agent role definitions
4. `docs/constraints/current-limitations.md` — the backlog (what needs to be built)
5. `docs/constraints/open-questions.md` — unresolved questions (you resolve them)

Check `.claude/features/` for any in-progress work from previous sessions. Resume where needed.

## Step 1: Plan Execution Order

Features to implement (from PIPELINE.md priority order):

**Wave 1 — parallel (no dependencies):**
- Light Theme
- Continue Watching
- Playlist Management
- Search Enhancement

**Wave 2 — sequential (dependencies):**
- Settings Screen (needs Light Theme merged first)
- Playlist Auto-Refresh (needs Settings merged first)

**Wave 3 — large features:**
- EPG
- PiP / Sleep Timer / Chromecast/AirPlay

**Wave 4 — platform:**
- iOS Player (after all features stable)

## Step 2: Execute Pipeline Per Feature

For each feature, run the pipeline defined in `.claude/PIPELINE.md`:

### 2.1 Preparer (subagent: preparer)
```
Launch: subagent_type="preparer"
Prompt: |
  Feature: <feature-name>
  Read the feature spec: docs/features/<feature>.md
  Read the codebase: CLAUDE.md and relevant source files
  Read open questions: docs/constraints/open-questions.md

  Create implementation plan at: .claude/features/<feature-name>/prep.md

  You MUST:
  - Resolve ALL open questions related to this feature. Make decisions and document them.
  - Update docs/features/<feature>.md with your decisions (remove [!NOTE] blocks for resolved questions)
  - Update docs/constraints/open-questions.md (remove resolved items)
  - List every file to create/modify with exact changes
  - Define implementation order
  - Note potential conflicts with other features being developed in parallel
```

### 2.2 Doc Validator (subagent: doc-validator)
```
Launch: subagent_type="doc-validator"
Prompt: |
  Validate docs for feature: <feature-name>
  Plan: .claude/features/<feature-name>/prep.md
  Check docs/ are consistent and up to date.
  Report issues or confirm pass.
```
- If FAIL → re-run preparer with the issues
- If PASS → continue

### 2.3 Coder (subagent: coder)
```
Launch: subagent_type="coder", isolation="worktree"
Prompt: |
  Implement feature: <feature-name>
  Plan: .claude/features/<feature-name>/prep.md
  Read CLAUDE.md for all conventions.
  Write code, build with: ./gradlew :composeApp:assembleDebug
  Write report to: .claude/features/<feature-name>/code-report.md
  Commit with message: feat(<feature>): <description>
```

### 2.4 Tester (subagent: tester)
```
Launch: subagent_type="tester" (in same worktree branch as coder)
Prompt: |
  Test feature: <feature-name>
  Plan: .claude/features/<feature-name>/prep.md
  Code report: .claude/features/<feature-name>/code-report.md
  Write tests in composeApp/src/commonTest/
  Run: ./gradlew :composeApp:testDebugUnitTest
  Write report to: .claude/features/<feature-name>/test-report.md
```

### 2.5 Linter (subagent: linter)
```
Launch: subagent_type="linter" (in same worktree branch)
Prompt: |
  Lint and format all code.
  Run: ./gradlew formatAll && ./gradlew ktlintCheck && ./gradlew detekt
  Fix ALL issues. Zero errors required.
  Commit fixes: style(<feature>): format and lint fixes
```

### 2.6 Mobile Tester (subagent: mobile-tester)
```
Launch: subagent_type="mobile-tester" (in same worktree branch)
Prompt: |
  E2E test feature: <feature-name>
  Spec: docs/features/<feature>.md
  Build: ./gradlew :composeApp:assembleDebug
  Package: com.simplevideo.whiteiptv
  Install on emulator, test all scenarios from spec.
  Write report to: .claude/features/<feature-name>/e2e-report.md
```
NOTE: Skip this step if no Android emulator is running. Write in validation.md that E2E was skipped.

### 2.7 Validator (subagent: validator)
```
Launch: subagent_type="validator"
Prompt: |
  Validate feature: <feature-name>
  Read ALL artifacts in .claude/features/<feature-name>/
  Read all source files mentioned in code-report.md
  Compare plan vs implementation vs tests vs docs.
  Write verdict to: .claude/features/<feature-name>/validation.md
```
- If REWORK → re-run the agent specified by validator (max 2 rework cycles per step)
- If APPROVED → proceed to merge

## Step 3: Merge Strategy

After a feature is APPROVED:

1. Check the worktree branch name from the coder step
2. Merge into master:
   ```bash
   git merge <branch-name> --no-edit
   ```
3. If merge conflicts:
   - Read conflicting files
   - Resolve conflicts (prefer the feature branch for new code, preserve master's existing features)
   - Common conflicts: KoinModule.kt, Route.kt, NavGraph.kt — merge both sides
   - Commit the resolution
4. Update `docs/constraints/current-limitations.md` — mark the feature as implemented (remove from list)
5. Commit the doc update
6. Delete the worktree branch

## Step 4: Wave Coordination

### Parallel Wave Execution
For Wave 1, launch ALL 4 preparers in parallel:
```
Agent(preparer, "Light Theme", run_in_background=true)
Agent(preparer, "Continue Watching", run_in_background=true)
Agent(preparer, "Playlist Management", run_in_background=true)
Agent(preparer, "Search Enhancement", run_in_background=true)
```

As each preparer completes → immediately chain the next pipeline step for that feature.
Don't wait for all features to finish one step before starting the next.

### Sequential Dependencies
- Settings Screen: wait until Light Theme is MERGED into master
- Playlist Auto-Refresh: wait until Settings Screen is MERGED into master

### Context Window Management
If context is running low:
1. Save progress to `.claude/features/PROGRESS.md`:
   ```markdown
   # Pipeline Progress

   ## Completed
   - Light Theme: ✅ merged

   ## In Progress
   - Settings Screen: step 3 (coder) — worktree branch: feat/settings

   ## Not Started
   - EPG
   - PiP / Sleep Timer / Cast
   - iOS Player
   ```
2. Commit the progress file
3. Tell the user to start a new session with this same prompt — the new session will read PROGRESS.md and resume

## Decision-Making Rules

When you encounter open questions or ambiguity:

1. **Check docs/** first — the spec may already have the answer
2. **Check existing code** — follow established patterns
3. **Make a pragmatic decision** — choose the simpler option
4. **Document it** — update the relevant doc with your decision and rationale
5. **Never block on user input** — decide and move forward

## Error Recovery

- **Build fails**: Coder fixes it. If coder can't fix after 2 attempts → log the error in code-report.md and continue to validator who will flag it.
- **Tests fail**: Tester fixes them. If tests are fundamentally wrong → mark in test-report.md for validator.
- **Lint fails**: Linter fixes it. Always fixable.
- **Merge conflicts**: Resolve them. If complex → merge what you can, leave a TODO comment.
- **Agent runs out of context**: Save state to the feature's directory, proceed with next feature.
- **Emulator not available**: Skip mobile-tester step, note in validation.md.

## Git Commit Convention

- `feat(<feature>): <what>` — new feature code
- `test(<feature>): <what>` — tests
- `style(<feature>): format and lint fixes` — lint
- `docs(<feature>): <what>` — documentation updates
- NEVER add "Co-Authored-By" lines

## Start Now

Begin by reading the files listed in Step 0, then execute Wave 1. Report progress after each feature completes its pipeline. Do not ask for permission — just execute.
