package io.github.lord1egypt.prootx.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.FeatureType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import java.util.* // ktlint-disable no-wildcard-imports
import kotlin.collections.HashMap

/**
 * Play Billing 8 client (migrated from the API34-incompatible billing-ktx 3.0.3).
 *
 * Billing is optional functionality: a missing Play Store, an unavailable service, an
 * offline device or an empty purchase list must never throw through the Activity
 * lifecycle. All failures are routed to the existing error/log path.
 *
 * When using this class:
 * - Call `queryPurchases()` in your Activity's onResume() method
 * - Call `query*SubscriptionSkuDetails()` when you want to show your in-app products
 * - Call `startPurchaseFlow()` when one of your in-app products is clicked on
 * - Call `destroy()` in your Activity's onDestroy() method
 */
class BillingManager(
    private val activity: Activity,
    private val onEntitledSubPurchases: (List<Purchase>) -> Unit,
    private val onEntitledInAppPurchases: (List<Purchase>) -> Unit,
    private val onPurchase: (Purchase) -> Unit,
    private val onSubscriptionSupportedChecked: (Boolean) -> Unit,
    clientFactory: (Context, PurchasesUpdatedListener) -> BillingClient = { context, listener ->
        BillingClient.newBuilder(context)
            .enablePendingPurchases(BillingQueries.pendingPurchasesParams())
            .setListener(listener)
            .build()
    }
) {

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        when (billingResult.responseCode) {
            BillingResponseCode.OK -> {
                purchases?.let {
                    for (purchase in purchases) {
                        when (purchase.purchaseState) {
                            Purchase.PurchaseState.PURCHASED -> {
                                onPurchase(purchase)
                                if (BillingQueries.shouldAcknowledge(purchase)) {
                                    val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                                        .setPurchaseToken(purchase.purchaseToken)
                                        .build()
                                    billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                                        log("acknowledgePurchase(), billingResult=$billingResult")
                                    }
                                }
                            }
                            Purchase.PurchaseState.PENDING -> {
                                // Here you can confirm to the user that they've started the pending
                                // purchase, and to complete it, they should follow instructions that
                                // are given to them. You can also choose to remind the user in the
                                // future to complete the purchase if you detect that it is still
                                // pending.
                            }
                        }
                    }
                }
                log("onPurchasesUpdated(), $purchases")
            }
            BillingResponseCode.USER_CANCELED -> log("onPurchasesUpdated() - user cancelled the purchase flow - skipping")
            else -> log("onPurchasesUpdated() got unknown resultCode: ${billingResult.responseCode}")
        }
    }

    private val productDetailsMap = HashMap<String, ProductDetails>()

    private val billingClient: BillingClient = clientFactory(activity, purchasesUpdatedListener)

    private var isBillingServiceConnected = false

    val populateProducts: (List<ProductDetails>) -> Unit = {
        it.forEach { productDetails -> productDetailsMap[productDetails.productId] = productDetails }
    }

    private fun handlePopulateProductError(code: Int, message: String) {
        log("Error trying to populate products.  code: $code message: $message")
    }

    init {
        startServiceConnection {
            onSubscriptionSupportedChecked(isSubscriptionPurchaseSupported())
            querySubPurchases()
            queryInAppPurchases()
            querySubscriptionProductDetails(Sku.SUBSCRIPTION_IDS, populateProducts, ::handlePopulateProductError)
            queryInAppProductDetails(Sku.IN_APP_IDS, populateProducts, ::handlePopulateProductError)
        }
    }

    fun querySubPurchases() {
        if (!isBillingServiceConnected) return
        if (!isSubscriptionPurchaseSupported()) return
        billingClient.queryPurchasesAsync(
            BillingQueries.purchasesParams(BillingClient.ProductType.SUBS)
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingResponseCode.OK) {
                onEntitledSubPurchases(Collections.unmodifiableList(purchases))
            } else {
                log("Error trying to query subscription purchases: $billingResult")
            }
        }
    }

    fun queryInAppPurchases() {
        if (!isBillingServiceConnected) return
        billingClient.queryPurchasesAsync(
            BillingQueries.purchasesParams(BillingClient.ProductType.INAPP)
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingResponseCode.OK) {
                onEntitledInAppPurchases(Collections.unmodifiableList(purchases))
            } else {
                log("Error trying to query in-app purchases: $billingResult")
            }
        }
    }

    fun startPurchaseFlow(productId: String) {
        val productDetails = productDetailsMap[productId]
        if (productDetails == null) {
            log("startPurchaseFlow() - no product details for $productId")
            return
        }
        startServiceConnection {
            val productDetailsParams = try {
                BillingQueries.flowParamsFor(productDetails)
            } catch (err: BillingOfferTokenUnavailable) {
                log("startPurchaseFlow() - ${err.message}")
                return@startServiceConnection
            }
            val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productDetailsParams))
                .build()
            val billingResult = billingClient.launchBillingFlow(activity, flowParams)
            log("startPurchaseFlow(...), billingResult=$billingResult")
        }
    }

    fun destroy() {
        log("destroy()")
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }

    private fun startServiceConnection(task: () -> Unit) {
        if (isBillingServiceConnected) {
            task()
        } else {
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    log("onBillingSetupFinished(...), billingResult=$billingResult")
                    if (billingResult.responseCode == BillingResponseCode.OK) {
                        isBillingServiceConnected = true
                        task()
                    }
                }

                override fun onBillingServiceDisconnected() {
                    log("onBillingServiceDisconnected()")
                    isBillingServiceConnected = false
                    // Try to restart the connection on the next request to
                    // Google Play by calling the startConnection() method.
                }
            })
        }
    }

    private fun querySubscriptionProductDetails(ids: List<String>, onSuccess: (List<ProductDetails>) -> Unit, onError: (code: Int, message: String) -> Unit) {
        billingClient.queryProductDetailsAsync(
            BillingQueries.productDetailsParams(ids, BillingClient.ProductType.SUBS)
        ) { billingResult, productDetailsResult ->
            val productDetailsList = productDetailsResult.productDetailsList
            if (billingResult.responseCode == BillingResponseCode.OK && productDetailsList != null) {
                onSuccess(productDetailsList)
            } else {
                onError(billingResult.responseCode, billingResult.debugMessage)
            }
        }
    }

    private fun queryInAppProductDetails(ids: List<String>, onSuccess: (List<ProductDetails>) -> Unit, onError: (code: Int, message: String) -> Unit) {
        billingClient.queryProductDetailsAsync(
            BillingQueries.productDetailsParams(ids, BillingClient.ProductType.INAPP)
        ) { billingResult, productDetailsResult ->
            val productDetailsList = productDetailsResult.productDetailsList
            if (billingResult.responseCode == BillingResponseCode.OK && productDetailsList != null) {
                onSuccess(productDetailsList)
            } else {
                onError(billingResult.responseCode, billingResult.debugMessage)
            }
        }
    }

    private fun isSubscriptionPurchaseSupported(): Boolean {
        val response = billingClient.isFeatureSupported(FeatureType.SUBSCRIPTIONS)
        if (response.responseCode != BillingResponseCode.OK) {
            log("isSubscriptionPurchaseSupported(), not supported, error response: $response")
        }
        return response.responseCode == BillingResponseCode.OK
    }

    private fun log(message: String) {
        Log.d("BillingManager", message)
    }

    /** The format of SKUs must start with number or lowercase letter and can contain only numbers (0-9),
     * lowercase letters (a-z), underscores (_) & periods (.).*/
    object Sku {
        const val US1_ONETIME = "1us_onetime"
        const val US5_ONETIME = "5us_onetime"
        const val US10_ONETIME = "10us_onetime"
        const val US20_ONETIME = "20us_onetime"
        const val US1_MONTHLY = "1us_monthly"
        const val US5_MONTHLY = "5us_monthly"
        const val US10_MONTHLY = "10us_monthly"
        const val US20_MONTHLY = "20us_monthly"
        const val US1_YEARLY = "1us_yearly"
        const val US5_YEARLY = "5us_yearly"
        const val US10_YEARLY = "10us_yearly"
        const val US20_YEARLY = "20us_yearly"

        val SUBSCRIPTION_IDS = listOf(
            US1_MONTHLY, US5_MONTHLY, US10_MONTHLY, US20_MONTHLY,
            US1_YEARLY, US5_YEARLY, US10_YEARLY, US20_YEARLY
        )
        val IN_APP_IDS = listOf(US1_ONETIME, US5_ONETIME, US10_ONETIME, US20_ONETIME)

        // Testing
        // const val TEST_PURCHASED = "android.test.purchased"
        // const val TEST_CANCELED = "android.test.canceled"
        // const val TEST_UNAVAILABLE = "android.test.item_unavailable"
    }
}
