package com.sponsorpay.mediation;

import java.util.Map;
import java.util.Map.Entry;

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
	
	private Context mContext;
	private String mSecurityToken;

	
	public static void requestConfig(SPCredentials credentials, Context appcontext) {
		
		UrlBuilder urlBuilder = UrlBuilder.newBuilder(getBaseUrl(), credentials).addSignature();
		
		new SPMediationConfigurationRequester(appcontext, credentials.getSecurityToken()).execute(urlBuilder);
	}
	
	private static String getBaseUrl() {
		return SponsorPayBaseUrlProvider.getBaseUrl(SERVER_SIDE_CONFIG_URL_KEY);
	}
	
	private SPMediationConfigurationRequester(Context context, String securityToken) {		
		mContext = context;
		mSecurityToken = securityToken;
	}
	
	
	/**
	 * @param result - the body from the HTTP request.
	 */
	@Override
	protected void onPostExecute(SignedServerResponse result) {
		//do nothing
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
		
		if (signedServerResponse != null && verifySignature(signedServerResponse, mSecurityToken)
				&& !hasErrorStatusCode(signedServerResponse.getStatusCode())) {

			String responseBody = signedServerResponse.getResponseBody();

			if (StringUtils.notNullNorEmpty(responseBody)) {

				SharedPreferences sharedpreferences = mContext
						.getSharedPreferences(TAG, Context.MODE_PRIVATE);
				Editor editor = sharedpreferences.edit();
				editor.putString(TAG, responseBody);

				if (editor.commit() ) {
					SponsorPayLogger.d(TAG,	"Server Side Configuration has been saved successfully.");
				} else {
					SponsorPayLogger.d(TAG, "Failed to save Server Side Configuration.");
				}

				Map<String, Map<String, Object>> settingsMapFromResponseBody = SPMediationConfigurator
						.parseConfiguration(responseBody);

				// iterate on all server side configurations
				for (Entry<String, Map<String, Object>> entry : settingsMapFromResponseBody
						.entrySet()) {

					String network = entry.getKey();
					Map<String, Object> serverConfigs = entry.getValue();
					Map<String, Object> localConfigs = SPMediationConfigurator.INSTANCE
							.getConfigurationForAdapter(network);

					// if the existing configurations adapter doesn't contain
					// the key, then save everything
					if (localConfigs == null || localConfigs.size() == 0) {
						SPMediationConfigurator.INSTANCE.setConfigurationForAdapter(network,
										serverConfigs);

						// else check if a value that exists in the server side configuration
						// and doesn't exist on local one. Then we are writing the value to the local
					} else {
						for (Entry<String, Object> serverSettingsEntry : serverConfigs.entrySet()) {
							if (!localConfigs.containsKey(serverSettingsEntry.getKey())) {
								localConfigs.put(serverSettingsEntry.getKey(), serverSettingsEntry.getValue());
							}
						}
					}
				}
			}
			mContext = null;
		}
		return signedServerResponse;
	}

}