# ProotX Engineering Decisions

> Durable decision log. Each entry records *why* a choice was made so future sessions do
> not relitigate it. This file is authoritative for architecture changes; `TASKS.md`
> tracks progress.

---

## D001 — ProotX 1.0.0 baseline is frozen and immutable

- **Date:** 2026-09-12
- **Status:** Accepted
- **Decision:** The ProotX 1.0.0 baseline at commit
  `94abf5fa520255bb10d087a6be3ba2bc70b0e127` (annotated tag `v1.0.0-baseline`) is frozen
  and must not be altered or rewritten.
- **Reason:** Modernization needs a reproducible recovery point and a stable reference for
  regression comparison.
- **Alternatives considered:** Continue editing on `main`; move the baseline forward as
  work lands.
- **Trade-offs:** Requires explicit branch/tag discipline, but guarantees recoverability.
- **Affected components:** Repository history, tags, release process.

---

## D002 — `main` is stable; development flows through `develop` and feature branches

- **Date:** 2026-09-12
- **Status:** Accepted
- **Decision:** `main` stays stable and does not receive direct development commits.
  Active work happens on `develop` and short-lived `feature/*` branches.
- **Reason:** Keeps a trustworthy stable ref while allowing parallel, reviewable work.
- **Alternatives considered:** Trunk-based development directly on `main`.
- **Trade-offs:** Slightly more branch bookkeeping; lower risk of destabilizing `main`.
- **Affected components:** Branching model, CI triggers, release process.

---

## D003 — Large changes are divided into explicit milestones with STOP gates

- **Date:** 2026-09-12
- **Status:** Accepted
- **Decision:** ProotX 2.0 is executed as named milestones (P0, P0.5, P1x, …). Each
  milestone ends at an explicit STOP gate; no milestone begins without authorization.
- **Reason:** Limits blast radius, keeps scope honest, and lets a fresh session resume
  safely without guessing.
- **Alternatives considered:** A single continuous modernization effort.
- **Trade-offs:** More coordination overhead; far better reviewability and safety.
- **Affected components:** Entire program, control-plane documents.

---

## D004 — Physical acceptance and source/test acceptance are different things

- **Date:** 2026-09-12
- **Status:** Accepted
- **Decision:** Passing build/unit tests ("source/test acceptance") does not by itself mean
  a change is accepted for release; a change may require physical-device acceptance as a
  separate step.
- **Reason:** This app manipulates PTYs, PRoot, filesystems and networking, which unit
  tests cannot fully validate on-device.
- **Alternatives considered:** Treating a green CI/unit run as sufficient acceptance.
- **Trade-offs:** Longer acceptance cycles; avoids shipping device-only regressions.
- **Affected components:** CI, release process, P1G regression acceptance.

---

## D005 — Future versions distinguish candidate builds from last physically accepted baseline

- **Date:** 2026-09-12
- **Status:** Accepted (direction)
- **Decision:** Future releases should clearly separate a *candidate* build from the *last
  physically accepted* baseline, rather than a single continuously moving version.
- **Reason:** Provides a stable "known good" reference that device testing can bless
  independently of whatever is being built.
- **Alternatives considered:** A single always-incrementing version (current behavior).
- **Trade-offs:** Extra version bookkeeping; clearer acceptance provenance.
- **Affected components:** Gradle versioning, release contract, acceptance workflow.

---

## D006 — Runtime/UI behavior is invariant during toolchain-only modernization

- **Date:** 2026-09-12
- **Status:** Accepted
- **Decision:** During toolchain-only modernization, runtime and UI behavior must remain
  invariant unless a specific milestone explicitly authorizes a behavior change.
- **Reason:** Isolates "does it still build/run after upgrades" from "did we change
  behavior", making regressions attributable.
- **Alternatives considered:** Refactoring behavior alongside toolchain upgrades.
- **Trade-offs:** Some cleanup is deferred; much easier regression analysis.
- **Affected components:** P1 modernization increments, runtime, UI.

---

## D007 — ProotX moves toward a generic, data-driven PRoot runtime

- **Date:** 2026-09-12
- **Status:** Accepted (direction)
- **Decision:** ProotX 2.0 will move toward a generic PRoot runtime driven by catalog/image
  data instead of distro-specific Android-side logic.
- **Reason:** Reduces per-distro code and enables a dynamic catalog and OCI/rootfs support.
- **Alternatives considered:** Continuing per-distro code paths.
- **Trade-offs:** Requires Runtime V2 design and catalog work; more scalable and simpler
  long-term.
- **Affected components:** Runtime, catalog, asset delivery.

---

## D008 — Multi-instance simultaneous runtime is a core ProotX 2.0 requirement

- **Date:** 2026-09-12
- **Status:** Accepted (requirement)
- **Decision:** Supporting multiple Linux instances running simultaneously is a core
  product requirement of ProotX 2.0 (not an optional enhancement).
- **Reason:** Power users expect concurrent environments; it shapes runtime, process/PTY,
  filesystem and port management design.
- **Alternatives considered:** Keeping the single-active-session model.
- **Trade-offs:** Significant runtime rework and per-instance isolation work.
- **Affected components:** Runtime V2, networking, storage, UI.

---

## D009 — Google Play and Full capabilities may require separate build flavors

- **Date:** 2026-09-12
- **Status:** Accepted (direction)
- **Decision:** Achieve Google Play compatibility and Full-distribution capabilities via
  separate build flavors if platform policy requires it.
- **Reason:** Some capabilities (e.g. select package installation/runtime behavior) may not
  be permissible on Play, while Full distribution should retain them.
- **Alternatives considered:** A single distribution constrained to Play rules.
- **Trade-offs:** Added build/release complexity; preserves both reach and capability.
- **Affected components:** Gradle flavors, CI/release, catalog, runtime.

---

## D010 — CI uses a two-stage JDK bootstrap

- **Date:** 2026-09-12
- **Status:** Accepted
- **Decision:** Continuous integration runs Android SDK/NDK provisioning under a modern
  JDK (JDK 17) and then runs the frozen Gradle/application build under JDK 8, in explicit
  sequential stages. The required SDK/NDK packages are pinned by the workflow.
- **Reason:** Android command-line tools (`sdkmanager`) require a modern JVM, while the
  frozen Gradle 5.1.1 / AGP 3.4.3 build only supports JDK 8. Running either stage under the
  wrong JDK fails.
- **Alternatives considered:** Upgrading Gradle/AGP to allow a single modern JDK (rejected
  for P1A — that is P1B+ modernization, out of scope); relying on runner-preinstalled SDK
  packages (rejected — non-deterministic).
- **Trade-offs:** The workflow is slightly more complex and must keep the two stages
  distinct, but the legacy build is preserved and CI is reproducible.
- **Affected components:** `.github/workflows/build.yml`, CI foundation, P1B+.

---

## D011 — Gradle 6.7.1 / AGP 4.2.2 is an intentional bridge toolchain

- **Date:** 2026-09-12
- **Status:** Accepted
- **Decision:** Android build-tool modernization proceeds through an explicit bridge step:
  Gradle 6.7.1 with AGP 4.2.2, keeping Kotlin 1.3.61, `compileSdk`/`targetSdk` 30 and
  NDK 21.4.7075529 unchanged. This is not the final toolchain; further migration (e.g. AGP
  7/8, Kotlin upgrade, synthetics removal) happens in later milestones.
- **Reason:** A direct jump from the 2019-era toolchain to AGP 8/9 crosses the Kotlin
  synthetics removal and the JDK 8→11 boundary at the same time, which would entangle
  unrelated failures. AGP 4.2+ pairs officially with Gradle 6.7.1, stays on the Android 30
  generation, and matches the NDK 21 generation.
- **Alternatives considered:** Jumping directly to a modern AGP/Kotlin (rejected — too many
  simultaneous variables); staying on AGP 3.4.3 (rejected — no forward progress).
- **Trade-offs:** An extra intermediate state to migrate through, but each migration step is
  independently verifiable and reversible.
- **Affected components:** Gradle wrapper, root `build.gradle`, CI, P1C+.

---

## D012 — UI view access uses View Binding; Kotlin synthetics are forbidden

- **Date:** 2026-09-12
- **Status:** Accepted
- **Decision:** Programmatic view access uses Android **View Binding**. Kotlin Android
  synthetic view imports (`kotlinx.android.synthetic`) must not be reintroduced; a unit
  test (`SyntheticViewImportsTest`) enforces this. Legacy `kotlinx.android.parcel.Parcelize`
  remains permitted until the `kotlin-android-extensions` plugin is removed in P1C2.
- **Reason:** The synthetics API was removed in Kotlin 1.8 and blocks Kotlin modernization;
  View Binding is null-safe, compile-time checked, and the standard modern mechanism.
- **Alternatives considered:** `findViewById` (rejected — untyped, error-prone);
  Data Binding (rejected — heavier, not needed).
- **Trade-offs:** Generated binding classes add minor build surface; fragments must clear
  binding in `onDestroyView` to avoid leaks.
- **Affected components:** `app/build.gradle`, `MainActivity`, `ui/*Fragment`, tests, P1C2.

---

## D013 — Kotlin and Moshi upgrades must move together

- **Date:** 2026-09-12
- **Status:** Accepted
- **Decision:** The Kotlin upgrade (1.3.61 → 1.4.32) and the Moshi upgrade
  (1.8.0 → 1.11.0) are treated as a single atomic change. Neither is applied alone.
- **Reason:** There is no Moshi version compatible with both Kotlin generations. Moshi
  1.8.0's `moshi-kotlin-codegen` cannot parse Kotlin 1.4 metadata (kapt
  `KotlinNullPointerException`); conversely, Moshi ≥1.10.0's codegen is compiled against
  Kotlin 1.4 and fails on Kotlin 1.3.61 with
  `NoSuchMethodError: kotlin.jvm.internal.FunctionReferenceImpl.<init>(...)` (observed:
  1.10.0 and 1.11.0 fail on 1.3.61; 1.9.3 passes on 1.3.61). The two version ranges do not
  overlap.
- **Alternatives considered:** Bumping Moshi alone on Kotlin 1.3.61 (rejected — verified to
  break kapt); bumping Kotlin alone (rejected — verified to break kapt); staying on Moshi
  1.9.3 (rejected — does not address Kotlin 1.4 support and is unverified for it).
- **Trade-offs:** The coordinated migration is larger and must be validated as one unit, but
  it is the only viable path.
- **Affected components:** `app/build.gradle`, `build.gradle`, kapt, P1C2 retry.

---

## D014 — Moshi 1.9.3 is the Kotlin 1.3 → 1.4 bridge

- **Date:** 2026-09-12
- **Status:** Accepted
- **Decision:** Use Moshi **1.9.3** as the bridge version for the Kotlin 1.4.32 migration.
  The verified compatibility matrix is: Kotlin 1.3.61 with Moshi 1.8.0 PASS / 1.9.3 PASS /
  1.10.0 FAIL / 1.11.0 FAIL; Kotlin 1.4.32 with Moshi 1.8.0 FAIL / **1.9.3 PASS**.
- **Reason:** Moshi 1.9.3 is the only tested version that works on both Kotlin generations,
  which lets the Kotlin upgrade proceed on a small hop without simultaneously jumping Moshi
  to 1.11.0.
- **Alternatives considered:** Coordinated jump to Kotlin 1.4.32 + Moshi 1.11.0 (still
  viable but a larger step; not needed now that 1.9.3 bridges); Moshi 1.9.3 on Kotlin 1.3.61
  alone (does not advance Kotlin).
- **Trade-offs:** Moshi stays on 1.9.3 for now; a later Moshi bump (e.g. 1.11.x) can happen
  in P1D once Kotlin is stable.
- **Affected components:** `app/build.gradle`, kapt, P1C2 retry, P1D.

---

## D015 — Parcelize uses `kotlin-parcelize`; legacy Android Extensions removed

- **Date:** 2026-09-12
- **Status:** Accepted
- **Decision:** The project uses the standalone `kotlin-parcelize` plugin with
  `kotlinx.parcelize.Parcelize`. The legacy `kotlin-android-extensions` plugin, the
  `androidExtensions {}` DSL, `kotlinx.android.parcel`, and `kotlinx.android.synthetic` are
  permanently removed and enforced by guard tests.
- **Reason:** `kotlin-android-extensions` was removed in Kotlin 1.8 and blocked Kotlin
  modernization; Parcelize was split into `kotlin-parcelize` in Kotlin 1.4.20.
  `LegacyAndroidExtensionsGuardTest` prevents reintroduction.
- **Alternatives considered:** Keeping the legacy plugin (rejected — removed in Kotlin 1.8);
  hand-written `Parcelable` (rejected — unnecessary churn).
- **Trade-offs:** None material; Parcelize behavior is unchanged and covered by
  `ParcelableContractTest`.
- **Affected components:** `app/build.gradle`, model entities, tests, P1D+.

---

## D016 — androidTest uses AndroidX Espresso; Barista is forbidden

- **Date:** 2026-09-12
- **Status:** Accepted
- **Decision:** Instrumented UI tests use direct AndroidX Espresso (core/contrib/intents at
  one coherent 3.2.0 family) plus `uiautomator`. The JCenter-only
  `com.schibsted.spain:barista` dependency is removed and its reinintroduction is blocked by
  `BaristaRemovalGuardTest`. CI compiles `:app:assembleDebugAndroidTest` so androidTest
  dependency resolution is a standing gate.
- **Reason:** Barista 3.1.0 was published only to JCenter, which is sunset, so androidTest
  no longer resolved; direct Espresso uses supported Google/Maven Central artifacts.
- **Alternatives considered:** Upgrading to a newer Barista release under a different
  artifact coordinate (rejected — the project wants one standard testing stack, no third-
  party abstraction).
- **Trade-offs:** Test helpers are slightly more verbose; a full Parcel/device round-trip is
  still deferred to the Golden Candidate gate.
- **Affected components:** `app/build.gradle`, `app/src/androidTest`, CI, P1D.

---

## D017 — No direct Play Services base dependency; `ENABLE_PLAY_SERVICES` removed

- **Date:** 2026-09-12
- **Status:** Accepted
- **Decision:** ProotX does not declare `com.google.android.gms:play-services-base`
  directly, and the `ENABLE_PLAY_SERVICES` BuildConfig flag is removed. Both were unused
  (no production/test consumer) and are enforced by `DeadPlayServicesGuardTest`.
- **Reason:** The dependency and flag were dead; removing them shrinks the APK (~445 KB)
  and removes the `GoogleApiActivity` / `com.google.android.gms.version` manifest injections
  that came from play-services-base. `play-services-base` is now absent from all
  configurations.
- **Alternatives considered:** Keeping the dependency "in case it is needed" (rejected —
  dead weight); force-excluding it transitively (unnecessary — it is fully absent, so no
  exclusions were added).
- **Trade-offs:** If Play services functionality is ever needed, a dedicated milestone must
  add the specific artifact and update the guard.
- **Affected components:** `app/build.gradle`, tests, P1D3+.

---

## D018 — Granular Lifecycle artifacts; `ViewModelProvider` API

- **Date:** 2026-09-12
- **Status:** Accepted
- **Decision:** ProotX does not use the deprecated monolithic
  `androidx.lifecycle:lifecycle-extensions`. It declares only the granular artifacts the
  source needs at stable **2.2.0** (`lifecycle-viewmodel`, `lifecycle-livedata`) and acquires
  ViewModels with direct `ViewModelProvider(...)` construction. `LifecycleModernizationGuardTest`
  prevents reintroduction.
- **Reason:** `lifecycle-extensions` is deprecated and pulled in unused artifacts
  (`lifecycle-process`, `lifecycle-service`); `ViewModelProviders.of(...)` is deprecated in
  favor of `ViewModelProvider(...)`.
- **Alternatives considered:** Keeping `lifecycle-extensions` (rejected — deprecated);
  using `by viewModels()`/`fragment-ktx` delegates (deferred — would couple to Fragment KTX
  versions and is out of scope).
- **Trade-offs:** `lifecycle-runtime` remains transitive at the version navigation requests
  (2.1.0-beta01) since the source does not use its APIs directly; no force was applied.
- **Affected components:** `app/build.gradle`, `MainActivity`, `ui/*Fragment`, tests, P1D4+.

---

## D019 — Navigation pinned to stable 2.1.0; Kotlin JVM target 1.8

- **Date:** 2026-09-12
- **Status:** Accepted
- **Decision:** The shared `navigation_version` is **2.1.0** (stable) for the Safe Args
  plugin and the runtime Navigation artifacts. The Kotlin compile target is set to
  `jvmTarget = '1.8'` to match the already-declared Java 1.8 source/target.
- **Reason:** Stabilize off the 2.1.0-alpha05 pre-release within the same Navigation line;
  Navigation 2.1.0's ktx inline bytecode targets JVM 1.8, so mismatched Kotlin 1.6 caused a
  compile failure. `NavigationStabilityGuardTest` prevents accidental pre-release regression.
- **Alternatives considered:** Jumping to a newer Navigation generation (rejected — out of
  scope); leaving the Kotlin target at 1.6 (rejected — incompatible with 2.1.0 bytecode).
- **Trade-offs:** None material; beneficial transitive stabilization of `fragment` (1.1.0)
  and `lifecycle-runtime`/`lifecycle-viewmodel-ktx` (2.1.0).
- **Affected components:** root `build.gradle`, `app/build.gradle`, tests, P1D5+.

---

## D020 — Room pinned to stable 2.1.0; database schema and migrations immutable

- **Date:** 2026-09-12
- **Status:** Accepted
- **Decision:** The shared `room_version` is **2.1.0** (stable) for room-runtime,
  room-compiler and room-testing. The database version (7), entities, DAOs, queries,
  migration classes (`Migration1To2`–`Migration6To7`), the `Data.db` filename and the
  exported schema history remain unchanged. `RoomStabilityGuardTest` prevents an accidental
  pre-release regression.
- **Reason:** Stabilize off the 2.1.0-beta01 pre-release within the same Room line without
  touching the database contract; schema 7 is byte-identical (no drift).
- **Alternatives considered:** Staying on 2.1.0-beta01 (rejected — pre-release); a Room
  architecture refactor (out of scope).
- **Trade-offs:** None material; migration tests remain instrumented (device execution
  deferred to the Golden Candidate gate).
- **Affected components:** `app/build.gradle`, tests, P1D6+.

---

## D021 — Preference pinned to stable 1.1.0; settings semantics frozen

- **Date:** 2026-09-12
- **Status:** Accepted
- **Decision:** The shared `preference_version` is **1.1.0** (stable) for
  `androidx.preference:preference`. Preference keys, defaults, dependencies, persistence
  backend, XML and `SettingsFragment` remain frozen. `PreferenceStabilityGuardTest` prevents
  an accidental pre-release regression.
- **Reason:** Stabilize off the 1.1.0-alpha05 pre-release within the same Preference line
  without touching settings behavior; appcompat transitively stabilized to 1.1.0.
- **Alternatives considered:** Staying on 1.1.0-alpha05 (rejected — pre-release); migrating
  to `preference-ktx`/DataStore (out of scope); pre-emptively reworking the
  `EditTextPreference` numeric input (deferred — physical verification).
- **Trade-offs:** The `inputType="number"` behavior is unchanged and must be verified on a
  device at the Golden Candidate gate.
- **Affected components:** `app/build.gradle`, `ui/SettingsFragment`, `res/xml/preferences.xml`,
  tests, P1D7+.

---

## D022 — Material transitives are not an API ownership contract (explicit direct deps)

- **Date:** 2026-09-12
- **Status:** Accepted (P1D7-U2)
- **Decision:** ProotX directly declares every library it directly consumes. Specifically it
  owns `androidx.swiperefreshlayout:swiperefreshlayout:1.0.0` and
  `androidx.localbroadcastmanager:localbroadcastmanager:1.0.0`, which Material 1.1.0-alpha06
  had supplied only transitively through `androidx.legacy:legacy-support-core-ui` /
  `legacy-support-core-utils`. Material moved to **1.1.0** stable with these declarations in
  place.
- **Reason:** Material 1.1.0 stable drops the `androidx.legacy` transitives. The app consumes
  `SwipeRefreshLayout` (`frag_app_list.xml`, `AppsListFragment`, `EspressoHelpers`) and
  `LocalBroadcastManager` (`MainActivity`, `ServerService`) directly, so it owns those
  dependencies; another library's transitives are not an API ownership contract.
- **Alternatives considered:** Staying on Material 1.1.0-alpha06 (keeps a pre-release);
  replacing `SwipeRefreshLayout`/`LocalBroadcastManager` (rejected — behavior/UI change);
  declaring only one (rejected — the build fails on the other).
- **Trade-offs:** Adds two explicit androidx UI dependencies; no behavior change.
  `LocalBroadcastManager` is deprecated technology — its eventual replacement is recorded as
  deferred debt, not done here.
- **Affected components:** `app/build.gradle`, `P1D7-U2`, `P1D8+`.

---

## D023 — API 36 toolchain target and staged P1E migration

- **Date:** 2026-09-15
- **Status:** Accepted (P1E0)
- **Decision:** The API-36 target stack is **AGP 8.10.x / Gradle 8.11.1 / Kotlin (KGP) 2.2.x /
  JDK 17 / Build Tools 35.0.0 / compileSdk 36 / targetSdk 36**, keeping `minSdk 21` and, if
  AGP 8.10 accepts it, **NDK 21.4.7075529** (NDK modernization and 16 KB alignment remain
  P1F's). Because Kotlin 1.4.32 cannot run on Gradle 8/AGP 8 and Moshi 1.9.3 is coupled to it,
  the migration is **staged** through an intermediate bridge:
  **P1E1** Gradle 7.6 / AGP 7.4.2 / Kotlin 1.9.x / Moshi 1.15.x / Navigation 2.7.x / JDK 17
  (compileSdk/targetSdk still 30) → **P1E2** the API-36 toolchain + AGP 8 DSL changes →
  **P1E3** compileSdk 36 (targetSdk 30) → **P1E4–P1E8** behavior/permission/manifest work →
  **P1E9** SDK 36 regression. Full design: `docs/P1E_ANDROID16_MIGRATION_PLAN.md`.
- **Reason:** Official tooling: the minimum AGP supporting API 36 is 8.10 (8.9 caps at 35),
  requiring Gradle 8.11.1 and JDK 17; KGP's support matrix makes an AGP 7.4 / Gradle 7.6 /
  Kotlin 1.9 intermediate the last station that bridges the old and new toolchains with bounded
  risk. Separating the Kotlin/codegen jump (P1E1) from the AGP-8 DSL breakage (P1E2), and
  raising targetSdk last, keeps one independently testable risk class per milestone.
- **Alternatives considered:** A single atomic jump to AGP 8.10/Gradle 8.11.1/Kotlin 2.2/JDK 17
  (rejected — entangles the Kotlin↔Moshi codegen leap with AGP-8 DSL breakage); jumping through
  more AGP 8 stations (rejected — crosses the AGP 8 defaults anyway); adopting AGP 8.11/Gradle
  8.13 (rejected — newer than necessary).
- **Trade-offs:** A larger total milestone count; each step is independently verifiable and
  reversible. NDK 21.4 may not be accepted by AGP 8.10 — if so it is a cross-phase blocker for
  P1F, not something to absorb silently.
- **Affected components:** `build.gradle`, `app/build.gradle`, `termux-app/*/build.gradle`,
  `.github/workflows/build.yml`, `gradle/wrapper`, P1E1–P1E9.

---

## D024 — App-scoped storage; external-storage permissions are not part of the runtime contract

- **Date:** 2026-09-15
- **Status:** Accepted (P1E0)
- **Decision:** ProotX's runtime uses only app-private/app-scoped storage
  (`filesDir`, `getExternalFilesDir`, `getExternalFilesDirs`). It does **not** require
  `READ_EXTERNAL_STORAGE`/`WRITE_EXTERNAL_STORAGE`. The current `PermissionHandler` gate that
  blocks app/session launch on those permissions must be removed/replaced in **P1E5** before
  targetSdk reaches 33 (where `READ_EXTERNAL_STORAGE` becomes non-grantable and would
  permanently block session launch). The manifest declarations and the terminal module's
  `WRITE_EXTERNAL_STORAGE` request are removed in the same milestone.
- **Reason:** Verified during P1E0 that no code path reads/writes public/shared external
  storage; the permissions are legacy UserLAnd behavior. Retaining them would hard-block the
  target-36 upgrade.
- **Alternatives considered:** Keeping the permissions and requesting them anyway (rejected —
  non-grantable at target 33+, would block launch); adding `MANAGE_EXTERNAL_STORAGE` (rejected —
  not needed and Play-restricted).
- **Trade-offs:** A deliberate runtime-permission behavior change, isolated to P1E5 with its own
  regression validation.
- **Affected components:** `PermissionHandler.kt`, `MainActivity.kt`, `AndroidManifest.xml`,
  `terminal-term` manifest/`TermuxActivity`, P1E5.

---

## D025 — Proven P1E1 intermediate bridge version set; Kotlin 2.x codegen gate

- **Date:** 2026-09-15
- **Status:** Accepted (P1E1-P, BRIDGE_FOUND)
- **Decision:** The intermediate bridge is proven locally as **Gradle 7.6.4 / AGP 7.4.2 /
  Kotlin (KGP) 1.9.25 / Moshi 1.15.2 (KAPT) / Navigation 2.3.5 / JDK 17 / NDK 21.4.7075529**,
  with **JaCoCo 0.8.8** plus `jacoco.excludes = ['jdk.internal.*']`, test-only **Mockito
  4.11.0**, and seven behavior-neutral Kotlin-1.9 source fixes. `gradle-download-task` 3.4.3 and
  the ktlint `JavaExec` task require no change. A separate codegen milestone (**P1E2: Moshi
  KAPT → KSP**) must be completed **before** the Kotlin 2.2 jump.
- **Corrections to P1E0:** the API-36 **minimum** AGP is **8.9.1** (the final target remains
  AGP 8.10.x); KGP **1.9.20–1.9.25** supports AGP through 8.1.0 (the P1E0 "1.9.10 highest for
  AGP 7.4" note was wrong); Moshi 1.15.x **KAPT** is a Kotlin 1.9-only bridge and emits a
  deprecation warning — it is **not** a Kotlin-2.x KAPT path.
- **Reason:** Probe evidence (Cells 1–3 + final candidate): AGP 7.4.2 rejects KGP < 1.5.20;
  Safe Args 2.1.0 fails Gradle-7.6 task validation; Navigation 2.5.3's graph demands
  compileSdk ≥ 31 while 2.3.5 stays within 30; JaCoCo 0.8.4 and Mockito 2.23.0 are
  JDK-17-incompatible. The combination above ran the canonical gate at 326 tests / 36 suites /
  0 failures.
- **Alternatives considered:** Navigation 2.5.3/2.7.x (rejected — needs compileSdk ≥ 31);
  Kotlin 1.9.10 (superseded by the corrected 1.9.20–1.9.25 range); keeping Moshi 1.15.2 KAPT
  into Kotlin 2.2 (rejected — unsupported); running the bridge on JDK 11 to avoid the
  JaCoCo/Mockito bumps (not chosen — the final target is JDK 17 and no JDK 11 toolchain is
  provisioned).
- **Trade-offs:** Adds a test-only Mockito bump and small source fixes to P1E1; inserts P1E2
  (codegen) before the Kotlin 2.2 jump. All targetSdk/behavior work still lands last.
- **Affected components:** `build.gradle`, `app/build.gradle`, `termux-app/terminal-emulator/
  build.gradle`, `gradle/wrapper`, `NavigationStabilityGuardTest`, five viewmodels +
  `FilesystemEditFragment`, P1E1–P1E3.

---

## D026 — Okio 3.x and transitive `core` movement are accepted bridge consequences

- **Date:** 2026-09-15
- **Status:** Accepted (P1E1-P)
- **Decision:** The bridge's transitive movement — fragment → 1.2.4, lifecycle → 2.2.0,
  activity → 1.1.0, `androidx.core:core` → 1.3.0 (while `core-ktx` stays 1.1.0), and Okio
  1.17.2 → **3.7.0** (from Moshi 1.15.2, shared with the pinned OkHttp 3.14.7) — is accepted as
  a consequence of the proven versions, not force-corrected. No version is forced; no
  pre-release is introduced.
- **Reason:** The probe's canonical gate passed with these selections, including the OkHttp +
  MockWebServer tests. Forcing versions back for cosmetic alignment would violate the no-force
  policy and mask real graph state.
- **Alternatives considered:** Pinning an older Moshi to keep Okio 1.x (rejected — Moshi 1.15.2
  is required for Kotlin 1.9 codegen); bumping `core-ktx` to 1.3.0 in P1E1 (deferred — record
  the `core`/`core-ktx` family note and address deliberately if it matters).
- **Trade-offs:** OkHttp 3.14.7 running against Okio 3.7.0 is a runtime-compatibility item to
  validate later (JVM tests pass); the Core family is temporarily misaligned again.
- **Affected components:** `app/build.gradle`, dependency graph, P1E1/parent.

---

## D027 — `gradle-download-task` upgraded to 5.0.0 for the Gradle 7.6 bridge

- **Date:** 2026-09-15
- **Status:** Accepted (P1E1)
- **Decision:** Upgrade `de.undercouch:gradle-download-task` from `3.4.3` to **`5.0.0`**. This
  supersedes the P1E1-P finding that 3.4.3 could remain unchanged.
- **Reason:** The P1E1-P probe never scheduled `:app:downloadAssets` locally because the
  gitignored `app/src/main/jniLibs` bundle was already present. On a clean CI checkout the
  assets are absent, so `checkIfAssetsMissing → fetchAssets → downloadAssets` runs and Gradle
  7.6 rejects the 3.4.3 `Download` task type (unannotated `authScheme`, `cachedETagsFile`,
  `credentials`, `dest`, `downloadTaskDir`). Reproduced locally with `:app:fetchAssets`.
  5.0.0 is the smallest 5.x (per the P1E1-P PART M fallback) and was verified by executing the
  real download path plus the canonical clean build.
- **Alternatives considered:** Keeping 3.4.3 (rejected — CI build fails); suppressing task
  validation (not supported cleanly); modernizing to a newer 5.x (unnecessary).
- **Trade-offs:** A third-party build plugin moves earlier than the AGP 8 milestone; no
  application API or behavior change.
- **Affected components:** `build.gradle`, `:app:downloadAssets`/`fetchAssets`, CI, P1E1.
