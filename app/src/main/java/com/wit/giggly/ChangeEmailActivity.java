package com.wit.giggly;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;

public class ChangeEmailActivity extends AppCompatActivity {

    private EditText newEmailEditText;
    private EditText confirmEmail;
    private TextView confirmEmailChangeButton;
    private EditText password;
    public Button cancelBTN;
    public Button confirmBtn;
    private FirebaseAuth mAuth;
    private FirebaseUser fUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_email);

        mAuth = FirebaseAuth.getInstance();
password = findViewById(R.id.password);
        confirmEmail = findViewById(R.id.confirmEmailEditText);
        newEmailEditText = findViewById(R.id.newEmailEditText);
        confirmEmailChangeButton = findViewById(R.id.confirmEmailChangeBtn);
        fUser = FirebaseAuth.getInstance().getCurrentUser();

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
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.lightgrey));
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
                overridePendingTransition(R.anim.slide_out_down, R.anim.slide_in_up);
                finish();
            }
        });

        confirmEmailChangeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String newEmail = newEmailEditText.getText().toString().trim();
                String currentPassword = password.getText().toString().trim();
String confirmEmailEditText = confirmEmail.getText().toString().trim();


                // Validate the new email
                if (!isValidEmail(newEmail)) {
                    newEmailEditText.setError("Invalid email address");
                    return;
                }

                // Validate the password
                if (currentPassword.isEmpty()) {
                    password.setError("Password is required");
                    return;
                }
                if (!newEmail.equals(confirmEmailEditText)) {
                    confirmEmail.setError("Please retype email ");
                    return;
                }
                if(newEmail.equals(fUser.getEmail())){
                    newEmailEditText.setError("This is the current email");
                    return;
                }
                showEmailConfirmationDialog();


            }
        });
    }

    private void showEmailConfirmationDialog() {
        //https://stackoverflow.com/questions/42273188/problems-with-custom-layout-for-alertdialog
        View alertDeleteDialog = LayoutInflater.from(ChangeEmailActivity.this).inflate(R.layout.confirm_emailchange_dialog,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(ChangeEmailActivity.this);
        builder.setView(alertDeleteDialog);

        confirmBtn = alertDeleteDialog.findViewById(R.id.acceptBtn);
        cancelBTN = alertDeleteDialog.findViewById(R.id.cancelBtn);

        final AlertDialog dialog = builder.create();

        //https://stackoverflow.com/questions/10795078/dialog-with-transparent-background-in-android
        //make window behind popup transparent
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        cancelBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String currentEmail = user.getEmail();
                String newEmail = newEmailEditText.getText().toString().trim();
                String currentPassword = password.getText().toString().trim();





                AuthCredential credential = EmailAuthProvider
                        .getCredential(user.getEmail(), currentPassword);

                user.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.d(TAG, "User re-authenticated.");
                                //Toast.makeText(ChangeEmailActivity.this, "User re authed.", Toast.LENGTH_SHORT).show();

                                //https://www.inflearn.com/questions/1039603/firebase-please-verify-the-new-email-before-changing-email-auth-operation-not
                                //had to disable email enumeration as there was a glitch in firebase code.

                                //https://stackoverflow.com/questions/49357150/how-to-update-email-from-firebase-in-android

                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                user.updateEmail(newEmail)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d(TAG, "User email address updated.");
                                                    Toast.makeText(ChangeEmailActivity.this, "User email address updated.", Toast.LENGTH_SHORT).show();
                                                    user.sendEmailVerification();


                                                    startActivity(new Intent(ChangeEmailActivity.this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                                    finish();

                                                }else{
                                                    Toast.makeText(ChangeEmailActivity.this, "email update failed check your password", Toast.LENGTH_SHORT).show();
                                                    Log.e(TAG, "email update failed "+ task.getException());
                                                }
                                            }
                                        });

                            }
                        });
            }
        });
    }

    private boolean isValidEmail(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }

    public void onBackPressed()
    {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left,
                R.anim.slide_out_right);
    }


}
