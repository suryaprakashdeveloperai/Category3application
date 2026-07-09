package com.example.category3.auth.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

class HmiActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Get the IP address that was passed from the Login screen
        val rawIp = intent.getStringExtra("IP_ADDRESS") ?: ""

        // 2. Make sure it starts with "http://"
        val targetUrl = if (rawIp.isNotEmpty() && !rawIp.startsWith("http://") && !rawIp.startsWith("https://")) {
            "http://$rawIp"
        } else {
            rawIp
        }

        // 3. Set the Compose UI content
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HmiScreen(url = targetUrl)
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun HmiScreen(url: String) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                layoutParams = android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                )

                webViewClient = object : WebViewClient() {

                    // 1. THIS IS REQUIRED FOR LOCAL HTTPS (Self-Signed Certificates)
                    @SuppressLint("WebViewClientOnReceivedSslError")
                    override fun onReceivedSslError(
                        view: WebView?,
                        handler: android.webkit.SslErrorHandler?,
                        error: android.net.http.SslError?
                    ) {
                        // Tells the WebView to bypass the SSL security block for local IPs
                        handler?.proceed()
                    }

                    // 2. Keep the Viewport Injector to prevent squishing
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        view?.loadUrl(
                            "javascript:(function() { " +
                                    "var meta = document.createElement('meta');" +
                                    "meta.setAttribute('name', 'viewport');" +
                                    "meta.setAttribute('content', 'width=device-width, initial-scale=1.0, maximum-scale=5.0, user-scalable=yes');" +
                                    "document.getElementsByTagName('head')[0].appendChild(meta);" +
                                    "})()"
                        )
                    }
                }

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true

                    // Force desktop rendering
                    useWideViewPort = true
                    loadWithOverviewMode = true

                    // Enable zoom for tiny HMI buttons
                    setSupportZoom(true)
                    builtInZoomControls = true
                    displayZoomControls = false

                    // 3. Allow Mixed Content (Crucial if HTTPS page loads HTTP images/scripts)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    }
                }
            }
        },
        update = { webView ->
            if (url.isNotEmpty()) {
                webView.loadUrl(url)
            }
        }
    )
}