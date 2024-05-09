package com.wit.giggly;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

public class AboutActivity extends AppCompatActivity {

    private LinearLayout aboutYourAccount;
    private LinearLayout privacyPolicy;
    private LinearLayout termsofservice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        aboutYourAccount = findViewById(R.id.aboutYourAccount);
        privacyPolicy = findViewById(R.id.privacyPolicy);
        termsofservice = findViewById(R.id.termsofUse);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            boolean isNightModeEnabled = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                    == Configuration.UI_MODE_NIGHT_YES;

            if(isNightModeEnabled){
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
        ///setting toolbar title and back buttons to white
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TypedValue typedValueTitle = new TypedValue();
        getTheme().resolveAttribute(R.attr.paragraph2, typedValueTitle, true);
        int titleColor = typedValueTitle.data;

        toolbar.setTitleTextColor(titleColor);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("About");
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

        aboutYourAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AboutActivity.this, AboutAccountActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));

                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

            }
        });
        privacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AboutActivity.this, PrivacyPolicyActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));

                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

            }
        });
        termsofservice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AboutActivity.this, TermsOfUseActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));

                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

            }
        });
    }

    public void onBackPressed()
    {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left,
                R.anim.slide_out_right);
    }
}