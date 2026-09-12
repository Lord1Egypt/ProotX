# ProotX Session Handoff

> Durable handoff for any future engineering session. Read this together with
> `PROJECT_STATE.md`, `TASKS.md`, `DECISIONS.md`, and `docs/PROOTX_2_ROADMAP.md`.
> Never rely on previous chat transcripts; the repository is the source of truth.

Last updated: 2026-09-12 (P1C2-R — Final Parcelize / Android Extensions migration: PASS)

## Current Objective

Modernize ProotX toward the ProotX 2.0 architecture in controlled milestones, keeping the
application runtime/UI behavior invariant during toolchain work.

## Last Completed Milestone

**P1C2-R — Final Android Extensions / Parcelize Migration**: **PASS**.
`kotlin-android-extensions` → `kotlin-parcelize`, `androidExtensions` removed, Parcelize
imports migrated to `kotlinx.parcelize`, legacy-extensions guard added. **P1C is CLOSED.**

## Current Milestone

**P1D — Dependency / AndroidX Modernization**: NOT STARTED.

## What Was Completed

- Replaced `apply plugin: 'kotlin-android-extensions'` with `kotlin-parcelize` and removed
  the obsolete `androidExtensions { experimental true }` block (`app/build.gradle`).
- Migrated all production Parcelize imports from `kotlinx.android.parcel.Parcelize` to
  `kotlinx.parcelize.Parcelize` (`App`, `Filesystem`, `Session`).
- Added `LegacyAndroidExtensionsGuardTest` (forbids `kotlinx.android.synthetic`,
  `kotlinx.android.parcel`, `kotlin-android-extensions`, `androidExtensions`; asserts
  `kotlin-parcelize` + `kotlinx.parcelize` are active).
- Added `ParcelableContractTest` (dependency-free check for `Parcelable` + static
  `CREATOR` on `App`, `Filesystem`, `Session`, and the `ServiceType` objects).
- Local + remote build and tests green (317 tests / 27 suites).

## What Was Intentionally NOT Changed

- Kotlin **1.4.32**, Moshi **1.9.3**, Gradle 6.7.1, AGP 4.2.2, JDK 8 build / JDK 17
  bootstrap, `compileSdk` 30, `targetSdk` 30, `minSdk` 21, NDK 21.4.7075529, build-tools
  30.0.2.
- Model semantics: `@Parcelize` annotations, `Parcelable` contracts, field order, Room
  annotations, sealed `ServiceType` hierarchy, `Session` semantics.
- Other dependency versions, package ID, `versionName`, `versionCode` algorithm.
- Runtime, PRoot, database, network, logging, UI, assets.

## Current Repository State

| Ref | SHA |
|---|---|
| Active branch | `feature/android-modernization` |
| Feature HEAD (P1C2-R) | `0c7a814cb097c41e004879d8352f8e3363f2937b` |
| `main` | `94abf5fa520255bb10d087a6be3ba2bc70b0e127` |
| `develop` | `94abf5fa520255bb10d087a6be3ba2bc70b0e127` |
| Baseline tag | `v1.0.0-baseline` → `94abf5fa520255bb10d087a6be3ba2bc70b0e127` |

## Accepted Baseline

Commit `94abf5fa520255bb10d087a6be3ba2bc70b0e127`, package
`io.github.lord1egypt.prootx`, version `1.0.0`. Baseline tests **313 / 24 suites**; current
tests **317 / 27 suites** (added source-guard/Parcelable contract tests).

## Current Toolchain

Gradle **6.7.1** · AGP **4.2.2** · Kotlin **1.4.32** · Moshi **1.9.3** · plugin
**`kotlin-parcelize`** · JDK **8** (build) · compileSdk **30** · targetSdk **30** · minSdk
**21** · NDK **21.4.7075529** · build-tools **30.0.2**. Repositories: `google()`,
`mavenCentral()` only.

## Known Deferred Findings

See [`docs/PROOTX_2_ROADMAP.md`](docs/PROOTX_2_ROADMAP.md#deferred-findings). Resolved: CI
SDK setup (P1A), `jcenter()` (P1B), synthetic views + Kotlin↔Moshi fork + legacy Android
Extensions/Parcelize (P1C). Still open: `com.schibsted.spain:barista:3.1.0` (androidTest,
JCenter-only); unused Sentry/Billing code (→ P1D); prebuilt rootfs profile remnant;
network-dependent unit tests; Play-readiness gaps (→ P1E); dynamic time-based `versionCode`.

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
9. CI runs the Android SDK tooling under a modern JDK and the application build under JDK 8
   (`DECISIONS.md` D010). Gradle 6.7.1 / AGP 4.2.2 is an intentional bridge (`D011`).
10. UI view access uses View Binding and Parcelize uses `kotlin-parcelize`; legacy Android
    Extensions are forbidden (`D012`, `D015`).
11. Kotlin and Moshi move together (`D013`); Moshi **1.9.3** is the Kotlin 1.3→1.4 bridge
    (`D014`).

## Next Safe Action

**P1D — Dependency / AndroidX modernization** (do not begin without explicit
authorization). Likely includes the `com.schibsted.spain:barista` androidTest replacement
and removal of unused Sentry/Billing code.

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
