package com.wit.giggly;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

public class changeEmailVerificationActivity extends AppCompatActivity {
    private DatabaseReference RootRef;
    private FirebaseAuth mAuth;
    private Button sendemailagain;
    ProgressDialog pd;
    private TextView youenteredemail;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        sendemailagain = findViewById(R.id.verifyEmailAgainBtn);
        youenteredemail = findViewById(R.id.youhaveenteredemailtxtbox);

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

        //setting the email text to bold
        String userEmail = user.getEmail();
        SpannableString spannableString = new SpannableString("You have entered " + userEmail + " as the email address for this account.");
        StyleSpan boldStyle = new StyleSpan(android.graphics.Typeface.BOLD);
        //https://stackoverflow.com/questions/37661755/how-to-have-bold-and-normal-text-in-same-textview-in-android
        spannableString.setSpan(boldStyle, 16, 16 + userEmail.length()+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        youenteredemail.setText(spannableString);

        sendemailagain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmailVerification();
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Handle the back button press to exit the app
        finishAffinity();
    }


    private void sendEmailVerification() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                pd.dismiss();
                                Toast.makeText(changeEmailVerificationActivity.this, "Verification email sent. Please check your email.", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(changeEmailVerificationActivity.this,LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));

                            } else {
                                pd.dismiss();
                                Toast.makeText(changeEmailVerificationActivity.this, "Failed to send verification email. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null && !currentUser.isEmailVerified()) {
            startActivity(new Intent(changeEmailVerificationActivity.this, LoginActivity.class));
            finish();
        }
    }


}