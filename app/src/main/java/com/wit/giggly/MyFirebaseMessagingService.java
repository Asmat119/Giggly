package com.wit.giggly;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.wit.giggly.Model.MyCustomNotification;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

   @Override
   public void onMessageReceived(@NonNull RemoteMessage message) {
      super.onMessageReceived(message);

      if (message.getNotification() != null) {
         // Handle notification messages
         String title = message.getNotification().getTitle();
         String body = message.getNotification().getBody();
         showNotification(title, body);
      }

      if (message.getData().size() > 0) {
         // Handle data messages
         // You can process additional data here if needed
      }
   }

   private void showNotification(String title, String body) {
      // Create a notification channel if not already created
      createNotificationChannel();

      // Create an intent to launch the NotificationsActivity when the notification is tapped
      Intent intent = new Intent(this, NotificationsActivity.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
      PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE); // Add the FLAG_IMMUTABLE flag

      // Build the notification
      NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "notify")
              .setSmallIcon(R.drawable.giggly_logo_2024_jan)
              .setContentTitle(title)
              .setContentText(body)
              .setPriority(NotificationCompat.PRIORITY_DEFAULT)
              .setAutoCancel(true)
              .setContentIntent(pendingIntent);

      // Display the notification
      NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
      notificationManagerCompat.notify(generateNotificationId(), builder.build());
   }

   private void createNotificationChannel() {
      // Create the notification channel (if needed)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
         CharSequence name = "notify";
         String description = "Likes, Comments, Follows";
         int importance = NotificationManager.IMPORTANCE_DEFAULT;
         NotificationChannel channel = new NotificationChannel("notify", name, importance);
         channel.setDescription(description);

         NotificationManager notificationManager = getSystemService(NotificationManager.class);
         notificationManager.createNotificationChannel(channel);
      }
   }

   private int generateNotificationId() {
      // Generate a unique notification ID
      return (int) System.currentTimeMillis();
   }

   @Override
   public void onNewToken(String s) {
      super.onNewToken(s);
      Log.e("newToken", s);
      getSharedPreferences("_", MODE_PRIVATE).edit().putString("fb", s).apply();
   }
}
