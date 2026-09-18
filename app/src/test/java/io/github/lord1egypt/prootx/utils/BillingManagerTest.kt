package io.github.lord1egypt.prootx.utils

import android.app.Activity
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mockStatic

class BillingManagerTest {

    /** android.util.Log is a throwing stub on the JVM. */
    private inline fun <T> withLog(block: () -> T): T =
        mockStatic(Log::class.java).use { block() }

    private fun result(code: Int): BillingResult =
        BillingResult.newBuilder().setResponseCode(code).build()

    private fun manager(
        client: BillingClient,
        onSubs: (List<Purchase>) -> Unit = {},
        onInApp: (List<Purchase>) -> Unit = {},
        onSupported: (Boolean) -> Unit = {}
    ) = BillingManager(
        activity = mock<Activity>(),
        onEntitledSubPurchases = onSubs,
        onEntitledInAppPurchases = onInApp,
        onPurchase = {},
        onSubscriptionSupportedChecked = onSupported,
        clientFactory = { _, _ -> client }
    )

    @Test
    fun `billing unavailable never runs queries or crashes`() = withLog {
        val client = mock<BillingClient>()
        whenever(client.startConnection(any())).thenAnswer { invocation ->
            (invocation.arguments[0] as BillingClientStateListener)
                .onBillingSetupFinished(result(BillingClient.BillingResponseCode.BILLING_UNAVAILABLE))
        }
        val subs = mutableListOf<List<Purchase>>()
        val inApp = mutableListOf<List<Purchase>>()
        val supported = mutableListOf<Boolean>()

        manager(client, { subs.add(it) }, { inApp.add(it) }, { supported.add(it) })

        assertTrue(subs.isEmpty())
        assertTrue(inApp.isEmpty())
        assertTrue(supported.isEmpty())
        verify(client, never()).queryPurchasesAsync(any(), any())
    }

    @Test
    fun `service disconnected leaves billing disconnected and safe`() = withLog {
        val client = mock<BillingClient>()
        var listener: BillingClientStateListener? = null
        whenever(client.startConnection(any())).thenAnswer { invocation ->
            listener = invocation.arguments[0] as BillingClientStateListener
            null
        }
        val m = manager(client)
        listener!!.onBillingServiceDisconnected()

        m.querySubPurchases()
        m.queryInAppPurchases()
        verify(client, never()).queryPurchasesAsync(any(), any())
    }

    @Test
    fun `empty purchase result is delivered without error`() = withLog {
        val client = mock<BillingClient>()
        val ok = result(BillingClient.BillingResponseCode.OK)
        whenever(client.startConnection(any())).thenAnswer { invocation ->
            (invocation.arguments[0] as BillingClientStateListener).onBillingSetupFinished(ok)
        }
        whenever(client.isFeatureSupported(any())).thenReturn(ok)
        whenever(client.queryPurchasesAsync(any(), any())).thenAnswer { invocation ->
            (invocation.arguments[1] as PurchasesResponseListener).onQueryPurchasesResponse(ok, emptyList())
        }
        val subs = mutableListOf<List<Purchase>>()
        val inApp = mutableListOf<List<Purchase>>()

        manager(client, { subs.add(it) }, { inApp.add(it) })

        assertEquals(1, subs.size)
        assertTrue(subs[0].isEmpty())
        assertEquals(1, inApp.size)
        assertTrue(inApp[0].isEmpty())
    }
}
