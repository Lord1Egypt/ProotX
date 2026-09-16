# ProotX Project State

> Canonical, current state of ProotX. This file is updated before stopping any
> substantial engineering session. Always re-verify with Git — this is a
> point-in-time record, and verified repository state overrides stale docs.

Last updated: 2026-09-16 (P1E8 targetSdk 34 runtime compatibility — CLOSED / PASS; P1E IN PROGRESS)

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
| P1E2 — Moshi Codegen KAPT → KSP Migration | **CLOSED / PASS** |
| P1E3-P — AGP 8.10 / Kotlin 2.2 Compatibility Probe | **CLOSED / BRIDGE_FOUND** |
| P1E3 — AGP 8.10 / Kotlin 2.2 Implementation | **CLOSED / PASS** |
| P1E4 — compileSdk 36 Migration | **CLOSED / PASS** |
| P1E5 — API 31+ Manifest / PendingIntent / Receiver Compatibility | **CLOSED / PASS** |
| P1E6 — Storage / Permission Runtime Compatibility | **CLOSED / PASS** |
| P1E7 — Foreground Service / Notification Compatibility | **CLOSED / PASS** |
| P1E8 — targetSdk 33/34 Runtime Compatibility | **CLOSED / PASS** |
| P1D7-U — SwipeRefreshLayout Ownership + Material Retry | **CLOSED / SUPERSEDED BY P1D7-U2** |
| P1D7-U2 — Explicit Legacy Replacements + Material Final Retry | **CLOSED / PASS** |

## Current Milestone

**P1E8 — targetSdk 33/34 Runtime Compatibility: CLOSED / PASS.** Implementation commit
`cff25f3f1dffa1a91d78a55915dd50513b694af4` raises the app's persistent `targetSdk` **30 → 34**
(`compileSdk 36`, `minSdk 21`). The app declares `POST_NOTIFICATIONS` and requests it
**contextually at the first real session start** through one shared application-private prefs flag
(`notification_permission` / `prompt_completed`); current grant state always comes from
`checkSelfPermission`, and **denial never blocks the Linux session**. The initial `ServerService`
foreground launch is deferred until the activity is **resumed**, and catches only
`ForegroundServiceStartNotAllowedException`, restoring the pending session and retrying on the next
resume. `TermuxActivity` (direct `ssh://` entry) participates in the same one-time policy and its
application-internal `reload_style` receiver uses the direct `Context.RECEIVER_NOT_EXPORTED`
constant on API 33+ while the legacy path remains. `:terminal-term` raises **only** its `compileSdk`
**29 → 36**, keeping `targetSdk 29` / `minSdk 21`; `:terminal-view`/`:terminal-emulator` stay
**29/29/21**. A test-only `app/src/androidTest/AndroidManifest.xml` supplies the `android:exported`
values `androidx.test:core:1.2.0` omits, which the target-31+ merger requires. `POST_NOTIFICATIONS`
is present; legacy storage permissions remain absent. Notification channel ID/IDs/actions, FGS
types, SSH behavior, session lifecycle, and UI are unchanged. Local canonical evidence **40 suites /
348 tests / 0 failures / 0 errors / 0 skipped** (+1 guard suite, +11 tests); remote CI run
`35037714144` passed with the same summary plus androidTest APK and both artifact uploads. Next:
**P1E9 — TARGETSDK 35/36 PLATFORM BEHAVIOR — NOT STARTED / READY TO START**.

## Repository State

| Ref | SHA | Notes |
|---|---|---|
| Active branch | `feature/android-modernization` | |
| Accepted P1E8 implementation | `cff25f3f1dffa1a91d78a55915dd50513b694af4` | app targetSdk 30 → 34; `POST_NOTIFICATIONS` declared + contextual one-time request; resumed-lifecycle FGS gate + narrow `ForegroundServiceStartNotAllowedException` handling; Termux direct-entry policy + `Context.RECEIVER_NOT_EXPORTED`; terminal-term compileSdk 29 → 36 (targetSdk 29); test-only androidTest manifest overlay |
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
| Unit tests (current) | **348 tests / 40 suites / 0 failures / 0 errors / 0 skipped** |
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
| AndroidX Lifecycle (direct) | 2.2.0 (`lifecycle-viewmodel`, `lifecycle-livedata`) |
| Parcelize plugin | `kotlin-parcelize` (legacy `kotlin-android-extensions` removed) |
| JaCoCo | **0.8.8** (+ `jdk.internal.*` exclusion for JDK 17) |
| Mockito (test-only) | **4.11.0** (core + inline) |
| gradle-download-task | **5.0.0** |
| JDK for the Gradle build | **17** |
| `compileSdk` / `targetSdk` (app) | 36 / 34 |
| `minSdk` | 21 |
| `:terminal-term` `compileSdk` / `targetSdk` / `minSdk` | 36 / 29 / 21 |
| `:terminal-view` / `:terminal-emulator` `compileSdk` / `targetSdk` / `minSdk` | 29 / 29 / 21 |
| Android NDK | 21.4.7075529 (explicit `ndkVersion`) |
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
`platforms;android-29`, `build-tools;35.0.0`, `ndk;21.4.7075529`.

**CI-R1 note:** `android-actions/setup-android@v3` runs with `packages: ''` (its default
`tools platform-tools` install broke when the legacy `tools` package was retired);
`platform-tools` is owned explicitly by the pinned `sdkmanager` step. Triggers unchanged.

Verified remote evidence (P1E8):

| Field | Value |
|---|---|
| Run | `35037714144` (push, commit `cff25f3`) — **SUCCESS** |
| Log proof | JDK 17 (`17.0.20.1`); Gradle 8.11.1; API 36 + API 29, Build Tools 35.0.0 and NDK 21.4 installed; canonical clean build and `assembleDebugAndroidTest` passed |
| Remote test summary | `suites=40 tests=348 failures=0 errors=0 skipped=0` |
| Artifacts | `prootx-debug-apk` and `prootx-debug-androidTest-apk` uploaded |
| JaCoCo | Standard CI does not run the report task. Separate local P1E8 proof: `jacocoCoverageReportForCi` executed from the clean/report-only state and produced non-empty XML (788,568 B) plus HTML |

Prior remote evidence (P1E7): run `35032934977` — **SUCCESS**, `suites=39 tests=337 failures=0
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

## UI State

The legacy UI (XML/Views) remains **visually unchanged**. The Compose + Material 3 redesign
has not started.

## Assets State

All six ProotX asset repositories (`ProotX-Assets-Support`, `-Debian`, `-Ubuntu`,
`-Arch`, `-Kali`, `-Alpine`) remain **unchanged since P0**. See `ASSET_TRACKING.md`.

## Current Blockers

**None.** P1E8 is closed; remote CI is green (run `35037714144`). The local JaCoCo regression
gate passes when the AGP 8 instrumented-class directory is absent (see the JaCoCo tooling
caveat above); this is pre-existing and non-blocking. Lint remains pre-existing legacy debt (13
errors / 134 warnings / 3 hints: `Range` ×3, `UseRequireInsteadOfGet` ×10; no P1E8-specific
blocker — the system `DownloadManager` receiver's `UnspecifiedRegisterReceiverFlag` false positive
is intentionally suppressed per `DECISIONS.md` D032).

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
- **Still open:** prebuilt rootfs profile remnant; network-dependent unit tests; Play-readiness
  gaps (→ P1E); dynamic time-based `versionCode`; LocalBroadcastManager modernization
  (deprecated tech; deferred, and classified above).

## Next Safe Action

**P1E9 — TARGETSDK 35/36 PLATFORM BEHAVIOR — NOT STARTED / READY TO START.** It owns the targetSdk
35/36 raise, edge-to-edge, predictive back, large-screen orientation/resizability behavior, and the
final SDK-36 behavior regression. Do **not** start it without explicit authorization. NDK / 16 KB
page-size work remains **P1F**; physical-device acceptance remains **P1G**.
