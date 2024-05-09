package com.wit.giggly;

import static com.wit.giggly.StartActivity.CHANNEL_ID;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.wit.giggly.Adapter.PhotoAdapter;
import com.wit.giggly.Model.MyCustomNotification;
import com.wit.giggly.Model.Post;
import com.wit.giggly.Model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import app.futured.donut.DonutProgressView;
import app.futured.donut.DonutSection;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {


    private PhotoAdapter postAdapterSaves;
    private List<Post> mySavedPosts;

    private RecyclerView recyclerView;

    private PhotoAdapter photoAdapter;
    private List<Post> myPhotoList;

    private CircleImageView imageProfile;
    private ImageView options;
    private TextView followers;
    private TextView gigglymetertext;
    private DonutProgressView gigglymetergraphChart;
    private TextView laughpercent;
    private LinearLayout gigglymeter;
    private TextView following;
    private TextView posts;
    private TextView fullname;
    private TextView bio;
    private TextView username;

    private TextView noPostsText;
    private TextView noSavedPostsText;

    private ImageView myPictures;
    private TextView back;


    private Button editProfile;

    private FirebaseUser fUser;

    String profileId;

    private ImageView verfiedbadge;



    @Override
    public void onCreate(Bundle savedInstanceState){
       super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_profile);




        fUser = FirebaseAuth.getInstance().getCurrentUser();

//        Toolbar toolbar = view.findViewById(R.id.toolbar);
//        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
//        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//
//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                getFragmentManager().popBackStack();
//            }
//        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TypedValue typedValue = new TypedValue();
            getTheme().resolveAttribute(R.attr.optionPageBackground2, typedValue, true);

            boolean isDarkTheme = ThemeSaver.getThemePreference(this);

            if(isDarkTheme){
                View decor = getWindow().getDecorView();
                decor.setSystemUiVisibility(0);
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.darkdarkgrey));
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

            }else{
                View decor = getWindow().getDecorView();
                decor.setSystemUiVisibility(0);
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.lightlightgrey));
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }

        }



        String data = ProfileActivity.this.getSharedPreferences("PROFILE", Context.MODE_PRIVATE).getString("profileId", "none");

        if (data.equals("none")) {
            profileId = fUser.getUid();
        } else {
            profileId = data;
            ProfileActivity.this.getSharedPreferences("PROFILE", Context.MODE_PRIVATE).edit().clear().apply();
        }

        imageProfile = findViewById(R.id.image_profile);
        options = findViewById(R.id.options);
        followers = findViewById(R.id.followers);
        following = findViewById(R.id.following);
        posts = findViewById(R.id.posts);
        fullname = findViewById(R.id.fullname);
        bio = findViewById(R.id.bio);
        username = findViewById(R.id.username);
        myPictures = findViewById(R.id.my_pictures);
back = findViewById(R.id.back);
        gigglymeter = findViewById(R.id.gigglymeter);
        editProfile = findViewById(R.id.edit_profile);
        laughpercent = findViewById(R.id.laughpercent);
        gigglymetertext = findViewById(R.id.gigglyratingtext);
        gigglymeter.setVisibility(View.GONE);
        gigglymetergraphChart = findViewById(R.id.donut_view);
        verfiedbadge = findViewById(R.id.verifiedbadge);

        recyclerView = findViewById(R.id.recucler_view_pictures);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(ProfileActivity.this, 3));
        myPhotoList = new ArrayList<>();
        photoAdapter = new PhotoAdapter(ProfileActivity.this, myPhotoList);
        recyclerView.setAdapter(photoAdapter);
        recyclerView.setNestedScrollingEnabled(false);






        userInfo();
        getFollowersAndFollowingCount();
        getPostCount();
        myPhotos();
        calculateLaughPercent(profileId);



        if (profileId.equals(fUser.getUid())) {
            editProfile.setText("Edit profile");
            //  ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        } else {
            checkFollowingStatus();
            options.setVisibility(View.INVISIBLE);
        }


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileActivity.super.onBackPressed();
            }
        });

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String btnText = editProfile.getText().toString();

                if (btnText.equals("Edit profile")) {
                    startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
                } else {
                    if (btnText.equals("follow")) {
                        FirebaseDatabase.getInstance().getReference().child("Follow").child(fUser.getUid())
                                .child("following").child(profileId).setValue(true);

                        FirebaseDatabase.getInstance().getReference().child("Follow").child(profileId)
                                .child("followers").child(fUser.getUid()).setValue(true);

                        createAndSendFollowNotification(profileId, "Followed you",fUser.getUid(),"Follow");

                    } else {
                        FirebaseDatabase.getInstance().getReference().child("Follow").child(fUser.getUid())
                                .child("following").child(profileId).removeValue();

                        FirebaseDatabase.getInstance().getReference().child("Follow").child(profileId)
                                .child("followers").child(fUser.getUid()).removeValue();
                        retrieveFollowNotification(profileId, fUser.getUid());
                    }
                }
            }
        });

        recyclerView.setVisibility(View.VISIBLE);


        myPictures.setColorFilter(ContextCompat.getColor(ProfileActivity.this, R.color.yellow), PorterDuff.Mode.SRC_IN);


        myPictures.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.setVisibility(View.VISIBLE);

                myPictures.setColorFilter(ContextCompat.getColor(ProfileActivity.this, R.color.yellow), PorterDuff.Mode.SRC_IN);

            }
        });


        followers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, FollowersActivity.class);
                intent.putExtra("id", profileId);
                intent.putExtra("title", "Followers");
                startActivity(intent);
            }
        });

        following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, FollowersActivity.class);
                intent.putExtra("id", profileId);
                intent.putExtra("title", "Following");
                startActivity(intent);
            }
        });

        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, OptionsActivity.class));

                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });


    }










    private void myPhotos() {

        FirebaseDatabase.getInstance().getReference().child("Posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myPhotoList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);

                    if (post.getPublisher().equals(profileId)) {
                        myPhotoList.add(post);
                    }
                }



                Collections.reverse(myPhotoList);
                photoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void checkFollowingStatus() {

        FirebaseDatabase.getInstance().getReference().child("Follow").child(fUser.getUid()).child("following").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(profileId).exists()) {
                    editProfile.setText("following");
                } else {
                    editProfile.setText("follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void getPostCount() {

        FirebaseDatabase.getInstance().getReference().child("Posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int counter = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);

                    if (post.getPublisher().equals(profileId)) counter ++;
                }

                posts.setText(String.valueOf(counter));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void getFollowersAndFollowingCount() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Follow").child(profileId);

        ref.child("followers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followers.setText("" + dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        ref.child("following").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                following.setText("" + dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }





    private void userInfo() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(profileId);

        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        String imageUrl = user.getImageurl();
                        String biotext = user.getBio();
                        String usernametext = "@" + user.getUsername();
                        String fullNametext = user.getFirstname()+" "+user.getSecondname();
                        if ("default".equals(imageUrl)) {
                            imageProfile.setImageResource(R.drawable.no_profile_image);
                        } else {
                            Picasso.get().load(imageUrl).into(imageProfile);
                        }
                        if (dataSnapshot.child("verified").exists()) {
                            // User has the "verified" field in the database
                            boolean isVerified = dataSnapshot.child("verified").getValue(Boolean.class);
                            if (isVerified) {
                                verfiedbadge.setVisibility(View.VISIBLE);
                            } else {
                                verfiedbadge.setVisibility(View.GONE);
                            }
                        } else {
                            // "verified" field does not exist in the database
                            verfiedbadge.setVisibility(View.GONE);
                        }
                        if (biotext.isEmpty()) {
                            bio.setText("No bio yet...");
                        } else {
                            bio.setText(biotext);
                        }
                        username.setText(usernametext);
                        fullname.setText(fullNametext);
                    }
                } else {
                    imageProfile.setImageResource(R.drawable.no_profile_image);
                    bio.setText("This profile is deleted, why are you here...");
                    username.setText("@Deleteduser");
                    fullname.setText("Deleted user");
                    editProfile.setVisibility(View.INVISIBLE);
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(ProfileActivity.this, StartActivity.class);
                    //https://stackoverflow.com/questions/12947916/android-remove-all-the-previous-activities-from-the-back-stack
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK); //prevents user from returning to profile when back is clicked
                    startActivity(intent);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        userRef.addValueEventListener(userListener);
    }

    private void createAndSendFollowNotification(String receiverUserId, String message, String senderUserId,String notifType) {
        String notificationId = FirebaseDatabase.getInstance().getReference("notifications").push().getKey();
        MyCustomNotification notification = new MyCustomNotification(receiverUserId, notificationId, message, senderUserId, notifType);
        DatabaseReference notificationRef = FirebaseDatabase.getInstance().getReference("notifications").child(receiverUserId).child(notificationId);
        notificationRef.setValue(notification).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    //sendFollowNotificationToUser(senderUserId, message,notification);
                } else {
                    // Handle failure
                }
            }
        });
    }

    private void sendFollowNotificationToUser(String userId, String message, MyCustomNotification notification) {
        // Check if the recipient matches the current user's ID
        if (!userId.equals(fUser.getUid())) {
            // Recipient does not match, do not display push notification
            return;
        }

        // Get the user ID to whom the notification is directed
        String goingToUserId = notification.getGoingToUserID();

        // Retrieve the user's name from the database
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(goingToUserId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Check if the user exists
                if (dataSnapshot.exists()) {
                    // Get the user's name
                    String userName = dataSnapshot.child("username").getValue(String.class);

                    // Create an intent to launch when the notification is clicked
                    Intent intent = new Intent(ProfileActivity.this, NotificationsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    PendingIntent pendingIntent = PendingIntent.getActivity(ProfileActivity.this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

                    // Create a notification
                    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(ProfileActivity.this, CHANNEL_ID)
                            .setSmallIcon(R.drawable.giggly_logo_2024_jan)
                            .setContentTitle(userName) // Set the user's name as the content title
                            .setContentText("Somebody followed you...")
                            .setAutoCancel(true)
                            .setContentIntent(pendingIntent);

                    // Get the NotificationManager service
                    NotificationManager notificationManager = (NotificationManager) ProfileActivity.this.getSystemService(Context.NOTIFICATION_SERVICE);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationChannel channel = notificationManager.getNotificationChannel(CHANNEL_ID);
                        if (channel == null) {
                            channel = new NotificationChannel("2", "Follow", NotificationManager.IMPORTANCE_DEFAULT);
                            notificationManager.createNotificationChannel(channel);
                        }
                    }

                    // Display the notification with a unique ID for each user
                    int notificationId = userId.hashCode(); // Use hashCode of userId as notificationId
                    notificationManager.notify(notificationId, notificationBuilder.build());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });
    }

    private void retrieveFollowNotification(String postPublisherId, String senderUserId) {
        DatabaseReference notificationsRef = FirebaseDatabase.getInstance().getReference("notifications").child(postPublisherId);

        notificationsRef.orderByChild("sentByUserID").equalTo(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MyCustomNotification notification = snapshot.getValue(MyCustomNotification.class);
                    if (notification != null && notification.getNotifType().equals("Follow") && notification.getSentByUserID().equals(senderUserId)) {

                        String notificationId = snapshot.getKey();
                        notificationsRef.child(notificationId).removeValue();
                       // Toast.makeText(ProfileActivity.this, notificationId, Toast.LENGTH_SHORT).show();

                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void calculateLaughPercent(String userId) {
        DatabaseReference userPostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        ValueEventListener postsListener = new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<String> postIds = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Post post = snapshot.getValue(Post.class);
                        if (post != null && post.getPublisher().equals(userId)) {
                            String postId = post.getPostId();
                            postIds.add(postId);
                        }
                    }
                    // Once we have all postIds, we can fetch likes and dislikes for each post
                    Log.d("Giggly", "Fetching likes and dislikes for user's posts...");
                    fetchLikesAndDislikes(postIds);
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
                Log.e("Giggly", "Failed to fetch user posts: " + databaseError.getMessage());
            }
        };
        userPostsRef.addListenerForSingleValueEvent(postsListener);
    }

    private void fetchLikesAndDislikes(List<String> postIds) {
        DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        DatabaseReference dislikesRef = FirebaseDatabase.getInstance().getReference().child("Boos");

        final int[] totalLikes = {0};
        final int[] totalDislikes = {0};

        for (String postId : postIds) {
            // Fetch likes for each post
            likesRef.child(postId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        totalLikes[0] += dataSnapshot.getChildrenCount(); // Counting the number of likes for this post
                        // Log likes for debugging
                        for (DataSnapshot likeSnapshot : dataSnapshot.getChildren()) {
                            Log.d("Giggly", "Like for post " + postId + " by user: " + likeSnapshot.getKey());
                        }
                    }
                    Log.d("Giggly", "Total likes for all posts: " + totalLikes[0]);
                    // After fetching likes for all posts, calculate the laugh percent
                    if (postId.equals(postIds.get(postIds.size() - 1))) {
                        calculateLaughPercent1(totalLikes[0], totalDislikes[0]);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle error
                    Log.e("Giggly", "Failed to fetch likes for post " + postId + ": " + databaseError.getMessage());
                }
            });

            // Fetch dislikes for each post
            dislikesRef.child(postId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        totalDislikes[0] += dataSnapshot.getChildrenCount(); // Counting the number of dislikes for this post
                        // Log dislikes for debugging
                        for (DataSnapshot dislikeSnapshot : dataSnapshot.getChildren()) {
                            Log.d("Giggly", "Dislike for post " + postId + " by user: " + dislikeSnapshot.getKey());
                        }
                    }
                    Log.d("Giggly", "Total dislikes for all posts: " + totalDislikes[0]);
                    // After fetching dislikes for all posts, calculate the laugh percent
                    if (postId.equals(postIds.get(postIds.size() - 1))) {
                        calculateLaughPercent1(totalLikes[0], totalDislikes[0]);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle error
                    Log.e("Giggly", "Failed to fetch dislikes for post " + postId + ": " + databaseError.getMessage());
                }
            });
        }
    }



    private void calculateLaughPercent1(int totalLikes, int totalDislikes) {
        if (totalLikes + totalDislikes == 0) {
            //laughpercent.setText(0 + "%");
            Log.d("Giggly", "Total likes and dislikes are 0");
            return;
        }else if(totalLikes + totalDislikes > 15){
            gigglymeter.setVisibility(View.VISIBLE);
        }

        double laughPercent = (double) totalLikes / (totalLikes + totalDislikes);
        int percent = (int) (laughPercent * 100);
        // Update UI with laugh percent
        laughpercent.setText(percent + "% ");

        String labelText;


        if (percent >= 0 && percent <= 15) {
            labelText = "Comedic Catastrophe";
        } else if (percent >= 16 && percent <= 30) {
            labelText = "Joke Novice";
        } else if (percent >= 31 && percent <= 50) {
            labelText = "Humor Apprentice";
        } else if (percent >= 51 && percent <= 70) {
            labelText = "Laugh Learner";
        } else if (percent >= 71 && percent <= 90) {
            labelText = "Chuckle Connoisseur";
        } else {
            labelText = "COMEDIC GOD";
        }

// Update UI with the appropriate label text
        gigglymetertext.setText(labelText);

        DonutSection section = new DonutSection("Section 1 Name", Color.parseColor("#f4b41a"), (float) percent);
        gigglymetergraphChart.setCap(100f);
        gigglymetergraphChart.setRotation(170f);
        gigglymetergraphChart.submitData(new ArrayList<>(Collections.singleton(section)));

        Log.d("Giggly", "Laugh percent calculated: " + percent + "%");
        Log.e("giggly o meter", String.valueOf(percent));
    }



}