# ProotX Development Changelog

> Engineering/development changelog — not a user-facing release changelog.
> Records what actually changed in the repository, per milestone.

## P0 — Baseline Freeze & Development Safety (2026-09-12)

- Established the initial ProotX baseline (rebrand of the last self-contained public
  UserLAnd v2.8.3 codebase; package `io.github.lord1egypt.prootx`, version `1.0.0`).
- Verified the baseline build: `./gradlew clean assembleDebug testDebugUnitTest` →
  BUILD SUCCESSFUL.
- Measured unit tests: **313 tests / 24 suites / 0 failures / 0 errors / 0 skipped**.
- Froze the baseline with annotated tag `v1.0.0-baseline` on commit
  `94abf5fa520255bb10d087a6be3ba2bc70b0e127`.
- Created branch structure: `main` (stable), `develop` (at baseline),
  `feature/android-modernization`.
- Added `docs/PROOTX_2_ROADMAP.md` (architectural direction + deferred findings).

## P0.5 — Project Control Plane (2026-09-12)

- Created the persistent engineering control plane:
  `PROJECT_STATE.md`, `SESSION_HANDOFF.md`, `TASKS.md`, `DECISIONS.md`,
  `CHANGELOG_DEV.md`, `UPSTREAM_BASELINE.md`, `ASSET_TRACKING.md`, `AGENTS.md`.
- Recorded the dynamic time-based `versionCode` release-contract finding as deferred.
- Reflected P0 CLOSED / P0.5 CURRENT / P1 NOT STARTED status in the roadmap.
- Documentation/state-management only: no source, build, runtime, UI, asset, or CI change.

## P1A — Build-System / JDK / CI Foundation (2026-09-12) — PASS

- Repaired the baseline CI failure: `sdkmanager` (cmdline-tools 16.0) requires a modern
  JVM, but JDK 8 was active during Android SDK setup.
- Reworked `.github/workflows/build.yml` into a two-stage JDK bootstrap:
  JDK 17 provisions the Android SDK/NDK, JDK 8 runs the legacy Gradle build.
- Pinned required SDK/NDK packages: `platforms;android-30`, `platforms;android-29`,
  `build-tools;28.0.3`, `ndk;21.4.7075529`.
- Extended CI triggers to `develop` and `feature/**` (PRs to `main`/`develop`).
- Explicit build contract: `./gradlew clean assembleDebug testDebugUnitTest --no-daemon`.
- Added a CI step reporting the exact unit-test summary.
- Added `docs/BUILD_ENVIRONMENT.md`.
- Verified remotely (run `34674686561`): all steps success, `suites=24 tests=313
  failures=0 errors=0 skipped=0`, debug APK artifact uploaded.
- No application source, toolchain version, dependency, manifest, resource, UI, runtime,
  or asset change.

## P1B — Gradle / AGP Bridge Migration (2026-09-12) — PASS

- Gradle wrapper 5.1.1 → **6.7.1** (canonical wrapper artifacts regenerated).
- Android Gradle Plugin 3.4.3 → **4.2.2**. Kotlin remains **1.3.61**.
- Removed the sunset `jcenter()` repository; resolved the full build/test classpath from
  `google()` + `mavenCentral()` only (clean Gradle home proof).
- CI: build-tools pin `28.0.3` → **`30.0.2`** (AGP 4.2.2 default).
- No Gradle DSL compatibility changes were required.
- Local: `clean assembleDebug testDebugUnitTest` green; **313 tests / 24 suites / 0
  failures**. Remote (run `34675865307`, commit `70bcc09`): all steps success, same test
  result, debug APK uploaded.
- Unchanged: Kotlin, `compileSdk` 30, `targetSdk` 30, `minSdk` 21, NDK 21.4.7075529,
  application dependency versions, source, UI, runtime, assets.
- New deferred finding: `com.schibsted.spain:barista:3.1.0` (androidTest-only) was JCenter-
  only and is not resolvable; replacement deferred to P1D.

## P1C1 — Synthetic Views → View Binding (2026-09-12) — PASS

- Enabled Android View Binding in the `app` module (`buildFeatures.viewBinding true`).
- Migrated `MainActivity` and seven view-using Fragments off
  `kotlinx.android.synthetic`:
  `HelpFragment`, `AppDetailsFragment`, `AppsListFragment`, `FilesystemListFragment`,
  `SessionListFragment`, `SessionEditFragment`, `FilesystemEditFragment`.
- Fragments use the lifecycle-safe `_binding`/`onDestroyView` pattern;
  `AppDetailsFragment`'s LiveData observer is scoped to `viewLifecycleOwner`.
- Added `SyntheticViewImportsTest` (source guard; forbids synthetic view imports, permits
  legacy Parcelize).
- **Zero** synthetic view imports remain in production source; no XML/layout changes were
  needed.
- Legacy Parcelize and `kotlin-android-extensions` intentionally preserved (P1C2).
- Local: `clean assembleDebug testDebugUnitTest` green — **314 tests / 25 suites / 0
  failures** (313 baseline + 1 guard). Remote (run `34676869322`, commit `fbf6f9d`): all
  steps success, same test result, debug APK uploaded.
- Unchanged: Kotlin 1.3.61, Gradle 6.7.1, AGP 4.2.2, SDK/NDK/toolchain, app dependency
  versions, runtime, visual design, assets.

## P1C2 — Kotlin / Parcelize Migration (2026-09-12) — BLOCKED

- Attempted Kotlin 1.3.61 → 1.4.32, `kotlin-android-extensions` → `kotlin-parcelize`,
  removal of `androidExtensions {}`, and `kotlinx.android.parcel.Parcelize` →
  `kotlinx.parcelize.Parcelize`.
- Local compile failed at `:app:kaptDebugKotlin`:
  `kotlin.KotlinNullPointerException` in `me.eugeniomarletti.kotlin.metadata.KotlinClassMetadata`
  invoked from **Moshi 1.8.0**'s `JsonClassCodegenProcessor`. Moshi 1.8.0's metadata reader
  cannot parse Kotlin 1.4 metadata.
- Investigated the remediation: bumping `moshi`/`moshi-kotlin-codegen` to **1.11.0** makes
  kapt succeed (Room 2.1.0-beta01 is unaffected). This is an application dependency change,
  which P1C2 forbids ("only Kotlin moves").
- Per the milestone's scope rules, the migration was **not** completed and **no changes were
  committed**; the working tree was restored to the P1C1 green state
  (`clean assembleDebug testDebugUnitTest` → 314 tests / 25 suites / 0 failures).
- Required next step: authorize the minimal Moshi bump, or move Kotlin 1.4.32 into P1D.

## P1C2-U — Moshi Compatibility Unblocker (2026-09-12) — BLOCKED

- Attempted the authorized Moshi bump on the frozen Kotlin 1.3.61 state
  (`moshi`/`moshi-kotlin-codegen` 1.8.0 → 1.11.0).
- `:app:kaptDebugKotlin` failed: `java.lang.NoSuchMethodError:
  kotlin.jvm.internal.FunctionReferenceImpl.<init>(ILjava/lang/Class;...)` at
  `com.squareup.moshi.kotlin.codegen.MetadataKt$unwrapTypeAlias$2.<init>`. Moshi 1.11.0's
  codegen is compiled against Kotlin 1.4 and cannot run on the Kotlin 1.3.61 stdlib.
- Investigated candidate versions on Kotlin 1.3.61: Moshi **1.10.0 fails**, Moshi
  **1.11.0 fails**, Moshi **1.9.3 passes**.
- Combined with P1C2 (Moshi 1.8.0 cannot parse Kotlin 1.4 metadata), there is **no Moshi
  version valid for both Kotlin 1.3 and 1.4**. The Moshi bump and Kotlin 1.4.32 migration
  must be performed atomically.
- No dependency/source changes were committed; the working tree was restored to the P1C1
  green state (`kaptDebugKotlin` PASS, Moshi 1.8.0).
- Control-plane also corrected two stale `PROJECT_STATE.md` fields: the Feature HEAD now
  reflects the actual branch tip, and Current Blockers no longer reads "None".
- Required next step: authorize a coordinated P1C2 retry (Kotlin 1.4.32 + Moshi 1.11.0
  together).

## P1C2-P — Moshi 1.9.3 / Kotlin 1.4 Bridge Probe (2026-09-12) — BRIDGE_FOUND

- Probed the previously untested compatibility cell **Kotlin 1.4.32 + Moshi 1.9.3**.
- Result: `:app:kaptDebugKotlin` **PASS**, and full `clean assembleDebug
  testDebugUnitTest` **PASS** (314 tests / 25 suites / 0 failures) with the legacy
  `kotlin-android-extensions` plugin, `androidExtensions` block, and
  `kotlinx.android.parcel.Parcelize` unchanged.
- Committed the verified bridge (single atomic change per D013): Kotlin
  **1.3.61 → 1.4.32** and Moshi **1.8.0 → 1.9.3**. Kotlin stdlib now resolves coherently
  to the 1.4.32 family.
- Full compatibility matrix now: Kotlin 1.3.61 → Moshi 1.8.0 PASS, 1.9.3 PASS, 1.10.0 FAIL,
  1.11.0 FAIL; Kotlin 1.4.32 → Moshi 1.8.0 FAIL, **1.9.3 PASS**. Moshi 1.9.3 is the bridge.
- Remote CI green (run `34679658479`, commit `4f61a47`); APK identity/ABIs/native payload
  unchanged.
- No Parcelize/plugin migration performed (that is the P1C2 retry); no runtime/UI change.

## P1C2-R — Final Parcelize / Android Extensions Migration (2026-09-12) — PASS

- Replaced `apply plugin: 'kotlin-android-extensions'` with `kotlin-parcelize`; removed the
  obsolete `androidExtensions { experimental true }` block.
- Migrated all production imports `kotlinx.android.parcel.Parcelize` →
  `kotlinx.parcelize.Parcelize` (`App`, `Filesystem`, `Session`); model contracts,
  field order, Room annotations and the sealed `ServiceType` hierarchy unchanged.
- Added `LegacyAndroidExtensionsGuardTest` (forbids legacy Android Extensions in
  production source/build config) and `ParcelableContractTest` (Parcelable + static
  `CREATOR` for the model types).
- Local: `clean assembleDebug testDebugUnitTest` green — **317 tests / 27 suites / 0
  failures**. Remote (run `34711411103`, commit `0c7a814`): all steps success, same test
  result, debug APK uploaded.
- **P1C is now CLOSED / PASS.** Unchanged: Kotlin 1.4.32, Moshi 1.9.3, Gradle 6.7.1,
  AGP 4.2.2, SDK/NDK, other dependencies, runtime, visual design, assets.

## P1D1 — Barista Removal / AndroidTest Build Restoration (2026-09-12) — PASS

- Removed the JCenter-only `com.schibsted.spain:barista:3.1.0` androidTest dependency and
  migrated its usage to direct AndroidX Espresso (core/contrib/intents **3.2.0**); added
  `androidx.test.uiautomator:uiautomator:2.2.0` (previously transitive via Barista).
- `EspressoHelpers` retry now catches only `NoMatchingViewException` / `AssertionFailedError`;
  added Espresso equivalents for the six Barista helpers used by `MainActivityTest`.
- Added `BaristaRemovalGuardTest`.
- Restored `:app:assembleDebugAndroidTest` (previously failed: `Could not find
  com.schibsted.spain:barista:3.1.0`) and added it as a CI gate + artifact upload.
- Local: app + androidTest builds green; **318 tests / 28 suites / 0 failures**. Remote
  (run `34712929161`, commit `45abb83`): all steps success, both APKs uploaded.
- P1D remains IN PROGRESS; no production source change.

## P1D2 — Dead Play Services Dependency Cleanup (2026-09-12) — PASS

- Removed the unused direct `com.google.android.gms:play-services-base:17.2.1` dependency
  and the stale `ENABLE_PLAY_SERVICES` BuildConfig fields (default + debug override), after
  an audit found zero production/test consumers of `com.google.android.gms.*` or the flag.
- Verified `play-services-base` is **ABSENT** from `debugCompileClasspath`,
  `debugRuntimeClasspath` and `releaseRuntimeClasspath`; its merged-manifest entries
  (`GoogleApiActivity`, `com.google.android.gms.version`) are gone. Debug APK size dropped
  ~445 KB. No forced exclusions; no other dependency changed.
- Added `DeadPlayServicesGuardTest`.
- Local: **319 tests / 29 suites / 0 failures**; app + androidTest builds green. Remote
  (run `34730775325`, commit `aec54c4`): all steps success, both APKs uploaded.
- P1D remains IN PROGRESS; no production source change.

## P1D3 — Lifecycle Extensions Removal / ViewModelProvider Migration (2026-09-12) — PASS

- Removed the deprecated `androidx.lifecycle:lifecycle-extensions:2.2.0-alpha01` dependency
  and declared the granular artifacts actually used at stable **2.2.0**:
  `lifecycle-viewmodel` and `lifecycle-livedata`.
- Migrated `ViewModelProviders.of(...)` → `ViewModelProvider(...)` in `MainActivity` and six
  Fragments; the `ViewModelStoreOwner` (`this`) is unchanged, so Activity/Fragment scopes are
  preserved.
- Added `LifecycleModernizationGuardTest`.
- `lifecycle-extensions` is **ABSENT** from debug/release graphs; no other dependency version
  changed (Room 2.1.0-beta01, Navigation 2.1.0-alpha05, etc. untouched).
- Local: **320 tests / 30 suites / 0 failures**; app + androidTest builds green. Remote
  (run `34731759494`, commit `94ef87e`): all steps success, both APKs uploaded.
- P1D remains IN PROGRESS; no behavior/UI change.

## P1D4 — Navigation 2.1.0 Stable Migration (2026-09-12) — PASS

- Moved the shared `navigation_version` from 2.1.0-alpha05 to **2.1.0** stable (Safe Args
  Gradle plugin + `navigation-fragment-ktx` + `navigation-ui-ktx` remain driven by it).
- Aligned the Kotlin compile target with the existing Java 1.8 target
  (`kotlinOptions.jvmTarget = '1.8'`), required because Navigation 2.1.0's ktx inline
  bytecode targets JVM 1.8.
- No navigation source/graph change (nav graph XML byte-identical).
- Beneficial transitive stabilization: `androidx.fragment` → 1.1.0; `lifecycle-runtime` /
  `lifecycle-viewmodel-ktx` → 2.1.0. Direct Lifecycle artifacts remain 2.2.0.
- Added `NavigationStabilityGuardTest`.
- Local: **321 tests / 31 suites / 0 failures**; app + androidTest builds green. Remote
  (run `34733003165`, commit `9717d5b`): all steps success, both APKs uploaded.
- P1D remains IN PROGRESS; runtime/UI unchanged.
