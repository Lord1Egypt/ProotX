# ProotX Build Environment

> Applies to the frozen ProotX 1.0.0 / legacy toolchain (P1A state).
> This documents the legacy build only. Toolchain modernization is a later milestone.

## Summary

The ProotX application still builds with its **frozen legacy toolchain**:

| Component | Version |
|---|---|
| Gradle (wrapper) | 5.1.1 |
| Android Gradle Plugin | 3.4.3 |
| Kotlin | 1.3.61 |
| JDK for the Gradle build | **8** |
| `compileSdk` / `targetSdk` (app) | 30 / 30 |
| `minSdk` | 21 |
| `compileSdk` (terminal modules) | 29 |
| Android NDK | 21.4.7075529 |

This toolchain is intentionally **not** upgraded in P1A.

## The two-JDK requirement

There is a version conflict between the tooling and the application build:

- The **Android command-line tools** (`sdkmanager`) shipped with current Android SDK
  packages require a **modern JVM** (Java 11+; JDK 17 is used).
- The **frozen Gradle 5.1.1 / AGP 3.4.3** build is only supported on **JDK 8**.

Therefore any build automation must run in two stages:

```
JDK 17   →  Android SDK tooling / sdkmanager / SDK+NDK install
   ↓
JDK 8    →  ./gradlew clean assembleDebug testDebugUnitTest
```

Running `sdkmanager` under JDK 8 fails (this was the original CI failure); running the
legacy Gradle build under JDK 17 is unsupported. Both stages must be explicit.

## Required Android SDK / NDK packages

Install exactly these (nothing more):

| Package | Why |
|---|---|
| `platforms;android-30` | `app` module `compileSdk` is 30 |
| `platforms;android-29` | terminal modules (`terminal-view`, `terminal-emulator`, `terminal-term`) use `compileSdk` 29 |
| `build-tools;28.0.3` | default build-tools for AGP 3.4.3 — pinned so CI is deterministic |
| `ndk;21.4.7075529` | native toolchain for the terminal emulator JNI (`ndkBuild`) |

`build-tools` is pinned because AGP 3.4.3 silently defaults to `28.0.3`; making it explicit
avoids depending on whatever happens to be preinstalled on a runner.

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
JDK 17 provisions the SDK/NDK, then JDK 8 runs the legacy Gradle build and unit tests.
The workflow runs on `main`, `develop`, and `feature/**` pushes, and on pull requests
targeting `main`/`develop`.

## Baseline result (reference)

The frozen baseline at tag `v1.0.0-baseline` measured **313 tests / 24 suites / 0 failures**
with this environment. See `PROJECT_STATE.md`.
