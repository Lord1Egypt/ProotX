package io.github.lord1egypt.prootx.utils

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams

/**
 * Pure Billing 8 request builders and decisions, kept separate from the Android
 * `BillingClient` so they can be unit-tested without a billing service.
 */
object BillingQueries {

    /** Pending purchases: one-time products only (ProotX has no prepaid plans). */
    fun pendingPurchasesParams(): PendingPurchasesParams =
        PendingPurchasesParams.newBuilder()
            .enableOneTimeProducts()
            .build()

    fun productDetailsParams(ids: List<String>, productType: String): QueryProductDetailsParams {
        val products = ids.map { id ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id)
                .setProductType(productType)
                .build()
        }
        return QueryProductDetailsParams.newBuilder().setProductList(products).build()
    }

    fun purchasesParams(productType: String): QueryPurchasesParams =
        QueryPurchasesParams.newBuilder().setProductType(productType).build()

    /**
     * The historical SkuDetails subscription flow implicitly used the base plan. Billing 8
     * requires an explicit offer token, so deliberately prefer the base plan (`offerId == null`)
     * and only fall back to the first declared offer if no base plan is present.
     */
    fun selectSubscriptionOfferToken(offers: List<ProductDetails.SubscriptionOfferDetails>): String? {
        val chosen = offers.firstOrNull { it.offerId == null } ?: offers.firstOrNull()
        return chosen?.offerToken
    }

    fun flowParamsFor(productDetails: ProductDetails): BillingFlowParams.ProductDetailsParams {
        val builder = BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(productDetails)
        if (productDetails.productType == BillingClient.ProductType.SUBS) {
            val token = selectSubscriptionOfferToken(productDetails.subscriptionOfferDetails ?: emptyList())
                ?: throw BillingOfferTokenUnavailable(productDetails.productId)
            builder.setOfferToken(token)
        }
        return builder.build()
    }

    fun shouldAcknowledge(purchase: Purchase): Boolean =
        purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged
}

class BillingOfferTokenUnavailable(productId: String) :
    IllegalStateException("no subscription offer token for $productId")
