package com.wit.giggly;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHelper {

   private static final String PREF_NAME = "MyAppPreferences";
   private static final String KEY_DEVICE_TOKEN = "deviceToken";

   private static SharedPreferencesHelper instance;
   private final SharedPreferences sharedPreferences;

   private SharedPreferencesHelper(Context context) {
      sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
   }

   public static synchronized SharedPreferencesHelper getInstance(Context context) {
      if (instance == null) {
         instance = new SharedPreferencesHelper(context.getApplicationContext());
      }
      return instance;
   }

   public void saveDeviceToken(String deviceToken) {
      SharedPreferences.Editor editor = sharedPreferences.edit();
      editor.putString(KEY_DEVICE_TOKEN, deviceToken);
      editor.apply();
   }

   public String getDeviceToken() {
      return sharedPreferences.getString(KEY_DEVICE_TOKEN, null);
   }

   public void clearDeviceToken() {
      SharedPreferences.Editor editor = sharedPreferences.edit();
      editor.remove(KEY_DEVICE_TOKEN);
      editor.apply();
      editor.commit();
   }
}
