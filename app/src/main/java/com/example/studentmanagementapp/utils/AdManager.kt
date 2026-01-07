package com.example.studentmanagementapp.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdManager {
    private var mInterstitialAd: InterstitialAd? = null
    // Test Ad Unit ID for Interstitial
    private const val AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    private const val TAG = "AdManager"

    fun initialize(context: Context) {
        MobileAds.initialize(context) { }
        loadInterstitial(context)
    }

    fun loadInterstitial(context: Context) {
        if (mInterstitialAd != null) return

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, AD_UNIT_ID, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, "Ad failed to load: ${adError.message}")
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d(TAG, "Ad loaded")
                mInterstitialAd = interstitialAd
            }
        })
    }

    fun showInterstitial(activity: Activity, onAdDismissed: () -> Unit) {
        if (mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Ad was dismissed.")
                    mInterstitialAd = null
                    loadInterstitial(activity) // Preload next ad
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.d(TAG, "Ad failed to show.")
                    mInterstitialAd = null
                    onAdDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Ad showed fullscreen content.")
                    mInterstitialAd = null
                }
            }
            mInterstitialAd?.show(activity)
        } else {
            Log.d(TAG, "The interstitial ad wasn't ready yet.")
            loadInterstitial(activity)
            onAdDismissed()
        }
    }
}
