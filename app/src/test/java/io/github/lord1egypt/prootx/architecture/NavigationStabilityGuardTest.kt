package io.github.lord1egypt.prootx.architecture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * P1D4 guard: the shared Navigation version must remain the stable `2.1.0` and must drive
 * the Safe Args plugin plus the runtime Navigation artifacts. It prevents an accidental
 * pre-release regression while this baseline is active (it does not ban future controlled
 * Navigation upgrades).
 */
class NavigationStabilityGuardTest {

    private val expectedVersion = "2.1.0"
    private val preReleaseMarkers = listOf("-alpha", "-beta", "-rc")

    @Test
    fun `navigation shared version is stable 2_1_0 and drives the family`() {
        val rootBuildFile = File("../build.gradle")
        assertTrue("Expected to locate root build.gradle", rootBuildFile.isFile)
        val rootText = rootBuildFile.readText()

        val match = Regex("""navigation_version\s*=\s*['"]([^'"]+)['"]""").find(rootText)
        assertTrue("navigation_version declaration must exist", match != null)
        val version = match!!.groupValues[1]

        assertEquals("navigation_version must be the stable 2.1.0", expectedVersion, version)
        assertTrue(
            "navigation_version must not be a pre-release ($version)",
            preReleaseMarkers.none { version.contains(it) }
        )
        assertTrue(
            "Safe Args plugin must be driven by the shared navigation_version",
            rootText.contains("androidx.navigation:navigation-safe-args-gradle-plugin:\$navigation_version")
        )

        val appBuildFile = File("build.gradle")
        assertTrue("Expected to locate app/build.gradle", appBuildFile.isFile)
        val appText = appBuildFile.readText()
        assertTrue(
            "navigation-fragment-ktx must be driven by the shared navigation_version",
            appText.contains("androidx.navigation:navigation-fragment-ktx:\$navigation_version")
        )
        assertTrue(
            "navigation-ui-ktx must be driven by the shared navigation_version",
            appText.contains("androidx.navigation:navigation-ui-ktx:\$navigation_version")
        )
    }
}
