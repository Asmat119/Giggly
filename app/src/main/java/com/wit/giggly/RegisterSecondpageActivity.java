package com.wit.giggly;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegisterSecondpageActivity extends AppCompatActivity {

    private Button next;
    private ImageView backButton;
    private EditText username;
    private DatabaseReference usersRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_secondpage);

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
        next = findViewById(R.id.next);
        username = findViewById(R.id.usernameRegister);
        username.setText(RegisterCache.getregisterUsername());
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");





        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txtUsername = username.getText().toString().trim().replaceAll("\\s+", " ");
                RegisterCache.setregisterUsername(txtUsername);
                Intent intent = new Intent(RegisterSecondpageActivity.this, RegisterFirstPageActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txtUsername = username.getText().toString().trim().replaceAll("\\s+", " ");
                if (TextUtils.isEmpty(txtUsername)) {
                    Toast.makeText(RegisterSecondpageActivity.this, "Empty Username", Toast.LENGTH_SHORT).show();
                } else if (txtUsername.length() > 30) {
                    Toast.makeText(RegisterSecondpageActivity.this, "Username cannot be more than 30 characters", Toast.LENGTH_SHORT).show();
                }else if(!txtUsername.matches("\\b\\w+\\b")){
                    username.setError("Don't have any spaces in the username");
                }else {
               checkExistingUsername(txtUsername);
                }
            }
        });
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        String txtUsername = username.getText().toString().trim().replaceAll("\\s+", " ");
        RegisterCache.setregisterUsername(txtUsername);
        Intent intent = new Intent(RegisterSecondpageActivity.this, RegisterFirstPageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

    private void checkExistingUsername(final String usernametxt) {
        //pd.setMessage("Please Wait");
        View alertloadingDialog = LayoutInflater.from(RegisterSecondpageActivity.this).inflate(R.layout.custom_progress_bar,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterSecondpageActivity.this);
        builder.setView(alertloadingDialog);
        final AlertDialog pd = builder.create();
        pd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        pd.setCanceledOnTouchOutside(false);

       // pd.show();

        // Check if the email is already taken
        usersRef.orderByChild("usernameLowerCase").equalTo(usernametxt.toLowerCase()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // The email is already taken
                   // pd.dismiss();
                    Toast.makeText(RegisterSecondpageActivity.this, "This username is already taken...", Toast.LENGTH_SHORT).show();
                } else {
                    // The email is available, proceed with creating the user account
                    String txtUsername = username.getText().toString().trim().replaceAll("\\s+", " ");
                    RegisterCache.setregisterUsername(txtUsername);
                    Intent intent = new Intent(RegisterSecondpageActivity.this, RegisterThirdPageActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //pd.dismiss();
                Toast.makeText(RegisterSecondpageActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }

        });

    }
}