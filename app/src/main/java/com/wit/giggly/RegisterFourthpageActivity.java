package com.wit.giggly;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class RegisterFourthpageActivity extends AppCompatActivity {


    private ImageView backButton;
    private DatabaseReference RootRef;
    private ImageView showpasswordbtn1;
    private ImageView showpasswordbtn2;
    private EditText password;

    private String firstname;
    private String secondname;
    private String username;
    private String email;
    private EditText verifypassword;
    private Button register;
    private FirebaseAuth mAuth;



    //at the same time we create the user in auth and realtimedb
    //if user is not verified after 10mins, remove both auth and realtimedb user.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_fourthpage);

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
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.yellow));
            }

        }

        backButton = findViewById(R.id.backButton);
        register = findViewById(R.id.register);
        showpasswordbtn1 = findViewById(R.id.showpasswordbtn1);
        showpasswordbtn2 = findViewById(R.id.showpasswordbtn2);
        password = findViewById(R.id.passwordRegister);
        verifypassword = findViewById(R.id.passwordConfirmRegister);
        password.setText(RegisterCache.getregisterPassword());
        verifypassword.setText(RegisterCache.getregisterConfirmpassword());
        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();
        FirebaseUser user = mAuth.getCurrentUser();

        firstname = RegisterCache.getregisterFirstname();
        secondname = RegisterCache.getregisterSecondname();
        username = RegisterCache.getregisterUsername();
        email = RegisterCache.getregisterEmail();


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txtPassword = password.getText().toString();
                String txtVerifyPassword = verifypassword.getText().toString();
                RegisterCache.setregisterPassword(txtPassword);
                RegisterCache.setregisterConfirmpassword(txtVerifyPassword);
                Intent intent = new Intent(RegisterFourthpageActivity.this, RegisterThirdPageActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String txtPassword = password.getText().toString();
                String txtVerifyPassword = verifypassword.getText().toString();
                RegisterCache.setregisterPassword(txtPassword);
                RegisterCache.setregisterConfirmpassword(txtVerifyPassword);
                 if (txtPassword.length() < 6) {
                    Toast.makeText(RegisterFourthpageActivity.this, "Password is too short", Toast.LENGTH_SHORT).show();
                } else if (!txtVerifyPassword.equals(txtPassword)) {
                    Toast.makeText(RegisterFourthpageActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                } else {
                     //create person in auth then move to email verifcation activity

                     mAuth.createUserWithEmailAndPassword(RegisterCache.getregisterEmail(), txtPassword).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                         @Override
                         public void onSuccess(AuthResult authResult) {
                            registerUser(username,firstname,secondname,email);
                         }
                     }).addOnFailureListener(new OnFailureListener() {
                         @Override
                         public void onFailure(@NonNull Exception e) {

                             Toast.makeText(RegisterFourthpageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                         }

                     });

                }
            }
        });


        showpasswordbtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (password.getTransformationMethod() instanceof PasswordTransformationMethod) {

                    password.setTransformationMethod(null);
                    showpasswordbtn1.setImageResource(R.drawable.view);
                } else {

                    password.setTransformationMethod(new PasswordTransformationMethod());
                    showpasswordbtn1.setImageResource(R.drawable.hide);
                }
            }
        });

        showpasswordbtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (verifypassword.getTransformationMethod() instanceof PasswordTransformationMethod) {

                    verifypassword.setTransformationMethod(null);
                    showpasswordbtn2.setImageResource(R.drawable.view);
                } else {

                    verifypassword.setTransformationMethod(new PasswordTransformationMethod());
                    showpasswordbtn2.setImageResource(R.drawable.hide);
                }
            }
        });

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        removeUserfromrealtime(mAuth.getCurrentUser().getUid());
        String txtPassword = password.getText().toString();
        String txtVerifyPassword = verifypassword.getText().toString();
        RegisterCache.setregisterPassword(txtPassword);
        RegisterCache.setregisterConfirmpassword(txtVerifyPassword);
        Intent intent = new Intent(RegisterFourthpageActivity.this, RegisterThirdPageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }


    private void registerUser(final String username, final String firstname,final String secondname, final String email) {
        long currentTimeMillis = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String registrationDate = sdf.format(new Date(currentTimeMillis));

        String token = SharedPreferencesHelper.getInstance(RegisterFourthpageActivity.this).getDeviceToken();

        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);


        HashMap<String, Object> map = new HashMap<>();
        map.put("firstname", firstname);
        map.put("secondname", secondname);
        map.put("email", email);
        map.put("username", username);
        map.put("id", mAuth.getCurrentUser().getUid());
        map.put("usernameLowerCase", username.toLowerCase()); //store lowercase version just for search features
        map.put("firstnameLowerCase", firstname.toLowerCase());
        map.put("secondnameLowerCase", secondname.toLowerCase());
        map.put("fullnameLowercase", firstname.toLowerCase()+ " "+ secondname.toLowerCase());
        map.put("bio", "");
        map.put("imageurl", "default");
        map.put("date", registrationDate);

        RootRef.child("Users").child(mAuth.getCurrentUser().getUid()).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    userRef.child("deviceToken").setValue(token);
                    Toast.makeText(RegisterFourthpageActivity.this, "verify email now", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterFourthpageActivity.this, emailVerificationActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();

                }
            }
        });
        //sendEmailVerification();

    }

    public void removeUserfromrealtime(String userid){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("Users").child(userid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    String txtPassword = password.getText().toString();
                    String txtVerifyPassword = verifypassword.getText().toString();
                    RegisterCache.setregisterPassword(txtPassword);
                    RegisterCache.setregisterConfirmpassword(txtVerifyPassword);
                    Intent intent = new Intent(RegisterFourthpageActivity.this, RegisterThirdPageActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    finish();
                }
            }
        });
    }

}