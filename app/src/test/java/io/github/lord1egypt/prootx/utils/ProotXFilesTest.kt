package io.github.lord1egypt.prootx.utils

import android.content.Context
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.github.lord1egypt.prootx.support.FileSupportAssetSource
import io.github.lord1egypt.prootx.support.Fixtures
import io.github.lord1egypt.prootx.support.NioSymlinker
import io.github.lord1egypt.prootx.support.SupportLane
import io.github.lord1egypt.prootx.support.SupportRuntimeInstaller
import org.junit.Assert.* // ktlint-disable no-wildcard-imports
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ProotXFilesTest {

    @get:Rule val tempFolder = TemporaryFolder()

    private val mockContext: Context = mock()

    private lateinit var testFilesDir: File
    private lateinit var testScopedDir: File
    private lateinit var testLibDir: File
    private lateinit var testSupportDir: File
    private lateinit var assetsDir: File
    private lateinit var nativeDir: File

    @Before
    fun setup() {
        testFilesDir = tempFolder.newFolder("files")
        testScopedDir = tempFolder.newFolder("scoped")
        testLibDir = tempFolder.newFolder("execLib")
        assetsDir = tempFolder.newFolder("assets")
        nativeDir = tempFolder.newFolder("native")
        testSupportDir = File(testFilesDir, "support")

        whenever(mockContext.filesDir).thenReturn(testFilesDir)
        whenever(mockContext.getExternalFilesDir(null)).thenReturn(testScopedDir)
        whenever(mockContext.getExternalFilesDirs(null)).thenReturn(arrayOf(testFilesDir))
    }

    private fun prootxFiles(sdk: Int = 29): ProotXFiles {
        Fixtures.writeAssets(assetsDir, modernNativeDir = nativeDir)
        val installer = SupportRuntimeInstaller(
            assetSource = FileSupportAssetSource(assetsDir),
            nativeLibraryDir = nativeDir,
            sdkInt = sdk,
            deviceAbis = listOf(Fixtures.ABI),
            symlinker = NioSymlinker()
        )
        return ProotXFiles(mockContext, testLibDir.path, NioSymlinker(), installer)
    }

    @Test
    fun `makePermissionsUsable sets permissions open`() {
        val testFileName = "test"
        val testFile = tempFolder.newFile(testFileName)
        testFile.createNewFile()

        prootxFiles().makePermissionsUsable(tempFolder.root.path, testFileName)

        var output = ""
        val proc = Runtime.getRuntime().exec("ls -l ${testFile.path}")

        proc.inputStream.bufferedReader(Charsets.UTF_8).forEachLine { output += it }
        val permissions = output.substring(0, 10)
        assertTrue(permissions == "-rwxrwxrwx")
    }

    @Test
    fun `sdCardUserDir is created if an sd card exists and public fields are created`() {
        val sdcardDir = File(tempFolder.root, "sdcard")
        whenever(mockContext.getExternalFilesDirs(null))
                .thenReturn(arrayOf(testScopedDir, sdcardDir))

        val prootxFiles = prootxFiles()

        val expectedUserDir = File(sdcardDir, "storage")
        assertTrue(expectedUserDir.exists())
        assertTrue(expectedUserDir.isDirectory)
        assertEquals(sdcardDir, prootxFiles.sdCardScopedDir)
        assertEquals(expectedUserDir, prootxFiles.sdCardUserDir)
    }

    @Test
    fun `sdCardUserDir is not created if sdcard does not exist and public fields are null`() {
        whenever(mockContext.getExternalFilesDirs(null)).thenReturn(arrayOf(testFilesDir))

        val prootxFiles = prootxFiles()

        assertEquals(null, prootxFiles.sdCardScopedDir)
        assertEquals(null, prootxFiles.sdCardUserDir)
    }

    @Test
    fun `libDir is created from libDirPath constructor parameter`() {
        assertEquals(testLibDir, prootxFiles().libDir)
    }

    @Test
    fun `initialization installs the modern lane and resolves arch without lib_arch`() {
        val prootxFiles = prootxFiles(sdk = 29)

        assertEquals(SupportLane.MODERN, prootxFiles.installation.lane)
        assertEquals("arm64", prootxFiles.getArchType())
        assertTrue(prootxFiles.busybox.exists())
        assertTrue(prootxFiles.proot.exists())
        assertFalse(File(testSupportDir, "lib_arch.so").exists())
        assertTrue(java.nio.file.Files.isSymbolicLink(File(testSupportDir, "proot").toPath()))
    }

    @Test
    fun `initialization installs the legacy lane on api 28`() {
        val prootxFiles = prootxFiles(sdk = 28)

        assertEquals(SupportLane.LEGACY, prootxFiles.installation.lane)
        assertTrue(File(testSupportDir, "proot").isFile)
        assertFalse(java.nio.file.Files.isSymbolicLink(File(testSupportDir, "proot").toPath()))
    }
}
