# AGENTS.md — ProotX Repository Engineering Contract

This file is the standing contract for any coding agent (or human) working in this
repository. It contains **no secrets and no volatile state** — current status lives in
`PROJECT_STATE.md` and `SESSION_HANDOFF.md`.

## Before starting any work

1. Read `PROJECT_STATE.md`.
2. Read `SESSION_HANDOFF.md`.
3. Read `TASKS.md`.
4. Read `DECISIONS.md`.
5. Read `docs/PROOTX_2_ROADMAP.md`.
6. Inspect Git state: `git status`, `git branch --show-current`, and
   `git log --oneline --decorate -10`.

Then determine the last completed milestone, the current milestone, any blocker, and the
next safe action — from the repository, not from memory.

## Rules

- **Never guess current state.** Verify with Git and the control-plane files.
- **Never silently change scope.** If the task is ambiguous, stop and ask.
- **One milestone at a time.** Respect STOP gates; do not start the next milestone
  unprovoked.
- **Do not touch `main`** unless explicitly instructed.
- **Do not rewrite published history.** No force-push, no rebase of shared branches, no
  tag rewrites.
- **Do not run `git reset --hard` or `git clean -fd`** against user work without explicit
  owner authorization.
- **Preserve licensing and provenance.** Keep GPLv3 notices and the required UserLAnd
  attribution intact (see `UPSTREAM_BASELINE.md`).
- **Preserve user data.** Never delete user environments, filesystems, or databases.
- **Record deferred findings**, do not opportunistically fix unrelated systems. Add them
  to `docs/PROOTX_2_ROADMAP.md` and/or `PROJECT_STATE.md`.
- **Update project-state files before stopping** a substantial session
  (`PROJECT_STATE.md`, `SESSION_HANDOFF.md`, `TASKS.md`, `CHANGELOG_DEV.md`).
- **Source and verified repository state override stale documentation.** If docs and source
  conflict, investigate and update the stale docs rather than blindly coding against them.
- **Do not modify the `ProotX-Assets-*` repositories** outside a dedicated assets
  milestone (see `ASSET_TRACKING.md`).

## Branching

- `main` — stable; no direct development commits.
- `develop` — integration branch.
- `feature/*` — short-lived work branches (current: `feature/android-modernization`).

## Commits

- Small, descriptive commits; state the milestone when relevant.
- Do not commit build outputs, secrets, or machine-specific configuration.

## Milestone etiquette

- Work only on the authorized branch.
- Produce a final report at each STOP gate.
- Do not merge, tag, or publish unless the milestone explicitly authorizes it.
