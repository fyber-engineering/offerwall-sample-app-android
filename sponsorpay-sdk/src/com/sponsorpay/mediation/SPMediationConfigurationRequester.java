package com.sponsorpay.mediation;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;

import com.sponsorpay.credentials.SPCredentials;
import com.sponsorpay.utils.AsyncTaskRequester;
import com.sponsorpay.utils.CurrencyAndMediationServerResponse;
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
public class SPMediationConfigurationRequester extends AsyncTaskRequester {
	
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
	 * Check if the response body retrieved from the 
	 * HTTP request above is empty or null. In
	 * case that isn't, saves the response as a
	 * shared preference. Finally we've logged
	 * if the shared preference commit was
	 * successful or not.
	 * 
	 * @param result - the body from the HTTP request.
	 */
	@Override
	protected void onPostExecute(CurrencyAndMediationServerResponse result) {

		if (AsyncTaskRequester.verifySignature(result.getResponseBody(), result.getResponseSignature(), mSecurityToken)
				&& !AsyncTaskRequester.hasErrorStatusCode(result.getStatusCode())) {

			String responseBody = result.getResponseBody();

			SharedPreferences sharedpreferences = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
			Editor editor = sharedpreferences.edit();

			if (StringUtils.notNullNorEmpty(responseBody)) {

				editor.putString(TAG, responseBody);

				boolean isSuccessfulCommit = editor.commit();

				if (isSuccessfulCommit) {

					setEachIndividualServerSideConfig(responseBody);
					SponsorPayLogger.d(getClass().getSimpleName(),
							"Server Side Configuration has been saved successfully.");

				} else {
					SponsorPayLogger.d(getClass().getSimpleName(), "Failed to save Server Side" + " Configuration.");

				}
			}

			context = null;
		}
	}
	
	/**
	 * Get the result, which is the JSON response from the 
	 * server and save into the SPMediationConfigurator's 
	 * Map<String, Map<String, Object>> mConfigurations.
	 */
	private void setEachIndividualServerSideConfig(String result){
		try {
			
			JSONObject resultAsJSONObject = new JSONObject(result);
			
			JSONArray jsonArrayForAdapters  = resultAsJSONObject.getJSONArray("adapters");	
			
			for (int i = 0; i < jsonArrayForAdapters.length(); i++) {
				
				JSONObject adapterObject = jsonArrayForAdapters.getJSONObject(i);
				JSONObject adapterSettingsValues = adapterObject.getJSONObject("settings");
				
				Map<String, Object> settingsMap = new HashMap<String, Object>();
				if(adapterSettingsValues != null && adapterSettingsValues.length() != 0){
					
					String settingsToString = adapterSettingsValues.toString();
					//remove the curly braces
					settingsToString = settingsToString.substring(1, settingsToString.length() - 1);
					
					String[] fields = settingsToString.split(",");
					
					for(String entry: fields){
						String[] keyValueSplitter = entry.split(":");
						settingsMap.put(keyValueSplitter[0], keyValueSplitter[1]);
					}
					
				}	
				SPMediationConfigurator.INSTANCE.setConfigurationForAdapter(adapterObject.getString("name"), settingsMap);
			}
			
		} catch (JSONException e) {
			SponsorPayLogger.d(TAG, "Error while parsing Server Side Configuration."+ e.getMessage());
		}
		
	}

}