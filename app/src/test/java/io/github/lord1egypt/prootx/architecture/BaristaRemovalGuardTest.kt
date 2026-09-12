package io.github.lord1egypt.prootx.architecture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * P1D1 guard: the obsolete JCenter-only Barista test dependency must not return.
 *
 * Scans `app/build.gradle` and `app/src/androidTest` for the Barista package or coordinate.
 * Espresso imports are unaffected.
 */
class BaristaRemovalGuardTest {

    private val forbiddenPatterns = listOf(
        "com.schibsted.spain.barista",
        "com.schibsted.spain:barista"
    )

    @Test
    fun `barista is absent from build config and androidTest sources`() {
        val offenders = mutableListOf<String>()

        val appBuildFile = File("build.gradle")
        assertTrue("Expected to locate app/build.gradle", appBuildFile.isFile)
        appBuildFile.readLines().forEachIndexed { index, line ->
            if (forbiddenPatterns.any { line.contains(it) }) {
                offenders.add("build.gradle:${index + 1}: ${line.trim()}")
            }
        }

        val androidTestRoot = File("src/androidTest")
        assertTrue("Expected to locate androidTest sources", androidTestRoot.isDirectory)
        var scanned = 0
        androidTestRoot.walkTopDown()
            .filter { it.isFile && (it.extension == "kt" || it.extension == "java") }
            .forEach { file ->
                scanned++
                file.readLines().forEachIndexed { index, line ->
                    if (forbiddenPatterns.any { line.contains(it) }) {
                        offenders.add("${file.relativeTo(androidTestRoot)}:${index + 1}: ${line.trim()}")
                    }
                }
            }
        assertTrue("Expected to scan androidTest sources", scanned > 0)

        assertEquals(
            "Barista usage/dependency is forbidden:\n${offenders.joinToString("\n")}",
            0,
            offenders.size
        )
    }
}
