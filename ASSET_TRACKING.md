# ProotX Asset Repository Tracking

> External runtime asset repositories consumed by the ProotX app. This file **tracks**
> them only. **Do not modify any asset repository** outside a dedicated assets milestone.

All six repositories are under the `Lord1Egypt` GitHub organization. Status values below
were last observed during P0/P0.5. The app resolves distribution assets from a repository's
**latest non-prerelease release**.

## Summary Table

| Repository | Purpose | Status | Modified in P0.5? | v1.0.0 release |
|---|---|---|---|---|
| `Lord1Egypt/ProotX-Assets-Support` | PRoot + Busybox support bundle, helper scripts, app catalog | Active | **No** | 4 assets, ~11 MB |
| `Lord1Egypt/ProotX-Assets-Debian` | Debian rootfs + environment assets | Active | **No** | 12 assets, ~1009 MB |
| `Lord1Egypt/ProotX-Assets-Ubuntu` | Ubuntu rootfs + environment assets | Active | **No** | 12 assets, ~868 MB |
| `Lord1Egypt/ProotX-Assets-Arch` | Arch rootfs + environment assets | Active | **No** | 6 assets, ~800 MB |
| `Lord1Egypt/ProotX-Assets-Kali` | Kali rootfs + environment assets | Active | **No** | 12 assets, ~1100 MB |
| `Lord1Egypt/ProotX-Assets-Alpine` | Alpine rootfs + environment assets | Active | **No** | 12 assets, ~881 MB |

(Arch ships only `arm64` and `x86_64`; the others ship `arm`, `arm64`, `x86`, `x86_64`.)

## Per-Repository Detail

### ProotX-Assets-Support

- **Purpose:** Common support bundle for every distro/app — PRoot, Busybox, helper scripts
  (e.g. `execInProot.sh`, extraction/add-user/server scripts), and the runtime app catalog
  (`apps/apps.txt`, icons, descriptions, startup scripts).
- **Status:** Active. Release `v1.0.0` published; support bundle embedded into the APK at
  build time via the `downloadAssets` Gradle task (pinned to `v1.0.0`).
- **Modified in current milestone:** No.
- **Known future work:** Rebuild support bundle when PRoot/native toolchain is modernized
  (P1F); keep app catalog in sync with Catalog V2 work.

### ProotX-Assets-Debian / -Ubuntu / -Arch / -Kali / -Alpine

- **Purpose:** Per-distribution root filesystem (`<arch>-rootfs.tar.gz`) plus environment
  scripts bundle (`<arch>-assets.tar.gz`) and manifest (`<arch>-assets.txt`).
- **Status:** Active. Release `v1.0.0` published for each; `arm64`/`x86_64` present for all,
  `arm`/`x86` additionally for Debian/Ubuntu/Kali/Alpine.
- **Modified in current milestone:** No.
- **Known future work:** Regenerate rootfs from newer upstream bases (see deferred finding
  below); validate against Runtime V2 / OCI support; potentially add more distributions.

## Confirmed Deferred Finding (rootfs/profile remnant)

The published distribution rootfs archives were built **before** the environment profile
script was renamed (`userland_profile.sh` → `prootx_profile.sh`). As a result, the prebuilt
rootfs blobs still contain an internal `/etc/profile.d/prootx.sh` artifact from the old
build path. The distro repositories' build scripts were already renamed, but the **already
published rootfs binaries were not rebuilt**.

- **Do not fix in P0.5.** Distribution assets must not be rebuilt or modified in this phase.
- **Resolve in a later assets/rebuild milestone**, alongside newer distro bases.

## Rules

- Treat the asset repositories as **external runtime dependencies**.
- Never modify them as a side effect of another milestone.
- Any rebuild is its own explicitly authorized milestone with its own acceptance criteria.
