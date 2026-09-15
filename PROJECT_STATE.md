# ProotX Project State

> Canonical, current state of ProotX. This file is updated before stopping any
> substantial engineering session. Always re-verify with Git — this is a
> point-in-time record, and verified repository state overrides stale docs.

Last updated: 2026-09-15 (P1E1 build-tooling bridge — CLOSED / PASS; P1E IN PROGRESS)

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
| P1D — Dependency / AndroidX Modernization | **CLOSED / PASS** |
| P1D1 — Barista Removal / AndroidTest Build Restoration | **CLOSED / PASS** |
| P1D2 — Dead Play Services Dependency Cleanup | **CLOSED / PASS** |
| P1D3 — Lifecycle Extensions Removal / ViewModelProvider Migration | **CLOSED / PASS** |
| P1D4 — Navigation 2.1.0 Stable Migration | **CLOSED / PASS** |
| P1D5 — Room 2.1.0 Stable Migration | **CLOSED / PASS** |
| P1D6 — AndroidX Preference 1.1.0 Stable Migration | **CLOSED / PASS** |
| P1D7 — Material Components 1.1.0 Stable Migration | **CLOSED / PASS** |
| P1D8 — Arch Core Testing 2.1.0 Stabilization | **CLOSED / PASS** |
| P1D9 — AndroidX Core KTX 1.1.0 Alignment | **CLOSED / PASS** |
| P1D Final Dependency Closure Audit | **CLOSED / PASS** |
| CI-R1 — Android SDK Bootstrap Remediation | **CLOSED / PASS** |
| P1E — SDK 36 / Manifest Compatibility | **IN PROGRESS** |
| P1E0 — Android 16 Toolchain + Platform Readiness Audit | **CLOSED / PASS** |
| P1E1-P — Kotlin / AGP Build-Tooling Bridge Compatibility Probe | **CLOSED / BRIDGE_FOUND** |
| P1E1 — Kotlin/AndroidX Codegen + Build-Tooling Bridge | **CLOSED / PASS** |
| P1E2 — Moshi Codegen KAPT → KSP Readiness | **NOT STARTED** |
| P1D7-U — SwipeRefreshLayout Ownership + Material Retry | **CLOSED / SUPERSEDED BY P1D7-U2** |
| P1D7-U2 — Explicit Legacy Replacements + Material Final Retry | **CLOSED / PASS** |

## Current Milestone

**P1E1 — Kotlin/AndroidX Codegen + Build-Tooling Bridge: CLOSED / PASS.** The proven P1E1-P
bridge is now persisted: **Gradle 7.6.4 / AGP 7.4.2 / Kotlin 1.9.25 / Moshi 1.15.2 (KAPT) /
Navigation 2.3.5 / JaCoCo 0.8.8 / Mockito 4.11.0 (test-only) / JDK 17 / Build Tools 30.0.3 /
NDK 21.4.7075529**. Seven behavior-neutral Kotlin-1.9 source fixes and a Navigation guard
update landed. CI is now a single JDK 17 stage. Final dependencies include a CI-forced minimal
`gradle-download-task` 3.4.3 → **5.0.0** correction. Local canonical gate and remote CI
(run `34938888803`) are green: **36 suites / 326 tests / 0 failures / 0 errors / 0 skipped**.
compileSdk/targetSdk/minSdk unchanged. Details:
[`docs/P1E_ANDROID16_MIGRATION_PLAN.md`](docs/P1E_ANDROID16_MIGRATION_PLAN.md) §18.
Next milestone: **P1E2 — Moshi Codegen KAPT → KSP Readiness — NOT STARTED**. Do not start it
from this document.

## Repository State

| Ref | SHA | Notes |
|---|---|---|
| Active branch | `feature/android-modernization` | |
| Feature HEAD | `bd3f6e4f335c4a1e9371c554e2850b69b1e2f30d` | P1E1 implementation (download-task fix); control-plane commit follows |
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
| Unit tests (current) | **326 tests / 36 suites / 0 failures / 0 errors / 0 skipped** (+ P1C1/P1C2-R/P1D1..P1D9 guards) |
| Baseline build | `./gradlew clean assembleDebug testDebugUnitTest` → **BUILD SUCCESSFUL** |

## Current Toolchain

| Component | Version |
|---|---|
| Gradle (wrapper) | **7.6.4** |
| Android Gradle Plugin | **7.4.2** |
| Kotlin / KGP | **1.9.25** |
| Moshi (runtime + codegen) | **1.15.2** (codegen via KAPT) |
| AndroidX Navigation | **2.3.5** (stable) |
| AndroidX Room | 2.1.0 (stable; runtime/compiler/testing) |
| AndroidX Preference | 1.1.0 (stable) |
| Material Components | 1.1.0 (stable) |
| SwipeRefreshLayout (direct) | 1.0.0 |
| LocalBroadcastManager (direct) | 1.0.0 |
| Arch Core testing (test-only) | 2.1.0 |
| Core KTX (direct) | 1.1.0 (transitive `core` resolves 1.3.0 via Navigation 2.3.5) |
| AndroidX Lifecycle (direct) | 2.2.0 (`lifecycle-viewmodel`, `lifecycle-livedata`) |
| Parcelize plugin | `kotlin-parcelize` (legacy `kotlin-android-extensions` removed) |
| JaCoCo | **0.8.8** (+ `jdk.internal.*` exclusion for JDK 17) |
| Mockito (test-only) | **4.11.0** (core + inline) |
| gradle-download-task | **5.0.0** |
| JDK for the Gradle build | **17** |
| `compileSdk` / `targetSdk` (app) | 30 / 30 |
| `minSdk` | 21 |
| Android NDK | 21.4.7075529 (explicit `ndkVersion`) |
| Android build-tools | 30.0.3 |

## UI View Access & Parcelize (P1C)

- Android **View Binding** is enabled (`buildFeatures.viewBinding true`).
- **Zero** `kotlinx.android.synthetic` imports; `SyntheticViewImportsTest` enforces this.
- **Zero** `kotlinx.android.parcel` usage; Parcelize uses `kotlinx.parcelize.Parcelize` via
  the `kotlin-parcelize` plugin; `LegacyAndroidExtensionsGuardTest` enforces this.
- `ParcelableContractTest` verifies the model types implement `Parcelable` and expose a
  static `CREATOR`.

## CI State

**CI is passing.** Single-stage **JDK 17** bootstrap and Gradle build (the JDK 8 build stage was
removed in P1E1). `google()` + `mavenCentral()` only. Pinned packages: `platform-tools`,
`platforms;android-30`, `platforms;android-29`, `build-tools;30.0.3`, `ndk;21.4.7075529`.

**CI-R1 note:** `android-actions/setup-android@v3` runs with `packages: ''` (its default
`tools platform-tools` install broke when the legacy `tools` package was retired);
`platform-tools` is owned explicitly by the pinned `sdkmanager` step. Triggers unchanged.

Verified remote evidence (P1E1):

| Field | Value |
|---|---|
| Run | `34938888803` (push, commit `bd3f6e4`) — **success** (4m57s) |
| Log proof | `sdkmanager 16.0`; installed `platform-tools`, `android-29`, `android-30`, `build-tools;30.0.3`, `ndk;21.4.7075529`; `Gradle 7.6.4` on `JVM 17.0.20.1`; `:app:downloadAssets`/`:app:fetchAssets` executed; two `BUILD SUCCESSFUL` (app + androidTest) |
| Remote test summary | `suites=36 tests=326 failures=0 errors=0 skipped=0` |
| Artifacts | `prootx-debug-apk` (ZIP 19,118,467 bytes; extracted APK 19,938,185 bytes, SHA-256 `d2090b90…803b90150`) and `prootx-debug-androidTest-apk` (ZIP 1,370,435 bytes; extracted APK 1,824,590 bytes, SHA-256 `54584e3f…d3856c21`) |
| Artifact identity | app: `io.github.lord1egypt.prootx` / 1.0.0 / targetSdk 30 / ABIs arm64-v8a, armeabi-v7a, x86, x86_64; androidTest: `io.github.lord1egypt.prootx.test` |

The workflow also runs `:app:assembleDebugAndroidTest` and uploads both APKs, so androidTest
dependency resolution is a standing gate.

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

**None.** P1D is closed; P1E1 is closed and remote CI is green (run `34938888803`).

## Deferred Findings

The canonical list lives in
[`docs/PROOTX_2_ROADMAP.md`](docs/PROOTX_2_ROADMAP.md#deferred-findings).

- **Resolved in P1A:** failing CI Android SDK setup.
- **Resolved in P1B:** `jcenter()` fallback repository.
- **Resolved in P1C:** synthetic views; Kotlin↔Moshi version fork; legacy Android
  Extensions and legacy Parcelize package (all closed via P1C1/P1C2-P/P1C2-R).
- **Resolved in P1D1:** `com.schibsted.spain:barista:3.1.0` (androidTest JCenter debt);
  androidTest resolves from `google()` + `mavenCentral()` and builds in CI.
- **Resolved in P1D2:** dead direct `com.google.android.gms:play-services-base:17.2.1`
  dependency and stale `ENABLE_PLAY_SERVICES` BuildConfig flag (now **ABSENT**; its
  merged-manifest entries removed).
- **Resolved in P1D3:** deprecated `lifecycle-extensions` and `ViewModelProviders` (replaced
  by granular Lifecycle 2.2.0 + `ViewModelProvider`).
- **Resolved in P1D4:** pre-release Navigation baseline (2.1.0-alpha05 → **2.1.0** stable).
  Beneficial transitive stabilization: `androidx.fragment` → 1.1.0, `lifecycle-runtime` /
  `lifecycle-viewmodel-ktx` → 2.1.0.
- **Resolved in P1D5:** pre-release Room baseline (2.1.0-beta01 → **2.1.0** stable). Schema 7
  byte-identical; DB version 7, migrations and `Data.db` unchanged.
- **Resolved in P1D6:** pre-release Preference baseline (1.1.0-alpha05 → **1.1.0** stable).
  Zero source/XML change; appcompat transitively stabilized to 1.1.0.
- **Resolved in P1D7-U2:** Material 1.1.0 stable. ProotX now owns the two libraries it
  consumes directly (`swiperefreshlayout:1.0.0`, `localbroadcastmanager:1.0.0`) that Material
  alpha previously supplied via `androidx.legacy`; Material's legacy transitive graph is not an
  API ownership contract (`DECISIONS.md` D022 accepted).
- **Resolved in P1D8:** pre-release/misaligned `core-testing` (2.0.0-beta01 → **2.1.0** stable);
  Arch Core family now coherent at 2.1.0. Test-only.
- **Resolved in P1D9:** misaligned `androidx.core:core-ktx` (1.0.2 → **1.1.0** stable), now
  matching the `androidx.core:core` 1.1.x family; `collection` resolves 1.1.0.
- **Classified in P1D closure audit (deferred dependency debt, non-blocking).** None of these
  blocks P1E; each requires a dedicated future milestone/decision:
  - **Coroutines:** direct `kotlinx-coroutines-core`/`-android` declaration is `1.0.0`, but
    resolution selects `core` `1.3.9` (via `billing-ktx:3.0.3`) and `android` `1.1.1` (via
    `lifecycle-viewmodel-ktx:2.1.0`). Source compiles against the resolved versions.
  - **Sentry** `io.sentry:sentry-android:1.7.22` — ACTIVE; legacy API (`Sentry.init`,
    `AndroidSentryClientFactory`, `EventBuilder`) deeply coupled to production logging;
    dedicated migration.
  - **Billing** `com.android.billingclient:billing-ktx:3.0.3` — ACTIVE; upgrade requires
    API/source migration (Play Billing library policy is a later release concern).
  - **OkHttp** `3.14.7`, **Moshi** `1.9.3` (pinned as the verified Kotlin 1.4 bridge),
    **Gson** `2.8.6` (rationalize/remove vs Moshi), **JArchiveLib** `0.8.0`,
    **SLF4J-nop** logging backend.
  - **UI support:** `androidx.constraintlayout:constraintlayout:1.1.3`; and
    `androidx.appcompat` / `androidx.fragment` / `androidx.recyclerview` are consumed
    directly but supplied transitively — explicit ownership hardening deferred.
  - **LocalBroadcastManager** `1.0.0` — deprecated technology; replacement deferred.
  - **Test stack:** JUnit `4.12`, Mockito `2.23.0` (+inline), mockito-kotlin `2.1.0`,
    AndroidX Test `1.2.0` / Ext-JUnit `1.1.0` / Espresso `3.2.0` / UiAutomator `2.2.0` /
    Orchestrator `1.2.0`.
- **P1D closure blockers found:** none.
- **Resolved in CI-R1:** GitHub Actions Android SDK bootstrap failure
  (`android-actions/setup-android@v3` requesting the retired `tools` package). Fixed with
  `packages: ''` plus explicit `platform-tools` in the pinned `sdkmanager` step.
- **Resolved in P1E1:** the AGP 7 / Kotlin 1.9 build-tooling bridge (Gradle 7.6.4, AGP 7.4.2,
  Kotlin 1.9.25, Moshi 1.15.2 KAPT, Navigation 2.3.5, JaCoCo 0.8.8, Mockito 4.11.0 test-only,
  JDK 17, NDK 21.4 pinned). CI moved to a single JDK 17 stage. A CI-only gap in the P1E1-P
  finding forced a minimal `gradle-download-task` 3.4.3 → 5.0.0 upgrade (local probe never
  scheduled the download task). **Deferred to P1E2:** Moshi codegen KAPT → KSP.
- **Correction:** Sentry and Billing are **ACTIVE** production dependencies (SentryLogger /
  Sentry; BillingManager / BillingClient / Purchase) — they are **not** unused and were left
  untouched.
- **Still open:** prebuilt rootfs profile remnant; network-dependent unit tests; Play-readiness
  gaps (→ P1E); dynamic time-based `versionCode`; LocalBroadcastManager modernization
  (deprecated tech; deferred, and classified above).

## Next Safe Action

**P1E2 — Moshi Codegen KAPT → KSP Readiness — NOT STARTED.** Migrate Moshi codegen from KAPT
to KSP on the current Kotlin 1.9.25 bridge (Room may keep KAPT temporarily) so the Kotlin 2.2
jump (P1E3) does not carry a deprecated KAPT codegen path. Do **not** start it from this
document.
