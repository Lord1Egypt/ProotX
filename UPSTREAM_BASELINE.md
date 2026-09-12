# ProotX Upstream Baseline & Provenance

> Legal and technical provenance of the ProotX codebase. Do not rewrite this history.

## Relationship to UserLAnd

ProotX is an **independent, modified derivative** of the **UserLAnd** project (GPLv3).
ProotX has its own branding, package identity (`io.github.lord1egypt.prootx`), repository,
and asset delivery. The ProotX name, logo, and original assets are original works of this
project.

## Selected Upstream Foundation

| Field | Value |
|---|---|
| Upstream project | UserLAnd |
| Upstream repository | https://github.com/CypherpunkArmory/UserLAnd |
| Upstream tag used as foundation | **v2.8.3** |
| Upstream commit SHA for v2.8.3 | `76346d134f7ab03fd6aecedc6f2309742286e2dd` (verified against the live upstream remote on 2026-09-12) |
| Upstream commit SHA recorded in *this* repository's history | **NOT RECORDED** — ProotX uses a fresh history and does not contain the upstream commit graph; the SHA above is external evidence, not part of ProotX history |

## Why v2.8.3 Was Selected

v2.8.3 is the **last self-contained, fully public, buildable** upstream release:

- Later upstream history depends on a `UserLAndLibrary` module that is **no longer
  public**. Without that module the later upstream tree cannot be built by anyone outside
  the original organization, and it is not archived in public archives consulted.
- v2.8.3 vendors everything needed (application code plus the terminal emulator modules),
  with no private submodule dependency, and was the basis of the last public builds.
- It provides a coherent, reproducible foundation for ProotX 2.0 modernization.

## Licensing & Attribution (must be preserved)

- ProotX is licensed under the **GNU General Public License v3.0** (`LICENSE`).
- Original code copyright: **UserLAnd Technologies, LLC** (see `COPYRIGHT`).
- Terminal emulator code derives from **Termux** (Apache-2.0); see `termux-app/LICENSE.md`.
- Required copyright and attribution notices **must remain** in place. Removing or altering
  them is not permitted.

## ProotX Baseline

| Field | Value |
|---|---|
| Baseline commit | `94abf5fa520255bb10d087a6be3ba2bc70b0e127` |
| Baseline tag | `v1.0.0-baseline` (annotated) |
| Package | `io.github.lord1egypt.prootx` |
| Version name | `1.0.0` |

## Notes

- ProotX's Git history begins at its own initial commit; upstream history is deliberately
  not carried over. Provenance is recorded here instead.
- No upstream commit SHA is invented. Where evidence is unavailable, it is marked as not
  recorded.
