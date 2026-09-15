# ProotX Build Environment

> Current state: **P1E7 toolchain** (Gradle 8.11.1 / AGP 8.10.1 / Kotlin 2.2.20, JDK 17,
> app compileSdk 36 / targetSdk 30).

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
| `compileSdk` / `targetSdk` (app) | 36 / 30 |
| `minSdk` | 21 |
| terminal `compileSdk` / `targetSdk` / `minSdk` | 29 / 29 / 21 |
| Android NDK | 21.4.7075529 (explicit `ndkVersion`) |
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
SDK levels.

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
| `platforms;android-29` | terminal modules (`terminal-view`, `terminal-emulator`, `terminal-term`) use `compileSdk` 29 |
| `build-tools;35.0.0` | AGP 8.10 build tools — pinned so CI is deterministic |
| `ndk;21.4.7075529` | native toolchain for the terminal emulator JNI (`ndkBuild`) |

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
export ANDROID_NDK_HOME="$ANDROID_SDK_ROOT/ndk/21.4.7075529"
export JAVA_HOME=/path/to/jdk17                   # JDK 17 for Gradle

./gradlew clean assembleDebug testDebugUnitTest --no-daemon
```

Notes:

- `app/src/main/jniLibs/` (the PRoot/Busybox support bundle) is produced automatically by
  the `downloadAssets` Gradle task from the `ProotX-Assets-Support` release; it is not
  committed.
- `local.properties` is **gitignored** and must not be committed. Point it at the SDK/NDK
  if you are not using environment variables:

  ```properties
  sdk.dir=/path/to/android-sdk
  ndk.dir=/path/to/android-sdk/ndk/21.4.7075529
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
`ndk;21.4.7075529`). The action major version and all pins are otherwise unchanged.

**Download task (P1E1):** `de.undercouch:gradle-download-task` is **5.0.0** — the 3.4.3 task
type fails Gradle 7.6 task-property validation when `:app:downloadAssets` runs on a clean
checkout (no pre-existing `jniLibs`).

**Code generation (P1E2/P1E3):** Moshi runs on **KSP2 2.2.20-2.0.4** (`apply plugin:
'com.google.devtools.ksp'` on `:app`; `ksp "com.squareup.moshi:moshi-kotlin-codegen"`). **Room
remains on KAPT** (`kotlin-kapt` applied) — a deliberate mixed-processing build until Room also
moves to KSP.

**compileSdk (P1E4):** only `:app` compiles against API 36; targetSdk/minSdk remain 30/21 and
terminal modules remain 29/29/21. API 36 marks `PackageInfo.versionName` nullable, so the
existing `AppsListFragment.getProotXVersion(): String` invariant is explicit as
`info.versionName!!`. No targetSdk behavior, dependency, manifest, runtime, or UI change was
included.

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
`READ_MEDIA_*` replacement is declared. App `targetSdk` remains **30** and terminal modules
remain **29/29/21**; a disposable targetSdk 33 probe built cleanly with no storage permissions in
the merged manifest/APK and was reverted. App-scoped storage paths are unchanged.

## Manifest / intent state (P1E5)

`MainActivity` and `TermuxActivity` declare `android:exported="true"`; `TermuxActivity` retains
its `ssh://` BROWSABLE deep link. The six application-owned `PendingIntent`s are explicitly
immutable (the stop-sessions service intent retains
`FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE`). App `targetSdk` remains **30** and terminal modules
remain **29/29/21**; a disposable targetSdk 31 manifest/assemble probe proved the Android 12
exported-component requirement is satisfied and was reverted.

## Foreground-service state (P1E7)

The app declares `android.permission.FOREGROUND_SERVICE` and
`android.permission.FOREGROUND_SERVICE_SPECIAL_USE`. `io.github.lord1egypt.prootx.ServerService`
and `com.termux.app.TermuxService` are both typed `android:foregroundServiceType="specialUse"`
with a `PROPERTY_SPECIAL_USE_FGS_SUBTYPE` property; the terminal service is overlaid from the
API-36 app manifest because `:terminal-term` still compiles against API 29 (its manifest remains
unchanged), and merges into exactly one `exported=false` component. Each service creates its own
`"ProotX"` channel at `IMPORTANCE_LOW`; the initial session/terminal launches use
`startForegroundService` on API 26+; `ServerService` promotes synchronously before asynchronous
work; and promotion uses `ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST` on API 29+ (two-argument
`startForeground` below). `POST_NOTIFICATIONS` is **not** declared in P1E7 — it is deferred to
P1E8 with the targetSdk 33/34 raise (`DECISIONS.md` D031). App `targetSdk` remains **30**;
terminal modules remain **29/29/21**. A disposable targetSdk 34 probe built cleanly with the
expected merged services/permissions and was reverted.

## Baseline result (reference)

The frozen baseline at tag `v1.0.0-baseline` measured **313 tests / 24 suites / 0 failures**.
The P1E7 toolchain measures **337 tests / 39 suites / 0 failures / 0 errors / 0 skipped**.
Remote CI run `35032934977` passed at `0e70b7e` and uploaded the debug and androidTest APK
artifacts. P1E7 local validation also proved the API-36 SDK platform, the androidTest APK build,
four ABIs, 16/16 native payloads, the FGS `specialUse` declarations in the APK, the absence of
`POST_NOTIFICATIONS`, and the separate JaCoCo report regression gate. See `PROJECT_STATE.md`.
