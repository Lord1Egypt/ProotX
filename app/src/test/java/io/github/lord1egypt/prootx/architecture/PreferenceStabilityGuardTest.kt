package io.github.lord1egypt.prootx.architecture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * P1D6 guard: the shared Preference version must remain the stable `1.1.0` and must drive
 * `androidx.preference:preference`. It prevents an accidental pre-release regression while
 * this baseline is active (it does not ban future controlled upgrades).
 */
class PreferenceStabilityGuardTest {

    private val expectedVersion = "1.1.0"
    private val preReleaseMarkers = listOf("-alpha", "-beta", "-rc")

    @Test
    fun `preference shared version is stable 1_1_0`() {
        val appBuildFile = File("build.gradle")
        assertTrue("Expected to locate app/build.gradle", appBuildFile.isFile)
        val text = appBuildFile.readText()

        val match = Regex("""preference_version\s*=\s*['"]([^'"]+)['"]""").find(text)
        assertTrue("preference_version declaration must exist", match != null)
        val version = match!!.groupValues[1]

        assertEquals("preference_version must be the stable 1.1.0", expectedVersion, version)
        assertTrue(
            "preference_version must not be a pre-release ($version)",
            preReleaseMarkers.none { version.contains(it) }
        )
        assertTrue(
            "androidx.preference:preference must use the shared preference_version",
            text.contains("androidx.preference:preference:\$preference_version")
        )
    }
}
