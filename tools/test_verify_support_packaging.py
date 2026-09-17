#!/usr/bin/env python3
"""Self-test for tools/verify_support_packaging.py using synthetic APK archives."""
import hashlib
import importlib.util
import io
import json
import os
import struct
import sys
import unittest
import zipfile

HERE = os.path.dirname(os.path.abspath(__file__))


def load_verifier():
    spec = importlib.util.spec_from_file_location(
        "verify_support_packaging", os.path.join(HERE, "verify_support_packaging.py"))
    mod = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(mod)
    return mod


VERIFIER = load_verifier()


def make_elf64(align, machine=0xB7):
    e_ident = b"\x7fELF" + bytes([2, 1, 1, 0]) + b"\x00" * 8
    header = e_ident + struct.pack(
        "<HHIQQQIHHHHHH", 3, machine, 1, 0, 64, 0, 0, 64, 56, 1, 0, 0, 0)
    phdr = struct.pack("<IIQQQQQQ", 1, 5, 0, 0, 0, 0, 0, align)
    return header + phdr


def support_map(common_names=("execInProot.sh",), legacy_names=("proot",), modern_names=("proot",)):
    common = [{"name": n, "assetPath": f"support/common/{n}",
               "sha256": hashlib.sha256(f"c-{n}".encode()).hexdigest(), "executable": True}
              for n in common_names]
    legacy = [{"name": n, "assetPath": f"support/legacy/arm64-v8a/{n}",
               "sha256": hashlib.sha256(f"l-{n}".encode()).hexdigest(), "executable": True}
              for n in legacy_names]
    modern = [{"name": n, "nativeLib": f"lib_{n}.so",
               "sha256": hashlib.sha256(f"m-{n}".encode()).hexdigest()} for n in modern_names]
    return {"schemaVersion": 1, "release": "v1.2.0", "supportedAbis": ["arm64-v8a"],
            "common": common, "abis": {"arm64-v8a": {"legacy": legacy, "modern": modern}}}


def build_apk(path, sm, libs, assets=None, extra=None):
    with zipfile.ZipFile(path, "w") as z:
        z.writestr("assets/support/metadata/support-map.json", json.dumps(sm))
        for n, data in libs.items():
            z.writestr(n, data)
        for n, data in (assets or {}).items():
            z.writestr(n, data)
        for n, data in (extra or {}).items():
            z.writestr(n, data)


class VerifySupportPackagingTest(unittest.TestCase):
    def setUp(self):
        self.tmp = os.path.join("/tmp", f"p1f4b-selftest-{os.getpid()}")
        os.makedirs(self.tmp, exist_ok=True)

    def tearDown(self):
        import shutil
        shutil.rmtree(self.tmp, ignore_errors=True)

    def run_check(self, path):
        """Returns None on pass, or the failure message."""
        with zipfile.ZipFile(path) as zf:
            try:
                sm = VERIFIER.read_support_map(zf)
                VERIFIER.check_native_set(zf, "APK", sm)
                VERIFIER.check_assets(zf, "APK", sm)
                VERIFIER.check_no_duplication(zf, "APK", sm)
            except SystemExit:
                return "failed"
        return None

    def test_valid_apk_passes(self):
        sm = support_map()
        libs = {
            "lib/arm64-v8a/lib_proot.so": make_elf64(0x4000),
            "lib/arm64-v8a/libtermux.so": make_elf64(0x4000),
        }
        assets = {"assets/support/common/execInProot.sh": b"c-execInProot.sh",
                  "assets/support/legacy/arm64-v8a/proot": b"l-proot"}
        p = os.path.join(self.tmp, "valid.apk")
        build_apk(p, sm, libs, assets)
        self.assertIsNone(self.run_check(p))

    def test_non_elf_in_lib_fails(self):
        sm = support_map()
        p = os.path.join(self.tmp, "nonelf.apk")
        build_apk(p, sm, {"lib/arm64-v8a/lib_proot.so": b"not-an-elf"})
        self.assertIsNotNone(self.run_check(p))

    def test_below_16k_64bit_fails(self):
        sm = support_map()
        p = os.path.join(self.tmp, "below.apk")
        build_apk(p, sm, {"lib/arm64-v8a/lib_proot.so": make_elf64(0x1000)})
        self.assertIsNotNone(self.run_check(p))

    def test_lib_arch_marker_fails(self):
        sm = support_map()
        p = os.path.join(self.tmp, "libarch.apk")
        build_apk(p, sm, {"lib/arm64-v8a/lib_proot.so": make_elf64(0x4000),
                          "lib/arm64-v8a/lib_arch.so": b"arm64-v8a"})
        self.assertIsNotNone(self.run_check(p))

    def test_unknown_lib_fails(self):
        sm = support_map()
        p = os.path.join(self.tmp, "unknown.apk")
        build_apk(p, sm, {"lib/arm64-v8a/lib_proot.so": make_elf64(0x4000),
                          "lib/arm64-v8a/lib_mystery.so": make_elf64(0x4000)})
        self.assertIsNotNone(self.run_check(p))

    def test_asset_hash_mismatch_fails(self):
        sm = support_map()
        p = os.path.join(self.tmp, "hash.apk")
        build_apk(p, sm, {"lib/arm64-v8a/lib_proot.so": make_elf64(0x4000)},
                  {"assets/support/common/execInProot.sh": b"tampered",
                   "assets/support/legacy/arm64-v8a/proot": b"l-proot"})
        self.assertIsNotNone(self.run_check(p))

    def test_missing_support_map_fails(self):
        p = os.path.join(self.tmp, "nomap.apk")
        with zipfile.ZipFile(p, "w") as z:
            z.writestr("lib/arm64-v8a/lib_proot.so", make_elf64(0x4000))
        self.assertIsNotNone(self.run_check(p))


if __name__ == "__main__":
    unittest.main()
