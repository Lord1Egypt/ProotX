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
