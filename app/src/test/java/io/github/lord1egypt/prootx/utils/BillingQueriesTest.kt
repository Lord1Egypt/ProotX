package io.github.lord1egypt.prootx.utils

import android.text.TextUtils
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mockStatic

class BillingQueriesTest {

    /** The Billing builders call android.text.TextUtils, which is a throwing stub on the JVM. */
    private inline fun <T> withAndroidStubs(block: () -> T): T =
        mockStatic(TextUtils::class.java).use {
            it.`when`<Boolean> { TextUtils.isEmpty(any()) }.thenReturn(false)
            block()
        }

    private fun offer(offerId: String?, token: String): ProductDetails.SubscriptionOfferDetails =
        mock<ProductDetails.SubscriptionOfferDetails>().also {
            whenever(it.offerId).thenReturn(offerId)
            whenever(it.offerToken).thenReturn(token)
        }

    private fun product(id: String, type: String, offers: List<ProductDetails.SubscriptionOfferDetails>? = null) =
        mock<ProductDetails>().also {
            whenever(it.productId).thenReturn(id)
            whenever(it.productType).thenReturn(type)
            whenever(it.subscriptionOfferDetails).thenReturn(offers)
        }

    @Test
    fun `pending purchases enable one-time products only`() = withAndroidStubs {
        assertNotNull(BillingQueries.pendingPurchasesParams())
    }

    @Test
    fun `product detail queries build for subscriptions and in-app`() = withAndroidStubs {
        assertNotNull(BillingQueries.productDetailsParams(BillingManager.Sku.SUBSCRIPTION_IDS, BillingClient.ProductType.SUBS))
        assertNotNull(BillingQueries.productDetailsParams(BillingManager.Sku.IN_APP_IDS, BillingClient.ProductType.INAPP))
    }

    @Test
    fun `purchase queries build for both product types`() = withAndroidStubs {
        assertNotNull(BillingQueries.purchasesParams(BillingClient.ProductType.SUBS))
        assertNotNull(BillingQueries.purchasesParams(BillingClient.ProductType.INAPP))
    }

    @Test
    fun `product ids are preserved exactly`() {
        assertEquals(
            listOf("1us_monthly", "5us_monthly", "10us_monthly", "20us_monthly",
                "1us_yearly", "5us_yearly", "10us_yearly", "20us_yearly"),
            BillingManager.Sku.SUBSCRIPTION_IDS
        )
        assertEquals(
            listOf("1us_onetime", "5us_onetime", "10us_onetime", "20us_onetime"),
            BillingManager.Sku.IN_APP_IDS
        )
    }

    @Test
    fun `subscription offer token prefers the base plan`() {
        val offers = listOf(offer("promo", "promo-token"), offer(null, "base-token"))
        assertEquals("base-token", BillingQueries.selectSubscriptionOfferToken(offers))
    }

    @Test
    fun `subscription offer token falls back to the first offer`() {
        val offers = listOf(offer("a", "a-token"), offer("b", "b-token"))
        assertEquals("a-token", BillingQueries.selectSubscriptionOfferToken(offers))
    }

    @Test
    fun `subscription offer token is null when there are no offers`() {
        assertNull(BillingQueries.selectSubscriptionOfferToken(emptyList()))
    }

    @Test
    fun `subscription flow params carry the base plan token`() = withAndroidStubs {
        val details = product("1us_monthly", BillingClient.ProductType.SUBS, listOf(offer(null, "base-token")))
        assertNotNull(BillingQueries.flowParamsFor(details))
    }

    @Test
    fun `subscription flow params fail closed without an offer token`() = withAndroidStubs {
        val details = product("1us_monthly", BillingClient.ProductType.SUBS, emptyList())
        val error = runCatching { BillingQueries.flowParamsFor(details) }.exceptionOrNull()
        assertTrue(error is BillingOfferTokenUnavailable)
    }

    @Test
    fun `one-time flow params do not require an offer token`() = withAndroidStubs {
        val details = product("1us_onetime", BillingClient.ProductType.INAPP, null)
        assertNotNull(BillingQueries.flowParamsFor(details))
    }

    @Test
    fun `acknowledgment decision follows purchase state`() {
        val purchasedUnacknowledged = mock<Purchase>().also {
            whenever(it.purchaseState).thenReturn(Purchase.PurchaseState.PURCHASED)
            whenever(it.isAcknowledged).thenReturn(false)
        }
        val purchasedAcknowledged = mock<Purchase>().also {
            whenever(it.purchaseState).thenReturn(Purchase.PurchaseState.PURCHASED)
            whenever(it.isAcknowledged).thenReturn(true)
        }
        val pending = mock<Purchase>().also {
            whenever(it.purchaseState).thenReturn(Purchase.PurchaseState.PENDING)
            whenever(it.isAcknowledged).thenReturn(false)
        }
        assertTrue(BillingQueries.shouldAcknowledge(purchasedUnacknowledged))
        assertFalse(BillingQueries.shouldAcknowledge(purchasedAcknowledged))
        assertFalse(BillingQueries.shouldAcknowledge(pending))
    }
}
