# ProotX Build Environment

> Current state: **P1C2-P bridge toolchain** (Gradle 6.7.1 / AGP 4.2.2 / Kotlin 1.4.32).
> This documents the current build only. Further Android modernization is a later milestone.

## Summary

The ProotX application currently builds with:

| Component | Version |
|---|---|
| Gradle (wrapper) | 6.7.1 |
| Android Gradle Plugin | 4.2.2 |
| Kotlin | **1.4.32** |
| Moshi (runtime + codegen) | **1.9.3** |
| JDK for the Gradle build | **8** |
| `compileSdk` / `targetSdk` (app) | 30 / 30 |
| `minSdk` | 21 |
| `compileSdk` (terminal modules) | 29 |
| Android NDK | 21.4.7075529 |
| Android build-tools | 30.0.2 |

History: P0/P1A used Gradle 5.1.1 / AGP 3.4.3. P1B migrated them to Gradle 6.7.1 /
AGP 4.2.2 as an intentional intermediate ("bridge") step. P1C1 migrated synthetic views to
View Binding. P1C2-P migrated Kotlin 1.3.61 → **1.4.32** together with Moshi 1.8.0 →
**1.9.3** (the verified cross-boundary bridge). SDK levels and NDK remain unchanged.

## The two-JDK requirement

There is a version conflict between the tooling and the application build:

- The **Android command-line tools** (`sdkmanager`) shipped with current Android SDK
  packages require a **modern JVM** (Java 11+; JDK 17 is used).
- The **Gradle 6.7.1 / AGP 4.2.2** build still runs on **JDK 8**.

Therefore any build automation must run in two stages:

```
JDK 17   →  Android SDK tooling / sdkmanager / SDK+NDK install
   ↓
JDK 8    →  ./gradlew clean assembleDebug testDebugUnitTest
```

Running `sdkmanager` under JDK 8 fails (the original CI failure); the bridge Gradle build
is not yet supported on JDK 11/17. Both stages must be explicit. Do **not** silently switch
the application build to a newer JDK.

## Required Android SDK / NDK packages

Install exactly these (nothing more):

| Package | Why |
|---|---|
| `platforms;android-30` | `app` module `compileSdk` is 30 |
| `platforms;android-29` | terminal modules (`terminal-view`, `terminal-emulator`, `terminal-term`) use `compileSdk` 29 |
| `build-tools;30.0.2` | default build-tools for AGP 4.2.2 — pinned so CI is deterministic |
| `ndk;21.4.7075529` | native toolchain for the terminal emulator JNI (`ndkBuild`) |

`build-tools` is pinned to AGP 4.2.2's default (`30.0.2`); under P1A it was `28.0.3`
(AGP 3.4.3's default). Pinning keeps CI independent of runner defaults.

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
export JAVA_HOME=/path/to/jdk8                    # JDK 8 for Gradle

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

GitHub Actions (`.github/workflows/build.yml`) implements the two-stage bootstrap:
JDK 17 provisions the SDK/NDK, then JDK 8 runs the bridge Gradle build and unit tests.
The workflow runs on `main`, `develop`, and `feature/**` pushes, and on pull requests
targeting `main`/`develop`. It installs the pinned packages above and prints the exact
unit-test summary.

**SDK bootstrap (CI-R1):** `android-actions/setup-android@v3` is configured with
`packages: ''` so it does **not** install its default `tools platform-tools` set — the legacy
`tools` package is no longer published and caused `Failed to find package 'tools'`. The SDK
processes/`platform-tools` are instead owned explicitly by the pinned `sdkmanager` step
(`platform-tools`, `platforms;android-30`, `platforms;android-29`, `build-tools;30.0.2`,
`ndk;21.4.7075529`). The action major version, the two-stage JDK model, and all pins are
unchanged.

## Baseline result (reference)

The frozen baseline at tag `v1.0.0-baseline` measured **313 tests / 24 suites / 0 failures**.
The P1B bridge toolchain preserves that result. See `PROJECT_STATE.md`.
