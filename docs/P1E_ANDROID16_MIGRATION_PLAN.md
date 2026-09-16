# ProotX — P1E Android 16 (SDK 36) Migration Plan

> **Status:** P1E9 implementation CLOSED / PASS; **P1E is CLOSED / PASS**. This document is the
> approved migration design and implementation record. Current state lives in
> `PROJECT_STATE.md`. Do not execute a later step without explicit authorization.

Current accepted implementation: `8103b835a670638c177a7adc6d7baea19680cd33`, remote CI
`35049015538` green, 42 suites / 360 tests / 0 failures / 0 errors / 0 skipped. Frozen
application baseline remains unchanged. **This P1E plan is complete (P1E CLOSED / PASS).** The
current program phase is **P1F — MODERN NDK / 16 KB PAGE-SIZE COMPATIBILITY: IN PROGRESS**; P1F-P is
CLOSED / PARTIAL_BRIDGE, P1F1 (in-tree NDK r29) is CLOSED / PASS, P1F2 (support
toolchain/provenance) is CLOSED / PASS, and P1F3 (support bundle publication `v1.1.0`) is
CLOSED / PASS, with P1F4 (support packaging / whole-app 16 KB integration) READY TO START. See `PROJECT_STATE.md` and `DECISIONS.md` D034/D035/D036 for the 16 KB scope boundary
(full application 16 KB compatibility is not yet claimed). P1G physical acceptance remains after
P1F.

---

## 1. Current state (verified)

### 1.1 Module SDK matrix

| Module | compileSdk | targetSdk | minSdk |
|---|---|---|---|
| `:app` | 36 | 36 | 21 |
| `:terminal-term` | 36 | 29 | 21 |
| `:terminal-view` | 29 | 29 | 21 |
| `:terminal-emulator` | 29 | 29 | 21 |

### 1.2 Build toolchain

Gradle `8.11.1` · AGP `8.10.1` · Kotlin (KGP/stdlib) `2.2.20` · KSP2
`2.2.20-2.0.4` · build-tools `35.0.0` · NDK `21.4.7075529` · app build and sdkmanager on
**JDK 17**. Repositories: `google()` + `mavenCentral()`.

### 1.3 Plugins / classpath (root `build.gradle`)

| Plugin | Version | P1E disposition |
|---|---|---|
| `com.android.tools.build:gradle` | 8.10.1 | Persisted in P1E3 |
| `org.jetbrains.kotlin:kotlin-gradle-plugin` | 2.2.20 | Persisted in P1E3 |
| `androidx.navigation:navigation-safe-args-gradle-plugin` | 2.3.5 | Retained; generation passes |
| `org.jacoco:org.jacoco.core` (+ `jacoco { toolVersion 0.8.8 }`) | 0.8.8 | Retained; report gate passes |
| `de.undercouch:gradle-download-task` | 5.0.0 | Retained; execution gate passes |
| ktlint (`com.pinterest:ktlint:0.32.0` via `JavaExec`) | 0.32.0 | Works as external CLI, but `main =` → `mainClass`; optional bump |

---

## 2. Official API 36 toolchain requirements

Sourced from `developer.android.com/build/releases/past-releases`:

| AGP | Max API level | Min Gradle | Default Build Tools | NDK default | Min JDK |
|---|---|---|---|---|---|
| 8.9 | **35** | 8.11.1 | 35.0.0 | 27.0.12077973 | 17 |
| **8.10** | **36** | **8.11.1** | **35.0.0** | 27.0.12077973 | **17** |
| 8.11 | 36 | 8.13 | 35.0.0 | 27.0.12077973 | 17 |

**Finding (corrected in P1E1-P):** official Android tooling documentation states the
**minimum AGP for API 36 is 8.9.1**, not 8.10. AGP 8.9's own release notes cap at API 35, so
the exact floor is version-sensitive. The ProotX **final target remains AGP 8.10.x** because
AGP 8.10 explicitly documents API-36 support with the Gradle 8.11.1 / Build Tools 35.0.0 /
JDK 17 baseline; that target is unchanged by the correction. AGP 8.11 also supports API 36 but
requires the newer Gradle 8.13 (unnecessary churn).

### 2.1 Kotlin Gradle Plugin compatibility (kotlinlang.org)

| KGP | Gradle (min–max) | AGP (min–max) |
|---|---|---|
| 2.2.0–2.2.10 | 7.6.3–8.14 | 7.3.1–**8.10.0** |
| 2.2.20–2.2.21 | 7.6.3–8.14 | 7.3.1–8.11.1 |
| 2.1.0–2.1.10 | 7.6.3–8.10 | 7.3.1–8.7.2 |
| 2.0.20–2.0.21 | 6.8.3–8.8 | 7.1.3–8.5 |
| 1.9.20–1.9.25 | 6.8.3–8.1.1 | 4.2.2–8.1.0 |
| 1.9.0–1.9.10 | 6.8.3–**7.6.0** | 4.2.2–**7.4.0** |

**Findings:**
- Kotlin `1.4.32` (KGP) **cannot** run on Gradle 8.x or with AGP 8.x — it is far outside
  every supported range. A Kotlin upgrade is mandatory.
- The **minimum KGP for AGP 8.10 is 2.2.0**. Recommended: **KGP 2.2.20** (supports AGP up to
  8.11.1, giving patch headroom).
- **KGP 1.9.20–1.9.25 supports Gradle 6.8.3–8.1.1 and AGP 4.2.2–8.1.0** (corrected in P1E1-P;
  the earlier "1.9.10 is the highest for AGP 7.4" note was imprecise). The recommended bridge
  is **KGP 1.9.25**. This makes an **AGP 7.4 / Gradle 7.6 / Kotlin 1.9.25** intermediate
  station possible.

---

## 3. Recommended target stack (PART X)

| Component | Recommended |
|---|---|
| Gradle | **8.11.1** (exact minimum for AGP 8.10) |
| AGP | **8.10.x** |
| Kotlin (KGP) | **2.2.x** (2.2.20 for patch headroom) |
| JDK (Gradle build) | **17** |
| Build Tools | **35.0.0** (AGP 8.10 default); 36.0.0 optional at compileSdk 36 |
| compileSdk | **36** |
| targetSdk | **36** (reached last) |
| minSdk | **21** (unchanged) |
| NDK | **21.4.7075529 preserved through P1E if AGP 8.10 accepts it — see §9** |

---

## 4. Kotlin / codegen compatibility implications

A Kotlin 2.2 upgrade pulls a coordinated codegen set. Nothing is upgraded in P1E0.

| Concern | Finding | Action |
|---|---|---|
| **Moshi** | `1.9.3` is pinned as the verified Kotlin 1.4 bridge (`D013`/`D014`). Moshi codegen (kapt) will not parse Kotlin 2.x metadata. Moshi **1.15.2** KAPT is verified for **Kotlin 1.9** (P1E1-P) but is **not** a Kotlin-2.x KAPT path — it emits a KAPT-deprecation warning and must migrate (KAPT → KSP) before the Kotlin 2.2 jump. | Move Moshi to **1.15.2** with the Kotlin 1.9 bridge; plan a **KSP migration milestone before Kotlin 2.2**. |
| **kapt** | Supported in Kotlin 2.2, but move to KSP is *not* required for P1E. | Keep kapt; no source change. |
| **Parcelize** | `kotlin-parcelize` supports Kotlin 2.2; `kotlinx.parcelize.Parcelize` usage unchanged. | No source change expected. |
| **ViewBinding** | Generated by AGP; unaffected by Kotlin version. | No change. |
| **Room 2.1.0 kapt** | Old Room 2.1.0 compiler may not run under Kotlin 2.2/kapt. | Room will likely need upgrading in the bridge milestone. |
| **Navigation Safe Args** | Plugin `2.1.0` is incompatible with Gradle 7+/8. Generated `*Args` classes used by 3 fragments. | Upgrade Navigation (runtime + safe-args) to a modern 2.7.x/2.8.x line. |
| **Coroutines 1.0.0 (declared)** | Resolves to core 1.3.9 / android 1.1.1. Old but compiles; a Kotlin 2.2 build may still accept the resolved versions. | Validate; coroutines modernization remains deferred debt. |
| **Mockito-Kotlin 2.1.0 / Mockito 2.23.0** | Test-only; bytecode-level, generally tolerant of Kotlin 2.2 compiled tests. | Validate; likely no change. |

**Conclusion:** the Kotlin jump is coupled to Moshi (and probably Room + Navigation). These
must land together in the bridge milestones, not independently.

---

## 5. AGP 8 DSL / build-script breakage inventory

Applies to all four modules unless noted.

| Item | Current | AGP 8 behavior | Required |
|---|---|---|---|
| `namespace` | absent; `package` in manifest | **Required** DSL property | Add `namespace` to all 4 modules |
| manifest `package` attribute | present in all 4 manifests | **Rejected/ignored** | Remove from all 4 manifests |
| `BuildConfig` | generated + 3 `buildConfigField`s + `BuildConfig.VERSION_NAME` | generation **off** by default | `buildFeatures { buildConfig true }` in `:app` |
| non-transitive R | `android.nonTransitiveRClass` default false | default **true** | Verified safe — see §6 |
| non-final res IDs | default false | default **true** | No `switch(R.id.*)` found — safe |
| R8 full mode | off | default **on** | `minifyEnabled false` in debug/release/beta — no immediate effect |
| `lintOptions` | used twice | renamed `lint` | Rename to `lint {}` |
| `compileSdkVersion` / `targetSdkVersion` / `minSdkVersion` | method form | deprecated | Move to `compileSdk` / `targetSdk` / `minSdk` |
| `testOptions.execution` | `ANDROIDX_TEST_ORCHESTRATOR` | supported | No change |
| `buildTypes.debug.testCoverageEnabled` | `true` | supported | No change (JaCoCo) |
| `sourceSets.androidTest.assets` | used | supported | No change |
| `javaCompileOptions.annotationProcessorOptions` | room schema location | supported | No change |
| `externalNativeBuild.ndkBuild` | used (`terminal-emulator`) | supported | Validate NDK (§9) |
| `ndk { abiFilters }` | `terminal-emulator` | supported | No change |
| `testNamespace` | absent | may be required (androidTest source is in app namespace) | Add/verify if AGP requires |
| custom tasks | see §5.1 | Gradle 7/8 API changes | Migrate |

### 5.1 Custom Gradle task / config migration

| Task | Issue | Required |
|---|---|---|
| `reportVersionCode`, `reportVersionName` | file write happens at **configuration time** inside the task body; breaks configuration cache | Move write into `doLast`; wire `preBuild.dependsOn` |
| `ktlint`, `ktlintFormat` | `JavaExec.main =` removed | `mainClass =` |
| `jacocoCoverageReport`, `jacocoCoverageReportForCi` | `reports { xml.enabled }` deprecated | `xml.required` |
| `downloadAssets(type: Download)` | `gradle-download-task` 3.4.3 incompatible | upgrade plugin to 5.x or replace |
| `downloadAssets` dest `buildDir` | `buildDir` deprecated | `layout.buildDirectory` |
| `checkIfAssetsMissing` | reads `new File(...).listFiles()` at configuration time | wrap in `doLast`/provider |
| `jacoco { toolVersion = 0.8.4 }` | too old for JDK 17 | bump ≥ 0.8.11 |
| `task testAll(dependsOn: ['test','connectedAndroidTest'])` | fine | no change |

---

## 6. Namespace plan (PART G)

| Module | Manifest `package` (current) | Java/Kotlin package | Proposed `namespace` |
|---|---|---|---|
| `:app` | `io.github.lord1egypt.prootx` | `io.github.lord1egypt.prootx` | `io.github.lord1egypt.prootx` |
| `:terminal-term` | `com.termux` | `com.termux.app` | `com.termux` |
| `:terminal-view` | `com.termux.view` | `com.termux.view` | `com.termux.view` |
| `:terminal-emulator` | `com.termux.terminal` | `com.termux.terminal` | `com.termux.terminal` |

- `applicationId` stays `io.github.lord1egypt.prootx` (app-only, unchanged).
- Removing the manifest `package` attribute is **required**; namespaces preserve existing
  `R`/`BuildConfig` references. The app manifest's `tools:replace` set is unaffected.
- `testNamespace`: androidTest sources live in `io.github.lord1egypt.prootx.*` and import
  `io.github.lord1egypt.prootx.R`. If AGP 8 requires a distinct test namespace, set it and
  verify the test R/import still resolve.

### 6.1 Non-transitive R / resource ownership (PART H)

Evidence:
- No module imports another module's `R`:
  - `:app` has **zero** `com.termux.*` references (verified) — it does not use the terminal
    library's code at all (see §11).
  - `:terminal-term` imports only `com.termux.R`; `:terminal-view` only its own `R`; no
    `com.termux.view.R`/`com.termux.terminal.R` cross-references exist.
- `@+id/terminal_view`, `@+id/drawer_layout`, etc. are defined in `:terminal-term`'s own
  layouts; `terminal-view` strings (`copy_text`, `paste_text`, …) are defined in
  `:terminal-view` and referenced only by `:terminal-view`.

**Finding:** enabling non-transitive R (the AGP 8 default) is expected to be **safe**; no
shared-resource fix is required. Validation belongs to P1E2. Do **not** globally disable the
modern default as a shortcut.

---

## 7. Android 12 / API 31 manifest audit (PART J)

Merged components with intent filters:

| Component | Filter | Current `exported` | Required at target ≥31 |
|---|---|---|---|
| `io.github.lord1egypt.prootx.MainActivity` | MAIN/LAUNCHER (+ VIEW) | **absent** | `android:exported="true"` (launcher) |
| `com.termux.app.TermuxActivity` | VIEW/BROWSABLE `ssh` | **absent** | Decide from real use case |
| `com.termux.app.TermuxService` | none | `false` | none |
| `io.github.lord1egypt.prootx.ServerService` | none | absent (defaults false) | add explicit `false` for clarity |
| `androidx.room.MultiInstanceInvalidationService` | none | `false` | none |
| `com.android.billingclient.api.ProxyBillingActivity` | none | `false` | none |
| `io.github.lord1egypt.prootx.provider.ProotXDocProvider` | DOCUMENTS_PROVIDER | `true` | keep `true` |

**MainActivity:** `exported="true"`.
**TermuxActivity:** The app's own code never launches it (see §11); the only current trigger
is an external `ssh://` BROWSABLE intent. Recommendation: **`exported="true"` only if the
external SSH-intent entry point is an intended product feature; otherwise `false` (or remove
the filter / the module).** This is a product decision for P1E5; P1E0 does not change it.

---

## 8. PendingIntent audit (PART K)

| File | Call | Purpose | Current flags | Required |
|---|---|---|---|---|
| `NotificationConstructor.kt:46` | `getActivity` | open session list | `0` | `FLAG_IMMUTABLE` |
| `NotificationConstructor.kt:50` | `getService` | stop all sessions | `FLAG_UPDATE_CURRENT` | `FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE` |
| `NotificationConstructor.kt:55` | `getActivity` | open settings | `0` | `FLAG_IMMUTABLE` |
| `TermuxService.java:182` | `getActivity` | open terminal | `0` | `FLAG_IMMUTABLE` |
| `TermuxService.java:218` | `getService` | exit service | `0` | `FLAG_IMMUTABLE` |
| `TermuxService.java:226` | `getService` | toggle wake lock | `0` | `FLAG_IMMUTABLE` |

**Finding:** on API 31+ (enforced at target ≥31) a mutable PendingIntent requires an explicit
mutability flag; no consumer mutates the underlying Intent, so **`FLAG_IMMUTABLE` is correct
for all six**. All must be updated before targetSdk reaches 31 (planned in P1E5 before the
later targetSdk milestones).

---

## 9. Foreground service audit (PART L/M) + NDK boundary

| Service | Declared type | Start path | Work | Notification | Runs without UI | FGS perms |
|---|---|---|---|---|---|---|
| `io.github.lord1egypt.prootx.ServerService` | **`specialUse`** | `startForegroundService` (API 26+) from `MainActivity.startSession()`; `startService` for already-running commands from `MainActivity.restartRunningSession` / `SessionListFragment` / `FilesystemListFragment` / `AppsListFragment`; `getService` PendingIntent ("stopAll") | starts PRoot server via `LocalServerManager`, holds sessions | `startForeground(id 1000, persistent, FOREGROUND_SERVICE_TYPE_MANIFEST)` | yes (`stopWithTask=true`, `START_STICKY`) | `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_SPECIAL_USE` |
| `com.termux.app.TermuxService` | **`specialUse`** (API-36 app manifest overlay) | `startForegroundService` (API 26+) + `bindService` from `TermuxActivity` | terminal sessions, wake/wifi lock | `startForeground(id 2000, ..., FOREGROUND_SERVICE_TYPE_MANIFEST)` in `onCreate` | yes | `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_SPECIAL_USE` |

- At target 34+, a foreground service **must declare `android:foregroundServiceType`** and hold
  the matching `FOREGROUND_SERVICE_*` permission; `foregroundServiceType` is also required in
  the merged manifest for the provider of the type.
- **Recommended type for both: `specialUse`**, with
  `android.permission.FOREGROUND_SERVICE_SPECIAL_USE` and
  `android:foregroundServiceType="specialUse"` plus
  `<property android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE" android:value="…"/>`
  describing "persistent Linux (PRoot) session / remote terminal server".
  `dataSync` is **not** appropriate (wrong semantics, subject to Android 15 timeouts).
- **Implemented in P1E7.** `ServerService` and `TermuxService` declare `specialUse`, the app and
  terminal module hold `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_SPECIAL_USE`, and the initial
  launch paths use `startForegroundService()` on API 26+. The notification action "stopAll" was
  reviewed and kept as an already-running-service command (`startService` semantics via
  `PendingIntent.getService`), not converted to a foreground launch.
- **P1E7 outcome:** the app now declares `FOREGROUND_SERVICE_SPECIAL_USE` and both services have a
  `foregroundServiceType`. See §25 for the implementation result.
- **NDK / P1F boundary:** the AGP 8.10 default NDK is `27.0.12077973`; the project pins
  `21.4.7075529`. AGP 8.10 may warn/require a newer NDK for `ndkBuild`. Whether NDK 21.4
  remains buildable under AGP 8.10 **must be validated in P1E2**. If it is rejected, NDK
  modernization (and 16 KB page alignment) is pulled forward from P1F — a **cross-phase
  blocker to report, not to silently absorb.**

### 9.1 FGS start restrictions (PART M)

- `ServerService` starts only from a user action in a foreground activity → allowed.
- `autoStart()` runs in `MainActivity.onCreate`/`onNewIntent` and can immediately start a
  session; this is foreground-driven, but the `onNewIntent` path (external intent while
  backgrounded) is a potential `ForegroundServiceStartNotAllowedException` risk on API 31+.
- Terminal `TermuxService` is started/`bindService`d from its activity → allowed.
- **Implemented in P1E8 (supersedes the earlier P1E6/P1E7 attribution):** the initial
  `ServerService` foreground launch is deferred until the activity is
  `Lifecycle.State.RESUMED` (AndroidX Lifecycle already present), the pending `Session` is
  retained, and `launchForegroundService` catches **only**
  `ForegroundServiceStartNotAllowedException`, records a diagnostic breadcrumb, restores the
  pending session, and retries on the next resume. `onCreate → autoStart` and
  `onNewIntent → autoStart` both flow through this single final gate; the autoStart `finish()`
  still runs only after a successful launch.

---

## 10. Notifications / storage / visibility / receivers

### 10.1 Notifications (PART N)
- Channel id `"ProotX"`, importance `LOW`, created by **each service itself** (`ServerService`
  and `TermuxService`), so neither depends on `MainActivity` having run first.
- Android 13+ (target 33+): `POST_NOTIFICATIONS` is required for the user to **see** the
  notifications. The FGS itself can run without it, but visibility/first-run UX is affected.
- **Corrected sequencing (P1E7):** `POST_NOTIFICATIONS` is **not** declared or requested while
  targetSdk is 30. An app targeting API ≤ 32 does not control the notification-permission dialog
  timing the way a target-33+ app does, so declaring it early could surface premature,
  system-timed first-run UX. P1E7 owns FGS structural compatibility, service-owned channels, and
  immediate foreground promotion; **P1E8** owns the declaration, the runtime request, and the
  permission UX together with the targetSdk 33/34 raise (see `DECISIONS.md` D031). This
  supersedes the earlier "request it on first session start (P1E7)" wording.
- **Implemented in P1E8:** the app targets 34, declares `POST_NOTIFICATIONS`, and requests it
  contextually at the first real session start through one shared application-private
  `notification_permission` / `prompt_completed` preference; current grant state always comes from
  `checkSelfPermission`; denial never blocks the session and an explicit denial suppresses later
  automatic prompts. `TermuxActivity`'s direct `ssh://` entry uses the same policy. See §26
  (`DECISIONS.md` D032).

### 10.2 Storage (PART O) — HIGH priority
- Only app-private/app-scoped storage is used:
  `context.filesDir`, `context.getExternalFilesDir(null)`,
  `context.getExternalFilesDirs(null)` (`ProotXFiles`), plus `DownloadManager` downloads
  into app storage. **No `Environment.getExternalStorageDirectory()` / public/shared path is
  used.**
- `PermissionHandler` gates **every app/session launch** on `READ_EXTERNAL_STORAGE` **and**
  `WRITE_EXTERNAL_STORAGE`. On target 33+, `READ_EXTERNAL_STORAGE` is a no-op for non-media
  and is **not grantable**, so this gate will deny session launch permanently.
- Equivalent legacy request exists in `TermuxActivity` (`WRITE_EXTERNAL_STORAGE,
  REQUESTCODE_PERMISSION_STORAGE`).
- **Finding:** the storage permissions are **not required by the current runtime**. P1E6 must
  remove/replace the gate (and the manifest `READ/WRITE_EXTERNAL_STORAGE` declarations) so
  targetSdk 33+ does not block session launch. This is a behavior change and requires its own
  milestone + regression validation.

### 10.3 Package visibility (PART P)
- `<queries>` covers `VIEW` with `vnc`, `x11`, `ssh`.
- Real external interactions: `ssh://` (startActivity — also matched by the app's own
  `TermuxActivity`), `vnc://` and `x11://` via `queryIntentActivities` (needs visibility),
  `market://`/`https://play.google.com` via `startActivity` (no visibility required).
- **Finding:** no additional `<queries>` needed for current behavior. The `ssh` query is
  arguably unnecessary; the `x11` query's action-bearing filter may not match the
  action-less XSDL intent — record as a verification item, do not broaden queries blindly.

### 10.4 Dynamic receivers (PART Q)
| Registration | Broadcast | Kind | API 34+ rule |
|---|---|---|---|
| `MainActivity` `downloadBroadcastReceiver` | `DownloadManager.ACTION_DOWNLOAD_COMPLETE` | system | **CORRECTED (P1E8): keeps the flag-less platform registration.** Android 14 exempts receivers registered only for system broadcasts from the exported/not-exported flag requirement; do **not** add `RECEIVER_NOT_EXPORTED`/`ContextCompat` flags here. The `UnspecifiedRegisterReceiverFlag` lint false positive is intentionally suppressed with justification. |
| `MainActivity` server-result | `LocalBroadcastManager` | in-process | unaffected |
| `TermuxActivity` `mBroadcastReceiever` | custom `com.termux.app.reload_style` | **custom** | **target 34+ requires explicit exported flag** → `RECEIVER_NOT_EXPORTED` (implemented in P1E8 using the direct `Context.RECEIVER_NOT_EXPORTED` constant on API 33+, legacy path below) |

---

## 11. Key structural finding — `:terminal-term` is unused by app code

- `:app` contains **zero** references to `com.termux.*` (verified). `implementation
  project(':terminal-term')` is declared but no app code calls `TermuxActivity`/
  `TermuxService`/`TerminalView`.
- The dependency's only observable effects are: (a) manifest merge of `TermuxActivity` +
  `TermuxService`, (b) runtime transitive `androidx.legacy` (already recorded in P1D),
  (c) the `ssh://` external entry point.
- The library's `android:sharedUserId="com.termux"` **does not appear in the app's merged
  manifest nor the shipped APK** (verified with `aapt dump xmltree`), so ProotX does **not**
  run under a shared UID today; the attribute is effectively inert.
- **Recommendation:** before/within P1E, decide whether the embedded terminal is a product
  feature. Removing the dependency (a dedicated, explicitly authorized cleanup) would
  eliminate the `TermuxService` FGS, the custom-receiver API-34 issue, the sharedUserId debt,
  and the terminal `WRITE_EXTERNAL_STORAGE` request at once. P1E0 does **not** act on this.

---

## 12. Platform behavior audits

### 12.1 Android 14 / API 34 (PART R)
| Requirement | Classification |
|---|---|
| FGS types required | **APPLICABLE** — `ServerService`, `TermuxService` need types (§9) |
| FGS type permissions | **APPLICABLE** |
| Dynamic receiver export flags | **APPLICABLE** — `TermuxActivity` custom receiver (§10.4) |
| Background FGS start restrictions | **APPLICABLE** — `autoStart`/`onNewIntent` (§9.1) |
| Partial media access | NOT APPLICABLE (no media access) |
| Non-SDK restrictions | REQUIRES DEVICE TEST (native/PRoot) |

### 12.2 Android 15 / API 35 (PART S)
| Item | Classification |
|---|---|
| Edge-to-edge enforced by default | **APPLICABLE** — legacy XML/View UI; `drawer_layout.xml` uses `fitsSystemWindows="true"` |
| `dataSync` FGS timeout | NOT APPLICABLE (we do not use `dataSync`) |
| PendingIntent background-activity-launch blocked | **APPLICABLE** — review notification PendingIntents |
| Large-screen behavior | REQUIRES DEVICE TEST |

### 12.3 Android 16 / API 36 (PART T)
| Item | Classification |
|---|---|
| Mandatory edge-to-edge (opt-out removed) | **APPLICABLE** — status-bar/system-bar handling must be validated |
| Predictive back (`onBackPressed` no longer called at target 36) | **APPLICABLE** — `TermuxActivity.onBackPressed()` (drawer close/finish) must migrate to `OnBackPressedCallback`; app's `MainActivity` does not override Back |
| Large-screen orientation/resizability/aspect-ratio ignored | **APPLICABLE (large screens)** — no `screenOrientation` locks exist; `resizeableActivity="true"` on `TermuxActivity`; VNC geometry computation (`DeviceDimensions`, `FORCE_PORTRAIT_GEOMETRY=true`) needs device validation |
| Intent matching / receiver flags | **APPLICABLE** (receiver export flags, §10.4) |
| Local Network Permission (roadmap) | **REQUIRES DEVICE TEST / FUTURE** — app connects to `localhost` only; LAN feature is roadmap-only |

**Predictive back & edge-to-edge implications:** the terminal UI and the navigation host
should be validated on a device; no UI redesign is authorized in P1E.

---

## 13. sharedUserId & manifest debt (PART U/V)

| Attribute | Location | Finding | P1E action |
|---|---|---|---|
| `android:sharedUserId="com.termux"` | `terminal-term` manifest | **Not present in app merged manifest/APK** — inert | Remove during manifest cleanup (P1E2) |
| `android:sharedUserLabel` | `terminal-term` manifest | tied to sharedUserId | Remove with it |
| `WRITE_EXTERNAL_STORAGE` | app + terminal-term | not needed by runtime | remove in P1E6 |
| `READ_EXTERNAL_STORAGE` | app | not grantable at target 33+ | remove + fix handler in P1E6 |
| `allowBackup` | app `false` (tools:replace) | effective value false | keep |
| `fullBackupContent` | terminal-term `@xml/backupscheme` | overridden by `allowBackup=false` | keep/cleanup |
| `extractNativeLibs="true"` | app + terminal-term | **required** (PRoot execs `.so` from nativeLibraryDir) | keep |
| `installLocation="internalOnly"` | app + terminal-term | fine | keep |
| `android:max_aspect` | app + terminal-term | legacy Samsung hint; harmless | keep or drop (cosmetic) |
| `android:resizeableActivity="true"` | terminal-term | API 36 ignores on large screens | validate |
| `FOREGROUND_SERVICE` perm | app has it; terminal-term has it | needed; types/perms missing | **added in P1E7** (`FOREGROUND_SERVICE_SPECIAL_USE` + `specialUse` type for both services) |
| `VIBRATE` | merged from terminal-term | unused by app | optional cleanup |

---

## 14. CI / JDK migration plan (PART Y)

- The two-stage JDK model exists because `sdkmanager` needs JDK 17 while Gradle 6.7.1/AGP 4.2.2
  needs JDK 8.
- AGP 7.4 requires **JDK 11+**; AGP 8.10 requires **JDK 17**. Therefore the JDK 8 build stage
  ends at **P1E1**, not P1E2.
- Plan: at **P1E1**, switch the Gradle stage to JDK 17 (or JDK 11 if AGP 7.4 needs it) and
  collapse the bootstrap to a single JDK 17 stage, while **keeping the explicit pinned
  `sdkmanager` package install** (`platform-tools`, platforms, build-tools, NDK).
- Package changes: `build-tools;35.0.0` at **P1E3**; `platforms;android-36` at **P1E4**.
- No CI edit is authorized in P1E0.

---

## 15. Approved migration sequence (revised after P1E1-P)

> One independently testable risk category per milestone. Target SDK rises **last**. The
> Kotlin 2.x codegen problem is now its own milestone (P1E2), because Moshi 1.15.2 KAPT is a
> Kotlin 1.9-only bridge and cannot carry into Kotlin 2.2.

| Milestone | Scope | Risk category | Exit gate |
|---|---|---|---|
| **P1E1** | **Proven AGP 7 / Kotlin 1.9 bridge implementation**: Gradle **7.6.4**, AGP **7.4.2**, Kotlin/KGP **1.9.25**, Moshi **1.15.2** (+KAPT), Navigation **2.3.5**, JaCoCo **0.8.8** (+`jdk.internal.*` exclusion), test-only Mockito **4.11.0**, JDK **17**, explicit `ndkVersion 21.4.7075529`; compileSdk/targetSdk still **30**; plus the behavior-neutral source fixes below | Kotlin/kapt/codegen/JDK bridge | Build + `326/36` tests green locally + remotely; runtime/UI unchanged |
| **P1E2** | **Kotlin 2.x codegen readiness**: migrate Moshi codegen **KAPT → KSP** (Room may stay KAPT); verify no KAPT-only codegen remains before Kotlin 2.2 | Codegen toolchain | Build + tests green on Kotlin 1.9 with KSP |
| **P1E3** | Gradle **8.11.1**, AGP **8.10.1**, Kotlin **2.2.20**, KSP **2.2.20-2.0.4**, JDK **17**, build-tools **35.0.0**; four namespaces, app-only `buildConfig true`, JaCoCo Gradle-8 DSL/path fixes, non-transitive R ownership, Kotlin 2 source API substitutions; compileSdk/targetSdk still **30** | Build-system API change | **CLOSED / PASS**; build + 327 tests green; NDK 21.4 validated |
| **P1E4** | app-only `compileSdk 36`, targetSdk stays **30**, terminal modules stay **29/29/21**; CI installs `platforms;android-36`; one API-36 nullability source-contract edit | Platform compile | **CLOSED / PASS**; build + 327 tests green; no intentional behavior change |
| **P1E5** | API 31 manifest/intent: `android:exported` (`MainActivity` `true`; `TermuxActivity` `true`, `ssh://` preserved), PendingIntent `FLAG_IMMUTABLE` (6×), dynamic receiver export flags classified/deferred | Manifest/intent | **CLOSED / PASS**; build + 327 tests green; androidTest build; disposable targetSdk 31 probe passed then reverted |
| **P1E6** | Storage/permission runtime: remove/repair `PermissionHandler` gate + storage permissions + terminal request | Runtime permission | **CLOSED / PASS**; app/session/SAF flows work without storage perms; 329 tests green; disposable targetSdk 33 probe passed then reverted |
| **P1E7** | FGS structural compatibility: `foregroundServiceType="specialUse"` + `FOREGROUND_SERVICE_SPECIAL_USE` + subtype property for both services; service-owned channels; initial launch via `startForegroundService`; immediate `ServerService` promotion; `FOREGROUND_SERVICE_TYPE_MANIFEST` on API 29+. **`POST_NOTIFICATIONS` intentionally deferred** (see §10.1) | FGS/notification | **CLOSED / PASS**; build + 337 tests green; disposable targetSdk 34 probe passed then reverted; FGS declarations validated in merged manifest + APK |
| **P1E8** | targetSdk **33/34** runtime: app targetSdk 30 → 34; `POST_NOTIFICATIONS` declaration + contextual one-time runtime request (denial never blocks the session); targetSdk 31+ deferred/resumed FGS start with narrow `ForegroundServiceStartNotAllowedException` handling; `TermuxActivity` custom receiver `RECEIVER_NOT_EXPORTED`; `:terminal-term` compileSdk 29 → 36 (targetSdk 29) | Target behavior | **CLOSED / PASS**; target-33 checkpoint passed; build + 348 tests green; disposable target-34 declarations validated in merged manifest + APK |
| **P1E9** | targetSdk **35/36** behavior: app targetSdk 34 → 36 after a target-35 checkpoint; real edge-to-edge with per-owner `WindowInsetsCompat`; Android 15/16 predictive back (Activity 1.11.0 bridge for MainActivity, platform `OnBackInvokedCallback` for TermuxActivity); no edge-to-edge/back/orientation/large-screen opt-outs | Regression/behavior | **CLOSED / PASS**; build + 355 tests green; no opt-outs in merged manifest; P1G physical acceptance queued |

### 15.1 Exact P1E1 implementation (proven by P1E1-P)

**P1E1 — Kotlin/AndroidX codegen + build-tooling bridge (Gradle 7.6.4 / AGP 7.4.2 / Kotlin
1.9.25 / Moshi 1.15.2 / Navigation 2.3.5 / JDK 17)**

Version changes only:
- `gradle/wrapper/gradle-wrapper.properties`: `6.7.1 → 7.6.4`.
- root `build.gradle`: `android_plugin 4.2.2 → 7.4.2`, `kotlin_version 1.4.32 → 1.9.25`,
  `jacoco_version 0.8.4 → 0.8.8`, `navigation_version 2.1.0 → 2.3.5`, and
  `de.undercouch:gradle-download-task 3.4.3 → 5.0.0` (see the CI correction in §18).
- `app/build.gradle`: `moshi_version 1.9.3 → 1.15.2`, `kotlin_jdk_version 1.4.32 → 1.9.25`,
  `mockito_version 2.23.0 → 4.11.0` (test-only), add `ndkVersion "21.4.7075529"`, and set
  `jacoco.excludes = ['jdk.internal.*']` in `tasks.withType(Test)`.
- `termux-app/terminal-emulator/build.gradle`: add `ndkVersion "21.4.7075529"`.
- CI: drop the JDK 8 build stage; run the Gradle build on **JDK 17** (keep the CI-R1 explicit
  pinned `sdkmanager` install).

Behavior-neutral source adjustments required by Kotlin 1.9 (no logic change):
- `FilesystemEditFragment.kt` and `MainActivityViewModel.kt`: add `else -> {}` to two
  non-exhaustive `when` statements (Kotlin 1.7+ requires exhaustiveness).
- `AppDetailsViewModel.kt`, `FilesystemEditViewModel.kt`, `FilesystemListViewModel.kt`,
  `MainActivityViewModel.kt`, `SessionEditViewModel.kt`: factory override signature
  `<T : ViewModel?>` → `<T : ViewModel>` (Java-generic override nullability).
- `NavigationStabilityGuardTest`: expected Navigation version `2.1.0 → 2.3.5` (guard update).

Evidence: local canonical gate green — `36 suites / 326 tests / 0 failures / 0 errors /
0 skipped`; Safe Args generation, Moshi codegen, Room KAPT, Parcelize and ViewBinding all PASS
on JDK 17 with NDK 21.4.7075529. Full detail in §17.

---

## 16. P1E0 confirmation of no changes

- `app/src` (main/test/androidTest), manifests, Gradle files, wrapper, workflow, resources:
  **unchanged** (`git diff` empty for all).
- Only documentation/control-plane files changed after this audit.

---

## 17. P1E1-P probe results (BRIDGE_FOUND)

Probe run 2026-09-15 on `577b6de`; **all disposable edits reverted** (`git status` clean).

### 17.1 Proven bridge candidate

| Component | Proven version |
|---|---|
| Gradle | **7.6.4** (on JDK 17) |
| AGP | **7.4.2** |
| Kotlin / KGP | **1.9.25** (`kotlin-stdlib-jdk8` 1.9.25) |
| Moshi (runtime + codegen) | **1.15.2** via KAPT |
| Navigation (plugin + fragment-ktx + ui-ktx) | **2.3.5** |
| JaCoCo | **0.8.8** + `jacoco.excludes = ['jdk.internal.*']` |
| Mockito (test-only) | **4.11.0** (`mockito-core` + `mockito-inline`; `mockito-kotlin` 2.1.0 unchanged) |
| JDK (Gradle build) | **17** |
| NDK | **21.4.7075529** (requires explicit `ndkVersion`; AGP default is 23.1.7779620) |
| Build Tools | 30.0.3 (AGP 7.4.2 default, auto-provisioned) |
| compileSdk / targetSdk / minSdk | 30 / 30 / 21 (app), 29/29/21 (terminal) — **unchanged** |

Proven locally: canonical `clean assembleDebug testDebugUnitTest` = **BUILD SUCCESSFUL**, plus
`:app:compileDebugKotlin`, `:app:kaptDebugKotlin`, `:app:compileDebugUnitTestKotlin`,
`:app:compileDebugAndroidTestKotlin`, `:app:assembleDebugAndroidTest` all PASS. Safe Args,
Moshi codegen, Room kapt, Parcelize and ViewBinding all generate/compile.

### 17.2 Probe cell findings

| Cell | Result |
|---|---|
| 1 (Gradle 7.6.4 / AGP 7.4.2 / JDK 17, rest unchanged) | Plugin/config loaded; **first blocker = AGP 7.4.2 requires KGP ≥ 1.5.20** (Kotlin 1.4.32 rejected). AGP auto-provisioned its default NDK 23.1.7779620. |
| 2 (add Kotlin 1.9.25 + Moshi 1.15.2) | `help` configured; task execution blocker = **Safe Args plugin 2.1.0 fails Gradle-7.6 task validation** (`ArgumentsGenerationTask.applicationIdResource` unannotated). |
| 3 (Navigation 2.5.3) | Safe Args 2.5.3 plugin works, but **entire Navigation 2.5.3 graph requires compileSdk ≥ 31** (`checkDebugAarMetadata`: core 1.8.0, fragment 1.5.4, activity 1.5.1, lifecycle 2.5.1, savedstate 1.2.0, window 1.0.0 …). Cell stopped; compileSdk **not** raised. |
| 3b (Navigation **2.3.5**) | **PASS** — smallest Navigation bridge that works with AGP 7.4.2 while staying compileSdk 30. Safe Args generation + AAR metadata + KAPT all PASS. |
| Kotlin source compat | 7 behavior-neutral errors under Kotlin 1.9.25: 2× non-exhaustive `when` (`else -> {}`), 5× `ViewModelProvider.NewInstanceFactory.create` override nullability (`<T : ViewModel?>` → `<T : ViewModel>`). |
| JaCoCo | 0.8.4 fails on JDK 17 (`Unsupported class file major version 58`); **0.8.8** passes the bytecode version, but `jacoco.includeNoLocationClasses = true` then needs `jacoco.excludes = ['jdk.internal.*']` (JDK 16+ module access) or tests crash with `NoClassDefFoundError: jdk/internal/reflect/GeneratedSerializationConstructorAccessor1`. |
| Mockito (test-only) | 2.23.0 fails on JDK 17 (Byte Buddy 1.9 cannot read Java-17 classes). **4.11.0** required to run the 326 tests. |
| `gradle-download-task` 3.4.3 | Local probe: plugin loads. **CI correction (§18): fails** Gradle 7.6 task-property validation when `:app:downloadAssets` enters the graph (clean checkout) → upgraded to **5.0.0**. |
| ktlint `JavaExec.main =` | **Unchanged** — `:app:ktlint` executes successfully on Gradle 7.6.4 (deprecated but functional); defer `mainClass` to the AGP 8 milestone. |
| `reportVersionCode` / `reportVersionName` | Execute (configuration-time write); works on Gradle 7.6.4. |
| `jacocoCoverageReport` / `testAll` / `downloadAssets` task | Not exercised by the normal gate; `reports { xml.enabled }` deprecations deferred to the AGP 8 milestone. |

### 17.3 Transitive movement caused by the bridge

Navigation 2.3.5: fragment 1.1.0 → **1.2.4**, lifecycle 2.1.0/2.0.0 → **2.2.0**, activity 1.0.0
→ **1.1.0**, `androidx.core:core` 1.1.0 → **1.3.0**. Moshi 1.15.2: okio 1.17.2 → **3.7.0**
(shared with OkHttp 3.14.7). Kotlin stdlib family → **1.9.25**. **No pre-release artifacts**
anywhere. `core-ktx` stays 1.1.0 while transitive `core` becomes 1.3.0 (the same family
misalignment P1D9 addressed — re-note for P1E1). OkHttp 3.14.7 running against Okio 3.7.0 is a
runtime-compatibility item to validate, though the JVM tests pass.

### 17.4 Future gate — Moshi KAPT → KSP before Kotlin 2.2

Moshi 1.15.2 KAPT emitted: *"Kapt support in Moshi Kotlin Code Gen is deprecated and will be
removed in 2.0. Please migrate to KSP."* Moshi 1.15.x KAPT is a **Kotlin 1.9-only** bridge;
Kotlin 2.2 codegen requires **KSP**. This is why P1E2 was inserted as a dedicated codegen
milestone before the Kotlin 2.2 jump (P1E3).

---

## 18. P1E1 implementation result (CLOSED / PASS)

The P1E1-P bridge above was persisted on `feature/android-modernization`. Local canonical gate
and remote CI both green: **36 suites / 326 tests / 0 failures / 0 errors / 0 skipped**.

**CI-only correction to the P1E1-P finding.** P1E1-P reported `gradle-download-task:3.4.3` as
"unchanged/works" because the `downloadAssets` task never entered the local execution graph
(the gitignored `app/src/main/jniLibs` bundle was already present). On a clean CI checkout the
assets are absent, so `checkIfAssetsMissing → fetchAssets → downloadAssets` is scheduled and
Gradle 7.6 fails the `Download` task-property validation (`authScheme`, `cachedETagsFile`,
`credentials`, `dest`, `downloadTaskDir` unannotated). Reproduced locally by executing
`:app:fetchAssets`. Fixed per the P1E1-P PART M fallback: **upgrade to the smallest 5.x —
`de.undercouch:gradle-download-task:5.0.0`** (verified by running the real `:app:fetchAssets`
download path, then the canonical clean build).

**Persistent bridge (implemented):** Gradle **7.6.4** · AGP **7.4.2** · Kotlin/KGP **1.9.25** ·
Moshi **1.15.2** (KAPT) · Navigation **2.3.5** · JaCoCo **0.8.8** (+`jdk.internal.*` exclusion)
· Mockito **4.11.0** (test-only) · JDK **17** · Build Tools **30.0.3** · NDK **21.4.7075529**
(explicit `ndkVersion`) · gradle-download-task **5.0.0**. compileSdk/targetSdk/minSdk unchanged
(30/30/21; terminal 29/29/21). CI is a single JDK 17 stage (JDK 8 stage removed). *(Superseded
by §19 for the Moshi codegen processor.)*

---

## 19. P1E2 result — Moshi codegen KAPT → KSP (CLOSED / PASS)

- **KSP `1.9.25-1.0.20`** pinned in the root buildscript (`ksp_version`) and applied to `:app`
  only. **Moshi codegen moved `kapt → ksp`**; **Room stays on KAPT** (`kotlin-kapt` retained).
- Processor separation (clean build): Moshi adapters
  (`GithubApiClient_ReleasesResponseJsonAdapter`, `GithubApiClient_GithubAssetJsonAdapter`)
  generate **only** under `app/build/generated/ksp/<variant>/kotlin/…`; Room `*_Impl` generates
  **only** under `app/build/generated/source/kapt/…`. No duplicate processors or adapters. The
  **Moshi KAPT deprecation warning is gone**. Generated adapter semantics are unchanged
  (same fields, `@Json` names, nullability, constructor mapping).
- `MoshiKspGuardTest` guards the split. Local + remote green:
  **37 suites / 327 tests / 0 failures / 0 errors / 0 skipped** (remote run `34941411912`).
- No production source, manifest, resource, SDK, NDK, wrapper, or CI change.

**Historical P1E3-P boundary:** this probe established the recipe subsequently implemented and
accepted in P1E3.

---

## 20. P1E3-P result — AGP 8.10 / Kotlin 2.2 compatibility probe (BRIDGE_FOUND)

Disposable probe on `ea6624f`; **all experimental edits reverted** (`git status` clean). A
coherent AGP 8 candidate builds locally **before** any compileSdk change.

### 20.1 Proven P1E3 candidate

| Component | Version |
|---|---|
| Gradle | **8.11.1** |
| AGP | **8.10.1** |
| Kotlin / KGP | **2.2.20** (`kotlin-stdlib-jdk8` 2.2.20) |
| KSP (Moshi) | **2.2.20-2.0.4** (KSP2, enabled by default — no `ksp.useKSP2` property needed) |
| JDK | **17** |
| Build Tools | **35.0.0** |
| NDK | **21.4.7075529** (accepted by AGP 8.10.1; P1F boundary intact) |
| Moshi | 1.15.2 → **KSP2** |
| Room | 2.1.0 → **KAPT** (works under Kotlin 2.2) |
| Navigation / Safe Args | 2.3.5 **unchanged** (plugin works under AGP 8.10.1) |
| JaCoCo | 0.8.8 (unchanged) |
| gradle-download-task | 5.0.0 (works) |
| Mockito | 4.11.0 (test-only) |
| compileSdk / targetSdk / minSdk | **30 / 30 / 21** (terminal 29/29/21) — frozen |

Local canonical gate: `clean assembleDebug testDebugUnitTest` = **37 suites / 327 tests /
0 failures / 0 errors / 0 skipped**.

### 20.2 Required P1E3 implementation changes (all proven)

1. `gradle/wrapper`: Gradle `7.6.4 → 8.11.1`.
2. root `build.gradle`: AGP `7.4.2 → 8.10.1`, Kotlin `1.9.25 → 2.2.20`, KSP
   `1.9.25-1.0.20 → 2.2.20-2.0.4`.
3. `app/build.gradle`: `kotlin_jdk_version 2.2.20`; add
   `namespace 'io.github.lord1egypt.prootx'`; `buildFeatures { viewBinding true; buildConfig true }`;
   JaCoCo report DSL `xml.enabled/html.enabled` → **`xml.required/html.required`** (removed in Gradle 8).
4. terminal modules: add `namespace` (`com.termux`, `com.termux.view`, `com.termux.terminal`).
5. Four production source files: `String.toLowerCase(Locale.ENGLISH)` →
   `lowercase(Locale.ENGLISH)` (`GithubAppsFetcher`, `AppsRepository`, `FilesystemEditFragment`,
   `FilesystemEditViewModel`) — behavior-neutral (Kotlin 2.x made `toLowerCase` an error).
6. `MainActivityTest.kt` (androidTest): `R.id.terminal_view` → **`com.termux.R.id.terminal_view`**
   — the resource is owned by `:terminal-term`, so AGP 8's non-transitive R requires the owning
   module's R class (proper ownership fix; do **not** disable `nonTransitiveRClass`).
7. `MoshiKspGuardTest`: expected KSP version → `2.2.20-2.0.4`.

Deferred (non-blocking) cleanup after P1E3: remove the four manifest `package` attributes
(AGP 8.10 ignores them with a warning), migrate the ktlint `JavaExec.main` → `mainClass`
(deprecated, removed in Gradle 9), and `String.capitalize()` → `replaceFirstChar` (warnings).

### 20.3 Probe findings

- **Raw Cell 1 first blockers:** (1) Gradle 8 removed JaCoCo `xml.enabled`/`html.enabled`;
  (2) AGP 8 disables BuildConfig (needs `buildConfig true`); (3) AGP 8 requires `namespace`.
- **Non-transitive R / non-final IDs:** defaults work; the only cross-module R reference is the
  androidTest `terminal_view` case above. No `switch(R.id)` issue.
- **DSL:** `compileSdkVersion`/`targetSdkVersion`/`minSdkVersion` and `lintOptions` are
  deprecated but **work** (warnings); `testOptions`, `sourceSets`, `ndkVersion`,
  `externalNativeBuild`, `testCoverageEnabled`, `javaCompileOptions`, `buildFeatures` work.
- **Gradle 8 tasks:** `downloadAssets`/`fetchAssets` execute with gradle-download-task 5.0.0;
  `reportVersionCode`/`reportVersionName` (configuration-time writes) work; `checkIfAssetsMissing`
  works; `ktlint` `main =` works (deprecated). Persistent validation later found AGP 8's moved
  unit-test JaCoCo `.exec` path; P1E3-R1 corrected both report tasks.
- **NDK:** AGP 8.10.1 accepts `ndkVersion "21.4.7075529"`; native build (all four ABIs) succeeds.
- **No dependency forced and no exclusions; no pre-release artifacts.** Graph: kotlin-stdlib
  2.2.20, moshi 1.15.2, okio 3.7.0, okhttp 3.14.7, room 2.1.0, navigation 2.3.5, fragment 1.2.4,
  activity 1.1.0, lifecycle 2.2.0, core 1.3.0, core-ktx 1.1.0, savedstate 1.0.0, coroutines 1.3.9.
- **SDK frozen** — no dependency required compileSdk > 30.
- **APK (disposable):** `io.github.lord1egypt.prootx` / 1.0.0 / targetSdk 30 / four ABIs;
  payload intact. androidTest package `io.github.lord1egypt.prootx.test`.

**Conclusion:** ProotX can reach the modern AGP 8.10 / Kotlin 2.2 build stack **before**
compileSdk 36. P1E3 persisted this recipe and is now **CLOSED / PASS**; no sequence change was
forced.

---

## 21. P1E3 implementation result — CLOSED / PASS

Accepted implementation: `1828cdd4441a291433d07bd8a3e4efa96bcbfc76`.

- Persisted Gradle `7.6.4 → 8.11.1`, AGP `7.4.2 → 8.10.1`, Kotlin/stdlib `1.9.25 → 2.2.20`,
  KSP `1.9.25-1.0.20 → 2.2.20-2.0.4`, and Build Tools `30.0.3 → 35.0.0`.
- Added explicit namespaces for `:app`, `:terminal-term`, `:terminal-view`, and
  `:terminal-emulator`; enabled BuildConfig only in `:app`; preserved default non-transitive R
  and non-final resource IDs.
- Migrated JaCoCo report properties from `enabled` to `required`; JaCoCo remains 0.8.8.
- Replaced exactly five `toLowerCase(Locale.ENGLISH)` calls with
  `lowercase(Locale.ENGLISH)` across four production files. Qualified only the two terminal
  resource references in `MainActivityTest` as `com.termux.R.id.terminal_view`.
- Moshi remains 1.15.2 on KSP2; Room remains 2.1.0 on KAPT; Navigation/Safe Args remains 2.3.5.
- compileSdk/targetSdk/minSdk remain 30/30/21; terminal modules remain 29/29/21; NDK remains
  21.4.7075529. Source manifests remain unchanged. No intentional runtime or UI change.
- Remote CI run `34974083190` at the accepted SHA passed JDK 17, explicit Build Tools 35.0.0 /
  NDK 21.4 setup, canonical clean build, **37 suites / 327 tests / 0 failures / 0 errors / 0
  skipped**, androidTest build, and both APK uploads.

### 21.1 P1E3-R1 — AGP 8 JaCoCo execution-data path

AGP 8 moved JVM unit-test coverage data from `build/jacoco/testDebugUnitTest.exec` to
`build/outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec`. Before remediation,
`jacocoCoverageReportForCi` was **SKIPPED** because none of its configured execution-data files
existed. Commit `1828cdd` updates only that unit-test path in both report tasks and preserves
their connected-test inputs and all other report configuration.

The separate local acceptance command
`./gradlew :app:jacocoCoverageReportForCi --no-daemon --info` then **EXECUTED** successfully
(87/87 actionable tasks), loaded the AGP 8 `.exec` (205,269 bytes; SHA-256
`84bb5242345707ab3e225f1bdcf387a20384fcac2157668aa11a3bd7bfb48e6f`), processed 331
classes, and emitted a parseable 788,365-byte XML report plus a 10,254-byte HTML index. The
standard GitHub workflow does not execute this JaCoCo report task; remote build/test acceptance
and local report acceptance are distinct proofs.

### 21.2 Deferred after P1E3

Manifest `package` warnings; `JavaExec.main` → `mainClass` before Gradle 9; legacy Android DSL
and `lintOptions`; `String.capitalize()`; configuration-time custom tasks; Node/action
maintenance warnings; `ndk.dir`; OkHttp 3.14.7 + Okio 3.7.0 runtime validation; eventual Room
KAPT migration; and the core/core-ktx family note remain deferred.

## 22. P1E4 compileSdk 36 implementation result — CLOSED / PASS

Accepted implementation: `6d30b333b0a1d0b8ab0be966af4c3052dcf29500`; remote CI run
`35013165950` — SUCCESS.

- Raised only `:app` `compileSdkVersion 30 → 36`; app targetSdk/minSdk remain 30/21 and all
  terminal modules remain 29/29/21. CI installs `platforms;android-36` plus the retained API 29,
  Build Tools 35.0.0, and NDK 21.4.7075529 packages.
- API 36 marks `PackageInfo.versionName` nullable. `AppsListFragment.getProotXVersion(): String`
  preserves ProotX's existing invariant with the sole production edit `info.versionName!!`.
  This is compileSdk source compatibility, not a targetSdk behavior change.
- Gradle/AGP/Kotlin/KSP and all dependency versions are unchanged. Source manifests, Room
  schema 1–7/migrations/`Data.db` name, merged-manifest behavior, runtime behavior, and UI are
  unchanged.
- Local compile/codegen/unit/androidTest/ktlint/download/native gates passed. The canonical
  build retained **37 suites / 327 tests / 0 failures / 0 errors / 0 skipped**, both APK
  identities, four ABIs, and 16/16 native/support payloads.
- The separate JaCoCo regression task executed, loaded the AGP 8 `.exec`, processed 331 classes,
  and produced non-empty parseable XML plus HTML. The standard CI workflow still does not run
  this report task.
- Remote CI proved JDK 17, Gradle 8.11.1, API 36/API 29/Build Tools 35.0.0/NDK 21.4 setup,
  canonical tests, androidTest build, and both artifact uploads.

## 23. P1E5 API 31+ manifest / PendingIntent / receiver implementation result — CLOSED / PASS

Accepted implementation: `6e8a0559bc691266d403a216c56ec4377ce0c98b`; remote CI run
`35020171430` — SUCCESS.

- `MainActivity` and `TermuxActivity` declare `android:exported="true"`. TermuxActivity retains
  its existing `VIEW`/`DEFAULT`/`BROWSABLE` `ssh://` deep-link `intent-filter`; no SSH support was
  removed. The embedded-terminal SSH entry point is therefore an accepted product feature.
- All six application-owned `PendingIntent`s now carry explicit mutability: the
  `NotificationConstructor.kt` session-list and settings intents use `FLAG_IMMUTABLE`, its
  stop-sessions service intent uses `FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE`, and the
  `TermuxService.java` content/exit/wake-lock intents use `FLAG_IMMUTABLE`. Mutable count **0**;
  unspecified-mutability count **0**. A repository-wide inventory found no additional
  application-owned `PendingIntent` creation.
- Receiver classification (per §10.4): the `LocalBroadcastManager` result receiver is in-process
  and unchanged; `MainActivity`'s `DownloadManager.ACTION_DOWNLOAD_COMPLETE` registration is a
  system broadcast and unchanged; the `TermuxActivity` custom `com.termux.app.reload_style`
  receiver is classified `RECEIVER_NOT_EXPORTED` for API-34 work, but the explicit flag is
  deferred because `:terminal-term` compiles against API 29 and the flag does not exist there.
- Disposable targetSdk 31 probe: `:app:processDebugMainManifest` and `:app:assembleDebug` both
  passed, proving no other merged component with an `intent-filter` lacks `android:exported`.
  The probe was reverted; persistent targetSdk remains **30** and `git diff` shows no targetSdk
  change.
- Exactly four functional files changed; no Gradle, dependency, SDK-level, Room/schema/migration,
  `Data.db`, resource, runtime, UI, storage-permission, or FGS change. `:app:lintDebug` surfaces
  only pre-existing debt (14 errors / 131 warnings / 3 hints) with **no**
  `UnspecifiedImmutableFlag`, `ExportedActivity`, `ExportedService`, or `ExportedReceiver`
  finding.
- Local gates: `:app:assembleDebugAndroidTest` completed (`BUILD SUCCESSFUL`), `:app:ktlint`
  passed, `:app:downloadAssets` passed, and `:app:jacocoCoverageReportForCi` executed and emitted
  non-empty XML (788,365 B, 391 classes) plus HTML. The custom JaCoCo report task fails if AGP 8's
  instrumented `intermediates/classes/debug/jacocoDebug` directory is present; it passes from a
  `clean` report-only state. No JaCoCo configuration was altered (deferred build-tooling cleanup).
- Merged debug manifest final state: `MainActivity` true, `TermuxActivity` true, `TermuxService`
  false, `ProotXDocProvider` true, `ServerService` non-exported default, targetSdk 30.
- Canonical build evidence was retained without a rerun (**37 suites / 327 tests / 0 failures /
  0 errors / 0 skipped**), because no functional file changed after the accepted run. Remote CI
  proved JDK 17, Gradle 8.11.1, API 36/API 29/Build Tools 35.0.0/NDK 21.4 setup, canonical tests,
  androidTest build, and both artifact uploads.
- No intentional runtime or UI change.

## 24. P1E6 storage / permission runtime compatibility result — CLOSED / PASS

Accepted implementation: `2202d6bda33512d3312827bf2bd6dc17f47dbae9`; remote CI run
`35028617203` — SUCCESS.

- Removed the app manifest's `READ_EXTERNAL_STORAGE` and `WRITE_EXTERNAL_STORAGE` and the
  terminal-term manifest's `WRITE_EXTERNAL_STORAGE`. No replacement broad permission
  (`MANAGE_EXTERNAL_STORAGE`, `READ_MEDIA_*`) was added. Final APK permissions are exactly
  ACCESS_NETWORK_STATE, INTERNET, BILLING, CHANGE_WIFI_STATE, FOREGROUND_SERVICE, WAKE_LOCK,
  VIBRATE.
- Deleted `PermissionHandler.kt` (zero production callers). The app/session launch permission
  gates in `MainActivity`, the storage-only `onRequestPermissionsResult` override, and the
  `PermissionHandler` gates in the filesystem import (`ACTION_OPEN_DOCUMENT`) and export
  (`ACTION_CREATE_DOCUMENT`) SAF flows were removed. SAF remains user-mediated and unchanged.
- `TermuxActivity` removed the storage helper `ensureStoragePermissionGranted()`, the
  `REQUESTCODE_PERMISSION_STORAGE` constant, the `"storage"` reload branch that called it, and
  the now-unused `android.Manifest`, `android.annotation.TargetApi`,
  `android.content.pm.PackageManager`, and `android.os.Build` imports. Terminal session
  creation, SSH parsing, notifications, receiver registration, and UI are untouched.
- App-scoped storage paths are unchanged: `filesDir`, `getExternalFilesDir(null)`,
  `getExternalFilesDirs(null)`, `emulatedScopedDir`/`emulatedUserDir`,
  `sdCardScopedDir`/`sdCardUserDir`, the `/storage/internal` and optional `/storage/sdcard`
  PRoot bindings, and the `AssetDownloader` `emulatedScopedDir/downloads` destination.
- Disposable targetSdk 33 probe: `:app:processDebugMainManifest` and `:app:assembleDebug`
  passed with no `READ_EXTERNAL_STORAGE`/`WRITE_EXTERNAL_STORAGE`/`MANAGE_EXTERNAL_STORAGE` in
  the merged manifest or APK; reverted to targetSdk **30** with no Gradle diff.
- Added `StoragePermissionGuardTest` (2 tests) as static proof that production source and source
  manifests contain no `PermissionHandler` or legacy broad-storage reference.
- Local gates passed: all compile/codegen tasks, `assembleDebugAndroidTest`, ktlint,
  `downloadAssets`, and `jacocoCoverageReportForCi` via the clean/report-only ordering
  (non-empty 782,963-byte XML / 389 classes + HTML). Canonical `clean assembleDebug
  testDebugUnitTest` = **38 suites / 329 tests / 0 failures / 0 errors / 0 skipped**.
- **Intentional runtime behavior change:** app/session launch, filesystem import/export, and
  embedded-terminal use no longer require legacy broad-storage permission. No UI redesign, no
  data-location migration, no filesystem architecture change. The unreachable legacy
  permission-continuation ViewModel code (`waitForPermissions`, `permissionsHaveBeenGranted`,
  the two permission `IllegalState`s, their tests, and the unused
  `alert_permissions_necessary_*` strings) is deferred as dead legacy follow-up.

## 25. P1E7 foreground service / notification compatibility result — CLOSED / PASS

Accepted implementation: `0e70b7e1e3f398eb6fdb92730542cae0da6f1975`; remote CI run
`35032934977` — SUCCESS.

- Declared `FOREGROUND_SERVICE_SPECIAL_USE` (retaining `FOREGROUND_SERVICE`) and typed
  `ServerService` and `TermuxService` as `android:foregroundServiceType="specialUse"`, each with
  a specific `PROPERTY_SPECIAL_USE_FGS_SUBTYPE` property. `TermuxService` is overlaid from the
  API-36 app manifest; `:terminal-term` stays compileSdk 29 and its manifest is unchanged, and
  the overlay merges into exactly one `exported=false` component.
- `POST_NOTIFICATIONS` is intentionally **absent** — not declared and not requested. This is the
  sequencing correction recorded in `DECISIONS.md` D031: P1E7 owns FGS structural compatibility
  and channel ownership; P1E8 owns the targetSdk 33/34 raise plus the `POST_NOTIFICATIONS`
  declaration and runtime request.
- Each service creates its own `"ProotX"` channel (`IMPORTANCE_LOW`); `MainActivity`'s redundant
  channel initialization was removed.
- Initial session (`MainActivity.startSession`) and terminal (`TermuxActivity.onCreate`) launches
  use `startForegroundService` on API 26+; already-running-service commands keep `startService`.
  `ServerService` promotes to the foreground synchronously in `onStartCommand` for `type="start"`
  before asynchronous session work. Promotion uses `FOREGROUND_SERVICE_TYPE_MANIFEST` on API 29+
  (two-argument below) in both services.
- Notification PendingIntents are unchanged (six immutable, mutable count 0); the trampoline audit
  found no notification action that launches an activity. `autoStart()`/`onNewIntent()` remains the
  documented targetSdk 31+ `ForegroundServiceStartNotAllowedException` risk for P1E8.
- Disposable targetSdk 34 probe passed and was reverted with no Gradle diff. Added
  `ForegroundServiceCompatibilityGuardTest` (8 tests). Canonical `clean assembleDebug
  testDebugUnitTest` = **39 suites / 337 tests / 0 failures / 0 errors / 0 skipped**; local JaCoCo
  report executed (non-empty 782,925-byte XML + HTML); androidTest APK built.
- Debug APK 19,913,063 bytes, SHA-256
  `6c3dc42adaaa1b866319f73768b714846768373ea659bef388af154dac63ff52`, package
  `io.github.lord1egypt.prootx`, versionName 1.0.0, SDK 36/30/21, four ABIs, 16/16 required
  payloads; permissions ACCESS_NETWORK_STATE, INTERNET, BILLING, CHANGE_WIFI_STATE,
  FOREGROUND_SERVICE, FOREGROUND_SERVICE_SPECIAL_USE, WAKE_LOCK, VIBRATE. androidTest APK
  1,825,680 bytes, SHA-256
  `5720dd54a6b07a1f8569b5e65e481c80e69a5d8cd1f1f6ac125c0195f474ebcd`, package
  `io.github.lord1egypt.prootx.test`.
- No Gradle, dependency, SDK-level, Room/schema/migration, `Data.db`, resource, runtime-behavior,
  or UI change. `targetSdk` remained **30**; terminal modules remained **29/29/21** at P1E7.

> **Superseded by §26 (P1E8):** the P1E7-era "`:terminal-term` stays compileSdk 29" and
> "terminal modules remain 29/29/21" statements are historical. After P1E8 the module matrix is
> `:app` **36/34/21**, `:terminal-term` **36/29/21**, `:terminal-view`/`:terminal-emulator`
> **29/29/21**; `POST_NOTIFICATIONS` is declared and requested, and the targetSdk 31+
> `ForegroundServiceStartNotAllowedException` risk is handled (see §26).

## 26. P1E8 targetSdk 33/34 runtime compatibility result — CLOSED / PASS

Accepted implementation: `cff25f3f1dffa1a91d78a55915dd50513b694af4`; remote CI run
`35037714144` — SUCCESS.

- App targetSdk **30 → 34** (`compileSdk 36`, `minSdk 21`) after a target-33 checkpoint
  (`processDebugMainManifest`, Kotlin/Java compile, `assembleDebug` all passed).
- `POST_NOTIFICATIONS` declared; requested **contextually at the first real session start** via one
  shared application-private `notification_permission` / `prompt_completed` preference;
  `checkSelfPermission` is authoritative; denial never blocks the session; an explicit denial
  suppresses later automatic prompts; a later Settings grant resumes normal behavior. No Room state,
  no custom rationale UI.
- Initial `ServerService` launch deferred until the activity is `Lifecycle.State.RESUMED`, with the
  pending `Session` retained; `launchForegroundService` catches **only**
  `ForegroundServiceStartNotAllowedException`, records a breadcrumb, restores the pending session,
  and retries on resume. `onCreate → autoStart` and `onNewIntent → autoStart` use the same gate.
- `TermuxActivity` (`ssh://` direct entry) uses the same policy before starting/binding
  `TermuxService` exactly once; its app-internal `reload_style` receiver uses the direct
  `Context.RECEIVER_NOT_EXPORTED` constant on API 33+ (legacy path retained).
- `:terminal-term` `compileSdk 29 → 36` (`targetSdk 29`/`minSdk 21`); manifest unchanged.
  `:terminal-view`/`:terminal-emulator` remain 29/29/21.
- Test-only `app/src/androidTest/AndroidManifest.xml` supplies the `android:exported` values
  `androidx.test:core:1.2.0` omits (target-31+ merger). No dependency upgrade.
- `ForegroundServiceCompatibilityGuardTest` updated in place; `TargetSdk34CompatibilityGuardTest`
  added. Canonical `clean assembleDebug testDebugUnitTest` = **40 suites / 348 tests / 0 failures /
  0 errors / 0 skipped**; local JaCoCo report executed (non-empty 788,568-byte XML + HTML).
- Debug APK 19,916,017 bytes, SHA-256
  `8f1c05443715b92cbbdceab071f8f1017b027dca60527124b710cfd8346cafe7`, package
  `io.github.lord1egypt.prootx`, versionName 1.0.0, SDK 36/34/21, four ABIs, 16/16 required
  payloads; permissions ACCESS_NETWORK_STATE, INTERNET, BILLING, CHANGE_WIFI_STATE,
  FOREGROUND_SERVICE, FOREGROUND_SERVICE_SPECIAL_USE, POST_NOTIFICATIONS, WAKE_LOCK, VIBRATE.
  androidTest APK 1,825,701 bytes, SHA-256
  `ba68481c3cd1b58b5b9374b185b4a2bcfaedc745e19ff83821375ceb55dc1d32`, package
  `io.github.lord1egypt.prootx.test`.
- Bounded target-34 audit clean (no dynamic DEX/JAR loading, no exact alarms, no legacy storage
  permissions, no mutable/unspecified PendingIntents, no un-flagged custom receivers, no implicit
  application-owned component issues). No dependency, Room/schema, storage-path, SSH, or UI change.
  Durable decision: `DECISIONS.md` D032.

## 27. P1E9 targetSdk 35/36 platform behavior result — CLOSED / PASS

Accepted implementation: `41cc7a8c629da364903de0ae71ab524541c7ef76`; remote CI run
`35043129415` — SUCCESS.

- App targetSdk **34 → 36** after a clean target-35 checkpoint (`processDebugMainManifest`,
  Kotlin/Java compile, `assembleDebug`; target-35-only blocker: none). Final matrix `:app`
  **36/36/21**; `:terminal-term` **36/29/21**; `:terminal-view`/`:terminal-emulator` **29/29/21**.
- **Edge-to-edge (Android 15):** `MainActivity` calls `enableEdgeToEdge()` and applies real
  `WindowInsetsCompat` per owner — toolbar owns the top system-bar/cutout inset, the
  `BottomNavigationView` owns the bottom one, the root owns left/right cutout/navigation-bar
  safety — computed from captured initial padding so dispatches never accumulate. Status/navigation
  icons stay light for the dark blue-gray chrome. No `windowOptOutEdgeToEdgeEnforcement`.
- **Predictive back (Android 16):** the authorized `androidx.activity:activity-ktx:1.11.0` bridge
  wires `MainActivity`'s `OnBackPressedDispatcher` to the platform `OnBackInvokedDispatcher` while
  `minSdk` stays 21 (`onNewIntent` became non-null). `TermuxActivity` registers a platform
  `OnBackInvokedCallback` on API 33+ (drawer open → close, otherwise finish) and retains the legacy
  `onBackPressed` fallback for API < 33 / non-predictive-back. No `enableOnBackInvokedCallback`
  opt-out; `TermuxActivity` is not converted to AppCompat.
- **Adaptive/large screen:** no `screenOrientation` lock, no aspect-ratio restriction, no Android 16
  large-screen/resizability opt-out; `TermuxActivity` keeps `resizeableActivity="true"` and
  `fitsSystemWindows="true"` (legacy translucent/status-bar theme attributes retained as inert
  under enforced edge-to-edge). `DeviceDimensions` keeps physical-display geometry for the external
  VNC/X client (classification A).
- Bounded target-35/36 audit clean: no dataSync/mediaProcessing FGS timeout (specialUse), no boot
  receiver, no SYSTEM_ALERT_WINDOW, no audio-focus request, no TLS-1.0/1.1 requirement, no
  nested-intent redirection, no dynamic DEX/JAR loading, no exact alarms, no orientation lock.
- Durable decision: `DECISIONS.md` D033. Transitive movement from the Activity bridge recorded:
  core/core-ktx 1.13.0, lifecycle 2.6.2, savedstate 1.2.1, coroutines 1.7.3, plus the injected
  signature `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`. Navigation 2.3.5 / AppCompat 1.1.0 /
  Material 1.1.0 / Room 2.1.0 unchanged.
- Guards: `TargetSdk34CompatibilityGuardTest` renamed/advanced to
  `TargetSdk36CompatibilityGuardTest`; new `TargetSdk36PlatformBehaviorGuardTest`. Canonical
  `clean assembleDebug testDebugUnitTest` = **41 suites / 355 tests / 0 failures / 0 errors /
  0 skipped**; local JaCoCo report executed (non-empty 788,336-byte XML + HTML).
- Debug APK 20,382,217 bytes, SHA-256
  `39086a7e2939cc44f332f49588e843ba667cebd3cadcf0bc06a1e902a07bac74`, package
  `io.github.lord1egypt.prootx`, versionName 1.0.0, SDK 36/36/21, four ABIs, 16/16 required
  payloads; permissions ACCESS_NETWORK_STATE, INTERNET, BILLING, CHANGE_WIFI_STATE,
  FOREGROUND_SERVICE, FOREGROUND_SERVICE_SPECIAL_USE, POST_NOTIFICATIONS, WAKE_LOCK, VIBRATE
  (+ the AndroidX-injected signature `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`). androidTest APK
  1,893,595 bytes, SHA-256
  `64c26dc5c6e5e63e856b83bd64f9a9d0eb89174007ae7b420a4dd9add32a58cb`, package
  `io.github.lord1egypt.prootx.test`.
- No Room/schema, storage-path, SSH, notification-policy, FGS-type/ID, or data-model change. No
  orientation lock, no large-screen opt-out, no edge-to-edge/back opt-out. **P1E is CLOSED / PASS.**

**Next:** P1F4 — SUPPORT PACKAGING / WHOLE-APP 16 KB INTEGRATION — **READY TO START**. Do not start
without explicit authorization. P1G physical acceptance remains after P1F.
