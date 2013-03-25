package com.sponsorpay.sdk.android.testapp.url;

import java.io.IOException;
import java.util.Properties;

import com.sponsorpay.sdk.android.utils.SPUrlProvider;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;
import com.sponsorpay.sdk.android.utils.StringUtils;

public class TestAppUrlsProvider implements SPUrlProvider {

	public static TestAppUrlsProvider INSTANCE = new TestAppUrlsProvider();
	private static final String TAG = "TestAppUrlsProvider";
	
	private Properties mStagingUrls;
	private String mOverridingUrl;
	private boolean mUseStaging;
	
	private TestAppUrlsProvider() {
		mStagingUrls = new Properties();
		try {
			mStagingUrls.load(this.getClass().getResourceAsStream("/staging.properties"));
		} catch (IOException e) {
			SponsorPayLogger.e(TAG, "An error happened while initializing url provider", e);
		}
	}
	
	@Override
	public String getBaseUrl(String product) {
		if (StringUtils.notNullNorEmpty(mOverridingUrl)) {
			return mOverridingUrl;
		}
		if (mUseStaging) {
			return mStagingUrls.getProperty(product);
		}
		return null;
	}

	public void setOverridingUrl(String mOverrideUrl) {
		this.mOverridingUrl = mOverrideUrl;
	}
	
	public void shouldUseStaging(boolean useStaging) {
		this.mUseStaging = useStaging;
	}
	
}
