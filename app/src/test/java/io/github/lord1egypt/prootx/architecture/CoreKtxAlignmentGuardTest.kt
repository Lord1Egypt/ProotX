package io.github.lord1egypt.prootx.architecture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * P1D9 guard: the directly declared AndroidX Core KTX artifact must stay aligned at the
 * stable `1.1.0` (matching the resolved `androidx.core:core` 1.1.x family) and keep using
 * the shared `ktx_version` variable. Prevents regression to 1.0.2 or a pre-release.
 */
class CoreKtxAlignmentGuardTest {

    private val expectedVersion = "1.1.0"
    private val forbiddenVersions = listOf("1.0.2", "-alpha", "-beta", "-rc")

    @Test
    fun `core-ktx is aligned at stable 1_1_0 via the shared variable`() {
        val appBuildFile = File("build.gradle")
        assertTrue("Expected to locate app/build.gradle", appBuildFile.isFile)
        val text = appBuildFile.readText()

        val match = Regex("""ktx_version\s*=\s*['"]([^'"]+)['"]""").find(text)
        assertTrue("ktx_version declaration must exist", match != null)
        val version = match!!.groupValues[1]

        assertEquals("ktx_version must be the aligned 1.1.0", expectedVersion, version)
        assertTrue(
            "ktx_version must not be a forbidden/pre-release version ($version)",
            forbiddenVersions.none { version.contains(it) }
        )
        assertTrue(
            "androidx.core:core-ktx must use the shared ktx_version",
            text.contains("androidx.core:core-ktx:\$ktx_version")
        )
    }
}
