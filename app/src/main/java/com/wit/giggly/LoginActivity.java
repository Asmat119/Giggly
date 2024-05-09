package com.wit.giggly;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.wit.giggly.Model.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;


public class LoginActivity extends AppCompatActivity {
    private EditText email;
    private EditText password;
    private Button loginBTN;
    private Button googleBTN;
    public Button cancelBTN;
    public Button logoutBtn;
    private TextView toRegisterPage;
    private ImageView backButton;
    private EditText passwordLogin;
    private ImageView showpasswordbtn;
    GoogleSignInClient mGoogleSignInClient;

    public Button sendemailbtn;
    public EditText forgotpasswordemail;

    public ImageView closebtn;

    int RC_SIGN_IN = 40;
    private DatabaseReference RootRef;
    private TextView forgotpass;
    public Button confirmBtn;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.emailLogin);
        password = findViewById(R.id.passwordLogin);
        loginBTN = findViewById(R.id.Login);
        toRegisterPage = findViewById(R.id.register_user);
        backButton = findViewById(R.id.backButton);
        passwordLogin = findViewById(R.id.passwordLogin);
        googleBTN = findViewById(R.id.signinWithGoogle);
        RootRef = FirebaseDatabase.getInstance().getReference();
        showpasswordbtn = findViewById(R.id.showpasswordbtn);
        forgotpass = findViewById(R.id.forgotpass);

        //if user is not verified after 10mins and tries to login, remove both auth and realtimedb user.


        mAuth = FirebaseAuth.getInstance();

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
//all the toolbar stuff
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView registerLink = findViewById(R.id.register_user);



        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, StartActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_up,R.anim.slide_out_down);
                finish();
            }
        });

        forgotpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
showresetpassword();
            }
        });

        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterFirstPageActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));

            }
        });

        loginBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String emailtxt = email.getText().toString();
                final String passwordtxt = password.getText().toString();

                if (TextUtils.isEmpty(emailtxt) || TextUtils.isEmpty(passwordtxt)) {
                    Toast.makeText(LoginActivity.this, "Empty credentials", Toast.LENGTH_SHORT).show();
                } else {
                    // Try to log in the user
                    loginUser(emailtxt, passwordtxt);
                }
            }
        });

        showpasswordbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (password.getTransformationMethod() instanceof PasswordTransformationMethod) {

                    password.setTransformationMethod(null);
                    showpasswordbtn.setImageResource(R.drawable.view);
                } else {

                    password.setTransformationMethod(new PasswordTransformationMethod());
                    showpasswordbtn.setImageResource(R.drawable.hide);
                }
            }
        });



        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();

        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);


        googleBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSignIn();
            }
        });







    }

    //https://www.youtube.com/watch?v=DImgf0Flxzg google signin tutorial
    private void googleSignIn() {

        mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                Intent intent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(intent, RC_SIGN_IN);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                // Pass the Google account's ID token to the firebaseAuth method
                firebaseAuth(account.getIdToken());
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private void firebaseAuth(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {

                                RootRef.child("Users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (!dataSnapshot.exists()) {

                                            Toast.makeText(LoginActivity.this, "Signup with this email manually before using Google signin with it.", Toast.LENGTH_SHORT).show();

                                            FirebaseUser currentUser = mAuth.getCurrentUser();
                                            currentUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {

                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                                                    startActivity(intent);
                                                }
                                            });



                                        } else {

                                            showPasswordPrompt(user.getEmail());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        } else {

                            Toast.makeText(LoginActivity.this, "Error using Google sign-in", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void showPasswordPrompt(final String userEmail) {
        View passworddialog = LayoutInflater.from(LoginActivity.this).inflate(R.layout.confirmpassword_google_signin, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setView(passworddialog);

        EditText passwordInput = passworddialog.findViewById(R.id.password);
        Button acceptBtn = passworddialog.findViewById(R.id.acceptBtn);
        Button cancelBtn = passworddialog.findViewById(R.id.cancelBtn);

        AlertDialog passwordDialog = builder.create();
        passwordDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        passwordDialog.show();

        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredPassword = passwordInput.getText().toString();


                AuthCredential credential = EmailAuthProvider.getCredential(userEmail, enteredPassword);
                mAuth.signInWithCredential(credential)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {

                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                } else {

                                    Toast.makeText(LoginActivity.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                passwordDialog.cancel();
                mAuth.signOut();
            }
        });
    }


    private void loginUser(final String email, final String password) {
        View alertloadingDialog = LayoutInflater.from(LoginActivity.this).inflate(R.layout.custom_progress_bar, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setView(alertloadingDialog);
        final AlertDialog pd = builder.create();
        pd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        pd.setCanceledOnTouchOutside(false);
        pd.show();

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        if (user.isEmailVerified()) {
                            // Dismiss the progress dialog
                            pd.dismiss();

                            // Get the new token
                            FirebaseMessaging.getInstance().getToken()
                                    .addOnCompleteListener(new OnCompleteListener<String>() {
                                        @Override
                                        public void onComplete(@NonNull Task<String> task) {
                                            if (task.isSuccessful()) {
                                                // Update the token in SharedPreferences
                                                String newToken = task.getResult();
                                                SharedPreferencesHelper.getInstance(LoginActivity.this).saveDeviceToken(newToken);

                                                // Update the token in the real-time database
                                                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
                                                userRef.child("deviceToken").setValue(newToken);

                                                // Navigate to the MainActivity
                                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                // Handle failure to fetch new token
                                                Toast.makeText(LoginActivity.this, "Error fetching new token.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            // Dismiss the progress dialog
                            pd.dismiss();
                            String useriddeleted = user.getUid();

                            // Check user creation time
                            long creationTimestamp = user.getMetadata().getCreationTimestamp();
                            long currentTimestamp = System.currentTimeMillis();
                            long timeDifferenceMinutes = (currentTimestamp - creationTimestamp) / (60 * 1000);

                            if (timeDifferenceMinutes > 10) {
                                // Delete the user from authentication and real-time database
                                user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> deleteTask) {
                                        if (deleteTask.isSuccessful()) {
                                            RegisterCache.clearUsernameCache();
                                            RegisterCache.clearEmail();
                                            RegisterCache.clearFirstnameCache();
                                            RegisterCache.clearSecondnameCache();
                                            RegisterCache.clearPasswordCache();
                                            RegisterCache.clearConfirmpasswordCache();
                                            Toast.makeText(LoginActivity.this, "User deleted due to unverified email.", Toast.LENGTH_SHORT).show();
                                            removeUserfromrealtime(useriddeleted);

                                        } else {
                                            // Failed to delete user
                                            Toast.makeText(LoginActivity.this, "Failed to delete user", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                // Redirect to email verification page
                                startActivity(new Intent(LoginActivity.this, emailVerificationActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                            }
                        }
                    }
                } else {
                    // Dismiss the progress dialog
                    pd.dismiss();

                    // Handle login failure
                    Toast.makeText(LoginActivity.this, "Login failed. Check your email and password.", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Dismiss the progress dialog
                pd.dismiss();

                // Handle failure
                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void onBackPressed() {

        super.onBackPressed();
    }

    public void removeUserfromrealtime(String userid){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("Users").child(userid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {


                    Intent intent = new Intent(LoginActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    finish();
                }
            }
        });
    }


    private void showresetpassword() {
        View forgotPasswordDialogView = LayoutInflater.from(LoginActivity.this).inflate(R.layout.forgotpassworddialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setView(forgotPasswordDialogView);

        final EditText forgotPasswordEmailEditText = forgotPasswordDialogView.findViewById(R.id.emailforgotpassword);
        Button sendEmailButton = forgotPasswordDialogView.findViewById(R.id.sendemail);
        ImageView closeButton = forgotPasswordDialogView.findViewById(R.id.closebtn);

        final AlertDialog dialog = builder.create();
        Window window = dialog.getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.gravity = Gravity.BOTTOM; // Set gravity to bottom
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT; // Match parent width
        window.setAttributes(layoutParams);

        // Set background drawable to make window behind popup transparent
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //https://stackoverflow.com/questions/10795078/dialog-with-transparent-background-in-android
        //make window behind popup transparent
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        sendEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = forgotPasswordEmailEditText.getText().toString().trim();

                if (!TextUtils.isEmpty(email)) {
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(LoginActivity.this, "Password reset email sent.", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Failed to send password reset email.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    Toast.makeText(LoginActivity.this, "Please enter your email.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }




}