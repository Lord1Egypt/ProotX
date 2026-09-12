# ProotX Session Handoff

> Durable handoff for any future engineering session. Read this together with
> `PROJECT_STATE.md`, `TASKS.md`, `DECISIONS.md`, and `docs/PROOTX_2_ROADMAP.md`.
> Never rely on previous chat transcripts; the repository is the source of truth.

Last updated: 2026-09-12 (P1D1 — Barista removal / androidTest build restoration: PASS)

## Current Objective

Modernize ProotX toward the ProotX 2.0 architecture in controlled milestones, keeping the
application runtime/UI behavior invariant during toolchain work.

## Last Completed Milestone

**P1D1 — Barista Removal / AndroidTest Build Restoration**: **PASS**.
The JCenter-only Barista dependency is gone; androidTest builds from `google()` +
`mavenCentral()` and CI now gates it. **P1D remains IN PROGRESS.**

## Current Milestone

**P1D2 — dependency / AndroidX modernization**: NOT STARTED.

## What Was Completed

- Removed `com.schibsted.spain:barista:3.1.0` (androidTest) and migrated its usage to direct
  AndroidX Espresso.
- Espresso family pinned to one coherent **3.2.0** (core + contrib + intents); added
  `uiautomator:2.2.0` (previously transitive via Barista) for `enterAsNativeViewText`.
- Rewrote `EspressoHelpers` retry logic to use `NoMatchingViewException` /
  `AssertionFailedError` only (no broad catches), and added Espresso equivalents for
  `assertDisplayedAtPosition`, `assertNotDisplayed`, `clickDialogPositiveButton`, `writeTo`,
  `clickListItem`, `clickRadioButtonItem`.
- Added `BaristaRemovalGuardTest`.
- Restored `:app:assembleDebugAndroidTest` locally and added it to CI (plus androidTest APK
  upload).

## What Was Intentionally NOT Changed

- Gradle 6.7.1, AGP 4.2.2, Kotlin 1.4.32, Moshi 1.9.3, JDK 8 build / JDK 17 bootstrap,
  `compileSdk` 30, `targetSdk` 30, `minSdk` 21, NDK 21.4.7075529, build-tools 30.0.2.
- Production application source, resources, manifest, runtime, UI, PRoot, assets.
- Sentry and Billing (they have production references; deferred to P1D2).
- Other dependency versions.

## Current Repository State

| Ref | SHA |
|---|---|
| Active branch | `feature/android-modernization` |
| Feature HEAD (P1D1) | `45abb83d63c178992192808c7f225edf42a4a3b8` |
| `main` | `94abf5fa520255bb10d087a6be3ba2bc70b0e127` |
| `develop` | `94abf5fa520255bb10d087a6be3ba2bc70b0e127` |
| Baseline tag | `v1.0.0-baseline` → `94abf5fa520255bb10d087a6be3ba2bc70b0e127` |

## Accepted Baseline

Commit `94abf5fa520255bb10d087a6be3ba2bc70b0e127`, package
`io.github.lord1egypt.prootx`, version `1.0.0`. Baseline tests **313 / 24 suites**; current
tests **318 / 28 suites** (added guard/contract tests).

## Current Toolchain

Gradle **6.7.1** · AGP **4.2.2** · Kotlin **1.4.32** · Moshi **1.9.3** · plugin
**`kotlin-parcelize`** · JDK **8** (build) · compileSdk **30** · targetSdk **30** · minSdk
**21** · NDK **21.4.7075529** · build-tools **30.0.2**. Repositories: `google()`,
`mavenCentral()` only.

## Known Deferred Findings

See [`docs/PROOTX_2_ROADMAP.md`](docs/PROOTX_2_ROADMAP.md#deferred-findings). Resolved: CI
SDK setup (P1A), `jcenter()` (P1B), Kotlin/Android Extensions (P1C), Barista/JCenter
androidTest debt (P1D1). Still open: unused Sentry/Billing code (→ P1D2); prebuilt rootfs
profile remnant; network-dependent unit tests; Play-readiness gaps (→ P1E); dynamic
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
9. CI runs the Android SDK tooling under a modern JDK and the application build under JDK 8
   (`DECISIONS.md` D010). Gradle 6.7.1 / AGP 4.2.2 is an intentional bridge (`D011`).
10. UI uses View Binding / `kotlin-parcelize`; legacy Android Extensions are forbidden
    (`D012`, `D015`). Kotlin and Moshi move together (`D013`); Moshi 1.9.3 is the bridge
    (`D014`). androidTest uses AndroidX Espresso, Barista forbidden (`D016`).

## Next Safe Action

**P1D2 — dependency / AndroidX modernization** (inventory and handle unused Sentry/Billing
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
