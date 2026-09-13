package io.github.lord1egypt.prootx.architecture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * P1D3 guard: the deprecated monolithic `lifecycle-extensions` artifact and the deprecated
 * `ViewModelProviders` API must not return to active use.
 *
 * Permitted: `androidx.lifecycle:lifecycle-viewmodel` / `lifecycle-livedata` (and other
 * granular artifacts) and the `androidx.lifecycle.ViewModelProvider` API.
 * Historical documentation references are out of scope (this guard inspects build config
 * and production source only).
 */
class LifecycleModernizationGuardTest {

    private val forbiddenBuildPatterns = listOf("lifecycle-extensions")
    private val forbiddenSourcePatterns = listOf("ViewModelProviders")

    @Test
    fun `lifecycle-extensions and ViewModelProviders stay removed`() {
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
            "Deprecated Lifecycle usage is forbidden:\n${offenders.joinToString("\n")}",
            0,
            offenders.size
        )
    }
}
