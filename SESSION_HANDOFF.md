# ProotX Session Handoff

> Durable handoff for any future engineering session. Read this together with
> `PROJECT_STATE.md`, `TASKS.md`, `DECISIONS.md`, and `docs/PROOTX_2_ROADMAP.md`.
> Never rely on previous chat transcripts; the repository is the source of truth.

Last updated: 2026-09-12 (P1C1 — Synthetic Views → View Binding)

## Current Objective

Modernize ProotX toward the ProotX 2.0 architecture in controlled milestones, keeping the
application runtime/UI behavior invariant during toolchain work.

## Last Completed Milestone

**P1C1 — Synthetic Views → View Binding**: **PASS**.
All `kotlinx.android.synthetic` view access replaced with View Binding; source guard test
added; legacy Parcelize and `kotlin-android-extensions` intentionally retained. Local and
remote build + tests green (314 tests / 25 suites).

## Current Milestone

**P1C2 — Kotlin modernization + legacy Parcelize migration + final
`kotlin-android-extensions` removal**: NOT STARTED.

## What Was Completed

- Enabled View Binding in the `app` module (`buildFeatures.viewBinding true`).
- Migrated `MainActivity` to `ActivityMainBinding`.
- Migrated seven Fragments to generated binding classes with the lifecycle-safe
  `_binding`/`onDestroyView` pattern:
  `HelpFragment`, `AppDetailsFragment`, `AppsListFragment`, `FilesystemListFragment`,
  `SessionListFragment`, `SessionEditFragment`, `FilesystemEditFragment`.
- Scoped `AppDetailsFragment`'s LiveData observer to `viewLifecycleOwner` so binding is
  never touched after the view is destroyed.
- Added `SyntheticViewImportsTest` to fail the build if synthetic view imports return
  (legacy Parcelize explicitly permitted).
- No XML/layout changes were required.

## What Was Intentionally NOT Changed

- `kotlin-android-extensions` plugin and `androidExtensions { experimental true }`
  (still required for legacy Parcelize in P1C1).
- `kotlinx.android.parcel.Parcelize` usage in `App`, `Filesystem`, `Session`.
- Kotlin **1.3.61**; Gradle 6.7.1; AGP 4.2.2; `compileSdk` 30; `targetSdk` 30; `minSdk` 21;
  NDK 21.4.7075529; build-tools 30.0.2.
- App dependency versions, package ID, `versionName`, `versionCode` algorithm.
- Runtime, PRoot, database, network, logging, billing, Sentry, assets.

## Current Repository State

| Ref | SHA |
|---|---|
| Active branch | `feature/android-modernization` |
| Feature HEAD (P1C1) | `fbf6f9d6dfd1b653886cdcd81a87ef4b0325a54f` |
| `main` | `94abf5fa520255bb10d087a6be3ba2bc70b0e127` |
| `develop` | `94abf5fa520255bb10d087a6be3ba2bc70b0e127` |
| Baseline tag | `v1.0.0-baseline` → `94abf5fa520255bb10d087a6be3ba2bc70b0e127` |

## Accepted Baseline

Commit `94abf5fa520255bb10d087a6be3ba2bc70b0e127`, package
`io.github.lord1egypt.prootx`, version `1.0.0`. Baseline tests **313 / 24 suites**; current
tests **314 / 25 suites** (one added source-guard test).

## Current Toolchain

Gradle **6.7.1** · AGP **4.2.2** · Kotlin **1.3.61** · JDK **8** (build) · compileSdk **30** ·
targetSdk **30** · minSdk **21** · NDK **21.4.7075529** · build-tools **30.0.2**.
Repositories: `google()`, `mavenCentral()` only.

## Known Deferred Findings

See [`docs/PROOTX_2_ROADMAP.md`](docs/PROOTX_2_ROADMAP.md#deferred-findings). Resolved: CI
SDK setup (P1A), `jcenter()` (P1B), synthetic views (P1C1). Remaining Kotlin legacy (P1C2):
`kotlin-android-extensions`, `androidExtensions`, `kotlinx.android.parcel.Parcelize`.
Still open: `com.schibsted.spain:barista:3.1.0` (androidTest, JCenter-only); unused
Sentry/Billing code (→ P1D); prebuilt rootfs profile remnant; network-dependent unit tests;
Play-readiness gaps (→ P1E); dynamic time-based `versionCode`.

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
10. UI view access uses View Binding; synthetic views are forbidden (`D012`). Legacy
    Parcelize remains until P1C2.

## Next Safe Action

**P1C2 — Kotlin modernization + legacy Parcelize migration + final
`kotlin-android-extensions` removal** (do not begin without explicit authorization).

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
