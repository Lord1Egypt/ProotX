package io.github.lord1egypt.prootx.support

import com.squareup.moshi.Moshi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.nio.file.Files

internal class NioSymlinker : Symlinker {
    override fun createSymlink(targetPath: String, linkPath: String) {
        val link = File(linkPath)
        link.delete()
        Files.createSymbolicLink(link.toPath(), File(targetPath).toPath())
    }
}

/** Counts symlink creations so a no-op re-initialization can be proven. */
internal class CountingSymlinker(private val delegate: Symlinker = NioSymlinker()) : Symlinker {
    var createCount = 0
        private set

    override fun createSymlink(targetPath: String, linkPath: String) {
        createCount++
        delegate.createSymlink(targetPath, linkPath)
    }
}

/** Counts asset reads so a no-op re-initialization can be proven. */
internal class CountingAssetSource(private val delegate: SupportAssetSource) : SupportAssetSource {
    var openCount = 0
        private set

    override fun open(path: String): java.io.InputStream {
        openCount++
        return delegate.open(path)
    }

    override fun list(path: String): List<String> = delegate.list(path)
}

internal object Fixtures {
    const val ABI = "arm64-v8a"

    /** Names present in both lanes. */
    val SHARED = listOf(
        "proot", "loader", "busybox", "busybox_static", "dbclient",
        "proot_meta", "proot_meta_leveldb", "libtalloc.so.2", "libtermux-auth.so",
        "libc++_shared.so"
    )

    /** v1.2.0 legacy-only natives. */
    val LEGACY_ONLY = listOf("libcrypto.so.1.1", "libleveldb.so.1", "libutil.so")

    /** v1.2.0 modern-only natives. */
    val MODERN_ONLY = listOf(
        "libcrypto.so.3", "libleveldb.so", "libandroid-shmem.so", "libandroid-selinux.so",
        "libbusybox.so.1.38.0", "libpcre2-8.so", "libsnappy.so", "libz.so.1"
    )

    val COMMON = listOf("execInProot.sh", "uptime")
    val LEGACY = SHARED + LEGACY_ONLY
    val MODERN = SHARED + MODERN_ONLY

    /** Routes every ABI must expose (mirrors the build-time guard). */
    val REQUIRED = setOf(
        "proot", "loader", "proot_meta", "proot_meta_leveldb",
        "busybox", "busybox_static", "dbclient", "execInProot.sh"
    )

    fun commonFile(name: String, exec: Boolean = true) =
        CommonFile(name = name, assetPath = "support/common/$name", sha256 = "common-$name", executable = exec)

    fun legacyFile(name: String, abi: String = ABI) =
        LegacyFile(name = name, assetPath = "support/legacy/$abi/$name", sha256 = "legacy-$name", executable = true)

    fun modernFile(name: String) = ModernFile(name = name, nativeLib = "lib_$name.so", sha256 = "modern-$name")

    /** Writes a valid, lane-asymmetric map plus its referenced asset bytes under [assetsRoot]. */
    fun writeAssets(
        assetsRoot: File,
        abi: String = ABI,
        modernNativeDir: File? = null
    ): SupportMap {
        val common = COMMON.map { commonFile(it, exec = it.endsWith(".sh")) }
        val legacy = LEGACY.map { legacyFile(it, abi) }
        val modern = MODERN.map { modernFile(it) }
        common.forEach { writeAsset(assetsRoot, it.assetPath, "data-${it.name}") }
        legacy.forEach { writeAsset(assetsRoot, it.assetPath, "data-${it.name}") }

        fun sha(path: String) = sha256Hex(File(assetsRoot, path).readBytes())
        val map = SupportMap(
            schemaVersion = 1,
            release = "v1.2.0",
            supportedAbis = listOf(abi),
            common = common.map { it.copy(sha256 = sha(it.assetPath)) },
            abis = mapOf(abi to SupportAbi(
                legacy = legacy.map { it.copy(sha256 = sha(it.assetPath)) },
                modern = modern
            ))
        )
        writeAsset(assetsRoot, SupportMapLoader.MAP_ASSET_PATH, SupportMapJson.encode(map))
        modernNativeDir?.let { dir ->
            modern.forEach { dir.mkdirs(); File(dir, it.nativeLib).writeText("native-${it.name}") }
        }
        return map
    }

    fun writeAsset(root: File, path: String, content: String) {
        val f = File(root, path)
        f.parentFile.mkdirs()
        f.writeText(content)
    }

    /** Captures support state so a no-op re-initialization can be proven structurally. */
    fun snapshot(dir: File): Map<String, String> {
        val out = sortedMapOf<String, String>()
        dir.listFiles()?.forEach { f ->
            out[f.name] = if (Files.isSymbolicLink(f.toPath())) {
                "link:" + Files.readSymbolicLink(f.toPath()).toString()
            } else {
                "file:" + sha256Hex(f.readBytes())
            }
        }
        return out
    }

    object SupportMapJson {
        private val moshi: Moshi = Moshi.Builder().build()
        fun encode(map: SupportMap): String = moshi.adapter(SupportMap::class.java).toJson(map)
    }
}

class AbiResolverTest {
    @Test
    fun `selects the first device ABI in preference order that is supported`() {
        assertEquals("x86_64", AbiResolver.select(listOf("arm64-v8a", "x86_64"), listOf("x86_64", "arm64-v8a")))
    }

    @Test
    fun `returns null when no device ABI is supported`() {
        assertNull(AbiResolver.select(listOf("arm64-v8a"), listOf("mips")))
    }
}

class SupportRoutingValidatorTest {
    private fun base() = SupportMap(
        schemaVersion = 1,
        release = "v1.2.0",
        supportedAbis = listOf(Fixtures.ABI),
        common = Fixtures.COMMON.map { Fixtures.commonFile(it) },
        abis = mapOf(
            Fixtures.ABI to SupportAbi(
                legacy = Fixtures.LEGACY.map { Fixtures.legacyFile(it) },
                modern = Fixtures.MODERN.map { Fixtures.modernFile(it) }
            )
        )
    )

    @Test
    fun `accepts a lane-asymmetric map`() {
        SupportRoutingValidator.validate(base())
    }

    @Test
    fun `rejects a missing required route`() {
        val m = base()
        val abi = m.abis[Fixtures.ABI]!!
        val map = m.copy(abis = mapOf(Fixtures.ABI to abi.copy(
            legacy = abi.legacy.filter { it.name != "proot" },
            modern = abi.modern.filter { it.name != "proot" }
        )))
        val e = runCatching { SupportRoutingValidator.validate(map) }.exceptionOrNull()
        assertTrue(e is SupportMapException && e.message!!.contains("proot"))
    }

    @Test
    fun `rejects a duplicate runtime name within a lane`() {
        val m = base()
        val abi = m.abis[Fixtures.ABI]!!
        val map = m.copy(abis = mapOf(Fixtures.ABI to abi.copy(legacy = abi.legacy + Fixtures.legacyFile("proot"))))
        val e = runCatching { SupportRoutingValidator.validate(map) }.exceptionOrNull()
        assertTrue(e is SupportMapException && e.message!!.contains("duplicate"))
    }
}

class SupportRuntimeInstallerTest {
    @get:Rule val temp = TemporaryFolder()

    private fun native() = temp.newFolder("native")

    private fun installer(
        assetSource: SupportAssetSource,
        nativeDir: File,
        sdk: Int,
        symlinker: Symlinker
    ) = SupportRuntimeInstaller(
        assetSource = assetSource,
        nativeLibraryDir = nativeDir,
        sdkInt = sdk,
        deviceAbis = listOf(Fixtures.ABI),
        symlinker = symlinker
    )

    @Test
    fun `api 28 installs common and legacy payload from assets`() {
        val native = native()
        val support = File(temp.newFolder("files"), "support")
        val root = temp.newFolder("assets-root")
        val map = Fixtures.writeAssets(root, modernNativeDir = native)
        val assets = CountingAssetSource(FileSupportAssetSource(root))

        val result = installer(assets, native, 28, CountingSymlinker()).install(support, map)

        assertEquals(SupportLane.LEGACY, result.lane)
        assertTrue(File(support, "proot").isFile)
        assertTrue(File(support, "execInProot.sh").isFile)
        assertTrue(File(support, "proot").canExecute())
        assertFalse(Files.isSymbolicLink(File(support, "proot").toPath()))
    }

    @Test
    fun `api 29 links modern payload from nativeLibraryDir`() {
        val native = native()
        val support = File(temp.newFolder("files"), "support")
        val root = temp.newFolder("assets-root")
        val map = Fixtures.writeAssets(root, modernNativeDir = native)
        val assets = CountingAssetSource(FileSupportAssetSource(root))

        val result = installer(assets, native, 29, CountingSymlinker()).install(support, map)

        assertEquals(SupportLane.MODERN, result.lane)
        assertTrue(Files.isSymbolicLink(File(support, "proot").toPath()))
        assertEquals(File(native, "lib_proot.so").canonicalPath, File(support, "proot").canonicalFile.path)
        assertFalse(Files.isSymbolicLink(File(support, "execInProot.sh").toPath()))
    }

    @Test
    fun `checksum mismatch fails closed`() {
        val native = native()
        val support = File(temp.newFolder("files"), "support")
        val root = temp.newFolder("assets-root")
        val map = Fixtures.writeAssets(root, modernNativeDir = native)
        Fixtures.writeAsset(root, "support/common/execInProot.sh", "tampered")
        val assets = CountingAssetSource(FileSupportAssetSource(root))

        val e = runCatching { installer(assets, native, 28, CountingSymlinker()).install(support, map) }.exceptionOrNull()
        assertTrue(e is SupportIntegrityException)
    }

    @Test
    fun `modern second initialization is a true no-op`() {
        val native = native()
        val support = File(temp.newFolder("files"), "support")
        val root = temp.newFolder("assets-root")
        val map = Fixtures.writeAssets(root, modernNativeDir = native)
        val assets = CountingAssetSource(FileSupportAssetSource(root))
        val symlinker = CountingSymlinker()
        val inst = installer(assets, native, 29, symlinker)

        inst.install(support, map)
        assertTrue(File(support, "libcrypto.so.3").exists())
        assertFalse(File(support, "libcrypto.so.1.1").exists())
        assertFalse(File(support, "libleveldb.so.1").exists())
        assertFalse(File(support, "libutil.so").exists())

        val before = Fixtures.snapshot(support)
        val markerBefore = File(support, SupportRuntimeInstaller.MARKER_NAME).readBytes().copyOf()
        val opens = assets.openCount
        val links = symlinker.createCount

        inst.install(support, map)

        assertEquals("no asset copies on re-init", opens, assets.openCount)
        assertEquals("no symlink creations on re-init", links, symlinker.createCount)
        assertEquals("support state unchanged", before, Fixtures.snapshot(support))
        assertTrue(
            "marker not rewritten",
            markerBefore.contentEquals(File(support, SupportRuntimeInstaller.MARKER_NAME).readBytes())
        )
    }

    @Test
    fun `legacy second initialization is a true no-op`() {
        val native = native()
        val support = File(temp.newFolder("files"), "support")
        val root = temp.newFolder("assets-root")
        val map = Fixtures.writeAssets(root, modernNativeDir = native)
        val assets = CountingAssetSource(FileSupportAssetSource(root))
        val symlinker = CountingSymlinker()
        val inst = installer(assets, native, 28, symlinker)

        inst.install(support, map)
        assertTrue(File(support, "libcrypto.so.1.1").isFile)
        assertFalse(File(support, "libcrypto.so.3").exists())
        assertFalse(File(support, "libz.so.1").exists())

        val before = Fixtures.snapshot(support)
        val markerBefore = File(support, SupportRuntimeInstaller.MARKER_NAME).readBytes().copyOf()
        val opens = assets.openCount
        val links = symlinker.createCount

        inst.install(support, map)

        assertEquals(opens, assets.openCount)
        assertEquals(links, symlinker.createCount)
        assertEquals(before, Fixtures.snapshot(support))
        assertTrue(markerBefore.contentEquals(File(support, SupportRuntimeInstaller.MARKER_NAME).readBytes()))
    }

    @Test
    fun `api 28 to api 29 transition reinstalls once then is a no-op`() {
        val native = native()
        val support = File(temp.newFolder("files"), "support")
        val root = temp.newFolder("assets-root")
        val map = Fixtures.writeAssets(root, modernNativeDir = native)
        val assets = CountingAssetSource(FileSupportAssetSource(root))
        val symlinker = CountingSymlinker()

        installer(assets, native, 28, symlinker).install(support, map)
        assertTrue(File(support, "libcrypto.so.1.1").isFile)

        val linksBefore = symlinker.createCount
        val opensBefore = assets.openCount
        installer(assets, native, 29, symlinker).install(support, map)

        assertTrue("transition must reinstall", assets.openCount > opensBefore)
        assertTrue("transition must create modern links", symlinker.createCount > linksBefore)
        assertTrue(File(support, "libcrypto.so.3").exists())
        assertFalse("legacy-only must not shadow modern", File(support, "libcrypto.so.1.1").exists())
        assertFalse(File(support, "libleveldb.so.1").exists())
        assertFalse(File(support, "libutil.so").exists())
        assertTrue(Files.isSymbolicLink(File(support, "proot").toPath()))

        val opens = assets.openCount
        val links = symlinker.createCount
        installer(assets, native, 29, symlinker).install(support, map)
        assertEquals(opens, assets.openCount)
        assertEquals(links, symlinker.createCount)
    }

    @Test
    fun `reconciles stale historical layout including lane-specific names`() {
        val native = native()
        val support = File(temp.newFolder("files"), "support").apply { mkdirs() }
        val root = temp.newFolder("assets-root")
        val map = Fixtures.writeAssets(root, modernNativeDir = native)
        val assets = CountingAssetSource(FileSupportAssetSource(root))

        File(support, "proot").writeText("stale")
        File(support, "libcrypto.so.1.1").writeText("stale")
        File(support, "libleveldb.so.1").writeText("stale")
        File(support, "libutil.so").writeText("stale")
        File(support, "lib_arch.so").writeText("stale-marker")

        installer(assets, native, 29, CountingSymlinker()).install(support, map)

        assertFalse(File(support, "libcrypto.so.1.1").exists())
        assertFalse(File(support, "libleveldb.so.1").exists())
        assertFalse(File(support, "libutil.so").exists())
        assertTrue(File(support, "libcrypto.so.3").exists())
        assertTrue(Files.isSymbolicLink(File(support, "proot").toPath()))
        assertFalse(SupportRuntimeInstaller.HISTORICAL_NAMES.contains("lib_arch.so"))
    }

    @Test
    fun `meta routes resolve per lane`() {
        val native = native()
        val root = temp.newFolder("assets-root")
        val map = Fixtures.writeAssets(root, modernNativeDir = native)
        val assets = CountingAssetSource(FileSupportAssetSource(root))
        val supportLegacy = File(temp.newFolder("f1"), "support")
        val supportModern = File(temp.newFolder("f2"), "support")

        installer(assets, native, 28, CountingSymlinker()).install(supportLegacy, map)
        installer(assets, native, 29, CountingSymlinker()).install(supportModern, map)

        assertTrue(File(supportLegacy, "proot_meta").isFile)
        assertFalse(Files.isSymbolicLink(File(supportLegacy, "proot_meta").toPath()))
        assertTrue(Files.isSymbolicLink(File(supportModern, "proot_meta").toPath()))
        assertTrue(Files.isSymbolicLink(File(supportModern, "proot_meta_leveldb").toPath()))
    }

    @Test
    fun `no a10 heuristic is used`() {
        val native = native()
        val support = File(temp.newFolder("files"), "support")
        val root = temp.newFolder("assets-root")
        val map = Fixtures.writeAssets(root, modernNativeDir = native)
        val assets = CountingAssetSource(FileSupportAssetSource(root))
        installer(assets, native, 29, CountingSymlinker()).install(support, map)
        assertFalse(support.listFiles()!!.any { it.name.contains(".a10") })
    }
}
