package com.sponsorpay.mediation;

import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;

import com.sponsorpay.credentials.SPCredentials;
import com.sponsorpay.utils.SignedResponseRequester;
import com.sponsorpay.utils.SignedServerResponse;
import com.sponsorpay.utils.SponsorPayBaseUrlProvider;
import com.sponsorpay.utils.SponsorPayLogger;
import com.sponsorpay.utils.StringUtils;
import com.sponsorpay.utils.UrlBuilder;

/**
 * <p>
 * Requests and loads a resource using the HTTP GET method in the background by using
 * the AsyncTaskRequester doInBackground method (Uses the Android {@link AsyncTask} 
 * mechanism.). Will save the response on the shared preferences and will update
 * the SPMediationConfigurator's Map<String, Map<String, Object>> mConfigurations.
 * </p>
 */
public class SPMediationConfigurationRequester extends SignedResponseRequester<SignedServerResponse>{
	
	public  static final String TAG = "ConfigurationRequester";
	private static final String SERVER_SIDE_CONFIG_URL_KEY = "config";
	
	private Activity mActivity;
	private String mSecurityToken;

	
	public static void requestConfig(SPCredentials credentials, Activity activity) {
		
		UrlBuilder urlBuilder = UrlBuilder.newBuilder(getBaseUrl(), credentials).addSignature();
		
		new SPMediationConfigurationRequester(activity, credentials.getSecurityToken()).execute(urlBuilder);
	}
	
	private static String getBaseUrl() {
		return SponsorPayBaseUrlProvider.getBaseUrl(SERVER_SIDE_CONFIG_URL_KEY);
	}
	
	private SPMediationConfigurationRequester(Activity activity, String securityToken) {		
		mActivity = activity;
		mSecurityToken = securityToken;
	}
	
	
	/**
	 * @param result - the body from the HTTP request.
	 */
	@Override
	protected void onPostExecute(SignedServerResponse result) {
		// even though this is runned inside UI thread, the method below will spawn a new background thread
		SPMediationCoordinator.INSTANCE.startMediationAdapters(mActivity);
	}
	

	@Override
	protected String getTag() {
		return TAG;
	}

	/**
	 * Check if the response body retrieved from the HTTP request 
	 * on AsyncTaskRequester is empty or null. In case that isn't, 
	 * saves the response as a shared preference. Finally we've logged
	 * if the shared preference commit was successful or not. This
	 * process is taking place on a background thread.
	 * 
	 * @param signedServerResponse - the body from the HTTP request.
	 */
	@Override
	protected SignedServerResponse parsedSignedResponse(
			SignedServerResponse signedServerResponse) {
		String json = StringUtils.EMPTY_STRING;
		SharedPreferences sharedpreferences = mActivity
				.getSharedPreferences(TAG, Context.MODE_PRIVATE);
		if (signedServerResponse != null && verifySignature(signedServerResponse, mSecurityToken)
				&& !hasErrorStatusCode(signedServerResponse.getStatusCode())) {

			String responseBody = signedServerResponse.getResponseBody();

			if (StringUtils.notNullNorEmpty(responseBody)) {

				Editor editor = sharedpreferences.edit();
				editor.putString(TAG, responseBody);

				if (editor.commit() ) {
					SponsorPayLogger.d(TAG,	"Server Side Configuration has been saved successfully.");
				} else {
					SponsorPayLogger.d(TAG, "Failed to save Server Side Configuration.");
				}
				
				json = responseBody;
				
			}
		}
		if (StringUtils.nullOrEmpty(json)) {
			// retrieve info from the store preferencs, if any
			SponsorPayLogger.d(TAG, "Using previously stored json file");
			json = sharedpreferences.getString(TAG, StringUtils.EMPTY_STRING);
		}
		overrideConfig(json);
		
		return signedServerResponse;
	}
	
	private void overrideConfig(String json) {
		if (StringUtils.notNullNorEmpty(json)) {
			Map<String, Map<String, Object>> settingsMapFromResponseBody = SPMediationConfigurator
					.parseConfiguration(json);
	
			// iterate on all server side configurations
			for (Entry<String, Map<String, Object>> entry : settingsMapFromResponseBody
					.entrySet()) {
	
				String network = entry.getKey();
				Map<String, Object> serverConfigs = entry.getValue();
				Map<String, Object> localConfigs = SPMediationConfigurator.INSTANCE
						.getConfigurationForAdapter(network);
	
				if(localConfigs != null) {
					serverConfigs.putAll(localConfigs);
				}
				SPMediationConfigurator.INSTANCE.setConfigurationForAdapter(network,
								serverConfigs);
			}
		} else {
			SponsorPayLogger.d(TAG, "There were no server side credentials to override");
		}
	}

}