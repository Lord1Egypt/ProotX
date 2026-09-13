# ProotX Session Handoff

> Durable handoff for any future engineering session. Read this together with
> `PROJECT_STATE.md`, `TASKS.md`, `DECISIONS.md`, and `docs/PROOTX_2_ROADMAP.md`.
> Never rely on previous chat transcripts; the repository is the source of truth.

Last updated: 2026-09-12 (P1D5 — Room 2.1.0 stable migration: PASS)

## Current Objective

Modernize ProotX toward the ProotX 2.0 architecture in controlled milestones, keeping the
application runtime/UI behavior invariant during toolchain work.

## Last Completed Milestone

**P1D5 — Room 2.1.0 Stable Migration**: **PASS**.
Shared `room_version` moved from 2.1.0-beta01 to **2.1.0** stable with zero database source
change and no schema drift. **P1D remains IN PROGRESS.**

## Current Milestone

**P1D6 — dependency / AndroidX modernization**: NOT STARTED.

## What Was Completed

- Changed the shared `room_version` to `2.1.0` (room-runtime, room-compiler, room-testing
  all remain driven by it).
- Verified zero database source change: `ProotXDatabase` version 7, entities, DAOs, queries,
  `Migration1To2`–`Migration6To7`, `Data.db` filename and schema export untouched.
- Verified exported schema 7 is byte-identical (sha256 `3909bb12…`); no schema regeneration
  drift.
- Added `RoomStabilityGuardTest`.

## What Was Intentionally NOT Changed

- Kotlin 1.4.32 (jvmTarget 1.8), Moshi 1.9.3, Gradle 6.7.1, AGP 4.2.2, Lifecycle 2.2.0,
  Navigation 2.1.0, and all other dependency versions; SDK/NDK/build-tools.
- Database version, entities, DAOs, queries, migrations, filename, builder/callbacks.
- No `fallbackToDestructiveMigration`, no `allowMainThreadQueries`.
- Runtime, UI, navigation, PRoot, network, billing, Sentry, assets.

## Current Repository State

| Ref | SHA |
|---|---|
| Active branch | `feature/android-modernization` |
| Feature HEAD (P1D5) | `a60a027c10ca2f88c1dcba60b5a524793ca4b18f` |
| `main` | `94abf5fa520255bb10d087a6be3ba2bc70b0e127` |
| `develop` | `94abf5fa520255bb10d087a6be3ba2bc70b0e127` |
| Baseline tag | `v1.0.0-baseline` → `94abf5fa520255bb10d087a6be3ba2bc70b0e127` |

## Accepted Baseline

Commit `94abf5fa520255bb10d087a6be3ba2bc70b0e127`, package
`io.github.lord1egypt.prootx`, version `1.0.0`. Baseline tests **313 / 24 suites**; current
tests **322 / 32 suites** (guard/contract tests).

## Current Toolchain

Gradle **6.7.1** · AGP **4.2.2** · Kotlin **1.4.32** (jvmTarget 1.8) · Moshi **1.9.3** ·
Navigation **2.1.0** · Room **2.1.0** · Lifecycle **2.2.0** · plugin **`kotlin-parcelize`** ·
JDK **8** (build) · compileSdk **30** · targetSdk **30** · minSdk **21** · NDK
**21.4.7075529** · build-tools **30.0.2**. Repositories: `google()`, `mavenCentral()`.

## Known Deferred Findings

See [`docs/PROOTX_2_ROADMAP.md`](docs/PROOTX_2_ROADMAP.md#deferred-findings). Resolved: CI
SDK setup (P1A), `jcenter()` (P1B), Kotlin/Android Extensions (P1C), Barista (P1D1), dead
Play Services (P1D2), Lifecycle extensions/ViewModelProviders (P1D3), Navigation pre-release
(P1D4), Room pre-release (P1D5). Still open: unused Sentry/Billing code (→ P1D6); prebuilt
rootfs profile remnant; network-dependent unit tests; Play-readiness gaps (→ P1E); dynamic
time-based `versionCode`.

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
9. Established durable decisions: D010 (two-JDK CI), D011 (Gradle/AGP bridge), D012/D015
   (View Binding / kotlin-parcelize), D013/D014 (Kotlin+Moshi), D016 (Espresso), D017 (no
   direct play-services-base), D018 (granular Lifecycle/ViewModelProvider), D019 (Navigation
   2.1.0 + jvmTarget 1.8), D020 (Room 2.1.0, schema/migrations immutable).

## Next Safe Action

**P1D6 — dependency / AndroidX modernization** (inventory and handle unused Sentry/Billing
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
