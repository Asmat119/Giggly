package com.wit.giggly;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class emailVerificationActivity extends AppCompatActivity {
    private DatabaseReference RootRef;
    private FirebaseAuth mAuth;
    private Button sendemailagain;
    ProgressDialog pd;
    private TextView youenteredemail;
    private String firstname;
    private String secondname;
    private String username;
    private String email;
    private ImageView backbtn;
    private Handler handler;
    private ImageView refresh;

    private FirebaseAuth.AuthStateListener mAuthListener;




    //once verified then take the users details from cache and create the user in realtimedatabse
    //if the user clicks back remove the user from auth - done
    //if the user is not verified and has been sitting in this screen for a while, then just ask to click the submit button again
    //if the user closes the app full down, then remove user from auth, and remove any cache from memery, the cache will be removed anyways




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        sendemailagain = findViewById(R.id.verifyEmailAgainBtn);
        youenteredemail = findViewById(R.id.youhaveenteredemailtxtbox);
        backbtn = findViewById(R.id.backbtn);
        refresh = findViewById(R.id.refreshemail);
        RootRef = FirebaseDatabase.getInstance().getReference();
        firstname = RegisterCache.getregisterFirstname();
        secondname = RegisterCache.getregisterSecondname();
        username = RegisterCache.getregisterUsername();
        email = RegisterCache.getregisterEmail();
        handler = new Handler();



        //pd = new ProgressDialog(this, R.style.MyAlertDialogStyle);

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


        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                refresh.setVisibility(View.VISIBLE);
            }
        }, 5000);

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

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(emailVerificationActivity.this, "Refreshing...", Toast.LENGTH_SHORT).show();

                // Check the authentication state
                FirebaseAuth.getInstance().getCurrentUser().reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                           // registerUser(username, firstname, secondname, email);
                            RegisterCache.clearUsernameCache();
                            RegisterCache.clearEmail();
                            RegisterCache.clearFirstnameCache();
                            RegisterCache.clearSecondnameCache();
                            RegisterCache.clearPasswordCache();
                            RegisterCache.clearConfirmpasswordCache();
                            Intent intent = new Intent(emailVerificationActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                            finish();
                           // Toast.makeText(emailVerificationActivity.this, "User Registered", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(emailVerificationActivity.this, "Email is not verified yet, click the yellow button again", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });


        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {

                    String enteredPassword = RegisterCache.getregisterPassword();


                    AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), enteredPassword);


                    currentUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> reauthTask) {
                            if (reauthTask.isSuccessful()) {
                                // User has been successfully reauthenticated, now delete the user
                                String uid = currentUser.getUid();


                                // Delete user from Firebase Authentication
                                currentUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> deleteTask) {
                                        if (deleteTask.isSuccessful()) {
                                            removeUserfromrealtime(uid);

                                            Intent intent = new Intent(emailVerificationActivity.this, RegisterFourthpageActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);
                                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                                            finish();
                                        } else {

                                            Toast.makeText(emailVerificationActivity.this, "Failed to delete user", Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });
                            } else {
                                // Reauthentication failed, handle accordingly
                                Toast.makeText(emailVerificationActivity.this, "Reauthentication failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else{

                }

            }
        });





    }


    private void sendEmailVerification() {
        View alertloadingDialog = LayoutInflater.from(emailVerificationActivity.this).inflate(R.layout.custom_progress_bar,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(emailVerificationActivity.this);
        builder.setView(alertloadingDialog);
        final AlertDialog pd = builder.create();
        pd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        pd.setCanceledOnTouchOutside(false);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                pd.dismiss();
                                Toast.makeText(emailVerificationActivity.this, "Verification email sent. Please check your email.", Toast.LENGTH_SHORT).show();
                               // startActivity(new Intent(emailVerificationActivity.this,LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));

                            } else {
                                pd.dismiss();
                                Toast.makeText(emailVerificationActivity.this, "Please wait a while before sending a new email.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }



    public void removeUserfromrealtime(String userid){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("Users").child(userid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {


                    Intent intent = new Intent(emailVerificationActivity.this, RegisterFourthpageActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    finish();
                }
            }
        });
    }


}