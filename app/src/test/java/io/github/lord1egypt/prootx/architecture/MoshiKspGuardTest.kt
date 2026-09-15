package io.github.lord1egypt.prootx.architecture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * P1E2 guard: Moshi code generation must run on KSP while Room stays on KAPT, with the
 * shared Kotlin/KSP versions pinned. Prevents an accidental regression back to Moshi-on-kapt
 * (or a Room-on-KSP move) while this bridge baseline is active. It does not ban future
 * controlled upgrades.
 */
class MoshiKspGuardTest {

    private val expectedKspVersion = "1.9.25-1.0.20"
    private val expectedMoshiVersion = "1.15.2"

    @Test
    fun `moshi codegen uses ksp and room remains on kapt`() {
        val rootBuildFile = File("../build.gradle")
        assertTrue("Expected to locate root build.gradle", rootBuildFile.isFile)
        val rootText = rootBuildFile.readText()

        val kspMatch = Regex("""ksp_version\s*=\s*['"]([^'"]+)['"]""").find(rootText)
        assertTrue("ksp_version declaration must exist", kspMatch != null)
        assertEquals("ksp_version must be pinned", expectedKspVersion, kspMatch!!.groupValues[1])
        assertTrue(
            "KSP plugin must use the shared ksp_version",
            rootText.contains("com.google.devtools.ksp:symbol-processing-gradle-plugin:\$ksp_version")
        )

        val appBuildFile = File("build.gradle")
        assertTrue("Expected to locate app/build.gradle", appBuildFile.isFile)
        val appText = appBuildFile.readText()

        val moshiMatch = Regex("""moshi_version\s*=\s*['"]([^'"]+)['"]""").find(appText)
        assertTrue("moshi_version declaration must exist", moshiMatch != null)
        assertEquals("moshi_version must be the accepted baseline", expectedMoshiVersion, moshiMatch!!.groupValues[1])

        assertTrue(
            "Moshi codegen must run on KSP",
            appText.contains("ksp \"com.squareup.moshi:moshi-kotlin-codegen:\$moshi_version\"")
        )
        assertFalse(
            "Moshi codegen must not run on KAPT",
            appText.contains("kapt \"com.squareup.moshi:moshi-kotlin-codegen")
        )
        assertTrue(
            "Room compiler must remain on KAPT",
            appText.contains("kapt \"androidx.room:room-compiler:\$room_version\"")
        )
        assertTrue("kotlin-kapt plugin must remain applied", appText.contains("apply plugin: 'kotlin-kapt'"))
        assertTrue(
            "KSP plugin must be applied",
            appText.contains("apply plugin: 'com.google.devtools.ksp'")
        )
    }
}
