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

## P0.5 — Project Control Plane — CURRENT

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
- [ ] Commit + push control plane on `feature/android-modernization` (this milestone)

## P1 — Android Modernization — NOT STARTED

Conceptual increments (subdivision not immutable; see `DECISIONS.md`):

- [ ] **P1A — Build-system / JDK / CI foundation**
  - [ ] Repair baseline CI Android SDK setup failure (JDK 8 vs. `sdkmanager`)
  - [ ] Establish reproducible modern build invocation
- [ ] **P1B — Gradle / AGP migration**
  - [ ] Upgrade Gradle and Android Gradle Plugin to supported versions
  - [ ] Migrate repositories off `jcenter()` to Maven Central / Google Maven
- [ ] **P1C — Kotlin + synthetics migration**
  - [ ] Remove `kotlin-android-extensions` synthetics (view binding)
  - [ ] Upgrade Kotlin to a supported version
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
