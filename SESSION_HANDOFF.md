# ProotX Session Handoff

> Durable handoff for any future engineering session. Read this together with
> `PROJECT_STATE.md`, `TASKS.md`, `DECISIONS.md`, and `docs/PROOTX_2_ROADMAP.md`.
> Never rely on previous chat transcripts; the repository is the source of truth.

Last updated: 2026-09-16 (P1F2 support toolchain / provenance modernization: CLOSED / PASS — P1F IN PROGRESS; P1F3 READY TO START)

## Current Objective

Modernize ProotX toward the ProotX 2.0 architecture in controlled milestones, keeping the
application runtime/UI behavior invariant during toolchain work.

## Last Completed Milestone

**P1F2 — Support Toolchain / Provenance Modernization**: **CLOSED / PASS** at support implementation
SHA `a363e82b63ccc30ca678aba7dbb96c87d2879c08` on `ProotX-Assets-Support` branch
`feature/p1f-support-modernization`; support CI run `35057757988` — SUCCESS. The unreproducible
historical builder (`ubuntu:latest`, the now-unavailable `Lord1Egypt/proot@merge-it` fork, floating
`android-5`, blind `sed`) was removed and replaced by a pinned, deterministic builder. The support
runtime is now **dual-lane**: host API 21–28 keep the frozen legacy normal slots; host API 29+ use
the modern `.a10` slots rebuilt at **API 24 with NDK r29** from `termux/proot v5.1.107.92`
(`7266fb3e…`, archive `29385d1d…`), `termux-packages` `0ffca06c…`, builder
`ghcr.io/termux/package-builder@sha256:374fedda…`. The modern arm64-v8a/x86_64 slots are 16 KB
aligned (`0x4000`) and define `HAVE_PROCESS_VM`. Two independent clean builds produced byte-identical
modern binaries and byte-identical candidate archives (`SOURCE_DATE_EPOCH=1787437959`). ProotX
`minSdk` stays **21**; `ProotXFiles` selection is unchanged (`DECISIONS.md` D035). **No release was
published — ProotX still downloads `v1.0.0`.** **Whole-app 16 KB compatibility is still NOT
achieved** (4 KB 64-bit legacy normal-slot ELFs remain shipped via `jniLibs`/`nativeLibraryDir`;
`proot_meta`/`proot_meta_leveldb` remain unknown-provenance frozen inputs).

## Current Milestone

**P1F — Modern NDK / 16 KB Page-Size Compatibility**: **IN PROGRESS**. P1F-P (probe) is
CLOSED / PARTIAL_BRIDGE, P1F1 (in-tree) is CLOSED / PASS, and P1F2 (support toolchain/provenance)
is CLOSED / PASS. Next: **P1F3 — SUPPORT BUNDLE 16 KB REBUILD / PUBLICATION — READY TO START**
(P1G physical acceptance remains after P1F).

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
- P1E9 raised the app `targetSdk` **34 → 36** (after a clean target-35 checkpoint). `MainActivity`
  calls `enableEdgeToEdge()` and applies real `WindowInsetsCompat` per owner (toolbar top, bottom
  navigation bottom, root left/right cutout/navigation safety) from captured initial padding so it
  never accumulates, keeping light icons for the dark chrome. `TermuxActivity` registers a platform
  `OnBackInvokedCallback` on API 33+ (sharing `handleBackPressed()` with the retained legacy
  `onBackPressed`). Added the authorized `androidx.activity:activity-ktx:1.11.0` bridge (minSdk
  stays 21) with the Activity 1.11.0 non-null `onNewIntent` adjustment. Renamed/advanced
  `TargetSdk34CompatibilityGuardTest` → `TargetSdk36CompatibilityGuardTest` and added
  `TargetSdk36PlatformBehaviorGuardTest`.
- P1F1 raised the in-tree NDK pin **21.4.7075529 → 29.0.14206865** for `:app` and
  `:terminal-emulator` with no source/linker/packaging change, pinned `ndk;29.0.14206865` in CI,
  removed the deprecated CI-written `ndk.dir` (Gradle `ndkVersion` is authoritative), and added a
  scoped CI guard that fails if the packaged 64-bit `libtermux.so` `PT_LOAD` alignment is below
  `0x4000`. Added `NdkR29ToolchainGuardTest`.

## What Was Intentionally NOT Changed

- minSdk (21), `:terminal-view`/`:terminal-emulator` targetSdk 29, `:terminal-term` SDKs, native
  support payload (`ProotX-Assets-Support` v1.0.0 unchanged), `Android.mk`, `termux.c`, linker
  flags, ABI filters, `android:extractNativeLibs`, the `nativeLibraryDir`/`ProotXFiles` runtime
  contract, Room schema 1–7, migrations, core runtime data model, SSH semantics, notification
  policy, FGS types/IDs/actions, and the visual design.
- No edge-to-edge or predictive-back opt-out, no orientation lock, no aspect-ratio restriction, no
  large-screen/resizability opt-out, no terminal redesign, no main-navigation redesign, no
  Navigation/AppCompat/Material/Room/AndroidX-Test upgrade. Room stays on KAPT; Moshi stays on
  KSP2. `TermuxActivity` is not converted to AppCompat. No `-Wl,-z,max-page-size=16384` was added
  (r29 already produces the required 64-bit alignment). Full 16 KB compatibility is not claimed.

## Current Repository State

| Ref | SHA |
|---|---|
| Active branch | `feature/android-modernization` |
| Accepted P1F2 support implementation | `a363e82b63ccc30ca678aba7dbb96c87d2879c08` (support `feature/p1f-support-modernization`) |
| Accepted P1F1 implementation | `8103b835a670638c177a7adc6d7baea19680cd33` |
| Accepted P1E9 implementation | `41cc7a8c629da364903de0ae71ab524541c7ef76` |
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
tests **360 / 42 suites** (guard/contract tests).

## Current Toolchain

Gradle **8.11.1** · AGP **8.10.1** · Kotlin/KGP and stdlib **2.2.20** (jvmTarget 1.8) ·
Moshi **1.15.2** via **KSP2 2.2.20-2.0.4** · Room **2.1.0** via KAPT ·
Navigation **2.3.5** · Preference **1.1.0** · Material **1.1.0** ·
SwipeRefreshLayout **1.0.0** / LocalBroadcastManager **1.0.0** (direct) · Arch Core testing
**2.1.0** (test-only) · Activity **1.11.0** (P1E9 bridge; resolves core/core-ktx **1.13.0**,
lifecycle **2.6.2**, savedstate **1.2.1**, coroutines **1.7.3**) · JaCoCo **0.8.8** · Mockito
**4.11.0** (test-only) · gradle-download-task **5.0.0** · plugin
**`kotlin-parcelize`** · JDK **17** (build) · compileSdk **36** · targetSdk **36** ·
minSdk **21** · terminal-term **36/29/21** · terminal-view/emulator **29/29/21** · NDK
**29.0.14206865 (r29)** · build-tools **35.0.0**.
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

**P1E9 RESOLVED the platform-target deferrals:** the app targets 36, runs true edge-to-edge with
per-owner `WindowInsetsCompat`, and supports Android 15/16 predictive back (MainActivity via the
Activity 1.11.0 bridge; TermuxActivity via a platform `OnBackInvokedCallback`). **Deferred after
P1E9 (non-blocking):** the authorized Activity bridge moved core/core-ktx to 1.13.0, lifecycle to
2.6.2, savedstate to 1.2.1, and coroutines to 1.7.3, and `androidx.core` injects the benign
signature `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`; a future dependency-alignment milestone can
rationalize the newer family. Physical validation of insets/back/IME/VNC geometry is **P1G**; NDK /
16 KB work is **P1F**. Plus the still-open P1E8 items (terminal `specialUse` overlay consolidation,
`UnspecifiedRegisterReceiverFlag` suppression, androidx.test overlay).

**P1F-P / P1F1:** the probe (**CLOSED / PARTIAL_BRIDGE**) established that AGP 8.10.1 already emits
`PAGE_ALIGNMENT_16K` in the AAB, that NDK r29 fixes the in-tree `libtermux.so` with no source
change, and that the `ProotX-Assets-Support` bundle remains 4 KB-only for x86_64. P1F1
(**CLOSED / PASS**) applied the in-tree NDK r29 + CI pin modernization (`DECISIONS.md` D034).
**Deferred to P1F2/P1F3:** regenerate the complete support ELF alignment table from binaries
(P1F-P report erratum: a blanket "all arm32 0x1000" statement conflicted with `armeabi-v7a
busybox_static` being classified 16K-compatible), pin the support builder (base image digest,
exact termux-packages and PRoot commits), and resolve the **currently unavailable** PRoot source
`Lord1Egypt/proot@merge-it` (404). Google Play's applicable requirement: apps targeting **Android 15
/ API 35+** must support **16 KB page sizes on 64-bit devices**; current Android documentation gives
**February 1, 2027** as the update-enforcement date. **Full application 16 KB compatibility is not
claimed.**

**CI infrastructure finding — RESOLVED in CI-R1:** the closure-documentation push had failed
remote CI in `android-actions/setup-android@v3` (`Warning: Failed to find package 'tools'`)
before any ProotX build step (upstream SDK package retirement). Fixed with `packages: ''` plus
explicit `platform-tools`; remote run `34919847167` is green.

**Deferred physical checks:** the `EditTextPreference` numeric input, all Material widget
appearance/interaction (BottomNavigationView, TextInputLayout/EditText, FAB, dialogs), and the new
P1E9 edge-to-edge/inset/predictive-back/IME/VNC-geometry behavior must be verified on a device at
the P1G gate; no physical acceptance is claimed yet.

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
   D020, D021, D022, D031, D032, D033, D034, D035.

## Next Safe Action

**P1F3 — SUPPORT BUNDLE 16 KB REBUILD / PUBLICATION — READY TO START.** It owns publishing a new
support release from the reproducible P1F2 builder and resolving the 4 KB 64-bit legacy
normal-slot packaging debt. It must not begin without explicit authorization. **Full application
16 KB compatibility must not be claimed** until P1F3/P1F4 complete and **P1G** physical/16 KB-emulator
acceptance passes; ProotX must not be called a Golden Candidate, release candidate, or store-ready
final. Sentry and Billing remain active and out of scope.

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
