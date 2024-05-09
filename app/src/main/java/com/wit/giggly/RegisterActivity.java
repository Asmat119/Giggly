package com.wit.giggly;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {

    private DatabaseReference usersRef;
    private EditText username;
    private EditText firstname;
    private EditText secondname;
    private EditText email;
    private EditText password;
    private EditText verifypassword;
    private Button register;
    private TextView loginUser;

    private DatabaseReference RootRef;
    private FirebaseAuth mAuth;
    private ImageView backButton;
    private ImageView showpasswordbtn1;
    private ImageView showpasswordbtn2;

    ProgressDialog pd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

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

        username = findViewById(R.id.usernameRegister);
        firstname = findViewById(R.id.firstnameRegister);
        secondname = findViewById(R.id.secondnameRegister);
        email = findViewById(R.id.emailRegister);
        password = findViewById(R.id.passwordRegister);
        register = findViewById(R.id.register);
        loginUser = findViewById(R.id.login_user);
        verifypassword = findViewById(R.id.passwordConfirmRegister);
        RootRef = FirebaseDatabase.getInstance().getReference();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();
        pd = new ProgressDialog(this);
        backButton = findViewById(R.id.backButton);
        showpasswordbtn1 = findViewById(R.id.showpasswordbtn1);
        showpasswordbtn2 = findViewById(R.id.showpasswordbtn2);


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, StartActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_up,R.anim.slide_out_down);
                finish();
            }
        });
        loginUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this,LoginActivity.class));

            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txtUsername = username.getText().toString().trim().replaceAll("\\s+", " ");
                String txtFirstName = firstname.getText().toString().trim().replaceAll("\\s+", " ");
                String txtSecondName = secondname.getText().toString().trim().replaceAll("\\s+", " ");
                String txtEmail = email.getText().toString().trim();
                String txtPassword = password.getText().toString();
                String txtVerifyPassword = verifypassword.getText().toString();

                if (TextUtils.isEmpty(txtUsername) || TextUtils.isEmpty(txtFirstName) ||TextUtils.isEmpty(txtSecondName) || TextUtils.isEmpty(txtEmail)) {
                    Toast.makeText(RegisterActivity.this, "Empty Credentials", Toast.LENGTH_SHORT).show();
                } else if (txtUsername.length() > 30 || txtFirstName.length() > 20|| txtSecondName.length() >30) {
                    Toast.makeText(RegisterActivity.this, "Username and Name cannot be more than 30 characters", Toast.LENGTH_SHORT).show();
                }else if(!txtUsername.matches("\\b\\w+\\b")){
                    username.setError("Don't have any spaces in the username");
                }else if (!txtFirstName.matches("^[a-zA-Z]+$")) {
                    firstname.setError("First name should only contain letters");
                }else if (!txtSecondName.matches("^[a-zA-Z]+$")) {
                    secondname.setError("First name should only contain letters");
                } else if (txtPassword.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "Password is too short", Toast.LENGTH_SHORT).show();
                } else if (!txtVerifyPassword.equals(txtPassword)) {
                    Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                } else {
                    checkExistingUser(txtEmail, txtUsername, txtFirstName,txtSecondName, txtPassword);
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

    private void checkExistingUser(final String email, final String username, final String firstname,final String secondname, final String password) {
        //pd.setMessage("Please Wait");
        View alertloadingDialog = LayoutInflater.from(RegisterActivity.this).inflate(R.layout.custom_progress_bar,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
        builder.setView(alertloadingDialog);
        final AlertDialog pd = builder.create();
        pd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        pd.setCanceledOnTouchOutside(false);

        pd.show();

        // Check if the email is already taken
        usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // The email is already taken
                    pd.dismiss();
                    Toast.makeText(RegisterActivity.this, "This email is already taken, please choose a different one", Toast.LENGTH_SHORT).show();
                } else {
                    // The email is available, proceed with creating the user account
                    registerUser(username, firstname,secondname, email, password);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                pd.dismiss();
                Toast.makeText(RegisterActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }

        });

    }
    private void registerUser(final String username, final String firstname,final String secondname, final String email, String password) {
        View alertloadingDialog = LayoutInflater.from(RegisterActivity.this).inflate(R.layout.custom_progress_bar,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
        builder.setView(alertloadingDialog);
        final AlertDialog pd = builder.create();
        pd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        pd.setCanceledOnTouchOutside(false);
        //pd.setMessage("Please Wait");
        pd.show();

        // Check if the username is already taken
        usersRef.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // The username is already taken
                    pd.dismiss();
                    Toast.makeText(RegisterActivity.this, "This username is already taken, please choose a different one", Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                } else {
                    // Check if the email is already taken
                    usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                // The email is already taken
                                pd.dismiss();
                                Toast.makeText(RegisterActivity.this, "This email is already registered, please use a different one", Toast.LENGTH_SHORT).show();
                                pd.dismiss();
                            } else {
                                // The email is available, proceed with creating the user account
                                mAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {

                                        long currentTimeMillis = System.currentTimeMillis();
                                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                        String registrationDate = sdf.format(new Date(currentTimeMillis));

                                        String token = SharedPreferencesHelper.getInstance(RegisterActivity.this).getDeviceToken();

                                        String userId = mAuth.getCurrentUser().getUid();
                                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
                                        userRef.child("deviceToken").setValue(token);

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
                                                    pd.dismiss();
                                                    Toast.makeText(RegisterActivity.this, "Almost there", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(RegisterActivity.this, emailVerificationActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                    startActivity(intent);
                                                    finish();

                                                }
                                            }
                                        });
                                        //sendEmailVerification();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pd.dismiss();
                                        Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                                    }

                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            pd.dismiss();
                            Toast.makeText(RegisterActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                pd.dismiss();
                Toast.makeText(RegisterActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


    public void onBackPressed() {

        super.onBackPressed();
    }
}



