package io.github.lord1egypt.prootx.architecture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * P1D7-U2 guard: Material is stable `1.1.0`, and ProotX directly declares the two libraries
 * it consumes directly (`swiperefreshlayout`, `localbroadcastmanager`) that Material alpha
 * previously supplied only through its `androidx.legacy` transitives.
 *
 * The guard does not require any `androidx.legacy` artifact and does not ban future
 * controlled upgrades.
 */
class MaterialDependencyOwnershipGuardTest {

    private val expectedMaterialVersion = "1.1.0"
    private val preReleaseMarkers = listOf("-alpha", "-beta", "-rc")
    private val requiredDirectDependencies = listOf(
        "androidx.swiperefreshlayout:swiperefreshlayout:1.0.0",
        "androidx.localbroadcastmanager:localbroadcastmanager:1.0.0"
    )

    @Test
    fun `material is stable 1_1_0 and removed legacy transitives are owned directly`() {
        val appBuildFile = File("build.gradle")
        assertTrue("Expected to locate app/build.gradle", appBuildFile.isFile)
        // Normalise quotes so both "..." and '...' declarations are matched.
        val text = appBuildFile.readText().replace("\"", "").replace("'", "")

        val match = Regex("""support_library_version\s*=\s*([^\s'"]+)""").find(text)
        assertTrue("support_library_version declaration must exist", match != null)
        val version = match!!.groupValues[1]

        assertEquals("support_library_version must be the stable 1.1.0", expectedMaterialVersion, version)
        assertTrue(
            "support_library_version must not be a pre-release ($version)",
            preReleaseMarkers.none { version.contains(it) }
        )
        assertTrue(
            "com.google.android.material:material must use the shared support_library_version",
            text.contains("com.google.android.material:material:\$support_library_version")
        )

        requiredDirectDependencies.forEach { coordinate ->
            assertTrue(
                "$coordinate must be declared directly",
                text.contains(coordinate)
            )
        }
    }
}
