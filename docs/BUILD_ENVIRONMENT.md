# ProotX Build Environment

> Current state: **P1E9 toolchain** (Gradle 8.11.1 / AGP 8.10.1 / Kotlin 2.2.20, JDK 17,
> app compileSdk 36 / targetSdk 36).

## Summary

The ProotX application currently builds with:

| Component | Version |
|---|---|
| Gradle (wrapper) | **8.11.1** |
| Android Gradle Plugin | **8.10.1** |
| Kotlin / KGP | **2.2.20** |
| Kotlin stdlib | **2.2.20** |
| Moshi (runtime + codegen) | **1.15.2** (codegen via **KSP2**) |
| KSP (Moshi codegen) | **2.2.20-2.0.4** |
| Room codegen | KAPT (Room 2.1.0) |
| AndroidX Navigation | **2.3.5** |
| JaCoCo | **0.8.8** |
| Mockito (test-only) | **4.11.0** |
| gradle-download-task | **5.0.0** |
| JDK for the Gradle build | **17** |
| `compileSdk` / `targetSdk` (app) | 36 / 36 |
| `minSdk` | 21 |
| `:terminal-term` `compileSdk` / `targetSdk` / `minSdk` | 36 / 29 / 21 |
| `:terminal-view` / `:terminal-emulator` `compileSdk` / `targetSdk` / `minSdk` | 29 / 29 / 21 |
| AndroidX Activity (P1E9 bridge) | **1.11.0** (`activity-ktx`, `:app` only) |
| Android NDK | **29.0.14206865 (r29)**, explicit `ndkVersion` in `:app` and `:terminal-emulator` |
| Android build-tools | **35.0.0** |

History: P0/P1A used Gradle 5.1.1 / AGP 3.4.3. P1B migrated to Gradle 6.7.1 / AGP 4.2.2.
P1C1 migrated synthetic views to View Binding. P1C2-P migrated Kotlin 1.3.61 → 1.4.32 with
Moshi 1.8.0 → 1.9.3. **P1E1** migrated the bridge to Gradle **7.6.4** / AGP **7.4.2** / Kotlin
**1.9.25** / Moshi **1.15.2** on **JDK 17**. P1E2 moved Moshi codegen to KSP. **P1E3**
migrated to Gradle **8.11.1** / AGP **8.10.1** / Kotlin **2.2.20** / KSP
**2.2.20-2.0.4** while preserving SDK levels and NDK. **P1E4** then raised only the app
compileSdk **30 → 36**, keeping targetSdk 30, minSdk 21, and terminal SDKs 29/29/21. **P1E6**
removed the obsolete legacy broad-storage permission dependency. **P1E7** made the two real
foreground services structurally compatible with the Android 12–16 FGS rules without changing
SDK levels. **P1E8** raised the app targetSdk **30 → 34**, added `POST_NOTIFICATIONS` + the
contextual one-time request, hardened the target-31+ FGS start, and raised `:terminal-term`
compileSdk **29 → 36** (targetSdk stays 29). **P1E9** raised the app targetSdk **34 → 36**
(P1E CLOSED / PASS), enabled real edge-to-edge with per-owner `WindowInsetsCompat`, migrated
predictive back to the platform dispatcher, and added the `androidx.activity:activity-ktx:1.11.0`
bridge while keeping `minSdk 21`. **P1F1** raised the in-tree NDK pin from 21.4.7075529 to
**29.0.14206865** for `:app` and `:terminal-emulator` (no source/linker/packaging change) so the
64-bit `libtermux.so` is 16 KB aligned, and moved NDK selection entirely to the Gradle
`ndkVersion` (CI no longer writes `ndk.dir`).

## JDK requirement

The Gradle build and the Android SDK tooling now share a single modern JVM:

```
JDK 17   →  Android SDK tooling / sdkmanager / SDK+NDK install
         →  ./gradlew clean assembleDebug testDebugUnitTest
```

The previous two-stage model (JDK 17 tooling + JDK 8 Gradle build) ended in **P1E1**. AGP 8.10
requires JDK 17. Do **not** reintroduce a JDK 8 build stage.

## Required Android SDK / NDK packages

Install exactly these (nothing more):

| Package | Why |
|---|---|
| `platform-tools` | adb/platform tools; owned explicitly (setup-android's default install is skipped) |
| `platforms;android-36` | `app` module `compileSdk` is 36 |
| `platforms;android-29` | `:terminal-view` and `:terminal-emulator` use `compileSdk` 29 (still required); `:terminal-term` now uses 36 |
| `build-tools;35.0.0` | AGP 8.10 build tools — pinned so CI is deterministic |
| `ndk;29.0.14206865` | native toolchain for the terminal emulator JNI (`ndkBuild`) |

`build-tools` is pinned to AGP 8.10's proven version (`35.0.0`). Pinning keeps CI independent
of runner defaults.

## Dependency repositories

Only these repositories are configured:

- `google()`
- `mavenCentral()`

`jcenter()` was removed in P1B (sunset repository). All dependencies required by the
application build and tests resolve from the two repositories above; this was verified with a
clean Gradle user home. The former JCenter-only Barista androidTest dependency was removed in
P1D1 and replaced with direct AndroidX Espresso usage.

## Verified build command

```bash
export ANDROID_SDK_ROOT=/path/to/android-sdk      # SDK with the packages above
export ANDROID_NDK_HOME="$ANDROID_SDK_ROOT/ndk/29.0.14206865"
export JAVA_HOME=/path/to/jdk17                   # JDK 17 for Gradle

./gradlew clean assembleDebug testDebugUnitTest --no-daemon
```

Notes:

- The PRoot/Busybox support bundle is produced automatically by the `prepareProotXSupport`
  Gradle task (`app/support.gradle`) from the pinned `ProotX-Assets-Support` release and staged
  under `app/build/generated/prootxSupport/` (modern `jniLibs/<abi>/`, common/legacy
  `assets/support/`, and `assets/support/metadata/support-map.json`). It is not committed, and the
  tracked `app/src/main/jniLibs/` tree is deliberately not a source directory (P1F4B).
- `local.properties` is **gitignored** and must not be committed. Point it at the SDK location;
  the NDK is selected by the Gradle module `ndkVersion`, so do **not** add a deprecated `ndk.dir`:

  ```properties
  sdk.dir=/path/to/android-sdk
  ```

- Do **not** vendor a JDK or Android SDK into this repository.

## Continuous integration

GitHub Actions (`.github/workflows/build.yml`) uses a single **JDK 17** stage for both the
Android SDK/NDK bootstrap and the Gradle build (the JDK 8 stage was removed in P1E1).
The workflow runs on `main`, `develop`, and `feature/**` pushes, and on pull requests
targeting `main`/`develop`. It installs the pinned packages above and prints the exact
unit-test summary.

**SDK bootstrap (CI-R1):** `android-actions/setup-android@v3` is configured with
`packages: ''` so it does **not** install its default `tools platform-tools` set — the legacy
`tools` package is no longer published and caused `Failed to find package 'tools'`. The SDK
packages/`platform-tools` are instead owned explicitly by the pinned `sdkmanager` step
(`platform-tools`, `platforms;android-36`, `platforms;android-29`, `build-tools;35.0.0`,
`ndk;29.0.14206865`). The action major version and all pins are otherwise unchanged.

**NDK selection (P1F1):** the Gradle module `ndkVersion` (`29.0.14206865`) is the authoritative NDK
selector; CI writes only `sdk.dir` to `local.properties` (a deprecated `ndk.dir` is no longer
written, and a stale one breaks configuration under r29). CI also runs a scoped step that fails if
the packaged 64-bit `libtermux.so` (`arm64-v8a`, `x86_64`) has a `PT_LOAD` alignment below `0x4000`.
The `ProotX-Assets-Support` payloads are **not** covered by that step (P1F2/P1F3).

**Support preparation (P1F4B):** `prepareProotXSupport` (`app/support.gradle`) replaced the
`de.undercouch:gradle-download-task`-based `downloadAssets`/`fetchAssets` pipeline. It downloads the
pinned support `v1.2.0` assets, verifies every SHA-256 against `app/support-release.lock.json`,
validates the published `routing.json`/`manifest.json` contract, and stages the generated payload;
it fails closed on any checksum or schema mismatch. The plugin is no longer applied by `:app`.

**Code generation (P1E2/P1E3):** Moshi runs on **KSP2 2.2.20-2.0.4** (`apply plugin:
'com.google.devtools.ksp'` on `:app`; `ksp "com.squareup.moshi:moshi-kotlin-codegen"`). **Room
remains on KAPT** (`kotlin-kapt` applied) — a deliberate mixed-processing build until Room also
moves to KSP.

**compileSdk (P1E4):** only `:app` compiled against API 36; at that milestone targetSdk/minSdk were
30/21 and terminal modules were 29/29/21 (superseded by P1E8). API 36 marks
`PackageInfo.versionName` nullable, so the existing `AppsListFragment.getProotXVersion(): String`
invariant is explicit as `info.versionName!!`. No targetSdk behavior, dependency, manifest,
runtime, or UI change was included.

**JaCoCo (P1E3-R1):** JaCoCo remains **0.8.8**. Gradle 8 uses `xml.required` and
`html.required`. AGP 8 writes JVM coverage data to
`app/build/outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec`; both report
tasks consume that path while retaining their connected-test inputs. The explicit local command
`./gradlew :app:jacocoCoverageReportForCi --no-daemon --info` executed successfully at
`1828cdd`, loaded the `.exec`, processed 331 classes, and generated parseable XML plus HTML.
The standard GitHub workflow does not run this report task.

**JaCoCo class-directory caveat (P1E5):** the custom report tasks list
`build/intermediates/classes/debug` in `classDirectories`; under AGP 8 that directory contains
the debug variant's instrumented `jacocoDebug` output (`testCoverageEnabled true`). Running the
report after `assembleDebug` therefore fails with `Cannot process instrumented class`. The gate
passes from a `clean` report-only state (e.g. `./gradlew clean :app:jacocoCoverageReportForCi`).
No JaCoCo configuration was changed in P1E5; this is recorded for a future build-tooling cleanup.

## Storage / permission state (P1E6)

The legacy broad-storage dependency is gone: neither the app nor `:terminal-term` declares
`READ_EXTERNAL_STORAGE`/`WRITE_EXTERNAL_STORAGE`, `PermissionHandler` is deleted, and app/session
launch plus SAF import/export request no storage permission. No `MANAGE_EXTERNAL_STORAGE` or
`READ_MEDIA_*` replacement is declared. At that milestone `targetSdk` was **30** and terminal
modules were **29/29/21**; a disposable targetSdk 33 probe built cleanly with no storage
permissions in the merged manifest/APK and was reverted. App-scoped storage paths are unchanged.

## Manifest / intent state (P1E5)

`MainActivity` and `TermuxActivity` declare `android:exported="true"`; `TermuxActivity` retains
its `ssh://` BROWSABLE deep link. The six application-owned `PendingIntent`s are explicitly
immutable (the stop-sessions service intent retains
`FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE`). At that milestone `targetSdk` was **30** and terminal
modules were **29/29/21**; a disposable targetSdk 31 manifest/assemble probe proved the Android 12
exported-component requirement is satisfied and was reverted.

## Foreground-service state (P1E7)

The app declares `android.permission.FOREGROUND_SERVICE` and
`android.permission.FOREGROUND_SERVICE_SPECIAL_USE`. `io.github.lord1egypt.prootx.ServerService`
and `com.termux.app.TermuxService` are both typed `android:foregroundServiceType="specialUse"`
with a `PROPERTY_SPECIAL_USE_FGS_SUBTYPE` property; the terminal service is overlaid from the
app manifest and merges into exactly one `exported=false` component. Each service creates its own
`"ProotX"` channel at `IMPORTANCE_LOW`; the initial session/terminal launches use
`startForegroundService` on API 26+; `ServerService` promotes synchronously before asynchronous
work; and promotion uses `ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST` on API 29+ (two-argument
`startForeground` below). `POST_NOTIFICATIONS` is now declared (P1E8).

## targetSdk 34 runtime state (P1E8)

The app targeted **34** (`compileSdk 36`, `minSdk 21`) and declared
`android.permission.POST_NOTIFICATIONS`. The permission is requested **contextually at the first
real session start** through one shared application-private prefs flag
(`notification_permission`/`prompt_completed`); current grant state always comes from
`checkSelfPermission`; denial never blocks the Linux session. The initial `ServerService` launch
is deferred until the activity is `Lifecycle.State.RESUMED` and catches only
`ForegroundServiceStartNotAllowedException`, retrying on the next resume. `TermuxActivity`'s direct
`ssh://` entry uses the same policy, and its app-internal `reload_style` receiver uses
`Context.RECEIVER_NOT_EXPORTED` on API 33+ (legacy path below). `:terminal-term` compiles at API 36
with `targetSdk 29`; `:terminal-view`/`:terminal-emulator` remain 29/29/21. The system
`DownloadManager` receiver keeps its flag-less registration (system-broadcast exemption), with the
`UnspecifiedRegisterReceiverFlag` lint false positive intentionally suppressed. `androidx.test:core:1.2.0`
lacks `android:exported` on its invoker activities, so a test-only `app/src/androidTest/AndroidManifest.xml`
overlay supplies them for the target-31+ merger. See `DECISIONS.md` D032.

## targetSdk 36 platform state (P1E9)

The app's final target is **36** (`compileSdk 36`, `minSdk 21`). `MainActivity` calls
`enableEdgeToEdge()` and applies real `WindowInsetsCompat` per owner (toolbar top, bottom
navigation bottom, root left/right cutout/navigation safety) from captured initial padding, with
light system-bar icons for the dark chrome. Predictive back uses the AndroidX Navigation dispatcher
bridged to the platform `OnBackInvokedDispatcher` by `androidx.activity:activity-ktx:1.11.0`; the
`TermuxActivity` terminal registers a platform `OnBackInvokedCallback` on API 33+ with the legacy
`onBackPressed` fallback. There is no edge-to-edge or predictive-back opt-out, no orientation lock,
and no large-screen/resizability opt-out. `TermuxActivity` keeps `fitsSystemWindows="true"` and
`resizeableActivity="true"`; `DeviceDimensions` keeps physical-display geometry for the external
VNC/X client. `:terminal-term` remains `36/29/21`; `:terminal-view`/`:terminal-emulator` remain
`29/29/21`. See `DECISIONS.md` D033.

## Baseline result (reference)

The frozen baseline at tag `v1.0.0-baseline` measured **313 tests / 24 suites / 0 failures**.
The P1E9 toolchain measures **355 tests / 41 suites / 0 failures / 0 errors / 0 skipped**.
Remote CI run `35043129415` passed at `41cc7a8` and uploaded the debug and androidTest APK
artifacts. P1E9 local validation also proved the API-36 SDK platform, the androidTest APK build,
four ABIs, 16/16 native payloads, targetSdk 36 in the merged manifest/APK, the absence of all
opt-outs, and the separate JaCoCo report regression gate. **P1E is CLOSED / PASS.** See
`PROJECT_STATE.md`.

## Support-bundle toolchain (P1F2)

The `ProotX-Assets-Support` runtime is built by `build-support.sh` (branch
`feature/p1f-support-modernization`), pinned in `provenance/sources.lock.json`:

| Component | Pin |
|---|---|
| Builder image | `ghcr.io/termux/package-builder@sha256:374fedda8d2ce7a8ab499735d39329301c4f2f18ea4411b3cf7c93d4668768ab` |
| termux-packages | `0ffca06c59752c6d52c646980d956b961064e1fc` |
| PRoot | `termux/proot` `v5.1.107.92` (`7266fb3e…`, archive `29385d1d…`) |
| NDK / build API | r29 / **API 24** |
| `SOURCE_DATE_EPOCH` | `1787437959` |

Lanes: host API 21–28 use the frozen legacy payload; host API 29+ use the source-rebuilt modern
payload. ProotX `minSdk` stays 21 and the logical runtime contract is unchanged. The modern lane
requires Docker with `/dev/fuse`, `CAP_SYS_ADMIN`, `seccomp=unconfined`, and `apparmor=unconfined`
(for termux's `fuse-overlayfs` standalone toolchain). See `docs/PROVENANCE.md` in the support repo
and `DECISIONS.md` D035/D037.

## Support release v1.2.0 (P1F4A/P1F4B)

The support runtime release **`v1.2.0`** is the first explicit dual-lane bundle (annotated tag
`2b5691c9…` → commit `889cb67…`; release `RE_kwDOUXjkQM4XTxeE`, 2026-09-17T21:04:07Z). Archives:
`arm64-v8a` `42fd0042…`, `armeabi-v7a` `496c5d70…`, `x86` `df5e8b3a…`, `x86_64` `d3884105…`, plus
`routing.json`, `SHA256SUMS`, `v1.2.0-provenance.json`, `v1.2.0.spdx.json`. Each archive uses the
explicit `common/`+`legacy/`+`modern/` schema with a per-file `manifest.json`. ProotX pins the
release and every asset SHA-256 in `app/support-release.lock.json`; `prepareProotXSupport` stages the
generated payload and the runtime installer routes legacy (API 21–28, extracted from assets) vs
modern (API 29+, linked from `nativeLibraryDir`, Android W^X). The `.a10` filename inference and
`lib_arch.so` marker are removed. Release CI is split into untrusted validation
(`support-validate.yml`), a privileged build limited to repository-owned refs (`support.yml`), and a
tag-triggered `support-release.yml` whose `contents: write` publish job does no rebuild; all release
actions are pinned by full commit SHA. Whole-app **static** 16 KB compatibility passes (APK/AAB lib
set all-ELF, no 4 KB 64-bit ELF, `zipalign -P 16`, bundletool `PAGE_ALIGNMENT_16K`); the 16 KB
**runtime/emulator** acceptance is P1F5 and physical acceptance is P1G.
See `DECISIONS.md` D036 and the support repo `docs/PROVENANCE.md`.

## 16 KB runtime acceptance and API36 launch remediation (P1F5 / P1E-R1)

The true 16 KB acceptance uses the official **16384-byte page-size** system image
`system-images;android-36;google_apis_ps16k;x86_64` (revision 7, Android 16 / API 36, emulator
37.1.11.0). Acceptance requires `adb shell getconf PAGE_SIZE` = `16384`; a 4096-byte emulator does
not count.

Real API36 execution exposed three **pre-existing, non-16 KB** launch defects that static review had
missed, all now fixed:

- `nav_graph.xml` needs a root `android:id` (Navigation 2.3.5 throws for `startDestination ==
  graphId == 0`); the start destination remains dynamic.
- The `DownloadManager` completion receiver must be registered `Context.RECEIVER_EXPORTED` on
  API 33+ (the provider is a separate process); the legacy two-argument path is retained below 33.
- Play Billing migrated from `billing-ktx:3.0.3` to `com.android.billingclient:billing:8.0.0`
  (AAR minSdk 21, targetSdk 34). The Java artifact is used because ProotX consumes the Java Billing
  API; `billing-ktx` would transitively bump kotlinx-coroutines. Merged `minSdk` stays 21.

Static gates are unchanged: the APK/AAB native set is all ELF, contains no 4 KB 64-bit ELF,
`zipalign -c -P 16` passes and bundletool reports `PAGE_ALIGNMENT_16K`.
