package io.github.lord1egypt.prootx.architecture

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * P1E9 guard: Android 15 edge-to-edge and Android 16 adaptive / predictive-back behavior must not
 * silently regress into an opt-out.
 *
 * Invariants:
 *  - no `windowOptOutEdgeToEdgeEnforcement` and no predictive-back opt-out anywhere
 *  - `MainActivity` enables edge-to-edge and applies real WindowInsets per owner (toolbar top,
 *    bottom navigation bottom, root left/right cutout) without accumulating padding
 *  - `TermuxActivity` dispatches back through the platform `OnBackInvokedDispatcher` on API 33+
 *    with the legacy `onBackPressed` fallback retained
 *  - `:app` keeps the accepted `androidx.activity:activity-ktx:1.11.0` bridge and minSdk 21
 *  - no orientation lock, no aspect-ratio restriction, no large-screen opt-out
 *  - P1E7/P1E8 invariants (specialUse FGS, POST_NOTIFICATIONS, no storage permissions) remain
 *
 * This guard inspects production source/build/manifest files only; assertions avoid
 * line-number/whitespace coupling.
 */
class TargetSdk36PlatformBehaviorGuardTest {

    private val appBuild = File("build.gradle")
    private val appManifest = File("src/main/AndroidManifest.xml")
    private val mainActivity = File("src/main/java/io/github/lord1egypt/prootx/MainActivity.kt")
    private val terminalManifest = File("../termux-app/terminal-term/src/main/AndroidManifest.xml")
    private val termuxActivity = File("../termux-app/terminal-term/src/main/java/com/termux/app/TermuxActivity.java")

    @Test
    fun `no edge-to-edge or predictive-back opt-out exists`() {
        listOf(appManifest, terminalManifest).forEach { manifest ->
            assertTrue("Expected to locate ${manifest.path}", manifest.isFile)
            val text = manifest.readText()
            assertFalse(
                "windowOptOutEdgeToEdgeEnforcement must not be used",
                text.contains("windowOptOutEdgeToEdgeEnforcement")
            )
            assertFalse(
                "predictive back must never be disabled",
                text.contains("enableOnBackInvokedCallback=\"false\"")
            )
        }
    }

    @Test
    fun `MainActivity enables edge-to-edge and applies insets per owner`() {
        assertTrue("Expected to locate MainActivity.kt", mainActivity.isFile)
        val text = mainActivity.readText()

        assertTrue("MainActivity must enable edge-to-edge", text.contains("enableEdgeToEdge()"))
        assertTrue("insets must be applied", text.contains("ViewCompat.setOnApplyWindowInsetsListener"))
        assertTrue("system bars insets must be read", text.contains("WindowInsetsCompat.Type.systemBars()"))
        assertTrue("display cutout insets must be read", text.contains("WindowInsetsCompat.Type.displayCutout()"))
        assertTrue(
            "the toolbar must own the top inset",
            text.contains("toolbar.updatePadding(top =")
        )
        assertTrue(
            "the bottom navigation must own the bottom inset",
            text.contains("bottomNav.updatePadding(bottom =")
        )
        assertTrue(
            "the root must own the left/right cutout safety",
            text.contains("root.setPadding(")
        )
        assertFalse(
            "MainActivity must not opt out of edge-to-edge",
            text.contains("windowOptOutEdgeToEdgeEnforcement")
        )
    }

    @Test
    fun `MainActivity insets are not cumulative`() {
        val text = mainActivity.readText()

        // The original padding must be captured once and combined with the current inset.
        val match = Regex("""private fun applyEdgeToEdgeInsets\b[\s\S]*?\n    \}""").find(text)
        assertTrue("Expected an applyEdgeToEdgeInsets function", match != null)
        val body = match!!.value

        assertTrue("original root padding must be captured", body.contains("val rootPaddingLeft ="))
        assertTrue("original toolbar padding must be captured", body.contains("val toolbarPaddingTop ="))
        assertTrue("original bottom nav padding must be captured", body.contains("val bottomNavPaddingBottom ="))
        assertFalse(
            "padding must not be accumulated with +=",
            body.contains("paddingLeft +=") || body.contains("paddingTop +=")
        )
    }

    @Test
    fun `activity bridge is the accepted version`() {
        assertTrue("Expected to locate app/build.gradle", appBuild.isFile)
        val text = appBuild.readText()

        assertTrue(
            "the accepted activity version must be pinned",
            text.contains("def activity_version = '1.11.0'")
        )
        assertTrue(
            "activity-ktx must be declared on the app",
            text.contains("implementation \"androidx.activity:activity-ktx:\$activity_version\"")
        )
    }

    @Test
    fun `TermuxActivity dispatches back through the platform on API 33 plus`() {
        assertTrue("Expected to locate TermuxActivity.java", termuxActivity.isFile)
        val text = termuxActivity.readText()

        assertTrue(
            "Termux must use the platform OnBackInvokedDispatcher",
            text.contains("getOnBackInvokedDispatcher()")
        )
        assertTrue(
            "Termux must register an OnBackInvokedCallback",
            text.contains("OnBackInvokedCallback")
        )
        assertTrue(
            "the callback must be gated on TIRAMISU",
            text.contains("Build.VERSION_CODES.TIRAMISU")
        )
        assertTrue(
            "the pre-33 onBackPressed fallback must remain",
            text.contains("onBackPressed()")
        )
        assertTrue(
            "the drawer/root back logic must be shared",
            text.contains("handleBackPressed()")
        )
        assertTrue(
            "Termux must not become an AppCompat activity",
            text.contains("public final class TermuxActivity extends Activity")
        )
    }

    @Test
    fun `no orientation lock or large-screen opt-out exists`() {
        val productionRoots = listOf(
            File("src/main"),
            File("../termux-app/terminal-term/src/main")
        )
        val forbidden = listOf(
            "screenOrientation",
            "setRequestedOrientation",
            "minAspectRatio",
            "maxAspectRatio",
            "windowOptOutEdgeToEdgeEnforcement",
            "PROPERTY_COMPAT_ALLOW_RESTRICTED_RESIZABILITY"
        )
        productionRoots.filter { it.isDirectory }.forEach { root ->
            root.walkTopDown()
                .filter { it.isFile && it.extension in setOf("xml", "kt", "java") }
                .forEach { file ->
                    val text = file.readText()
                    forbidden.forEach { token ->
                        assertFalse(
                            "$token must not be used (${file.name})",
                            text.contains(token)
                        )
                    }
                }
        }
    }

    @Test
    fun `P1E7 and P1E8 platform invariants remain`() {
        val manifestText = appManifest.readText()
        assertTrue(
            "POST_NOTIFICATIONS must remain declared",
            manifestText.contains("android.permission.POST_NOTIFICATIONS\"")
        )
        assertTrue(
            "FOREGROUND_SERVICE_SPECIAL_USE must remain declared",
            manifestText.contains("android.permission.FOREGROUND_SERVICE_SPECIAL_USE\"")
        )
        assertTrue(
            "the specialUse FGS type must remain on both services",
            Regex("foregroundServiceType=\"specialUse\"").findAll(manifestText).count() >= 2
        )
    }
}
