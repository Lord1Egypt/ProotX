package io.github.lord1egypt.prootx

import android.content.Context
import androidx.navigation.NavController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Runtime guard for the API36 first-launch crash: the dynamically-inflated navigation graph
 * must carry a non-zero id and accept both dynamic start destinations without
 * `IllegalArgumentException`.
 */
@RunWith(AndroidJUnit4::class)
@SmallTest
class NavigationGraphInstrumentedTest {

    @Test
    fun dynamicStartDestinationsInflateWithoutError() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val navController = NavController(context)

        val graph = navController.navInflater.inflate(R.navigation.nav_graph)
        assertNotEquals(0, graph.id)

        graph.startDestination = R.id.app_list_fragment
        assertEquals(R.id.app_list_fragment, graph.startDestination)

        graph.startDestination = R.id.session_list_fragment
        assertEquals(R.id.session_list_fragment, graph.startDestination)
    }
}
