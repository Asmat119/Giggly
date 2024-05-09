package com.wit.giggly;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wit.giggly.Fragments.UpdatePromptDialogFragment;

public class SplashActivity extends AppCompatActivity {


    private static final long SPLASH_DELAY = 2500;
    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TypedValue typedValue = new TypedValue();
            getTheme().resolveAttribute(R.attr.optionPageBackground2, typedValue, true);

            boolean isDarkTheme = ThemeSaver.getThemePreference(this);

            if (isDarkTheme) {
                View decor = getWindow().getDecorView();
                decor.setSystemUiVisibility(0);
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.darkdarkgrey));
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

            } else {
                View decor = getWindow().getDecorView();
                decor.setSystemUiVisibility(0);
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.yellow));
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }

        }
        checkUpdate();
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//
//                goToNextScreen();
//            }
//        }, SPLASH_DELAY);


    }

    void checkUpdate() {
        try {
            FirebaseDatabase.getInstance().getReference().child("versionCode")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists() && snapshot.getValue() != null) {
                                long dbVersionCode = snapshot.getValue(Long.class);
                                Log.d(TAG, "dbVersionCode: " + dbVersionCode);
                                if (dbVersionCode > BuildConfig.VERSION_CODE) {
                                    showUpdateDialog();
                                } else {
                                    // No update available
                                    goToNextScreen();
                                }

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getLocalizedMessage());
        }

    }

    private void goToNextScreen() {
        Intent mainIntent = new Intent(SplashActivity.this, StartActivity.class);
        startActivity(mainIntent);
        finish();
    }

    private void showUpdateDialog() {
        UpdatePromptDialogFragment dialog = new UpdatePromptDialogFragment();
        dialog.setOnClickListener(new UpdatePromptDialogFragment.OnDialogClickListener() {
            @Override
            public void onPositiveClickListener() {
                openPlayStore();
            }

            @Override
            public void onNegativeClickListener() {
                finishAndRemoveTask();
            }
        });
        dialog.show(getSupportFragmentManager(), null);
    }

    private void openPlayStore() {
        Uri uri = Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);

        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID));
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "No play store or browser app", Toast.LENGTH_LONG).show();
            }
        }
    }


}

