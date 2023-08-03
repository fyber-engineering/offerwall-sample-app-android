package com.fyber.offerwall.sampleapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fyber.fairbid.ads.OfferWall;
import com.fyber.fairbid.ads.offerwall.ShowOptions;
import com.fyber.fairbid.ads.offerwall.VirtualCurrencyRequestOptions;
import com.fyber.offerwall.sampleapp.R;

import java.util.HashMap;
import java.util.Map;

public class OfferwallFragment extends FyberFragment {
    private static final String TAG = OfferwallFragment.class.getSimpleName();

    Button offerwallButton;
    Button requestCurrencyButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_offerwall, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        offerwallButton = view.findViewById(R.id.offer_wall_button);
        offerwallButton.setOnClickListener(this::onOfferWallButtonClicked);
        requestCurrencyButton = view.findViewById(R.id.vcs_request_button);
        requestCurrencyButton.setOnClickListener(this::onRequestVcsButtonClicked);
        setButtonToSuccessState(offerwallButton, getShowText());
        setButtonToSuccessState(requestCurrencyButton, getRequestVcsText());
    }

    // using butter knife to link Button click
    public void onOfferWallButtonClicked(View view) {
        requestOrShowAd();
    }

    public void onRequestVcsButtonClicked(View view) {
        requestCurrency();
    }

    /*
     * ** Code to perform an Offer Wall request **
     */

    @Override
    protected void performRequest() {
        // You can request the offer wall with the default placement just by calling the following method:
        // OfferWall.show();

        // You can customize the OfferWall behaviour using the ShowOptions class
        boolean closeOnRedirect = true;
        Map<String, String> customParameters = new HashMap<>();
        customParameters.put("key", "value");
        ShowOptions showOptions = new ShowOptions(closeOnRedirect, customParameters);
        // If you don't need to pass the custom params with your show request,
        // you can omit this value within the ShowOptions constructor.
        // ShowOptions showOptions = new ShowOptions(closeOnRedirect);

        // Then, you can request to show the OfferWall with given options:
        OfferWall.show(showOptions);

        // If you need to display the offer wall for a given placement, you can achieve this by calling the following method:
        // OfferWall.show(showOptions, "placement Id");
    }

    /*
     * ** Code to perform a Virtual Currency request **
     */
    private void requestCurrency() {
        // If you want to request for default currency, you just can call this method:
        // OfferWall.requestCurrency();

        // If you need some behaviour customization or request for a specific custom currency,
        // you should use the VirtualCurrencyRequestOptions class, that accepts the following parameters:
        // - whether the toast message should appear upon the successful gratification,
        // - the ID of the currency that you want to request for. This parameter is optional.
        boolean showToastOnReward = true;
        String currencyId = "coins";
        VirtualCurrencyRequestOptions options = new VirtualCurrencyRequestOptions(showToastOnReward, currencyId);
        OfferWall.requestCurrency(options);
    }

    /*
     * ** FyberFragment methods **
     */

    @Override
    public String getLogTag() {
        return TAG;
    }

    @Override
    public String getRequestText() {
        return getString(R.string.show_offer_wall);
    }

    @Override
    public String getShowText() {
        return getString(R.string.show_offer_wall);
    }

    public String getRequestVcsText() {
        return getString(R.string.request_currency);
    }

    @Override
    public Button getButton() {
        return offerwallButton;
    }

    /*
     * ** Offer wall specific state methods **
     */

    @Override
    protected boolean isRequestingState() {
        //for our sample app, Offer Wall is never in the requesting sate. It is always ready to show.
        return false;
    }

    @Override
    protected void setButtonToRequestingMode() {
        //do nothing: there is only one state in Offer Wall
    }

    @Override
    protected void setButtonToOriginalState() {
        //do nothing: there is only one state in Offer Wall
    }
}
