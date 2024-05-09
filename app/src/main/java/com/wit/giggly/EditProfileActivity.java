package com.wit.giggly;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.wit.giggly.Model.User;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView close;
    private CircleImageView imageProfile;
    private TextView save;
    private TextView changePhoto;
    private EditText fullname;
    private EditText username;
    private EditText bio;

    private FirebaseUser fUser;

    private Uri mImageUri;
    private EditText firstname;
    private EditText secondname;
    private DatabaseReference usersRef;
    private StorageTask uploadTask;
    private StorageReference storageRef;
    //ProgressDialog pd;

    private View customProgressBarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);



        //pd.setContentView(R.layout.custom_progress_bar);
        View alertloadingDialog = LayoutInflater.from(EditProfileActivity.this).inflate(R.layout.custom_progress_bar,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
        builder.setView(alertloadingDialog);
        final AlertDialog pd = builder.create();

        //https://stackoverflow.com/questions/10795078/dialog-with-transparent-background-in-android
        //make window behind popup transparent
        pd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        close = findViewById(R.id.close);
        imageProfile = findViewById(R.id.image_profile);
        firstname = findViewById(R.id.firstname);
        secondname = findViewById(R.id.secondname);
        save = findViewById(R.id.save);
        changePhoto = findViewById(R.id.change_photo);

        username = findViewById(R.id.username);
        bio = findViewById(R.id.bio);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        fUser = FirebaseAuth.getInstance().getCurrentUser();
        storageRef = FirebaseStorage.getInstance().getReference().child("ProfileImages");

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

        FirebaseDatabase.getInstance().getReference().child("Users").child(fUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                username.setText(user.getUsername());
                firstname.setText(user.getFirstname());
                secondname.setText(user.getSecondname());
                bio.setText(user.getBio());
                if(user.getImageurl().equals("default")){
                    imageProfile.setImageResource(R.drawable.no_profile_image);
                }else {
                    Picasso.get().load(user.getImageurl()).into(imageProfile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        changePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity().setCropShape(CropImageView.CropShape.OVAL).start(EditProfileActivity.this);
            }
        });

        imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity().setCropShape(CropImageView.CropShape.OVAL).start(EditProfileActivity.this);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
                //startActivity(new Intent(EditProfileActivity.this, MainActivity.class));
            }
        });
    }



    private void updateProfile() {
        String fullName = firstname.getText().toString().trim().replaceAll("\\s+", " ")+" "+secondname.getText().toString().trim().replaceAll("\\s+", " ");
        String userName = username.getText().toString().trim().replaceAll("\\s+", " ");
        String userBio = bio.getText().toString().trim().replaceAll("\\s+", " ");
        String firstName = firstname.getText().toString().trim().replaceAll("\\s+", " ");
        String secondName = secondname.getText().toString().trim().replaceAll("\\s+", " ");

        if (!firstName.matches("\\b\\w+\\b") || !secondName.matches("\\b\\w+\\b")) {
            Toast.makeText(EditProfileActivity.this, "First name and second name should be one word each", Toast.LENGTH_SHORT).show();
            return;
        }
        // Check character limits
        if(TextUtils.isEmpty(fullName) || TextUtils.isEmpty(userBio) || TextUtils.isEmpty(userName)){
            Toast.makeText(EditProfileActivity.this, "Nothing is allowed be empty", Toast.LENGTH_SHORT).show();
        } else if (fullName.length() > 60) {
            firstname.setError("Full name wayyy too long");
            secondname.setError("Full name wayyy too long");
        }else if(fullName.length() >30 &&fullName.length() < 60){
            firstname.setError("Just a tad bit too long");
            secondname.setError("Just a tad bit too long");
        }else if (!firstName.matches("^[a-zA-Z]+$")) {
            firstname.setError("First name should only contain letters");
        }else if (!secondName.matches("^[a-zA-Z]+$")) {
            secondname.setError("Second name should only contain letters");
        }else if (userBio.length() > 100) {
            bio.setError("This is too long");
        } else if (userName.length() > 60) {
            username.setError("This is wayyy to long");
        }else if(userName.length() > 30 && userName.length() < 60){
            username.setError("This is just a tad too long");
        }else if(!userName.matches("\\b\\w+\\b")){
            username.setError("Don't have any spaces in the username");
        }else {
            fUser = FirebaseAuth.getInstance().getCurrentUser();

            if (fUser != null) {
                String currentUserId = fUser.getUid();

                //next few steps are for checking if the username has been edited or not, if it has but its the same, dont say "username taken" instead allow it as it is thr same name
                // Get the currently logged-in user's data from the database using their UID
                DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);


                currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            User user = dataSnapshot.getValue(User.class);
                            String currentUsername = user.getUsername();

                            // Check if the new username is the same as the current one
                            if (userName.equals(currentUsername)) {
                                // If it's the same, proceed with updating the profile without checking for uniqueness
                                updateProfileData(fullName,firstName,secondName, userName, userBio);
                            } else {
                                // If it's different, check if the new username is already taken
                                usersRef.orderByChild("username").equalTo(userName).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            // The username is already taken

                                            Toast.makeText(EditProfileActivity.this, "This username is already taken, please choose a different one", Toast.LENGTH_SHORT).show();
                                        } else {
                                            // Username is not taken, proceed with updating the profile
                                            if (fullName.length() > 20) {
                                                Toast.makeText(EditProfileActivity.this, "Full Name is too long", Toast.LENGTH_SHORT).show();
                                            } else if (userBio.length() > 100) {
                                                Toast.makeText(EditProfileActivity.this, "Bio is too long", Toast.LENGTH_SHORT).show();
                                            } else if (userName.length() > 20) {
                                                Toast.makeText(EditProfileActivity.this, "Username is too long", Toast.LENGTH_SHORT).show();
                                            } else {
                                                updateProfileData(fullName,firstName,secondName, userName, userBio);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }
    }

    private void updateProfileData(String fullName,String firstName,String secondName, String userName, String userBio) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", fullName);
        map.put("firstname", firstName);
        map.put("secondname", secondName);
        map.put("username", userName);
        map.put("usernameLowerCase", userName.toLowerCase());
        map.put("firstnameLowerCase", firstName.toLowerCase());
        map.put("secondnameLowerCase", secondName.toLowerCase());
        map.put("fullnameLowercase",  firstName.toLowerCase()+ " "+ secondName.toLowerCase());

        map.put("bio", userBio);

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fUser.getUid());
        userRef.updateChildren(map).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();

                startActivity(new Intent(EditProfileActivity.this, MainActivity.class)); // Change to the correct destination
                finish();
            } else {
                Toast.makeText(EditProfileActivity.this, "Failed to update profile. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void uploadImage() {
        View alertloadingDialog = LayoutInflater.from(EditProfileActivity.this).inflate(R.layout.custom_progress_bar,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
        builder.setView(alertloadingDialog);
        final AlertDialog pd = builder.create();
        pd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        pd.setCanceledOnTouchOutside(false);
        pd.show();

        if (mImageUri != null) {
            final StorageReference fileRef = storageRef.child(System.currentTimeMillis() + ".jpeg");

            uploadTask = fileRef.putFile(mImageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    return  fileRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String url = downloadUri.toString();

                        FirebaseDatabase.getInstance().getReference().child("Users").child(fUser.getUid()).child("imageurl").setValue(url);
                        pd.dismiss();
                    } else {
                        Toast.makeText(EditProfileActivity.this, "Upload failed!", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                }
            });
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //https://stackoverflow.com/questions/62995965/cropimage-crop-image-activity-request-code-never-getting-called-in-fragment
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            mImageUri = result.getUri();

            uploadImage();
        } else {
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }


    public void onBackPressed()
    {
        super.onBackPressed();
//        overridePendingTransition(R.anim.slide_in_left,
//                R.anim.slide_out_right);
    }


}