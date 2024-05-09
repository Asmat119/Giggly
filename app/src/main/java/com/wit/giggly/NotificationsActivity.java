package com.wit.giggly;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Notification;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.wit.giggly.Adapter.NotificationsAdapter;
import com.wit.giggly.Model.MyCustomNotification;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationsAdapter notificationAdapter;
    private SwipeRefreshLayout swiperefresh;
    private List<MyCustomNotification> notificationList;

    private static final String FCM_API = "https://fcm.googleapis.com/fcm/send";
    private static final String SERVER_KEY = "AAAAz-E70BY:APA91bG4JmyzoErCHnztd7d9Gg6pqQ2lAfn0xaX2pxX_UnbRGFjNE0LOwJyKhbuvzCu6crwXYv1zjBdL_XhdARiPdxbE5XC2sLOZ1qMIsDy69WOW1m0UDqt3h3QfC91q4IiYck2DBZpP";
    String currentDeviceToken = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        recyclerView = findViewById(R.id.notificationsrecycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(NotificationsActivity.this));
        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationsAdapter(NotificationsActivity.this, notificationList);
        recyclerView.setAdapter(notificationAdapter);

        swiperefresh = findViewById(R.id.swiperefresh);
        swiperefresh.setProgressBackgroundColorSchemeResource(R.color.yellow);
        swiperefresh.setColorSchemeColors(Color.BLACK);

        readNotifications();


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


        ///setting toolbar title and back buttons to white
        ///setting toolbar title and back buttons to white
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TypedValue typedValueTitle = new TypedValue();
        getTheme().resolveAttribute(R.attr.paragraph2, typedValueTitle, true);
        int titleColor = typedValueTitle.data;

        toolbar.setTitleTextColor(titleColor);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Notifications");
            actionBar.setDisplayHomeAsUpEnabled(true);

            // Resolve the color attribute dynamically for navigation icon tint
            TypedValue typedValueIcon = new TypedValue();
            getTheme().resolveAttribute(R.attr.paragraph2, typedValueIcon, true);
            int iconColor = typedValueIcon.data;

            Drawable drawable = toolbar.getNavigationIcon();
            if (drawable != null) {
                drawable.mutate();
                drawable.setTint(iconColor);
                toolbar.setNavigationIcon(drawable);
            }
        }


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                overridePendingTransition(R.anim.slide_in_left,
                        R.anim.slide_out_right);
                finish();
            }
        });

        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {


                notificationList.clear();


                readNotifications();

            }
        });
    }

    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left,
                R.anim.slide_out_right);
    }


    private void readNotifications() {
        try {
            currentDeviceToken = FirebaseMessaging.getInstance().getToken().toString();
            //Toast.makeText(this, currentDeviceToken, Toast.LENGTH_SHORT).show();
            Log.w("NotificationActivity", currentDeviceToken);
        } catch (Exception e) {
            Log.e("NotificationActivity", "Failed to retrieve device token", e);

        }

        FirebaseDatabase.getInstance().getReference().child("notifications").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                notificationList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MyCustomNotification notification = snapshot.getValue(MyCustomNotification.class);

                    // Check if notification is older than 1 week
                    long currentTime = System.currentTimeMillis();
                    long notificationTime = notification.getTimestamp();
                    long oneWeekInMillis = 7 * 24 * 60 * 60 * 1000; // 1 week in milliseconds
                    if (currentTime - notificationTime > oneWeekInMillis) {
                        // Delete the notification
                        snapshot.getRef().removeValue();
                    } else {
                        notificationList.add(notification);

                        // Check if notification is for the current user
                        if (notification.getGoingToUserID().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            //sendPushNotification(currentDeviceToken, notification.getSentByUserID(), "Liked your message");
                        }
                    }
                }

                Collections.reverse(notificationList);
                notificationAdapter.notifyDataSetChanged();
                swiperefresh.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }




    // Inside NotificationsActivity


}