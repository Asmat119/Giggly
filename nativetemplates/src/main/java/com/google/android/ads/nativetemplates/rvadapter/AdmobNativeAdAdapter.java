package com.google.android.ads.nativetemplates.rvadapter;

import static androidx.core.content.res.ResourcesCompat.getColor;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.ads.nativetemplates.NativeTemplateStyle;
import com.google.android.ads.nativetemplates.R;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.NativeAdOptions;

/**
 * Created by thuanle on 2/12/17.
 */
public class AdmobNativeAdAdapter extends RecyclerViewAdapterWrapper {
    private static final int TYPE_FB_NATIVE_ADS = 900;
    private static final int DEFAULT_AD_ITEM_INTERVAL = 4;
    private final Param mParam;
    Context context;
    NativeTemplateStyle.Builder builder = new NativeTemplateStyle.Builder();

    private final int mLightBackgroundColor;
    private final int mDarkBackgroundColor;
    private final int mLightTextColor;
    private final int mDarkTextColor;

    private AdmobNativeAdAdapter(Param param,Context context) {
        super(param.adapter);
        this.mParam = param;
        this.context=context;

        mLightBackgroundColor = ContextCompat.getColor(context, R.color.gnt_white);
        mDarkBackgroundColor = ContextCompat.getColor(context, R.color.gnt_black);
        mLightTextColor = ContextCompat.getColor(context, R.color.gnt_black);
        mDarkTextColor = ContextCompat.getColor(context, R.color.gnt_white);
        assertConfig();
        setSpanAds();
    }

    private void assertConfig() {
        if (mParam.gridLayoutManager != null) {
            //if user set span ads
            int nCol = mParam.gridLayoutManager.getSpanCount();
            if (mParam.adItemInterval % nCol != 0) {
                throw new IllegalArgumentException(String.format("The adItemInterval (%d) is not divisible by number of columns in GridLayoutManager (%d)", mParam.adItemInterval, nCol));
            }
        }
    }

    private int convertAdPosition2OrgPosition(int position) {

        return position - (position + 1) / (mParam.adItemInterval + 1);
    }

    @Override
    public int getItemCount() {
        int realCount = super.getItemCount();
        return realCount + realCount / mParam.adItemInterval;
    }

    @Override
    public int getItemViewType(int position) {
        if (isAdPosition(position)) {
            return TYPE_FB_NATIVE_ADS;
        }
        return super.getItemViewType(convertAdPosition2OrgPosition(position));
    }

    private boolean isAdPosition(int position) {
        /*if(position==1|| position==4)return true;*/
        return (position + 1) % (mParam.adItemInterval + 1) == 0;
    }
    public static final boolean isValidPhoneNumber(CharSequence target) {
        if (target.length()!=10) {
            return false;
        } else {
            return android.util.Patterns.PHONE.matcher(target).matches();
        }
    }
    private void onBindAdViewHolder(final RecyclerView.ViewHolder holder) {

        final AdViewHolder adHolder = (AdViewHolder) holder;
        if (mParam.forceReloadAdOnBind || !adHolder.loaded) {
            AdLoader adLoader = new AdLoader.Builder(adHolder.getContext(), mParam.admobNativeId)
                    .forNativeAd(NativeAd -> {
                        /*NativeTemplateStyle styles = new
                                NativeTemplateStyle.Builder().withMainBackgroundColor().build();*/
                        //adHolder.template.setStyles(styles);
                        Log.e("admobnative","loaded");
                        builder = new NativeTemplateStyle.Builder();
//                        setNativeAdThemeModel();

                        builder.withPrimaryTextSize(11f);
                        builder.withSecondaryTextSize(10f);
                        builder.withTertiaryTextSize(06f);
                        builder.withCallToActionTextSize(11f);
                        if(mParam.layout==0){
                            adHolder.templatesmall.setVisibility(View.VISIBLE);
                            adHolder.templatesmall.setStyles(builder.build());
                            adHolder.templatesmall.setNativeAd(NativeAd);
                        }
                        else if(mParam.layout==1){
                            adHolder.templatemedium.setVisibility(View.VISIBLE);
                            adHolder.templatemedium.setStyles(builder.build());
                            adHolder.templatemedium.setNativeAd(NativeAd);
                        }
                        else {
                            adHolder.templatecustom.setVisibility(View.VISIBLE);
                            adHolder.templatecustom.setStyles(builder.build());
                            adHolder.templatecustom.setNativeAd(NativeAd);
                        }
                        adHolder.loaded=true;

                    })
                    .withAdListener(new AdListener() {
                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            super.onAdFailedToLoad(loadAdError);
                            Log.e("admobnative","error:"+loadAdError);
                            adHolder.adContainer.setVisibility(View.GONE);
                            // Handle the failure by logging, altering the UI, and so on.
                        }
                    })
                    .withNativeAdOptions(new NativeAdOptions.Builder()
                            // Methods in the NativeAdOptions.Builder class can be
                            // used here to specify individual options settings.
                            .build())
                    .build();
            adLoader.loadAd(new AdRequest.Builder().build());
        }
    }

    public void setNativeAdThemeModel() {
        ColorDrawable colorDrawableDark = new ColorDrawable(context.getResources().getColor(R.color.dark_gray));
        ColorDrawable buttonColor = new ColorDrawable(context.getResources().getColor(R.color.yellows));
        ColorDrawable lightColor = new ColorDrawable(context.getResources().getColor(R.color.gnt_white));
        builder = new NativeTemplateStyle.Builder();
        builder.withCallToActionBackgroundColor(buttonColor).build();
        switch (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                builder.withMainBackgroundColor(colorDrawableDark).build();

//                builder.withSecondaryTextBackgroundColor(lightColor).build();
//                builder.withPrimaryTextBackgroundColor(lightColor).build();
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                builder.withMainBackgroundColor(lightColor).build();
//                builder.withSecondaryTextBackgroundColor(colorDrawableDark).build();
//                builder.withPrimaryTextBackgroundColor(colorDrawableDark).build();
                break;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_FB_NATIVE_ADS) {
            onBindAdViewHolder(holder);
        } else {
            super.onBindViewHolder(holder, convertAdPosition2OrgPosition(position));
        }
    }

    private RecyclerView.ViewHolder onCreateAdViewHolder(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View adLayoutOutline = inflater
                .inflate(mParam.itemContainerLayoutRes, parent, false);
        ViewGroup vg = adLayoutOutline.findViewById(mParam.itemContainerId);

        LinearLayout adLayoutContent = (LinearLayout) inflater
                .inflate(R.layout.item_admob_native_ad, parent, false);
        vg.addView(adLayoutContent);
        return new AdViewHolder(adLayoutOutline);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_FB_NATIVE_ADS) {
            return onCreateAdViewHolder(parent);
        }
        return super.onCreateViewHolder(parent, viewType);
    }

    private void setSpanAds() {
        if (mParam.gridLayoutManager == null) {
            return ;
        }
        final GridLayoutManager.SpanSizeLookup spl = mParam.gridLayoutManager.getSpanSizeLookup();
        mParam.gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (isAdPosition(position)){
                    return spl.getSpanSize(position);
                }
                return 1;
            }
        });
    }

    private static class Param {
        String admobNativeId;
        RecyclerView.Adapter adapter;
        int adItemInterval;
        boolean forceReloadAdOnBind;

        int layout;

        @LayoutRes
        int itemContainerLayoutRes;

        @IdRes
        int itemContainerId;

        GridLayoutManager gridLayoutManager;
    }

    public static class Builder {
        private final Param mParam;
        private final Context mContext;

        private Builder(Param param,Context context) {
            mParam = param;
            mContext = context;
        }

        public static Builder with(String placementId, RecyclerView.Adapter wrapped, String layout,Context context) {
            Param param = new Param();

            param.admobNativeId = placementId;
            param.adapter = wrapped;
            if(layout.toLowerCase().equals("small")){
                param.layout=0;
            }

            else if(layout.toLowerCase().equals("medium")){

                param.layout=1;
            }
            else {
                param.layout=2;

            }

            //default value
            param.adItemInterval = DEFAULT_AD_ITEM_INTERVAL;
            param.itemContainerLayoutRes = R.layout.item_admob_native_ad_outline;
            param.itemContainerId = R.id.ad_container;
            param.forceReloadAdOnBind = true;
            return new Builder(param,context);
        }

        public Builder adItemInterval(int interval) {
            mParam.adItemInterval = interval;
            return this;
        }

        public Builder adLayout(@LayoutRes int layoutContainerRes, @IdRes int itemContainerId) {
            mParam.itemContainerLayoutRes = layoutContainerRes;
            mParam.itemContainerId = itemContainerId;
            return this;
        }

        public AdmobNativeAdAdapter build() {
            return new AdmobNativeAdAdapter(mParam,mContext);
        }

        public Builder enableSpanRow(GridLayoutManager layoutManager) {
            mParam.gridLayoutManager = layoutManager;
            return this;
        }
        public Builder adItemIterval(int i) {
            mParam.adItemInterval=i;
            return this;
        }

        public Builder forceReloadAdOnBind(boolean forced) {
            mParam.forceReloadAdOnBind = forced;
            return this;
        }
    }

    private static class AdViewHolder extends RecyclerView.ViewHolder {
        TemplateView templatesmall,templatemedium,templatecustom;
        LinearLayout adContainer;
        boolean loaded;
        AdViewHolder(View view) {
            super(view);
            templatesmall=view.findViewById(R.id.my_templatesmall);
            templatecustom=view.findViewById(R.id.my_templatecustom);
            templatemedium=view.findViewById(R.id.my_templatemedium);
            loaded = false;
            adContainer = (LinearLayout)view.findViewById(R.id.native_ad_container);

        }
        Context getContext() {
            return adContainer.getContext();
        }
    }
}