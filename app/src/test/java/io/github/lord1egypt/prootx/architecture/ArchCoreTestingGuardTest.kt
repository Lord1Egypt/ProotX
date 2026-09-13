package io.github.lord1egypt.prootx.architecture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * P1D8 guard: the test-only Arch Core testing dependency must remain stable `2.1.0` and must
 * drive both the JVM and androidTest declarations. Prevents a regression to the old
 * `2.0.0-beta01` (or any other pre-release). Does not ban future controlled upgrades.
 */
class ArchCoreTestingGuardTest {

    private val expectedVersion = "2.1.0"
    private val forbiddenVersions = listOf("2.0.0-beta01", "2.1.0-alpha", "2.1.0-beta", "2.1.0-rc")

    @Test
    fun `arch core testing is stable 2_1_0 for both test source sets`() {
        val appBuildFile = File("build.gradle")
        assertTrue("Expected to locate app/build.gradle", appBuildFile.isFile)
        val text = appBuildFile.readText()

        val match = Regex("""core_testing_version\s*=\s*['"]([^'"]+)['"]""").find(text)
        assertTrue("core_testing_version declaration must exist", match != null)
        val version = match!!.groupValues[1]

        assertEquals("core_testing_version must be the stable 2.1.0", expectedVersion, version)
        assertTrue(
            "core_testing_version must not be a forbidden/pre-release version ($version)",
            forbiddenVersions.none { version.contains(it) }
        )
        assertTrue(
            "testImplementation must use the shared core_testing_version",
            text.contains("testImplementation \"androidx.arch.core:core-testing:\$core_testing_version\"")
        )
        assertTrue(
            "androidTestImplementation must use the shared core_testing_version",
            text.contains("androidTestImplementation \"androidx.arch.core:core-testing:\$core_testing_version\"")
        )
    }
}
