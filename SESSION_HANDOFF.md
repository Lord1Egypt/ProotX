# ProotX Session Handoff

> Durable handoff for any future engineering session. Read this together with
> `PROJECT_STATE.md`, `TASKS.md`, `DECISIONS.md`, and `docs/PROOTX_2_ROADMAP.md`.
> Never rely on previous chat transcripts; the repository is the source of truth.

Last updated: 2026-09-12 (P1D4 — Navigation 2.1.0 stable migration: PASS)

## Current Objective

Modernize ProotX toward the ProotX 2.0 architecture in controlled milestones, keeping the
application runtime/UI behavior invariant during toolchain work.

## Last Completed Milestone

**P1D4 — Navigation 2.1.0 Stable Migration**: **PASS**.
Shared `navigation_version` moved from 2.1.0-alpha05 to **2.1.0** stable; Kotlin compile
target aligned to JVM 1.8; beneficial transitive stabilization of fragment/lifecycle.
**P1D remains IN PROGRESS.**

## Current Milestone

**P1D5 — dependency / AndroidX modernization**: NOT STARTED.

## What Was Completed

- Changed the shared `navigation_version` to `2.1.0` (Safe Args plugin +
  `navigation-fragment-ktx` + `navigation-ui-ktx` all remain driven by that variable).
- Aligned the Kotlin compile target with the already-declared Java 1.8 target
  (`kotlinOptions.jvmTarget = '1.8'`) — required because Navigation 2.1.0's ktx inline
  bytecode targets JVM 1.8.
- Added `NavigationStabilityGuardTest`.
- Beneficial transitive stabilization: `androidx.fragment` 1.1.0-beta01 → 1.1.0;
  `lifecycle-runtime` / `lifecycle-viewmodel-ktx` 2.1.0-beta01 → 2.1.0.

## What Was Intentionally NOT Changed

- No navigation source/graph change (nav graph byte-identical; destinations/actions/Safe
  Args consumers unchanged).
- Direct Lifecycle artifacts remain 2.2.0; Room 2.1.0-beta01; all other dependency
  versions; Kotlin 1.4.32; Moshi 1.9.3; Gradle 6.7.1; AGP 4.2.2; SDK/NDK/build-tools.
- Runtime, UI, PRoot, database, network, billing, Sentry, assets.

## Current Repository State

| Ref | SHA |
|---|---|
| Active branch | `feature/android-modernization` |
| Feature HEAD (P1D4) | `9717d5b6740cf80be08a755e742b383a169394ce` |
| `main` | `94abf5fa520255bb10d087a6be3ba2bc70b0e127` |
| `develop` | `94abf5fa520255bb10d087a6be3ba2bc70b0e127` |
| Baseline tag | `v1.0.0-baseline` → `94abf5fa520255bb10d087a6be3ba2bc70b0e127` |

## Accepted Baseline

Commit `94abf5fa520255bb10d087a6be3ba2bc70b0e127`, package
`io.github.lord1egypt.prootx`, version `1.0.0`. Baseline tests **313 / 24 suites**; current
tests **321 / 31 suites** (guard/contract tests).

## Current Toolchain

Gradle **6.7.1** · AGP **4.2.2** · Kotlin **1.4.32** (jvmTarget 1.8) · Moshi **1.9.3** ·
Navigation **2.1.0** stable · Lifecycle **2.2.0** (viewmodel/livedata) · plugin
**`kotlin-parcelize`** · JDK **8** (build) · compileSdk **30** · targetSdk **30** · minSdk
**21** · NDK **21.4.7075529** · build-tools **30.0.2**. Repositories: `google()`,
`mavenCentral()` only.

## Known Deferred Findings

See [`docs/PROOTX_2_ROADMAP.md`](docs/PROOTX_2_ROADMAP.md#deferred-findings). Resolved: CI
SDK setup (P1A), `jcenter()` (P1B), Kotlin/Android Extensions (P1C), Barista (P1D1), dead
Play Services (P1D2), Lifecycle extensions/ViewModelProviders (P1D3), Navigation pre-release
baseline (P1D4). Still open: unused Sentry/Billing code (→ P1D5); prebuilt rootfs profile
remnant; network-dependent unit tests; Play-readiness gaps (→ P1E); dynamic time-based
`versionCode`.

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
   (`D010`). Gradle 6.7.1 / AGP 4.2.2 bridge (`D011`). View Binding / `kotlin-parcelize`
   (`D012`, `D015`); Kotlin+Moshi together (`D013`, `D014`); Espresso not Barista (`D016`);
   no direct play-services-base (`D017`); granular Lifecycle + `ViewModelProvider` (`D018`);
   Navigation stable 2.1.0 (`D019`).

## Next Safe Action

**P1D5 — dependency / AndroidX modernization** (inventory and handle unused Sentry/Billing
and remaining dependency debt). Do not begin without explicit authorization.

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
