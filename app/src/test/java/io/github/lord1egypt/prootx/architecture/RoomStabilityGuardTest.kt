package io.github.lord1egypt.prootx.architecture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * P1D5 guard: the shared Room version must remain the stable `2.1.0` and must drive
 * room-runtime, room-compiler and room-testing. It prevents an accidental pre-release
 * regression while this baseline is active (it does not ban future controlled upgrades).
 */
class RoomStabilityGuardTest {

    private val expectedVersion = "2.1.0"
    private val preReleaseMarkers = listOf("-alpha", "-beta", "-rc")

    @Test
    fun `room shared version is stable 2_1_0 and drives the family`() {
        val appBuildFile = File("build.gradle")
        assertTrue("Expected to locate app/build.gradle", appBuildFile.isFile)
        val text = appBuildFile.readText()

        val match = Regex("""room_version\s*=\s*['"]([^'"]+)['"]""").find(text)
        assertTrue("room_version declaration must exist", match != null)
        val version = match!!.groupValues[1]

        assertEquals("room_version must be the stable 2.1.0", expectedVersion, version)
        assertTrue(
            "room_version must not be a pre-release ($version)",
            preReleaseMarkers.none { version.contains(it) }
        )

        listOf("room-runtime", "room-compiler", "room-testing").forEach { artifact ->
            assertTrue(
                "androidx.room:$artifact must use the shared room_version",
                text.contains("androidx.room:$artifact:\$room_version")
            )
        }
    }
}
