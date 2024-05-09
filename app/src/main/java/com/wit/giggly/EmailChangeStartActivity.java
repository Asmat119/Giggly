package com.wit.giggly;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wit.giggly.Model.User;

public class EmailChangeStartActivity extends AppCompatActivity {

    private TextView emailDetails;

    private FirebaseAuth mAuth;
    private Button changeEmail;
    private DatabaseReference mDatabase;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_change_start);

        emailDetails = findViewById(R.id.emailDetails);
        changeEmail = findViewById(R.id.ChangeEmail);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();


        //https://stackoverflow.com/questions/42433899/setstatusbarcolor-not-working
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
            actionBar.setTitle("Change email");
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
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

                finish();
            }
        });


        changeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EmailChangeStartActivity.this, ChangeEmailActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
                overridePendingTransition(R.anim.slide_in_up,R.anim.slide_out_down);
            }
        });

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference userRef = mDatabase.child("Users").child(userId);

            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                        User user = dataSnapshot.getValue(User.class);

                        String email = user.getEmail();



                        emailDetails.setText(email);

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle database error
                }
            });
        }
    }

    public void onBackPressed()
    {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left,
                R.anim.slide_out_right);
    }
}