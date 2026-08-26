package com.leetcodeautomation.ui

import android.annotation.SuppressLint
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay

private const val LEETCODE_LOGIN_URL = "https://leetcode.com/accounts/login/"
private const val LEETCODE_COOKIE_DOMAIN = "https://leetcode.com"

/**
 * Logs the user into LeetCode's real login page inside an in-app WebView, then lifts the
 * resulting session cookies straight out of CookieManager — no more copying values out of
 * browser DevTools by hand.
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LeetCodeLoginScreen(onCaptured: (session: String, csrf: String) -> Unit, onClose: () -> Unit) {
    val context = LocalContext.current
    var captured by remember { mutableStateOf(false) }

    fun tryCapture() {
        if (captured) return
        val raw = CookieManager.getInstance().getCookie(LEETCODE_COOKIE_DOMAIN) ?: return
        val cookies = raw.split(";").associate { entry ->
            val parts = entry.trim().split("=", limit = 2)
            parts[0] to parts.getOrElse(1) { "" }
        }
        val session = cookies["LEETCODE_SESSION"]
        val csrf = cookies["csrftoken"]
        if (!session.isNullOrBlank() && !csrf.isNullOrBlank()) {
            captured = true
            onCaptured(session, csrf)
        }
    }

    Scaffold(
        containerColor = CanvasBlack,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Log in to LeetCode", color = OnSurface) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel", tint = OnSurface)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = CanvasBlack),
            )
        },
    ) { padding ->
        AndroidView(
            modifier = Modifier.fillMaxSize().padding(padding),
            factory = {
                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                android.webkit.WebStorage.getInstance().deleteAllData()
                val webView = WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    cookieManager.setAcceptThirdPartyCookies(this, true)
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView, url: String?) {
                            super.onPageFinished(view, url)
                            tryCapture()
                        }
                    }
                }
                // Clear any leftover session/Google sign-in cookies first, so this always shows
                // a real login prompt instead of instantly re-capturing a stale session and
                // closing before the user sees anything (what "Log in again" looked like before).
                cookieManager.removeAllCookies { webView.loadUrl(LEETCODE_LOGIN_URL) }
                webView
            },
        )
    }

    // Logging in redirects within a single-page app, which doesn't always trigger a fresh
    // page load, so also poll for the session cookie showing up.
    LaunchedEffect(Unit) {
        while (!captured) {
            delay(1200)
            tryCapture()
        }
    }
}
