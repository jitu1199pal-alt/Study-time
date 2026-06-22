package com.example

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdmobManager {
    private const val TAG = "AdmobManager"

    // Official Google Mobile Ads Unit IDs
    const val TEST_MINI_BANNER_ID = "ca-app-pub-2585981026340393/9149642997"
    const val TEST_INTERSTITIAL_ID = "ca-app-pub-2585981026340393/3532685935"
    const val OFFICIAL_NATIVE_AD_ID = "ca-app-pub-2585981026340393/4569671094"

    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            MobileAds.initialize(context) { status ->
                isInitialized = true
                Log.d(TAG, "MobileAds SDK Initialized successfully. Map: ${status.adapterStatusMap}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize MobileAds SDK: ${e.message}")
        }
    }

    /**
     * Loads an Interstitial Ad asynchronously.
     */
    fun loadInterstitial(
        context: Context,
        adUnitId: String? = null,
        onLoaded: (InterstitialAd) -> Unit,
        onFailed: (LoadAdError) -> Unit
    ) {
        // Automatically initialize if not done yet
        initialize(context)

        val unitId = adUnitId ?: TEST_INTERSTITIAL_ID
        Log.d(TAG, "Loading Interstitial Ad with ID: $unitId")

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            unitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d(TAG, "AdMob Interstitial Ad loaded successfully!")
                    onLoaded(interstitialAd)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e(TAG, "AdMob Interstitial failed to load: ${loadAdError.message}")
                    onFailed(loadAdError)
                }
            }
        )
    }

    /**
     * Shows an Interstitial Ad with custom callbacks for dismiss or fail-to-show actions.
     */
    fun showInterstitial(
        activity: Activity,
        interstitialAd: InterstitialAd,
        onAdClosed: () -> Unit
    ) {
        interstitialAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "AdMob Interstitial was dismissed. Resuming app flow.")
                onAdClosed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "AdMob Interstitial failed to show: ${adError.message}. Resuming flow.")
                onAdClosed()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "AdMob Interstitial shown successfully.")
            }
        }

        interstitialAd.show(activity)
    }
}

@Composable
fun AdmobBanner(
    modifier: Modifier = Modifier,
    adUnitId: String? = null
) {
    val unitId = adUnitId ?: AdmobManager.TEST_MINI_BANNER_ID
    
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        factory = { context ->
            AdmobManager.initialize(context)
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                setAdUnitId(unitId)
                loadAd(AdRequest.Builder().build())
            }
        },
        update = { adView ->
            // Do not reload AdRequest here to prevent rapid refresh cycling and violating AdMob policies.
            // Composition recomposes very frequently, so loading AdRequest here causes excessive requests and risk of account suspension.
        }
    )
}
