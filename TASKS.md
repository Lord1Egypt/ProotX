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
- [~] **P1C — Kotlin / Synthetics Migration** — IN PROGRESS
  - [x] **P1C1 — Synthetic views → view binding** — PASS
    - [x] Enable View Binding (`buildFeatures.viewBinding`)
    - [x] Migrate MainActivity + 7 view-using Fragments
    - [x] Zero synthetic view imports; source guard test added
    - [x] Legacy Parcelize and `kotlin-android-extensions` preserved
  - [~] **P1C2 — Kotlin modernization + legacy Parcelize migration + plugin removal** — READY TO RETRY
    - [x] Upgrade Kotlin 1.3.61 → 1.4.32 (done in P1C2-P)
    - [x] Moshi 1.8.0 → 1.9.3 bridge (done in P1C2-P)
    - [ ] Migrate `kotlinx.android.parcel.Parcelize` → `kotlinx.parcelize.Parcelize`
    - [ ] Remove `kotlin-android-extensions` and `androidExtensions`
    - [ ] Add legacy-Android-extensions source guard
  - [x] **P1C2-U — Moshi compatibility unblocker** — CLOSED (superseded by P1C2-P)
    - Finding: Moshi ≥1.10.0 codegen is compiled against Kotlin 1.4 and throws
      `NoSuchMethodError` on Kotlin 1.3.61 (1.10.0/1.11.0 fail; 1.9.3 passes).
  - [x] **P1C2-P — Moshi 1.9.3 / Kotlin 1.4 bridge probe** — BRIDGE_FOUND
    - [x] Verify Kotlin 1.3.61 + Moshi 1.9.3 (PASS)
    - [x] Verify the missing cell Kotlin 1.4.32 + Moshi 1.9.3 (kapt PASS, full build PASS)
    - [x] Commit the verified bridge (Kotlin 1.4.32 + Moshi 1.9.3); remote CI green
- [ ] **P1D — AndroidX / dependency modernization**
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
