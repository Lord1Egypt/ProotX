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
- **Status:** Accepted (P1E0) — **IMPLEMENTED in P1E6 (CLOSED / PASS)**
- **Decision:** ProotX's runtime uses only app-private/app-scoped storage
  (`filesDir`, `getExternalFilesDir`, `getExternalFilesDirs`). It does **not** require
  `READ_EXTERNAL_STORAGE`/`WRITE_EXTERNAL_STORAGE`. The current `PermissionHandler` gate that
  blocks app/session launch on those permissions must be removed/replaced in **P1E6** before
  targetSdk reaches 33 (where `READ_EXTERNAL_STORAGE` becomes non-grantable and would
  permanently block session launch). The manifest declarations and the terminal module's
  `WRITE_EXTERNAL_STORAGE` request are removed in the same milestone.
- **Reason:** Verified during P1E0 that no code path reads/writes public/shared external
  storage; the permissions are legacy UserLAnd behavior. Retaining them would hard-block the
  target-36 upgrade.
- **Alternatives considered:** Keeping the permissions and requesting them anyway (rejected —
  non-grantable at target 33+, would block launch); adding `MANAGE_EXTERNAL_STORAGE` (rejected —
  not needed and Play-restricted).
- **Trade-offs:** A deliberate runtime-permission behavior change, isolated to P1E6 with its own
  regression validation.
- **Affected components:** `PermissionHandler.kt` (deleted), `MainActivity.kt`,
  `AndroidManifest.xml`, `terminal-term` manifest/`TermuxActivity`, guard test
  `StoragePermissionGuardTest`, P1E6 (implementation `2202d6b`).

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

---

## D028 — Moshi codegen on KSP; Room stays on KAPT (mixed processing)

- **Date:** 2026-09-15
- **Status:** Accepted (P1E2)
- **Decision:** Moshi code generation runs on **KSP `1.9.25-1.0.20`**
  (`ksp "com.squareup.moshi:moshi-kotlin-codegen:1.15.2"`), while Room code generation remains
  on **KAPT** (`kapt "androidx.room:room-compiler:2.1.0"`; `kotlin-kapt` retained). The two
  processors are intentionally split for the Kotlin 1.9 bridge. A guard
  (`MoshiKspGuardTest`) enforces the split and the pinned versions.
- **Reason:** Moshi 1.15.2 KAPT emits a deprecation warning and is a Kotlin-1.9-only path;
  moving Moshi to KSP removes that path before the Kotlin 2.2 jump without forcing a Room KSP
  migration (Room 2.1.0 predates KSP and would need an upgrade, which is out of scope here).
  KSP also avoids the KAPT stub-generation step for Moshi.
- **Alternatives considered:** Migrating Room to KSP too (rejected — Room 2.1.0 has no KSP
  support; would require a Room upgrade); leaving Moshi on KAPT (rejected — deprecated,
  blocks Kotlin 2.x readiness); moving both later (rejected — Moshi KAPT is the known blocker).
- **Trade-offs:** A mixed KAPT+KSP build in the interim; `build/generated/ksp` becomes the
  Moshi output of record. Runtime/serialization semantics are unchanged (same fields, `@Json`
  names, nullability, constructor mapping).
- **Affected components:** `build.gradle` (`ksp_version`, KSP plugin classpath),
  `app/build.gradle` (ksp plugin, Moshi `ksp` config), `MoshiKspGuardTest`, P1E2/P1E3.

---

## D029 — AGP 8.10 / Kotlin 2.2 bridge is reachable before compileSdk 36

- **Date:** 2026-09-15
- **Status:** Accepted (P1E3-P, BRIDGE_FOUND)
- **Decision:** ProotX will implement P1E3 as **Gradle 8.11.1 / AGP 8.10.1 / Kotlin 2.2.20 /
  KSP 2.2.20-2.0.4 / JDK 17 / Build Tools 35.0.0 / NDK 21.4.7075529**, with **Moshi on KSP2**,
  **Room 2.1.0 on KAPT**, and **Navigation/Safe Args 2.3.5 unchanged**, while keeping
  `compileSdk`/`targetSdk`/`minSdk` at **30/30/21** (terminal 29/29/21). Required changes are
  enumerable and behavior-neutral: namespaces (all modules), `buildFeatures { buildConfig true }`,
  JaCoCo report DSL `xml/html.enabled → required`, `toLowerCase(Locale) → lowercase(Locale)` (5×),
  and androidTest `R.id.terminal_view → com.termux.R.id.terminal_view`.
- **Reason:** P1E3-P proved a full local build (`37 suites / 327 tests / 0 failures`) with no
  dependency forced, no exclusion, no pre-release, and no compileSdk increase. Room 2.1.0 KAPT
  and Safe Args 2.3.5 both survive AGP 8.10.1/Kotlin 2.2, so no unplanned dependency migration
  is required and the P1E sequence does not need to change.
- **Alternatives considered:** Raising Navigation or Room (rejected — unnecessary); disabling
  `nonTransitiveRClass` (rejected — proper ownership fix used instead); changing compileSdk
  (rejected — not required); adopting AGP 8.11/Gradle 8.13 (rejected — newer than needed).
- **Trade-offs:** AGP 8's non-transitive R and namespace requirements force small,
  behavior-neutral source/build edits; manifest `package` attributes become inert (removal
  recommended); `ktlint`'s `JavaExec.main` and `String.capitalize()` are deprecated and should
  be cleaned up before Gradle 9 / later Kotlin.
- **Affected components:** `gradle/wrapper`, `build.gradle`, `app/build.gradle`,
  `termux-app/*/build.gradle`, 4 production source files + `MainActivityTest.kt` +
  `MoshiKspGuardTest.kt`, P1E3.

## D030 — TermuxActivity `ssh://` entry point is an intended feature; receiver export flag deferred to targetSdk-34

- **Date:** 2026-09-16
- **Status:** Accepted (P1E5, CLOSED / PASS)
- **Decision:** `com.termux.app.TermuxActivity` remains an exported component
  (`android:exported="true"`) so its existing `VIEW`/`DEFAULT`/`BROWSABLE` `ssh://` deep-link
  entry point keeps working; the embedded-terminal SSH entry point is an intended product
  feature. All six application-owned `PendingIntent`s are explicitly immutable (five
  `FLAG_IMMUTABLE`; the stop-sessions service intent `FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE`).
  The `TermuxActivity` custom `com.termux.app.reload_style` receiver is classified as
  `RECEIVER_NOT_EXPORTED`, but the explicit flag is **deferred** to the targetSdk-34 milestone.
- **Reason:** P1E0 left the TermuxActivity export as an open product question; P1E5 resolves it in
  favor of preserving the SSH deep link. The dynamic-receiver export flag only exists from API 33/
  34, while `:terminal-term` still compiles against **API 29**, so adding it now would require a
  terminal `compileSdk` increase that P1E5 is not authorized to make. App `targetSdk` stays **30**.
- **Alternatives considered:** `exported="false"` or removing the filter/module (rejected — would
  drop the current SSH entry point); applying `RECEIVER_NOT_EXPORTED` now (rejected — API 29
  compile target); raising terminal `compileSdk` (rejected — out of scope).
- **Trade-offs:** the custom receiver keeps its current default export behavior until the
  targetSdk-34 milestone; the deferred flag is tracked in `TASKS.md`/`PROJECT_STATE.md`.
- **Affected components:** `app/src/main/AndroidManifest.xml`,
  `termux-app/terminal-term/src/main/AndroidManifest.xml`, `NotificationConstructor.kt`,
  `TermuxService.java`, P1E5/P1E8.

---

## D031 — Foreground-service specialUse type; `POST_NOTIFICATIONS` deferred until targetSdk 33

- **Date:** 2026-09-16
- **Status:** Accepted (P1E7, CLOSED / PASS)
- **Decision:** ProotX's two real foreground services are typed **`specialUse`**. Both
  `io.github.lord1egypt.prootx.ServerService` and `com.termux.app.TermuxService` declare
  `android:foregroundServiceType="specialUse"` with an
  `android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE` property describing the real user-facing
  reason, and the app declares `android.permission.FOREGROUND_SERVICE_SPECIAL_USE` alongside
  the retained `android.permission.FOREGROUND_SERVICE`. `TermuxService` is overlaid from the
  API-36 app manifest because `:terminal-term` still compiles against API 29, whose AAPT does
  not recognize the newer enum; the overlay merges into the single existing library component.
  Each service now creates its own `"ProotX"` notification channel (`IMPORTANCE_LOW`) and
  promotes with `ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST` on API 29+ (two-argument
  `startForeground` below), so the manifest is the single source of truth. **`POST_NOTIFICATIONS` is intentionally _not_ declared or
  requested in P1E7**; it is deferred to P1E8, which raises targetSdk to 33/34 and owns the
  declaration, the runtime request, and the permission UX as one unit.
- **Reason:** `dataSync`/`mediaPlayback`/`location`/`connectedDevice`/`mediaProjection`/
  `camera`/`microphone`/`health`/`remoteMessaging` do not describe a user-initiated local Linux
  / interactive terminal runtime, and `shortService`/`systemExempted` are invalid here, so
  `specialUse` is the correct type. On Android 13+ an app targeting API ≤ 32 does not control
  the notification-permission dialog timing in the same way a target-33+ app does; declaring
  `POST_NOTIFICATIONS` while targetSdk is still 30 could surface premature, system-controlled
  first-run permission UX. The FGS itself runs without `POST_NOTIFICATIONS`; only notification
  visibility is affected. Keeping the declaration and the request together in P1E8 (with the
  targetSdk raise) lets ProotX own the request timing.
- **Alternatives considered:** `dataSync`/`systemExempted`/`shortService` (rejected — wrong or
  invalid semantics); declaring/requesting `POST_NOTIFICATIONS` in P1E7 (rejected —
  system-timed UX at target ≤ 32; the original migration-plan wording was corrected);
  hard-coding `FOREGROUND_SERVICE_TYPE_SPECIAL_USE` at runtime (rejected —
  `FOREGROUND_SERVICE_TYPE_MANIFEST` keeps the manifest authoritative).
- **Trade-offs:** The specialUse type is a declared rationale, not a stronger platform
  guarantee, and notification visibility on Android 13+ remains dependent on the P1E8 request.
  The `TermuxService` type lives in the app manifest overlay rather than the terminal module.
- **Affected components:** `app/src/main/AndroidManifest.xml`, `ServerService.kt`,
  `MainActivity.kt`, `TermuxActivity.java`, `TermuxService.java`,
  `ForegroundServiceCompatibilityGuardTest`, P1E7/P1E8.

---

## D032 — targetSdk 34; contextual one-time POST_NOTIFICATIONS; terminal-term compileSdk 36 / targetSdk 29

- **Date:** 2026-09-16
- **Status:** Accepted (P1E8, CLOSED / PASS)
- **Decision:** The app's persistent `targetSdk` is **34** (`compileSdk 36`, `minSdk 21`). The app
  declares `POST_NOTIFICATIONS` and requests it **contextually at the first real session start**
  (never at ordinary app startup, browsing, or import/export). One shared application-private
  `SharedPreferences` file `notification_permission` with key `prompt_completed` records only that
  ProotX has already presented the request and received an explicit result; current grant state is
  always read from `checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)`. **Denial never
  blocks the Linux session**: the pending session is continued regardless of the permission
  outcome, and an explicit denial suppresses later automatic prompts. The initial
  `ServerService` foreground launch is **deferred until the activity is resumed**
  (`lifecycle.currentState.isAtLeast(RESUMED)`), and the actual launch catches only
  `ForegroundServiceStartNotAllowedException`, restoring the pending session and retrying on the
  next resume. `TermuxActivity` (direct `ssh://` entry) participates in the same one-time policy.
  `TermuxActivity`'s application-internal `com.termux.app.reload_style` receiver is registered with
  the direct API-33 `Context.RECEIVER_NOT_EXPORTED` constant on TIRAMISU+ with the legacy
  two-argument path below. `:terminal-term` raises **only its `compileSdk` to 36**, keeping
  `targetSdk 29` / `minSdk 21`; `:terminal-view` and `:terminal-emulator` stay **29/29/21**.
- **Reason:** targetSdk 34 is the P1E8 boundary; it activates the Android 14 FGS type, runtime
  receiver-export, and permission behavior without pulling in Android 15/16 UI behavior. Requesting
  `POST_NOTIFICATIONS` in context (and never blocking the session) avoids premature or nagging UX.
  The lifecycle/exception gate removes the target-31+ `ForegroundServiceStartNotAllowedException`
  path that the pre-34 target could not trigger. `Context.RECEIVER_NOT_EXPORTED` is the direct API
  rather than a magic integer or reflection, and the terminal module needs API 33/34 compile access
  for it. `MainActivity`'s `DownloadManager.ACTION_DOWNLOAD_COMPLETE` receiver listens only to a
  system broadcast, which Android 14 exempts from the export-flag requirement, so it deliberately
  keeps the flag-less registration.
- **Alternatives considered:** Requesting `POST_NOTIFICATIONS` at startup or on every session start
  (rejected — premature/nagging UX); blocking the session when denied (rejected — notifications are
  not required for the FGS to run); a numeric/reflection receiver flag (rejected — magic values);
  `RECEIVER_NOT_EXPORTED` on the DownloadManager system receiver (rejected — incorrect for a
  system-only broadcast); raising `:terminal-view`/`:terminal-emulator` compileSdk (rejected — not
  required by compilation); upgrading `androidx.test` to fix the target-31+ test-manifest merge
  (rejected — dependency upgrade out of scope; a test-only manifest overlay supplies the missing
  `android:exported` values instead).
- **Trade-offs:** The androidTest APK now needs the test-only overlay to keep building at target
  34 with `androidx.test:core:1.2.0`. The lint `UnspecifiedRegisterReceiverFlag` false positive on
  the system DownloadManager receiver is suppressed with an explicit justification rather than
  adding an incorrect flag.
- **Affected components:** `app/build.gradle`, `app/src/main/AndroidManifest.xml`, `MainActivity.kt`,
  `termux-app/terminal-term/build.gradle`, `TermuxActivity.java`,
  `app/src/androidTest/AndroidManifest.xml`, `ForegroundServiceCompatibilityGuardTest`,
  `TargetSdk34CompatibilityGuardTest`, P1E8/P1E9.

---

## D033 — Final targetSdk 36; real edge-to-edge and predictive back (no opt-outs); Activity 1.11.0 bridge

- **Date:** 2026-09-16
- **Status:** Accepted (P1E9, CLOSED / PASS)
- **Decision:** The app's final `targetSdk` is **36** (`compileSdk 36`, `minSdk 21`). ProotX adapts
  to Android 15/16 rather than opting out:
  - **Edge-to-edge:** `MainActivity` calls `enableEdgeToEdge()` and applies real
    `WindowInsetsCompat` per owner — the toolbar owns the top system-bar/cutout inset, the
    `BottomNavigationView` owns the bottom one, and the root owns left/right cutout/navigation-bar
    safety. Original padding is captured once and combined with the current inset so repeated
    dispatches never accumulate. Status/navigation icons stay light for the dark blue-gray chrome.
    No `windowOptOutEdgeToEdgeEnforcement`.
  - **Predictive back:** `MainActivity` keeps the AndroidX Navigation dispatcher path; a new
    `androidx.activity:activity-ktx:1.11.0` bridge (the last line before Activity raises minSdk to
    23) wires `OnBackPressedDispatcher` to the platform `OnBackInvokedDispatcher` while `minSdk`
    stays 21. `TermuxActivity` registers a platform `OnBackInvokedCallback` on API 33+ (drawer open
    → close; otherwise finish) and keeps its legacy `onBackPressed` fallback for API < 33 /
    non-predictive-back. No `android:enableOnBackInvokedCallback="false"` and no opt-out.
  - **Adaptive / large screen:** no `screenOrientation` lock, no aspect-ratio restriction, no
    Android 16 large-screen/resizability opt-out; `TermuxActivity` stays
    `resizeableActivity="true"`. ProotX accepts Android 16's adaptive behavior.
  - **Termux edge-to-edge:** kept the existing `fitsSystemWindows="true"` root and the
    terminal-black theme; the legacy `statusBarColor`/translucent attributes are retained (inert
    under enforced edge-to-edge but harmless and preserved for pre-35 devices). `TermuxActivity`
    is not converted to an AppCompat activity.
  - `DeviceDimensions` retains physical `defaultDisplay.getRealMetrics()` geometry: it feeds the
    external VNC/X client resolution, which is intentionally physical-display geometry.
- **Reason:** Android 15 enforces edge-to-edge for target 35+ and Android 16 removes the practical
  opt-out; predictive back becomes the default dispatch path. Adapting is the correct final
  architecture. The Activity 1.11.0 bridge is the bounded minimum that keeps `minSdk 21` and does
  not require a Navigation/AppCompat/Material/Room upgrade.
- **Alternatives considered:** `windowOptOutEdgeToEdgeEnforcement` or
  `enableOnBackInvokedCallback="false"` (rejected — escape hatches, not the product direction);
  fixed-dp padding (rejected — wrong on cutouts/landscape); converting `TermuxActivity` to
  AppCompat (rejected — unnecessary architecture change); Activity 1.12+/1.13+ (rejected — raises
  minSdk to 23); upgrading Navigation/AppCompat (rejected — not required).
- **Trade-offs:** The authorized Activity bridge moves transitive selections: activity/activity-ktx
  **1.1.0 → 1.11.0**, core/core-ktx **1.1.0/1.3.0 → 1.13.0**, lifecycle **2.2.0 → 2.6.2**,
  savedstate **1.0.0 → 1.2.1**, coroutines **1.3.9 → 1.7.3**, plus new runtime transitives
  `core-viewtree`, `tracing`, `profileinstaller`. `androidx.core:core:1.13.0` also injects the
  benign signature-level `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` into the merged manifest.
  Navigation 2.3.5, AppCompat 1.1.0, Material 1.1.0, Room 2.1.0, Fragment 1.2.4, and the test
  stack are unchanged. `MainActivity.onNewIntent` became non-null (`Intent`). Final APK grows
  because of the newer AndroidX. Physical validation of insets/back/IME on devices remains P1G.
- **Affected components:** `app/build.gradle`, `MainActivity.kt`,
  `termux-app/terminal-term/src/main/java/com/termux/app/TermuxActivity.java`,
  `TargetSdk36CompatibilityGuardTest`, `TargetSdk36PlatformBehaviorGuardTest`, P1E9/P1F/P1G.

---

## D034 — In-tree native toolchain moves to NDK r29; NDK selected by `ndkVersion`; support-bundle 16 KB remains separate

- **Date:** 2026-09-16
- **Status:** Accepted (P1F1, CLOSED / PASS)
- **Decision:** The in-tree native toolchain pin for `:app` and `:terminal-emulator` moves from NDK
  `21.4.7075529` to **NDK r29 `29.0.14206865`**. No source, `Android.mk`, linker, or ABI change is
  made. CI installs `ndk;29.0.14206865`, keeps the pinned Android platforms/build-tools, and
  **stops writing the deprecated `ndk.dir`**: the Gradle module `ndkVersion` is the single
  authoritative NDK selector and `local.properties` carries `sdk.dir` only. CI gains a narrowly
  scoped guard that extracts the packaged `lib/arm64-v8a/libtermux.so` and
  `lib/x86_64/libtermux.so` and fails if any `PT_LOAD` alignment is below `0x4000`, deriving the
  `llvm-readelf` path from `ANDROID_SDK_ROOT`/`ANDROID_HOME` + `ANDROID_NDK_VERSION`.
- **Reason:** Android 15+ 16 KB page-size support requires native ELF `PT_LOAD` alignment >= 16 KB
  on 64-bit ABIs. The P1F-P probe proved NDK r29 does this for the in-tree library with no source
  change (arm64-v8a and x86_64 `libtermux.so`: `0x1000` → `0x4000`) and that AGP 8.10.1 already
  emits `PAGE_ALIGNMENT_16K` in the AAB. Dropping `ndk.dir` is required in practice: a stale
  `ndk.dir=21.4...` with `ndkVersion 29.0.14206865` made Gradle configuration fail.
- **Scope boundary:** P1F1 makes the in-tree 64-bit native library 16 KB compatible and modernizes
  the NDK/CI selection. It does **not** make the complete application 16 KB compatible: the
  `ProotX-Assets-Support` v1.0.0 bundle still ships 4 KB-only ELFs for x86_64 (and the arm64
  `loader32` 32-bit helper), and the historical PRoot source used by the support builder
  (`Lord1Egypt/proot@merge-it`) is currently unavailable (404). Those are P1F2/P1F3.
- **Alternatives considered:** keeping NDK 21.4 (rejected — 4 KB-only ELFs); adding
  `-Wl,-z,max-page-size=16384` to the in-tree build (rejected — unnecessary; r29 already produces
  `0x4000`); suppressing the `ndk.dir` deprecation warning instead of removing it (rejected — the
  deprecation is a correctness/ambiguity issue, not just noise); rebuilding the support bundle in
  P1F1 (rejected — out of scope and source is unavailable).
- **Trade-offs:** The support-bundle payloads are unchanged and remain a separate blocker; the
  strip warnings for the intentionally non-ELF pseudo-`.so` files remain (benign). Full 16 KB
  compatibility must not be claimed until P1F3/P1F4 and P1G.
- **Affected components:** `app/build.gradle`, `termux-app/terminal-emulator/build.gradle`,
  `.github/workflows/build.yml`, `NdkR29ToolchainGuardTest`, P1F1/P1F2/P1F3/P1F4, P1G.

---

## D035 — Split support-runtime lanes: frozen legacy (host API 21–28) + reproducible modern (host API 29+)

- **Date:** 2026-09-16
- **Status:** Accepted (P1F2, CLOSED / PASS)
- **Decision:** ProotX keeps application `minSdk 21` through a **split support-runtime model**
  selected by the existing `ProotXFiles` logic (unchanged): host Android **API 21–28** use the
  frozen **legacy** normal slots (`proot`, `loader`, `loader32`, `libtalloc.so.2`); host Android
  **API 29+** use the **modern** `.a10` slots (`proot.a10`, `loader.a10`, `loader32.a10`,
  `libtalloc.so.2.a10`, plus the new `libandroid-shmem.so`), which are **source-rebuilt at API 24
  with NDK r29**. `.a10` is a **legacy filename denoting the modern host runtime slot**, not API 10.
  The support build is now reproducible via a pinned builder image digest, a pinned `termux-packages`
  commit, a checksum-verified `termux/proot v5.1.107.92` source, deterministic packaging, and a
  `SOURCE_DATE_EPOCH` (the PRoot tag-commit timestamp `1787437959`). The historical builder
  (`ubuntu:latest`, floating `merge-it` fork — now unavailable —, floating `android-5`, blind `sed`)
  is removed and documented. No release/asset is published by P1F2; ProotX remains on support
  `v1.0.0`.
- **Reason:** The P1F2 probe proved modern `termux/proot v5.1.107.92` cannot be built at API 21 —
  `HAVE_PROCESS_VM` is absent (`process_vm_readv`/`writev` are API 23+) and `getifaddrs` is API 24+
  — while ProotX must keep `minSdk 21`. A modern-lane API-24 build is fully reproducible, defines
  `HAVE_PROCESS_VM` (`process_vm = yes`), satisfies the complete ProotX CLI/loader contract, and is
  selected only on API 29+ hosts, so `minSdk` and current runtime selection are unchanged.
- **Alternatives considered:** patching the PRoot feature probe / `syscall/enter.c` or raising the
  API to 21-compat (forbidden by the technical lead); a custom PRoot fork (not authorized); raising
  `minSdk` (not authorized); rebuilding legacy API-21 binaries (source unavailable).
- **Trade-offs:** This is **not** a claim of whole-app 16 KB compliance. Some **4 KB 64-bit legacy
  normal-slot** ELFs (`proot`, `libtalloc.so.2`, `proot_meta`, `proot_meta_leveldb`, vendored
  binaries) are still shipped through `jniLibs`/`nativeLibraryDir`; a later P1F3/P1F4 strategy
  (rebuild legacy 64-bit with 16 KB alignment, move them outside native-library packaging, or an
  approved support-floor change) must be chosen before P1F closes. `proot_meta`/`proot_meta_leveldb`
  remain **unknown-provenance** frozen compatibility inputs. Modern-lane runtime equivalence of the
  `.a10` slot (ashmem/memfd) is a P1G physical-validation item.
- **Affected components:** `ProotX-Assets-Support` (`build-support.sh`, `provenance/`,
  `docs/PROVENANCE.md`, `docs/HISTORICAL_BUILDER.md`, `THIRD_PARTY_NOTICES.md`, support CI,
  branch `feature/p1f-support-modernization`), P1F2/P1F3/P1F4/P1G.

---

## D036 — v1.1.0 support release published; trusted release CI split; published tags/assets are immutable

- **Date:** 2026-09-16
- **Status:** Accepted (P1F3, CLOSED / PASS)
- **Decision:** The first provenance-backed support release, **`v1.1.0`**, is published from the
  P1F2 toolchain. Annotated tag `v1.1.0` (object `ff55608c0e9490dfd3a6dc392717c19a1916d636`) points
  at support commit `acc28abcd0756cca66782145099ef54ed4cbc46c` (support `main`, fast-forwarded from
  `0fa736a`). Release `RE_kwDOUXjkQM4XOeTw` (published 2026-09-16T05:59:57Z) carries the four
  `*-assets.zip`, `SHA256SUMS`, `v1.1.0-provenance.json`, and `v1.1.0.spdx.json`. The release CI is
  split for safety: untrusted PR validation (`support-validate.yml`) and the privileged modern
  builder (`support.yml`) run with `contents: read` and the builder never runs on `pull_request`; the
  tag-triggered `support-release.yml` separates a read-only build job (two independent clean
  four-ABI builds, reproducibility comparison, 16 KB gate, manifest/SHA256SUMS/SBOM) from a
  `contents: write` publish job that performs **no rebuild**. All release actions are pinned by full
  commit SHA. Published tags and assets are never mutated or replaced; corrections require
  `v1.1.1` or later. `v1.0.0` remains untouched.
- **Reason:** The accepted P1F2 builder is reproducible (two clean builds produced byte-identical
  four-ABI archives), so publishing an immutable, provenance-backed release is safe. Splitting the
  privileged build from untrusted PR events and from the write-token publish step prevents arbitrary
  PR code from running in a privileged container or receiving write credentials.
- **Alternatives considered:** keeping the single workflow with a `pull_request` trigger and a
  privileged builder (rejected — untrusted code execution); reusing one job with `contents: write`
  for both build and publish (rejected — exposes write token to the privileged build); replacing
  v1.0.0 (rejected — v1.0.0 is the historical frozen baseline).
- **Trade-offs:** The release build repeats the four-ABI build twice inside CI (time cost) to prove
  reproducibility at release time. Whole-app 16 KB compatibility is **still not achieved**: 13
  x86_64 legacy normal-slot ELFs remain 4 KB aligned and are still shipped through
  `jniLibs`/`nativeLibraryDir`; `proot_meta`/`proot_meta_leveldb` remain unknown-provenance frozen
  inputs. P1F4 owns packaging isolation and the ProotX switch to `v1.1.0`.
- **Affected components:** `ProotX-Assets-Support` (`.github/workflows/support-validate.yml`,
  `support.yml`, `support-release.yml`, `scripts/release_metadata.py`,
  `provenance/releases/v1.1.0.json`, `docs/RELEASE_NOTES_v1.1.0.md`), P1F3/P1F4/P1G.

---

## D037 — Dual support lanes with an explicit common/legacy/modern schema; v1.2.0 support release

- **Date:** 2026-09-17
- **Status:** Accepted (P1F4A, CLOSED / PASS)
- **Decision:** The support bundle moves to an **explicit, non-ambiguous dual-lane schema**.
  Every ABI archive contains `common/` (architecture-neutral non-native scripts/data),
  `legacy/` (the frozen API 21–28 executable/native payload) and `modern/` (the source-built
  API 29+ payload), plus a machine-readable `manifest.json` and an explicit routing contract
  (`routes`, and a release-level `routing.json`). The legacy lane preserves the frozen
  v1.1.0/v1.0.0 native files byte-for-byte (verified against
  `provenance/legacy-v1.0.0.files.sha256`); it is intentionally 4 KB aligned and is **not**
  packaged through Android `nativeLibraryDir`. The modern lane is rebuilt from pinned source
  with NDK r29 (`29.0.14206865`) at API 24; every modern 64-bit ELF has `PT_LOAD >= 0x4000`.
  The historical metadata sidecars are built from the **binary-proven 2019
  `CypherpunkArmory/proot` lineages** (`proot_meta` @ `2a7f6d9…`, `proot_meta_leveldb` @
  `998dd31…`) with the historical **`-DUSERLAND`** contract; `-DUSERLAND` gates the fake_id0
  metadata paths and is a required build input, never removed. The modern lane also ships a
  **reproducibly built static BusyBox 1.38.0** (`busybox_static`, `NEEDED=0`). There is **no
  filesystem migration**: `.proot_version = _meta` and `.proot_version = _meta_leveldb`
  semantics are unchanged and existing metadata databases are never rewritten or converted.
  `minSdk` stays 21 and `ProotXFiles` selection is unchanged. **P1F4B** owns the packaging
  boundary: legacy files are packaged outside `nativeLibraryDir`; modern files inside it.
- **Reason:** The v1.0.0/v1.1.0 flat layout let one filename ambiguously represent both lanes
  (e.g. `.a10` slots) and forced P1F4B to infer routing from filenames. The R1 probe also proved
  the frozen `proot_meta`/`proot_meta_leveldb` are **not unknown-provenance** but are exact 2019
  source lineages that require `-DUSERLAND`; rebuilding without it silently drops fake-ownership
  persistence. An explicit schema plus a routing manifest makes P1F4B deterministic and makes the
  modern 16 KB requirement enforceable independently of the frozen legacy payload.
- **Alternatives considered:** keeping the flat layout with `.a10` naming (rejected — ambiguous,
  and the mission forbids one filename representing both lanes); rebuilding the legacy 4 KB
  binaries (rejected — source unavailable; P1F4B will package them outside `nativeLibraryDir`);
  migrating existing `_meta` sessions to a new format (rejected — unnecessary and risky, since R1
  proved runtime parity); omitting `busybox_static` from the modern lane (rejected — the support
  scripts require it and it must be 16 KB aligned and reproducible).
- **Trade-offs:** The committed release manifest records the exact reproducible archive hashes,
  so the release pipeline verifies the freshly built archives against it. BusyBox embeds its build
  timestamp, so the build fixes `TZ=UTC`/`LC_ALL=C`/`LANG=C` around `SOURCE_DATE_EPOCH`; two clean
  independent builds are byte-identical and match the CI artifact. Whole-app 16 KB compatibility
  is **still not achieved**: the 13 x86_64 legacy ELFs remain 4 KB aligned and ProotX still
  downloads `v1.0.0` until P1F4B.
- **Affected components:** `ProotX-Assets-Support` (`build-support.sh`, `scripts/`,
  `provenance/sources.lock.json`, `provenance/file_sources.json`,
  `provenance/releases/v1.2.0.json`, `THIRD_PARTY_NOTICES.md`, support CI),
  P1F4A/P1F4B/P1G.
- **Implementation (P1F4B, CLOSED / PASS):** ProotX consumes `v1.2.0` through
  `app/support-release.lock.json` (release + per-asset SHA-256). A deterministic
  `prepareProotXSupport` task verifies the pinned release and stages generated output under
  `app/build/generated/prootxSupport/` — modern natives as `jniLibs/<abi>/lib_<name>.so`, common and
  legacy as `assets/support/{common,legacy/<abi>}/`, and a `support-map.json` routing artifact.
  `sourceSets` use only the generated jniLibs dir, so a stale tracked `src/main/jniLibs` can never be
  packaged. The runtime installer resolves the lane from `Build.SUPPORTED_ABIS` + the routing
  manifest: API 21–28 extracts the frozen legacy payload from assets into `filesDir/support`
  (SHA-256 verified); API 29+ links the modern payload from `nativeLibraryDir` and never copies
  executable code into writable storage (Android W^X). The `.a10` filename inference and the
  `lib_arch.so` pseudo-native marker are removed, and `extractFilesystem.sh`/`compressFilesystem.sh`
  are invoked through `busybox_static sh`. The APK/AAB native-library set is all ELF, contains no
  legacy/common file, and has no 4 KB 64-bit ELF; `zipalign -c -P 16` passes and bundletool reports
  `PAGE_ALIGNMENT_16K`. `minSdk` stays 21, all four ABIs are retained, and `extractNativeLibs` stays
  `true`. This is static whole-app acceptance only: P1F5 owns the 16 KB runtime/emulator acceptance
  and P1G the physical-device acceptance. The implementation is `9f14ee3` (feature
  `android-modernization`), feature CI `35281111291` SUCCESS (46 suites / 375 tests).
- **Remediation (P1F4B-R1, `55f7547`):** an independent technical-lead audit found that
  `SupportInstallation.runtimeNames` required the union of the common + legacy + modern names while
  `install()` installs only the active lane. The v1.2.0 payload is lane-asymmetric, so the marker
  fast path could never match and every startup unnecessarily reconciled and reinstalled the support
  state. `runtimeNames` now returns the common payload plus the active lane only (with
  `allDeclaredNames` preserving the union for any future caller); the test fixture is lane-asymmetric
  and the idempotency tests prove zero asset reads, zero symlink creations, an unchanged support
  snapshot and an unrewritten marker on the second initialization, plus a one-time API 28 → API 29
  transition reinstall. Runtime-confirmed on an API 30 x86_64 emulator (marker byte-identical across
  two launches, `proot_smoke_ok`). The architecture is unchanged; no support release, schema,
  packaging layout, W^X, ABI resolver or lock change.
