# ProotX Session Handoff

> Durable handoff for any future engineering session. Read this together with
> `PROJECT_STATE.md`, `TASKS.md`, `DECISIONS.md`, and `docs/PROOTX_2_ROADMAP.md`.
> Never rely on previous chat transcripts; the repository is the source of truth.

Last updated: 2026-09-12 (P0.5 — Project Control Plane)

## Current Objective

Maintain a reproducible, documented ProotX baseline and a persistent engineering control
plane so that ProotX 2.0 can proceed safely across many separate sessions.

## Last Completed Milestone

**P0 — Baseline Freeze & Development Safety**: CLOSED / PASS.
Baseline verified, build verified (313 tests), annotated tag created, `develop` and
`feature/android-modernization` created, roadmap written.

## Current Milestone

**P0.5 — Project Control Plane** (documentation/state-management only).

## What Was Completed

- Frozen and verified the ProotX 1.0.0 baseline at `94abf5f`.
- Created annotated tag `v1.0.0-baseline` (object `edbabdf`).
- Created `develop` at the baseline and `feature/android-modernization` from it.
- Authored `docs/PROOTX_2_ROADMAP.md` (architecture direction + deferred findings).
- Established the persistent control plane: `PROJECT_STATE.md`, `SESSION_HANDOFF.md`,
  `TASKS.md`, `DECISIONS.md`, `CHANGELOG_DEV.md`, `UPSTREAM_BASELINE.md`,
  `ASSET_TRACKING.md`, `AGENTS.md`.

## What Was Intentionally NOT Changed

- No Android/terminal source, Gradle config, dependencies, manifest, resources, UI.
- No runtime, PRoot, database, logging, network, package, or version behavior.
- No CI implementation change (baseline CI failure remains documented, not fixed).
- No asset repositories modified; no rootfs/distro rebuild.
- No toolchain upgrades; no dependency upgrades.

## Current Repository State

| Ref | SHA |
|---|---|
| Active branch | `feature/android-modernization` |
| `main` | `94abf5fa520255bb10d087a6be3ba2bc70b0e127` |
| `develop` | `94abf5fa520255bb10d087a6be3ba2bc70b0e127` |
| Baseline tag | `v1.0.0-baseline` → `94abf5fa520255bb10d087a6be3ba2bc70b0e127` |

## Accepted Baseline

Commit `94abf5fa520255bb10d087a6be3ba2bc70b0e127`, package
`io.github.lord1egypt.prootx`, version `1.0.0`, **313 tests / 24 suites / 0 failures**,
JDK 8 / Gradle 5.1.1 / AGP 3.4.3 / Kotlin 1.3.61 / compileSdk 30 / NDK 21.4.7075529.

## Known Deferred Findings

See [`docs/PROOTX_2_ROADMAP.md`](docs/PROOTX_2_ROADMAP.md#deferred-findings). Summary:
failing CI Android SDK setup; legacy toolchain; Kotlin Android synthetics blocking
Kotlin upgrade; unused Sentry + Play Billing code and permission; prebuilt rootfs
containing old `/etc/profile.d/prootx.sh`; `jcenter()` fallback; network-dependent unit
tests; Play-readiness gaps (`targetSdk` 30, missing `android:exported`); and the dynamic
time-based `versionCode` release-contract gap.

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

## Next Safe Action

**P1A — Build-system / JDK / CI foundation** (do not begin without explicit authorization).

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
