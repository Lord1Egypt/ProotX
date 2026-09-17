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

internal object Fixtures {
    const val ABI = "arm64-v8a"

    val REQUIRED = listOf(
        "proot", "loader", "proot_meta", "proot_meta_leveldb",
        "busybox", "busybox_static", "dbclient"
    )

    fun commonFile(name: String, exec: Boolean = true) =
        CommonFile(name = name, assetPath = "support/common/$name", sha256 = "common-$name", executable = exec)

    fun legacyFile(name: String, abi: String = ABI) =
        LegacyFile(name = name, assetPath = "support/legacy/$abi/$name", sha256 = "legacy-$name", executable = true)

    fun modernFile(name: String) = ModernFile(name = name, nativeLib = "lib_$name.so", sha256 = "modern-$name")

    /** Writes a valid map plus its referenced asset bytes under [assetsRoot]. */
    fun writeAssets(
        assetsRoot: File,
        abi: String = ABI,
        corrupt: Set<String> = emptySet(),
        modernNativeDir: File? = null
    ): SupportMap {
        val common = listOf(commonFile("execInProot.sh"), commonFile("uptime", exec = false))
        val legacy = REQUIRED.map { legacyFile(it, abi) }
        val modern = REQUIRED.map { modernFile(it) }
        common.forEach { writeAsset(assetsRoot, it.assetPath, if (it.name in corrupt) "corrupt" else "data-${it.name}") }
        legacy.forEach { writeAsset(assetsRoot, it.assetPath, if (it.name in corrupt) "corrupt" else "data-${it.name}") }

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
        common = listOf(Fixtures.commonFile("execInProot.sh")),
        abis = mapOf(
            Fixtures.ABI to SupportAbi(
                legacy = Fixtures.REQUIRED.map { Fixtures.legacyFile(it) },
                modern = Fixtures.REQUIRED.map { Fixtures.modernFile(it) }
            )
        )
    )

    @Test
    fun `accepts a complete map`() {
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
    fun `rejects a duplicate runtime name`() {
        val m = base()
        val abi = m.abis[Fixtures.ABI]!!
        val map = m.copy(abis = mapOf(Fixtures.ABI to abi.copy(legacy = abi.legacy + Fixtures.legacyFile("proot"))))
        val e = runCatching { SupportRoutingValidator.validate(map) }.exceptionOrNull()
        assertTrue(e is SupportMapException && e.message!!.contains("duplicate"))
    }
}

class SupportRuntimeInstallerTest {
    @get:Rule val temp = TemporaryFolder()

    private fun installer(assets: File, nativeDir: File, sdk: Int) =
        SupportRuntimeInstaller(
            assetSource = FileSupportAssetSource(assets),
            nativeLibraryDir = nativeDir,
            sdkInt = sdk,
            deviceAbis = listOf(Fixtures.ABI),
            symlinker = NioSymlinker()
        )

    @Test
    fun `api 28 installs common and legacy payload from assets`() {
        val assets = temp.newFolder("assets")
        val native = temp.newFolder("native")
        val support = File(temp.newFolder("files"), "support")
        val map = Fixtures.writeAssets(assets)

        val result = installer(assets, native, 28).install(support, map)

        assertEquals(SupportLane.LEGACY, result.lane)
        assertTrue(File(support, "proot").isFile)
        assertTrue(File(support, "execInProot.sh").isFile)
        assertTrue(File(support, "proot").canExecute())
        assertFalse(Files.isSymbolicLink(File(support, "proot").toPath()))
    }

    @Test
    fun `api 29 links modern payload from nativeLibraryDir`() {
        val assets = temp.newFolder("assets")
        val native = temp.newFolder("native")
        val support = File(temp.newFolder("files"), "support")
        val map = Fixtures.writeAssets(assets, modernNativeDir = native)

        val result = installer(assets, native, 29).install(support, map)

        assertEquals(SupportLane.MODERN, result.lane)
        val prootLink = File(support, "proot")
        assertTrue(Files.isSymbolicLink(prootLink.toPath()))
        assertEquals(File(native, "lib_proot.so").canonicalPath, prootLink.canonicalFile.path)
        assertFalse(Files.isSymbolicLink(File(support, "execInProot.sh").toPath()))
    }

    @Test
    fun `checksum mismatch fails closed`() {
        val assets = temp.newFolder("assets")
        val native = temp.newFolder("native")
        val support = File(temp.newFolder("files"), "support")
        val map = Fixtures.writeAssets(assets)
        Fixtures.writeAsset(assets, "support/common/execInProot.sh", "tampered")

        val e = runCatching { installer(assets, native, 28).install(support, map) }.exceptionOrNull()
        assertTrue(e is SupportIntegrityException)
    }

    @Test
    fun `reconciles stale historical layout and is idempotent`() {
        val assets = temp.newFolder("assets")
        val native = temp.newFolder("native")
        val support = File(temp.newFolder("files"), "support").apply { mkdirs() }
        val map = Fixtures.writeAssets(assets, modernNativeDir = native)

        File(support, "proot").writeText("stale")
        File(support, "libcrypto.so.1.1").writeText("stale")
        File(support, "lib_arch.so").writeText("stale-marker")

        val inst = installer(assets, native, 29)
        inst.install(support, map)
        assertFalse(File(support, "libcrypto.so.1.1").exists())
        assertTrue(Files.isSymbolicLink(File(support, "proot").toPath()))
        assertFalse(SupportRuntimeInstaller.HISTORICAL_NAMES.contains("lib_arch.so"))

        val marker = File(support, SupportRuntimeInstaller.MARKER_NAME)
        val markerText = marker.readText()
        val prootTarget = File(support, "proot").canonicalPath
        inst.install(support, map)
        assertEquals(markerText, marker.readText())
        assertEquals(prootTarget, File(support, "proot").canonicalPath)
    }

    @Test
    fun `meta routes resolve per lane`() {
        val assets = temp.newFolder("assets")
        val native = temp.newFolder("native")
        val supportLegacy = File(temp.newFolder("f1"), "support")
        val supportModern = File(temp.newFolder("f2"), "support")
        val map = Fixtures.writeAssets(assets, modernNativeDir = native)

        installer(assets, native, 28).install(supportLegacy, map)
        installer(assets, native, 29).install(supportModern, map)

        assertTrue(File(supportLegacy, "proot_meta").isFile)
        assertFalse(Files.isSymbolicLink(File(supportLegacy, "proot_meta").toPath()))
        assertTrue(Files.isSymbolicLink(File(supportModern, "proot_meta").toPath()))
        assertTrue(Files.isSymbolicLink(File(supportModern, "proot_meta_leveldb").toPath()))
    }

    @Test
    fun `no a10 heuristic is used`() {
        val assets = temp.newFolder("assets")
        val native = temp.newFolder("native")
        val support = File(temp.newFolder("files"), "support")
        val map = Fixtures.writeAssets(assets, modernNativeDir = native)
        installer(assets, native, 29).install(support, map)
        assertFalse(support.listFiles()!!.any { it.name.contains(".a10") })
    }
}
