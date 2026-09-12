package io.github.lord1egypt.prootx.architecture

import android.os.Parcelable
import io.github.lord1egypt.prootx.model.entities.App
import io.github.lord1egypt.prootx.model.entities.Filesystem
import io.github.lord1egypt.prootx.model.entities.Session
import io.github.lord1egypt.prootx.model.entities.ServiceType
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Modifier

/**
 * P1C2-R contract check: the `kotlin-parcelize` plugin must still generate the Parcelable
 * machinery for every model type that the legacy Android Extensions Parcelize generator
 * used to handle.
 *
 * A full Parcel round-trip is not exercised here because the project has no Robolectric /
 * instrumentation test infrastructure; device-level Parcel validation happens at the Golden
 * Candidate gate. This test provides deterministic, dependency-free evidence that the types
 * are Parcelable and expose a static `CREATOR`.
 */
class ParcelableContractTest {

    private val parcelableTypes = listOf(
        App::class.java,
        Filesystem::class.java,
        Session::class.java,
        ServiceType.Unselected::class.java,
        ServiceType.Ssh::class.java,
        ServiceType.Vnc::class.java,
        ServiceType.Xsdl::class.java
    )

    @Test
    fun `parcelize generates a Parcelable contract for all model types`() {
        parcelableTypes.forEach { type ->
            assertTrue(
                "$type must implement android.os.Parcelable",
                Parcelable::class.java.isAssignableFrom(type)
            )
            val creator = type.getField("CREATOR")
            assertTrue(
                "$type must expose a static CREATOR",
                Modifier.isStatic(creator.modifiers)
            )
        }
    }
}
