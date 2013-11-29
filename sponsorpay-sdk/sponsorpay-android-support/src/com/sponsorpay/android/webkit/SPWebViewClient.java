package com.sponsorpay.android.webkit;

import android.util.Log;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class SPWebViewClient extends WebViewClient {

	
	@Override
	public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
		Log.e("BLAAAAAA", url);
		return super.shouldInterceptRequest(view, url);
	}

}
