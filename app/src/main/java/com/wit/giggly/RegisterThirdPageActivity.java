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
import android.text.TextUtils;
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

public class RegisterThirdPageActivity extends AppCompatActivity {

    private Button next;
    private ImageView backButton;
    private EditText email;
    private DatabaseReference usersRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_third_page);

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
        email = findViewById(R.id.emailRegister);
        email.setText(RegisterCache.getregisterEmail());
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txtEmail = email.getText().toString().trim();
                RegisterCache.setregisterEmail(txtEmail);
                Intent intent = new Intent(RegisterThirdPageActivity.this, RegisterSecondpageActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txtEmail = email.getText().toString().trim();
                if (TextUtils.isEmpty(txtEmail)) {
                    Toast.makeText(RegisterThirdPageActivity.this, "Empty Credentials", Toast.LENGTH_SHORT).show();
                }else if(!isValidEmailAddress(txtEmail)){
                    Toast.makeText(RegisterThirdPageActivity.this, "Not a valid email...", Toast.LENGTH_SHORT).show();

                }else {
checkExistingEmail(txtEmail);
                }
            }
        });
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        String txtEmail = email.getText().toString().trim();
        RegisterCache.setregisterEmail(txtEmail);
        Intent intent = new Intent(RegisterThirdPageActivity.this, RegisterSecondpageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

    public boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }


    private void checkExistingEmail(final String emailtxt) {
        //pd.setMessage("Please Wait");
        View alertloadingDialog = LayoutInflater.from(RegisterThirdPageActivity.this).inflate(R.layout.custom_progress_bar,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterThirdPageActivity.this);
        builder.setView(alertloadingDialog);
        final AlertDialog pd = builder.create();
        pd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        pd.setCanceledOnTouchOutside(false);

        // pd.show();

        // Check if the email is already taken
        usersRef.orderByChild("email").equalTo(emailtxt).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // The email is already taken
                    // pd.dismiss();
                    Toast.makeText(RegisterThirdPageActivity.this, "This email is already in use...", Toast.LENGTH_SHORT).show();
                } else {
                    // The email is available, proceed with creating the user account
                    String txtemail = email.getText().toString().trim().replaceAll("\\s+", " ");
                    RegisterCache.setregisterEmail(txtemail);
                    Intent intent = new Intent(RegisterThirdPageActivity.this, RegisterFourthpageActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //pd.dismiss();
                Toast.makeText(RegisterThirdPageActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }

        });

    }
}