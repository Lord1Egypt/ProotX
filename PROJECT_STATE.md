# ProotX Project State

> Canonical, current state of ProotX. This file is updated before stopping any
> substantial engineering session. Always re-verify with Git — this is a
> point-in-time record, and verified repository state overrides stale docs.

Last updated: 2026-09-12 (P1D1 — Barista removal / androidTest build restoration)

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
| P1C — Kotlin / Synthetics Migration | **CLOSED / PASS** |
| P1C1 — Synthetic Views → View Binding | **CLOSED / PASS** |
| P1C2-U — Moshi Compatibility Unblocker | **CLOSED / BLOCKED (superseded)** |
| P1C2-P — Moshi 1.9.3 / Kotlin 1.4 Bridge Probe | **CLOSED / BRIDGE_FOUND** |
| P1C2 — Kotlin + Legacy Parcelize + Plugin Removal | **CLOSED / PASS** |
| P1D — Dependency / AndroidX Modernization | **IN PROGRESS** |
| P1D1 — Barista Removal / AndroidTest Build Restoration | **CLOSED / PASS** |

## Current Milestone

**P1D1 — Barista Removal / AndroidTest Build Restoration: COMPLETE.** The JCenter-only
`com.schibsted.spain:barista:3.1.0` androidTest dependency was removed and its usage
migrated to direct AndroidX Espresso; `:app:assembleDebugAndroidTest` now resolves and
builds from `google()` + `mavenCentral()` and is enforced in CI. Next milestone is **P1D2**
(dependency/AndroidX modernization; not started).

## Repository State

| Ref | SHA | Notes |
|---|---|---|
| Active branch | `feature/android-modernization` | |
| Feature HEAD | `45abb83d63c178992192808c7f225edf42a4a3b8` | P1D1 commits (Barista→Espresso, CI androidTest gate, guard); updated by the follow-up control-plane commit |
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
| Unit tests (baseline) | **313 tests / 24 suites / 0 failures / 0 errors / 0 skipped** |
| Unit tests (current) | **318 tests / 28 suites / 0 failures / 0 errors / 0 skipped** (+ P1C1/P1C2-R/P1D1 guards) |
| Baseline build | `./gradlew clean assembleDebug testDebugUnitTest` → **BUILD SUCCESSFUL** |

## Current Toolchain

| Component | Version |
|---|---|
| Gradle (wrapper) | 6.7.1 |
| Android Gradle Plugin | 4.2.2 |
| Kotlin | 1.4.32 |
| Moshi (runtime + codegen) | 1.9.3 |
| Parcelize plugin | `kotlin-parcelize` (legacy `kotlin-android-extensions` removed) |
| JDK for the Gradle build | 8 |
| `compileSdk` / `targetSdk` (app) | 30 / 30 |
| `minSdk` | 21 |
| Android NDK | 21.4.7075529 |
| Android build-tools | 30.0.2 |

## UI View Access & Parcelize (P1C)

- Android **View Binding** is enabled (`buildFeatures.viewBinding true`).
- **Zero** `kotlinx.android.synthetic` imports; `SyntheticViewImportsTest` enforces this.
- **Zero** `kotlinx.android.parcel` usage; Parcelize uses `kotlinx.parcelize.Parcelize` via
  the `kotlin-parcelize` plugin; `LegacyAndroidExtensionsGuardTest` enforces this.
- `ParcelableContractTest` verifies the model types implement `Parcelable` and expose a
  static `CREATOR`.

## CI State

**CI is passing.** Two-stage bootstrap: JDK 17 provisions the Android SDK/NDK, JDK 8 runs
the Gradle build. `google()` + `mavenCentral()` only. Pinned packages:
`platforms;android-30`, `platforms;android-29`, `build-tools;30.0.2`, `ndk;21.4.7075529`.

Verified remote evidence (P1D1):

| Field | Value |
|---|---|
| Run | `34712929161` (push, commit `45abb83`) — **success** |
| Log proof | `Gradle 6.7.1`; JDK 17 bootstrap + JDK 8 build; two `BUILD SUCCESSFUL` (app + androidTest) |
| Remote test summary | `suites=28 tests=318 failures=0 errors=0 skipped=0` |
| Artifacts | `prootx-debug-apk` (app-debug.apk, 18,588,898 bytes) and `prootx-debug-androidTest-apk` (app-debug-androidTest.apk, 1,754,708 bytes) |
| Artifact identity | app: `io.github.lord1egypt.prootx` / 1.0.0 / ABIs arm64-v8a, armeabi-v7a, x86, x86_64; androidTest: `io.github.lord1egypt.prootx.test` |

The CI workflow now also runs `:app:assembleDebugAndroidTest` and uploads both APKs, so
androidTest dependency resolution is a standing gate.

## Runtime State

The legacy UserLAnd-derived runtime remains **behaviorally unchanged** from the frozen
P0 baseline. No Runtime V2 work has started.

## UI State

The legacy UI (XML/Views) remains **visually unchanged**. The Compose + Material 3 redesign
has not started.

## Assets State

All six ProotX asset repositories (`ProotX-Assets-Support`, `-Debian`, `-Ubuntu`,
`-Arch`, `-Kali`, `-Alpine`) remain **unchanged since P0**. See `ASSET_TRACKING.md`.

## Current Blockers

**None.** P1C is closed; no active blockers.

## Deferred Findings

The canonical list lives in
[`docs/PROOTX_2_ROADMAP.md`](docs/PROOTX_2_ROADMAP.md#deferred-findings).

- **Resolved in P1A:** failing CI Android SDK setup.
- **Resolved in P1B:** `jcenter()` fallback repository.
- **Resolved in P1C:** synthetic views; Kotlin↔Moshi version fork; legacy Android
  Extensions and legacy Parcelize package (all closed via P1C1/P1C2-P/P1C2-R).
- **Resolved in P1D1:** `com.schibsted.spain:barista:3.1.0` (androidTest JCenter debt);
  androidTest resolves from `google()` + `mavenCentral()` and builds in CI.
- **Still open:** unused Sentry/Billing code (→ P1D2); prebuilt rootfs profile remnant;
  network-dependent unit tests; Play-readiness gaps (→ P1E); dynamic time-based
  `versionCode`.

## Next Safe Action

**P1D2 — dependency / AndroidX modernization** (e.g. inventory and handle unused
Sentry/Billing code and remaining dependency debt).

Do **not** start P1D2 from this document. A phase must be explicitly authorized.
