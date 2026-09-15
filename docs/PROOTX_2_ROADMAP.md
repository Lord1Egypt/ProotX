# ProotX 2.0 — Development Roadmap

> Status:
>
> - **P0 — Baseline freeze and development safety:** CLOSED / PASS
> - **P0.5 — Project Control Plane:** CLOSED / PASS
> - **P1 — Android Modernization:** IN PROGRESS
>   - **P1A — Build-System / JDK / CI Foundation:** CLOSED / PASS
>   - **P1B — Gradle / AGP Bridge Migration:** CLOSED / PASS
>   - **P1C — Kotlin / Synthetics Migration:** CLOSED / PASS
>     - **P1C1 — Synthetic Views → View Binding:** CLOSED / PASS
>     - **P1C2-P — Moshi 1.9.3 / Kotlin 1.4 Bridge Probe:** CLOSED / BRIDGE_FOUND
>     - **P1C2 — Kotlin + Legacy Parcelize + Plugin Removal:** CLOSED / PASS
>   - **P1D — Dependency / AndroidX Modernization:** CLOSED / PASS
>     - **P1D1 — Barista Removal / AndroidTest Build Restoration:** CLOSED / PASS
>     - **P1D2 — Dead Play Services Dependency Cleanup:** CLOSED / PASS
>     - **P1D3 — Lifecycle Extensions / ViewModelProvider Migration:** CLOSED / PASS
>     - **P1D4 — Navigation 2.1.0 Stable Migration:** CLOSED / PASS
>     - **P1D5 — Room 2.1.0 Stable Migration:** CLOSED / PASS
>     - **P1D6 — AndroidX Preference 1.1.0 Stable Migration:** CLOSED / PASS
>     - **P1D7 — Material Components 1.1.0 Stable Migration:** CLOSED / PASS
>       - **P1D7-U2 — Explicit Legacy Replacements + Material Final Retry:** CLOSED / PASS
>     - **P1D8 — Arch Core Testing 2.1.0 Stabilization:** CLOSED / PASS
>     - **P1D9 — AndroidX Core KTX 1.1.0 Alignment:** CLOSED / PASS
>     - **P1D Final Dependency Closure Audit:** CLOSED / PASS (no blocker)
>   - **CI-R1 — Android SDK Bootstrap Remediation:** CLOSED / PASS
>   - **P1E — SDK 36 / Manifest Compatibility:** IN PROGRESS
>     - **P1E0 — Android 16 Toolchain + Platform Readiness Audit:** CLOSED / PASS
>       (plan: [`P1E_ANDROID16_MIGRATION_PLAN.md`](P1E_ANDROID16_MIGRATION_PLAN.md));
>       next authorized candidate **P1E1** (not started)
>
> This roadmap records the agreed architectural direction at a high level only.
> No implementation work starts until a later phase is explicitly authorized.
> Current status lives in [`PROJECT_STATE.md`](../PROJECT_STATE.md).

## Program principles

- Preserve user data, licensing, and attribution at every step.
- Keep runtime behavior and UI stable until a phase explicitly changes them.
- Land work in small, reviewable increments on `feature/android-modernization`.

## Baseline (frozen)

The ProotX 1.0.0 baseline is frozen at tag `v1.0.0-baseline`.

- Package: `io.github.lord1egypt.prootx`
- Version: `1.0.0`
- Derived from the last self-contained public UserLAnd v2.8.3 codebase (GPLv3).
- Toolchain at freeze: JDK 8, Gradle 5.1.1, AGP 3.4.3, Kotlin 1.3.61, compileSdk 30, NDK 21.4.7075529.
- Verified with `assembleDebug` and the unit test suite (see the tag annotation for exact numbers).

## Agreed architectural direction

These are directional goals, not a committed order or design.

### 1. Modern Android / Google Play-compatible toolchain

Move to a currently supported Android build toolchain and SDK level so the app can be
maintained and distributed on Google Play, replacing the frozen legacy toolchain.

### 2. Modern UI: Jetpack Compose + Material 3

Replace the legacy XML/View UI with Jetpack Compose and Material 3, as a deliberate,
separately scoped redesign phase. The current UI is intentionally untouched until then.

### 3. Generic PRoot runtime

Replace distro-specific Android-side logic with a single generic PRoot runtime driven by
data (catalog and image metadata) rather than distro-specific code paths in the app.

### 4. Multiple simultaneous Linux instances

Support running more than one environment (instance) at the same time, instead of the
current single-active-session model.

### 5. Per-instance isolation and management

Manage processes, PTYs, filesystem layout, and network ports per instance, with clear
ownership and cleanup semantics for each running environment.

### 6. Full logging subsystem

Introduce structured logging with selectable verbosity modes (Normal / Debug / Trace)
available to users and support, replacing ad-hoc logging.

### 7. LAN browser-based web terminal

Provide an optional browser-based terminal on the local network for interacting with
running instances without a dedicated client app.

### 8. Secure optional remote access

Offer opt-in remote access with an explicit security model (authentication,
encryption, and safe defaults).

### 9. Dynamic distro/app/image catalog

Make the distro, application, and image catalog dynamic and remotely updatable, rather
than a fixed in-app list, so new environments can be introduced without an app release.

### 10. OCI / rootfs support

Support OCI-style images in addition to plain rootfs archives, broadening where
environments can come from.

### 11. ProotX curated images

Publish curated, ready-to-run ProotX images (for example, Hermes-on-Alpine and
OpenClaw-on-Alpine) as first-class catalog entries.

### 12. Play-compatible and Full distributions

If platform constraints require it, ship separate build variants: a Play-compatible
distribution and a Full distribution with capabilities Play does not permit.

### 13. Backup, restore, clone, export, import

Provide user-facing lifecycle operations for environments: backup, restore, clone,
export, and import.

### 14. Security and release hardening (final phase)

As the closing phase, perform a dedicated security review and release hardening pass
across the modernized codebase.

## Deferred Findings

Issues observed during P0 that are **intentionally not fixed in this phase**. Fixes are
deferred to the appropriate later phase.

1. **CI baseline failure — RESOLVED in P1A.** Both baseline GitHub Actions runs failed at
   the "Set up Android SDK" step because `actions/setup-java@v4` pinned JDK 8 while
   `sdkmanager` requires a modern JVM. CI now provisions the Android SDK/NDK under JDK 17
   and runs the legacy build under JDK 8 (green run `34674686561`).
2. **Legacy toolchain.** Gradle 5.1.1 / AGP 3.4.3 / Kotlin 1.3.61 / compileSdk 30 emit a
   deprecation warning ("incompatible with Gradle 6.0"). Modernization phase.
3. **Kotlin Android Extensions — FULLY RESOLVED in P1C (P1C1 + P1C2-P/R).** Synthetic view
   access migrated to View Binding; the legacy plugin/DSL and `kotlinx.android.parcel` were
   removed and replaced with `kotlin-parcelize` / `kotlinx.parcelize.Parcelize`. Two guard
   tests (`SyntheticViewImportsTest`, `LegacyAndroidExtensionsGuardTest`) prevent
   reintroduction.
4. **Sentry and Billing are ACTIVE — not unused.** `SentryLogger`/Sentry are used by
   production logging/repository/runtime paths, and `BillingManager`/`BillingClient`/`Purchase`
   are used by production purchase/contribution paths (plus the `com.android.vending.BILLING`
   permission). Both are intentionally left untouched; any change requires a dedicated
   milestone/decision.
5. **Prebuilt rootfs blobs.** Released distribution rootfs archives were built before the
   profile script rename and still contain an internal `/etc/profile.d/prootx.sh` from the
   old build. Distribution assets must not be modified in this phase; regenerate them in a
   later assets phase.
6. **`jcenter()` fallback repository — RESOLVED in P1B.** `jcenter()` was removed from
   `buildscript` and `allprojects`; the full build/test classpath resolves from
   `google()` + `mavenCentral()` (verified with a clean Gradle user home).
   Residual: `com.schibsted.spain:barista:3.1.0` (androidTest-only, JCenter-published) was
   **RESOLVED in P1D1**: removed and replaced with direct AndroidX Espresso; the debug app
   and unit tests were never affected. `BaristaRemovalGuardTest` blocks reintroduction and
   CI now compiles `:app:assembleDebugAndroidTest`.
7. **Network-dependent unit tests.** Some unit tests fetch live files over the network
   (raw GitHub asset catalog), making the test suite and CI network-dependent and flaky.
   Should be replaced with fixtures or mocked HTTP during modernization.
8. **Play readiness gaps.** `targetSdk` is 30 and the launcher activity lacks an explicit
   `android:exported` declaration required from target SDK 31+, so store-readiness work is
   required before any Play release.
9. **Dynamic `versionCode`.** `versionCode` is generated from wall-clock time at Gradle
   configuration time in `app/build.gradle`
   (`def vcode = (int)(((new Date().getTime()/1000) - 1559347200) / 10)`). This prevents a
   clean separation between a *candidate* version and the *last physically accepted*
   version (see `DECISIONS.md` D005). Recorded for a later release-contract milestone; not
   fixed in P0.5.
10. **Kotlin↔Moshi compatibility — RESOLVED in P1C2-P (bridge found).** Kotlin 1.4.32 was
    incompatible with Moshi 1.8.0 (metadata `KotlinNullPointerException`), while Moshi
    ≥1.10.0 could not run on Kotlin 1.3.61 (`NoSuchMethodError`). The previously untested
    cell **Kotlin 1.4.32 + Moshi 1.9.3** passes kapt and a full build, so **Moshi 1.9.3 is
    the bridge** across the Kotlin 1.3 → 1.4 boundary. Kotlin is now 1.4.32 and Moshi 1.9.3
    (see `DECISIONS.md` D013/D014).
11. **Dead Play Services dependency — RESOLVED in P1D2.** The unused direct
    `com.google.android.gms:play-services-base:17.2.1` dependency and the stale
    `ENABLE_PLAY_SERVICES` BuildConfig flag were removed; `play-services-base` is now
    **ABSENT** from all configurations and its manifest injections are gone. Guarded by
    `DeadPlayServicesGuardTest` (`DECISIONS.md` D017).
12. **Deprecated Lifecycle extensions / ViewModelProviders — RESOLVED in P1D3.** The
    monolithic `lifecycle-extensions` artifact was replaced by granular
    `lifecycle-viewmodel`/`lifecycle-livedata` 2.2.0, and `ViewModelProviders.of(...)` was
    migrated to `ViewModelProvider(...)` with scopes preserved (`DECISIONS.md` D018).
13. **Pre-release Navigation baseline — RESOLVED in P1D4.** `navigation_version` moved from
    2.1.0-alpha05 to **2.1.0** stable; the Kotlin JVM target was aligned to 1.8. Beneficial
    transitive stabilization: `androidx.fragment` → 1.1.0 and `lifecycle-runtime` /
    `lifecycle-viewmodel-ktx` → 2.1.0 (`DECISIONS.md` D019).
14. **Pre-release Room baseline — RESOLVED in P1D5.** `room_version` moved from 2.1.0-beta01
    to **2.1.0** stable. Zero database source change; exported schema 7 byte-identical (no
    drift); DB version 7 and migrations untouched (`DECISIONS.md` D020).
15. **Pre-release Preference baseline — RESOLVED in P1D6.** `preference_version` moved from
    1.1.0-alpha05 to **1.1.0** stable with zero source/XML change; appcompat transitively
    stabilized to 1.1.0 (`DECISIONS.md` D021).
16. **Material 1.1.0 stable drops `androidx.legacy` transitives — RESOLVED in P1D7-U2.**
    Material 1.1.0 stable's POM removes `androidx.legacy:legacy-support-core-ui`/
    `legacy-support-core-utils`. The app directly consumes two artifacts those supplied
    (`androidx.swiperefreshlayout:swiperefreshlayout:1.0.0`, `androidx.localbroadcastmanager:localbroadcastmanager:1.0.0`),
    so ProotX now declares both directly and Material is on **1.1.0** stable
    (`DECISIONS.md` D022). `androidx.legacy` remains only via `:terminal-term` (runtime) and
    `espresso-contrib` (androidTest), which are legitimate parents.
17. **Pre-release/misaligned `core-testing` — RESOLVED in P1D8.** The test-only
    `androidx.arch.core:core-testing` moved from 2.0.0-beta01 to **2.1.0** stable, aligning it
    with `core-common`/`core-runtime` (both already 2.1.0). Test-only; production unchanged.
18. **Misaligned `androidx.core:core-ktx` — RESOLVED in P1D9.** The direct `core-ktx`
    declaration moved from 1.0.2 to **1.1.0** stable, matching the resolved
    `androidx.core:core` 1.1.x family. No direct `collection`/`core` dependency was required;
    no source change.
19. **P1D Final Dependency Closure Audit — CLOSED / PASS (2026-09-15).** No dependency
    blocker was found: zero active direct pre-releases, zero pre-release transitives in every
    resolved graph, `core-ktx`/`core` coherent at `1.1.0`, and the canonical build + `326/36`
    JVM tests green. The audit produced a **classified deferred-debt backlog** (each item
    requires a dedicated future milestone/decision and none blocks P1E):
    - **Coroutines** — direct `1.0.0` declaration superseded at resolution (`core` `1.3.9` via
      `billing-ktx:3.0.3`, `android` `1.1.1` via `lifecycle-viewmodel-ktx:2.1.0`).
    - **Sentry** `1.7.22` — ACTIVE; legacy API (`Sentry.init`,
      `AndroidSentryClientFactory`, `EventBuilder`) coupled to production logging.
    - **Billing** `3.0.3` — ACTIVE; upgrade is API/source migration and a Play/release-policy
      concern, not a P1E prerequisite.
    - **Networking/serialization** — OkHttp `3.14.7`, Moshi `1.9.3` (verified Kotlin 1.4
      bridge), Gson `2.8.6` (rationalization/removal).
    - **Other** — JArchiveLib `0.8.0`, SLF4J-nop `1.7.26`, ConstraintLayout `1.1.3`,
      LocalBroadcastManager `1.0.0` (deprecated tech), and the JUnit4 / Mockito `2.23.0` /
      mockito-kotlin `2.1.0` / AndroidX-Test stack.
    - **Ownership** — `androidx.appcompat`, `androidx.fragment`, and `androidx.recyclerview`
      are consumed directly but supplied transitively; explicit ownership hardening deferred.
    - `androidx.legacy` remains only via `:terminal-term` (runtime) and
      `room-testing`/`espresso-contrib` (androidTest) — legitimate parents, not excluded.
20. **CI bootstrap infra breakage — RESOLVED in CI-R1.** The closure-documentation push
    failed remote CI in the third-party `android-actions/setup-android@v3` step, *before any
    ProotX build step*: the action reported "Wrong version in preinstalled sdkmanager" and
    then `Warning: Failed to find package 'tools'` → `sdkmanager` exit 1. Every prior run on
    this workflow succeeded, so this was an upstream runner-image / SDK-repository change,
    independent of repository content. Fixed by running `setup-android@v3` with
    `packages: ''` and installing `platform-tools` explicitly in the pinned `sdkmanager`
    step. Remote run `34919847167` (commit `4f3c812`) is green end-to-end.

## Non-goals for P0

No Android UI redesign, dependency/Gradle/AGP/Kotlin/SDK/NDK upgrades, runtime refactors,
distro additions, catalog changes, remote terminal implementation, or unrelated cleanup.
