package com.wit.giggly;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;

public class AdViewActivity extends AppCompatActivity {

    private TextView close;
    AdHelper adHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_view);
        adHelper = new AdHelper();

        close = findViewById(R.id.close);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            boolean isNightModeEnabled = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                    == Configuration.UI_MODE_NIGHT_YES;

            if (isNightModeEnabled) {
                View decor = getWindow().getDecorView();
                decor.setSystemUiVisibility(0);
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.darkdarkgrey));
            } else {
                View decor = getWindow().getDecorView();
                decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
            }

        }

        adHelper.loadAndDisplayNativeAd(this, findViewById(R.id.fl_adplaceholder));
//        loadAndDisplayNativeAd();

        new Handler().postDelayed(() -> {

            new Handler().postDelayed(() -> {
                MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.crowdlaughing07120585);
                mediaPlayer.setOnCompletionListener(MediaPlayer::release);
                mediaPlayer.start();
            }, 100);
//            startActivity(new Intent(AdViewActivity.this, MainActivity.class));
            close.setVisibility(View.VISIBLE);

        }, 11000);


        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdViewActivity.this, MainActivity.class));
                finish();
            }
        });
    }


    //https://developers.google.com/admob/android/native
    //https://developers.google.com/admob/android/native/advanced
    //https://github.com/googleads/googleads-mobile-android-examples/blob/main/java/admob/NativeAdvancedExample/app/src/main/java/com/google/example/gms/nativeadvancedexample/MainActivity.java

    private void loadAndDisplayNativeAd() {
        //the adUnitID is an example ad id for test ads provided by google
        AdLoader adLoader = new AdLoader.Builder(AdViewActivity.this, "ca-app-pub-3881246768570702/1447674343")
                .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                    @Override
                    public void onNativeAdLoaded(NativeAd loadedAd) {
                        displayNativeAd(findViewById(R.id.fl_adplaceholder), loadedAd);
                    }
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        findViewById(R.id.fl_adplaceholder).setVisibility(View.GONE);
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder().build())
                .build();
        adLoader.loadAd(new AdRequest.Builder().build());
    }

    private void displayNativeAd(ViewGroup parent, NativeAd ad) {

        View alertloadingDialog = LayoutInflater.from(AdViewActivity.this).inflate(R.layout.custom_progress_bar, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(AdViewActivity.this);
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

    @Override
    public void onBackPressed() {
    }
}