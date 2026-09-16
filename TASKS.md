# ProotX Tasks

> Milestone-level tracking. `DECISIONS.md` remains authoritative for architecture
> changes; this file tracks progress, not design.

Legend: `[x]` done · `[ ]` not started · `[~]` in progress · `[!]` blocked

## P0 — Baseline Freeze & Development Safety — CLOSED / PASS

- [x] Repository and Git state inspected
- [x] Baseline build verified (`assembleDebug`)
- [x] Measured test count recorded (313 tests / 24 suites / 0 failures)
- [x] Annotated baseline tag created (`v1.0.0-baseline`)
- [x] `develop` branch created at baseline
- [x] `feature/android-modernization` branch created from `develop`
- [x] Roadmap created (`docs/PROOTX_2_ROADMAP.md`)
- [x] P0 closed

## P0.5 — Project Control Plane — CLOSED / PASS

- [x] Verify P0 baseline invariants (main/develop/tag/feature)
- [x] Create `PROJECT_STATE.md`
- [x] Create `SESSION_HANDOFF.md`
- [x] Create `TASKS.md`
- [x] Create `DECISIONS.md`
- [x] Create `CHANGELOG_DEV.md`
- [x] Create `UPSTREAM_BASELINE.md`
- [x] Create `ASSET_TRACKING.md`
- [x] Create `AGENTS.md`
- [x] Record dynamic `versionCode` deferred finding
- [x] Reflect P0/P0.5/P1 status in roadmap
- [x] Commit + push control plane on `feature/android-modernization` (this milestone)

## P1 — Android Modernization — IN PROGRESS

Conceptual increments (subdivision not immutable; see `DECISIONS.md`):

- [x] **P1A — Build-system / JDK / CI foundation** — PASS
  - [x] Repair baseline CI Android SDK setup failure (JDK 8 vs. `sdkmanager`)
  - [x] Two-stage CI: JDK 17 for SDK tooling, JDK 8 for the legacy Gradle build
  - [x] Pin required SDK/NDK packages
  - [x] Extend CI triggers to `develop` and `feature/**`
  - [x] Establish reproducible build invocation and remote green run
  - [x] Document environment in `docs/BUILD_ENVIRONMENT.md`
- [x] **P1B — Gradle / AGP bridge migration** — PASS
  - [x] Upgrade Gradle wrapper 5.1.1 → 6.7.1
  - [x] Upgrade AGP 3.4.3 → 4.2.2 (Kotlin unchanged)
  - [x] Remove `jcenter()`; prove resolution from Google Maven + Maven Central
  - [x] Align CI build-tools pin to `30.0.2`
  - [x] Local + remote green build and 313 tests; APK uploaded
- [x] **P1C — Kotlin / Synthetics Migration** — CLOSED / PASS
  - [x] **P1C1 — Synthetic views → view binding** — PASS
    - [x] Enable View Binding (`buildFeatures.viewBinding`)
    - [x] Migrate MainActivity + 7 view-using Fragments
    - [x] Zero synthetic view imports; source guard test added
  - [x] **P1C2 — Kotlin modernization + legacy Parcelize migration + plugin removal** — PASS
    - [x] Upgrade Kotlin 1.3.61 → 1.4.32 (done in P1C2-P)
    - [x] Moshi 1.8.0 → 1.9.3 bridge (done in P1C2-P)
    - [x] Migrate `kotlinx.android.parcel.Parcelize` → `kotlinx.parcelize.Parcelize`
    - [x] Replace `kotlin-android-extensions` with `kotlin-parcelize`; remove `androidExtensions`
    - [x] Add legacy-Android-extensions source guard + Parcelable contract test
  - [x] **P1C2-U — Moshi compatibility unblocker** — CLOSED (superseded by P1C2-P)
    - Finding: Moshi ≥1.10.0 codegen is compiled against Kotlin 1.4 and throws
      `NoSuchMethodError` on Kotlin 1.3.61 (1.10.0/1.11.0 fail; 1.9.3 passes).
  - [x] **P1C2-P — Moshi 1.9.3 / Kotlin 1.4 bridge probe** — BRIDGE_FOUND
    - [x] Verify Kotlin 1.3.61 + Moshi 1.9.3 (PASS)
    - [x] Verify the missing cell Kotlin 1.4.32 + Moshi 1.9.3 (kapt PASS, full build PASS)
    - [x] Commit the verified bridge (Kotlin 1.4.32 + Moshi 1.9.3); remote CI green
- [x] **P1D — AndroidX / dependency modernization** — CLOSED / PASS
  - [x] **P1D1 — Remove Barista / restore androidTest build** — PASS
    - [x] Remove `com.schibsted.spain:barista:3.1.0`
    - [x] Migrate Barista usage to AndroidX Espresso (core/contrib/intents 3.2.0)
    - [x] Restore `:app:assembleDebugAndroidTest` and gate it in CI
    - [x] Add Barista regression guard test
  - [x] **P1D2 — Dead Play Services dependency cleanup** — PASS
    - [x] Remove unused direct `com.google.android.gms:play-services-base` dependency
    - [x] Remove stale `ENABLE_PLAY_SERVICES` BuildConfig flag (default + debug)
    - [x] Verify `play-services-base` ABSENT in all configurations; guard reintroduction
  - [x] **P1D3 — Lifecycle extensions removal / ViewModelProvider migration** — PASS
    - [x] Replace `lifecycle-extensions` with granular `lifecycle-viewmodel`/`lifecycle-livedata` 2.2.0
    - [x] Migrate `ViewModelProviders.of(...)` → `ViewModelProvider(...)` (scopes preserved)
    - [x] Guard against `lifecycle-extensions` / `ViewModelProviders` reintroduction
  - [x] **P1D4 — Navigation 2.1.0 stable migration** — PASS
    - [x] Move shared `navigation_version` 2.1.0-alpha05 → 2.1.0 stable
    - [x] Align Kotlin `jvmTarget` to 1.8 (Navigation ktx inline bytecode)
    - [x] Guard stable Navigation baseline
  - [x] **P1D5 — Room 2.1.0 stable migration** — PASS
    - [x] Move shared `room_version` 2.1.0-beta01 → 2.1.0 stable
    - [x] Verify zero database source change and no schema drift (schema 7 byte-identical)
    - [x] Guard stable Room baseline
  - [x] **P1D6 — AndroidX Preference 1.1.0 stable migration** — PASS
    - [x] Move shared `preference_version` 1.1.0-alpha05 → 1.1.0 stable
    - [x] Verify zero source/XML change (settings keys/defaults/dependencies frozen)
    - [x] Guard stable Preference baseline
  - [x] **P1D7 — Material Components 1.1.0 stable migration** — PASS
    - [x] Material `1.1.0-alpha06` → `1.1.0` stable
  - [x] **P1D7-U — SwipeRefreshLayout ownership + Material retry** — CLOSED (superseded by P1D7-U2)
  - [x] **P1D7-U2 — Explicit legacy replacements + Material final retry** — PASS
    - [x] Declare `androidx.swiperefreshlayout:swiperefreshlayout:1.0.0` directly
    - [x] Declare `androidx.localbroadcastmanager:localbroadcastmanager:1.0.0` directly
    - [x] Material 1.1.0 stable (androidx.legacy no longer needed on the compile classpath)
    - [x] Guard Material dependency ownership
  - [x] **P1D8 — Arch Core testing 2.1.0 stabilization** — PASS
    - [x] `core_testing_version` 2.0.0-beta01 → 2.1.0 (test + androidTest)
    - [x] Arch Core family coherent at 2.1.0; production source/runtime unchanged
    - [x] Guard stable Arch Core testing baseline
  - [x] **P1D9 — AndroidX Core KTX 1.1.0 alignment** — PASS
    - [x] `ktx_version` 1.0.2 → 1.1.0 (matches resolved `core` 1.1.0)
    - [x] No direct `collection`/`core` dependency needed
    - [x] Guard Core KTX alignment
  - [x] **P1D Final Dependency Closure Audit** — PASS (no dependency blocker; P1D CLOSED)
  - [ ] Dependency modernization beyond the accepted baseline — **DEFERRED** to dedicated
        future migrations: Coroutines, Sentry, Billing, OkHttp, Moshi, Gson, JArchiveLib,
        ConstraintLayout, AppCompat/Fragment/RecyclerView ownership, test stack.
  - [ ] Sentry/Billing: **ACTIVE** production code — requires a dedicated decision before any change
        (not "unused")
- [x] **CI-R1 — Android SDK bootstrap remediation** — CLOSED / PASS
  - [x] Root cause: `android-actions/setup-android@v3` default `tools` package retired
  - [x] `packages: ''` on `setup-android`; explicit `platform-tools` in pinned `sdkmanager` step
  - [x] Remote run `34919847167` (commit `4f3c812`) green: bootstrap, build, `326/36` tests,
        androidTest APK, both artifacts
- [x] **P1E — SDK 36 / manifest compatibility** — CLOSED / PASS (P1E0–P1E9 complete; targetSdk 36)
  - Plan: `docs/P1E_ANDROID16_MIGRATION_PLAN.md`
  - [x] **P1E0 — Android 16 toolchain + platform readiness audit** — PASS
        (target: AGP 8.10 / Gradle 8.11.1 / Kotlin 2.2.x / JDK 17 / Build Tools 35;
        staged bridge required; no changes made)
  - [x] **P1E1-P — Kotlin / AGP build-tooling bridge compatibility probe** — BRIDGE_FOUND
        (proven: Gradle 7.6.4 / AGP 7.4.2 / Kotlin 1.9.25 / Moshi 1.15.2 / Navigation 2.3.5 /
        JDK 17 / NDK 21.4 / JaCoCo 0.8.8 + Mockito 4.11.0; 326 tests green; reverted)
  - [x] **P1E1 — Kotlin/AndroidX codegen + build-tooling bridge** — CLOSED / PASS
        (persisted Gradle 7.6.4 / AGP 7.4.2 / Kotlin 1.9.25 / Moshi 1.15.2 / Navigation 2.3.5 /
        JaCoCo 0.8.8 / Mockito 4.11.0 / JDK 17 / NDK 21.4 / gradle-download-task 5.0.0;
        7 Kotlin-1.9 source fixes; CI on JDK 17; local + remote 326 tests green)
  - [x] **P1E2 — Moshi codegen KAPT → KSP migration** — CLOSED / PASS
        (KSP 1.9.25-1.0.20 on `:app`; Moshi codegen → `ksp`; Room stays `kapt`; kotlin-kapt
        retained; Moshi KAPT warning gone; + `MoshiKspGuardTest`; local + remote 327 tests)
  - [x] **P1E3-P — AGP 8.10 / Kotlin 2.2 compatibility probe** — BRIDGE_FOUND
        (proven: Gradle 8.11.1 / AGP 8.10.1 / Kotlin 2.2.20 / KSP 2.2.20-2.0.4 / JDK 17 /
        Build Tools 35.0.0 / NDK 21.4; Moshi KSP2 + Room KAPT + Safe Args 2.3.5; 327 tests;
        all edits reverted)
  - [x] **P1E3 — AGP 8.10 / Kotlin 2.2 implementation** — CLOSED / PASS
        (Gradle 8.11.1 / AGP 8.10.1 / Kotlin + stdlib 2.2.20 / KSP 2.2.20-2.0.4 KSP2 /
        JDK 17 / Build Tools 35.0.0 / NDK 21.4; four namespaces; app-only BuildConfig;
        Moshi KSP2 + Room KAPT + Safe Args 2.3.5; five lowercase substitutions; non-transitive
        terminal R ownership; JaCoCo `required` DSL and AGP-8 execution-data path; remote run
        `34974083190` + local JaCoCo report proof; 37 suites / 327 tests)
  - [x] **P1E4 — COMPILESDK 36 MIGRATION** — CLOSED / PASS
        (`:app` compileSdk 30 → 36; targetSdk/minSdk remain 30/21; terminal 29/29/21;
        CI platform 30 → 36; API-36 `PackageInfo.versionName` nullability contract made
        explicit with `info.versionName!!`; local gates + JaCoCo PASS; remote run
        `35013165950`; 37 suites / 327 tests; runtime/UI unchanged)
  - [x] **P1E5 — API 31+ manifest / PendingIntent / receiver compatibility** — CLOSED / PASS
        (`MainActivity` + `TermuxActivity` `android:exported="true"`; `ssh://` BROWSABLE entry
        point preserved; six PendingIntents explicitly immutable with one retaining
        `FLAG_UPDATE_CURRENT`; receiver classification documented, Termux custom-receiver flag
        deferred to targetSdk-34 work; disposable targetSdk 31 probe passed then reverted;
        targetSdk remains 30; local gates + JaCoCo PASS; `assembleDebugAndroidTest` PASS; remote
        run `35020171430`; 37 suites / 327 tests; runtime/UI unchanged)
  - [x] **P1E6 — storage / permission runtime compatibility** — CLOSED / PASS
        (removed `READ_EXTERNAL_STORAGE`/`WRITE_EXTERNAL_STORAGE` from the app manifest and
        `WRITE_EXTERNAL_STORAGE` from terminal-term; deleted `PermissionHandler.kt`; app/session
        launch + SAF import/export no longer permission-gated; `TermuxActivity` obsolete storage
        helper/constant/imports removed; no replacement broad permission; app-scoped storage
        paths unchanged; disposable targetSdk 33 probe passed then reverted; new
        `StoragePermissionGuardTest`; local gates + JaCoCo PASS; remote run `35028617203`;
        38 suites / 329 tests)
  - [x] **P1E7 — foreground service / notification compatibility** — CLOSED / PASS
        (`FOREGROUND_SERVICE_SPECIAL_USE` declared; `ServerService` + `TermuxService` typed
        `specialUse` with `PROPERTY_SPECIAL_USE_FGS_SUBTYPE` (terminal service overlaid from the
        API-36 app manifest; `:terminal-term` stays compileSdk 29; one merged component,
        `exported=false`); each service owns its `"ProotX"` channel (`IMPORTANCE_LOW`);
        `MainActivity`'s redundant channel init removed; initial session/terminal launch uses
        `startForegroundService` on API 26+; `ServerService` promotes synchronously before async
        work; `FOREGROUND_SERVICE_TYPE_MANIFEST` on API 29+; `POST_NOTIFICATIONS` deferred to P1E8
        per `D031`; disposable targetSdk 34 probe passed then reverted; new
        `ForegroundServiceCompatibilityGuardTest`; local gates + JaCoCo PASS; remote run
        `35032934977`; 39 suites / 337 tests)
  - [x] **P1E8 — targetSdk 33/34 runtime compatibility** — CLOSED / PASS
        (app targetSdk 30 → 34; `POST_NOTIFICATIONS` declared + requested contextually at first
        session start via one shared `notification_permission`/`prompt_completed` prefs flag;
        `checkSelfPermission` is authoritative; denial never blocks the session and an explicit
        denial stops re-prompting; resumed-lifecycle FGS gate + narrow
        `ForegroundServiceStartNotAllowedException` handling with pending-session retry;
        `TermuxActivity` direct `ssh://` entry uses the same one-time policy and starts/binds once;
        `Context.RECEIVER_NOT_EXPORTED` on API 33+ for the app-internal `reload_style` receiver;
        `:terminal-term` compileSdk 29 → 36 (targetSdk 29 / minSdk 21), terminal-view/emulator stay
        29/29/21; test-only androidTest manifest overlay for `androidx.test:core:1.2.0` exported
        values; `TargetSdk34CompatibilityGuardTest` added and the P1E7 guard updated; disposable
        targetSdk 33 checkpoint passed; local gates + JaCoCo PASS; remote run `35037714144`;
        40 suites / 348 tests)
  - [x] **P1E9 — targetSdk 35/36 platform behavior + final SDK 36 regression** — CLOSED / PASS
        (app targetSdk 34 → 36 after a clean target-35 checkpoint; `MainActivity` `enableEdgeToEdge()`
        + per-owner `WindowInsetsCompat` (toolbar top, bottom nav bottom, root left/right cutout)
        without cumulative padding; `TermuxActivity` platform `OnBackInvokedCallback` on API 33+
        with legacy `onBackPressed` fallback; authorized `androidx.activity:activity-ktx:1.11.0`
        bridge (minSdk 21 preserved, `onNewIntent` non-null); no edge-to-edge/predictive-back
        opt-out, no orientation lock, no large-screen opt-out; `TargetSdk34CompatibilityGuardTest`
        renamed/advanced to `TargetSdk36CompatibilityGuardTest`; new
        `TargetSdk36PlatformBehaviorGuardTest`; local gates + JaCoCo PASS; remote run `35043129415`;
        41 suites / 355 tests)
- [~] **P1F — Modern native/NDK and 16 KB page readiness** — IN PROGRESS
  - [x] **P1F-P — NDK / 16 KB compatibility probe** — CLOSED / PARTIAL_BRIDGE
        (AGP 8.10.1 AAB already `PAGE_ALIGNMENT_16K`; NDK r29 fixes in-tree `libtermux.so` with no
        source change; support bundle 4 KB-only for x86_64; PRoot source fork unavailable)
  - [x] **P1F1 — In-tree NDK r29 migration + CI pin modernization** — CLOSED / PASS
        (in-tree NDK 21.4.7075529 → 29.0.14206865 for `:app` + `:terminal-emulator`;
        arm64-v8a/x86_64 `libtermux.so` `PT_LOAD 0x4000`; CI pins `ndk;29.0.14206865`, drops
        `ndk.dir`, adds scoped 16 KB native guard; `NdkR29ToolchainGuardTest`; local gates + JaCoCo
        PASS; remote run `35049015538`; 42 suites / 360 tests)
  - [x] **P1F2 — Support toolchain / provenance modernization** — CLOSED / PASS
        (support `feature/p1f-support-modernization` implementation
        `a363e82b63ccc30ca678aba7dbb96c87d2879c08`, support CI `35057757988` SUCCESS; reproducible
        builder pinned to builder digest + termux-packages `0ffca06c…` + checksum-verified
        `termux/proot v5.1.107.92`; dual-lane runtime — frozen legacy normal slots for host API
        21–28, modern `.a10` slots rebuilt at API 24 / NDK r29 for host API 29+; 16 KB aligned,
        `process_vm = yes`; two independent clean builds byte-identical; no release published)
  - [x] **P1F3 — Support bundle publication `v1.1.0`** — CLOSED / PASS
        (annotated tag `v1.1.0` object `ff55608c…` → support commit `acc28ab…`; release
        `RE_kwDOUXjkQM4XOeTw` published 2026-09-16T05:59:57Z with four `*-assets.zip` +
        `SHA256SUMS` + `v1.1.0-provenance.json` + `v1.1.0.spdx.json`; two independent clean
        four-ABI builds byte-identical; release CI split into untrusted validation / privileged
        build / write-token publish with no rebuild, all actions SHA-pinned; post-download hashes
        verified; v1.0.0 untouched; whole-app 16 KB still not claimed)
  - [ ] **P1F4 — Support packaging / whole-app 16 KB integration (switch to v1.1.0 + isolate legacy 4 KB ELFs)** — READY TO START
  - [ ] **P1F5 — 16 KB emulator/static acceptance gate** — NOT STARTED
- [ ] **P1G — Modernization regression candidate and physical acceptance** — NOT STARTED
  - [ ] Produce a modernization candidate build
  - [ ] Physical-device acceptance (separate from source/test acceptance)

## Later Program Milestones (high level) — NOT STARTED

- [ ] Runtime V2 (generic, data-driven PRoot runtime)
- [ ] Multi-instance simultaneous runtime
- [ ] Logging subsystem (Normal / Debug / Trace)
- [ ] Compose + Material 3 UI redesign
- [ ] Catalog V2 (dynamic distro/app/image catalog)
- [ ] OCI / rootfs image support
- [ ] LAN browser-based web terminal
- [ ] Secure optional remote access
- [ ] ProotX Hub (curated images, e.g. Hermes-on-Alpine, OpenClaw-on-Alpine)
- [ ] Play / Full build flavors
- [ ] Backup / restore / clone / export / import
- [ ] Final security and release hardening
- [ ] Release-contract work: candidate vs. last physically accepted version (dynamic
      `versionCode` finding)
