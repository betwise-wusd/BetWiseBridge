package com.betwise.betwisebridge.webview

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.webkit.WebSettings
import com.betwise.betwisebridge.plugin.manager.WalletHandlerManager
import com.betwise.betwisebridge.wallet.WalletManager
import com.github.lzyzsd.jsbridge.BridgeWebView

/**
 *
 * BetWiseWebview
 * desc:
 *
 */
class BetWiseWebview(context: Context, attrs: AttributeSet): BridgeWebView(context, attrs) {

    init{
        settings?.domStorageEnabled = true
        settings?.cacheMode = WebSettings.LOAD_NO_CACHE
        settings?.textZoom = 100
        settings?.setBuiltInZoomControls(false)
        settings?.setSupportZoom(false)
        settings?.setDisplayZoomControls(false)

        isScrollbarFadingEnabled = false
        setBackgroundColor(Color.parseColor("#FFFFFF"))
    }

    fun initWallet(netType: Int){
        WalletManager.instance.initNetType(netType)
    }

    override fun loadUrl(url: String?) {
        super.loadUrl(url)
        registWalletHandler()
    }

    private fun registWalletHandler(){
        val handlers = WalletHandlerManager(context).registerHandlers()
        handlers.forEach {
            registerHandler(it.key, it.value)
        }
    }

}