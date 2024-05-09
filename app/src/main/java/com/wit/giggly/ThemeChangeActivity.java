package com.wit.giggly;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

public class ThemeChangeActivity extends AppCompatActivity {

    private ToggleButton themeToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_change);

        themeToggle = findViewById(R.id.themeToggle);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //https://stackoverflow.com/questions/60081671/how-the-setting-of-appcompatdelegate-mode-night-follow-system-relate-to-result-o
            boolean isDarkMode = ThemeSaver.getThemePreference(this);
            Log.e("Dark mode", ""+isDarkMode);
            if(isDarkMode){
                View decor = getWindow().getDecorView();
                decor.setSystemUiVisibility(0);
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.darkdarkgrey));


            }else{
                View decor = getWindow().getDecorView();
                decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));


            }


        }
        ///setting toolbar title and back buttons to white
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TypedValue typedValueTitle = new TypedValue();
        getTheme().resolveAttribute(R.attr.paragraph2, typedValueTitle, true);
        int titleColor = typedValueTitle.data;

        toolbar.setTitleTextColor(titleColor);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Theme");
            actionBar.setDisplayHomeAsUpEnabled(true);


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
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
            }
        });

        int currentNightMode = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        themeToggle.setChecked(currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES);

        themeToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                final AlertDialog pd;
                View alertloadingDialog = LayoutInflater.from(ThemeChangeActivity.this).inflate(R.layout.closing_progress_bar,null);
                AlertDialog.Builder builder = new AlertDialog.Builder(ThemeChangeActivity.this);
                builder.setView(alertloadingDialog);
                pd = builder.create();
                pd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                pd.setCanceledOnTouchOutside(false);
                pd.show();

                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        pd.dismiss(); // Dismiss the dialog before starting the new activity
                        startActivity(new Intent(ThemeChangeActivity.this, MainActivity.class));
                        finish();
                    }
                }, 2000);

                ThemeSaver.saveThemePreference(ThemeChangeActivity.this, isChecked);
                recreate();
            }
        });

//        themeToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                View alertloadingDialog = LayoutInflater.from(ThemeChangeActivity.this).inflate(R.layout.closing_progress_bar,null);
//                AlertDialog.Builder builder = new AlertDialog.Builder(ThemeChangeActivity.this);
//                builder.setView(alertloadingDialog);
//                final AlertDialog pd = builder.create();
//                pd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//                pd.setCanceledOnTouchOutside(false);
//                pd.show();
//                if (isChecked) {
//                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//                    Handler handler = new Handler();
//                    handler.postDelayed(new Runnable() {
//                        public void run() {
//                            startActivity(new Intent(ThemeChangeActivity.this,MainActivity.class));
//
//                            finish();
////                            ThemeChangeActivity.this.finishAffinity();
////                            pd.dismiss();
//                        }
//                    }, 2000);
//
//                } else {
//                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
//                    Handler handler = new Handler();
//                    handler.postDelayed(new Runnable() {
//                        public void run() {
//                            startActivity(new Intent(ThemeChangeActivity.this,MainActivity.class));
//
//                            finish();
//
//                            pd.dismiss();
//                        }
//                    }, 2000);
//                }
//                ThemeSaver.saveThemePreference(ThemeChangeActivity.this, isChecked);
//                recreate();
//            }
//        });


    }
}