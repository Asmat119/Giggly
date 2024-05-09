package com.wit.giggly.Model;

import com.google.android.gms.ads.nativead.NativeAd;

public abstract class BaseAdModel {
    private boolean isAd; // Add this field to indicate whether the item is an ad
    private NativeAd nativeAd; // Add this field to hold the native ad object

    // Constructor with ad parameter
    public BaseAdModel(boolean isAd, NativeAd adObject) {
        this.isAd = isAd;
        this.nativeAd = adObject;
    }

    // Getter and setter methods for the new fields
    public boolean isAd() {
        return isAd;
    }

    public void setAd(boolean ad) {
        isAd = ad;
    }

    public NativeAd getNativeAd() {
        return nativeAd;
    }

    public void setNativeAd(NativeAd nativeAd) {
        this.nativeAd = nativeAd;
    }
}
