# ProotX Session Handoff

> Durable handoff for any future engineering session. Read this together with
> `PROJECT_STATE.md`, `TASKS.md`, `DECISIONS.md`, and `docs/PROOTX_2_ROADMAP.md`.
> Never rely on previous chat transcripts; the repository is the source of truth.

Last updated: 2026-09-16 (P1E6 storage / permission runtime compatibility: CLOSED / PASS — P1E IN PROGRESS)

## Current Objective

Modernize ProotX toward the ProotX 2.0 architecture in controlled milestones, keeping the
application runtime/UI behavior invariant during toolchain work.

## Last Completed Milestone

**P1E6 — Storage / Permission Runtime Compatibility**: **CLOSED / PASS** at implementation SHA
`2202d6bda33512d3312827bf2bd6dc17f47dbae9`. The obsolete legacy broad-storage dependency was
removed: `READ_EXTERNAL_STORAGE`/`WRITE_EXTERNAL_STORAGE` are gone from the app manifest,
`WRITE_EXTERNAL_STORAGE` is gone from the terminal-term manifest, `PermissionHandler.kt` is
deleted, and app/session launch plus SAF import/export no longer request storage permissions.
No replacement broad permission (`MANAGE_EXTERNAL_STORAGE`, `READ_MEDIA_*`) was added. targetSdk
remains **30**; terminal modules remain **29/29/21**; app-scoped storage paths, the runtime data
model, and UI are unchanged. A disposable targetSdk 33 probe passed with no storage permissions in
the merged manifest/APK, then reverted. Local canonical evidence **38 suites / 329 tests /
0 failures / 0 errors / 0 skipped**; remote CI run `35028617203` passed the canonical clean build,
androidTest build, and both artifact uploads.

## Current Milestone

**P1E — SDK 36 / Manifest Compatibility**: IN PROGRESS. P1E6 is closed; **P1E7 — FOREGROUND
SERVICE / NOTIFICATION COMPATIBILITY** is **NOT STARTED / READY TO START**.

## What Was Completed

- Persisted Gradle 8.11.1, AGP 8.10.1, Kotlin/stdlib 2.2.20, KSP 2.2.20-2.0.4, four module
  namespaces, app-only BuildConfig, and CI Build Tools 35.0.0 ownership.
- Kept Moshi 1.15.2 on KSP2, Room 2.1.0 on KAPT, Navigation/Safe Args 2.3.5, JaCoCo 0.8.8,
  gradle-download-task 5.0.0, JDK 17, and NDK 21.4.7075529.
- Applied exactly five behavior-neutral `lowercase(Locale.ENGLISH)` substitutions and the
  androidTest `com.termux.R.id.terminal_view` ownership qualification.
- P1E3-R1 corrected both JaCoCo report tasks from the legacy unit-test `.exec` path to AGP 8's
  `outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec` path.
- P1E4 raised only `:app` compileSdk 30 → 36 and CI platform 30 → 36; all dependency and
  toolchain pins remain unchanged. The only source compatibility edit is
  `return info.versionName!!`.
- P1E5 added `android:exported="true"` to `MainActivity` (launcher) and `TermuxActivity`
  (`ssh://` BROWSABLE entry point preserved) and made all six application-owned `PendingIntent`s
  explicitly immutable. It also probed targetSdk 31 and reverted. No dependency, Gradle, Room,
  schema, runtime, UI, or terminal-SDK change.
- P1E5 receiver classification: `MainActivity`'s `LocalBroadcastManager` registration is
  in-process (unaffected); its `DownloadManager` `ACTION_DOWNLOAD_COMPLETE` registration listens
  to a system broadcast and is left unchanged; the `TermuxActivity` custom
  `com.termux.app.reload_style` receiver is classified for future work as
  `RECEIVER_NOT_EXPORTED` but the flag is deferred until `:terminal-term` compiles against an
  API that offers it.
- P1E6 removed the obsolete legacy broad-storage runtime dependency: the app manifest's
  `READ_EXTERNAL_STORAGE`/`WRITE_EXTERNAL_STORAGE`, the terminal-term `WRITE_EXTERNAL_STORAGE`,
  the `PermissionHandler.kt` gate/dialog class, the app/session launch permission gates, the
  SAF import (FilesystemEditFragment) and export (FilesystemListFragment) gates, and the
  `TermuxActivity` obsolete storage helper/constant/imports. No replacement broad permission was
  added. App-scoped storage paths (`filesDir`, `getExternalFilesDir(s)`, `emulatedScopedDir`,
  the `/storage/internal` and optional `/storage/sdcard` bindings, and the `AssetDownloader`
  download destination) are unchanged.

## What Was Intentionally NOT Changed

- targetSdk/minSdk (30/21), terminal SDKs (29/29/21), NDK 21.4, resources, dependency versions,
  Room schema 1–7, migrations, core runtime data model, and UI.
- No FGS type/permission, notification-permission, receiver-export flag, targetSdk raise,
  edge-to-edge, or predictive-back work (those are later P1E milestones). Room stays on KAPT;
  Moshi stays on KSP2. P1E6's permission-gate removal is the only intentional runtime behavior
  change so far; the unreachable legacy permission-continuation ViewModel code is deferred, not
  deleted.

## Current Repository State

| Ref | SHA |
|---|---|
| Active branch | `feature/android-modernization` |
| Accepted P1E6 implementation | `2202d6bda33512d3312827bf2bd6dc17f47dbae9` |
| Accepted P1E5 implementation | `6e8a0559bc691266d403a216c56ec4377ce0c98b` |
| Accepted P1E4 implementation | `6d30b333b0a1d0b8ab0be966af4c3052dcf29500` |
| Accepted P1E3 implementation | `1828cdd4441a291433d07bd8a3e4efa96bcbfc76` |
| `main` | `94abf5fa520255bb10d087a6be3ba2bc70b0e127` |
| `develop` | `94abf5fa520255bb10d087a6be3ba2bc70b0e127` |
| Baseline tag | `v1.0.0-baseline` → `94abf5fa520255bb10d087a6be3ba2bc70b0e127` |

## Accepted Baseline

Commit `94abf5fa520255bb10d087a6be3ba2bc70b0e127`, package
`io.github.lord1egypt.prootx`, version `1.0.0`. Baseline tests **313 / 24 suites**; current
tests **329 / 38 suites** (guard/contract tests).

## Current Toolchain

Gradle **8.11.1** · AGP **8.10.1** · Kotlin/KGP and stdlib **2.2.20** (jvmTarget 1.8) ·
Moshi **1.15.2** via **KSP2 2.2.20-2.0.4** · Room **2.1.0** via KAPT ·
Navigation **2.3.5** · Preference **1.1.0** · Material **1.1.0** ·
SwipeRefreshLayout **1.0.0** / LocalBroadcastManager **1.0.0** (direct) · Arch Core testing
**2.1.0** (test-only) · Core KTX **1.1.0** (transitive `core` 1.3.0) · Lifecycle **2.2.0** ·
JaCoCo **0.8.8** · Mockito **4.11.0** (test-only) · gradle-download-task **5.0.0** · plugin
**`kotlin-parcelize`** · JDK **17** (build) · compileSdk **36** · targetSdk **30** ·
minSdk **21** · terminal SDK **29/29/21** · NDK **21.4.7075529** · build-tools **35.0.0**.
Repositories: `google()`,
`mavenCentral()`.

## Known Deferred Findings

See [`docs/PROOTX_2_ROADMAP.md`](docs/PROOTX_2_ROADMAP.md#deferred-findings). Resolved: CI
SDK setup (P1A), `jcenter()` (P1B), Kotlin/Android Extensions (P1C), Barista (P1D1), dead
Play Services (P1D2), Lifecycle extensions (P1D3), Navigation pre-release (P1D4), Room
pre-release (P1D5), Preference pre-release (P1D6), Material pre-release (P1D7), Arch Core
testing pre-release (P1D8), Core KTX misalignment (P1D9). **Sentry and Billing are ACTIVE
production dependencies, not unused; both were left untouched.** Newly classified in the P1D
closure audit (deferred, **non-blocking**): Coroutines (`1.0.0` declaration superseded at
resolution), Sentry `1.7.22`, Billing `3.0.3`, OkHttp `3.14.7` + Okio `3.7.0` runtime
validation, Gson `2.8.6`,
JArchiveLib `0.8.0`, ConstraintLayout `1.1.3`, transitive AppCompat/Fragment/RecyclerView
ownership, LocalBroadcastManager, and the JUnit4/Mockito/AndroidX-Test stack. Still open:
prebuilt rootfs profile remnant; network-dependent unit tests; Play-readiness gaps (→ P1E);
dynamic time-based `versionCode`; `LocalBroadcastManager` modernization (deprecated tech,
deferred).

**P1E3 deferred build-tooling cleanup:** manifest `package` warnings; `JavaExec.main` →
`mainClass` before Gradle 9; legacy Android DSL and `lintOptions`; `String.capitalize()`;
configuration-time custom tasks; Node/action maintenance warnings; `ndk.dir`; eventual Room
KAPT migration; and the core/core-ktx family note.

**CI infrastructure finding — RESOLVED in CI-R1:** the closure-documentation push had failed
remote CI in `android-actions/setup-android@v3` (`Warning: Failed to find package 'tools'`)
before any ProotX build step (upstream SDK package retirement). Fixed with `packages: ''` plus
explicit `platform-tools`; remote run `34919847167` is green.

**Deferred physical checks:** the `EditTextPreference` numeric input and all Material widget
appearance/interaction (BottomNavigationView, TextInputLayout/EditText, FAB, dialogs) must be
verified on a device at the Golden Candidate gate; no physical acceptance is claimed yet.

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
9. Established durable decisions: D010, D011, D012/D015, D013/D014, D016, D017, D018, D019,
   D020, D021, D022.

## Next Safe Action

**P1E7 — FOREGROUND SERVICE / NOTIFICATION COMPATIBILITY — NOT STARTED / READY TO START.** It owns
`foregroundServiceType`/`FOREGROUND_SERVICE_SPECIAL_USE`, `startForegroundService`, and
notification-permission compatibility. Sentry and Billing remain active and out of scope. Do not
begin without explicit authorization.

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
