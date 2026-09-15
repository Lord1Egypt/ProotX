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

## P1D5 — Room 2.1.0 Stable Migration (2026-09-12) — PASS

- Moved the shared `room_version` from 2.1.0-beta01 to **2.1.0** stable (room-runtime,
  room-compiler, room-testing remain driven by it).
- Zero database source change: `ProotXDatabase` version 7, entities, DAOs, queries,
  `Migration1To2`–`Migration6To7`, `Data.db` filename and schema export are untouched.
- Exported schema 7 is byte-identical (sha256 `3909bb12…`); no schema drift. No
  destructive-migration option added.
- Added `RoomStabilityGuardTest`.
- Local: **322 tests / 32 suites / 0 failures**; app + androidTest builds green. Remote
  (run `34734146196`, commit `a60a027`): all steps success, both APKs uploaded.
- P1D remains IN PROGRESS; database/runtime/UI unchanged.

## P1D6 — AndroidX Preference 1.1.0 Stable Migration (2026-09-12) — PASS

- Moved the shared `preference_version` from 1.1.0-alpha05 to **1.1.0** stable
  (`androidx.preference:preference` remains driven by it).
- Zero source/XML change: `SettingsFragment`, `preferences.xml`, preference keys, defaults,
  dependencies, `inputType="number"`, divider overrides and persistence are unchanged.
- Beneficial transitive stabilization: `androidx.appcompat` / `appcompat-resources`
  1.1.0-alpha05 → 1.1.0.
- Added `PreferenceStabilityGuardTest`.
- Local: **323 tests / 33 suites / 0 failures**; app + androidTest builds green. Remote
  (run `34734985029`, commit `787ab91`): all steps success, both APKs uploaded.
- Physical settings-screen verification (numeric input) deferred to the Golden Candidate
  gate. P1D remains IN PROGRESS; runtime/UI intentionally unchanged.

## P1D7 — Material Components 1.1.0 Stable Migration (2026-09-12) — BLOCKED

- Attempted Material `1.1.0-alpha06` → `1.1.0` stable.
- Build failed: generated `FragAppListBinding` — `cannot find symbol: class SwipeRefreshLayout`.
- Root cause: Material 1.1.0 stable's POM drops `androidx.legacy:legacy-support-core-ui`
  and `legacy-support-core-utils`; those were the only provider of
  `androidx.swiperefreshlayout:swiperefreshlayout:1.0.0`. The app uses `SwipeRefreshLayout`
  directly (`frag_app_list.xml`, `AppsListFragment`, `EspressoHelpers`).
- Minimal fix is an explicit `androidx.swiperefreshlayout:swiperefreshlayout` dependency —
  outside the milestone's "only Material moves" scope. Per the STOP rules, no dependency
  change was made; Material was reverted to 1.1.0-alpha06 and the branch is green.
- Required next step: authorize the explicit swiperefreshlayout dependency (then retry
  P1D7), or leave Material at 1.1.0-alpha06. No P1D7 commits.

## P1D7-U — SwipeRefreshLayout Ownership + Material Retry (2026-09-12) — BLOCKED

- Added the authorized explicit `androidx.swiperefreshlayout:swiperefreshlayout:1.0.0`
  (validated under Material 1.1.0-alpha06; `FragAppListBinding` compiled).
- Retried Material 1.1.0 stable. `SwipeRefreshLayout` now resolved, but a **second** removed
  transitive surfaced: `androidx.localbroadcastmanager:localbroadcastmanager:1.0.0` (also
  supplied by `androidx.legacy:legacy-support-core-utils`), consumed directly by
  `MainActivity` and `ServerService` → `Unresolved reference: LocalBroadcastManager`.
- That second dependency is outside P1D7-U's single-dependency authorization, so per Part H
  the experiment was stopped: both changes reverted; no commits; branch green.
- Required next step: authorize `androidx.localbroadcastmanager:localbroadcastmanager:1.0.0`
  in addition, then retry Material 1.1.0 stable.

## P1D7-U2 — Explicit Legacy Replacements + Material Final Retry (2026-09-12) — PASS

- Pre-flight audit of the `androidx.legacy` child artifacts found no third disappearing direct
  consumer (only SwipeRefreshLayout + LocalBroadcastManager).
- Declared both explicitly: `androidx.swiperefreshlayout:swiperefreshlayout:1.0.0` and
  `androidx.localbroadcastmanager:localbroadcastmanager:1.0.0` (validated under Material
  alpha06).
- Moved Material `1.1.0-alpha06` → **1.1.0** stable; the app's compile classpath no longer
  needs `androidx.legacy` (it remains only via `:terminal-term` at runtime and
  `espresso-contrib` in androidTest — legitimate parents, not excluded).
- Added `MaterialDependencyOwnershipGuardTest`.
- Local: **324 tests / 34 suites / 0 failures**; app + androidTest builds green. Remote
  (run `34738845782`, commit `7be3462`): all steps success, both APKs uploaded.
- **P1D7 is CLOSED.** No production source/resource change.

## P1D8 — Arch Core Testing 2.1.0 Stabilization (2026-09-12) — PASS

- Moved the test-only shared `core_testing_version` from 2.0.0-beta01 to **2.1.0** stable
  (`testImplementation` and `androidTestImplementation` remain driven by it), resolving the
  mixed Arch Core family (`core-testing` beta alongside `core-common`/`core-runtime` 2.1.0).
- No test-source change and no production change; `InstantTaskExecutorRule` consumers compile
  unchanged (8 JVM tests, 4 androidTest tests).
- Added `ArchCoreTestingGuardTest`.
- Local: **325 tests / 35 suites / 0 failures**; app + androidTest builds green. Remote
  (run `34740381789`, commit `b53ca39`): all steps success, both APKs uploaded.
- Note: Sentry and Billing are ACTIVE production dependencies; untouched.
- P1D remains IN PROGRESS.

## P1D9 — AndroidX Core KTX 1.1.0 Alignment (2026-09-12) — PASS

- Moved the direct `androidx.core:core-ktx` declaration from 1.0.2 to **1.1.0** stable
  (`ktx_version`), matching the already-resolved `androidx.core:core` 1.1.x family.
- No source change; `bundleOf` (core-ktx) and `ContextCompat`/`NotificationCompat` (core)
  consumers compile unchanged. No direct `androidx.collection` usage, so no direct
  `collection`/`core` dependency was added.
- Added `CoreKtxAlignmentGuardTest`.
- Local: **326 tests / 36 suites / 0 failures**; app + androidTest builds green. Remote
  (run `34741460145`, commit `99f27c0`): all steps success, both APKs uploaded.
- P1D remains IN PROGRESS; next action is the P1D Final Dependency Closure Audit.

## P1D Final Dependency Closure Audit (2026-09-15) — PASS

- Audited the complete direct dependency and plugin inventory across `root`, `:app`,
  `:terminal-term`, `:terminal-view`, and `:terminal-emulator`, and inspected the resolved
  graphs for `debugCompileClasspath`, `debugRuntimeClasspath`, `releaseRuntimeClasspath`,
  `debugAndroidTestCompileClasspath`, `debugAndroidTestRuntimeClasspath`, and the unit-test
  compile/runtime classpaths. **No dependency was changed.**
- **Zero active direct pre-release artifacts** and **zero pre-release transitives** in every
  resolved graph. `androidx.core:core-ktx` and `androidx.core:core` are coherent at `1.1.0`;
  `androidx.collection:collection` resolves `1.1.0`.
- `androidx.legacy` is **not** on the app compile classpath. It remains only via
  `:terminal-term` (runtime, `legacy-support-core-ui:1.0.0`) and via `room-testing` +
  `espresso-contrib` (androidTest, `legacy-support-core-utils:1.0.0`) — legitimate parents.
- **Sentry** (`io.sentry:sentry-android:1.7.22`) and **Billing**
  (`com.android.billingclient:billing-ktx:3.0.3`) confirmed **ACTIVE** production code
  (`SentryLogger`/`Logger` consumers; `BillingManager`/`BillingClient`/`Purchase`,
  `com.android.vending.BILLING`, `ProxyBillingActivity`); both left untouched.
- Dead-dependency audit: every direct production dependency has a concrete consumer
  (Sentry, Billing, Gson, SLF4J-nop backend, JArchiveLib, OkHttp, Moshi, Coroutines,
  ConstraintLayout, SwipeRefreshLayout, LocalBroadcastManager). **No proven-dead dependency
  found; nothing removed.**
- Deferred dependency debt recorded for later dedicated milestones: Coroutines `1.0.0`
  declaration superseded at resolution (`core` → `1.3.9` via `billing-ktx`, `android` →
  `1.1.1` via `lifecycle-viewmodel-ktx:2.1.0`); Sentry, Billing, OkHttp `3.14.7`,
  Moshi `1.9.3`, Gson `2.8.6`, JArchiveLib `0.8.0`, ConstraintLayout `1.1.3`,
  LocalBroadcastManager `1.0.0` (deprecated tech), and the JUnit4/Mockito/AndroidX-Test
  stack. AppCompat/Fragment/RecyclerView are consumed directly but supplied transitively;
  ownership hardening is deferred. **None of these blocks P1E.**
- Canonical verification: `:app:compileDebugKotlin`, `:app:kaptDebugKotlin`,
  `:app:compileDebugUnitTestKotlin`, `:app:compileDebugAndroidTestKotlin`,
  `:app:assembleDebugAndroidTest`, and `clean assembleDebug testDebugUnitTest` all
  **BUILD SUCCESSFUL**; **326 tests / 36 suites / 0 failures / 0 errors / 0 skipped**.
  Room 2.1.0 / schema 7 unchanged; merged debug manifest unchanged; APK keeps four ABIs and
  the native support payload. No production source, test source, dependency, manifest, or
  resource change.
- **P1D is CLOSED / PASS.** Next milestone: **P1E — SDK 36 / Manifest Compatibility
  (NOT STARTED)**.
- **Remote CI note:** the closure-documentation push (run `34918425938`, commit `7479089`)
  failed in the third-party `android-actions/setup-android@v3` bootstrap step
  (`Warning: Failed to find package 'tools'`), **before any ProotX build step**; the prior
  run on this workflow was green (`34741782765`). One rerun reproduced it. This is an
  upstream runner/SDK-repository change, not a repository regression, and is recorded as CI
  remediation debt (an authorized workflow/bootstrap fix is out of scope for this audit).

## CI-R1 — Android SDK Bootstrap Remediation (2026-09-15) — PASS

- Root cause confirmed: `android-actions/setup-android@v3` defaults to installing
  `tools platform-tools`; the legacy `tools` SDK package is no longer published, so the
  action ran `sdkmanager tools` → `Warning: Failed to find package 'tools'` and failed
  **before any ProotX step**. The runner image also reported a preinstalled
  `cmdline-tools;12.0` as "Wrong version", then downloaded cmdline-tools `16.0`.
- Minimal fix in `.github/workflows/build.yml`:
  - `setup-android@v3` now runs with `packages: ''` (skip its default extra-package install).
  - `platform-tools` added to the existing pinned `sdkmanager` invocation, alongside
    `platforms;android-30`, `platforms;android-29`, `build-tools;30.0.2`, `ndk;21.4.7075529`.
  - Corrected a stale stage-2 comment (Gradle 6.7.1 / AGP 4.2.2 / Kotlin 1.4.32).
  - No action major-version, pinned-package, trigger, or two-stage-JDK change.
- Remote run `34919847167` (push, commit `4f3c812`) — **success** (3m45s): SDK bootstrap PASS
  (`sdkmanager 16.0`; platform-tools + all pinned packages installed and verified); JDK 8
  Gradle 6.7.1 build PASS; **326 tests / 36 suites / 0 failures / 0 errors / 0 skipped**;
  `:app:assembleDebugAndroidTest` PASS; both artifacts uploaded
  (`prootx-debug-apk` ZIP 17,452,876 B; `prootx-debug-androidTest-apk` ZIP 1,367,404 B).
- No application source, dependency, Gradle/AGP/Kotlin, SDK/NDK, manifest, resource, runtime,
  UI, or database change.
- **CI-R1 CLOSED / PASS. P1D remains CLOSED / PASS. P1E NOT STARTED.**

## P1E0 — Android 16 Toolchain + Platform Readiness Audit (2026-09-15) — PASS

- Read-only audit defining the safe path from compileSdk/targetSdk **30 → 36**.
- Official API-36 requirement: **AGP 8.10 is the minimum** (AGP 8.9 caps at API 35); AGP 8.10
  needs Gradle **8.11.1**, Build Tools **35.0.0**, JDK **17**; AGP 8.11 needs Gradle 8.13.
- Kotlin 1.4.32 (KGP) cannot run on Gradle 8/AGP 8. Minimum KGP for AGP 8.10 is **2.2.0**;
  Kotlin 1.9.10 is the highest compatible with the last AGP 7 line, enabling an intermediate
  bridge. Moshi 1.9.3 (the Kotlin 1.4 bridge) must move with Kotlin; Navigation Safe Args
  2.1.0, Jacoco 0.8.4 and gradle-download-task 3.4.3 are Gradle-8-incompatible.
- AGP 8 breakage inventory: `namespace` required + manifest `package` removed (all 4 modules),
  `buildFeatures { buildConfig true }` required in `:app`, `lintOptions`→`lint`, custom-task
  API fixes. Non-transitive R verified **safe** (no cross-module `R` references).
- Platform findings: 2 exported fixes, 6 PendingIntents needing `FLAG_IMMUTABLE`, 2 FGS needing
  a `specialUse` type + `FOREGROUND_SERVICE_SPECIAL_USE`, `POST_NOTIFICATIONS` for visibility,
  a **HIGH** storage finding (`PermissionHandler` gates launch on non-grantable
  `READ/WRITE_EXTERNAL_STORAGE`), 1 custom dynamic receiver needing an export flag, and
  API 34/35/36 applicability (predictive back, edge-to-edge, large-screen).
- Structural findings: the shipped APK has **no `sharedUserId`**; `:app` references **no**
  `com.termux.*` (the embedded terminal library is unused by app code).
- Approved sequence P1E1–P1E9 + target stack recorded in
  `docs/P1E_ANDROID16_MIGRATION_PLAN.md`. Recommended target: **AGP 8.10.x / Gradle 8.11.1 /
  Kotlin 2.2.x / JDK 17 / Build Tools 35.0.0 / compileSdk 36 / targetSdk 36**, NDK 21.4
  preserved through P1E (P1F owns NDK/16 KB).
- **No source, manifest, Gradle, wrapper, workflow, or resource change.** P1E IN PROGRESS;
  next authorized candidate **P1E1** (not started).

## P1E1-P — Kotlin / AGP Build-Tooling Bridge Compatibility Probe (2026-09-15) — BRIDGE_FOUND

- Disposable compatibility probe; **all experimental edits reverted** (`git status` clean), no
  implementation committed.
- Proven intermediate bridge: **Gradle 7.6.4 / AGP 7.4.2 / Kotlin (KGP) 1.9.25 / Moshi 1.15.2
  (KAPT) / Navigation 2.3.5 / JDK 17 / NDK 21.4.7075529**, with **JaCoCo 0.8.8** (+
  `jacoco.excludes = ['jdk.internal.*']`) and test-only **Mockito 4.11.0**. compileSdk/
  targetSdk/minSdk unchanged (30/30/21; terminal 29/29/21).
- Probe findings: AGP 7.4.2 requires KGP ≥ 1.5.20; Safe Args 2.1.0 fails Gradle-7.6 task
  validation; Navigation 2.5.3's whole graph needs compileSdk ≥ 31, so **2.3.5** is the smallest
  working Navigation line; 7 behavior-neutral Kotlin-1.9 source fixes required; JaCoCo 0.8.4
  fails on JDK 17; Mockito 2.23.0/Byte Buddy 1.9 cannot run on JDK 17. `gradle-download-task`
  3.4.3 and the ktlint `JavaExec` task need **no** change.
- Transitive movement: fragment 1.1.0→1.2.4, lifecycle→2.2.0, activity→1.1.0, core 1.1.0→1.3.0
  (core-ktx stays 1.1.0), okio 1.17.2→3.7.0 (Moshi 1.15.2). No pre-release artifacts.
- Local canonical gate: `clean assembleDebug testDebugUnitTest` = **36 suites / 326 tests /
  0 failures / 0 errors / 0 skipped**, plus compile/kapt/androidTest PASS.
- P1E0 corrections recorded: API-36 minimum AGP is **8.9.1** (final target stays 8.10.x);
  KGP **1.9.20–1.9.25** supports AGP through 8.1.0 (not "1.9.10 highest for AGP 7.4"); Moshi
  1.15.x KAPT is a **Kotlin 1.9** bridge only — **KSP migration required before Kotlin 2.2**.
- Revised sequence: **P1E1** bridge → **P1E2** Moshi KAPT→KSP → **P1E3** AGP 8.10/Kotlin 2.2 →
  compileSdk/platform milestones. **P1E1 NOT STARTED.**

## P1E1 — Kotlin / AGP Build-Tooling Bridge (2026-09-15) — PASS

- Persisted the P1E1-P proven intermediate bridge:
  - `gradle/wrapper`: Gradle `6.7.1 → 7.6.4`.
  - root `build.gradle`: AGP `4.2.2 → 7.4.2`, Kotlin/KGP `1.4.32 → 1.9.25`, JaCoCo `0.8.4 →
    0.8.8`, Navigation `2.1.0 → 2.3.5`, `gradle-download-task 3.4.3 → 5.0.0` (see below).
  - `app/build.gradle`: Moshi `1.9.3 → 1.15.2` (KAPT), `kotlin_jdk_version 1.9.25`, Mockito
    `2.23.0 → 4.11.0` (test-only), explicit `ndkVersion "21.4.7075529"`, and
    `jacoco.excludes = ['jdk.internal.*']` for JDK 17.
  - `termux-app/terminal-emulator/build.gradle`: explicit `ndkVersion "21.4.7075529"`.
- Seven behavior-neutral Kotlin 1.9 compiler-contract edits: `else -> {}` on two non-exhaustive
  `when` statements; `<T : ViewModel?>` → `<T : ViewModel>` on five
  `ViewModelProvider.NewInstanceFactory.create` overrides. `NavigationStabilityGuardTest`
  expected version `2.1.0 → 2.3.5` (intent unchanged).
- CI: single **JDK 17** stage (JDK 8 build stage removed); pinned Build Tools `30.0.2 → 30.0.3`;
  CI-R1 `setup-android packages: ''` + explicit `sdkmanager` ownership retained.
- **CI-only correction:** P1E1-P's "download-task 3.4.3 works unchanged" held only locally,
  where the gitignored `jniLibs` bundle meant `:app:downloadAssets` never entered the graph. On
  a clean checkout it does, and Gradle 7.6 fails the `Download` task-property validation. Fixed
  per the P1E1-P PART M fallback with **`gradle-download-task:5.0.0`** (verified by executing
  the real `:app:fetchAssets` download path locally).
- Local canonical gate green: `clean assembleDebug testDebugUnitTest` = **36 suites / 326 tests /
  0 failures / 0 errors / 0 skipped**; Safe Args, Moshi codegen, Room KAPT, Parcelize and
  ViewBinding all PASS; NDK 21.4.7075529 used. Accepted warning: Moshi KAPT deprecation →
  **P1E2 KSP**.
- Remote run `34938888803` (commit `bd3f6e4`): all steps **success** (4m57s), single JDK 17
  stage, `:app:downloadAssets`/`fetchAssets` executed, `suites=36 tests=326 failures=0 errors=0
  skipped=0`, both artifacts uploaded.
- compileSdk/targetSdk/minSdk unchanged (30/30/21; terminal 29/29/21); source manifests
  byte-identical; runtime/UI unchanged.
- **P1E1 CLOSED / PASS. P1E IN PROGRESS. P1E2 NOT STARTED.**

## P1E2 — Moshi Codegen KAPT → KSP Migration (2026-09-15) — PASS

- Pinned **KSP `1.9.25-1.0.20`** (`ksp_version`) and added the KSP Gradle plugin
  (`com.google.devtools.ksp:symbol-processing-gradle-plugin`) to the root buildscript; applied
  `com.google.devtools.ksp` to **`:app` only** (Groovy buildscript style preserved).
- Moved Moshi code generation `kapt → ksp`:
  `ksp "com.squareup.moshi:moshi-kotlin-codegen:1.15.2"`; Moshi runtime stays `1.15.2`. **Room
  compiler stays on `kapt`** and `kotlin-kapt` remains applied — deliberate mixed build.
- Added `MoshiKspGuardTest` (asserts the KSP pin, Moshi-on-ksp, Moshi-not-on-kapt,
  Room-on-kapt, and both plugins applied). +1 suite / +1 test.
- Processor separation proven on a clean build: the two Moshi adapters
  (`GithubApiClient_ReleasesResponseJsonAdapter`, `GithubApiClient_GithubAssetJsonAdapter`)
  generate **only** under `app/build/generated/ksp/`; Room `*_Impl` classes generate **only**
  under `app/build/generated/source/kapt/`; no duplicates. The Moshi KAPT deprecation warning
  is **gone**.
- No production source change; no manifest/resource/SDK/wrapper/CI change. Room schema 7
  unchanged.
- Local canonical gate green: `clean assembleDebug testDebugUnitTest` = **37 suites / 327 tests
  / 0 failures / 0 errors / 0 skipped**; compile/kapt/androidTest/ktlint PASS.
- Remote run `34941411912` (commit `f84a18f`): all steps **success** (5m14s),
  `:app:kspDebugKotlin` + `:app:kaptDebugKotlin` both ran, no Moshi KAPT warning,
  `suites=37 tests=327 failures=0 errors=0 skipped=0`, both artifacts uploaded (normal APK
  19,938,198 B SHA `ffaed71f…`; androidTest APK 1,824,591 B SHA `d61a4539…`).
- **P1E2 CLOSED / PASS. P1E IN PROGRESS. P1E3-P NOT STARTED.**

## P1E3-P — AGP 8.10 / Kotlin 2.2 Compatibility Probe (2026-09-15) — BRIDGE_FOUND

- Disposable probe; **all experimental edits reverted** (`git status` clean). Proved ProotX can
  reach the modern stack **before** compileSdk 36:
  **Gradle 8.11.1 / AGP 8.10.1 / Kotlin 2.2.20 / KSP 2.2.20-2.0.4 / JDK 17 / Build Tools 35.0.0
  / NDK 21.4.7075529**, with Moshi on **KSP2**, Room 2.1.0 on **KAPT**, Navigation/Safe Args
  **2.3.5**, JaCoCo 0.8.8, gradle-download-task 5.0.0, Mockito 4.11.0; compileSdk/targetSdk/minSdk
  **30/30/21** (terminal 29/29/21).
- First raw blockers: Gradle 8 removed JaCoCo `xml.enabled`/`html.enabled` (→ `required`); AGP 8
  disables BuildConfig (→ `buildConfig true`); AGP 8 requires `namespace` (all four modules).
- Proven non-blockers: Safe Args 2.3.5 under AGP 8.10.1; Room 2.1.0 KAPT under Kotlin 2.2; NDK
  21.4 accepted; `downloadAssets` executes on Gradle 8; `ktlint`/`lintOptions`/`compileSdkVersion`
  deprecated-but-work; non-transitive R defaults fine (one androidTest ownership fix).
- Required behavior-neutral source contract fixes: 5× `toLowerCase(Locale.ENGLISH)` →
  `lowercase(Locale.ENGLISH)` (Kotlin 2.2); androidTest `R.id.terminal_view` →
  `com.termux.R.id.terminal_view` (non-transitive R ownership); `MoshiKspGuardTest` KSP pin.
- Full recipe + findings: `docs/P1E_ANDROID16_MIGRATION_PLAN.md` §20.
- Local canonical gate: `clean assembleDebug testDebugUnitTest` = **37 suites / 327 tests /
  0 failures / 0 errors / 0 skipped**; APK identity/ABIs/payload intact.
- **P1E3-P CLOSED / BRIDGE_FOUND. P1E IN PROGRESS. P1E3 NOT STARTED.**

## P1E3 — AGP 8.10 / Kotlin 2.2 Implementation (2026-09-15) — PASS

- Persisted the P1E3-P bridge: Gradle **7.6.4 → 8.11.1**, AGP **7.4.2 → 8.10.1**,
  Kotlin/KGP and stdlib **1.9.25 → 2.2.20**, KSP **1.9.25-1.0.20 → 2.2.20-2.0.4**, and
  CI Build Tools **30.0.3 → 35.0.0**. JDK 17 and NDK 21.4.7075529 remain pinned.
- Added the four required namespaces and enabled BuildConfig only in `:app`. Preserved AGP 8
  default non-transitive R/non-final IDs and qualified only the two terminal-owned resource
  references in `MainActivityTest`.
- Migrated JaCoCo XML/HTML report properties from `enabled` to `required`; retained JaCoCo
  **0.8.8**, filters, sources, dependencies, connected coverage inputs, and report formats.
- Applied exactly five behavior-neutral `toLowerCase(Locale.ENGLISH)` →
  `lowercase(Locale.ENGLISH)` substitutions across four production files.
- Moshi **1.15.2** remains on KSP2; Room **2.1.0** remains on KAPT; Navigation/Safe Args
  **2.3.5**, gradle-download-task **5.0.0**, and test-only Mockito **4.11.0** remain unchanged.
- SDK levels remain app **30/30/21** and terminal **29/29/21**. Source manifests, resources,
  runtime behavior, and UI behavior are unchanged.
- P1E3-R1 corrected both JaCoCo tasks from legacy `build/jacoco/testDebugUnitTest.exec` to AGP
  8's `build/outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec`. Before the
  fix the CI-oriented report was skipped for missing data. Local proof at `1828cdd`: task
  executed (not skipped), processed 331 classes, and generated parseable XML plus HTML.
- Remote CI run `34974083190` at `1828cdd4441a291433d07bd8a3e4efa96bcbfc76`: **SUCCESS**;
  Gradle 8.11.1/JDK 17, explicit Build Tools 35.0.0 + NDK 21.4 installation, canonical clean
  build, **37 suites / 327 tests / 0 failures / 0 errors / 0 skipped**, androidTest build, and
  both APK uploads passed. The workflow does not run the separate JaCoCo report task.
- Deferred without cleanup: manifest package warnings, Gradle-9 `JavaExec.main`, legacy Android
  DSL/`lintOptions`, `String.capitalize()`, configuration-time tasks, Node/action maintenance,
  `ndk.dir`, OkHttp/Okio runtime validation, future Room KSP migration, and core/core-ktx note.
- **P1E3-P CLOSED / BRIDGE_FOUND. P1E3 CLOSED / PASS. P1E IN PROGRESS. P1E4 COMPILESDK 36
  MIGRATION NOT STARTED / READY TO START.**

## P1E4 — compileSdk 36 Migration (2026-09-15) — PASS

- Raised only `:app` `compileSdkVersion` **30 → 36**. Preserved app targetSdk/minSdk
  **30/21**, terminal modules **29/29/21**, and the established Gradle 8.11.1 / AGP 8.10.1 /
  Kotlin 2.2.20 / KSP2 2.2.20-2.0.4 / JDK 17 / Build Tools 35.0.0 / NDK 21.4 toolchain.
- Changed CI's application platform package from `platforms;android-30` to
  `platforms;android-36`; retained `platforms;android-29`, Build Tools 35.0.0, and NDK
  21.4.7075529.
- API 36 exposes `PackageInfo.versionName` as nullable. The one authorized source-contract
  edit, `return info.versionName!!`, preserves `getProotXVersion(): String` and ProotX's
  existing invariant; it is not a targetSdk behavior change or intentional runtime feature.
- All local compile/codegen gates passed: Moshi KSP2 adapters, Room KAPT implementations,
  Safe Args, Parcelize, ViewBinding, BuildConfig, Kotlin/Java/unit/androidTest compilation,
  androidTest APK, ktlint, `downloadAssets`, and the four-ABI native build.
- JaCoCo regression gate executed (not skipped), loaded the AGP 8 unit-test execution data,
  processed 331 classes, and emitted non-empty parseable XML plus HTML. JaCoCo remains 0.8.8.
- Canonical `clean assembleDebug testDebugUnitTest` passed with **37 suites / 327 tests /
  0 failures / 0 errors / 0 skipped**. Dependency versions, Room 2.1.0 schema 1–7/migrations,
  source manifests, and merged-manifest behavior are unchanged.
- Local debug APK: 19,915,149 bytes, SHA-256
  `8f4974cd18f6ca3e17265e229008f7c8ab08deccebd11a24351932a9bb7a91f0`, package
  `io.github.lord1egypt.prootx`, versionName 1.0.0, SDK 36/30/21, four ABIs and 16/16 required
  native/support payloads. androidTest APK: 1,825,890 bytes, SHA-256
  `f2518d7aa35a1419ba1ab270e71b7b65fddaff0c65b5e19d5aec314d24403769`, package
  `io.github.lord1egypt.prootx.test`.
- Remote CI run `35013165950` at implementation `6d30b333b0a1d0b8ab0be966af4c3052dcf29500`:
  **SUCCESS**; explicit API 36/API 29/Build Tools 35.0.0/NDK 21.4 setup, canonical build,
  exact tests, androidTest build, and both artifacts passed.
- New compileSdk-36 warnings are deferred API deprecations (Safe Args `Bundle.get`, network,
  parcelable-extra/foreground-service, and display metrics). Existing manifest, Gradle 9,
  Kotlin annotation/`capitalize`, `ndk.dir`, native-strip, and action/Node warnings remain
  deferred. No intentional runtime or UI change.
- **P1E4 CLOSED / PASS. P1E IN PROGRESS. P1E5 API 31+ MANIFEST / PENDINGINTENT / RECEIVER
  COMPATIBILITY NOT STARTED / READY TO START.**

## P1E5 — API 31+ Manifest / PendingIntent / Receiver Compatibility (2026-09-16) — PASS

- Added `android:exported="true"` to `MainActivity` (`app/src/main/AndroidManifest.xml`) and to
  `TermuxActivity` (`termux-app/terminal-term/src/main/AndroidManifest.xml`), preserving
  TermuxActivity's existing `VIEW`/`DEFAULT`/`BROWSABLE` `ssh://` deep-link entry point.
- Made all six application-owned `PendingIntent` creations explicitly immutable:
  `NotificationConstructor.kt` session-list and settings (`FLAG_IMMUTABLE`) and stop-sessions
  (`FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE`); `TermuxService.java` notification content, exit
  service, and wake-lock toggle (`FLAG_IMMUTABLE`). Mutable count: **0**; unspecified-mutability
  count: **0**.
- Exactly four functional files changed; no Gradle, dependency, SDK-level, Room/schema/
  migration, resource, runtime, or UI change. Repository-wide `PendingIntent` inventory confirms
  no additional application-owned creation exists.
- Receiver classification (documented, not changed): `LocalBroadcastManager` server-result
  registration is in-process; `MainActivity`'s `DownloadManager.ACTION_DOWNLOAD_COMPLETE`
  registration listens to a system broadcast; the `TermuxActivity` custom
  `com.termux.app.reload_style` receiver is classified `RECEIVER_NOT_EXPORTED` and its explicit
  flag is deferred to targetSdk-34 work because `:terminal-term` compiles against API 29.
- Disposable probe: temporarily raised `:app` targetSdk 30 → 31; `:app:processDebugMainManifest`
  and `:app:assembleDebug` both passed, proving Android 12 exported-component requirements are
  satisfied; reverted to targetSdk 30 with a clean `git diff`. targetSdk remains **30**.
- Completed the previously interrupted `:app:assembleDebugAndroidTest` (`BUILD SUCCESSFUL`).
  Local gates passed: `:app:ktlint`, `:app:downloadAssets` (four ABI assets), and
  `:app:jacocoCoverageReportForCi` (executed, non-empty 788,365-byte XML / 391 classes + HTML).
- `:app:lintDebug` is diagnostic and fails on pre-existing legacy debt (14 errors, 131 warnings,
  3 hints: `Range` ×3, `UseRequireInsteadOfGet` ×10, and an intentional `ExpiredTargetSdkVersion`
  because targetSdk stays 30). No `UnspecifiedImmutableFlag`, `ExportedActivity`,
  `ExportedService`, or `ExportedReceiver` finding exists.
- Merged debug manifest: `MainActivity` exported `true`, `TermuxActivity` exported `true`,
  `TermuxService` exported `false`, `ProotXDocProvider` exported `true`, `ServerService`
  non-exported default; `targetSdkVersion="30"`. Local debug APK 19,915,145 bytes, SHA-256
  `860fb93744f8a83823d7476e797fa51d04f96a32c86b3fb04997f8cdeeaadc88`, package
  `io.github.lord1egypt.prootx`, versionName 1.0.0, SDK 36/30/21, four ABIs, 16/16 required
  payloads. androidTest APK 1,825,890 bytes, SHA-256
  `f2518d7aa35a1419ba1ab270e71b7b65fddaff0c65b5e19d5aec314d24403769`, package
  `io.github.lord1egypt.prootx.test`.
- Canonical `clean assembleDebug testDebugUnitTest` evidence retained (**37 suites / 327 tests /
  0 failures / 0 errors / 0 skipped**); the canonical build was not rerun because no functional
  file changed after the accepted run. Remote CI run `35020171430` at implementation
  `6e8a0559bc691266d403a216c56ec4377ce0c98b`: **SUCCESS**; explicit API 36/API 29/Build Tools
  35.0.0/NDK 21.4 setup, canonical build, exact tests, androidTest build, and both artifact
  uploads passed.
- No intentional runtime or UI change.
- **P1E5 CLOSED / PASS. P1E IN PROGRESS. P1E6 STORAGE / PERMISSION RUNTIME COMPATIBILITY READY
  TO START.**

## P1E6 — Storage / Permission Runtime Compatibility (2026-09-16) — PASS

- Removed `<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />` and
  `<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />` from
  `app/src/main/AndroidManifest.xml`, and `WRITE_EXTERNAL_STORAGE` from
  `termux-app/terminal-term/src/main/AndroidManifest.xml`. No replacement permission
  (`MANAGE_EXTERNAL_STORAGE`, `READ_MEDIA_*`) was added; other permissions are unchanged.
- Deleted `app/src/main/java/io/github/lord1egypt/prootx/utils/PermissionHandler.kt` after
  proving it had zero remaining production callers.
- `MainActivity.appHasBeenSelected` and `sessionHasBeenSelected` now proceed directly to
  `viewModel.submitAppSelection` / `viewModel.submitSessionSelection`; the storage-permission
  branch and the storage-only `onRequestPermissionsResult` override were removed.
- `FilesystemEditFragment` (SAF `ACTION_OPEN_DOCUMENT` import) and `FilesystemListFragment`
  (SAF `ACTION_CREATE_DOCUMENT` export) no longer gate on `PermissionHandler`; the SAF intents,
  suggested backup filename, `setFilesystemToBackup`, and `ContentResolver` export flow are
  unchanged.
- `TermuxActivity.java`: removed the unreferenced-elsewhere storage helper
  `ensureStoragePermissionGranted()`, the `REQUESTCODE_PERMISSION_STORAGE` constant, the
  `"storage"` reload branch that called it, and the now-unused `android.Manifest`,
  `android.annotation.TargetApi`, `android.content.pm.PackageManager`, and `android.os.Build`
  imports. Terminal session creation, SSH parsing, notifications, receiver registration, and UI
  are untouched.
- `MainActivityTest` (androidTest) dropped its `GrantPermissionRule` for the removed permissions
  and the now-unused `Manifest`/`GrantPermissionRule` imports.
- Added `app/src/test/java/io/github/lord1egypt/prootx/architecture/StoragePermissionGuardTest.kt`
  (2 tests) forbidding `PermissionHandler` and legacy broad-storage permission references in
  production source and source manifests.
- App-scoped storage paths are unchanged: `filesDir`, `getExternalFilesDir(null)`,
  `getExternalFilesDirs(null)`, `emulatedScopedDir`, `emulatedUserDir`, `sdCardScopedDir`,
  `sdCardUserDir`, the `/storage/internal` and optional `/storage/sdcard` bindings, and the
  `AssetDownloader` `emulatedScopedDir/downloads` destination.
- Disposable targetSdk 33 probe: `:app:processDebugMainManifest` and `:app:assembleDebug`
  passed; the merged manifest and APK contained no `READ_EXTERNAL_STORAGE`,
  `WRITE_EXTERNAL_STORAGE`, or `MANAGE_EXTERNAL_STORAGE`; reverted to targetSdk **30** with no
  Gradle diff.
- Local gates passed: all compile/codegen tasks, `:app:assembleDebugAndroidTest`, `:app:ktlint`,
  `:app:downloadAssets`, and `:app:jacocoCoverageReportForCi` (executed, non-empty 782,963-byte
  XML / 389 classes + HTML) via the clean/report-only ordering. Canonical
  `clean assembleDebug testDebugUnitTest` = **38 suites / 329 tests / 0 failures / 0 errors /
  0 skipped** (2 new guard tests).
- Final debug APK 19,912,535 bytes, SHA-256
  `83d24aedb2582f26736b6e2f4808e6a543373b1cce469b8c96ce9befd31706fd`, package
  `io.github.lord1egypt.prootx`, versionName 1.0.0, SDK 36/30/21, four ABIs, 16/16 required
  payloads; permissions are exactly ACCESS_NETWORK_STATE, INTERNET, BILLING, CHANGE_WIFI_STATE,
  FOREGROUND_SERVICE, WAKE_LOCK, VIBRATE. androidTest APK 1,825,680 bytes, SHA-256
  `5720dd54a6b07a1f8569b5e65e481c80e69a5d8cd1f1f6ac125c0195f474ebcd`, package
  `io.github.lord1egypt.prootx.test`.
- Remote CI run `35028617203` at implementation
  `2202d6bda33512d3312827bf2bd6dc17f47dbae9`: **SUCCESS**; JDK 17, Gradle 8.11.1, API 36/API 29,
  Build Tools 35.0.0, NDK 21.4.7075529, canonical build, **38 suites / 329 tests / 0 failures /
  0 errors / 0 skipped**, androidTest build, and both artifact uploads passed.
- **Intentional runtime behavior change:** app/session launch, filesystem import/export, and
  embedded-terminal use no longer require the legacy broad-storage permissions. No UI redesign,
  no data-location migration, and no filesystem architecture change. The unreachable legacy
  permission-continuation ViewModel code and unused dialog strings are deferred as dead legacy
  follow-up.
- **P1E6 CLOSED / PASS. P1E IN PROGRESS. P1E7 FOREGROUND SERVICE / NOTIFICATION COMPATIBILITY
  READY TO START.**

## P1E7 — Foreground Service / Notification Compatibility (2026-09-16) — PASS

- Declared `<uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />`
  in `app/src/main/AndroidManifest.xml` alongside the retained `FOREGROUND_SERVICE`.
  `POST_NOTIFICATIONS` is intentionally **not** declared or requested in P1E7.
- Typed `io.github.lord1egypt.prootx.ServerService` as
  `android:foregroundServiceType="specialUse"` with a
  `android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE` property ("Runs user-initiated local Linux
  sessions and keeps their local SSH VNC and X11 server processes alive while the user uses those
  sessions"); `android:stopWithTask="true"` and the non-exported default are unchanged.
- Overlaid `com.termux.app.TermuxService` from the API-36 app manifest with
  `android:foregroundServiceType="specialUse"` and a subtype property ("Keeps user-initiated
  interactive terminal sessions and their child processes alive while the user switches between
  ProotX terminal and client applications"), preserving `android:exported="false"`. The
  `:terminal-term` manifest is **unchanged** (the module still compiles against API 29, whose AAPT
  cannot recognize the newer enum); the overlay merges into exactly one component.
- `ServerService.onCreate()` now calls `notificationManager.createServiceNotificationChannel()`, so
  the service owns its own foreground-notification prerequisite; `MainActivity`'s now-redundant
  `NotificationConstructor` channel-initialization property and call were removed (the class is
  still used by `ServerService`).
- `ServerService` promotes to the foreground **synchronously** in `onStartCommand` for
  `type = "start"` via a new `promoteToForeground()` helper, before the asynchronous
  `startSession` coroutine is scheduled; the redundant `startForeground` call inside `startSession`
  was removed. Promotion uses three-argument `startForeground(..., ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST)`
  on API 29+ and the existing two-argument call below.
- Only the initial session launch changed in `MainActivity.startSession()`:
  `startForegroundService(serviceIntent)` on API 26+, `startService(serviceIntent)` below. The
  already-running-service commands (`restartRunningSession`, `stopApp`, `filesystemIsBeingDeleted`,
  `kill`, and the notification `stopAll` PendingIntent) keep their existing `startService`
  semantics.
- `TermuxActivity` now calls `startForegroundService(serviceIntent)` on API 26+ for the initial
  terminal-service start and retains `doBindService(serviceIntent)`; `TermuxService.onCreate()`
  creates the shared `"ProotX"` channel (`IMPORTANCE_LOW`) itself before promoting, and uses
  `ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST` on API 29+ (two-argument `startForeground` below).
  SSH parsing, session behavior, and binding semantics are unchanged.
- Notification PendingIntents are unchanged (six immutable; mutable count **0**), and the
  notification trampoline audit found no notification action that launches an activity.
- Disposable targetSdk 34 probe: `:app:processDebugMainManifest` and `:app:assembleDebug` both
  passed; the merged manifest had one `ServerService` (`foregroundServiceType=specialUse` +
  subtype property), one `TermuxService` (`exported=false`, `specialUse`, subtype property),
  `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_SPECIAL_USE` present, and `POST_NOTIFICATIONS`
  absent; reverted to targetSdk **30** with no Gradle diff.
- Added `app/src/test/java/io/github/lord1egypt/prootx/architecture/ForegroundServiceCompatibilityGuardTest.kt`
  (8 tests) guarding the permission set, `POST_NOTIFICATIONS` absence, both `specialUse`
  declarations + subtype properties, the single-component overlay, service-owned channels,
  `startForegroundService`/`FOREGROUND_SERVICE_TYPE_MANIFEST` usage, and the unchanged terminal SDK
  levels.
- Local gates passed: Kotlin/Java/unit/androidTest compilation, KSP2 Moshi, KAPT Room, Safe Args,
  Parcelize, ViewBinding, BuildConfig, `:app:assembleDebugAndroidTest`, `:app:ktlint`,
  `:app:downloadAssets`, and `:app:jacocoCoverageReportForCi` (executed from the clean/report-only
  state; non-empty 782,925-byte XML + HTML). Canonical `clean assembleDebug testDebugUnitTest` =
  **39 suites / 337 tests / 0 failures / 0 errors / 0 skipped** (+1 guard suite).
- Final debug APK 19,913,063 bytes, SHA-256
  `6c3dc42adaaa1b866319f73768b714846768373ea659bef388af154dac63ff52`, package
  `io.github.lord1egypt.prootx`, versionName 1.0.0, SDK 36/30/21, four ABIs, 16/16 required
  payloads; permissions are exactly ACCESS_NETWORK_STATE, INTERNET, BILLING, CHANGE_WIFI_STATE,
  FOREGROUND_SERVICE, FOREGROUND_SERVICE_SPECIAL_USE, WAKE_LOCK, VIBRATE. androidTest APK
  1,825,680 bytes, SHA-256
  `5720dd54a6b07a1f8569b5e65e481c80e69a5d8cd1f1f6ac125c0195f474ebcd`, package
  `io.github.lord1egypt.prootx.test` (byte-identical to P1E6).
- Remote CI run `35032934977` at implementation
  `0e70b7e1e3f398eb6fdb92730542cae0da6f1975`: **SUCCESS**; JDK 17, Gradle 8.11.1, API 36/API 29,
  Build Tools 35.0.0, NDK 21.4.7075529, canonical build, **39 suites / 337 tests / 0 failures /
  0 errors / 0 skipped**, androidTest build, and both artifact uploads passed.
- No intentional runtime or UI change: notification IDs, actions, PendingIntents, session
  lifecycle, SSH behavior, dependency versions, Room schema/migrations, and app-scoped storage
  paths are unchanged. `POST_NOTIFICATIONS` sequencing correction recorded in `DECISIONS.md` D031.
- **P1E7 CLOSED / PASS. P1E IN PROGRESS. P1E8 TARGETSDK 33/34 RUNTIME COMPATIBILITY READY TO
  START.**
