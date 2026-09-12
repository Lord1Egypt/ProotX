<div align="center">

<img src="fastlane/metadata/android/en-US/images/icon.png" width="140" alt="ProotX logo" />

# ProotX

**The latest Linux distributions on Android — no root required.**

Run full desktop-class Linux environments and individual Linux applications directly on your Android device, installed and removed like any regular app.

[![Release](https://img.shields.io/github/v/release/Lord1Egypt/ProotX?style=for-the-badge&logo=github&color=7C3AED)](https://github.com/Lord1Egypt/ProotX/releases)
[![CI](https://img.shields.io/github/actions/workflow/status/Lord1Egypt/ProotX/build.yml?style=for-the-badge&logo=githubactions&label=build)](https://github.com/Lord1Egypt/ProotX/actions/workflows/build.yml)
[![License: GPL-3.0](https://img.shields.io/badge/License-GPLv3-blue.svg?style=for-the-badge)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.3.61-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Android API](https://img.shields.io/badge/API-21%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Stars](https://img.shields.io/github/stars/Lord1Egypt/ProotX?style=for-the-badge&logo=github&color=e879f9)](https://github.com/Lord1Egypt/ProotX/stargazers)
[![Issues](https://img.shields.io/github/issues/Lord1Egypt/ProotX?style=for-the-badge&logo=github)](https://github.com/Lord1Egypt/ProotX/issues)
[![Last commit](https://img.shields.io/github/last-commit/Lord1Egypt/ProotX?style=for-the-badge&logo=git&logoColor=white&color=22d3ee)](https://github.com/Lord1Egypt/ProotX/commits)

</div>

---

## ✨ Features

- 🐧 **Full Linux distros** — boot Debian, Ubuntu, Arch, Kali and Alpine as complete environments on top of Android.
- 🖥️ **Desktop environments** — add LXDE or XFCE and connect over VNC for a full graphical desktop.
- 📦 **One-click apps** — Firefox, LibreOffice, GIMP, Inkscape, Octave, R, Gnuplot, Git, IDLE and more, each running in its own Linux environment.
- 🔓 **No root required** — everything runs in user space through [PRoot](https://proot-me.github.io/); your bootloader stays locked and your warranty stays intact.
- 🧩 **App-like lifecycle** — install and uninstall environments like regular apps; nothing touches your system partition.
- 💾 **Shared storage access** — your Android files are available inside every environment.
- 🌐 **Network services** — SSH (Dropbear), VNC (TightVNC) and X11 (via XSDL) out of the box.
- 🔄 **Always current** — distribution root filesystems and support binaries are published as release assets and updated independently of the app.

##  Supported distributions

| Distribution | CLI | Desktop (VNC/XSDL) |
|:---|:---:|:---:|
| Debian | ✅ | ✅ |
| Ubuntu | ✅ | ✅ |
| Arch | ✅ | ✅ |
| Kali Linux | ✅ | ✅ |
| Alpine | ✅ | ✅ |

Desktop environments **LXDE** and **XFCE** can be layered on top of any Debian-based environment.

## 🏗️ How it works

ProotX bundles [PRoot](https://proot-me.github.io/) together with Busybox and a small set of support tools inside the APK. When you create an environment, the matching root filesystem is downloaded from the [ProotX-Assets](#-asset-repositories) release assets, extracted to app-local storage, and executed through PRoot with the Android filesystem bound in. Sessions are driven by the built-in terminal (a fork of the Termux terminal emulator) or by any VNC/XSDL client.

```
┌─────────────────────────────── Android ───────────────────────────────┐
│  ProotX app (Kotlin)                                                  │
│   ├─ Session & filesystem management (Room)                           │
│   ├─ Built-in terminal emulator (termux-app modules)                  │
│   └─ Support bundle: PRoot + Busybox (jniLibs)                        │
│        └─ PRoot chroot ──► Linux rootfs (Debian / Ubuntu / …)         │
│              ├─ SSH / VNC / XSDL servers                              │
│              └─ Your Linux apps & desktop environments                │
└───────────────────────────────────────────────────────────────────────┘
```

## 🚀 Getting started

### Build from source

Requirements: JDK 8+, Android SDK (compileSdk 30), Android NDK (for the terminal emulator JNI).

```bash
git clone https://github.com/Lord1Egypt/ProotX.git
cd ProotX
./gradlew assembleDebug
```

The first build downloads the PRoot/Busybox support bundle from [ProotX-Assets-Support](https://github.com/Lord1Egypt/ProotX-Assets-Support) releases automatically.

Install the debug APK on a device running Android 5.0 (API 21) or newer, launch ProotX, pick a distribution, and start a session.

### First session

1. Open ProotX and tap a distribution (or an app) in the **Apps** tab.
2. Choose a username and password when prompted — this creates the environment.
3. The root filesystem downloads once; afterwards sessions start instantly.
4. Use the **Sessions** tab to start, stop and edit environments (SSH/VNC services, desktop environments, startup commands).

## 📦 Asset repositories

Runtime assets live outside the app and are delivered through GitHub releases:

| Repository | Contents |
|:---|:---|
| [ProotX-Assets-Support](https://github.com/Lord1Egypt/ProotX-Assets-Support) | PRoot, Busybox, support scripts, app catalog & icons |
| [ProotX-Assets-Debian](https://github.com/Lord1Egypt/ProotX-Assets-Debian) | Debian rootfs + environment assets |
| [ProotX-Assets-Ubuntu](https://github.com/Lord1Egypt/ProotX-Assets-Ubuntu) | Ubuntu rootfs + environment assets |
| [ProotX-Assets-Arch](https://github.com/Lord1Egypt/ProotX-Assets-Arch) | Arch rootfs + environment assets |
| [ProotX-Assets-Kali](https://github.com/Lord1Egypt/ProotX-Assets-Kali) | Kali rootfs + environment assets |
| [ProotX-Assets-Alpine](https://github.com/Lord1Egypt/ProotX-Assets-Alpine) | Alpine rootfs + environment assets |

## 🗂️ Repository layout

```
ProotX/
├─ app/                  # Main application (Kotlin)
│   └─ src/main/jniLibs/ # PRoot + Busybox support bundle (fetched at build time)
├─ termux-app/           # Terminal emulator modules (emulator, view, term)
├─ fastlane/             # Store metadata & graphics
└─ gradle/               # Wrapper
```

## ❓ FAQ

**Does ProotX need root?**
No. Everything runs in user space via PRoot.

**Will it void my warranty or trip SafetyNet/Play Integrity?**
No. Nothing is flashed, no system partition is modified.

**Which architectures are supported?**
`arm64-v8a`, `armeabi-v7a`, `x86_64` and `x86`.

**Where is my data stored?**
Environments live in ProotX's app-private storage. Uninstalling the app removes them completely.

**Can I use my own VNC client?**
Yes — start a VNC session in ProotX and connect with any client (e.g. aVNC) to `localhost:5901`.

**Something is broken — what now?**
Open an [issue](https://github.com/Lord1Egypt/ProotX/issues) with your device model, Android version and the ProotX debug log (Settings → proot debug level).

## 🤝 Contributing

Contributions are welcome: bug reports, feature requests, translations and pull requests. Open an issue first for anything large so we can align on direction.

## ⚖️ License & attribution

ProotX is free software licensed under the [GNU General Public License v3.0](LICENSE).

ProotX is an independent, modified derivative of the [UserLAnd](https://github.com/CypherpunkArmory/UserLAnd) project (© UserLAnd Technologies, LLC, GPLv3) and contains code from [Termux](https://github.com/termux/termux-app) (Apache-2.0). See [COPYRIGHT](COPYRIGHT). The ProotX name, logo and branding are original works of this project.

---

<div align="center">

**Made with 💜 for the terminal-curious.**

[Report a bug](https://github.com/Lord1Egypt/ProotX/issues) · [Request a feature](https://github.com/Lord1Egypt/ProotX/issues) · [Releases](https://github.com/Lord1Egypt/ProotX/releases)

</div>
