# ProotX Project State

> Canonical, current state of ProotX. This file is updated before stopping any
> substantial engineering session. Always re-verify with Git — this is a
> point-in-time record, and verified repository state overrides stale docs.

Last updated: 2026-09-12 (P1B — Gradle / AGP Bridge Migration)

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
| P1A — Build-System / JDK / CI Foundation | **CLOSED / PASS** |
| P1B — Gradle / AGP Bridge Migration | **CLOSED / PASS** |
| P1C — Kotlin / Synthetics Migration | **NOT STARTED** |

## Current Milestone

**P1B — Gradle / AGP Bridge Migration: COMPLETE.** Gradle 5.1.1 → 6.7.1 and AGP
3.4.3 → 4.2.2, with Kotlin, SDK levels and NDK unchanged. Next milestone is **P1C**
(not started).

## Repository State

| Ref | SHA | Notes |
|---|---|---|
| Active branch | `feature/android-modernization` | |
| Feature HEAD (P1B build) | `70bcc09a74ce81a5dfefef30ca2c19a88cb2c86a` | P1B bridge build commits (state docs updated by the follow-up control-plane commit) |
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

## Current Toolchain (P1B bridge)

| Component | Version |
|---|---|
| Gradle (wrapper) | 6.7.1 |
| Android Gradle Plugin | 4.2.2 |
| Kotlin | 1.3.61 (unchanged) |
| JDK for the Gradle build | 8 |
| `compileSdk` / `targetSdk` (app) | 30 / 30 (unchanged) |
| `minSdk` | 21 (unchanged) |
| Android NDK | 21.4.7075529 (unchanged) |
| Android build-tools | 30.0.2 |

Gradle 6.7.1 / AGP 4.2.2 is an **intentional bridge** (see `DECISIONS.md` D011), not the
final toolchain.

## CI State

**CI is passing.** Two-stage bootstrap unchanged: JDK 17 provisions the Android SDK/NDK,
JDK 8 runs the bridge Gradle build. `jcenter()` removed; only `google()` and
`mavenCentral()` are configured. Pinned packages: `platforms;android-30`,
`platforms;android-29`, `build-tools;30.0.2`, `ndk;21.4.7075529`.

Verified remote evidence (P1B):

| Field | Value |
|---|---|
| Final run | `34675865307` (push, commit `70bcc09`) — **success** |
| Log proof | `Gradle 6.7.1`; `openjdk 17.0.20.1` (bootstrap) + `openjdk 1.8.0_504` (build) |
| Remote test summary | `suites=24 tests=313 failures=0 errors=0 skipped=0` |
| Artifact | `prootx-debug-apk` → `app-debug.apk` (18,566,126 bytes) |
| Artifact SHA-256 | `0b441df5965ced75e60cd843366ebbedf2f82a518c524a760634e9a9e33c121e` |
| Artifact identity | package `io.github.lord1egypt.prootx`, versionName `1.0.0`, ABIs `arm64-v8a, armeabi-v7a, x86, x86_64` |

(`versionCode` is time-generated and therefore differs per run — expected.)

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

- **Resolved in P1A:** failing CI Android SDK setup.
- **Resolved in P1B:** `jcenter()` fallback repository (removed; resolution proven from
  google + mavenCentral with a clean Gradle home).
- **New in P1B:** `com.schibsted.spain:barista:3.1.0` is an androidTest-only dependency
  published only to JCenter and is not resolvable now. It does not affect the debug build
  or unit tests; instrumented-test resolution requires a replacement (deferred to P1D).
- **Still open:** legacy Kotlin / synthetics (→ P1C); unused Sentry/Billing code (→ P1D);
  prebuilt rootfs profile remnant (assets milestone); network-dependent unit tests;
  Play-readiness gaps (→ P1E); dynamic time-based `versionCode` release-contract gap.

## Next Safe Action

**P1C — Kotlin synthetics removal / view binding migration and controlled Kotlin
modernization.**

Do **not** start P1C from this document. A phase must be explicitly authorized.
