#!/usr/bin/env python3
"""P1F4B whole-app static 16 KB packaging gate.

Verifies an assembled debug APK and (optionally) its AAB against the pinned v1.2.0
support contract:

  * every APK ``lib/<abi>/*.so`` is a real ELF;
  * no ``lib_arch.so`` pseudo-native marker exists;
  * every ``lib_*`` file is a declared modern support native (nothing else sneaks in);
  * no legacy/common support file is packaged under ``lib/``;
  * every 64-bit ELF under ``lib/arm64-v8a`` and ``lib/x86_64`` has ``PT_LOAD >= 0x4000``;
  * the packaged common/legacy assets match their ``support-map.json`` SHA-256;
  * ``zipalign -c -P 16 -v 4`` passes;
  * the AAB declares ``PAGE_ALIGNMENT_16K`` and carries no 4 KB-aligned 64-bit ELF.

Exits non-zero on the first failed gate.
"""
import argparse
import hashlib
import json
import struct
import subprocess
import sys
import zipfile

PT_LOAD = 1
EM_64 = (0x3E, 0xB7, 0xF3)  # x86-64, aarch64, riscv
SUPPORT_MAP = "assets/support/metadata/support-map.json"


def elf_load_alignments(data):
    if len(data) < 64 or data[:4] != b"\x7fELF":
        return None
    is64 = data[4] == 2
    if is64:
        phoff = struct.unpack_from("<Q", data, 32)[0]
        phentsize = struct.unpack_from("<H", data, 54)[0]
        phnum = struct.unpack_from("<H", data, 56)[0]
    else:
        phoff = struct.unpack_from("<I", data, 28)[0]
        phentsize = struct.unpack_from("<H", data, 42)[0]
        phnum = struct.unpack_from("<H", data, 44)[0]
    machine = struct.unpack_from("<H", data, 18)[0]
    loads = []
    for i in range(phnum):
        off = phoff + i * phentsize
        if off + phentsize > len(data):
            break
        if struct.unpack_from("<I", data, off)[0] == PT_LOAD:
            align = struct.unpack_from("<Q", data, off + 48)[0] if is64 else \
                struct.unpack_from("<I", data, off + 28)[0]
            loads.append(align)
    return is64, machine, loads


def fail(msg):
    print("GATE FAIL:", msg, file=sys.stderr)
    sys.exit(1)


def read_support_map(zf):
    try:
        return json.loads(zf.read(SUPPORT_MAP))
    except KeyError:
        fail(f"{SUPPORT_MAP} is missing from the artifact")


def check_native_set(zf, label, support_map):
    physical = set()
    for abi, lanes in support_map["abis"].items():
        for f in lanes["modern"]:
            physical.add(f["nativeLib"])
    # APK entries are `lib/<abi>/<name>.so`; AAB entries are `base/lib/<abi>/<name>.so`.
    lib_entries = [n for n in zf.namelist()
                   if (n.startswith("lib/") or "/lib/" in n) and n.endswith(".so")]
    if not lib_entries:
        fail(f"{label}: no native libraries packaged")
    below = []
    nonelf = []
    unknown_lib = []
    for n in lib_entries:
        data = zf.read(n)
        info = elf_load_alignments(data)
        if info is None:
            nonelf.append(n)
            continue
        is64, machine, loads = info
        if n.split("/")[1] in ("arm64-v8a", "x86_64") and is64:
            for a in loads:
                if a < 0x4000:
                    below.append((n, hex(a)))
        base = n.split("/")[-1]
        if base == "lib_arch.so":
            fail(f"{label}: lib_arch.so pseudo-native marker is packaged")
        if base.startswith("lib_") and base not in physical:
            unknown_lib.append(n)
    if nonelf:
        fail(f"{label}: non-ELF files under lib/: {nonelf}")
    if below:
        fail(f"{label}: 64-bit ELF with PT_LOAD < 0x4000: {below}")
    if unknown_lib:
        fail(f"{label}: lib_* files not declared by the support map: {unknown_lib}")
    print(f"{label}: {len(lib_entries)} native libraries, all ELF, all 64-bit >= 0x4000")
    return lib_entries


def check_no_duplication(zf, label, support_map):
    """Exact coverage: each lane's packaged set must equal the declared set."""
    names = zf.namelist()
    for abi, lanes in support_map["abis"].items():
        declared_modern = {f["nativeLib"] for f in lanes["modern"]}
        packaged_modern = {n.split("/")[-1] for n in names
                           if (n.startswith("lib/") or "/lib/" in n)
                           and n.split("/")[-2] == abi and n.endswith(".so")
                           and n.split("/")[-1].startswith("lib_")}
        if packaged_modern != declared_modern:
            fail(f"{label}: {abi} modern set mismatch: "
                 f"extra={sorted(packaged_modern - declared_modern)} "
                 f"missing={sorted(declared_modern - packaged_modern)}")
        declared_legacy = {f["name"] for f in lanes["legacy"]}
        packaged_legacy = {n.split("/")[-1] for n in names
                           if n.startswith(f"assets/support/legacy/{abi}/")}
        if packaged_legacy != declared_legacy:
            fail(f"{label}: {abi} legacy asset set mismatch: "
                 f"extra={sorted(packaged_legacy - declared_legacy)} "
                 f"missing={sorted(declared_legacy - packaged_legacy)}")
    declared_common = {f["name"] for f in support_map["common"]}
    packaged_common = {n.split("/")[-1] for n in names if n.startswith("assets/support/common/")}
    if packaged_common != declared_common:
        fail(f"{label}: common asset set mismatch: "
             f"extra={sorted(packaged_common - declared_common)} "
             f"missing={sorted(declared_common - packaged_common)}")
    print(f"{label}: no double-packaging; every declared support file appears exactly once")


def check_assets(zf, label, support_map):
    mismatches = []
    for f in support_map["common"]:
        digest = hashlib.sha256(zf.read("assets/" + f["assetPath"])).hexdigest()
        if digest != f["sha256"]:
            mismatches.append(f["assetPath"])
    legacy_count = 0
    for abi, lanes in support_map["abis"].items():
        for f in lanes["legacy"]:
            legacy_count += 1
            digest = hashlib.sha256(zf.read("assets/" + f["assetPath"])).hexdigest()
            if digest != f["sha256"]:
                mismatches.append(f["assetPath"])
    if mismatches:
        fail(f"{label}: packaged support asset hash mismatch: {mismatches}")
    print(f"{label}: {len(support_map['common'])} common + {legacy_count} legacy assets verified")
    return legacy_count


def check_aab(aab_path, support_map, bundletool, java):
    with zipfile.ZipFile(aab_path) as zf:
        check_native_set(zf, "AAB", support_map)
    if bundletool and java:
        out = subprocess.run([java, "-jar", bundletool, "dump", "config", f"--bundle={aab_path}"],
                             capture_output=True, text=True)
        if out.returncode != 0:
            fail(f"bundletool dump config failed: {out.stderr.strip()}")
        if "PAGE_ALIGNMENT_16K" not in out.stdout:
            fail("AAB does not declare PAGE_ALIGNMENT_16K")
        if "PAGE_ALIGNMENT_4K" in out.stdout:
            fail("AAB declares PAGE_ALIGNMENT_4K")
        print("AAB: bundletool reports PAGE_ALIGNMENT_16K")


def check_zipalign(apk_path, zipalign):
    out = subprocess.run([zipalign, "-c", "-P", "16", "-v", "4", apk_path],
                         capture_output=True, text=True)
    if out.returncode != 0:
        fail(f"zipalign -P 16 failed: {out.stdout[-2000:]} {out.stderr[-2000:]}")
    print("APK: zipalign -c -P 16 -v 4 PASS")


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--apk", required=True)
    ap.add_argument("--aab")
    ap.add_argument("--zipalign")
    ap.add_argument("--bundletool")
    ap.add_argument("--java")
    args = ap.parse_args()

    with zipfile.ZipFile(args.apk) as zf:
        support_map = read_support_map(zf)
        check_native_set(zf, "APK", support_map)
        check_assets(zf, "APK", support_map)
        check_no_duplication(zf, "APK", support_map)

    if args.zipalign:
        check_zipalign(args.apk, args.zipalign)
    if args.aab:
        check_aab(args.aab, support_map, args.bundletool, args.java)

    print("SUPPORT PACKAGING GATE: PASS")


if __name__ == "__main__":
    main()
