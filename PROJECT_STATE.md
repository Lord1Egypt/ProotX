# ProotX Project State

> Canonical, current state of ProotX. This file is updated before stopping any
> substantial engineering session. Always re-verify with Git — this is a
> point-in-time record, and verified repository state overrides stale docs.

Last updated: 2026-09-12 (P1C1 — Synthetic Views → View Binding)

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
| P1C2 — Kotlin + Legacy Parcelize + Plugin Removal | **BLOCKED** |

## Current Milestone

**P1C2 — Kotlin + Legacy Parcelize + Plugin Removal: BLOCKED.** The Kotlin 1.4.32
migration cannot compile because **Moshi 1.8.0's kapt codegen** is incompatible with
Kotlin 1.4 metadata. Completing P1C2 requires bumping Moshi (a runtime dependency),
which is outside this milestone's "only Kotlin moves" invariant and belongs to P1D
pending explicit authorization. No changes were committed; the branch remains at the
P1C1 green state. Prior milestone **P1C1** is COMPLETE.

## Repository State

| Ref | SHA | Notes |
|---|---|---|
| Active branch | `feature/android-modernization` | |
| Feature HEAD (P1C1) | `fbf6f9d6dfd1b653886cdcd81a87ef4b0325a54f` | P1C1 migration + guard test (state docs updated by the follow-up control-plane commit) |
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

## Current Toolchain (unchanged since P1B)

| Component | Version |
|---|---|
| Gradle (wrapper) | 6.7.1 |
| Android Gradle Plugin | 4.2.2 |
| Kotlin | 1.3.61 |
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
  remain, pending P1C2.

## CI State

**CI is passing.** Two-stage bootstrap unchanged: JDK 17 provisions the Android SDK/NDK,
JDK 8 runs the bridge Gradle build. `jcenter()` removed; only `google()` and
`mavenCentral()`. Pinned packages: `platforms;android-30`, `platforms;android-29`,
`build-tools;30.0.2`, `ndk;21.4.7075529`.

Verified remote evidence (P1C1):

| Field | Value |
|---|---|
| Run | `34676869322` (push, commit `fbf6f9d`) — **success** |
| Log proof | `Gradle 6.7.1`; `openjdk 17.0.20.1` (bootstrap) + `openjdk 1.8.0_504` (build) |
| Remote test summary | `suites=25 tests=314 failures=0 errors=0 skipped=0` |
| Artifact | `prootx-debug-apk` → `app-debug.apk` (18,579,590 bytes) |
| Artifact SHA-256 | `6121fcec1b8e78249b2231fd7c26dd306c577143325be66512ff658ad9aa6793` |
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

None.

## Deferred Findings

The canonical list lives in
[`docs/PROOTX_2_ROADMAP.md`](docs/PROOTX_2_ROADMAP.md#deferred-findings).

- **Resolved in P1A:** failing CI Android SDK setup.
- **Resolved in P1B:** `jcenter()` fallback repository.
- **Resolved in P1C1:** Kotlin Android **synthetic views** (migrated to View Binding with
  a guard test).
- **P1C2 BLOCKER (new):** Kotlin 1.4.32 is incompatible with **Moshi 1.8.0**
  (`moshi-kotlin-codegen`) — its `me.eugeniomarletti.kotlin.metadata` reader throws
  `KotlinNullPointerException`, failing `kaptDebugKotlin`. Verified remediation: bumping
  `moshi`/`moshi-kotlin-codegen` to **1.11.0** lets kapt succeed (Room 2.1.0-beta01 is
  fine). This is an application dependency change → defer to **P1D** / needs authorization.
- **Remaining Kotlin legacy (blocked by P1C2):** `kotlin-android-extensions` plugin,
  `androidExtensions { experimental = true }`, and `kotlinx.android.parcel.Parcelize`.
- **Still open:** `com.schibsted.spain:barista:3.1.0` (androidTest, JCenter-only);
  unused Sentry/Billing code (P1D); prebuilt rootfs profile remnant; network-dependent
  unit tests; Play-readiness gaps (P1E); dynamic time-based `versionCode`.

## Next Safe Action

**Authorization decision for P1C2:** permit the minimal **Moshi 1.8.0 → 1.11.0**
annotation-processor/runtime compatibility bump (verified to unblock Kotlin 1.4.32), or
move the Kotlin 1.4.32 migration to **P1D** (dependency modernization) and rebase P1C2 on
it.

Do **not** start P1C2 from this document. A phase must be explicitly authorized.
