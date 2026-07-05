package com.kiran.movie.core.ui.webview

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
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

        // Redirect/popup URL patterns to block
        private val AD_URL_PATTERNS = listOf(
            "popunder", "pop-under", "popup", "pop-up",
            "click.php", "redirect.php", "ad.php",
            "track.php", "tracking.", "aff.", "affiliate",
        )

        private val EMPTY_RESPONSE by lazy {
            WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream(ByteArray(0)))
        }

        private fun shouldBlockResource(url: String?): Boolean {
            if (url == null) return false
            return try {
                val host = android.net.Uri.parse(url).host?.lowercase() ?: return false
                AD_HOSTS.any { host == it || host.endsWith(".$it") }
            } catch (_: Exception) { false }
        }

        /**
         * JS injected after page load — uses native events instead of polling.
         * Calls AndroidBridge.onVideoPlay() / onVideoPause() so the native UI
         * reacts without any setInterval/setTimeout on the Android side.
         */
        private val VIDEO_OBSERVER_JS = """
            (function() {
                function attachObserver(video) {
                    if (video._androidObserverAttached) return;
                    video._androidObserverAttached = true;
                    video.addEventListener('play',  function() { AndroidBridge.onVideoPlay();  });
                    video.addEventListener('pause', function() { AndroidBridge.onVideoPause(); });
                }
                document.querySelectorAll('video').forEach(attachObserver);
                new MutationObserver(function(mutations) {
                    mutations.forEach(function(m) {
                        m.addedNodes.forEach(function(n) {
                            if (n.nodeName === 'VIDEO') attachObserver(n);
                            if (n.querySelectorAll) n.querySelectorAll('video').forEach(attachObserver);
                        });
                    });
                }).observe(document.body, { childList: true, subtree: true });
            })();
        """.trimIndent()
    }

    private lateinit var webView: WebView
    private lateinit var root: FrameLayout
    private lateinit var playPauseBtn: android.widget.ImageButton
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    private var chromeClient: WebChromeClient? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    // Declared as a field so it can be cancelled precisely
    private val hideButtonRunnable = Runnable {
        playPauseBtn.animate()
            .alpha(0f)
            .setDuration(400)
            .withEndAction { playPauseBtn.isClickable = false }
            .start()
    }

    /** Receives play/pause events from the JavaScript bridge. */
    inner class AndroidBridge {
        @JavascriptInterface
        fun onVideoPlay() {
            mainHandler.post {
                playPauseBtn.setImageResource(android.R.drawable.ic_media_pause)
                mainHandler.removeCallbacks(hideButtonRunnable)
                mainHandler.postDelayed(hideButtonRunnable, 2500)
            }
        }

        @JavascriptInterface
        fun onVideoPause() {
            mainHandler.post {
                playPauseBtn.setImageResource(android.R.drawable.ic_media_play)
                mainHandler.removeCallbacks(hideButtonRunnable)
                playPauseBtn.animate().cancel()
                playPauseBtn.alpha = 1f
                playPauseBtn.isClickable = true
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val url = intent.getStringExtra(EXTRA_URL) ?: run { finish(); return }

        root = FrameLayout(this)
        setContentView(root)

        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

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
            settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.builtInZoomControls = false
            settings.setSupportMultipleWindows(false)
            settings.javaScriptCanOpenWindowsAutomatically = false
            settings.userAgentString =
                "Mozilla/5.0 (Linux; Android 10; Mobile) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/120.0.0.0 Mobile Safari/537.36"

            // ── JS Bridge (play/pause events → native UI) ──────────────────
            addJavascriptInterface(AndroidBridge(), "AndroidBridge")

            // ── Ad-blocking WebViewClient ────────────────────────────────────
            webViewClient = object : WebViewClient() {
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

                    // Allow all other navigations (CDN hops, stream segments, etc.)
                    return false
                }

                override fun onPageFinished(view: WebView, url: String) {
                    super.onPageFinished(view, url)
                    // Inject event-listener bridge — no more polling
                    view.evaluateJavascript(VIDEO_OBSERVER_JS, null)
                }
            }
        }

        // ── Native Center Play/Pause Button Overlay ───────────────────────
        playPauseBtn = android.widget.ImageButton(this).apply {
            layoutParams = FrameLayout.LayoutParams(200, 200).apply {
                gravity = android.view.Gravity.CENTER
            }
            setImageResource(android.R.drawable.ic_media_play)
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL
                setColor(android.graphics.Color.parseColor("#99000000"))
            }
            imageTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE)
            scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
            setPadding(48, 48, 48, 48)
            elevation = 16f
            alpha = 0f
            isClickable = false
            setOnClickListener {
                animate().cancel()
                alpha = 1f
                webView.evaluateJavascript(
                    """
                    (function() {
                        var v = document.querySelector('video');
                        if (v) { if (v.paused) v.play(); else v.pause(); }
                    })();
                    """.trimIndent()
                    // UI state is updated by the JS bridge (onVideoPlay / onVideoPause)
                , null)
            }
        }

        // ── Touch: reveal button on tap, auto-hide if playing ────────────
        @SuppressLint("ClickableViewAccessibility")
        val touchListener = android.view.View.OnTouchListener { _, event ->
            if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                mainHandler.removeCallbacks(hideButtonRunnable)
                playPauseBtn.animate().cancel()
                playPauseBtn.alpha = 1f
                playPauseBtn.isClickable = true

                webView.evaluateJavascript(
                    "document.querySelector('video') ? !document.querySelector('video').paused : false"
                ) { isPlaying ->
                    if (isPlaying == "true") {
                        mainHandler.postDelayed(hideButtonRunnable, 2500)
                    }
                }
            }
            false
        }

        webView.setOnTouchListener(touchListener)

        // ── Chrome client for media + fullscreen ─────────────────────────
        val client = object : WebChromeClient() {

            override fun onPermissionRequest(request: PermissionRequest) {
                request.grant(request.resources)
            }

            @SuppressLint("ClickableViewAccessibility")
            override fun onShowCustomView(view: View, callback: CustomViewCallback) {
                if (customView != null) { callback.onCustomViewHidden(); return }
                customView = view
                customViewCallback = callback
                root.addView(
                    view,
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT,
                    ),
                )
                view.setOnTouchListener(touchListener)
                root.bringChildToFront(playPauseBtn)
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
        root.addView(playPauseBtn)

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
        mainHandler.removeCallbacks(hideButtonRunnable)
        webView.onPause()
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    override fun onDestroy() {
        mainHandler.removeCallbacks(hideButtonRunnable)
        webView.removeJavascriptInterface("AndroidBridge")
        webView.stopLoading()
        webView.destroy()
        super.onDestroy()
    }
}
