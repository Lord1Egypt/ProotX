# ProotX Project State

> Canonical, current state of ProotX. This file is updated before stopping any
> substantial engineering session. Always re-verify with Git — this is a
> point-in-time record, and verified repository state overrides stale docs.

Last updated: 2026-09-17 (P1F4B support packaging / whole-app static 16 KB integration — CLOSED / PASS; P1F IN PROGRESS; P1F5 READY TO START)

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
| P1E — SDK 36 / Manifest Compatibility | **CLOSED / PASS** |
| P1E0 — Android 16 Toolchain + Platform Readiness Audit | **CLOSED / PASS** |
| P1E1-P — Kotlin / AGP Build-Tooling Bridge Compatibility Probe | **CLOSED / BRIDGE_FOUND** |
| P1E1 — Kotlin/AndroidX Codegen + Build-Tooling Bridge | **CLOSED / PASS** |
| P1E2 — Moshi Codegen KAPT → KSP Migration | **CLOSED / PASS** |
| P1E3-P — AGP 8.10 / Kotlin 2.2 Compatibility Probe | **CLOSED / BRIDGE_FOUND** |
| P1E3 — AGP 8.10 / Kotlin 2.2 Implementation | **CLOSED / PASS** |
| P1E4 — compileSdk 36 Migration | **CLOSED / PASS** |
| P1E5 — API 31+ Manifest / PendingIntent / Receiver Compatibility | **CLOSED / PASS** |
| P1E6 — Storage / Permission Runtime Compatibility | **CLOSED / PASS** |
| P1E7 — Foreground Service / Notification Compatibility | **CLOSED / PASS** |
| P1E8 — targetSdk 33/34 Runtime Compatibility | **CLOSED / PASS** |
| P1E9 — targetSdk 35/36 Platform Behavior | **CLOSED / PASS** |
| P1F — Modern NDK / 16 KB Page-Size Compatibility | **IN PROGRESS** |
| P1F-P — NDK / 16 KB Compatibility Probe | **CLOSED / PARTIAL_BRIDGE** |
| P1F1 — In-Tree NDK r29 Migration + CI Pin Modernization | **CLOSED / PASS** |
| P1F2 — Support Toolchain / Provenance Modernization | **CLOSED / PASS** |
| P1F3 — Support Bundle Publication (v1.1.0) | **CLOSED / PASS** |
| P1F4-P — Whole-App 16 KB Preflight | **CLOSED / BRIDGE_FOUND** |
| P1F4A — Complete Dual-Lane Support Release (v1.2.0) | **CLOSED / PASS** |
| P1F4B — Support Packaging / Whole-App 16 KB Integration | **CLOSED / PASS** |
| P1F5 — 16 KB Runtime / Emulator Acceptance | **READY TO START** |
| P1G — Physical Device Acceptance | **NOT STARTED** |
| P1D7-U — SwipeRefreshLayout Ownership + Material Retry | **CLOSED / SUPERSEDED BY P1D7-U2** |
| P1D7-U2 — Explicit Legacy Replacements + Material Final Retry | **CLOSED / PASS** |

## Current Milestone

**P1F4B — Support Packaging / Whole-App Static 16 KB Integration: CLOSED / PASS. P1F is IN PROGRESS.**
ProotX now consumes support **`v1.2.0`**. The old flat pseudo-`.so` transport (downloadAssets/
fetchAssets writing into the tracked `src/main/jniLibs`) is removed. A single deterministic
`prepareProotXSupport` task downloads the pinned release, verifies every asset SHA-256 against
`app/support-release.lock.json`, validates the published `routing.json` + per-ABI `manifest.json`,
and stages a generated payload under `app/build/generated/prootxSupport/`: modern natives into
`jniLibs/<abi>/lib_<name>.so`, common/legacy into `assets/support/{common,legacy/<abi>}/`, and a
`support-map.json` runtime routing artifact. A manifest-driven installer resolves the lane at
runtime: API 21–28 extracts the frozen legacy payload from assets into `filesDir/support`
(integrity-checked); API 29+ links the modern payload from `nativeLibraryDir` and never copies
executable code into writable storage (Android W^X). The `.a10` filename inference and the
`lib_arch.so` pseudo-native marker are gone; ABI selection uses `Build.SUPPORTED_ABIS` and the
routing manifest. The legacy 4 KB x86_64 ELFs are packaged only under `assets/support/legacy/`, so
the APK/AAB native-library set contains no 4 KB 64-bit ELF. Static gates: APK/AAB `lib/` inventory
(all ELF, no `lib_arch`, no legacy/common), every 64-bit `PT_LOAD >= 0x4000`,
`zipalign -c -P 16` PASS, and bundletool `PAGE_ALIGNMENT_16K`. `minSdk` stays **21**, all four ABIs
are retained, and `android:extractNativeLibs="true"` is kept (`DECISIONS.md` D037). This is
**static whole-app acceptance only**: P1F5 owns the 16 KB runtime/emulator acceptance and P1G the
physical-device acceptance. **Whole-app runtime 16 KB compatibility is not yet claimed.**

## Repository State

| Ref | SHA | Notes |
|---|---|---|
| Active branch | `feature/android-modernization` | |
| Accepted P1F4B implementation | `9f14ee3f78af9f997ee38cc4c72770783dd35fd8` (feature/android-modernization) | pins support v1.2.0 (`app/support-release.lock.json`), deterministic `prepareProotXSupport` generated staging (modern `jniLibs`, common/legacy assets, `support-map.json` routing), manifest-driven installer (legacy extracted on API 21–28, modern linked from `nativeLibraryDir` on API 29+), `.a10`/`lib_arch` removed, APK/AAB static 16 KB gates |
| Accepted P1F4A support release | support tag `v1.2.0` (object `2b5691c9aa6b4ee6716a4bbcfd8559d30095d7f6`) → commit `889cb67bf8fcb55eb252513d381d00203fe9a8b4`; release `RE_kwDOUXjkQM4XTxeE` | four `*-assets.zip` (explicit `common/`+`legacy/`+`modern/` schema + `manifest.json` routing) + `routing.json` + `SHA256SUMS` + `v1.2.0-provenance.json` + `v1.2.0.spdx.json`; modern arm64/x86_64 lane 16 KB aligned; whole-app 16 KB still not claimed |
| Accepted P1F3 support release | support tag `v1.1.0` (object `ff55608c0e9490dfd3a6dc392717c19a1916d636`) → commit `acc28abcd0756cca66782145099ef54ed4cbc46c`; release `RE_kwDOUXjkQM4XOeTw` | four `*-assets.zip` + `SHA256SUMS` + `v1.1.0-provenance.json` + `v1.1.0.spdx.json`; modern arm64/x86_64 lane 16 KB aligned; whole-app 16 KB still not claimed |
| Accepted P1F2 support implementation | `a363e82b63ccc30ca678aba7dbb96c87d2879c08` (support repo `feature/p1f-support-modernization`) | reproducible support builder (pinned builder image digest + `termux-packages` commit + checksum-verified `termux/proot v5.1.107.92`), dual-lane legacy/modern model, deterministic archives, support CI |
| Accepted P1F1 implementation | `8103b835a670638c177a7adc6d7baea19680cd33` | in-tree NDK pin 21.4.7075529 → 29.0.14206865 (:app + :terminal-emulator); CI pins `ndk;29.0.14206865`, drops `ndk.dir`, adds scoped 16 KB native guard; no source/linker/packaging change |
| Accepted P1E9 implementation | `41cc7a8c629da364903de0ae71ab524541c7ef76` | app targetSdk 34 → 36; real edge-to-edge with per-owner insets; platform `OnBackInvokedCallback` for Termux on API 33+; authorized activity-ktx 1.11.0 bridge (minSdk 21 preserved); no opt-outs |
| Accepted P1E8 implementation | `cff25f3f1dffa1a91d78a55915dd50513b694af4` | `POST_NOTIFICATIONS` + contextual one-time request; resumed-lifecycle FGS gate; Termux `RECEIVER_NOT_EXPORTED`; terminal-term compileSdk 36 |
| Accepted P1E7 implementation | `0e70b7e1e3f398eb6fdb92730542cae0da6f1975` | specialUse FGS type/permission + subtype properties, service-owned channels, `startForegroundService`, immediate promotion, `FOREGROUND_SERVICE_TYPE_MANIFEST`; targetSdk remained 30 |
| Accepted P1E6 implementation | `2202d6bda33512d3312827bf2bd6dc17f47dbae9` | legacy READ/WRITE_EXTERNAL_STORAGE gate + `PermissionHandler` removed; SAF/launch no longer permission-gated; targetSdk remains 30 |
| Accepted P1E5 implementation | `6e8a0559bc691266d403a216c56ec4377ce0c98b` | `exported` on MainActivity/TermuxActivity + six immutable PendingIntents; targetSdk remains 30 |
| Accepted P1E4 implementation | `6d30b333b0a1d0b8ab0be966af4c3052dcf29500` | app compileSdk 36; targetSdk remains 30 |
| Accepted P1E3 implementation | `1828cdd4441a291433d07bd8a3e4efa96bcbfc76` | AGP 8 / Kotlin 2 implementation + JaCoCo AGP-8 path remediation |
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
| Unit tests (current) | **360 tests / 42 suites / 0 failures / 0 errors / 0 skipped** |
| Baseline build | `./gradlew clean assembleDebug testDebugUnitTest` → **BUILD SUCCESSFUL** |

## Current Toolchain

| Component | Version |
|---|---|
| Gradle (wrapper) | **8.11.1** |
| Android Gradle Plugin | **8.10.1** |
| Kotlin / KGP | **2.2.20** |
| Kotlin stdlib | **2.2.20** |
| Moshi (runtime + codegen) | **1.15.2** (codegen via **KSP2**) |
| KSP (Moshi codegen) | **2.2.20-2.0.4** |
| Room codegen | KAPT (Room 2.1.0 `room-compiler`) |
| AndroidX Navigation | **2.3.5** (stable) |
| AndroidX Room | 2.1.0 (stable; runtime/compiler/testing) |
| AndroidX Preference | 1.1.0 (stable) |
| Material Components | 1.1.0 (stable) |
| SwipeRefreshLayout (direct) | 1.0.0 |
| LocalBroadcastManager (direct) | 1.0.0 |
| Arch Core testing (test-only) | 2.1.0 |
| Core KTX (direct) | 1.1.0 (transitive `core` resolves 1.3.0 via Navigation 2.3.5) |
| AndroidX Lifecycle (direct) | 2.2.0 declaration; resolves to **2.6.2** via the P1E9 Activity 1.11.0 bridge |
| AndroidX Activity (P1E9 bridge, `:app`) | **1.11.0** (`activity-ktx`; last line before minSdk 23) — brings core/core-ktx 1.13.0, lifecycle 2.6.2, savedstate 1.2.1, coroutines 1.7.3 |
| Parcelize plugin | `kotlin-parcelize` (legacy `kotlin-android-extensions` removed) |
| JaCoCo | **0.8.8** (+ `jdk.internal.*` exclusion for JDK 17) |
| Mockito (test-only) | **4.11.0** (core + inline) |
| gradle-download-task | **5.0.0** |
| JDK for the Gradle build | **17** |
| `compileSdk` / `targetSdk` (app) | 36 / 36 |
| `minSdk` | 21 |
| `:terminal-term` `compileSdk` / `targetSdk` / `minSdk` | 36 / 29 / 21 |
| `:terminal-view` / `:terminal-emulator` `compileSdk` / `targetSdk` / `minSdk` | 29 / 29 / 21 |
| Android NDK | **29.0.14206865 (r29)** — explicit `ndkVersion` in `:app` and `:terminal-emulator`; selected via Gradle (no `ndk.dir`) |
| Android build-tools | **35.0.0** |

## UI View Access & Parcelize (P1C)

- Android **View Binding** is enabled (`buildFeatures.viewBinding true`).
- **Zero** `kotlinx.android.synthetic` imports; `SyntheticViewImportsTest` enforces this.
- **Zero** `kotlinx.android.parcel` usage; Parcelize uses `kotlinx.parcelize.Parcelize` via
  the `kotlin-parcelize` plugin; `LegacyAndroidExtensionsGuardTest` enforces this.
- `ParcelableContractTest` verifies the model types implement `Parcelable` and expose a
  static `CREATOR`.

## CI State

**CI is passing.** Single-stage **JDK 17** bootstrap and Gradle 8.11.1 build. `google()` +
`mavenCentral()` only. Pinned packages: `platform-tools`, `platforms;android-36`,
`platforms;android-29`, `build-tools;35.0.0`, `ndk;29.0.14206865`. `local.properties` carries
`sdk.dir` only; the NDK is selected by the Gradle module `ndkVersion`.

**CI-R1 note:** `android-actions/setup-android@v3` runs with `packages: ''` (its default
`tools platform-tools` install broke when the legacy `tools` package was retired);
`platform-tools` is owned explicitly by the pinned `sdkmanager` step. Triggers unchanged.

**P1F1 16 KB native guard:** the workflow now extracts the packaged
`lib/arm64-v8a/libtermux.so` and `lib/x86_64/libtermux.so` and fails if any `PT_LOAD` alignment is
below `0x4000`, using the NDK `llvm-readelf` derived from `ANDROID_SDK_ROOT`/`ANDROID_HOME` +
`ANDROID_NDK_VERSION`. It intentionally does **not** inspect the `ProotX-Assets-Support` payloads
(P1F2/P1F3 scope).

Verified remote evidence (P1F1):

| Field | Value |
|---|---|
| Run | `35049015538` (push, commit `8103b83`) — **SUCCESS** |
| Log proof | JDK 17 (`17.0.20.1`); Gradle 8.11.1; API 36 + API 29, Build Tools 35.0.0, NDK `29.0.14206865` installed; no `ndk.dir` written; canonical clean build and `assembleDebugAndroidTest` passed |
| 16 KB guard | `Verify in-tree 16 KB native alignment` step PASSED, printing `PT_LOAD align=0x4000` for arm64-v8a and x86_64 (`libtermux.so`) |
| Remote test summary | `suites=42 tests=360 failures=0 errors=0 skipped=0` |
| Artifacts | `prootx-debug-apk` (19,519,119 B artifact) and `prootx-debug-androidTest-apk` (1,400,901 B artifact) uploaded |
| JaCoCo | Standard CI does not run the report task. Separate local P1F1 proof: `jacocoCoverageReportForCi` executed from the clean/report-only state and produced non-empty XML (788,336 B) plus HTML |

Prior remote evidence (P1E9): run `35043129415` — **SUCCESS**, `suites=41 tests=355 failures=0
errors=0 skipped=0`.

**JaCoCo tooling caveat (pre-existing, non-blocking).** The custom `jacocoCoverageReportForCi`
task points its `classDirectories` at `build/intermediates/classes/debug`, which under AGP 8
contains the instrumented `jacocoDebug` output produced by the debug-variant `jacocoDebug`
transform (`testCoverageEnabled true`). If that instrumented directory is present (i.e. after
`assembleDebug`), JaCoCo aborts with `Cannot process instrumented class`. The task passes when
run from a state where only the non-instrumented unit-test classes are present (e.g.
`clean :app:jacocoCoverageReportForCi`). No JaCoCo configuration was altered; recorded for a
future build-tooling cleanup.

The workflow also runs `:app:assembleDebugAndroidTest` and uploads both APKs, so androidTest
dependency resolution is a standing gate.

## Runtime State

The legacy UserLAnd-derived runtime architecture remains **unchanged** from the frozen P0
baseline. No Runtime V2 work has started.

P1E6 is the one intentional runtime behavior change so far: app launch, session launch, filesystem
import, filesystem export, and embedded-terminal use no longer require the legacy
`READ_EXTERNAL_STORAGE`/`WRITE_EXTERNAL_STORAGE` permissions. Data locations and the PRoot
environment are unchanged — only the obsolete permission gate was removed.

P1E7 is **not** an intentional user-visible behavior change: it makes the same foreground services
structurally compatible with the Android 12–16 FGS rules (declared `specialUse` type, service-owned
notification channels, `startForegroundService` on API 26+, immediate foreground promotion,
`FOREGROUND_SERVICE_TYPE_MANIFEST` on API 29+). Notification IDs, actions, PendingIntents, session
lifecycle, and SSH behavior are unchanged.

P1E8 **is** an intentional runtime behavior change: the app targets SDK 34, so Android 13+ now asks
for notification permission contextually at the first real session start (one shared one-time
prompt, denial never blocks the session), the initial `ServerService` foreground launch is deferred
until the activity is resumed (with narrow `ForegroundServiceStartNotAllowedException` handling),
and the app-internal terminal `reload_style` receiver is explicitly app-private on API 33+. The
Linux/PRoot runtime, session lifecycle, SSH behavior, and data model are unchanged.

P1E9 **is** an intentional platform-compatibility change, not a feature/UI redesign: the app now
targets SDK 36, so Android 15+ runs true edge-to-edge (MainActivity insets the toolbar/bottom nav
and the root left/right cutout safety) and Android 15/16 predictive back is handled by the platform
dispatcher for both activities. There is no edge-to-edge or predictive-back opt-out, no orientation
lock, and no large-screen/resizability opt-out. The Linux/PRoot runtime, session lifecycle, SSH
behavior, data model, and visual design are unchanged.

P1F4B **is** an intentional support-runtime packaging change, not a feature/UI redesign. The logical
runtime contract (`filesDir/support/<name>`, `busybox sh support/execInProot.sh`, `.proot_version`
`_meta`/`_meta_leveldb` selection, `LD_LIBRARY_PATH` = support dir) is unchanged. What changed is how
the support payload reaches those paths: the modern lane is resolved from `nativeLibraryDir` and the
legacy lane is extracted from APK assets only on API 21–28. Existing filesystems keep working with no
metadata migration, and the extract/compress scripts are invoked through `busybox_static sh` so no
writable-storage script is executed directly on API 29+.

## UI State

The legacy UI (XML/Views) remains **visually unchanged** apart from correct system-bar/cutout
inset handling required by Android 15 edge-to-edge. The Compose + Material 3 redesign has not
started.

## Assets State

All six ProotX asset repositories (`ProotX-Assets-Support`, `-Debian`, `-Ubuntu`,
`-Arch`, `-Kali`, `-Alpine`) remain **unchanged since P0**. See `ASSET_TRACKING.md`.

## Current Blockers

**No build blocker.** P1F4B is CLOSED / PASS. The 13 x86_64 **legacy** ELFs (`busybox`,
`busybox_static`, `dbclient`, `libc++_shared.so`, `libcrypto.so.1.1`, `libleveldb.so.1`,
`libtalloc.so.2`, `libtermux-auth.so`, `libutil.so`, `loader`, `proot`, `proot_meta`,
`proot_meta_leveldb`) are 4 KB aligned but are now packaged only under `assets/support/legacy/`;
the APK/AAB native-library set contains no 4 KB 64-bit ELF. Remaining P1F work is **P1F5**
(16 KB runtime/emulator acceptance, `adb shell getconf PAGE_SIZE` = 16384) and the modern lane
runtime equivalence (ashmem/memfd). Physical validation of edge-to-edge/back/IME/VNC geometry is
**P1G**. The local JaCoCo gate passes from the clean/report-only state; lint remains pre-existing
legacy debt (13 errors / 134 warnings / 3 hints).

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
    resolution selects `core` `1.3.9` and `android` `1.3.0`. Source compiles against the
    resolved versions.
  - **Sentry** `io.sentry:sentry-android:1.7.22` — ACTIVE; legacy API (`Sentry.init`,
    `AndroidSentryClientFactory`, `EventBuilder`) deeply coupled to production logging;
    dedicated migration.
  - **Billing** `com.android.billingclient:billing-ktx:3.0.3` — ACTIVE; upgrade requires
    API/source migration (Play Billing library policy is a later release concern).
  - **OkHttp** `3.14.7` + **Okio** `3.7.0` runtime validation, **Gson** `2.8.6`
    (rationalize/remove vs Moshi), **JArchiveLib** `0.8.0`,
    **SLF4J-nop** logging backend.
  - **UI support:** `androidx.constraintlayout:constraintlayout:1.1.3`; and
    `androidx.appcompat` / `androidx.fragment` / `androidx.recyclerview` are consumed
    directly but supplied transitively — explicit ownership hardening deferred.
  - **LocalBroadcastManager** `1.0.0` — deprecated technology; replacement deferred.
  - **Test stack:** JUnit `4.12`, Mockito `4.11.0` (+inline), mockito-kotlin `2.1.0`,
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
- **Resolved in P1E3/P1E3-R1:** the AGP 8 / Kotlin 2.2 implementation and JaCoCo unit-test
  execution-data path. AGP 8 writes the file under
  `build/outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec`; both report
  tasks now consume it. JaCoCo remains 0.8.8.
- **Resolved in P1E4:** the app now compiles against API 36 while targetSdk remains 30. API 36
  declares `PackageInfo.versionName` nullable; `AppsListFragment` preserves ProotX's existing
  non-null return invariant with `info.versionName!!`. No intentional runtime or UI change.
- **Resolved in P1E5:** `MainActivity` and `TermuxActivity` now declare `android:exported="true"`
  (TermuxActivity's `ssh://` BROWSABLE deep link preserved); the six application-owned
  `PendingIntent`s are explicitly immutable (the stop-sessions service intent retains
  `FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE`). targetSdk remains 30; the debug and androidTest
  manifests merge and build, and a disposable targetSdk 31 probe passed. No runtime or UI change.
- **Deferred after P1E5:** the `TermuxActivity` custom `com.termux.app.reload_style` receiver is
  classified as `RECEIVER_NOT_EXPORTED` for future targetSdk-34 work, but the explicit flag is
  **not** applied yet because `:terminal-term` still compiles against API 29 (the flag is only
  available from API 33/34). `LocalBroadcastManager` (in-process) and `MainActivity`'s
  system `DownloadManager` receiver are deliberately unchanged. The JaCoCo instrumented-class
  class-directory caveat is recorded above; lint debt (14 pre-existing errors, 131 warnings,
  3 hints — `Range`, `UseRequireInsteadOfGet`, and the intentional `ExpiredTargetSdkVersion`)
  remains deferred.
- **Resolved in P1E6:** the obsolete legacy broad-storage dependency is fully removed —
  `READ_EXTERNAL_STORAGE`/`WRITE_EXTERNAL_STORAGE` declarations deleted from the app manifest,
  `WRITE_EXTERNAL_STORAGE` deleted from the terminal-term manifest, `PermissionHandler.kt`
  deleted, and the app/session launch + SAF import/export permission gates removed. No
  replacement broad permission (`MANAGE_EXTERNAL_STORAGE`, `READ_MEDIA_*`) was added. A new
  `StoragePermissionGuardTest` statically forbids reintroduction. This is an intentional
  compatibility behavior change; app-scoped storage paths and the runtime data model are
  unchanged.
- **Resolved in P1E7:** both real foreground services are structurally ready for the Android
  12–16 FGS rules. `ServerService` and `TermuxService` declare
  `android:foregroundServiceType="specialUse"` plus `PROPERTY_SPECIAL_USE_FGS_SUBTYPE`
  (the terminal service via the API-36 app manifest overlay, so `:terminal-term` stays
  compileSdk 29), the app declares `FOREGROUND_SERVICE_SPECIAL_USE` alongside
  `FOREGROUND_SERVICE`, each service creates its own `"ProotX"` channel (`IMPORTANCE_LOW`),
  the initial session/terminal launches use `startForegroundService` on API 26+, and foreground
  promotion uses `ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST` on API 29+. A new
  `ForegroundServiceCompatibilityGuardTest` statically guards these invariants. No notification
  ID/action/PendingIntent, SSH, session-lifecycle, dependency, or SDK-level change.
- **Resolved in P1E8 (supersedes the P1E7 → P1E8 deferrals):** the app now targets SDK **34**.
  `POST_NOTIFICATIONS` is declared and requested contextually at the first real session start via
  one shared application-private prefs flag (`notification_permission`/`prompt_completed`); current
  grant state always comes from `checkSelfPermission`; denial never blocks the session and an
  explicit denial suppresses further automatic prompts. The initial `ServerService` foreground
  launch is deferred until the activity is resumed and catches only
  `ForegroundServiceStartNotAllowedException`, restoring the pending session and retrying on resume.
  `TermuxActivity` (direct `ssh://` entry) uses the same policy, and its application-internal
  `com.termux.app.reload_style` receiver is registered with the direct `Context.RECEIVER_NOT_EXPORTED`
  constant on API 33+ (legacy two-argument path retained). `:terminal-term` raises **only** its
  `compileSdk` to **36** (`targetSdk 29`/`minSdk 21`); `:terminal-view`/`:terminal-emulator` remain
  **29/29/21**. A test-only `app/src/androidTest/AndroidManifest.xml` supplies the `android:exported`
  values `androidx.test:core:1.2.0` omits, required by the target-31+ merger. New
  `TargetSdk34CompatibilityGuardTest`; the P1E7 `ForegroundServiceCompatibilityGuardTest` was
  updated in place (its durable FGS invariants kept, milestone assumptions advanced). Notification
  channel ID/IDs/actions, FGS types, PendingIntent mutability (0 mutable / 0 unspecified), SSH
  behavior, session lifecycle, Room schema, and UI are unchanged (`DECISIONS.md` D032). The system
  `DownloadManager` receiver correctly keeps its flag-less registration (system-broadcast exemption);
  the corresponding lint false positive is intentionally suppressed.
- **Resolved in P1E9:** the app's final `targetSdk` is **36**. `MainActivity` enables edge-to-edge
  and applies real `WindowInsetsCompat` per owner (toolbar top, bottom navigation bottom, root
  left/right cutout/navigation safety) without accumulating padding; `TermuxActivity` registers a
  platform `OnBackInvokedCallback` on API 33+ with the legacy `onBackPressed` fallback retained.
  The authorized `androidx.activity:activity-ktx:1.11.0` bridge keeps `minSdk 21` and wires
  `OnBackPressedDispatcher` to the platform `OnBackInvokedDispatcher`. There is no edge-to-edge or
  predictive-back opt-out, no orientation lock, no aspect-ratio restriction, and no large-screen
  opt-out; `TermuxActivity` keeps `fitsSystemWindows="true"` and stays `resizeableActivity="true"`.
  Navigation 2.3.5, AppCompat 1.1.0, Material 1.1.0, and Room 2.1.0 are unchanged. `MainActivity`
  was also adjusted for the Activity 1.11.0 non-null `onNewIntent`. Guards:
  `TargetSdk34CompatibilityGuardTest` was renamed/advanced to **`TargetSdk36CompatibilityGuardTest`**
  (durable P1E8 invariants kept) and a new **`TargetSdk36PlatformBehaviorGuardTest`** covers the
  no-opt-out/insets/back invariants (`DECISIONS.md` D033).
- **Resolved in P1F1:** the in-tree native toolchain now uses **NDK r29 `29.0.14206865`** for
  `:app` and `:terminal-emulator`, and CI selects the NDK through the Gradle module `ndkVersion`
  (the deprecated `ndk.dir` is no longer written). The packaged 64-bit `libtermux.so` carries
  `PT_LOAD` alignment `0x4000` for arm64-v8a and x86_64, and a scoped CI guard fails if that
  regresses. No source, `Android.mk`, linker, ABI, packaging, or `extractNativeLibs` change
  (`DECISIONS.md` D034).
- **Resolved in P1F2:** the support toolchain is now source-traceable and reproducible. The
  complete support ELF table was regenerated from the v1.0.0 binaries (108 files: 68 ELF, 40
  non-ELF; 17 16K-compatible / 51 4K-only), resolving the P1F-P arm32 erratum
  (`armeabi-v7a busybox_static` is `0x10000` and is the **only** arm32 ELF that is 16K-compatible).
  The unreproducible historical builder was removed and replaced by pinned inputs
  (builder `ghcr.io/termux/package-builder@sha256:374fedda…`, `termux-packages` `0ffca06c…`,
  checksum-verified `termux/proot v5.1.107.92`), a `provenance/sources.lock.json` source lock,
  `docs/PROVENANCE.md`, `docs/HISTORICAL_BUILDER.md`, `THIRD_PARTY_NOTICES.md`, deterministic
  archives, and support CI. The **dual-lane** model keeps `minSdk 21`: frozen legacy normal slots
  for host API 21–28 and a source-rebuilt modern `.a10` lane (API 24 / NDK r29, 16 KB aligned,
  `process_vm = yes`) for host API 29+, with `ProotXFiles` selection unchanged (`DECISIONS.md`
  D035). Two independent clean builds produced byte-identical modern binaries and byte-identical
  candidate archives. **Still deferred:** the 4 KB 64-bit legacy normal-slot ELFs and the
  unknown-provenance `proot_meta`/`proot_meta_leveldb` remain shipped through
  `jniLibs`/`nativeLibraryDir`; a P1F3/P1F4 strategy must resolve this before P1F closes. Google
  Play's applicable requirement: apps targeting **Android 15 / API 35+** must support **16 KB page
  sizes on 64-bit devices**; current Android documentation gives **February 1, 2027** as the
  update-enforcement date. **Full application 16 KB compatibility is not claimed.**
- **Resolved in P1F3:** the first provenance-backed support release **`v1.1.0`** is published
  (annotated tag `ff55608c…` → commit `acc28ab…`; release `RE_kwDOUXjkQM4XOeTw`, published
  2026-09-16T05:59:57Z). Assets: `arm64-v8a-assets.zip` `7f279264…`, `armeabi-v7a-assets.zip`
  `f7b935f6…`, `x86-assets.zip` `25a53c33…`, `x86_64-assets.zip` `f6248107…`, plus `SHA256SUMS`,
  `v1.1.0-provenance.json`, `v1.1.0.spdx.json`; all re-verified after download. Two independent
  clean four-ABI builds were byte-identical. Release CI is split (untrusted validation /
  privileged build / write-token publish with no rebuild) and all release actions are SHA-pinned
  (`DECISIONS.md` D036). **Still deferred to P1F4:** the 13 x86_64 legacy normal-slot 4 KB ELFs and
  the unknown-provenance `proot_meta`/`proot_meta_leveldb` remain shipped through
  `jniLibs`/`nativeLibraryDir`; ProotX still downloads `v1.0.0`; whole-app 16 KB compatibility is not
  claimed. Google Play's applicable requirement: apps targeting **Android 15 / API 35+** must
  support **16 KB page sizes on 64-bit devices**; current Android documentation gives
  **February 1, 2027** as the update-enforcement date.
- **Resolved in P1F4-P / P1F4A:** the **whole-app 16 KB preflight** (P1F4-P) was initially
  downgraded to `PARTIAL_BRIDGE` after the metadata fixture failed, then **recovered to
  `BRIDGE_FOUND`** by the R1 probe, which proved the frozen `proot_meta`/`proot_meta_leveldb` are
  the exact 2019 `CypherpunkArmory/proot` lineages (`2a7f6d9…` / `998dd31…`) and require the
  historical **`-DUSERLAND`** contract; runtime parity was then proven. **P1F4A** published
  **`v1.2.0`** (annotated tag object `2b5691c9…` → commit `889cb67…`; release `RE_kwDOUXjkQM4XTxeE`,
  2026-09-17T21:04:07Z), the first complete explicit dual-lane support release
  (`common/`+`legacy/`+`modern/`+`manifest.json`, plus `routing.json`). The legacy lane is the
  frozen v1.1.0/v1.0.0 payload verified against the per-file lock; the modern lane is source-built
  with NDK r29 at API 24 and every modern 64-bit ELF is `PT_LOAD >= 0x4000`. The modern static
  BusyBox 1.38.0 is reproducibly built (a `TZ=UTC` fix removed a build-timestamp timezone leak so
  two clean builds and the CI artifact are byte-identical). Final-release fixtures reproduce the
  frozen metadata behaviour with byte-identical parity, and there is no filesystem migration for
  `_meta`/`_meta_leveldb` sessions. `v1.0.0` and `v1.1.0` are untouched (`DECISIONS.md` D037).
  **Still deferred to P1F4B:** ProotX still downloads `v1.0.0`; the 13 x86_64 legacy 4 KB ELFs are
  still shipped through `jniLibs`/`nativeLibraryDir`; whole-app 16 KB compatibility is not claimed.
- **Resolved in P1F4B:** ProotX now consumes support **`v1.2.0`**. The flat pseudo-`.so` transport is
  removed: a single deterministic `prepareProotXSupport` task hash-verifies the pinned release and
  stages a generated payload (modern → `jniLibs/<abi>/lib_<name>.so`, common/legacy →
  `assets/support/...`, plus a `support-map.json` routing artifact). A manifest-driven installer
  extracts the frozen legacy payload on API 21–28 and links the modern payload from
  `nativeLibraryDir` on API 29+ (no writable-storage execution, Android W^X). The `.a10` inference
  and the `lib_arch.so` pseudo-native marker are gone. The APK/AAB native-library set is all-ELF,
  contains no legacy/common file, and every 64-bit `PT_LOAD >= 0x4000`; `zipalign -c -P 16` passes
  and bundletool reports `PAGE_ALIGNMENT_16K`. `minSdk` stays 21, all four ABIs are retained, and
  `extractNativeLibs="true"` is kept (`DECISIONS.md` D037). **Static acceptance only:** P1F5 owns
  the 16 KB runtime/emulator acceptance and P1G the physical-device acceptance; whole-app runtime
  16 KB compatibility is not claimed.
- **Deferred after P1E9 (non-blocking):** the Activity 1.11.0 bridge moved transitive selections
  (core/core-ktx 1.13.0, lifecycle 2.6.2, savedstate 1.2.1, coroutines 1.7.3, new
  `core-viewtree`/`tracing`/`profileinstaller`) and `androidx.core` injects the benign signature
  `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`; a future dependency-alignment milestone can
  rationalize the now-newer core/lifecycle/coroutines family. Physical validation of edge-to-edge
  insets, predictive back, IME, and VNC geometry belongs to **P1G**. NDK / 16 KB work is **P1F**.
- **Deferred after P1E8:** `:terminal-term` still declares the `specialUse` FGS type via the app
  manifest overlay rather than its own manifest (kept as the single app-owned policy source); a
  future cleanup may consolidate it now that the module compiles at API 36. The lint false positive
  suppression and the test-only androidx.test manifest overlay should be revisited when
  `androidx.test` is eventually upgraded (deferred dependency debt).
- **Deferred after P1E6:** the now-unreachable legacy permission-continuation machinery
  (`MainActivityViewModel.waitForPermissions`, `permissionsHaveBeenGranted`,
  `TooManySelectionsMadeWhenPermissionsGranted`, `NoSelectionsMadeWhenPermissionsGranted`, the
  related unit tests, and the unused `alert_permissions_necessary_*` strings) is intentionally
  classified as dead legacy follow-up and **not** removed in P1E6, because deleting it would
  expand the change into ViewModel state classes, localization resources, and multiple unrelated
  tests. The androidTest `GrantPermissionRule` for the removed permissions was dropped as a
  direct consequence of the manifest change.
- **Deferred after P1E3:** manifest `package` warnings; `JavaExec.main` → `mainClass` before
  Gradle 9; legacy Android DSL/`lintOptions` cleanup; `String.capitalize()`; configuration-time
  custom tasks; action/Node maintenance warnings; `ndk.dir`; OkHttp 3.14.7 + Okio 3.7.0
  runtime validation; eventual Room KAPT migration; and the core/core-ktx family note.
- **Correction:** Sentry and Billing are **ACTIVE** production dependencies (SentryLogger /
  Sentry; BillingManager / BillingClient / Purchase) — they are **not** unused and were left
  untouched.
- **Still open:** prebuilt rootfs profile remnant; network-dependent unit tests; dynamic time-based
  `versionCode`; LocalBroadcastManager modernization (deprecated tech; deferred, and classified
  above). The former "Play-readiness gaps (→ P1E)" item — legacy target SDK and missing
  exported/manifest declarations — is **RESOLVED**: P1E is CLOSED / PASS at targetSdk 36.

## Next Safe Action

**P1F5 — 16 KB RUNTIME / EMULATOR ACCEPTANCE — READY TO START.** It owns running the integrated
application on an Android 15/16 **16 KB page-size** environment (`adb shell getconf PAGE_SIZE` must
return `16384`), exercising real Linux sessions, and proving the modern lane executes correctly from
`nativeLibraryDir`. It must not begin without explicit authorization. **Full application 16 KB
compatibility must not be claimed** until P1F5 and **P1G** physical-device acceptance pass. ProotX
must **not** be called a Golden Candidate, release candidate, or store-ready final.
