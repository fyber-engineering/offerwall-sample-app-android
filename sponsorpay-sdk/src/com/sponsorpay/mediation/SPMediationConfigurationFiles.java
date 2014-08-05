/**
 * SponsorPay Android SDK
 *
 * Copyright 2011 - 2014 SponsorPay. All rights reserved.
 */

package com.sponsorpay.mediation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import android.webkit.URLUtil;

import com.sponsorpay.utils.GetRemoteFileContentTask;
import com.sponsorpay.utils.SponsorPayLogger;
import com.sponsorpay.utils.StringUtils;

public class SPMediationConfigurationFiles {

	private static final String TAG = "SPMediationConfigurationFiles";

	private static SPMediationConfigurationFiles instance = new SPMediationConfigurationFiles();

	private String adaptersConfigLocation = "/adapters.config";
	private String adaptersInfoLocation = "/adapters.info";

	private String adaptersConfig = null;
	private String adaptersInfo = null;

	private SPMediationConfigurationFiles() {
	}

	public static String getAdapterInfo() {
		if (StringUtils.notNullNorEmpty(instance.adaptersInfo)) {
			return instance.adaptersInfo;
		}
		return instance.getContent(instance.adaptersInfoLocation);
	}

	public static String getAdaptersConfig() {
		if (StringUtils.notNullNorEmpty(instance.adaptersConfig)) {
			return instance.adaptersConfig;
		}
		return instance.getContent(instance.adaptersConfigLocation);
	}

	private String getContent(String urlString) {
		if (URLUtil.isNetworkUrl(urlString)) {
			try {
				return new GetRemoteFileContentTask().execute(urlString).get();
			} catch (InterruptedException e) {
				SponsorPayLogger.e(TAG, "Error occured", e);
			} catch (ExecutionException e) {
				SponsorPayLogger.e(TAG, "Error occured", e);
			}
		} else {
			try {
				return readFile(urlString);
			} catch (IOException e) {
				SponsorPayLogger.e(TAG, "Error occured", e);
			} catch (URISyntaxException e) {
				SponsorPayLogger.e(TAG, "Error occured", e);
			}
		}
		return StringUtils.EMPTY_STRING;
	}

	private String readFile(String file) throws IOException, URISyntaxException {
		String content = null;
		InputStream is = getClass().getResourceAsStream(file);
		if (is == null) {
			try {
				is = URI.create(file).toURL().openStream();
			} catch (Exception e) {
				// do nothing
			}
		}
		if (is != null) {
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			try {
				StringBuilder sb = new StringBuilder();
				String line = br.readLine();

				while (line != null) {
					sb.append(line + '\n');
					line = br.readLine();
				}
				content = sb.toString();
			} finally {
				br.close();
			}
		}
		return content;
	}

	public static void setAdaptersConfigLocation(String configLocation) {
		if (StringUtils.notNullNorEmpty(configLocation)) {
			instance.adaptersConfigLocation = configLocation;
		}
	}

	public static void setAdaptersInfoLocation(String infoLocation) {
		if (StringUtils.notNullNorEmpty(infoLocation)) {
			instance.adaptersInfoLocation = infoLocation;
		}
	}

	public static void setAdaptersConfig(String config) {
		instance.adaptersConfig = config;
	}

	public static void setAdaptersInfo(String info) {
		instance.adaptersInfo = info;
	}

}
