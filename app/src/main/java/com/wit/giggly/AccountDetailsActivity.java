package com.wit.giggly;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wit.giggly.Model.User;

import java.util.ArrayList;
import java.util.List;

public class AccountDetailsActivity extends AppCompatActivity {

    private TextView usernameDetails;
    private TextView emailDetails;
    private TextView nameDetails;
    public Button logoutBtn;

    private TextView toemailchanging;
    private TextView deleteProfile;
    private TextView topasswordchanging;
    private Button changeEmail;
    public Button cancelBTN;
    public Button confirmBtn;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference mDatabase;


    FirebaseUser currentUser = mAuth.getCurrentUser();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_details);


        usernameDetails = findViewById(R.id.usernameDetails);
        emailDetails = findViewById(R.id.emailDetails);
        nameDetails = findViewById(R.id.passwordDetails);
        deleteProfile = findViewById(R.id.deleteProfile);
        toemailchanging = findViewById(R.id.toemailchanging);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        topasswordchanging = findViewById(R.id.topasswordchanging);


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
            actionBar.setTitle("Account Details");
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
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

                finish();
            }
        });

        toemailchanging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AccountDetailsActivity.this, ChangeEmailActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
                overridePendingTransition(R.anim.slide_in_up,R.anim.slide_out_down);
            }
        });

        deleteProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                showDeleteprofileDialog();
                Log.e("users post ids", collectUserPostIds(currentUser.getUid()).toString());
            }

        });

        topasswordchanging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    String userEmail = user.getEmail();
                    if (userEmail != null) {
                        FirebaseAuth.getInstance().sendPasswordResetEmail(userEmail)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {

                                            Toast.makeText(AccountDetailsActivity.this, "Password reset email sent.", Toast.LENGTH_SHORT).show();
                                        } else {

                                            Toast.makeText(AccountDetailsActivity.this, "Failed to send password reset email.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    } else {

                        Toast.makeText(AccountDetailsActivity.this, "User email not found.", Toast.LENGTH_SHORT).show();
                    }
                } else {

                    Toast.makeText(AccountDetailsActivity.this, "No user logged in.", Toast.LENGTH_SHORT).show();
                }
            }
        });




        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference userRef = mDatabase.child("Users").child(userId);

            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                        User user = dataSnapshot.getValue(User.class);
                        String username = user.getUsername();
                        String email = user.getEmail();
                        String name = user.getFirstname()+" "+user.getSecondname();

                        usernameDetails.setText(username);
                        emailDetails.setText(email);
                        nameDetails.setText(name);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle database error
                }
            });
        }
    }

    public void onBackPressed()
    {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left,
                R.anim.slide_out_right);
    }

    private void showDeleteprofileDialog() {
        //https://stackoverflow.com/questions/42273188/problems-with-custom-layout-for-alertdialog
        View alertDeleteDialog = LayoutInflater.from(AccountDetailsActivity.this).inflate(R.layout.delete_profile_dialog,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(AccountDetailsActivity.this);
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
                showCustomPasswordDialog();
            }

        });
    }

    private List<String> collectUserPostIds(String userId) {
        // Collect post IDs before deleting user's posts
        List<String> postIds = new ArrayList<>();

        DatabaseReference userPostsRef = FirebaseDatabase.getInstance().getReference("Posts");

        userPostsRef.orderByChild("publisher").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    postIds.add(snapshot.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        return postIds;
    }

    private void showCustomPasswordDialog() {

        View passworddialog = LayoutInflater.from(AccountDetailsActivity.this).inflate(R.layout.delete_profile_password_enter, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(AccountDetailsActivity.this);
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

                if(enteredPassword.isEmpty()|| enteredPassword == null){
                    Toast.makeText(AccountDetailsActivity.this, "Empty password... having cold feet?", Toast.LENGTH_SHORT).show();
                }else {
                    reauthenticateAndDelete(enteredPassword);
                    passwordDialog.dismiss();
                }
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwordDialog.dismiss();
            }
        });
    }

    private void reauthenticateAndDelete(final String enteredPassword) {

        View alertloadingDialog = LayoutInflater.from(AccountDetailsActivity.this).inflate(R.layout.custom_progress_bar,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(AccountDetailsActivity.this);
        builder.setView(alertloadingDialog);
        final AlertDialog pd = builder.create();
        pd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        pd.setCanceledOnTouchOutside(false);
        pd.show();



        if (currentUser != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), enteredPassword);

            // Reauthenticate the user
            currentUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> reauthTask) {
                    if (reauthTask.isSuccessful()) {
                        // User has been successfully reauthenticated, now do deletion
                        String uid = currentUser.getUid();
                        List<String> postIds = collectUserPostIds(uid);


                        // Delete user from Firebase Authentication
                        currentUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // User deleted from Authentication, now delete from Realtime Database
                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                                    ref.child("Users").child(uid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                Toast.makeText(AccountDetailsActivity.this, uid, Toast.LENGTH_SHORT).show();
                                                // User deleted from both Authentication and Realtime Database
                                                deleteLikesForUserPosts(postIds);
                                                deleteCommentsForUserPosts(postIds);
                                                deleteBoosForUserPosts(postIds);
                                                deletePosts(uid);


                                                deleteLikesSentByUser(uid);
                                                deleteBoosSentByUser(uid);
                                                deleteCommentsSentByUser(uid);

                                                deleteFollowersAndFollowing(uid);

                                                // deleteNotificationsAndMentions(uid);

                                                logoutUser();
                                                signOutGoogle();

                                                Intent intent = new Intent(AccountDetailsActivity.this, StartActivity.class);
                                                //https://stackoverflow.com/questions/12947916/android-remove-all-the-previous-activities-from-the-back-stack
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK); //prevents user from returning to profile when back is clicked
                                                startActivity(intent);
                                            } else {
                                                // Failed to delete user from Realtime Database
                                                pd.dismiss();
                                                Toast.makeText(AccountDetailsActivity.this, "Failed to delete user from Realtime Database", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                } else {
                                    // Failed to delete user from Authentication
                                    pd.dismiss();
                                    Toast.makeText(AccountDetailsActivity.this, "Failed to delete user from Authentication", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        // Reauthentication failed
                        pd.dismiss();
                        Toast.makeText(AccountDetailsActivity.this, "Incorrect password. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            // User is not logged in
            pd.dismiss();
            startActivity(new Intent(AccountDetailsActivity.this, LoginActivity.class));
        }
    }


    private void signOutGoogle() {
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);
        mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // Google Sign Out completed
            }
        });
    }

    //----------------deleting everything the user had on their profile----------------------------
    private void deletePosts(String userId) {
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("Posts");
        postsRef.orderByChild("publisher").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    snapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }




    private void deleteLikesForUserPosts(List<String> postIds) {
        DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference("Likes");

        for (String postId : postIds) {
            likesRef.child(postId).removeValue();
        }
    }

    private void deleteBoosForUserPosts(List<String> postIds) {
        DatabaseReference boosRef = FirebaseDatabase.getInstance().getReference("Boos");

        for (String postId : postIds) {
            boosRef.child(postId).removeValue();
        }
    }

    private void deleteCommentsForUserPosts(List<String> postIds) {
        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference("Comments");

        for (String postId : postIds) {
            commentsRef.child(postId).removeValue();
        }
    }
    //-----------------------------------------------------------------------------------------

    //----------------deleteing everything the user sent to other posts----------------------------------
    private void deleteLikesSentByUser(String userId) {
        DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference("Likes");

        likesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    // go through each post
                    String postId = postSnapshot.getKey();

                    // Check if the user has liked this post
                    if (postSnapshot.child(userId).exists()) {

                        likesRef.child(postId).child(userId).removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void deleteBoosSentByUser(String userId) {
        DatabaseReference boosRef = FirebaseDatabase.getInstance().getReference("Boos");

        boosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    // go through each post
                    String postId = postSnapshot.getKey();

                    // Check if the user has booed this post
                    if (postSnapshot.child(userId).exists()) {

                        boosRef.child(postId).child(userId).removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void deleteCommentsSentByUser(String userId) {
        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference("Comments");

        commentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    // Iterate through each user's comments
                    for (DataSnapshot commentSnapshot : userSnapshot.getChildren()) {
                        String commentPublisher = commentSnapshot.child("publisher").getValue(String.class);


                        if (userId.equals(commentPublisher)) {

                            commentsRef.child(userSnapshot.getKey()).child(commentSnapshot.getKey()).removeValue();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
//--------------------------------------------------------------------------------------------

    //------------------------------delete followers and following for both deleted user and other users---------------------
    private void deleteFollowersAndFollowing(String userId) {
        DatabaseReference followRef = FirebaseDatabase.getInstance().getReference("Follow");

        followRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {

                    if (userSnapshot.getKey().equals(userId)) {

                        userSnapshot.getRef().removeValue();
                    } else {

                        removeUserFromFollowers(userSnapshot.child("followers"), userId);

                        removeUserFromFollowing(userSnapshot.child("following"), userId);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void removeUserFromFollowers(DataSnapshot followersSnapshot, String userId) {
        for (DataSnapshot followerSnapshot : followersSnapshot.getChildren()) {
            if (followerSnapshot.getKey().equals(userId)) {
                followerSnapshot.getRef().removeValue();
                break;
            }
        }
    }

    private void removeUserFromFollowing(DataSnapshot followingSnapshot, String userId) {
        for (DataSnapshot followingUserSnapshot : followingSnapshot.getChildren()) {
            if (followingUserSnapshot.getKey().equals(userId)) {
                followingUserSnapshot.getRef().removeValue();
                break;
            }
        }
    }

    private void showLogoutConfirmationDialog() {
        //https://stackoverflow.com/questions/42273188/problems-with-custom-layout-for-alertdialog
        View alertDeleteDialog = LayoutInflater.from(AccountDetailsActivity.this).inflate(R.layout.good_logout_dialog,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(AccountDetailsActivity.this);
        builder.setView(alertDeleteDialog);


        logoutBtn = alertDeleteDialog.findViewById(R.id.acceptBtn);
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

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutUser();
            }
        });
    }

    private void logoutUser() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
            userRef.child("deviceToken").setValue(""); // Clear the device token
            SharedPreferencesHelper.getInstance(this).clearDeviceToken();

            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(this, StartActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {

        }
    }






}