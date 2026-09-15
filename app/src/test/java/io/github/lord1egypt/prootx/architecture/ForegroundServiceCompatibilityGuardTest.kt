package io.github.lord1egypt.prootx.architecture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * P1E7 guard: the two real foreground services must stay structurally compatible with the
 * Android 12–16 foreground-service rules while the persistent app targetSdk stays 30.
 *
 * Invariants:
 *  - app manifest declares `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_SPECIAL_USE`
 *  - `POST_NOTIFICATIONS` is intentionally deferred to P1E8 (must be absent)
 *  - both services declare `android:foregroundServiceType="specialUse"` with a
 *    `PROPERTY_SPECIAL_USE_FGS_SUBTYPE` property describing the real user-facing reason
 *  - `TermuxService` is declared once, via the API-36 app manifest overlay (the terminal
 *    module keeps compiling against API 29)
 *  - both services create their own notification channel
 *  - the initial FGS launch paths use `startForegroundService` on API 26+
 *  - foreground promotion uses `FOREGROUND_SERVICE_TYPE_MANIFEST` on API 29+
 *  - terminal module SDK levels are unchanged (29/29/21)
 *
 * This guard inspects production source and the source manifests only; historical
 * documentation references are out of scope.
 */
class ForegroundServiceCompatibilityGuardTest {

    private val appManifest = File("src/main/AndroidManifest.xml")
    private val terminalManifest = File("../termux-app/terminal-term/src/main/AndroidManifest.xml")
    private val serverService = File("src/main/java/io/github/lord1egypt/prootx/ServerService.kt")
    private val mainActivity = File("src/main/java/io/github/lord1egypt/prootx/MainActivity.kt")
    private val termuxActivity = File("../termux-app/terminal-term/src/main/java/com/termux/app/TermuxActivity.java")
    private val termuxService = File("../termux-app/terminal-term/src/main/java/com/termux/app/TermuxService.java")
    private val terminalBuild = File("../termux-app/terminal-term/build.gradle")

    private fun serviceElement(manifestText: String, name: String): String {
        val match = Regex("""<service\s+android:name="$name"[\s\S]*?</service>""").find(manifestText)
        assertTrue("Expected a <service android:name=\"$name\"> element", match != null)
        return match!!.value
    }

    @Test
    fun `app manifest declares foreground service permissions and defers POST_NOTIFICATIONS`() {
        assertTrue("Expected to locate the app manifest", appManifest.isFile)
        val text = appManifest.readText()

        assertTrue(
            "FOREGROUND_SERVICE must be declared",
            text.contains("android.permission.FOREGROUND_SERVICE\"")
        )
        assertTrue(
            "FOREGROUND_SERVICE_SPECIAL_USE must be declared",
            text.contains("android.permission.FOREGROUND_SERVICE_SPECIAL_USE\"")
        )
        assertFalse(
            "POST_NOTIFICATIONS is intentionally deferred to P1E8 and must be absent in P1E7",
            text.contains("POST_NOTIFICATIONS")
        )
    }

    @Test
    fun `ServerService declares specialUse type plus subtype metadata`() {
        val element = serviceElement(appManifest.readText(), ".ServerService")

        assertTrue(
            "ServerService must declare the specialUse foreground service type",
            element.contains("android:foregroundServiceType=\"specialUse\"")
        )
        assertTrue(
            "ServerService must keep stopWithTask",
            element.contains("android:stopWithTask=\"true\"")
        )
        assertTrue(
            "ServerService must declare a special-use subtype property",
            element.contains("android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE")
        )
        assertTrue(
            "ServerService subtype must describe local Linux sessions",
            element.contains("Runs user-initiated local Linux sessions")
        )
    }

    @Test
    fun `TermuxService is overlaid once with specialUse type from the API-36 app manifest`() {
        val appText = appManifest.readText()
        val element = serviceElement(appText, "com.termux.app.TermuxService")

        assertTrue(
            "TermuxService overlay must keep exported=false",
            element.contains("android:exported=\"false\"")
        )
        assertTrue(
            "TermuxService overlay must declare the specialUse foreground service type",
            element.contains("android:foregroundServiceType=\"specialUse\"")
        )
        assertTrue(
            "TermuxService overlay must declare a special-use subtype property",
            element.contains("android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE")
        )
        assertTrue(
            "TermuxService subtype must describe interactive terminal sessions",
            element.contains("Keeps user-initiated interactive terminal sessions")
        )

        val occurrences = Regex(Regex.escape("android:name=\"com.termux.app.TermuxService\""))
            .findAll(appText)
            .count()
        assertEquals("TermuxService must be overlaid exactly once", 1, occurrences)
    }

    @Test
    fun `terminal manifest does not declare the newer FGS type`() {
        assertTrue("Expected to locate the terminal manifest", terminalManifest.isFile)
        val text = terminalManifest.readText()

        assertFalse(
            "foregroundServiceType must not be declared in the API-29 terminal manifest",
            text.contains("foregroundServiceType")
        )
        assertFalse(
            "specialUse must not appear in the API-29 terminal manifest",
            text.contains("specialUse")
        )
    }

    @Test
    fun `ServerService creates its own channel and promotes via manifest type`() {
        assertTrue("Expected to locate ServerService.kt", serverService.isFile)
        val text = serverService.readText()

        assertTrue(
            "ServerService.onCreate must create its foreground notification channel",
            text.contains("notificationManager.createServiceNotificationChannel()")
        )
        assertTrue(
            "ServerService must promote to foreground synchronously",
            text.contains("promoteToForeground()")
        )
        assertTrue(
            "ServerService must use the manifest-declared type on API 29+",
            text.contains("ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST")
        )
        assertFalse(
            "ServerService must not hard-code the specialUse type constant",
            text.contains("FOREGROUND_SERVICE_TYPE_SPECIAL_USE")
        )
    }

    @Test
    fun `initial foreground service launch paths use startForegroundService on API 26+`() {
        assertTrue("Expected to locate MainActivity.kt", mainActivity.isFile)
        val mainText = mainActivity.readText()
        assertTrue(
            "MainActivity must launch the session foreground service with startForegroundService",
            mainText.contains("startForegroundService(serviceIntent)")
        )
        assertTrue(
            "MainActivity must gate startForegroundService behind API 26",
            mainText.contains("Build.VERSION_CODES.O")
        )
        assertFalse(
            "MainActivity must not own the service notification channel any more",
            mainText.contains("createServiceNotificationChannel")
        )

        assertTrue("Expected to locate TermuxActivity.java", termuxActivity.isFile)
        val termuxText = termuxActivity.readText()
        assertTrue(
            "TermuxActivity must launch TermuxService with startForegroundService",
            termuxText.contains("startForegroundService(serviceIntent)")
        )
        assertTrue(
            "TermuxActivity must retain binding after the start",
            termuxText.contains("doBindService(serviceIntent)")
        )
    }

    @Test
    fun `TermuxService creates its own channel and uses the manifest type`() {
        assertTrue("Expected to locate TermuxService.java", termuxService.isFile)
        val text = termuxService.readText()

        assertTrue(
            "TermuxService must create the ProotX notification channel itself",
            text.contains("createNotificationChannel()")
        )
        assertTrue(
            "TermuxService must keep the existing ProotX channel id",
            text.contains("\"ProotX\"")
        )
        assertTrue(
            "TermuxService must use the manifest-declared type on API 29+",
            text.contains("ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST")
        )
        assertFalse(
            "TermuxService must not reference the specialUse constant from API-29 source",
            text.contains("FOREGROUND_SERVICE_TYPE_SPECIAL_USE")
        )
    }

    @Test
    fun `terminal module SDK levels are unchanged`() {
        assertTrue("Expected to locate terminal-term/build.gradle", terminalBuild.isFile)
        val text = terminalBuild.readText()

        assertTrue("terminal-term compileSdk must remain 29", text.contains("compileSdkVersion 29"))
        assertTrue("terminal-term targetSdk must remain 29", text.contains("targetSdkVersion 29"))
        assertTrue("terminal-term minSdk must remain 21", text.contains("minSdkVersion 21"))
    }
}
