package com.wit.giggly;

import android.net.Uri;

public class ImageCache {
   private static Uri imageUri;

   public static void setImageUri(Uri uri) {
      imageUri = uri;
   }

   public static Uri getImageUri() {
      return imageUri;
   }
}