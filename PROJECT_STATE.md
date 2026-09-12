# ProotX Project State

> Canonical, current state of ProotX. This file is updated before stopping any
> substantial engineering session. Always re-verify with Git — this is a
> point-in-time record, and verified repository state overrides stale docs.

Last updated: 2026-09-12 (P0.5 — Project Control Plane)

## Project Identity

| Field | Value |
|---|---|
| Project | ProotX |
| Package | `io.github.lord1egypt.prootx` |
| Current public version | `1.0.0` |
| Development program | ProotX 2.0 |
| Main repository | https://github.com/Lord1Egypt/ProotX |

## Current Phase

| Phase | Status |
|---|---|
| P0 — Baseline Freeze & Development Safety | **CLOSED / PASS** |
| P0.5 — Project Control Plane | **CURRENT** |
| P1 — Android Modernization | **NOT STARTED** |

## Current Milestone

**Project Control Plane** (P0.5) — documentation/state-management only.
No runtime, UI, build, or asset work.

The P0.5 control-plane files are introduced by the commit that added this file
(`git log --oneline -1 feature/android-modernization`).

## Repository State

| Ref | SHA | Notes |
|---|---|---|
| Branch (current) | `feature/android-modernization` | Active work branch |
| Current feature HEAD | `7f030cd9e346f800b0f1ff9545e9295ceb68977c` | P0 roadmap commit (branch HEAD when P0.5 docs were authored) |
| `develop` HEAD | `94abf5fa520255bb10d087a6be3ba2bc70b0e127` | Equals baseline |
| `main` HEAD | `94abf5fa520255bb10d087a6be3ba2bc70b0e127` | Stable; unchanged since P0 |
| Baseline tag | `v1.0.0-baseline` | Annotated tag object `edbabdf57c0d64264fae19f1a0298d9de6d82c5c` |
| Baseline commit | `94abf5fa520255bb10d087a6be3ba2bc70b0e127` | Points from baseline tag |

## Accepted Baseline

The frozen ProotX 1.0.0 baseline (measured in P0):

| Field | Value |
|---|---|
| Baseline commit | `94abf5fa520255bb10d087a6be3ba2bc70b0e127` |
| Baseline tag | `v1.0.0-baseline` (annotated) |
| Version name | `1.0.0` |
| Package | `io.github.lord1egypt.prootx` |
| Source origin | Last self-contained public UserLAnd **v2.8.3** codebase (GPLv3) |
| Unit tests | **313 tests / 24 suites / 0 failures / 0 errors / 0 skipped** |
| Baseline build | `./gradlew clean assembleDebug testDebugUnitTest` → **BUILD SUCCESSFUL** |
| Debug APK | `app/build/outputs/apk/debug/app-debug.apk` (~18.66 MB); label `ProotX` |
| Toolchain | JDK 8, Gradle 5.1.1, AGP 3.4.3, Kotlin 1.3.61, compileSdk 30, NDK 21.4.7075529 |

No data is recorded here that was not measured during P0.

## CI State

GitHub Actions workflow `build` (`.github/workflows/build.yml`) exists and is active,
but the **baseline CI currently fails** in ~10–14 seconds at the **"Set up Android SDK"**
step. `actions/setup-java@v4` pins JDK 8, while `android-actions/setup-android@v3`
needs a newer Java runtime for `sdkmanager`. The Gradle build/test steps are skipped.

This is **deferred to modernization**; it is not fixed in P0.5.

## Runtime State

The legacy UserLAnd-derived runtime remains **behaviorally unchanged** from the frozen
P0 baseline. No Runtime V2 work has started.

## UI State

The legacy UI (XML/Views) remains **unchanged**. The Compose + Material 3 redesign has
not started.

## Assets State

All six ProotX asset repositories (`ProotX-Assets-Support`, `-Debian`, `-Ubuntu`,
`-Arch`, `-Kali`, `-Alpine`) remain **unchanged since P0**. See `ASSET_TRACKING.md`.

## Current Blockers

None. Baseline is frozen, verified, and reproducible.

## Deferred Findings

The canonical Deferred Findings list lives in
[`docs/PROOTX_2_ROADMAP.md`](docs/PROOTX_2_ROADMAP.md#deferred-findings). Items currently
tracked there: failing CI SDK setup, legacy toolchain, Kotlin synthetics, unused
Sentry/Billing code, prebuilt rootfs profile remnant, `jcenter()` fallback, network-
dependent tests, and Play-readiness gaps.

Additionally confirmed in P0.5 from source (`app/build.gradle`):

- **Dynamic `versionCode` (release-contract gap).** `versionCode` is generated as a
  function of wall-clock time at Gradle configuration time
  (`def vcode = (int)(((new Date().getTime()/1000) - 1559347200) / 10)`).
  This prevents a clean PocketClaw-style separation between a *candidate* version and the
  *last physically accepted* version. **Not fixed in P0.5** — recorded for a later
  modernization / release-contract milestone.

## Next Safe Action

**P1A — first Android modernization increment** (build-system / JDK / CI foundation).

Do **not** start P1A from this document. A phase must be explicitly authorized.
