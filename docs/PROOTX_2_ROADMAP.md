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
>   - **P1E — SDK 36 / Manifest Compatibility:** CLOSED / PASS
>     - **P1E0 — Android 16 Toolchain + Platform Readiness Audit:** CLOSED / PASS
>     - **P1E1-P — Kotlin / AGP Build-Tooling Bridge Compatibility Probe:** CLOSED / BRIDGE_FOUND
>     - **P1E1 — Kotlin/AndroidX Codegen + Build-Tooling Bridge:** CLOSED / PASS
>       (Gradle 7.6.4 / AGP 7.4.2 / Kotlin 1.9.25 / Moshi 1.15.2 / Navigation 2.3.5 / JDK 17 /
>       NDK 21.4; plan: [`P1E_ANDROID16_MIGRATION_PLAN.md`](P1E_ANDROID16_MIGRATION_PLAN.md));
>     - **P1E2 — Moshi Codegen KAPT → KSP Migration:** CLOSED / PASS
>       (Moshi codegen → KSP 1.9.25-1.0.20; Room stays KAPT);
>     - **P1E3-P — AGP 8.10 / Kotlin 2.2 Compatibility Probe:** CLOSED / BRIDGE_FOUND
>       (Gradle 8.11.1 / AGP 8.10.1 / Kotlin 2.2.20 / KSP 2.2.20-2.0.4 / JDK 17 / Build Tools
>       35.0.0 / NDK 21.4; compileSdk stays 30);
>     - **P1E3 — AGP 8.10 / Kotlin 2.2 Implementation:** CLOSED / PASS
>       (accepted implementation `1828cdd`; remote CI `34974083190`; 37 suites / 327 tests;
>       local JaCoCo AGP-8 execution-data-path report PASS);
>     - **P1E4 — COMPILESDK 36 MIGRATION:** CLOSED / PASS
>       (app compileSdk 36; targetSdk 30; terminal 29/29/21; implementation `6d30b33`;
>       remote CI `35013165950`; local JaCoCo regression PASS);
>     - **P1E5 — API 31+ MANIFEST / PENDINGINTENT / RECEIVER COMPATIBILITY:** CLOSED / PASS
>       (MainActivity + TermuxActivity `exported="true"`; `ssh://` preserved; six immutable
>       PendingIntents; targetSdk stays 30; implementation `6e8a055`; remote CI `35020171430`)
>     - **P1E6 — STORAGE / PERMISSION RUNTIME COMPATIBILITY:** CLOSED / PASS
>       (legacy READ/WRITE_EXTERNAL_STORAGE + PermissionHandler removed; launch/SAF no longer
>       permission-gated; targetSdk stays 30; implementation `2202d6b`; remote CI `35028617203`)
>     - **P1E7 — FOREGROUND SERVICE / NOTIFICATION COMPATIBILITY:** CLOSED / PASS
>       (`specialUse` FGS type + `FOREGROUND_SERVICE_SPECIAL_USE` + subtype properties for both
>       services; service-owned channels; `startForegroundService` + immediate promotion +
>       `FOREGROUND_SERVICE_TYPE_MANIFEST`; `POST_NOTIFICATIONS` deferred to P1E8; targetSdk
>       stays 30; implementation `0e70b7e`; remote CI `35032934977`; 39 suites / 337 tests)
>     - **P1E8 — TARGETSDK 33/34 RUNTIME COMPATIBILITY:** CLOSED / PASS
>       (app targetSdk 30 → 34; `POST_NOTIFICATIONS` declared + one shared contextual request at
>       first session start; denial never blocks the session; resumed-lifecycle deferred FGS start
>       with narrow `ForegroundServiceStartNotAllowedException` handling; Termux `ssh://` direct
>       entry policy; `Context.RECEIVER_NOT_EXPORTED` on API 33+; `:terminal-term` compileSdk
>       29 → 36 with targetSdk 29; implementation `cff25f3`; remote CI `35037714144`;
>       40 suites / 348 tests)
>     - **P1E9 — TARGETSDK 35/36 PLATFORM BEHAVIOR:** CLOSED / PASS
>       (app targetSdk 34 → 36; real edge-to-edge with per-owner insets; Android 15/16 predictive
>       back; no edge-to-edge/back/orientation/large-screen opt-outs; Activity 1.11.0 bridge keeps
>       minSdk 21; implementation `41cc7a8`; remote CI `35043129415`; 41 suites / 355 tests)
>   - **P1E — SDK 36 / MANIFEST COMPATIBILITY:** CLOSED / PASS (P1E0–P1E9 complete; targetSdk 36)
> - **P1F — Modern Native/NDK and 16 KB Page Readiness:** IN PROGRESS
>   - **P1F-P — NDK / 16 KB Compatibility Probe:** CLOSED / PARTIAL_BRIDGE
>     (AGP 8.10.1 AAB already `PAGE_ALIGNMENT_16K`; NDK r29 fixes the in-tree `libtermux.so` with no
>     source change; support bundle remains 4 KB-only for x86_64; historical PRoot fork unavailable)
>   - **P1F1 — In-Tree NDK r29 Migration + CI Pin Modernization:** CLOSED / PASS
>     (in-tree NDK 21.4.7075529 → 29.0.14206865; 64-bit `libtermux.so` `PT_LOAD 0x4000`; CI pins
>     `ndk;29.0.14206865` and drops `ndk.dir`; scoped 16 KB native guard; implementation `8103b83`;
>     remote CI `35049015538`; 42 suites / 360 tests)
>   - **P1F2 — Support Toolchain / Provenance Modernization:** CLOSED / PASS
>     (unreproducible historical builder replaced; pinned builder digest + `termux-packages`
>     `0ffca06c` + checksum-verified `termux/proot v5.1.107.92`; dual-lane runtime — frozen legacy
>     normal slots for host API 21–28, modern `.a10` slots rebuilt at API 24 / NDK r29 for host API
>     29+; 16 KB aligned and `process_vm = yes`; two clean builds byte-identical; no release
>     published; support implementation `3e5c51e`, support CI `35057757988`)
>   - **P1F3 — Support Bundle Publication `v1.1.0`:** CLOSED / PASS
>     (provenance-backed release published; support `main` fast-forwarded to `acc28ab`; annotated
>     tag `ff55608`; release `RE_kwDOUXjkQM4XOeTw`; two clean four-ABI builds byte-identical; split
>     release CI with SHA-pinned actions; post-download hashes verified; whole-app 16 KB not
>     claimed)
>   - **P1F4-P — Whole-App 16 KB Preflight:** CLOSED / BRIDGE_FOUND
>     (initially downgraded to `PARTIAL_BRIDGE` after the metadata fixture failed; the R1 probe
>     recovered the exact 2019 `CypherpunkArmory/proot` meta lineage and the historical
>     `-DUSERLAND` contract, then proved runtime parity)
>   - **P1F4A — Complete Dual-Lane Support Release `v1.2.0`:** CLOSED / PASS
>     (explicit `common/`+`legacy/`+`modern/` schema + `manifest.json` routing + `routing.json`;
>     legacy frozen payload byte-for-byte; modern source-built NDK r29 API 24 all 64-bit
>     `PT_LOAD >= 0x4000`; binary-proven 2019 meta sidecars with `-DUSERLAND`; reproducible static
>     BusyBox 1.38.0; two clean four-ABI builds byte-identical with `SOURCE_DATE_EPOCH`+`TZ=UTC`;
>     final-release fixtures byte-identical parity; no filesystem migration; support `main`
>     fast-forwarded to `889cb67`; annotated tag `2b5691c9`; release `RE_kwDOUXjkQM4XTxeE`;
>     post-download verification passed; `v1.0.0`/`v1.1.0` untouched; whole-app 16 KB not claimed)
>   - **P1F4B — Support Packaging / Whole-App Static 16 KB Integration:** CLOSED / PASS
>     (ProotX consumes support `v1.2.0` via `app/support-release.lock.json`; deterministic
>     `prepareProotXSupport` generated staging — modern `jniLibs/<abi>/lib_<name>.so`, common/legacy
>     `assets/support/...`, `support-map.json` routing; manifest-driven installer extracts legacy on
>     API 21–28 and links modern from `nativeLibraryDir` on API 29+ (Android W^X); `.a10` and
>     `lib_arch` removed; APK/AAB native set all-ELF with no 4 KB 64-bit ELF; `zipalign -c -P 16`
>     PASS; bundletool `PAGE_ALIGNMENT_16K`; 46 suites / 375 tests; implementation `9f14ee3`;
>     feature CI `35281111291`; static acceptance only)
>   - **P1E-R1 — API36 First-Launch Platform Remediation:** CLOSED / PASS
>     (real API36 execution exposed three pre-existing launch defects static review missed: nav
>     graph root id; DownloadManager receiver export flag; Play Billing 3.0.3 internal receiver.
>     Fixed with a stable graph id, `Context.RECEIVER_EXPORTED` on API 33+ with id validation, and
>     migration to `com.android.billingclient:billing:8.0.0` preserving minSdk 21; `65b729f`)
>   - **P1F5 — True 16 KB Runtime / Emulator Acceptance Gate:** CLOSED / PASS
>     (real 16384-byte Android 16 / API36 `google_apis_ps16k` x86_64 emulator; support
>     `v1.2.0`/`x86_64`/`MODERN`; idempotency; busybox; busybox_static; proot
>     `prootx_16k_session_ok`; execInProot `prootx_execinproot_16k_ok`; normal filesystem;
>     `_meta`/`_meta_leveldb` parity + restart persistence; compress/extract roundtrip; session
>     restart; process cleanup; lifecycle; real DownloadManager completion; clean logcat; 50
>     suites / 398 tests; feature CI `35297254107`; real-distro smoke NOT RUN — external
>     dependency)
> - **P1F — Modern NDK / 16 KB Page-Size Compatibility:** CLOSED / PASS
> - **P1G — Physical-Device Acceptance:** READY TO START
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
2. **Legacy build toolchain and compile SDK — RESOLVED through P1E4.** The persistent build
   uses Gradle 8.11.1 / AGP 8.10.1 / Kotlin 2.2.20 / JDK 17, and the app compiles against API
   36 while targetSdk remains 30. Terminal modules remain 29/29/21.
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
    the bridge** across the Kotlin 1.3 → 1.4 boundary. At that milestone Kotlin became 1.4.32
    and Moshi 1.9.3 (see `DECISIONS.md` D013/D014); later P1E milestones superseded both pins.
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
    - **Networking/serialization** — OkHttp `3.14.7` + Okio `3.7.0` runtime validation and
      Gson `2.8.6` rationalization/removal. Moshi is now 1.15.2 on KSP2.
    - **Other** — JArchiveLib `0.8.0`, SLF4J-nop `1.7.26`, ConstraintLayout `1.1.3`,
      LocalBroadcastManager `1.0.0` (deprecated tech), and the JUnit4 / Mockito `4.11.0` /
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
21. **P1E3 build-tooling follow-ups — DEFERRED, non-blocking.** Manifest `package` warnings;
    `JavaExec.main` → `mainClass` before Gradle 9; legacy Android DSL and `lintOptions` cleanup;
    `String.capitalize()`; configuration-time custom task behavior; Node/action maintenance
    warnings; `ndk.dir`; OkHttp 3.14.7 + Okio 3.7.0 runtime validation; future Room KAPT
    migration; and the core/core-ktx family note. P1E3 intentionally did not fix these.
22. **P1E4 API-36 compile warnings — DEFERRED, non-blocking.** The newer SDK surfaces
    deprecations in generated Safe Args `Bundle.get` calls and existing network, parcelable
    extra, foreground-service, and display-metrics APIs. These are recorded for their owning
    compatibility/dependency milestones; P1E4 made no opportunistic behavior change. Existing
    native-strip, manifest, Gradle 9, Kotlin annotation/`capitalize`, `ndk.dir`, and action/Node
    maintenance warnings also remain deferred.
23. **P1E5 receiver/export and tooling follow-ups — DEFERRED, non-blocking.** The
    `TermuxActivity` custom `com.termux.app.reload_style` receiver is classified as
    `RECEIVER_NOT_EXPORTED` for future targetSdk-34 work, but the explicit flag cannot be applied
    while `:terminal-term` compiles against API 29. `LocalBroadcastManager` (in-process) and the
    `MainActivity` `DownloadManager` system-broadcast receiver remain unchanged. The custom
    `jacocoCoverageReportForCi` task lists AGP 8's instrumented
    `intermediates/classes/debug/jacocoDebug` output in its `classDirectories`, so it fails with
    `Cannot process instrumented class` when run after `assembleDebug`; it passes from a
    `clean` report-only state. This is a pre-existing build-tooling limitation (P1E3-era) and was
    **not** remediated in P1E5. Lint debt (14 pre-existing errors, 131 warnings, 3 hints)
    remains deferred, including the intentional `ExpiredTargetSdkVersion` while targetSdk is 30.
24. **P1E6 dead legacy permission-continuation code — DEFERRED, non-blocking.** Removing the live
    storage-permission dependency left `MainActivityViewModel.waitForPermissions` /
    `permissionsHaveBeenGranted`, the `TooManySelectionsMadeWhenPermissionsGranted` /
    `NoSelectionsMadeWhenPermissionsGranted` `IllegalState`s, their unit tests, and the unused
    `alert_permissions_necessary_*` strings unreachable. Deleting them would expand P1E6 into
    ViewModel state classes, localization resources, and unrelated tests, so they are recorded
    for a dedicated cleanup milestone. The `jacocoCoverageReportForCi` instrumented-class caveat
    remains pre-existing/deferred. P1E6's removal of the permission gate is an intentional
    compatibility behavior change; data locations and the runtime architecture are unchanged.

25. **P1E7 notification-permission sequencing — CORRECTED and DEFERRED, non-blocking.** The
    earlier plan placed `POST_NOTIFICATIONS` in P1E7. That is corrected: while app targetSdk is 30,
    an app targeting API ≤ 32 does not control the Android 13+ notification-permission dialog
    timing the way a target-33+ app does, so declaring it early could surface premature,
    system-timed first-run UX. P1E7 therefore owns only FGS structural compatibility and
    service-owned channels; `POST_NOTIFICATIONS` is declared and requested in **P1E8** together
    with the targetSdk 33/34 raise (`DECISIONS.md` D031). Also deferred to P1E8: the targetSdk
    31+ FGS background-start restrictions for `MainActivity.autoStart()`/`onNewIntent()` (the
    `onNewIntent` external-intent-while-backgrounded path is a potential
    `ForegroundServiceStartNotAllowedException` risk; the normal foreground/user-initiated path is
    structurally correct now), and the `TermuxActivity` custom `com.termux.app.reload_style`
    receiver `RECEIVER_NOT_EXPORTED` flag. **All three landed in P1E8 (see item 26).**
26. **P1E8 targetSdk 34 runtime changes — RESOLVED, with two small deferred follow-ups.** The app now
    targets 34 with a contextual one-time `POST_NOTIFICATIONS` request (denial never blocks the
    session), a resumed-lifecycle FGS start gate with narrow
    `ForegroundServiceStartNotAllowedException` handling, and a `Context.RECEIVER_NOT_EXPORTED`
    custom terminal receiver on API 33+; `:terminal-term` compiles at API 36 while its targetSdk
    stays 29 (`DECISIONS.md` D032). Two non-blocking items remain recorded for later:
    - the `TermuxService` `specialUse` FGS type is still declared via the app manifest overlay
      rather than the terminal module's own manifest — revisitable now that the module compiles at
      API 36;
    - `androidx.test:core:1.2.0` omits `android:exported` on its `InstrumentationActivityInvoker`
      activities, so the target-31+ merger needs a test-only `app/src/androidTest/AndroidManifest.xml`
      overlay; revisit (and drop the overlay + the `UnspecifiedRegisterReceiverFlag` lint
      suppression for the system `DownloadManager` receiver) when `androidx.test` is eventually
      upgraded.
27. **P1E9 targetSdk 36 platform behavior — RESOLVED (P1E CLOSED / PASS).** The app targets 36 and
    adapts to Android 15/16 rather than opting out: true edge-to-edge with per-owner
    `WindowInsetsCompat`, and predictive back via the authorized
    `androidx.activity:activity-ktx:1.11.0` bridge (`MainActivity`) and a platform
    `OnBackInvokedCallback` (`TermuxActivity`), keeping `minSdk 21` (`DECISIONS.md` D033). No
    edge-to-edge/back opt-out, no orientation lock, no large-screen opt-out. Non-blocking follow-up
    recorded for a later dependency-alignment milestone: the bridge moved core/core-ktx to 1.13.0,
    lifecycle to 2.6.2, savedstate to 1.2.1, and coroutines to 1.7.3, and `androidx.core` injects
    the benign signature `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`. Physical validation of
    edge-to-edge, predictive back, IME, and VNC geometry is **P1G**; NDK / 16 KB work is **P1F**.
28. **P1F-P / P1F1 16 KB native work — IN PROGRESS, support bundle still blocking.** The probe
    (PARTIAL_BRIDGE) confirmed AGP 8.10.1 already emits `PAGE_ALIGNMENT_16K` for the AAB and that
    NDK r29 fixes the in-tree library; P1F1 migrated the in-tree NDK pin to **29.0.14206865**, moved
    NDK selection to the Gradle `ndkVersion` (dropping the deprecated `ndk.dir`), and added a scoped
    CI guard on the packaged 64-bit `libtermux.so` (`DECISIONS.md` D034). Blocking follow-ups:
    `ProotX-Assets-Support` v1.0.0 still ships 4 KB-only x86_64 ELFs (and the arm64 `loader32` 32-bit
    helper), the support builder is unpinned (`ubuntu:latest`, floating branches), and its PRoot
    source `Lord1Egypt/proot@merge-it` is currently **unavailable (404)** — so the bundle is not
    reproducible as documented. **P1F2 must regenerate the complete support ELF alignment table from
    binaries first** (the P1F-P report contained an internal inconsistency: a blanket "all arm32
    `0x1000`" statement versus `armeabi-v7a busybox_static` being classified 16K-compatible).
    Google Play's applicable requirement: apps targeting **Android 15 / API 35+** must support
    **16 KB page sizes on 64-bit devices**; current Android documentation gives **February 1, 2027**
    as the update-enforcement date. **Full application 16 KB compatibility is not claimed.**
29. **P1F2 support toolchain/provenance — RESOLVED, legacy 64-bit 4 KB debt still open.** The
    `ProotX-Assets-Support` builder is now reproducible and source-traceable (`DECISIONS.md` D035):
    pinned builder image digest, `termux-packages` commit, and checksum-verified
    `termux/proot v5.1.107.92`; the historical `ubuntu:latest`/floating-fork/blind-`sed` pipeline is
    removed. The runtime is split: frozen legacy normal slots (host API 21–28) and a source-rebuilt
    modern `.a10` lane at API 24 / NDK r29 (host API 29+, 16 KB aligned, `process_vm = yes`), with
    `ProotXFiles` selection unchanged and `minSdk` still 21. Two independent clean builds produced
    byte-identical modern binaries and candidate archives. **Still blocking final P1F closure:**
    4 KB 64-bit **legacy normal-slot** ELFs (`proot`, `libtalloc.so.2`, `proot_meta`,
    `proot_meta_leveldb`, vendored 64-bit) remain shipped through `jniLibs`/`nativeLibraryDir`, and
    `proot_meta`/`proot_meta_leveldb` remain unknown-provenance frozen inputs. No support release was
    published; ProotX remains on `v1.0.0`. P1F3 must resolve the legacy packaging debt.
30. **P1F3 support bundle publication — RESOLVED; legacy 4 KB debt still open.** Support release
    `v1.1.0` is published (annotated tag `ff55608c…` → commit `acc28abcd…`; release
    `RE_kwDOUXjkQM4XOeTw`; assets `arm64-v8a 7f279264…`, `armeabi-v7a f7b935f6…`, `x86 25a53c33…`,
    `x86_64 f6248107…` + `SHA256SUMS` + provenance manifest + SPDX SBOM). Two independent clean
    four-ABI builds were byte-identical; release CI is split (untrusted validation / privileged
    build / write-token publish with no rebuild) with SHA-pinned actions (`DECISIONS.md` D036).
    `v1.0.0` remains untouched and ProotX still downloads it. **Still open (P1F4):** 13 x86_64
    legacy normal-slot 4 KB ELFs and the unknown-provenance `proot_meta`/`proot_meta_leveldb` are
    still shipped through `jniLibs`/`nativeLibraryDir`; whole-app 16 KB compatibility is not claimed.
31. **P1F4A complete dual-lane support release — RESOLVED; legacy packaging still open.** Support
    release `v1.2.0` is published (annotated tag object `2b5691c9…` → commit `889cb67b…`; release
    `RE_kwDOUXjkQM4XTxeE`; assets `arm64-v8a 42fd0042…`, `armeabi-v7a 496c5d70…`, `x86 df5e8b3a…`,
    `x86_64 d3884105…`, plus `routing.json`, `SHA256SUMS`, provenance manifest and SPDX SBOM). Each
    archive uses an explicit `common/`+`legacy/`+`modern/` schema with `manifest.json`. The legacy
    lane preserves the frozen v1.1.0/v1.0.0 payload; the modern lane is source-built with NDK r29 at
    API 24 and all 64-bit ELFs are `PT_LOAD >= 0x4000`. `proot_meta`/`proot_meta_leveldb` are no
    longer unknown-provenance (binary-proven 2019 lineages with `-DUSERLAND`), and final-release
    fixtures reproduce the frozen metadata behaviour with byte-identical parity. Two clean four-ABI
    builds are byte-identical (`SOURCE_DATE_EPOCH`, `TZ=UTC`) and the release CI verified the
    archives against the committed manifest (`DECISIONS.md` D037). `v1.0.0`/`v1.1.0` are untouched
    and ProotX still downloads `v1.0.0`. **Still open (P1F4B):** the 13 x86_64 legacy 4 KB ELFs are
    still shipped through `jniLibs`/`nativeLibraryDir`; whole-app 16 KB compatibility is not claimed.
32. **P1F4B support packaging / whole-app static 16 KB integration — RESOLVED; runtime acceptance
    still open.** ProotX consumes support `v1.2.0` through `app/support-release.lock.json`. The flat
    pseudo-`.so` transport is replaced by a deterministic `prepareProotXSupport` generated staging
    (modern `jniLibs/<abi>/lib_<name>.so`, common/legacy `assets/support/...`, `support-map.json`
    routing), and a manifest-driven installer extracts the frozen legacy payload on API 21–28 and
    links the modern payload from `nativeLibraryDir` on API 29+ (Android W^X). `.a10` inference and
    the `lib_arch.so` marker are removed. The APK/AAB native-library set is all-ELF, contains no
    legacy/common file, and has no 4 KB 64-bit ELF; `zipalign -c -P 16` passes and bundletool reports
    `PAGE_ALIGNMENT_16K` (`DECISIONS.md` D037). Implementation `9f14ee3`; feature CI `35281111291`.
    `minSdk` stays 21 and all four ABIs are retained. **Still open (P1F5/P1G):** the 16 KB
    runtime/emulator acceptance (`getconf PAGE_SIZE` = 16384) and physical-device acceptance;
    whole-app runtime 16 KB compatibility is not claimed.

## Non-goals for P0

No Android UI redesign, dependency/Gradle/AGP/Kotlin/SDK/NDK upgrades, runtime refactors,
distro additions, catalog changes, remote terminal implementation, or unrelated cleanup.
