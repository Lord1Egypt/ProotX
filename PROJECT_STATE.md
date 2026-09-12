# ProotX Project State

> Canonical, current state of ProotX. This file is updated before stopping any
> substantial engineering session. Always re-verify with Git — this is a
> point-in-time record, and verified repository state overrides stale docs.

Last updated: 2026-09-12 (P1A — Build-System / JDK / CI Foundation)

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
| P0.5 — Project Control Plane | **CLOSED / PASS** |
| P1 — Android Modernization | **IN PROGRESS** |
| P1A — Build-System / JDK / CI Foundation | **PASS** |
| P1B — Gradle / AGP Migration | **NOT STARTED** |

## Current Milestone

**P1A — Build-System / JDK / CI Foundation: COMPLETE.** The legacy application is
unchanged; only CI and documentation were updated. Next milestone is **P1B** (not started).

## Repository State

| Ref | SHA | Notes |
|---|---|---|
| Active branch | `feature/android-modernization` | |
| Current feature HEAD | `006dc98055038c5510bc0672435417dacef8a807` | P1A build/CI commit (state docs updated by the follow-up control-plane commit) |
| `develop` HEAD | `94abf5fa520255bb10d087a6be3ba2bc70b0e127` | Equals baseline |
| `main` HEAD | `94abf5fa520255bb10d087a6be3ba2bc70b0e127` | Stable; unchanged since P0 |
| Baseline tag | `v1.0.0-baseline` | Annotated tag object `edbabdf57c0d64264fae19f1a0298d9de6d82c5c` |
| Baseline commit | `94abf5fa520255bb10d087a6be3ba2bc70b0e127` | Points from baseline tag |

## Accepted Baseline

The frozen ProotX 1.0.0 baseline (measured in P0; still the accepted application baseline):

| Field | Value |
|---|---|
| Baseline commit | `94abf5fa520255bb10d087a6be3ba2bc70b0e127` |
| Baseline tag | `v1.0.0-baseline` (annotated) |
| Version name | `1.0.0` |
| Package | `io.github.lord1egypt.prootx` |
| Source origin | Last self-contained public UserLAnd **v2.8.3** codebase (GPLv3) |
| Unit tests | **313 tests / 24 suites / 0 failures / 0 errors / 0 skipped** |
| Baseline build | `./gradlew clean assembleDebug testDebugUnitTest` → **BUILD SUCCESSFUL** |
| Toolchain | JDK 8, Gradle 5.1.1, AGP 3.4.3, Kotlin 1.3.61, compileSdk 30, NDK 21.4.7075529 |

## CI State

**CI is now PASSING** (this is the P1A deliverable). The baseline failure was that
`actions/setup-java` pinned JDK 8 before `android-actions/setup-android` invoked
`sdkmanager` (cmdline-tools 16.0), which requires a modern JVM.

The workflow now uses a **two-stage JDK bootstrap**:

```
JDK 17 → Android SDK/NDK provisioning (sdkmanager)
JDK 8  → ./gradlew clean assembleDebug testDebugUnitTest --no-daemon
```

Pinned packages: `platforms;android-30`, `platforms;android-29`, `build-tools;28.0.3`,
`ndk;21.4.7075529`. Triggers: push to `main`, `develop`, `feature/**`; PR to `main`,
`develop`.

Verified remote evidence:

| Field | Value |
|---|---|
| Final run | `34674686561` (push, commit `006dc98`) — **success** |
| First green foundation run | `34674420285` (push, commit `0145656`) — success |
| Remote test summary | `suites=24 tests=313 failures=0 errors=0 skipped=0` |
| Artifact | `prootx-debug-apk` → `app-debug.apk` (18,650,305 bytes) |
| Artifact SHA-256 | `8a6ce2a1f70f784048c890b5f65ff733ff3a4fb31cd66267b6d3a0457be6d558` |
| Artifact identity | package `io.github.lord1egypt.prootx`, versionName `1.0.0`, ABIs `arm64-v8a, armeabi-v7a, x86, x86_64` |

(`versionCode` is time-generated and therefore differs per run — expected; see Deferred
Findings.)

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

None.

## Deferred Findings

The canonical list lives in
[`docs/PROOTX_2_ROADMAP.md`](docs/PROOTX_2_ROADMAP.md#deferred-findings).

- **Resolved in P1A:** failing CI Android SDK setup (finding #1).
- **Still open:** legacy toolchain (→ P1B+); Kotlin synthetics (→ P1C); unused
  Sentry/Billing code (→ P1D); prebuilt rootfs profile remnant (assets milestone);
  `jcenter()` fallback (→ P1B); network-dependent unit tests (later milestone);
  Play-readiness gaps (→ P1E); dynamic time-based `versionCode` release-contract gap.

## Next Safe Action

**P1B — Gradle / Android Gradle Plugin migration.**

Do **not** start P1B from this document. A phase must be explicitly authorized.
