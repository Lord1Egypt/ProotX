package io.github.lord1egypt.prootx.support

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Guards the app-side support release pin. The lock must name support v1.2.0 and every
 * release asset must carry a 64-hex SHA-256; the Gradle pipeline verifies the actual
 * download against these values.
 */
class SupportReleaseLockTest {

    private fun lockFile(): File {
        val candidates = listOf(File("support-release.lock.json"), File("app/support-release.lock.json"))
        return candidates.firstOrNull { it.isFile }
            ?: error("support-release.lock.json not found from ${File(".").absolutePath}")
    }

    private fun parse(): Map<String, Any> {
        val type = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
        val json = lockFile().readText()
        return Moshi.Builder().build().adapter<Map<String, Any>>(type).fromJson(json)!!
    }

    @Test
    fun `pins support v1_2_0`() {
        val lock = parse()
        assertEquals(1, (lock["schemaVersion"] as Number).toInt())
        assertEquals("v1.2.0", lock["release"])
        assertEquals("RE_kwDOUXjkQM4XTxeE", lock["releaseId"])
    }

    @Test
    fun `declares the four supported ABIs`() {
        val abis = parse()["supportedAbis"] as List<*>
        assertEquals(listOf("arm64-v8a", "armeabi-v7a", "x86_64", "x86"), abis)
    }

    @Test
    fun `every pinned asset carries a 64-hex sha256`() {
        val assets = parse()["assets"] as Map<*, *>
        assertEquals(8, assets.size)
        val required = setOf(
            "arm64-v8a-assets.zip", "armeabi-v7a-assets.zip", "x86-assets.zip", "x86_64-assets.zip",
            "routing.json", "SHA256SUMS", "v1.2.0-provenance.json", "v1.2.0.spdx.json"
        )
        assertEquals(required, assets.keys)
        assets.values.forEach { digest ->
            assertTrue("$digest is not a 64-hex sha256", digest is String && Regex("[0-9a-f]{64}").matches(digest))
        }
    }
}
