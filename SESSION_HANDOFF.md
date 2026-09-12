# ProotX Session Handoff

> Durable handoff for any future engineering session. Read this together with
> `PROJECT_STATE.md`, `TASKS.md`, `DECISIONS.md`, and `docs/PROOTX_2_ROADMAP.md`.
> Never rely on previous chat transcripts; the repository is the source of truth.

Last updated: 2026-09-12 (P1C2-P — Moshi 1.9.3 / Kotlin 1.4 bridge probe: BRIDGE_FOUND)

## Current Objective

Modernize ProotX toward the ProotX 2.0 architecture in controlled milestones, keeping the
application runtime/UI behavior invariant during toolchain work.

## Last Completed Milestone

**P1C2-P — Moshi 1.9.3 / Kotlin 1.4 Bridge Probe**: **BRIDGE_FOUND**.
The previously untested cell **Kotlin 1.4.32 + Moshi 1.9.3** passes kapt and a full clean
build + unit tests (314 / 25). Moshi 1.9.3 is a valid bridge across Kotlin 1.3 → 1.4.

## Current Milestone

**P1C2 — Kotlin + Legacy Parcelize + Plugin Removal**: **READY TO RETRY** on the new bridge
baseline. Kotlin is already 1.4.32 and Moshi already 1.9.3; the remaining work is the
Parcelize/plugin swap. Not started.

## What Was Completed

- Probed the missing compatibility cell: Kotlin 1.4.32 + Moshi 1.9.3 → `:app:kaptDebugKotlin`
  **PASS**.
- Full `clean assembleDebug testDebugUnitTest` **PASS** (314 tests / 25 suites) with the
  legacy `kotlin-android-extensions` plugin, `androidExtensions` block, and
  `kotlinx.android.parcel.Parcelize` all unchanged.
- Committed the verified bridge: Kotlin **1.3.61 → 1.4.32** and Moshi **1.8.0 → 1.9.3**
  (single atomic commit per D013).
- Remote CI green (run `34679658479`, commit `4f61a47`).

## What Was Intentionally NOT Changed

- `kotlin-android-extensions` plugin, `androidExtensions { experimental true }`,
  `kotlinx.android.parcel.Parcelize` (all preserved for the P1C2 retry).
- Gradle 6.7.1; AGP 4.2.2; JDK 8 build / JDK 17 bootstrap; `compileSdk` 30; `targetSdk` 30;
  `minSdk` 21; NDK 21.4.7075529; build-tools 30.0.2.
- Other app dependency versions (AndroidX, Room, Coroutines, OkHttp, Sentry, Billing, …),
  package ID, `versionName`, `versionCode` algorithm.
- Runtime, PRoot, database, network, logging, UI, assets.

## Current Repository State

| Ref | SHA |
|---|---|
| Active branch | `feature/android-modernization` |
| Feature HEAD (P1C2-P bridge) | `4f61a4710bf9092255028d9c744c0641b151a673` |
| `main` | `94abf5fa520255bb10d087a6be3ba2bc70b0e127` |
| `develop` | `94abf5fa520255bb10d087a6be3ba2bc70b0e127` |
| Baseline tag | `v1.0.0-baseline` → `94abf5fa520255bb10d087a6be3ba2bc70b0e127` |

## Accepted Baseline

Commit `94abf5fa520255bb10d087a6be3ba2bc70b0e127`, package
`io.github.lord1egypt.prootx`, version `1.0.0`. Baseline tests **313 / 24 suites**; current
tests **314 / 25 suites** (one added source-guard test).

## Current Toolchain

Gradle **6.7.1** · AGP **4.2.2** · Kotlin **1.4.32** · Moshi **1.9.3** · JDK **8** (build) ·
compileSdk **30** · targetSdk **30** · minSdk **21** · NDK **21.4.7075529** · build-tools
**30.0.2**. Repositories: `google()`, `mavenCentral()` only.

## Known Deferred Findings

See [`docs/PROOTX_2_ROADMAP.md`](docs/PROOTX_2_ROADMAP.md#deferred-findings). Resolved: CI
SDK setup (P1A), `jcenter()` (P1B), synthetic views (P1C1), Kotlin↔Moshi version fork
(P1C2-P). Remaining Kotlin legacy (→ P1C2 retry): `kotlin-android-extensions`,
`androidExtensions`, `kotlinx.android.parcel.Parcelize`. Still open:
`com.schibsted.spain:barista:3.1.0` (androidTest, JCenter-only); unused Sentry/Billing code
(→ P1D); prebuilt rootfs profile remnant; network-dependent unit tests; Play-readiness gaps
(→ P1E); dynamic time-based `versionCode`.

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
    Parcelize remains until the P1C2 retry.
11. Kotlin and Moshi move together (`D013`); Moshi **1.9.3** is the Kotlin 1.3→1.4 bridge
    (`D014`).

## Next Safe Action

**P1C2 retry (requires authorization)** — now a small hop on the bridge baseline: swap
`kotlin-android-extensions` → `kotlin-parcelize`, remove `androidExtensions`, migrate
`kotlinx.android.parcel` → `kotlinx.parcelize`, add the legacy-extensions guard, and
re-validate. Kotlin 1.4.32 and Moshi 1.9.3 are already in place.

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
