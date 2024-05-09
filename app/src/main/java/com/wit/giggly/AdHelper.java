package com.wit.giggly;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;

public class AdHelper {

    public void loadAndDisplayNativeAd(Context context, ViewGroup view) {
        //the adUnitID is an example ad id for test ads provided by google
        AdLoader adLoader = new AdLoader.Builder(context, "ca-app-pub-3881246768570702/1447674343")
                .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                    @Override
                    public void onNativeAdLoaded(NativeAd loadedAd) {
                        displayNativeAd(view, loadedAd);
                    }
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        view.setVisibility(View.GONE);
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder().build())
                .build();
        adLoader.loadAd(new AdRequest.Builder().build());
    }

    private void displayNativeAd(ViewGroup parent, NativeAd ad) {

        View alertloadingDialog = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_progress_bar, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(parent.getContext());
        builder.setView(alertloadingDialog);
        final AlertDialog pd = builder.create();
        pd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        pd.setCanceledOnTouchOutside(false);
        pd.show();

        // Inflate the native ad layout
        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        NativeAdView nativeAdView = (NativeAdView) inflater.inflate(R.layout.native_ad_layout, parent, false);
        TextView headlineView = nativeAdView.findViewById(R.id.ad_headline);
        if (TextUtils.isEmpty(ad.getHeadline())) {
            headlineView.setVisibility(View.INVISIBLE);
        } else {
            headlineView.setVisibility(View.VISIBLE);
            headlineView.setText(ad.getHeadline());
        }
        nativeAdView.setHeadlineView(headlineView);
        TextView callToActionView = nativeAdView.findViewById(R.id.ad_call_to_action);
        if (TextUtils.isEmpty(ad.getCallToAction())) {
            callToActionView.setVisibility(View.INVISIBLE);
        } else {
            callToActionView.setVisibility(View.VISIBLE);
            callToActionView.setText(ad.getCallToAction());
        }
        nativeAdView.setCallToActionView(callToActionView);
        ImageView iconView = nativeAdView.findViewById(R.id.ad_icon);
        NativeAd.Image icon = ad.getIcon();
        if (icon != null && icon.getDrawable() != null) {
            iconView.setImageDrawable(icon.getDrawable());
            iconView.setVisibility(View.VISIBLE);
        } else {
            iconView.setVisibility(View.GONE);
        }
        TextView advertiserView = nativeAdView.findViewById(R.id.ad_advertiser);
        if (TextUtils.isEmpty(ad.getAdvertiser())) {
            advertiserView.setVisibility(View.INVISIBLE);
        } else {
            advertiserView.setVisibility(View.VISIBLE);
            advertiserView.setText(ad.getAdvertiser());
        }
        nativeAdView.setAdvertiserView(advertiserView);
        TextView adBodyView = nativeAdView.findViewById(R.id.ad_body);
        if (TextUtils.isEmpty(ad.getBody())) {
            adBodyView.setVisibility(View.INVISIBLE);
        } else {
            adBodyView.setVisibility(View.VISIBLE);
            adBodyView.setText(ad.getBody());
        }
        nativeAdView.setBodyView(adBodyView);


        // Repeat the process for other asset views

        // Register the MediaView
        MediaView mediaView = nativeAdView.findViewById(R.id.ad_media);
        nativeAdView.setMediaView(mediaView);

        // Register the native ad object
        nativeAdView.setNativeAd(ad);

        // Ensure that the parent view doesn't already contain an ad view
        parent.removeAllViews();
        pd.dismiss();
        // Place the NativeAdView into the parent
        parent.addView(nativeAdView);
    }
}
