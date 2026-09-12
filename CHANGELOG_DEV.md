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
