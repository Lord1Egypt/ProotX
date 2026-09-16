# ProotX Session Handoff

> Durable handoff for any future engineering session. Read this together with
> `PROJECT_STATE.md`, `TASKS.md`, `DECISIONS.md`, and `docs/PROOTX_2_ROADMAP.md`.
> Never rely on previous chat transcripts; the repository is the source of truth.

Last updated: 2026-09-16 (P1E8 targetSdk 34 runtime compatibility: CLOSED / PASS — P1E IN PROGRESS)

## Current Objective

Modernize ProotX toward the ProotX 2.0 architecture in controlled milestones, keeping the
application runtime/UI behavior invariant during toolchain work.

## Last Completed Milestone

**P1E8 — targetSdk 33/34 Runtime Compatibility**: **CLOSED / PASS** at implementation SHA
`cff25f3f1dffa1a91d78a55915dd50513b694af4`. The app's persistent `targetSdk` is **34**
(`compileSdk 36`, `minSdk 21`). `POST_NOTIFICATIONS` is declared and requested contextually at the
first real session start through one shared application-private prefs flag
(`notification_permission`/`prompt_completed`); current grant state always comes from
`checkSelfPermission`, denial never blocks the Linux session, and an explicit denial suppresses
later automatic prompts. The initial `ServerService` launch is deferred until the activity is
resumed and catches only `ForegroundServiceStartNotAllowedException`, restoring the pending session
and retrying on the next resume. `TermuxActivity` (direct `ssh://` entry) uses the same one-time
policy, and its app-internal `reload_style` receiver uses the direct
`Context.RECEIVER_NOT_EXPORTED` constant on API 33+ with the legacy two-argument path retained.
`:terminal-term` raises **only** its `compileSdk` to **36** (`targetSdk 29`/`minSdk 21`);
`:terminal-view`/`:terminal-emulator` stay **29/29/21**. A test-only
`app/src/androidTest/AndroidManifest.xml` supplies the `android:exported` values that
`androidx.test:core:1.2.0` omits (required by the target-31+ merger). Notification channel
ID/IDs/actions, FGS types, PendingIntents, SSH, session lifecycle, Room schema, and UI are
unchanged. Local canonical evidence **40 suites / 348 tests / 0 failures / 0 errors / 0 skipped**;
remote CI run `35037714144` passed the canonical clean build, androidTest build, and both artifact
uploads.

## Current Milestone

**P1E — SDK 36 / Manifest Compatibility**: IN PROGRESS. P1E8 is closed; **P1E9 — TARGETSDK 35/36
PLATFORM BEHAVIOR** is **NOT STARTED / READY TO START**.

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
- P1E7 declared `android.permission.FOREGROUND_SERVICE_SPECIAL_USE` (retaining
  `FOREGROUND_SERVICE`) and typed both real foreground services as
  `android:foregroundServiceType="specialUse"` with a `PROPERTY_SPECIAL_USE_FGS_SUBTYPE`
  property: `ServerService` directly in the app manifest and `TermuxService` via an API-36 app
  manifest overlay (the terminal module stays compileSdk 29), which merges into the single
  existing library component (`exported=false`). It made each service own its `"ProotX"`
  notification channel (`IMPORTANCE_LOW`), removed `MainActivity`'s now-redundant channel
  initialization, switched only the initial session/terminal launch paths to
  `startForegroundService` on API 26+, promoted `ServerService` to the foreground synchronously
  before asynchronous session work, and used `ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST` on
  API 29+ (two-argument `startForeground` below). `POST_NOTIFICATIONS` was intentionally deferred
  to P1E8. Added `ForegroundServiceCompatibilityGuardTest`.
- P1E8 raised the app `targetSdk` **30 → 34** and declared `POST_NOTIFICATIONS`. `MainActivity`
  requests it contextually at the final session-start boundary, records the one-time prompt in a
  shared `notification_permission`/`prompt_completed` prefs flag, checks current grant state with
  `checkSelfPermission`, defers the pending session until the activity is resumed, and catches only
  `ForegroundServiceStartNotAllowedException` around the launch (retrying on the next resume).
  `TermuxActivity` implements the same one-time policy for the direct `ssh://` entry before it
  starts/binds `TermuxService` exactly once, and registers its app-internal `reload_style` receiver
  with the direct `Context.RECEIVER_NOT_EXPORTED` constant on API 33+. `:terminal-term` raised only
  its `compileSdk` 29 → 36. Added a test-only `app/src/androidTest/AndroidManifest.xml` for the
  `androidx.test:core:1.2.0` exported values and `TargetSdk34CompatibilityGuardTest`; updated
  `ForegroundServiceCompatibilityGuardTest` in place.

## What Was Intentionally NOT Changed

- minSdk (21), `:terminal-view`/`:terminal-emulator` SDKs (29/29/21), NDK 21.4, resources,
  dependency versions, Room schema 1–7, migrations, core runtime data model, and UI.
- No targetSdk 35/36 raise, no terminal targetSdk raise, no receiver magic flag / reflection, no
  exported `reload_style` receiver, no flags on the `DownloadManager` system receiver, no storage
  permission restoration, no session blocking on notification denial, no startup notification
  request, no custom rationale UI, no edge-to-edge or predictive-back work (P1E9). Room stays on
  KAPT; Moshi stays on KSP2. The unreachable legacy permission-continuation ViewModel code is
  deferred, not deleted.

## Current Repository State

| Ref | SHA |
|---|---|
| Active branch | `feature/android-modernization` |
| Accepted P1E8 implementation | `cff25f3f1dffa1a91d78a55915dd50513b694af4` |
| Accepted P1E7 implementation | `0e70b7e1e3f398eb6fdb92730542cae0da6f1975` |
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
tests **348 / 40 suites** (guard/contract tests).

## Current Toolchain

Gradle **8.11.1** · AGP **8.10.1** · Kotlin/KGP and stdlib **2.2.20** (jvmTarget 1.8) ·
Moshi **1.15.2** via **KSP2 2.2.20-2.0.4** · Room **2.1.0** via KAPT ·
Navigation **2.3.5** · Preference **1.1.0** · Material **1.1.0** ·
SwipeRefreshLayout **1.0.0** / LocalBroadcastManager **1.0.0** (direct) · Arch Core testing
**2.1.0** (test-only) · Core KTX **1.1.0** (transitive `core` 1.3.0) · Lifecycle **2.2.0** ·
JaCoCo **0.8.8** · Mockito **4.11.0** (test-only) · gradle-download-task **5.0.0** · plugin
**`kotlin-parcelize`** · JDK **17** (build) · compileSdk **36** · targetSdk **34** ·
minSdk **21** · terminal-term **36/29/21** · terminal-view/emulator **29/29/21** · NDK
**21.4.7075529** · build-tools **35.0.0**.
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

**P1E8 RESOLVED the P1E5/P1E7 receiver and notification deferrals:** the terminal
`com.termux.app.reload_style` receiver now uses `Context.RECEIVER_NOT_EXPORTED` on API 33+ (legacy
path retained), `POST_NOTIFICATIONS` is declared and requested contextually at first session start,
and the target-31+ FGS launch is deferred until the activity is resumed with narrow
`ForegroundServiceStartNotAllowedException` handling. **Deferred after P1E8 (non-blocking):** the
`specialUse` type for `TermuxService` still lives in the app manifest overlay (now that
`:terminal-term` compiles at API 36 it may be consolidated later); the system `DownloadManager`
receiver's `UnspecifiedRegisterReceiverFlag` lint false positive is intentionally suppressed (the
flag-less registration is correct for a system-only broadcast); and the test-only
`androidx.test:core:1.2.0` exported-value overlay should be revisited when `androidx.test` is
eventually upgraded.

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
   D020, D021, D022, D031, D032.

## Next Safe Action

**P1E9 — TARGETSDK 35/36 PLATFORM BEHAVIOR — NOT STARTED / READY TO START.** It owns the targetSdk
35/36 raise, edge-to-edge, predictive back, large-screen orientation/resizability behavior, and the
final SDK-36 behavior regression. NDK / 16 KB work remains **P1F**; physical acceptance remains
**P1G**. Sentry and Billing remain active and out of scope. Do not begin without explicit
authorization.

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
