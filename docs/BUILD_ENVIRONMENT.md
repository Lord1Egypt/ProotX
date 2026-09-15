# ProotX Build Environment

> Current state: **P1E1 bridge toolchain** (Gradle 7.6.4 / AGP 7.4.2 / Kotlin 1.9.25, JDK 17).
> This documents the current build only. The AGP 8 / SDK 36 migration is a later milestone.

## Summary

The ProotX application currently builds with:

| Component | Version |
|---|---|
| Gradle (wrapper) | 7.6.4 |
| Android Gradle Plugin | 7.4.2 |
| Kotlin / KGP | **1.9.25** |
| Moshi (runtime + codegen) | **1.15.2** (codegen via **KSP**) |
| KSP (Moshi codegen) | **1.9.25-1.0.20** |
| Room codegen | KAPT (Room 2.1.0) |
| AndroidX Navigation | **2.3.5** |
| JaCoCo | **0.8.8** |
| Mockito (test-only) | **4.11.0** |
| gradle-download-task | **5.0.0** |
| JDK for the Gradle build | **17** |
| `compileSdk` / `targetSdk` (app) | 30 / 30 |
| `minSdk` | 21 |
| `compileSdk` (terminal modules) | 29 |
| Android NDK | 21.4.7075529 (explicit `ndkVersion`) |
| Android build-tools | 30.0.3 |

History: P0/P1A used Gradle 5.1.1 / AGP 3.4.3. P1B migrated to Gradle 6.7.1 / AGP 4.2.2.
P1C1 migrated synthetic views to View Binding. P1C2-P migrated Kotlin 1.3.61 → 1.4.32 with
Moshi 1.8.0 → 1.9.3. **P1E1** migrated the bridge to Gradle **7.6.4** / AGP **7.4.2** / Kotlin
**1.9.25** / Moshi **1.15.2** on **JDK 17**. SDK levels and NDK remain unchanged.

## JDK requirement

The Gradle build and the Android SDK tooling now share a single modern JVM:

```
JDK 17   →  Android SDK tooling / sdkmanager / SDK+NDK install
         →  ./gradlew clean assembleDebug testDebugUnitTest
```

The previous two-stage model (JDK 17 tooling + JDK 8 Gradle build) ended in **P1E1**; AGP 7.4
and Kotlin 1.9 require a modern JDK. Do **not** reintroduce a JDK 8 build stage.

## Required Android SDK / NDK packages

Install exactly these (nothing more):

| Package | Why |
|---|---|
| `platform-tools` | adb/platform tools; owned explicitly (setup-android's default install is skipped) |
| `platforms;android-30` | `app` module `compileSdk` is 30 |
| `platforms;android-29` | terminal modules (`terminal-view`, `terminal-emulator`, `terminal-term`) use `compileSdk` 29 |
| `build-tools;30.0.3` | default build-tools for AGP 7.4.2 — pinned so CI is deterministic |
| `ndk;21.4.7075529` | native toolchain for the terminal emulator JNI (`ndkBuild`) |

`build-tools` is pinned to AGP 7.4.2's default (`30.0.3`); under P1B it was `30.0.2`
(AGP 4.2.2's default). Pinning keeps CI independent of runner defaults.

## Dependency repositories

Only these repositories are configured:

- `google()`
- `mavenCentral()`

`jcenter()` was removed in P1B (sunset repository). All dependencies required by the
application build and unit tests resolve from the two repositories above; this was verified
with a clean Gradle user home. (`com.schibsted.spain:barista:3.1.0` is an androidTest-only
dependency that was published only to JCenter and is not resolvable — see the deferred
findings in `docs/PROOTX_2_ROADMAP.md`. It is not used by the debug build or unit tests.)

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
(`platform-tools`, `platforms;android-30`, `platforms;android-29`, `build-tools;30.0.3`,
`ndk;21.4.7075529`). The action major version and all pins are otherwise unchanged.

**Download task (P1E1):** `de.undercouch:gradle-download-task` is **5.0.0** — the 3.4.3 task
type fails Gradle 7.6 task-property validation when `:app:downloadAssets` runs on a clean
checkout (no pre-existing `jniLibs`).

**Code generation (P1E2):** Moshi runs on **KSP 1.9.25-1.0.20** (`apply plugin:
'com.google.devtools.ksp'` on `:app`; `ksp "com.squareup.moshi:moshi-kotlin-codegen"`). **Room
remains on KAPT** (`kotlin-kapt` applied) — a deliberate mixed-processing build until Room also
moves to KSP.

## Baseline result (reference)

The frozen baseline at tag `v1.0.0-baseline` measured **313 tests / 24 suites / 0 failures**.
The current P1E1 bridge toolchain measures **326 tests / 36 suites / 0 failures / 0 errors /
0 skipped** (local and remote). See `PROJECT_STATE.md`.
