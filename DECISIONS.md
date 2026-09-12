# ProotX Engineering Decisions

> Durable decision log. Each entry records *why* a choice was made so future sessions do
> not relitigate it. This file is authoritative for architecture changes; `TASKS.md`
> tracks progress.

---

## D001 — ProotX 1.0.0 baseline is frozen and immutable

- **Date:** 2026-09-12
- **Status:** Accepted
- **Decision:** The ProotX 1.0.0 baseline at commit
  `94abf5fa520255bb10d087a6be3ba2bc70b0e127` (annotated tag `v1.0.0-baseline`) is frozen
  and must not be altered or rewritten.
- **Reason:** Modernization needs a reproducible recovery point and a stable reference for
  regression comparison.
- **Alternatives considered:** Continue editing on `main`; move the baseline forward as
  work lands.
- **Trade-offs:** Requires explicit branch/tag discipline, but guarantees recoverability.
- **Affected components:** Repository history, tags, release process.

---

## D002 — `main` is stable; development flows through `develop` and feature branches

- **Date:** 2026-09-12
- **Status:** Accepted
- **Decision:** `main` stays stable and does not receive direct development commits.
  Active work happens on `develop` and short-lived `feature/*` branches.
- **Reason:** Keeps a trustworthy stable ref while allowing parallel, reviewable work.
- **Alternatives considered:** Trunk-based development directly on `main`.
- **Trade-offs:** Slightly more branch bookkeeping; lower risk of destabilizing `main`.
- **Affected components:** Branching model, CI triggers, release process.

---

## D003 — Large changes are divided into explicit milestones with STOP gates

- **Date:** 2026-09-12
- **Status:** Accepted
- **Decision:** ProotX 2.0 is executed as named milestones (P0, P0.5, P1x, …). Each
  milestone ends at an explicit STOP gate; no milestone begins without authorization.
- **Reason:** Limits blast radius, keeps scope honest, and lets a fresh session resume
  safely without guessing.
- **Alternatives considered:** A single continuous modernization effort.
- **Trade-offs:** More coordination overhead; far better reviewability and safety.
- **Affected components:** Entire program, control-plane documents.

---

## D004 — Physical acceptance and source/test acceptance are different things

- **Date:** 2026-09-12
- **Status:** Accepted
- **Decision:** Passing build/unit tests ("source/test acceptance") does not by itself mean
  a change is accepted for release; a change may require physical-device acceptance as a
  separate step.
- **Reason:** This app manipulates PTYs, PRoot, filesystems and networking, which unit
  tests cannot fully validate on-device.
- **Alternatives considered:** Treating a green CI/unit run as sufficient acceptance.
- **Trade-offs:** Longer acceptance cycles; avoids shipping device-only regressions.
- **Affected components:** CI, release process, P1G regression acceptance.

---

## D005 — Future versions distinguish candidate builds from last physically accepted baseline

- **Date:** 2026-09-12
- **Status:** Accepted (direction)
- **Decision:** Future releases should clearly separate a *candidate* build from the *last
  physically accepted* baseline, rather than a single continuously moving version.
- **Reason:** Provides a stable "known good" reference that device testing can bless
  independently of whatever is being built.
- **Alternatives considered:** A single always-incrementing version (current behavior).
- **Trade-offs:** Extra version bookkeeping; clearer acceptance provenance.
- **Affected components:** Gradle versioning, release contract, acceptance workflow.

---

## D006 — Runtime/UI behavior is invariant during toolchain-only modernization

- **Date:** 2026-09-12
- **Status:** Accepted
- **Decision:** During toolchain-only modernization, runtime and UI behavior must remain
  invariant unless a specific milestone explicitly authorizes a behavior change.
- **Reason:** Isolates "does it still build/run after upgrades" from "did we change
  behavior", making regressions attributable.
- **Alternatives considered:** Refactoring behavior alongside toolchain upgrades.
- **Trade-offs:** Some cleanup is deferred; much easier regression analysis.
- **Affected components:** P1 modernization increments, runtime, UI.

---

## D007 — ProotX moves toward a generic, data-driven PRoot runtime

- **Date:** 2026-09-12
- **Status:** Accepted (direction)
- **Decision:** ProotX 2.0 will move toward a generic PRoot runtime driven by catalog/image
  data instead of distro-specific Android-side logic.
- **Reason:** Reduces per-distro code and enables a dynamic catalog and OCI/rootfs support.
- **Alternatives considered:** Continuing per-distro code paths.
- **Trade-offs:** Requires Runtime V2 design and catalog work; more scalable and simpler
  long-term.
- **Affected components:** Runtime, catalog, asset delivery.

---

## D008 — Multi-instance simultaneous runtime is a core ProotX 2.0 requirement

- **Date:** 2026-09-12
- **Status:** Accepted (requirement)
- **Decision:** Supporting multiple Linux instances running simultaneously is a core
  product requirement of ProotX 2.0 (not an optional enhancement).
- **Reason:** Power users expect concurrent environments; it shapes runtime, process/PTY,
  filesystem and port management design.
- **Alternatives considered:** Keeping the single-active-session model.
- **Trade-offs:** Significant runtime rework and per-instance isolation work.
- **Affected components:** Runtime V2, networking, storage, UI.

---

## D009 — Google Play and Full capabilities may require separate build flavors

- **Date:** 2026-09-12
- **Status:** Accepted (direction)
- **Decision:** Achieve Google Play compatibility and Full-distribution capabilities via
  separate build flavors if platform policy requires it.
- **Reason:** Some capabilities (e.g. select package installation/runtime behavior) may not
  be permissible on Play, while Full distribution should retain them.
- **Alternatives considered:** A single distribution constrained to Play rules.
- **Trade-offs:** Added build/release complexity; preserves both reach and capability.
- **Affected components:** Gradle flavors, CI/release, catalog, runtime.
