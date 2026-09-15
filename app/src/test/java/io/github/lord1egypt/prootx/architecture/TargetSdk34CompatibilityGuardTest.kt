package io.github.lord1egypt.prootx.architecture

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * P1E8 guard: the accepted targetSdk 34 runtime behavior must not silently regress.
 *
 * Invariants:
 *  - app SDK matrix is compileSdk 36 / targetSdk 34 / minSdk 21
 *  - terminal-term compiles against API 36 while targetSdk stays 29 / minSdk 21
 *  - `POST_NOTIFICATIONS` is declared; no legacy broad-storage permission returns
 *  - `MainActivity` requests the notification permission contextually, keeps a pending session,
 *    gates the FGS launch on a resumed lifecycle, and catches only
 *    `ForegroundServiceStartNotAllowedException` around the launch
 *  - `TermuxActivity` participates in the same one-time prompt policy and uses
 *    `Context.RECEIVER_NOT_EXPORTED` on API 33+ with the legacy path retained
 *  - `MainActivity`'s system DownloadManager receiver keeps the flag-less registration
 *
 * This guard inspects production source/build files only; documentation references are out of
 * scope. Assertions avoid line-number/whitespace coupling.
 */
class TargetSdk34CompatibilityGuardTest {

    private val appBuild = File("build.gradle")
    private val appManifest = File("src/main/AndroidManifest.xml")
    private val mainActivity = File("src/main/java/io/github/lord1egypt/prootx/MainActivity.kt")
    private val terminalBuild = File("../termux-app/terminal-term/build.gradle")
    private val termuxActivity = File("../termux-app/terminal-term/src/main/java/com/termux/app/TermuxActivity.java")

    @Test
    fun `app SDK matrix is compileSdk 36 targetSdk 34 minSdk 21`() {
        assertTrue("Expected to locate app/build.gradle", appBuild.isFile)
        val text = appBuild.readText()

        assertTrue("app compileSdk must be 36", text.contains("compileSdkVersion 36"))
        assertTrue("app targetSdk must be 34", text.contains("targetSdkVersion 34"))
        assertTrue("app minSdk must be 21", text.contains("minSdkVersion 21"))
    }

    @Test
    fun `terminal-term compiles against API 36 with targetSdk 29 and minSdk 21`() {
        assertTrue("Expected to locate terminal-term/build.gradle", terminalBuild.isFile)
        val text = terminalBuild.readText()

        assertTrue("terminal-term compileSdk must be 36", text.contains("compileSdkVersion 36"))
        assertTrue("terminal-term targetSdk must remain 29", text.contains("targetSdkVersion 29"))
        assertTrue("terminal-term minSdk must remain 21", text.contains("minSdkVersion 21"))
    }

    @Test
    fun `notification permission is declared and no legacy storage permission returns`() {
        assertTrue("Expected to locate the app manifest", appManifest.isFile)
        val text = appManifest.readText()

        assertTrue(
            "POST_NOTIFICATIONS must be declared",
            text.contains("android.permission.POST_NOTIFICATIONS\"")
        )
        listOf(
            "READ_EXTERNAL_STORAGE",
            "WRITE_EXTERNAL_STORAGE",
            "MANAGE_EXTERNAL_STORAGE",
            "READ_MEDIA_"
        ).forEach { forbidden ->
            assertFalse(
                "$forbidden must not be declared",
                text.contains(forbidden)
            )
        }
    }

    @Test
    fun `MainActivity presents the notification permission contextually at session start`() {
        assertTrue("Expected to locate MainActivity.kt", mainActivity.isFile)
        val text = mainActivity.readText()

        assertTrue("notification permission must be requested", text.contains("POST_NOTIFICATIONS"))
        assertTrue("permission result must be handled", text.contains("onRequestPermissionsResult"))
        assertTrue("a dedicated request code must exist", text.contains("NOTIFICATION_PERMISSION_REQUEST_CODE"))
        assertTrue("current grant state must come from checkSelfPermission", text.contains("checkSelfPermission"))
        assertTrue(
            "the shared prefs name must be the agreed one",
            text.contains("\"notification_permission\"")
        )
        assertTrue(
            "the shared prompt key must be the agreed one",
            text.contains("\"prompt_completed\"")
        )
        assertFalse(
            "the prompt must not be shown merely because the app opened",
            text.contains("createServiceNotificationChannel")
        )
    }

    @Test
    fun `MainActivity keeps a pending session and gates the launch on a resumed lifecycle`() {
        val text = mainActivity.readText()

        assertTrue("a pending session must be retained", text.contains("pendingSession"))
        assertTrue("a deferred continuation path must exist", text.contains("continuePendingSessionIfPossible"))
        assertTrue(
            "the FGS gate must use the AndroidX lifecycle",
            text.contains("Lifecycle.State.RESUMED")
        )
        assertTrue(
            "the notification dialog in-flight state must be tracked",
            text.contains("notificationPermissionRequestInFlight")
        )
    }

    @Test
    fun `the FGS launch catches only ForegroundServiceStartNotAllowedException`() {
        val text = mainActivity.readText()
        val match = Regex("""private fun launchForegroundService\b[\s\S]*?\n    \}""").find(text)
        assertTrue("Expected a launchForegroundService function", match != null)
        val body = match!!.value

        assertTrue(
            "launchForegroundService must handle the target-31 FGS start race",
            body.contains("ForegroundServiceStartNotAllowedException")
        )
        assertFalse(
            "launchForegroundService must not swallow a broad Exception",
            body.contains("catch (e: Exception)")
        )
        assertFalse(
            "launchForegroundService must not swallow Throwable",
            body.contains("Throwable")
        )
    }

    @Test
    fun `notification denial continues the session rather than blocking it`() {
        val text = mainActivity.readText()

        // The permission result handler must always continue the pending session, and the grant
        // state must never gate the launch itself.
        val callback = Regex("""override fun onRequestPermissionsResult\b[\s\S]*?\n    \}""").find(text)
        assertTrue("Expected onRequestPermissionsResult", callback != null)
        assertTrue(
            "onRequestPermissionsResult must continue the pending session",
            callback!!.value.contains("continuePendingSessionIfPossible()")
        )
        assertFalse(
            "grant state must not become a launch precondition",
            text.contains("if (!notificationPermissionIsGranted()) return")
        )
    }

    @Test
    fun `TermuxActivity participates in the shared notification permission policy`() {
        assertTrue("Expected to locate TermuxActivity.java", termuxActivity.isFile)
        val text = termuxActivity.readText()

        assertTrue("Termux must request POST_NOTIFICATIONS", text.contains("POST_NOTIFICATIONS"))
        assertTrue("Termux must handle the permission result", text.contains("onRequestPermissionsResult"))
        assertTrue(
            "Termux must use the shared prefs name",
            text.contains("\"notification_permission\"")
        )
        assertTrue(
            "Termux must use the shared prompt key",
            text.contains("\"prompt_completed\"")
        )
        assertTrue(
            "Termux must defer the service start until the permission flow completes",
            text.contains("startAndBindService")
        )
        assertTrue(
            "Termux must start/bind the service exactly once",
            text.contains("mServiceStarted")
        )
    }

    @Test
    fun `Termux custom receiver uses the direct API-33 not-exported constant`() {
        val text = termuxActivity.readText()

        assertTrue(
            "the custom receiver must use Context.RECEIVER_NOT_EXPORTED on API 33+",
            text.contains("Context.RECEIVER_NOT_EXPORTED")
        )
        assertTrue(
            "the flag must be gated on TIRAMISU",
            text.contains("Build.VERSION_CODES.TIRAMISU")
        )
        assertTrue(
            "the pre-33 two-argument registration must remain",
            text.contains("registerReceiver(mBroadcastReceiever, new IntentFilter(RELOAD_STYLE_ACTION));")
        )
        assertFalse(
            "the receiver must not be exported",
            text.contains("RECEIVER_EXPORTED")
        )
        assertFalse(
            "no magic receiver-flag integer may be used",
            text.contains("0x00000004")
        )
    }

    @Test
    fun `MainActivity system DownloadManager receiver keeps the flag-less registration`() {
        val text = mainActivity.readText()

        assertTrue(
            "the system broadcast receiver must keep the two-argument registration",
            text.contains(
                "registerReceiver(downloadBroadcastReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))"
            )
        )
        assertFalse(
            "the system DownloadManager receiver must not gain an export flag",
            text.contains("RECEIVER_NOT_EXPORTED") || text.contains("RECEIVER_EXPORTED")
        )
        assertTrue(
            "the in-process LocalBroadcastManager registration stays unchanged",
            text.contains("LocalBroadcastManager")
        )
    }

    @Test
    fun `no dynamic dex or jar loading is present in production source`() {
        val forbidden = listOf("DexClassLoader", "PathClassLoader", "InMemoryDexClassLoader")
        listOf(
            File("src/main/java"),
            File("../termux-app/terminal-term/src/main/java")
        ).filter { it.isDirectory }.forEach { root ->
            root.walkTopDown()
                .filter { it.isFile && (it.extension == "kt" || it.extension == "java") }
                .forEach { file ->
                    val text = file.readText()
                    val fileName = file.name
                    forbidden.forEach { api ->
                        assertFalse(
                            "$api must not be used ($fileName)",
                            text.contains(api)
                        )
                    }
                }
        }
    }
}
