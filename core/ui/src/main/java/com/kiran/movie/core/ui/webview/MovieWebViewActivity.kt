package com.kiran.movie.core.ui.webview

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
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
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.ByteArrayInputStream

/**
 * A full-screen Activity that hosts a WebView for streaming.
 * Handles proper fullscreen video, ad-blocking, and hardware acceleration.
 */
class MovieWebViewActivity : ComponentActivity() {
    companion object {
        const val EXTRA_URL = "extra_url"
        const val EXTRA_TITLE = "extra_title"

        private val AD_HOSTS =
            setOf(
                "doubleclick.net",
                "googlesyndication.com",
                "googleadservices.com",
                "adservice.google.com",
                "adservice.google.co.in",
                "pagead2.googlesyndication.com",
                "tpc.googlesyndication.com",
                "ads.pubmatic.com",
                "simage2.pubmatic.com",
                "secure.adnxs.com",
                "ib.adnxs.com",
                "prebid.io",
                "prebid.org",
                "taboola.com",
                "trc.taboola.com",
                "outbrain.com",
                "widgets.outbrain.com",
                "amazon-adsystem.com",
                "aax.amazon-adsystem.com",
                "criteo.com",
                "static.criteo.net",
                "advertising.com",
                "adtech.com",
                "rubiconproject.com",
                "ads.rubiconproject.com",
                "openx.net",
                "openx.com",
                "moatads.com",
                "z.moatads.com",
                "casalemedia.com",
                "scdn.cxense.com",
                "ads.yahoo.com",
                "media.net",
                "scorecardresearch.com",
                "cdn.admanager.com",
                "ads.exoclick.com",
                "adx.ads.exoclick.com",
                "popads.net",
                "popcash.net",
                "trafficjunky.net",
                "traffichaus.com",
                "trafficfactory.biz",
                "propellerads.com",
                "admaven.com",
                "revcontent.com",
                "adtelligent.com",
            )

        // Redirect/popup URL patterns to block
        private val AD_URL_PATTERNS =
            listOf(
                "popunder",
                "pop-under",
                "popup",
                "pop-up",
                "click.php",
                "redirect.php",
                "ad.php",
                "track.php",
                "tracking.",
                "aff.",
                "affiliate",
                "banner",
                "advert",
                "ad-network",
                "cpm",
                "cpc",
                "betting",
                "casino",
            )

        private val EMPTY_RESPONSE by lazy {
            WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream(ByteArray(0)))
        }

        private fun shouldBlockResource(urlToCheck: String?): Boolean {
            if (urlToCheck == null) return false
            val lowerUrl = urlToCheck.lowercase()

            if (AD_URL_PATTERNS.any { lowerUrl.contains(it) }) return true

            return try {
                val host =
                    urlToCheck
                        .toUri()
                        .host
                        ?.lowercase() ?: return false
                AD_HOSTS.any { host == it || host.endsWith(".$it") }
            } catch (_: Exception) {
                false
            }
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

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val url =
            intent.getStringExtra(EXTRA_URL) ?: run {
                finish()
                return
            }

        root = FrameLayout(this)
        setContentView(root)

        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        webView =
            WebView(this).apply {
                layoutParams =
                    FrameLayout.LayoutParams(
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
                settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                settings.builtInZoomControls = true
                settings.displayZoomControls = true
                settings.setSupportZoom(true)
                settings.setSupportMultipleWindows(false)
                settings.javaScriptCanOpenWindowsAutomatically = false
                settings.userAgentString =
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

                // ── Ad-blocking WebViewClient ────────────────────────────────────
                webViewClient =
                    object : WebViewClient() {
                        override fun shouldInterceptRequest(
                            view: WebView,
                            request: WebResourceRequest,
                        ): WebResourceResponse? {
                            if (shouldBlockResource(request.url.toString())) return EMPTY_RESPONSE
                            return super.shouldInterceptRequest(view, request)
                        }

                        override fun shouldOverrideUrlLoading(
                            view: WebView,
                            request: WebResourceRequest,
                        ): Boolean {
                            val urlStr = request.url.toString().lowercase()
                            val host = request.url.host?.lowercase() ?: return true
                            val scheme = request.url.scheme?.lowercase() ?: return true

                            // Block non-http(s) navigation schemes (intent://, market://, etc.)
                            if (scheme !in setOf("http", "https", "data", "blob")) return true

                            // Block known ad hosts
                            if (AD_HOSTS.any { host == it || host.endsWith(".$it") }) return true

                            // Block known ad redirect URL patterns
                            if (AD_URL_PATTERNS.any { urlStr.contains(it) }) return true

                            // Block main-frame navigation away from the original host (aggressively stops redirect popups)
                            if (request.isForMainFrame) {
                                val originalHost =
                                    url
                                        .toUri()
                                        .host
                                        ?.lowercase()
                                if (originalHost != null) {
                                    val isSameDomain =
                                        host == originalHost || host.endsWith(".$originalHost") || originalHost.endsWith(".$host")
                                    if (!isSameDomain) return true
                                }
                            }

                            // Allow all other navigations (CDN hops, stream segments, etc.)
                            return false
                        }

                        override fun onPageFinished(
                            view: WebView,
                            pageUrl: String,
                        ) {
                            super.onPageFinished(view, pageUrl)
                            // Aggressively disable popups/new tabs created via JS
                            view.evaluateJavascript("window.open = function() { return null; };", null)
                        }
                    }
            }

        // ── Chrome client for media + fullscreen ─────────────────────────
        val client =
            object : WebChromeClient() {
                override fun onPermissionRequest(request: PermissionRequest) {
                    request.grant(request.resources)
                }

                @SuppressLint("ClickableViewAccessibility")
                override fun onShowCustomView(
                    view: View,
                    callback: CustomViewCallback,
                ) {
                    if (customView != null) {
                        callback.onCustomViewHidden()
                        return
                    }
                    customView = view
                    customViewCallback = callback
                    root.addView(
                        view,
                        FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT,
                        ),
                    )
                    enterImmersive()
                }

                override fun onHideCustomView() {
                    customView?.let { root.removeView(it) }
                    customView = null
                    customViewCallback?.onCustomViewHidden()
                    customViewCallback = null
                    exitImmersive()
                }

                override fun onConsoleMessage(msg: android.webkit.ConsoleMessage): Boolean = true
            }
        chromeClient = client
        webView.webChromeClient = client

        webView.loadUrl(url)

        root.addView(webView)

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    when {
                        customView != null -> chromeClient?.onHideCustomView()
                        webView.canGoBack() -> webView.goBack()
                        else -> finish()
                    }
                }
            },
        )
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
        // Prevent memory leaks by properly detaching and clearing the WebView before destruction
        root.removeView(webView)
        webView.apply {
            stopLoading()
            webChromeClient = WebChromeClient()
            webViewClient = WebViewClient()
            clearHistory()
            clearCache(true)
            loadUrl("about:blank")
            removeAllViews()
            destroy()
        }

        customView = null
        customViewCallback = null
        chromeClient = null

        super.onDestroy()
    }
}
