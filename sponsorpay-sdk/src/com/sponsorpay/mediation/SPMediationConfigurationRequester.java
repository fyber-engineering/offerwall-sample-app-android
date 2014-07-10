package com.sponsorpay.mediation;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;

import com.sponsorpay.credentials.SPCredentials;
import com.sponsorpay.utils.AsyncTaskRequester;
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
public class SPMediationConfigurationRequester extends AsyncTaskRequester{
	
	public  static final String TAG = "ConfigurationRequester";
	private static final String SERVER_SIDE_CONFIG_URL_KEY = "config";
	
	private Context context;
	private static String mSecurityToken;

	
	public static void requestConfig(SPCredentials credentials, Context appcontext) {
		
		UrlBuilder urlBuilder = UrlBuilder.newBuilder(getBaseUrl(), credentials).addSignature();
		
		mSecurityToken = credentials.getSecurityToken();
				
		new SPMediationConfigurationRequester(appcontext).execute(urlBuilder);
	}
	
	private static String getBaseUrl() {
		return SponsorPayBaseUrlProvider.getBaseUrl(SERVER_SIDE_CONFIG_URL_KEY);
	}
	
	private SPMediationConfigurationRequester(Context context) {		
		this.context = context;
	}
	
	
	/**
	 * @param result - the body from the HTTP request.
	 */
	@Override
	protected void onPostExecute(SignedServerResponse result) {
		if(result != null){
			saveResponseOnSharedPreferences(result);
		}
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
	 * @param result - the body from the HTTP request.
	 */
	private void saveResponseOnSharedPreferences(final SignedServerResponse result) {
		
		class ExecuteOnBackground implements Runnable {
		
			@Override
			public void run() {

				if (verifySignature(
						result.getResponseBody(),result.getResponseSignature(), mSecurityToken)
						&& !hasErrorStatusCode(result.getStatusCode())) {

					String responseBody = result.getResponseBody();

					SharedPreferences sharedpreferences = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
					Editor editor = sharedpreferences.edit();

					if (StringUtils.notNullNorEmpty(responseBody)) {

						editor.putString(TAG, responseBody);

						boolean isSuccessfulCommit = editor.commit();

						if (isSuccessfulCommit) {
							
							Map<String, Map<String, Object>> settingsMap = SPMediationConfigurator.parseConfiguration(responseBody);
							
							SponsorPayLogger.d(getClass().getSimpleName(), "Server Side Configuration has been saved successfully.");
							
							//get the list of all keys from the existing configuration
							Set<String> allKeys = SPMediationConfigurator.INSTANCE.allConfigurationKeys();
							
							//iterate on all server side configurations
							for (Entry<String, Map<String, Object>> entry : settingsMap.entrySet()) {			
								
								//if the existing config doesn't contain the key from the
								//server side config, then add it into.
								if(!allKeys.contains(entry.getKey())){
									SPMediationConfigurator.INSTANCE.setConfigurationForAdapter(entry.getKey(), entry.getValue());
								}
								
							}

						} else {
							SponsorPayLogger.d(getClass().getSimpleName(),"Failed to save Server Side Configuration.");

						}
					}
					context = null;
				}
			}
		}
		
		Thread thread = new Thread(new ExecuteOnBackground());
		thread.start(); 
	}

}