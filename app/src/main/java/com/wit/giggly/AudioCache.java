package com.wit.giggly;

import android.net.Uri;

public class AudioCache {
   private static String audioFilePath;

   public static void setAudioFilePath(String path) {
      audioFilePath = path;
   }

   public static String getAudioFilePath() {
      return audioFilePath;
   }

   // Additional methods for clearing or resetting the cache if needed
   public static void clearAudioCache() {
      audioFilePath = null;
   }
}