package com.wit.giggly;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.wit.giggly.R;
import com.wit.giggly.BuildConfig;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.auth.FirebaseAuth;



public class StartActivity extends AppCompatActivity {

    private Button signin;
    private Button signup;
    public Button updateButton;
    private Button OkBtn;
    private TextView disclaimer;
    private LinearLayout fullStartPage;
    private ViewGroup optionLayout;
    private TextView toTerms;
    private TextView toPriv;
    public static final String CHANNEL_ID = "1";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TypedValue typedValue = new TypedValue();
            getTheme().resolveAttribute(R.attr.optionPageBackground2, typedValue, true);

            boolean isDarkTheme = ThemeSaver.getThemePreference(this);

            if(isDarkTheme){
                View decor = getWindow().getDecorView();
                decor.setSystemUiVisibility(0);
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.darkdarkgrey));
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

            }else{
                View decor = getWindow().getDecorView();
                decor.setSystemUiVisibility(0);
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.yellow));
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }

        }

        disclaimer = findViewById(R.id.textView2);
        signin = findViewById(R.id.signinBtn);
        signup = findViewById(R.id.SignupBtn);
        OkBtn = findViewById(R.id.acceptBtn);
        fullStartPage = findViewById(R.id.fullstartpage);
        optionLayout = findViewById(R.id.optionLayout);
toPriv = findViewById(R.id.toPriv);
        toTerms = findViewById(R.id.toTerms);
        //all the disclaimer string under signin and signup buttons


        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.e("Device Token", "Error fetching token");
                            return;
                        }

                        String token = task.getResult();
                        Log.e("Token at start", token);
                       // Toast.makeText(StartActivity.this, token, Toast.LENGTH_SHORT).show();

                        // Save the device token into shared preferences
                        SharedPreferencesHelper.getInstance(StartActivity.this).saveDeviceToken(token);



                    }
                });



        createNotificationChannel();

            signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(StartActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartActivity.this, RegisterFirstPageActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            }
        });
        toPriv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartActivity.this, startPrivacyPolicy.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_up,R.anim.slide_out_down);

            }
        });
        toTerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartActivity.this, startTermsOfService.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_up,R.anim.slide_out_down);

            }
        });

    }



    private boolean isNetworkConnected() {
        //https://stackoverflow.com/questions/45581285/android-getactivenetwork-always-returns-null
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }


    private void showNoInternetDialog() {
        //https://stackoverflow.com/questions/42273188/problems-with-custom-layout-for-alertdialog
        View alertNoInternetDialog = LayoutInflater.from(StartActivity.this).inflate(R.layout.no_internet_dialog,null);
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(StartActivity.this);
        builder.setView(alertNoInternetDialog);


        OkBtn = alertNoInternetDialog.findViewById(R.id.acceptBtn);


        final AlertDialog dialog = builder.create();

        //https://stackoverflow.com/questions/10795078/dialog-with-transparent-background-in-android
        //make window behind popup transparent
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);



        OkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(StartActivity.this, MainActivity.class));
                finish();
            }
        });
    }
    //onstart, start in this activity, but check if the user is signed in or not, if they are go striaght to the main activity with the signed in users data reference.
    @Override
    protected void onStart() {
        super.onStart();

        if(FirebaseAuth.getInstance().getCurrentUser()!=null){

            if(isNetworkConnected() == true){
                startActivity(new Intent(StartActivity.this, MainActivity.class));
                finish();
            }else{

                fullStartPage.setVisibility(View.INVISIBLE);
                showNoInternetDialog();

            }




        }else{

        }
    }


    private int getPlayStoreVersionCode(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo("com.wit.giggly", 0);
            return pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {

            return -1;
        }
    }

    private void createNotificationChannel() {
        // Check if Android version is Oreo or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Define the notification channel
            CharSequence name = "Liked";
            String description = "Somebody likes your post...";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Get the NotificationManager service
            NotificationManager notificationManager = getSystemService(NotificationManager.class);

            // Create the notification channel
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showUpdatePopup() {
        //https://stackoverflow.com/questions/42273188/problems-with-custom-layout-for-alertdialog
        View alertDeleteDialog = LayoutInflater.from(StartActivity.this).inflate(R.layout.updateavailable,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this);
        builder.setView(alertDeleteDialog);

        updateButton = alertDeleteDialog.findViewById(R.id.acceptBtn);
builder.setCancelable(false);

        final AlertDialog dialog = builder.create();

        //https://stackoverflow.com/questions/10795078/dialog-with-transparent-background-in-android
        //make window behind popup transparent
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();


        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the Google Play Store for updating the app
                openPlayStoreForUpdate();
                dialog.dismiss();
            }
        });
    }
    private void openPlayStoreForUpdate() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
        }
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}