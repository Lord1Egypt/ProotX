# ProotX Session Handoff

> Durable handoff for any future engineering session. Read this together with
> `PROJECT_STATE.md`, `TASKS.md`, `DECISIONS.md`, and `docs/PROOTX_2_ROADMAP.md`.
> Never rely on previous chat transcripts; the repository is the source of truth.

Last updated: 2026-09-15 (P1E1-P bridge probe: BRIDGE_FOUND — P1E IN PROGRESS)

## Current Objective

Modernize ProotX toward the ProotX 2.0 architecture in controlled milestones, keeping the
application runtime/UI behavior invariant during toolchain work.

## Last Completed Milestone

**P1E1-P — Build-Tooling Bridge Compatibility Probe**: **BRIDGE_FOUND** (disposable;
fully reverted). A coherent intermediate bridge is proven locally: **Gradle 7.6.4 / AGP 7.4.2 /
Kotlin 1.9.25 / Moshi 1.15.2 (KAPT) / Navigation 2.3.5 / JDK 17 / NDK 21.4.7075529**,
with JaCoCo **0.8.8** (+`jdk.internal.*` exclusion) and test-only Mockito **4.11.0** →
canonical gate **326 tests / 36 suites / 0 failures/errors/skips**. See
`docs/P1E_ANDROID16_MIGRATION_PLAN.md` §17.

## Current Milestone

**P1E — SDK 36 / Manifest Compatibility**: IN PROGRESS (P1E1-P done; P1E1 not started).

## What Was Completed

- Probe Cells 1–3 + final candidate (all disposable, reverted): attributed AGP 7.4.2's KGP
  floor, Safe Args 2.1.0 task-validation break, Navigation 2.5.3 minCompileSdk wall, and the
  smallest working Navigation (2.3.5).
- Identified required JDK-17 fixes: JaCoCo 0.8.8 + exclusion, test-only Mockito 4.11.0, and 7
  behavior-neutral Kotlin-1.9 source fixes.
- Confirmed `gradle-download-task` 3.4.3 and the ktlint JavaExec task need **no** change.
- Recorded P1E0 factual corrections and the revised sequence in the migration plan.
- **No persistent implementation change** (only documentation).

## What Was Intentionally NOT Changed

- compileSdk/targetSdk/minSdk (30/30/21; terminal 29/29/21), NDK 21.4, manifests, production
  source, Gradle files, wrapper, workflow, resources.
- No `android:exported`, PendingIntent, FGS, storage or notification work (those are later P1E
  milestones).

## What Was Intentionally NOT Changed

- **No dependency version was changed** (no upgrade, addition, removal, force, or exclusion);
  the closure audit only inspected and classified.
- Deferred (non-blocking) debt recorded: Coroutines (declared `1.0.0`, resolved `1.3.9`/
  `1.1.1`), Sentry `1.7.22`, Billing `3.0.3`, OkHttp `3.14.7`, Moshi `1.9.3`, Gson `2.8.6`,
  JArchiveLib `0.8.0`, ConstraintLayout `1.1.3`, LocalBroadcastManager `1.0.0`, and the
  JUnit4/Mockito/AndroidX-Test stack.
- Kotlin 1.4.32 (jvmTarget 1.8), Moshi 1.9.3, Gradle 6.7.1, AGP 4.2.2, Lifecycle 2.2.0,
  Navigation 2.1.0, Room 2.1.0, Core KTX 1.1.0, and all other dependency versions;
  SDK/NDK/build-tools.
- Preference keys/defaults/dependencies/persistence, SharedPreferences names, AutoApp
  clearing, Proot debug settings.
- Runtime, UI, navigation, database, PRoot, network, billing, Sentry, assets.

## Current Repository State

| Ref | SHA |
|---|---|
| Active branch | `feature/android-modernization` |
| Feature HEAD (P1E1-P baseline) | `577b6de98a1c4eceab0dd0e129711ad118ad935f` |
| `main` | `94abf5fa520255bb10d087a6be3ba2bc70b0e127` |
| `develop` | `94abf5fa520255bb10d087a6be3ba2bc70b0e127` |
| Baseline tag | `v1.0.0-baseline` → `94abf5fa520255bb10d087a6be3ba2bc70b0e127` |

## Accepted Baseline

Commit `94abf5fa520255bb10d087a6be3ba2bc70b0e127`, package
`io.github.lord1egypt.prootx`, version `1.0.0`. Baseline tests **313 / 24 suites**; current
tests **326 / 36 suites** (guard/contract tests).

## Current Toolchain

Gradle **6.7.1** · AGP **4.2.2** · Kotlin **1.4.32** (jvmTarget 1.8) · Moshi **1.9.3** ·
Navigation **2.1.0** · Room **2.1.0** · Preference **1.1.0** · Material **1.1.0** ·
SwipeRefreshLayout **1.0.0** / LocalBroadcastManager **1.0.0** (direct) · Arch Core testing
**2.1.0** (test-only) · Core KTX **1.1.0** (aligned with `core` 1.1.0) · Lifecycle **2.2.0** ·
plugin **`kotlin-parcelize`** · JDK **8** (build) · compileSdk **30** · targetSdk **30** ·
minSdk **21** · NDK **21.4.7075529** · build-tools **30.0.2**. Repositories: `google()`,
`mavenCentral()`.

## Known Deferred Findings

See [`docs/PROOTX_2_ROADMAP.md`](docs/PROOTX_2_ROADMAP.md#deferred-findings). Resolved: CI
SDK setup (P1A), `jcenter()` (P1B), Kotlin/Android Extensions (P1C), Barista (P1D1), dead
Play Services (P1D2), Lifecycle extensions (P1D3), Navigation pre-release (P1D4), Room
pre-release (P1D5), Preference pre-release (P1D6), Material pre-release (P1D7), Arch Core
testing pre-release (P1D8), Core KTX misalignment (P1D9). **Sentry and Billing are ACTIVE
production dependencies, not unused; both were left untouched.** Newly classified in the P1D
closure audit (deferred, **non-blocking**): Coroutines (`1.0.0` declaration superseded at
resolution), Sentry `1.7.22`, Billing `3.0.3`, OkHttp `3.14.7`, Moshi `1.9.3`, Gson `2.8.6`,
JArchiveLib `0.8.0`, ConstraintLayout `1.1.3`, transitive AppCompat/Fragment/RecyclerView
ownership, LocalBroadcastManager, and the JUnit4/Mockito/AndroidX-Test stack. Still open:
prebuilt rootfs profile remnant; network-dependent unit tests; Play-readiness gaps (→ P1E);
dynamic time-based `versionCode`; `LocalBroadcastManager` modernization (deprecated tech,
deferred).

**CI infrastructure finding — RESOLVED in CI-R1:** the closure-documentation push had failed
remote CI in `android-actions/setup-android@v3` (`Warning: Failed to find package 'tools'`)
before any ProotX build step (upstream SDK package retirement). Fixed with `packages: ''` plus
explicit `platform-tools`; remote run `34919847167` is green.

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
   D020, D021, D022.

## Next Safe Action

**P1E1 — Kotlin/AndroidX codegen + build-tooling bridge (Gradle 7.6.4 / AGP 7.4.2 / Kotlin
1.9.25 / Moshi 1.15.2 / Navigation 2.3.5 / JDK 17) — NOT STARTED.** See
`docs/P1E_ANDROID16_MIGRATION_PLAN.md` §15.1 for the exact version set and required
behavior-neutral source fixes. Sentry and Billing remain active and require a dedicated
decision before any change. Do not begin without explicit authorization.

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
