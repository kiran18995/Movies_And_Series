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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
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
            settings.mixedContentMode =
                android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.builtInZoomControls = false
            settings.setSupportMultipleWindows(false)
            settings.javaScriptCanOpenWindowsAutomatically = false
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

                override fun shouldOverrideUrlLoading(
                    view: WebView,
                    request: WebResourceRequest,
                ): Boolean {
                    // Block all top-level navigations (ad redirects)
                    val host = request.url.host ?: return true
                    if (!host.contains("streamimdb.ru")) {
                        return true // True means we intercept it and cancel the navigation
                    }
                    return false
                }
            }
        }

        // ── Native Center Play/Pause Button Overlay ───────────────────────
        val playPauseBtn = android.widget.ImageButton(this).apply {
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
                        if(v) {
                            if(v.paused) v.play();
                            else v.pause();
                            return v.paused.toString();
                        }
                        return "null";
                    })();
                    """.trimIndent()
                ) { result ->
                    if (result == "\"false\"" || result == "false") {
                        setImageResource(android.R.drawable.ic_media_pause)
                        animate().alpha(0f).setStartDelay(1500).setDuration(500)
                            .withEndAction { isClickable = false }.start()
                    } else {
                        setImageResource(android.R.drawable.ic_media_play)
                        isClickable = true
                    }
                }
            }
        }

        val updateStateRunnable = object : Runnable {
            override fun run() {
                webView.evaluateJavascript(
                    "document.querySelector('video') ? document.querySelector('video').paused.toString() : 'null'"
                ) { result ->
                    if (result == "\"true\"" || result == "true") {
                        playPauseBtn.setImageResource(android.R.drawable.ic_media_play)
                        playPauseBtn.animate().cancel()
                        playPauseBtn.alpha = 1f
                        playPauseBtn.isClickable = true
                    } else if (result == "\"false\"" || result == "false") {
                        playPauseBtn.setImageResource(android.R.drawable.ic_media_pause)
                    }
                }
                playPauseBtn.postDelayed(this, 1000)
            }
        }
        playPauseBtn.post(updateStateRunnable)

        @SuppressLint("ClickableViewAccessibility")
        val touchListener = android.view.View.OnTouchListener { _, event ->
            if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                if (playPauseBtn.alpha == 0f) {
                    playPauseBtn.animate().cancel()
                    playPauseBtn.alpha = 1f
                    playPauseBtn.isClickable = true
                    
                    webView.evaluateJavascript("document.querySelector('video') ? document.querySelector('video').paused.toString() : 'null'") { res ->
                        if (res == "\"false\"" || res == "false") {
                            playPauseBtn.animate()
                                .alpha(0f)
                                .setStartDelay(2500)
                                .setDuration(300)
                                .withEndAction { playPauseBtn.isClickable = false }
                                .start()
                        }
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
                view.setOnTouchListener(touchListener)
                root.bringChildToFront(playPauseBtn)

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
        webView.webChromeClient = client

        webView.loadUrl(url)

    root.addView(webView)
    root.addView(playPauseBtn)

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
