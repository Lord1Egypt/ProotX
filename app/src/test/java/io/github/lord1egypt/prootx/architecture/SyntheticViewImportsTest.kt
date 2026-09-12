package io.github.lord1egypt.prootx.architecture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Source guard for the P1C1 view-binding migration.
 *
 * Synthetic view access (`kotlinx.android.synthetic...`) must never be reintroduced.
 * Legacy Parcelize (`kotlinx.android.parcel.Parcelize`) is intentionally still permitted
 * until P1C2 removes `kotlin-android-extensions` entirely.
 */
class SyntheticViewImportsTest {

    private val forbiddenSyntheticViewImport = "kotlinx.android.synthetic"
    private val legacyParcelizeImport = "kotlinx.android.parcel.Parcelize"

    @Test
    fun `production source contains no synthetic view imports`() {
        val sourceRoot = findProductionSourceRoot()

        val offenders = mutableListOf<String>()
        var scannedFiles = 0

        sourceRoot.walkTopDown()
            .filter { it.isFile && (it.extension == "kt" || it.extension == "java") }
            .forEach { file ->
                scannedFiles++
                file.readLines().forEachIndexed { index, line ->
                    if (line.isForbiddenSyntheticViewImport()) {
                        offenders.add("${file.relativeTo(sourceRoot)}:${index + 1}: ${line.trim()}")
                    }
                }
            }

        assertTrue("Expected to scan production sources under $sourceRoot", scannedFiles > 0)
        assertEquals(
            "Synthetic view imports are forbidden:\n${offenders.joinToString("\n")}",
            0,
            offenders.size
        )

        // The gate must distinguish synthetic views (forbidden) from legacy Parcelize (permitted).
        assertTrue("import kotlinx.android.synthetic.main.frag_help.*".isForbiddenSyntheticViewImport())
        assertFalse("import $legacyParcelizeImport".isForbiddenSyntheticViewImport())
    }

    private fun String.isForbiddenSyntheticViewImport(): Boolean =
        contains(forbiddenSyntheticViewImport)

    private fun findProductionSourceRoot(): File {
        val candidates = listOf(File("src/main/java"), File("app/src/main/java"))
        return candidates.firstOrNull { it.isDirectory }
            ?: error("Production source root not found from ${File(".").absolutePath}")
    }
}
