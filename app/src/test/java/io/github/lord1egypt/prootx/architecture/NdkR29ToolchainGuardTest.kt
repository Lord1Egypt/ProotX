package io.github.lord1egypt.prootx.architecture

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * P1F1 guard: the in-tree native toolchain pins must stay on the accepted NDK r29 release and CI
 * must keep selecting the NDK through the Gradle `ndkVersion` (not the deprecated `ndk.dir`).
 *
 * This guard deliberately does NOT assert full-application 16 KB compatibility — the
 * ProotX-Assets-Support payloads are a separate blocker (P1F2/P1F3). It only protects the
 * toolchain migration owned by P1F1.
 *
 * Assertions avoid line-number/whitespace coupling.
 */
class NdkR29ToolchainGuardTest {

    private val expectedNdk = "29.0.14206865"

    private val appBuild = File("build.gradle")
    private val terminalEmulatorBuild = File("../termux-app/terminal-emulator/build.gradle")
    private val workflow = File("../.github/workflows/build.yml")

    @Test
    fun `app pins the accepted NDK r29`() {
        assertTrue("Expected to locate app/build.gradle", appBuild.isFile)
        val text = appBuild.readText()
        assertTrue(
            "app ndkVersion must be $expectedNdk",
            text.contains("ndkVersion \"$expectedNdk\"")
        )
        assertFalse(
            "the legacy NDK pin must be gone",
            text.contains("ndkVersion \"21.4.7075529\"")
        )
    }

    @Test
    fun `terminal-emulator pins the accepted NDK r29`() {
        assertTrue("Expected to locate terminal-emulator/build.gradle", terminalEmulatorBuild.isFile)
        val text = terminalEmulatorBuild.readText()
        assertTrue(
            "terminal-emulator ndkVersion must be $expectedNdk",
            text.contains("ndkVersion \"$expectedNdk\"")
        )
        assertFalse(
            "the legacy NDK pin must be gone",
            text.contains("ndkVersion \"21.4.7075529\"")
        )
    }

    @Test
    fun `CI pins and installs NDK r29`() {
        assertTrue("Expected to locate the workflow", workflow.isFile)
        val text = workflow.readText()
        assertTrue(
            "CI ANDROID_NDK_VERSION must be $expectedNdk",
            text.contains("ANDROID_NDK_VERSION: '$expectedNdk'")
        )
        assertTrue(
            "CI must install the pinned NDK package",
            text.contains("\"ndk;\$ANDROID_NDK_VERSION\"")
        )
    }

    @Test
    fun `CI selects the NDK via Gradle and no longer writes ndk dir`() {
        val text = workflow.readText()
        assertFalse(
            "CI must not write the deprecated ndk.dir",
            text.contains("echo \"ndk.dir=")
        )
        assertTrue(
            "CI must still write the SDK location",
            text.contains("echo \"sdk.dir=\$SDK_ROOT\" > local.properties")
        )
    }

    @Test
    fun `CI keeps the in-tree 16 KB native alignment check`() {
        val text = workflow.readText()
        assertTrue(
            "CI must inspect the packaged in-tree libtermux",
            text.contains("lib/arm64-v8a/libtermux.so") && text.contains("lib/x86_64/libtermux.so")
        )
        assertTrue(
            "CI must use the NDK llvm-readelf derived from the SDK root and pinned version",
            text.contains("toolchains/llvm/prebuilt/linux-x86_64/bin/llvm-readelf") &&
                text.contains("ndk/\$ANDROID_NDK_VERSION")
        )
        assertTrue(
            "CI must fail on PT_LOAD alignment below 0x4000",
            text.contains("16384")
        )
    }
}
