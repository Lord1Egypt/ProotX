package io.github.lord1egypt.prootx.model.daos

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.InstrumentationRegistry
import androidx.test.filters.SmallTest
import org.junit.After
import org.junit.Assert.* // ktlint-disable no-wildcard-imports
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import io.github.lord1egypt.prootx.androidTestHelpers.blockingObserve
import io.github.lord1egypt.prootx.model.entities.App
import io.github.lord1egypt.prootx.model.repositories.ProotXDatabase

@SmallTest
class AppsDaoTest {

    @get:Rule
    val instantExectutorRule = InstantTaskExecutorRule()

    private lateinit var db: ProotXDatabase

    @Before
    fun initDb() {
        db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(),
                ProotXDatabase::class.java)
                .allowMainThreadQueries()
                .build()
    }

    @After
    fun closeDb() = db.close()

    @Test
    fun insertApplicationAndGetByName() {
        val inserted = App(name = DEFAULT_NAME)

        db.appsDao().insertApp(inserted)
        val retrieved = db.appsDao().getAppByName(DEFAULT_NAME)

        assertNotNull(retrieved)
        assertEquals(inserted, retrieved)
    }

    @Test
    fun dbApplicationIsReplacedOnConflict() {
        val app1 = App(name = "test", category = "")
        val app2 = App(name = "test", category = "test")
        db.appsDao().insertApp(app1)
        db.appsDao().insertApp(app2)

        val retrieved = db.appsDao().getAllApps().blockingObserve()!!

        assertTrue(retrieved.contains(app2))
        assertFalse(retrieved.contains(app1))
    }

    companion object {
        val DEFAULT_NAME = "test"
    }
}