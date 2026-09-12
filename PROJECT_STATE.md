# ProotX Project State

> Canonical, current state of ProotX. This file is updated before stopping any
> substantial engineering session. Always re-verify with Git — this is a
> point-in-time record, and verified repository state overrides stale docs.

Last updated: 2026-09-12 (P1C2-P — Moshi 1.9.3 / Kotlin 1.4 bridge probe)

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
| P1C — Kotlin / Synthetics Migration | **IN PROGRESS** |
| P1C1 — Synthetic Views → View Binding | **CLOSED / PASS** |
| P1C2-U — Moshi Compatibility Unblocker | **CLOSED / BLOCKED (superseded)** |
| P1C2-P — Moshi 1.9.3 / Kotlin 1.4 Bridge Probe | **BRIDGE_FOUND** |
| P1C2 — Kotlin + Legacy Parcelize + Plugin Removal | **READY TO RETRY** |

## Current Milestone

**P1C2-P — Moshi 1.9.3 / Kotlin 1.4 Bridge Probe: BRIDGE_FOUND.** The previously untested
cell **Kotlin 1.4.32 + Moshi 1.9.3** passes `:app:kaptDebugKotlin` and a full
`clean assembleDebug testDebugUnitTest` (314 tests / 25 suites). Moshi **1.9.3 is a valid
bridge** across the Kotlin 1.3 → 1.4 boundary, so the Kotlin 1.4.32 migration and the Moshi
bump can proceed together on a small hop (no need to jump Moshi to 1.11.0). The bridge is
committed as a new green baseline with legacy Parcelize/extensions untouched.

## Repository State

| Ref | SHA | Notes |
|---|---|---|
| Active branch | `feature/android-modernization` | |
| Feature HEAD | `4f61a4710bf9092255028d9c744c0641b151a673` | Bridge commit (Kotlin 1.4.32 + Moshi 1.9.3); updated by the follow-up control-plane commit |
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
| Unit tests (current) | **314 tests / 25 suites / 0 failures / 0 errors / 0 skipped** (+1 P1C1 source guard) |
| Baseline build | `./gradlew clean assembleDebug testDebugUnitTest` → **BUILD SUCCESSFUL** |

## Current Toolchain

| Component | Version |
|---|---|
| Gradle (wrapper) | 6.7.1 |
| Android Gradle Plugin | 4.2.2 |
| Kotlin | **1.4.32** (was 1.3.61; raised in P1C2-P) |
| Moshi (runtime + codegen) | **1.9.3** (was 1.8.0; raised in P1C2-P) |
| JDK for the Gradle build | 8 |
| `compileSdk` / `targetSdk` (app) | 30 / 30 |
| `minSdk` | 21 |
| Android NDK | 21.4.7075529 |
| Android build-tools | 30.0.2 |

## UI View Access (P1C1)

- Android **View Binding** is enabled (`buildFeatures.viewBinding true`) and used for the
  Activity and all view-using Fragments.
- **Zero** `kotlinx.android.synthetic` imports in production source; a unit test
  (`SyntheticViewImportsTest`) enforces this.
- Legacy `kotlinx.android.parcel.Parcelize` and the `kotlin-android-extensions` plugin
  remain, pending the P1C2 retry.

## CI State

**CI is passing.** Two-stage bootstrap unchanged: JDK 17 provisions the Android SDK/NDK,
JDK 8 runs the Gradle build. `jcenter()` removed; only `google()` and `mavenCentral()`.
Pinned packages: `platforms;android-30`, `platforms;android-29`, `build-tools;30.0.2`,
`ndk;21.4.7075529`.

Verified remote evidence (P1C2-P bridge):

| Field | Value |
|---|---|
| Run | `34679658479` (push, commit `4f61a47`) — **success** |
| Log proof | `Gradle 6.7.1`; `openjdk 17.0.20.1` (bootstrap) + `openjdk 1.8.0_504` (build) |
| Remote test summary | `suites=25 tests=314 failures=0 errors=0 skipped=0` |
| Artifact | `prootx-debug-apk` → `app-debug.apk` (18,589,235 bytes local) |
| Artifact SHA-256 (local) | `53dc31ac594861c4a2a426369e5ca804251219b408130286d6d579995c5d03da` |
| Artifact identity | package `io.github.lord1egypt.prootx`, versionName `1.0.0`, ABIs `arm64-v8a, armeabi-v7a, x86, x86_64` |

## Runtime State

The legacy UserLAnd-derived runtime remains **behaviorally unchanged** from the frozen
P0 baseline. No Runtime V2 work has started.

## UI State

The legacy UI (XML/Views) remains **visually unchanged**. View Binding replaced only the
programmatic view-access mechanism; layouts, colors, themes and navigation are untouched.
The Compose + Material 3 redesign has not started.

## Assets State

All six ProotX asset repositories (`ProotX-Assets-Support`, `-Debian`, `-Ubuntu`,
`-Arch`, `-Kali`, `-Alpine`) remain **unchanged since P0**. See `ASSET_TRACKING.md`.

## Current Blockers

**None.** The Moshi/Kotlin version fork is resolved by the Moshi 1.9.3 bridge; both toolchain
and application build are green.

## Deferred Findings

The canonical list lives in
[`docs/PROOTX_2_ROADMAP.md`](docs/PROOTX_2_ROADMAP.md#deferred-findings).

- **Resolved in P1A:** failing CI Android SDK setup.
- **Resolved in P1B:** `jcenter()` fallback repository.
- **Resolved in P1C1:** Kotlin Android **synthetic views**.
- **Resolved in P1C2-P:** the Kotlin↔Moshi version fork. Verified matrix — Kotlin 1.3.61:
  1.8.0 PASS, 1.9.3 PASS, 1.10.0 FAIL, 1.11.0 FAIL; Kotlin 1.4.32: 1.8.0 FAIL, **1.9.3
  PASS**. Moshi **1.9.3 is the bridge**.
- **Remaining Kotlin legacy (→ P1C2 retry):** `kotlin-android-extensions` plugin,
  `androidExtensions { experimental = true }`, and `kotlinx.android.parcel.Parcelize`.
- **Still open:** `com.schibsted.spain:barista:3.1.0` (androidTest, JCenter-only);
  unused Sentry/Billing code (P1D); prebuilt rootfs profile remnant; network-dependent
  unit tests; Play-readiness gaps (P1E); dynamic time-based `versionCode`.

## Next Safe Action

**P1C2 retry (requires authorization), now a small hop on the bridge baseline:**
run `kotlin-android-extensions` → `kotlin-parcelize`, remove `androidExtensions`, and
migrate `kotlinx.android.parcel` → `kotlinx.parcelize`; Kotlin is already 1.4.32 and Moshi
already 1.9.3.

Do **not** start the retry from this document. A phase must be explicitly authorized.
