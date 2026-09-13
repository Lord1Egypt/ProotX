package io.github.lord1egypt.prootx.architecture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * P1D2 guard: the obsolete direct Play Services base dependency and its stale
 * `ENABLE_PLAY_SERVICES` BuildConfig flag must not return.
 *
 * This targets only that specific dependency/flag — it does not ban Google Play Services or
 * gms artifacts generally (a legitimate transitive may exist later).
 */
class DeadPlayServicesGuardTest {

    private val forbiddenBuildPatterns = listOf(
        "com.google.android.gms:play-services-base",
        "ENABLE_PLAY_SERVICES"
    )

    private val forbiddenSourcePatterns = listOf(
        "ENABLE_PLAY_SERVICES",
        "com.google.android.gms"
    )

    @Test
    fun `dead play-services-base dependency and config flag stay removed`() {
        val offenders = mutableListOf<String>()

        val appBuildFile = File("build.gradle")
        assertTrue("Expected to locate app/build.gradle", appBuildFile.isFile)
        appBuildFile.readLines().forEachIndexed { index, line ->
            if (forbiddenBuildPatterns.any { line.contains(it) }) {
                offenders.add("build.gradle:${index + 1}: ${line.trim()}")
            }
        }

        val sourceRoot = listOf(File("src/main/java"), File("app/src/main/java"))
            .firstOrNull { it.isDirectory }
            ?: error("Production source root not found from ${File(".").absolutePath}")
        var scanned = 0
        sourceRoot.walkTopDown()
            .filter { it.isFile && (it.extension == "kt" || it.extension == "java") }
            .forEach { file ->
                scanned++
                file.readLines().forEachIndexed { index, line ->
                    if (forbiddenSourcePatterns.any { line.contains(it) }) {
                        offenders.add("${file.relativeTo(sourceRoot)}:${index + 1}: ${line.trim()}")
                    }
                }
            }
        assertTrue("Expected to scan production sources", scanned > 0)

        assertEquals(
            "Dead Play Services dependency/config is forbidden:\n${offenders.joinToString("\n")}",
            0,
            offenders.size
        )
    }
}
