package com.example.rpmmultiplier

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var offlineLayout: LinearLayout
    private lateinit var connectivityManager: ConnectivityManager

    private val targetUrl = "http://192.168.4.1"

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            connectivityManager.bindProcessToNetwork(network)
            runOnUiThread {
                loadDashboard()
            }
        }

        override fun onLost(network: Network) {
            connectivityManager.bindProcessToNetwork(null)
            runOnUiThread {
                showOffline("Wi-Fi отключён. Подключись к RPM_Multiplier")
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = FrameLayout(this).apply {
            setBackgroundColor(Color.parseColor("#121417"))
        }

        webView = WebView(this).apply {
            setBackgroundColor(Color.parseColor("#121417"))
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    offlineLayout.visibility = View.GONE
                    webView.visibility = View.VISIBLE
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    if (request?.isForMainFrame == true) {
                        showOffline("Нет связи с 192.168.4.1.\nПроверь подключение к Wi-Fi RPM_Multiplier")
                    }
                }
            }
        }

        offlineLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
            setBackgroundColor(Color.parseColor("#121417"))
        }

        val statusText = TextView(this).apply {
            text = "Подключи телефон к Wi-Fi\nRPM_Multiplier (12345678)"
            setTextColor(Color.parseColor("#F1F5F9"))
            textSize = 18f
            gravity = Gravity.CENTER
        }

        val retryBtn = Button(this).apply {
            text = "Обновить"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#2563EB"))
            setPadding(48, 24, 48, 24)
            setOnClickListener { loadDashboard() }
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = 36 }

        offlineLayout.addView(statusText)
        offlineLayout.addView(retryBtn, params)

        root.addView(webView)
        root.addView(offlineLayout)
        setContentView(root)

        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        connectivityManager.registerNetworkCallback(request, networkCallback)
        loadDashboard()
    }

    private fun loadDashboard() {
        webView.loadUrl(targetUrl)
    }

    private fun showOffline(msg: String) {
        (offlineLayout.getChildAt(0) as? TextView)?.text = msg
        webView.visibility = View.GONE
        offlineLayout.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        connectivityManager.unregisterNetworkCallback(networkCallback)
        connectivityManager.bindProcessToNetwork(null)
    }
}
