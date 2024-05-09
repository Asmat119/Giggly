package com.wit.giggly;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionsActivity extends AppCompatActivity {

    private TextView camerapermissionBtn;
    private TextView micpermissionBtn;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1;
    private static final int MIC_PERMISSION_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);

        camerapermissionBtn = findViewById(R.id.cameraPermissionBtn);
        micpermissionBtn = findViewById(R.id.micpermissionBtn);

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
            actionBar.setTitle("Permissions");
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

        camerapermissionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestCameraPermission();
            }
        });

        micpermissionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestMicPermission();
            }
        });
    }

    private void requestCameraPermission() {
        //https://stackoverflow.com/questions/36003047/contextcompat-checkselfpermissioncontext-manifest-permission-camera-alway-ret
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }else{
            Toast.makeText(this, "Camera permission granted already", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestMicPermission() {
        //https://stackoverflow.com/questions/36003047/contextcompat-checkselfpermissioncontext-manifest-permission-camera-alway-ret
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, MIC_PERMISSION_REQUEST_CODE);
        }else{
            Toast.makeText(this, "Microphone permission granted already", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        //https://stackoverflow.com/questions/32714787/android-m-permissions-onrequestpermissionsresult-not-being-called
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == MIC_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Microphone permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Microphone permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onBackPressed()
    {
        startActivity(new Intent(PermissionsActivity.this, OptionsActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));

        overridePendingTransition(R.anim.slide_in_left,
                R.anim.slide_out_right);
    }
}
