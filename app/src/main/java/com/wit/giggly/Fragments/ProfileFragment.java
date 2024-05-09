package com.wit.giggly.Fragments;


import static android.content.Intent.getIntent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.wit.giggly.Adapter.PhotoAdapter;
import com.wit.giggly.EditProfileActivity;
import com.wit.giggly.FollowersActivity;
import com.wit.giggly.Model.Post;
import com.wit.giggly.Model.User;
import com.wit.giggly.OptionsActivity;
import com.wit.giggly.R;
import com.wit.giggly.StartActivity;


import org.w3c.dom.Attr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import app.futured.donut.DonutProgressView;
import app.futured.donut.DonutSection;
import de.hdodenhof.circleimageview.CircleImageView;



public class ProfileFragment extends Fragment {


    private PhotoAdapter postAdapterSaves;
    private List<Post> mySavedPosts;

    private RecyclerView recyclerView;

    private PhotoAdapter photoAdapter;
    private List<Post> myPhotoList;

    private CircleImageView imageProfile;
    private ImageView options;
    private TextView followers;
    private TextView following;
    private TextView posts;
    private TextView fullname;
    private TextView bio;
    private TextView username;

    private TextView noPostsText;
    private TextView noSavedPostsText;

    private ImageView myPictures;
private TextView gigglymetertext;

    private Button editProfile;
    private DonutProgressView gigglymetergraphChart;
    private TextView laughpercent;

    private FirebaseUser fUser;
    private ImageView verifiedbadge;

    String profileId;
    private LinearLayout gigglymeter;





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
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
            boolean isNightModeEnabled = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                    == Configuration.UI_MODE_NIGHT_YES;

            if(isNightModeEnabled){
                View decor = getActivity().getWindow().getDecorView();
                decor.setSystemUiVisibility(0);
                getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(requireContext(), R.color.darkdarkgrey));
            }else{
                View decor = getActivity().getWindow().getDecorView();
                decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(requireContext(), R.color.lightlightgrey));
            }

        }

        String data = getContext().getSharedPreferences("PROFILE", Context.MODE_PRIVATE).getString("profileId", "none");

        if (data.equals("none")) {
            profileId = fUser.getUid();
        } else {
            profileId = data;
            getContext().getSharedPreferences("PROFILE", Context.MODE_PRIVATE).edit().clear().apply();
        }

        imageProfile = view.findViewById(R.id.image_profile);
        options = view.findViewById(R.id.options);
        followers = view.findViewById(R.id.followers);
        following = view.findViewById(R.id.following);
        posts = view.findViewById(R.id.posts);
        fullname = view.findViewById(R.id.fullname);
        bio = view.findViewById(R.id.bio);
        gigglymeter = view.findViewById(R.id.gigglymeter);
        laughpercent = view.findViewById(R.id.laughpercent);
        username = view.findViewById(R.id.username);
        verifiedbadge = view.findViewById(R.id.verifiedbadge);
        myPictures = view.findViewById(R.id.my_pictures);
        editProfile = view.findViewById(R.id.edit_profile);
        recyclerView = view.findViewById(R.id.recucler_view_pictures);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        myPhotoList = new ArrayList<>();
        photoAdapter = new PhotoAdapter(getContext(), myPhotoList);
        recyclerView.setAdapter(photoAdapter);
        recyclerView.setNestedScrollingEnabled(false);

        gigglymetertext = view.findViewById(R.id.gigglyratingtext);
        //gigglymeter.setVisibility(View.GONE);
gigglymetergraphChart = view.findViewById(R.id.donut_view);





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

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String btnText = editProfile.getText().toString();

                if (btnText.equals("Edit profile")) {
                    startActivity(new Intent(getContext(), EditProfileActivity.class));
                } else {
                    if (btnText.equals("follow")) {
                        FirebaseDatabase.getInstance().getReference().child("Follow").child(fUser.getUid())
                                .child("following").child(profileId).setValue(true);

                        FirebaseDatabase.getInstance().getReference().child("Follow").child(profileId)
                                .child("followers").child(fUser.getUid()).setValue(true);
                    } else {
                        FirebaseDatabase.getInstance().getReference().child("Follow").child(fUser.getUid())
                                .child("following").child(profileId).removeValue();

                        FirebaseDatabase.getInstance().getReference().child("Follow").child(profileId)
                                .child("followers").child(fUser.getUid()).removeValue();
                    }
                }
            }
        });

        recyclerView.setVisibility(View.VISIBLE);


        myPictures.setColorFilter(ContextCompat.getColor(requireContext(), R.color.yellow), PorterDuff.Mode.SRC_IN);


        myPictures.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.setVisibility(View.VISIBLE);

                myPictures.setColorFilter(ContextCompat.getColor(requireContext(), R.color.yellow), PorterDuff.Mode.SRC_IN);

            }
        });


        followers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FollowersActivity.class);
                intent.putExtra("id", profileId);
                intent.putExtra("title", "Followers");
                startActivity(intent);
            }
        });

        following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FollowersActivity.class);
                intent.putExtra("id", profileId);
                intent.putExtra("title", "Following");
                startActivity(intent);
            }
        });

        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), OptionsActivity.class));
                Activity activity = requireActivity();
                activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        return view;
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
                if (isAdded() && getContext() != null) {
                    if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null) {
                            String imageUrl = user.getImageurl();
                            String biotext = user.getBio();
                            String usernametext = "@" + user.getUsername();
                            String fullNametext = user.getFirstname() + " " + user.getSecondname();
                            if ("default".equals(imageUrl)) {
                                imageProfile.setImageResource(R.drawable.no_profile_image);
                            } else {
                                Picasso.get().load(imageUrl).into(imageProfile);
                            }
                            if (biotext.isEmpty()) {
                                bio.setText("No bio yet...");
                            } else {
                                bio.setText(biotext);
                            }
                            if (dataSnapshot.child("verified").exists()) {
                                // User has the "verified" field in the database
                                boolean isVerified = dataSnapshot.child("verified").getValue(Boolean.class);
                                if (isVerified) {
                                    verifiedbadge.setVisibility(View.VISIBLE);
                                } else {
                                    verifiedbadge.setVisibility(View.GONE);
                                }
                            } else {
                                // "verified" field does not exist in the database
                                verifiedbadge.setVisibility(View.GONE);
                            }
                            username.setText(usernametext);
                            fullname.setText(fullNametext);

                            // Get user's posts

                        }
                    } else {
                        imageProfile.setImageResource(R.drawable.no_profile_image);
                        bio.setText("This profile is deleted, why are you here...");
                        username.setText("@Deleteduser");
                        fullname.setText("Deleted user");
                        editProfile.setVisibility(View.INVISIBLE);
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(requireContext(), StartActivity.class);
                        //https://stackoverflow.com/questions/12947916/android-remove-all-the-previous-activities-from-the-back-stack
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK); //prevents user from returning to profile when back is clicked
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        };

        userRef.addValueEventListener(userListener);
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
            gigglymeter.setVisibility(View.GONE);
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
            labelText = "You make me cry";
        } else if (percent >= 16 && percent <= 30) {
            labelText = "You suck";
        } else if (percent >= 31 && percent <= 50) {
            labelText = "Insert cricket noises here";
        } else if (percent >= 51 && percent <= 70) {
            labelText = "Not bad kid";
        } else if (percent >= 71 && percent <= 90) {
            labelText = "Comedic genius";
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


