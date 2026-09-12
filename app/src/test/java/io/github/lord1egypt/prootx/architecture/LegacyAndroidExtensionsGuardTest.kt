package io.github.lord1egypt.prootx.architecture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * P1C2-R guard: legacy Kotlin Android Extensions must not return to active production
 * source or build configuration.
 *
 * Forbidden (active code/build):
 *  - `kotlinx.android.synthetic`   (synthetic views)
 *  - `kotlinx.android.parcel`      (legacy Parcelize package)
 *  - `kotlin-android-extensions`   (legacy Gradle plugin)
 *  - `androidExtensions`           (legacy Gradle DSL block)
 *
 * Permitted (current approach):
 *  - `kotlinx.parcelize.Parcelize` and `kotlin-parcelize`
 *
 * Historical references in documentation (e.g. CHANGELOG_DEV.md / roadmap) are out of
 * scope: this guard inspects production source (`app/src/main`) and Gradle build files.
 */
class LegacyAndroidExtensionsGuardTest {

    private val forbiddenSourcePatterns = listOf(
        "kotlinx.android.synthetic",
        "kotlinx.android.parcel"
    )

    private val forbiddenBuildPatterns = listOf(
        "kotlin-android-extensions",
        "androidExtensions"
    )

    @Test
    fun `production source and build config contain no active legacy Android Extensions`() {
        val sourceRoot = findProductionSourceRoot()
        val offenders = mutableListOf<String>()

        var scannedSources = 0
        sourceRoot.walkTopDown()
            .filter { it.isFile && (it.extension == "kt" || it.extension == "java") }
            .forEach { file ->
                scannedSources++
                file.readLines().forEachIndexed { index, line ->
                    if (forbiddenSourcePatterns.any { line.contains(it) }) {
                        offenders.add("${file.relativeTo(sourceRoot)}:${index + 1}: ${line.trim()}")
                    }
                }
            }
        assertTrue("Expected to scan production sources under $sourceRoot", scannedSources > 0)

        val buildFiles = listOf(File("build.gradle"), File("../build.gradle")).filter { it.isFile }
        assertTrue("Expected to locate Gradle build files", buildFiles.isNotEmpty())
        buildFiles.forEach { file ->
            file.readLines().forEachIndexed { index, line ->
                if (forbiddenBuildPatterns.any { line.contains(it) }) {
                    offenders.add("${file.path}:${index + 1}: ${line.trim()}")
                }
            }
        }

        assertEquals(
            "Active legacy Android Extensions usage is forbidden:\n${offenders.joinToString("\n")}",
            0,
            offenders.size
        )
    }

    @Test
    fun `kotlin-parcelize is active and the migrated Parcelize package is used`() {
        val appBuildFile = File("build.gradle")
        assertTrue("Expected to locate app/build.gradle", appBuildFile.isFile)
        assertTrue(
            "kotlin-parcelize must be applied",
            appBuildFile.readText().contains("kotlin-parcelize")
        )

        var parcelizeUsages = 0
        findProductionSourceRoot().walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .forEach { file ->
                file.readLines().forEach { line ->
                    if (line.contains("kotlinx.parcelize.Parcelize")) parcelizeUsages++
                }
            }
        assertTrue("Expected kotlinx.parcelize.Parcelize usage in production", parcelizeUsages > 0)
    }

    private fun findProductionSourceRoot(): File {
        val candidates = listOf(File("src/main/java"), File("app/src/main/java"))
        return candidates.firstOrNull { it.isDirectory }
            ?: error("Production source root not found from ${File(".").absolutePath}")
    }
}
