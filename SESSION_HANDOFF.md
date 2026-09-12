# ProotX Session Handoff

> Durable handoff for any future engineering session. Read this together with
> `PROJECT_STATE.md`, `TASKS.md`, `DECISIONS.md`, and `docs/PROOTX_2_ROADMAP.md`.
> Never rely on previous chat transcripts; the repository is the source of truth.

Last updated: 2026-09-12 (P1A — Build-System / JDK / CI Foundation)

## Current Objective

Modernize ProotX toward the ProotX 2.0 architecture in controlled milestones, keeping the
application runtime/UI behavior invariant during toolchain work.

## Last Completed Milestone

**P1A — Build-System / JDK / CI Foundation**: **PASS**.
CI now provisions the Android SDK with JDK 17 and builds/tests the legacy app with JDK 8.
First fully green remote run achieved; remote test summary measured.

## Current Milestone

**P1B — Gradle / Android Gradle Plugin migration**: NOT STARTED.

## What Was Completed

- Repaired the baseline CI failure (JDK 8 was active when `sdkmanager` ran).
- Implemented a two-stage JDK bootstrap in `.github/workflows/build.yml`:
  JDK 17 for Android SDK/NDK provisioning, JDK 8 for the legacy Gradle build.
- Pinned the required SDK/NDK packages instead of relying on runner defaults:
  `platforms;android-30`, `platforms;android-29`, `build-tools;28.0.3`,
  `ndk;21.4.7075529`.
- Extended CI triggers to `develop` and `feature/**` (and PRs to `main`/`develop`).
- Made the build contract explicit:
  `./gradlew clean assembleDebug testDebugUnitTest --no-daemon`.
- Added a CI step that prints the exact unit-test summary.
- Added `docs/BUILD_ENVIRONMENT.md` documenting the two-JDK requirement.

## What Was Intentionally NOT Changed

- No Android/terminal source, Gradle/AGP/Kotlin versions, SDK levels, NDK version,
  dependencies, manifest, resources, UI, runtime, database, logging, network, billing,
  Sentry, package ID, `versionName`, or the `versionCode` algorithm.
- No asset repositories modified.

## Current Repository State

| Ref | SHA |
|---|---|
| Active branch | `feature/android-modernization` |
| Feature HEAD (P1A commit) | `006dc98055038c5510bc0672435417dacef8a807` |
| `main` | `94abf5fa520255bb10d087a6be3ba2bc70b0e127` |
| `develop` | `94abf5fa520255bb10d087a6be3ba2bc70b0e127` |
| Baseline tag | `v1.0.0-baseline` → `94abf5fa520255bb10d087a6be3ba2bc70b0e127` |

## Accepted Baseline

Commit `94abf5fa520255bb10d087a6be3ba2bc70b0e127`, package
`io.github.lord1egypt.prootx`, version `1.0.0`, **313 tests / 24 suites / 0 failures**,
JDK 8 / Gradle 5.1.1 / AGP 3.4.3 / Kotlin 1.3.61 / compileSdk 30 / NDK 21.4.7075529.
The P0 baseline remains the accepted application baseline (P1A changed no app artifact
source/build configuration).

## Known Deferred Findings

See [`docs/PROOTX_2_ROADMAP.md`](docs/PROOTX_2_ROADMAP.md#deferred-findings). The CI
Android SDK finding is **resolved** (P1A). Still open: legacy toolchain; Kotlin Android
synthetics; unused Sentry + Play Billing code and permission; prebuilt rootfs containing
old `/etc/profile.d/prootx.sh`; `jcenter()` fallback; network-dependent unit tests;
Play-readiness gaps (`targetSdk` 30, missing `android:exported`); dynamic time-based
`versionCode` release-contract gap.

## Important Invariants

1. `main` is stable and must not receive direct commits.
2. `develop` and `main` both point at the baseline until a milestone advances them.
3. `feature/android-modernization` is the only active work branch.
4. Runtime and UI behavior must stay invariant during toolchain-only modernization unless a
   milestone explicitly authorizes behavior change.
5. GPLv3 copyright and the required UserLAnd attribution must be preserved.
6. The six `ProotX-Assets-*` repositories are external runtime assets and are not modified
   without a dedicated assets milestone.
7. Published history is immutable: no force-push, no history rewrite.
8. One milestone at a time; respect STOP gates.
9. CI must run the Android SDK tooling under a modern JDK and the legacy Gradle build under
   JDK 8 (see `DECISIONS.md` D010).

## Next Safe Action

**P1B — Gradle / Android Gradle Plugin migration** (do not begin without explicit
authorization).

## Resume Procedure

Every future engineering session must, before any work:

1. Read `AGENTS.md`.
2. Read `PROJECT_STATE.md`.
3. Read `SESSION_HANDOFF.md`.
4. Read `TASKS.md`.
5. Read `DECISIONS.md`.
6. Read `docs/PROOTX_2_ROADMAP.md`.
7. Run `git status`.
8. Run `git branch --show-current`.
9. Inspect recent history: `git log --oneline --decorate -10`.
10. Verify baseline/develop/main invariants (SHAs above; re-resolve with `git rev-parse`).

Then determine:

- last completed milestone,
- current milestone,
- current blocker (if any),
- next safe action.

Do **not** restart the project from the beginning.
Do **not** trust chat memory over repository state.
