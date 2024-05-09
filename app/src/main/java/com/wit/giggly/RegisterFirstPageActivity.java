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
import android.widget.TextView;
import android.widget.Toast;

public class RegisterFirstPageActivity extends AppCompatActivity {

    private Button next;
    private ImageView backButton;
    private EditText firstname;
    private Button confirmbtn;
    private Button cancelBTN;
    private EditText secondname;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_first_page);

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

        firstname = findViewById(R.id.firstnameRegister);
        secondname = findViewById(R.id.secondnameRegister);
        backButton = findViewById(R.id.backButton);
        next = findViewById(R.id.next);

        firstname.setText(RegisterCache.getregisterFirstname());
        secondname.setText(RegisterCache.getregisterSecondname());


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDiscardPrompt();
            }
        });



        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txtFirstName = firstname.getText().toString().trim().replaceAll("\\s+", " ");
                String txtSecondName = secondname.getText().toString().trim().replaceAll("\\s+", " ");
                if (TextUtils.isEmpty(txtFirstName) ||TextUtils.isEmpty(txtSecondName) ) {
                    Toast.makeText(RegisterFirstPageActivity.this, "Something is empty...", Toast.LENGTH_SHORT).show();
                } else if (txtFirstName.length() > 20|| txtSecondName.length() >30) {
                    Toast.makeText(RegisterFirstPageActivity.this, "Username and Name cannot be more than 30 characters", Toast.LENGTH_SHORT).show();
                }else if (!txtFirstName.matches("^[a-zA-Z]+$")) {
                    firstname.setError("First name should only contain letters");
                }else if (!txtSecondName.matches("^[a-zA-Z]+$")) {
                    secondname.setError("First name should only contain letters");
                }else{
                    RegisterCache.setregisterFirstname(txtFirstName);
                    RegisterCache.setregisterSecondname(txtSecondName);
                    Intent intent = new Intent(RegisterFirstPageActivity.this, RegisterSecondpageActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                }
            }
        });


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        showDiscardPrompt();
    }


    private void showDiscardPrompt() {
        //https://stackoverflow.com/questions/42273188/problems-with-custom-layout-for-alertdialog
        View alertDeleteDialog = LayoutInflater.from(RegisterFirstPageActivity.this).inflate(R.layout.discard_post_dialog,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterFirstPageActivity.this);
        builder.setView(alertDeleteDialog);

        confirmbtn = alertDeleteDialog.findViewById(R.id.acceptBtn);
        cancelBTN = alertDeleteDialog.findViewById(R.id.cancelBtn);

        final AlertDialog dialog = builder.create();

        //https://stackoverflow.com/questions/10795078/dialog-with-transparent-background-in-android
        //make window behind popup transparent
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(false);

        dialog.show();

        cancelBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();

            }
        });

        confirmbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RegisterCache.clearUsernameCache();
                RegisterCache.clearFirstnameCache();
                RegisterCache.clearSecondnameCache();
                RegisterCache.clearPasswordCache();
                RegisterCache.clearConfirmpasswordCache();
                RegisterCache.clearEmail();
                Intent intent = new Intent(RegisterFirstPageActivity.this, StartActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();

            }
        });
    }
}