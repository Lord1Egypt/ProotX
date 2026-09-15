package io.github.lord1egypt.prootx.architecture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * P1E6 guard: the obsolete legacy broad-storage permission dependency must not return to
 * production source or to the source manifests.
 *
 * Forbidden (active code/manifests):
 *  - `PermissionHandler` (the removed READ/WRITE_EXTERNAL_STORAGE gate/dialog)
 *  - `READ_EXTERNAL_STORAGE` / `WRITE_EXTERNAL_STORAGE` / `MANAGE_EXTERNAL_STORAGE`
 *  - `READ_MEDIA_*`
 *
 * Permitted (current approach):
 *  - app-private/app-scoped storage (`filesDir`, `getExternalFilesDir(s)`)
 *  - Storage Access Framework (`ACTION_OPEN_DOCUMENT` / `ACTION_CREATE_DOCUMENT`)
 *
 * Historical references in documentation and tests are out of scope: this guard inspects
 * production source (`app/src/main`, `termux-app/terminal-term/src/main`) and the source
 * manifests.
 */
class StoragePermissionGuardTest {

    private val forbiddenSourcePatterns = listOf(
        "PermissionHandler",
        "READ_EXTERNAL_STORAGE",
        "WRITE_EXTERNAL_STORAGE",
        "MANAGE_EXTERNAL_STORAGE",
        "READ_MEDIA_"
    )

    private val forbiddenManifestPatterns = listOf(
        "android.permission.READ_EXTERNAL_STORAGE",
        "android.permission.WRITE_EXTERNAL_STORAGE",
        "android.permission.MANAGE_EXTERNAL_STORAGE",
        "android.permission.READ_MEDIA_"
    )

    private val sourceRoots = listOf(
        File("src/main/java"),
        File("../termux-app/terminal-term/src/main/java")
    )

    private val manifestFiles = listOf(
        File("src/main/AndroidManifest.xml"),
        File("../termux-app/terminal-term/src/main/AndroidManifest.xml")
    )

    @Test
    fun `production source contains no legacy storage permission dependency`() {
        val offenders = mutableListOf<String>()

        var scannedSources = 0
        var scannedRoots = 0
        sourceRoots.filter { it.isDirectory }.forEach { root ->
            scannedRoots++
            root.walkTopDown()
                .filter { it.isFile && (it.extension == "kt" || it.extension == "java") }
                .forEach { file ->
                    scannedSources++
                    file.readLines().forEachIndexed { index, line ->
                        if (forbiddenSourcePatterns.any { line.contains(it) }) {
                            offenders.add("${file.relativeTo(root)}:${index + 1}: ${line.trim()}")
                        }
                    }
                }
        }

        assertTrue("Expected to scan at least one production source root", scannedRoots > 0)
        assertTrue("Expected to scan production sources", scannedSources > 0)

        assertEquals(
            "Legacy storage permission dependency is forbidden in production source:\n" +
                offenders.joinToString("\n"),
            0,
            offenders.size
        )
    }

    @Test
    fun `source manifests declare no legacy broad-storage permission`() {
        val offenders = mutableListOf<String>()

        var scannedManifests = 0
        manifestFiles.filter { it.isFile }.forEach { file ->
            scannedManifests++
            file.readLines().forEachIndexed { index, line ->
                if (forbiddenManifestPatterns.any { line.contains(it) }) {
                    offenders.add("${file.path}:${index + 1}: ${line.trim()}")
                }
            }
        }

        assertTrue("Expected to locate source manifests", scannedManifests > 0)

        assertEquals(
            "Legacy broad-storage permission declarations are forbidden:\n" +
                offenders.joinToString("\n"),
            0,
            offenders.size
        )
    }
}
