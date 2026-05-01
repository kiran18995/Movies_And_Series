package com.kiran.movie.core.ui.webview

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import java.io.ByteArrayInputStream

/**
 * A full-screen Activity that hosts a WebView for streaming.
 * Handles proper fullscreen video, quality controls exposed by the
 * embedded player, ad-blocking, and hardware acceleration.
 */
class MovieWebViewActivity : ComponentActivity() {

    companion object {
        const val EXTRA_URL = "extra_url"
        const val EXTRA_TITLE = "extra_title"

        private val AD_HOSTS = setOf(
            "doubleclick.net", "googlesyndication.com", "googleadservices.com",
            "adservice.google.com", "adservice.google.co.in",
            "pagead2.googlesyndication.com", "tpc.googlesyndication.com",
            "ads.pubmatic.com", "simage2.pubmatic.com",
            "secure.adnxs.com", "ib.adnxs.com",
            "prebid.io", "prebid.org",
            "taboola.com", "trc.taboola.com",
            "outbrain.com", "widgets.outbrain.com",
            "amazon-adsystem.com", "aax.amazon-adsystem.com",
            "criteo.com", "static.criteo.net",
            "advertising.com", "adtech.com",
            "rubiconproject.com", "ads.rubiconproject.com",
            "openx.net", "openx.com",
            "moatads.com", "z.moatads.com",
            "casalemedia.com", "scdn.cxense.com",
            "ads.yahoo.com", "media.net",
            "scorecardresearch.com",
            "cdn.admanager.com", "ads.exoclick.com", "adx.ads.exoclick.com",
            "popads.net", "popcash.net", "trafficjunky.net",
            "traffichaus.com", "trafficfactory.biz",
            "propellerads.com", "admaven.com",
            "revcontent.com", "adtelligent.com",
        )

        private val EMPTY_RESPONSE by lazy {
            WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream(ByteArray(0)))
        }

        private fun shouldBlock(url: String?): Boolean {
            if (url == null) return false
            return try {
                val host = android.net.Uri.parse(url).host?.lowercase() ?: return false
                AD_HOSTS.any { host == it || host.endsWith(".$it") }
            } catch (_: Exception) { false }
        }
    }

    private lateinit var webView: WebView
    private lateinit var root: FrameLayout
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    private var chromeClient: WebChromeClient? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep screen on while watching
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val url = intent.getStringExtra(EXTRA_URL) ?: run { finish(); return }

        root = FrameLayout(this)
        setContentView(root)

        webView = WebView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
            )

            // ── Settings ────────────────────────────────────────────────────
            @Suppress("SetJavaScriptEnabled")
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.allowContentAccess = true
            settings.allowFileAccess = true
            settings.loadsImagesAutomatically = true
            settings.mixedContentMode =
                android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.builtInZoomControls = false
            settings.setSupportMultipleWindows(true)
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.userAgentString =
                "Mozilla/5.0 (Linux; Android 10; Mobile) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/120.0.0.0 Mobile Safari/537.36"

            // ── Ad-blocking client ───────────────────────────────────────────
            webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(
                    view: WebView,
                    request: WebResourceRequest,
                ): WebResourceResponse? {
                    if (shouldBlock(request.url.toString())) return EMPTY_RESPONSE
                    return super.shouldInterceptRequest(view, request)
                }
            }

            // ── Chrome client for media + fullscreen ─────────────────────────
            val client = object : WebChromeClient() {

                override fun onPermissionRequest(request: PermissionRequest) {
                    request.grant(request.resources)
                }

                override fun onShowCustomView(
                    view: View,
                    callback: CustomViewCallback,
                ) {
                    // If a custom view is already showing, dismiss it first
                    if (customView != null) {
                        callback.onCustomViewHidden()
                        return
                    }
                    customView = view
                    customViewCallback = callback

                    // Add fullscreen view on top of everything
                    root.addView(
                        view,
                        FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT,
                        ),
                    )

                    // Hide system bars for true immersive fullscreen
                    enterImmersive()
                }

                override fun onHideCustomView() {
                    customView?.let { root.removeView(it) }
                    customView = null
                    customViewCallback?.onCustomViewHidden()
                    customViewCallback = null
                    exitImmersive()
                }

                override fun onConsoleMessage(
                    consoleMessage: android.webkit.ConsoleMessage,
                ): Boolean = true // suppress console noise
            }
            chromeClient = client
            webChromeClient = client

            loadUrl(url)
        }

        root.addView(webView)

        // Back press: go back in WebView history, or exit fullscreen, or finish
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    customView != null -> chromeClient?.onHideCustomView()
                    webView.canGoBack() -> webView.goBack()
                    else -> finish()
                }
            }
        })
    }

    @Suppress("DEPRECATION")
    private fun enterImmersive() {
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        )
    }

    @Suppress("DEPRECATION")
    private fun exitImmersive() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    override fun onDestroy() {
        webView.stopLoading()
        webView.destroy()
        super.onDestroy()
    }
}
