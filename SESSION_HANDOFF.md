# ProotX Session Handoff

> Durable handoff for any future engineering session. Read this together with
> `PROJECT_STATE.md`, `TASKS.md`, `DECISIONS.md`, and `docs/PROOTX_2_ROADMAP.md`.
> Never rely on previous chat transcripts; the repository is the source of truth.

Last updated: 2026-09-12 (P1B — Gradle / AGP Bridge Migration)

## Current Objective

Modernize ProotX toward the ProotX 2.0 architecture in controlled milestones, keeping the
application runtime/UI behavior invariant during toolchain work.

## Last Completed Milestone

**P1B — Gradle / AGP Bridge Migration**: **PASS**.
Gradle 5.1.1 → 6.7.1 and AGP 3.4.3 → 4.2.2, Kotlin unchanged, `jcenter()` removed,
build-tools aligned to 30.0.2. Local and remote build + 313 unit tests green.

## Current Milestone

**P1C — Kotlin synthetics removal / view binding migration and controlled Kotlin
modernization**: NOT STARTED.

## What Was Completed

- Upgraded the Gradle wrapper to 6.7.1 (regenerated canonical wrapper artifacts).
- Upgraded AGP to 4.2.2; Kotlin remains 1.3.61.
- Removed `jcenter()` from `buildscript` and `allprojects`; verified clean resolution from
  `google()` + `mavenCentral()` with a fresh Gradle user home.
- Aligned the CI build-tools pin to `30.0.2` (AGP 4.2.2 default).
- No DSL compatibility fixes were required (the existing `lintOptions`,
  `androidExtensions`, `testCoverageEnabled`, and nested `dependencies {}` blocks all
  configured/build under AGP 4.2.2).
- Updated `docs/BUILD_ENVIRONMENT.md` for the bridge toolchain.

## What Was Intentionally NOT Changed

- Kotlin version (1.3.61), `kotlin-android-extensions` / synthetics usage.
- `compileSdk` 30, `targetSdk` 30, `minSdk` 21, NDK 21.4.7075529.
- Application/runtime dependency versions, package ID, `versionName`, `versionCode`
  algorithm.
- Any application Kotlin/Java source, UI, resources, runtime, PRoot, database, or assets.
- Build JDK contract: JDK 17 for SDK tooling, JDK 8 for Gradle.

## Current Repository State

| Ref | SHA |
|---|---|
| Active branch | `feature/android-modernization` |
| Feature HEAD (P1B build) | `70bcc09a74ce81a5dfefef30ca2c19a88cb2c86a` |
| `main` | `94abf5fa520255bb10d087a6be3ba2bc70b0e127` |
| `develop` | `94abf5fa520255bb10d087a6be3ba2bc70b0e127` |
| Baseline tag | `v1.0.0-baseline` → `94abf5fa520255bb10d087a6be3ba2bc70b0e127` |

## Accepted Baseline

Commit `94abf5fa520255bb10d087a6be3ba2bc70b0e127`, package
`io.github.lord1egypt.prootx`, version `1.0.0`, **313 tests / 24 suites / 0 failures**.
The P0 baseline remains the accepted application baseline (P1B changed only the build
toolchain, not the app).

## Current Toolchain

Gradle **6.7.1** · AGP **4.2.2** · Kotlin **1.3.61** · JDK **8** (build) · compileSdk **30** ·
targetSdk **30** · minSdk **21** · NDK **21.4.7075529** · build-tools **30.0.2**.
Repositories: `google()`, `mavenCentral()` only.

## Known Deferred Findings

See [`docs/PROOTX_2_ROADMAP.md`](docs/PROOTX_2_ROADMAP.md#deferred-findings). Resolved: CI
SDK setup (P1A), `jcenter()` (P1B). New: `com.schibsted.spain:barista:3.1.0` (androidTest,
JCenter-only) is unresolvable and needs a replacement in P1D. Still open: Kotlin
synthetics (→ P1C); unused Sentry/Billing code (→ P1D); prebuilt rootfs profile remnant;
network-dependent unit tests; Play-readiness gaps (→ P1E); dynamic time-based `versionCode`.

## Important Invariants

1. `main` is stable and must not receive direct commits.
2. `develop` and `main` both point at the baseline until a milestone advances them.
3. `feature/android-modernization` is the only active work branch.
4. Runtime and UI behavior must stay invariant during toolchain-only modernization unless a
   milestone explicitly authorizes behavior change.
5. GPLv3 copyright and the required UserLAnd attribution must be preserved.
6. The six `ProotX-Assets-*` repositories are external runtime assets and are not modified
   without a dedicated assets milestone.
7. Published history is immutable: no force-push, no history rewrite.
8. One milestone at a time; respect STOP gates.
9. CI runs the Android SDK tooling under a modern JDK and the application build under JDK 8
   (`DECISIONS.md` D010). Gradle 6.7.1 / AGP 4.2.2 is an intentional bridge (`D011`).

## Next Safe Action

**P1C — Kotlin synthetics removal / view binding migration and controlled Kotlin
modernization** (do not begin without explicit authorization).

## Resume Procedure

Every future engineering session must, before any work:

1. Read `AGENTS.md`.
2. Read `PROJECT_STATE.md`.
3. Read `SESSION_HANDOFF.md`.
4. Read `TASKS.md`.
5. Read `DECISIONS.md`.
6. Read `docs/PROOTX_2_ROADMAP.md`.
7. Run `git status`.
8. Run `git branch --show-current`.
9. Inspect recent history: `git log --oneline --decorate -10`.
10. Verify baseline/develop/main invariants (SHAs above; re-resolve with `git rev-parse`).

Then determine:

- last completed milestone,
- current milestone,
- current blocker (if any),
- next safe action.

Do **not** restart the project from the beginning.
Do **not** trust chat memory over repository state.
