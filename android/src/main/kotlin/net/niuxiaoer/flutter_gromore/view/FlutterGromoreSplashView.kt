package net.niuxiaoer.flutter_gromore.view

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import com.bytedance.sdk.openadsdk.AdSlot
import com.bytedance.sdk.openadsdk.CSJAdError
import com.bytedance.sdk.openadsdk.CSJSplashAd
import com.bytedance.sdk.openadsdk.TTAdNative
import com.bytedance.sdk.openadsdk.TTAdSdk
import com.bytedance.sdk.openadsdk.mediation.ad.MediationAdSlot
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.platform.PlatformView
import net.niuxiaoer.flutter_gromore.constants.FlutterGromoreConstants
import net.niuxiaoer.flutter_gromore.utils.Utils


class FlutterGromoreSplashView(
        private val context: Context,
        private val activity: Activity,
        viewId: Int,
        private val creationParams: Map<String?, Any?>?,
        binaryMessenger: BinaryMessenger
) :
        FlutterGromoreBase(binaryMessenger, "${FlutterGromoreConstants.splashTypeId}/$viewId"),
        PlatformView, TTAdNative.CSJSplashAdListener, CSJSplashAd.SplashAdListener {

    private val TAG: String = this::class.java.simpleName

    // 开屏广告容器
    private var container: FrameLayout = FrameLayout(context)

    private var splashAd: CSJSplashAd? = null

    init {
        initAd()
    }

    override fun initAd() {
        require(creationParams != null)

        val adUnitId = creationParams["adUnitId"] as String
        require(adUnitId.isNotEmpty())

        val adNativeLoader = TTAdSdk.getAdManager().createAdNative(activity)

        // 注意开屏广告view：width >=70%屏幕宽；height >=50%屏幕高，否则会影响计费。
        // 开屏广告可支持的尺寸：图片尺寸传入与展示区域大小保持一致，避免素材变形
        val containerWidth = creationParams["width"] as? Int ?: Utils.getScreenWidthInPx(context)
        val containerHeight = creationParams["height"] as? Int ?: Utils.getScreenHeightInPx(context)
        val muted = creationParams["muted"] as? Boolean ?: false
        val preload = creationParams["preload"] as? Boolean ?: true
        val volume = creationParams["volume"] as? Float ?: 1f
        val isSplashShakeButton = creationParams["splashShakeButton"] as? Boolean ?: true
        val isBidNotify = creationParams["bidNotify"] as? Boolean ?: false

        container.layoutParams = FrameLayout.LayoutParams(containerWidth, containerHeight)

        val adSlot = AdSlot.Builder()
                .setImageAcceptedSize(containerWidth, containerHeight)
                .setMediationAdSlot(MediationAdSlot.Builder()
                        .setSplashPreLoad(preload)
                        .setMuted(muted)
                        .setVolume(volume)
                        .setSplashShakeButton(isSplashShakeButton)
                        .setBidNotify(isBidNotify)
                        .build())
                .build()
        adNativeLoader.loadSplashAd(adSlot, this, 3500)
        //adNativeLoader.loadSplashAd(adSlot, this)
    }

    private fun finishSplash() {
        // 销毁
        splashAd?.mediationManager?.destroy()
        splashAd = null

        postMessage("onAdEnd")
    }

    override fun getView(): View {
        return container
    }

    override fun dispose() {
        finishSplash()
    }




    override fun onSplashLoadSuccess(ad: CSJSplashAd?) {
        splashAd = ad

        if (ad?.splashView != null) {
            Log.d(TAG, "onSplashAdLoadSuccess")
            postMessage("onSplashAdLoadSuccess")
            container.addView(ad.splashView)
        } else {
            Log.d(TAG, "splashView is null")
            finishSplash()
        }
    }

    override fun onSplashLoadFail(p0: CSJAdError?) {
        Log.d(TAG, "onSplashAdLoadFail")

        finishSplash()
        postMessage("onSplashAdLoadFail")
    }

    override fun onSplashRenderSuccess(ad: CSJSplashAd?) {
        Log.d(TAG, "onSplashRenderSuccess")
        postMessage("onSplashRenderSuccess")
        ad?.setSplashAdListener(this)
    }

    override fun onSplashRenderFail(ad: CSJSplashAd?, csjAdError: CSJAdError?) {
        Log.d(TAG, "onSplashRenderFail ${csjAdError?.msg}")
        postMessage("onSplashRenderFail")

        finishSplash()
    }

    override fun onSplashAdShow(p0: CSJSplashAd?) {
        Log.d(TAG, "onAdShow")
        postMessage("onAdShow")

    }

    override fun onSplashAdClick(p0: CSJSplashAd?) {
        Log.d(TAG, "onAdClicked")
        postMessage("onAdClicked")
    }

    override fun onSplashAdClose(p0: CSJSplashAd?, p1: Int) {
        Log.d(TAG, "onSplashAdClose")
        postMessage("onSplashAdClose")

        finishSplash()
    }

}