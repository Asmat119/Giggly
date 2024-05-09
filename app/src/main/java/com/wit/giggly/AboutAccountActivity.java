package com.wit.giggly;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.squareup.picasso.Picasso;
import com.wit.giggly.Model.User;

public class AboutAccountActivity extends AppCompatActivity {

    private TextView usernameTextView;
    private TextView dateTextView;
    private ImageView imageProfile;
    private FirebaseAuth mAuth;
    private String userId;
    private DatabaseReference mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_account);
        usernameTextView = findViewById(R.id.username);
        dateTextView = findViewById(R.id.date);
        imageProfile = findViewById(R.id.image_profile);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();

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
            actionBar.setTitle("About Account");
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

        if (user != null) {
            userId = getIntent().getStringExtra("userId");
            if (userId == null) {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    userId = currentUser.getUid();
                } else {

                    return;
                }
            }
            DatabaseReference userRef = mDatabase.child("Users").child(userId);

            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                        User user = dataSnapshot.getValue(User.class);
                        String username = user.getUsername();
                        String date = user.getDate();
                        String imageUrl = user.getImageurl(); // Get the image URL from Firebase

                        usernameTextView.setText(username);
                        dateTextView.setText(date);


                        if(user.getImageurl().equals("default")){
                            imageProfile.setImageResource(R.drawable.no_profile_image);
                        }else {
                            Picasso.get().load(user.getImageurl()).into(imageProfile);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

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