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
- [~] **P1D — AndroidX / dependency modernization** — IN PROGRESS
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
  - [!] **P1D7 — Material Components 1.1.0 stable migration** — BLOCKED
    - [!] Material 1.1.0 stable drops `legacy-support-core-ui`/`core-utils`, removing
      transitive `androidx.swiperefreshlayout:swiperefreshlayout:1.0.0`
    - Blocker: app uses `SwipeRefreshLayout` directly (`frag_app_list.xml`) → build fails;
      requires an explicit `androidx.swiperefreshlayout` dependency (outside authorized scope)
  - [!] **P1D7-U — SwipeRefreshLayout ownership + Material retry** — BLOCKED
    - [x] Add explicit `androidx.swiperefreshlayout:swiperefreshlayout:1.0.0` (validated under Material alpha06)
    - [!] Material 1.1.0 retry exposed a **second** missing transitive:
      `androidx.localbroadcastmanager:localbroadcastmanager:1.0.0` (consumed by
      `MainActivity`/`ServerService`) — outside P1D7-U's single-dependency authorization → reverted
  - [ ] **P1D8 — dependency/AndroidX modernization** (Sentry/Billing inventory, etc.)
  - [ ] Update AndroidX and third-party dependencies to supported versions
  - [ ] Remove unused Sentry / Play Billing code and billing permission
- [ ] **P1E — SDK 36 / manifest compatibility**
  - [ ] Raise `compileSdk`/`targetSdk`; add `android:exported` and related manifest work
- [ ] **P1F — Modern native/NDK and 16 KB page readiness**
  - [ ] Update NDK / native toolchain; verify 16 KB page-size compatibility
- [ ] **P1G — Modernization regression candidate and physical acceptance**
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
