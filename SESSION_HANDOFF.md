# ProotX Session Handoff

> Durable handoff for any future engineering session. Read this together with
> `PROJECT_STATE.md`, `TASKS.md`, `DECISIONS.md`, and `docs/PROOTX_2_ROADMAP.md`.
> Never rely on previous chat transcripts; the repository is the source of truth.

Last updated: 2026-09-12 (P1D7-U2 — Explicit legacy replacements + Material final retry: PASS)

## Current Objective

Modernize ProotX toward the ProotX 2.0 architecture in controlled milestones, keeping the
application runtime/UI behavior invariant during toolchain work.

## Last Completed Milestone

**P1D7-U2 — Explicit Legacy Replacements + Material Final Retry**: **PASS**.
ProotX now directly declares `androidx.swiperefreshlayout:swiperefreshlayout:1.0.0` and
`androidx.localbroadcastmanager:localbroadcastmanager:1.0.0`; Material moved to **1.1.0**
stable. **P1D7 is CLOSED.**

## Current Milestone

**P1D8 — dependency / AndroidX modernization**: NOT STARTED.

## What Was Completed

- Added the two explicit direct dependencies the app consumes (previously only transitive via
  Material alpha's `androidx.legacy` graph).
- Moved Material `1.1.0-alpha06` → **1.1.0** stable; `androidx.legacy` is no longer required
  by the app's compile classpath.
- Added `MaterialDependencyOwnershipGuardTest`.

## What Was Intentionally NOT Changed

- Kotlin 1.4.32 (jvmTarget 1.8), Moshi 1.9.3, Gradle 6.7.1, AGP 4.2.2, Lifecycle 2.2.0,
  Navigation 2.1.0, Room 2.1.0, and all other dependency versions; SDK/NDK/build-tools.
- Preference keys/defaults/dependencies/persistence, SharedPreferences names, AutoApp
  clearing, Proot debug settings. No DataStore migration; no `preference-ktx` added.
- Runtime, UI, navigation, database, PRoot, network, billing, Sentry, assets.

## Current Repository State

| Ref | SHA |
|---|---|
| Active branch | `feature/android-modernization` |
| Feature HEAD (P1D7-U2) | `7be3462d2d755ab82551e24bde8f1cb46befafc4` |
| `main` | `94abf5fa520255bb10d087a6be3ba2bc70b0e127` |
| `develop` | `94abf5fa520255bb10d087a6be3ba2bc70b0e127` |
| Baseline tag | `v1.0.0-baseline` → `94abf5fa520255bb10d087a6be3ba2bc70b0e127` |

## Accepted Baseline

Commit `94abf5fa520255bb10d087a6be3ba2bc70b0e127`, package
`io.github.lord1egypt.prootx`, version `1.0.0`. Baseline tests **313 / 24 suites**; current
tests **324 / 34 suites** (guard/contract tests).

## Current Toolchain

Gradle **6.7.1** · AGP **4.2.2** · Kotlin **1.4.32** (jvmTarget 1.8) · Moshi **1.9.3** ·
Navigation **2.1.0** · Room **2.1.0** · Preference **1.1.0** · Material **1.1.0** ·
SwipeRefreshLayout **1.0.0** / LocalBroadcastManager **1.0.0** (direct) · Lifecycle **2.2.0** ·
plugin **`kotlin-parcelize`** · JDK **8** (build) · compileSdk **30** · targetSdk **30** ·
minSdk **21** · NDK **21.4.7075529** · build-tools **30.0.2**. Repositories: `google()`,
`mavenCentral()`.

## Known Deferred Findings

See [`docs/PROOTX_2_ROADMAP.md`](docs/PROOTX_2_ROADMAP.md#deferred-findings). Resolved: CI
SDK setup (P1A), `jcenter()` (P1B), Kotlin/Android Extensions (P1C), Barista (P1D1), dead
Play Services (P1D2), Lifecycle extensions (P1D3), Navigation pre-release (P1D4), Room
pre-release (P1D5), Preference pre-release (P1D6), Material pre-release (P1D7). Still open:
unused Sentry/Billing code (→ P1D8); prebuilt rootfs profile remnant; network-dependent unit
tests; Play-readiness gaps (→ P1E); dynamic time-based `versionCode`;
`LocalBroadcastManager` modernization (deprecated tech, deferred).

**Deferred physical checks:** the `EditTextPreference` numeric input and all Material widget
appearance/interaction (BottomNavigationView, TextInputLayout/EditText, FAB, dialogs) must be
verified on a device at the Golden Candidate gate; no physical acceptance is claimed yet.

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
9. Established durable decisions: D010, D011, D012/D015, D013/D014, D016, D017, D018, D019,
   D020, D021 (Preference 1.1.0), D022 (directly consumed libraries must be directly declared).

## Next Safe Action

**P1D8 — dependency / AndroidX modernization** (inventory and handle unused Sentry/Billing and
remaining dependency debt). Do not begin without explicit authorization.

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
