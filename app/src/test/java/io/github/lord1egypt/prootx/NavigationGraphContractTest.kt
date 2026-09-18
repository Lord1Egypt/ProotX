package io.github.lord1egypt.prootx

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Regression guard for the API36 first-launch crash: `NavGraph.onInflate` calls
 * `setStartDestination(0)` when `app:startDestination` is absent, and Navigation 2.3.5 throws
 * when the graph's own id is also 0. The root therefore needs a stable non-zero id while the
 * start destination stays dynamic (set in `MainActivity.setNavStartDestination`).
 */
class NavigationGraphContractTest {

    private fun graphFile(): File {
        val candidates = listOf(
            File("src/main/res/navigation/nav_graph.xml"),
            File("app/src/main/res/navigation/nav_graph.xml")
        )
        return candidates.firstOrNull { it.isFile }
            ?: error("nav_graph.xml not found from ${File(".").absolutePath}")
    }

    @Test
    fun `navigation root declares a stable id`() {
        val text = graphFile().readText()
        assertTrue("nav_graph root must declare android:id", text.contains("android:id=\"@+id/nav_graph\""))
    }

    @Test
    fun `start destination stays dynamic`() {
        val text = graphFile().readText()
        assertFalse("nav_graph must not pin a static app:startDestination", text.contains("app:startDestination"))
    }
}
