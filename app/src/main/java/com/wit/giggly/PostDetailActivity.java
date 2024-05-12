package com.wit.giggly;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.google.android.ads.nativetemplates.rvadapter.AdmobNativeAdAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hendraanggrian.appcompat.widget.SocialTextView;
import com.wit.giggly.Adapter.PostAdapter;
import com.wit.giggly.Model.MyCustomNotification;
import com.wit.giggly.Model.Post;
import com.wit.giggly.databinding.ActivityPostDetailBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PostDetailActivity extends AppCompatActivity {

    public static final String TAG = "hazro";
    private String postId;
    private FirebaseUser fUser;
    public ImageView imageProfile;
    public TextView categorytextview;
    public ImageView unmuteSymbolImageView;
    public ImageView muteSymbolImageView;

    public ImageView postImage;
    public Button reportBtn;
    public ImageView like;
    public ImageView comment;
    public ImageView save;
    public ImageView more;

    public TextView usernametextview;
    public TextView timestamptextview;
    public TextView noOfLikestextview;
    public TextView authortextview;
    public TextView postSquareProfiletextview;
    public TextView atTexttextview;
    public TextView posttitletextview;
    private List<Post> postList;
    private MediaPlayer mediaPlayerOld;
    private boolean isPlaying = false;

    //public RelativeLayout newItemCard;
    public TextView noOfCommentstextview;
    public ImageView profileImage;
    public ImageView boo;
    public Button followbtn;
    public String retrievedPostaudioLink;
    public TextView noOfBoostextview;
    public TextView audiourltextview;
    SocialTextView descriptiontextview;
    public Button cancelBTN;
    public ConstraintLayout postclickRoot;
    public Button deletePost;
    public Button deletepostBtn;
    private  int lastPosition;
    public Button whoisbtn;
    public ImageView closebtn;
    public boolean isMuted = false;

    private static final String FCM_API = "https://fcm.googleapis.com/fcm/send";
    private static final String SERVER_KEY = "AAAAz-E70BY:APA91bG4JmyzoErCHnztd7d9Gg6pqQ2lAfn0xaX2pxX_UnbRGFjNE0LOwJyKhbuvzCu6crwXYv1zjBdL_XhdARiPdxbE5XC2sLOZ1qMIsDy69WOW1m0UDqt3h3QfC91q4IiYck2DBZpP";

    private PostAdapter postAdapter;
    public TextView timestampdetailtextview;
    public  String postAuthorId;
    private int postCount = 0;
    AdmobNativeAdAdapter admobNativeAdAdapter;

    ActivityPostDetailBinding binding;
    private RecyclerView recyclerViewPosts;

    MediaPlayer mediaPlayer;
    @Override
    public void onBackPressed() {
        postAdapter.stopAudio();
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostDetailBinding.inflate(getLayoutInflater());
//        setContentView(R.layout.activity_post_detail);
        setContentView(binding.getRoot());

        mediaPlayer = new MediaPlayer();

//        mediaPlayer = new MediaPlayer();
//        setContentView(binding.getRoot());
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        postId = getSharedPreferences("PREFS", Context.MODE_PRIVATE).getString("postid", "none");
        postAuthorId = getIntent().getStringExtra("postAuthorId");
//        recyclerViewPosts = findViewById(R.id.postdetailrecycler);
        recyclerViewPosts = findViewById(R.id.postdetailrecycler);
//        recyclerViewPosts = binding.;

        recyclerViewPosts.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(PostDetailActivity.this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
//        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerViewPosts.setLayoutManager(linearLayoutManager);
        SnapHelper mSnaphelp = new PagerSnapHelper();
        postList = new ArrayList<>();
        recyclerViewPosts.addOnScrollListener(new SnapOnScrollListener(mSnaphelp, SnapOnScrollListener.Behavior.NOTIFY_ON_SCROLL, new OnSnapPositionChangeListener() {
            @Override
            public void onSnapPositionChange(int position) {
                Post post = postList.get(position);
                Log.d(TAG, "onSnapPositionChange: " + position);
                Log.d(TAG, "onSnapPositionChange: " + post.getTitle());
                Log.d(TAG, "onSnapPositionChange: " + post.getAudio());

                try {
                    // Check if MediaPlayer is already playing and stop it
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                    }

                    // Initialize MediaPlayer
                    mediaPlayer = new MediaPlayer();

                    // Set data source and prepare asynchronously
                    mediaPlayer.setDataSource(post.getAudio());
                    mediaPlayer.prepare();
                    mediaPlayer.setLooping(true);
                    mediaPlayer.start();

                    // Set listener for when preparation is complete
//                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                        @Override
//                        public void onPrepared(MediaPlayer mp) {
//                            Log.d(TAG, "onPrepared: called");
//                            // Start playing the audio
//                            mp.setLooping(true);
//                            mp.start();
//                        }
//                    });

                    // Set error listener


                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("hazro", "onSnapPositionChange: ");
//                    throw new RuntimeException(e);
                }


            }
        }));
        mediaPlayer.setOnErrorListener((mp, what, extra) -> {
            // Handle error
            Log.e(TAG, "MediaPlayer error: " + what + ", " + extra);
            return false; // Return true if the error is handled
        });
        mSnaphelp.attachToRecyclerView(recyclerViewPosts);
        postAdapter = new PostAdapter(PostDetailActivity.this, postList);
        admobNativeAdAdapter = AdmobNativeAdAdapter.Builder.with(
                        PostDetailActivity.this.getString(R.string.native_ad_id),
                        postAdapter,
                        "small",
                        PostDetailActivity.this
                ).adItemIterval(5)
                .build();
        admobNativeAdAdapter.setNativeAdThemeModel();
        recyclerViewPosts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                lastPosition = linearLayoutManager.findLastVisibleItemPosition();
//                postAdapter.startAudio(lastPosition);
//                Toast.makeText(PostDetailActivity.this, "pos"+lastPosition, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
//        recyclerViewPosts.setAdapter(postAdapter);
        recyclerViewPosts.setAdapter(admobNativeAdAdapter);
//        postAdapter.startAudio(0);
        readPosts(postId);


//        followbtn = findViewById(R.id.followbtn);
//        imageProfile = findViewById(R.id.image_profile);
//        profileImage = findViewById(R.id.image_profile);
//        postImage = findViewById(R.id.postimage);
//
//        categorytextview = findViewById(R.id.category);
//
//        boo = findViewById(R.id.boo);
//        postclickRoot = findViewById(R.id.postclickroot);
//        retrievedPostaudioLink = null;
//
//        like = findViewById(R.id.like);
//        comment = findViewById(R.id.comment);
//
//        more = findViewById(R.id.more);
//
//        usernametextview = findViewById(R.id.username);
//        noOfLikestextview = findViewById(R.id.no_of_likes);
//        noOfBoostextview = findViewById(R.id.no_of_boos);
//        authortextview = findViewById(R.id.author);
//        noOfCommentstextview = findViewById(R.id.no_of_comments);
//        descriptiontextview = findViewById(R.id.description);
//        //newItemCard = itemView.findViewById(R.id.new_post_item_card);
//        unmuteSymbolImageView = findViewById(R.id.unmuteSymbolImageView);
//        muteSymbolImageView = findViewById(R.id.muteSymbolImageView);
//        timestampdetailtextview = findViewById(R.id.timestampdetail);





        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(0);
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.darkdarkgrey));
        }




         Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {

            actionBar.setDisplayHomeAsUpEnabled(true);

            Drawable drawable = toolbar.getNavigationIcon();
            if (drawable != null) {
                drawable.mutate();
                drawable.setTint(getResources().getColor(android.R.color.white));
                toolbar.setNavigationIcon(drawable);
            }
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                overridePendingTransition(R.anim.slide_in_left,
                        R.anim.slide_out_right);
                finish();
            }
        });

        // initializeMediaPlayer(getSelectedPostAudioUrl());
        // Log.e("audio link in oncreate", audioLink);





//        FirebaseDatabase.getInstance().getReference().child("Users").child(postAuthorId).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                User user = dataSnapshot.getValue(User.class);
//
//                if (user != null ) {
//                    if (user.getImageurl().equals("default")) {
//                        profileImage.setImageResource(R.drawable.no_profile_image);
//                    } else {
//                        Picasso.get().load(user.getImageurl()).placeholder(R.drawable.no_profile_image).into(profileImage);
//                    }
//                    String username = user.getUsername().length() > 20 ? user.getUsername().substring(0, 20) : user.getUsername();
//                    String author = user.getFirstname() +" "+ user.getSecondname();
//                    usernametextview.setText("@" + username);
//                    authortextview.setText(author);
//                    isFollowed(user.getId() , followbtn);
//
//
//
//                }else{
//                    profileImage.setImageResource(R.drawable.no_profile_image);
//                    usernametextview.setText("@DeletedAccount");
//                    authortextview.setText("Delete user");
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//
//
//
//
//        FirebaseDatabase.getInstance().getReference().child("Posts").child(postId)
//                .addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        Post retrievedPost = dataSnapshot.getValue(Post.class);
//                        if (retrievedPost != null) {
//
//                            isLiked(retrievedPost.getPostId(), like);
//                            noOfLikes(retrievedPost.getPostId(), noOfLikestextview);
//                            isBooed(retrievedPost.getPostId(),boo);
//                            noOfBoos(retrievedPost.getPostId(),noOfBoostextview);
//                            getComments(retrievedPost.getPostId(), noOfCommentstextview);
//                            startAudio(retrievedPost.getAudio());
//
//
//                            if(retrievedPost.getPublisher().equals(fUser.getUid())){
//                                followbtn.setVisibility(View.GONE);
//                            }
//
//
//
//
//                            long timeStampMillis = retrievedPost.getTimestamp();
//                            TimeAgoMessages messages = new TimeAgoMessages.Builder().withLocale(Locale.getDefault()).build();
//                            String timeAgo = TimeAgo.Companion.using(timeStampMillis, messages);
//
//                            String description = retrievedPost.getDescription().replaceAll("[^\\p{L}\\p{M}\\p{N}\\p{P}\\p{Z}\\p{Cf}\\p{Cs}\\s]", "");
//                            description = description.trim();
//                            String descriptionCapitalized = description.substring(0, 1).toUpperCase() + description.substring(1);
//
//
//                            setTitle(retrievedPost.getTitle());
//                            if (descriptionCapitalized.length() > 100) {
//                                descriptionCapitalized = descriptionCapitalized.substring(0, 100) + "...";
//                            }
//
//                            descriptiontextview.setText(descriptionCapitalized);
//                            toolbar.setTitle(retrievedPost.getTitle());
//                            categorytextview.setText(retrievedPost.getCategory());
//                            descriptiontextview.setText(retrievedPost.getDescription());
//                            timestampdetailtextview.setText(timeAgo);
//
//
//                            Picasso.get().load(retrievedPost.getImage()).into(postImage);
//                            postImage.setColorFilter(ContextCompat.getColor(PostDetailActivity.this, R.color.postdetailoverlay), android.graphics.PorterDuff.Mode.MULTIPLY);
//
//
//
//
//
//                                more.setVisibility(View.VISIBLE);
//
//                            more.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View view) {
//                                    showOptionsMenu( retrievedPost.getPostId());
//                                }
//                            });
//
//
//                            descriptiontextview.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    if (descriptiontextview.getMaxLines() == 1) {
//
//                                        descriptiontextview.setMaxLines(Integer.MAX_VALUE);
//
//                                        postImage.setColorFilter(ContextCompat.getColor(PostDetailActivity.this, R.color.mediumdarkdarkGrey), android.graphics.PorterDuff.Mode.MULTIPLY);
//                                    } else {
//
//                                        descriptiontextview.setMaxLines(1);
//
//                                        postImage.setColorFilter(ContextCompat.getColor(PostDetailActivity.this, R.color.postdetailoverlay), android.graphics.PorterDuff.Mode.MULTIPLY);
//                                    }
//                                }
//                            });
//
//                            imageProfile.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    PostDetailActivity.this.getSharedPreferences("PROFILE", Context.MODE_PRIVATE).edit().putString("profileId", retrievedPost.getPublisher()).apply();
//
//                                    startActivity(new Intent(PostDetailActivity.this, ProfileActivity.class));
//                                }
//                            });
//
//                            authortextview.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    PostDetailActivity.this.getSharedPreferences("PROFILE", Context.MODE_PRIVATE).edit().putString("profileId", retrievedPost.getPublisher()).apply();
//
//                                    startActivity(new Intent(PostDetailActivity.this, ProfileActivity.class));
//                                }
//                            });
//                            usernametextview.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    PostDetailActivity.this.getSharedPreferences("PROFILE", Context.MODE_PRIVATE).edit().putString("profileId", retrievedPost.getPublisher()).apply();
//
//                                    startActivity(new Intent(PostDetailActivity.this, ProfileActivity.class));
//                                }
//                            });
//
//                            postImage.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//
//                                    if (mediaPlayer != null) {
//
//                                        if (!isMuted) {
//                                            mediaPlayer.setVolume(0, 0);
//                                            showMuteSymbol();
//                                            isMuted = true;
//                                        } else {
//                                            mediaPlayer.setVolume(1, 1);
//                                            showUnmuteSymbol();
//                                            isMuted = false;
//                                        }
//                                    }
//                                }
//                            });
//
//
//
//
//                            //onclick actions for buttons
//                            followbtn.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    String btnText = followbtn.getText().toString();
//
//
//                                    if (btnText.equals("Follow")) {
//                                        FirebaseDatabase.getInstance().getReference().child("Follow").child(fUser.getUid())
//                                                .child("following").child(postAuthorId).setValue(true);
//
//                                        FirebaseDatabase.getInstance().getReference().child("Follow").child(postAuthorId)
//                                                .child("followers").child(fUser.getUid()).setValue(true);
//                                        createAndSendFollowNotification(postAuthorId, "Followed you",fUser.getUid(),"Follow");
//
//                                    } else {
//                                        FirebaseDatabase.getInstance().getReference().child("Follow").child(fUser.getUid())
//                                                .child("following").child(postAuthorId).removeValue();
//
//                                        FirebaseDatabase.getInstance().getReference().child("Follow").child(postAuthorId)
//                                                .child("followers").child(fUser.getUid()).removeValue();
//                                        retrieveFollowNotification(postAuthorId, fUser.getUid());
//                                    }
//
//                                }
//                            });
//
//                            like.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//
//                                    if (like.getTag().equals("like")) {
//
//                                        //animation
//                                        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(like, "scaleX", 1.0f, 1.5f);
//                                        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(like, "scaleY", 1.0f, 1.5f);
//                                        AnimatorSet scaleUp = new AnimatorSet();
//                                        scaleUp.play(scaleUpX).with(scaleUpY);
//                                        scaleUp.setDuration(300);
//                                        scaleUp.setInterpolator(new OvershootInterpolator());
//
//                                        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(like, "scaleX", 1.5f, 1.0f);
//                                        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(like, "scaleY", 1.5f, 1.0f);
//                                        AnimatorSet scaleDown = new AnimatorSet();
//                                        scaleDown.play(scaleDownX).with(scaleDownY);
//                                        scaleDown.setDuration(300);
//                                        scaleDown.setInterpolator(new OvershootInterpolator());
//
//                                        AnimatorSet animators = new AnimatorSet();
//                                        animators.playSequentially(scaleUp, scaleDown);
//                                        animators.start();
//
//                                        //firebase stuff
//                                        FirebaseDatabase.getInstance().getReference().child("Likes")
//                                                .child(retrievedPost.getPostId()).child(fUser.getUid()).setValue(true);
//
//                                        FirebaseDatabase.getInstance().getReference().child("Boos")
//                                                .child(retrievedPost.getPostId()).child(fUser.getUid()).removeValue();
//                                        createAndSendLikeNotification(retrievedPost.getPublisher(), "Liked your post",fUser.getUid(),"Like",retrievedPost.getPostId());
//                                        retrieveBooNotification(retrievedPost.getPublisher(), fUser.getUid(), retrievedPost.getPostId());
//
//                                    } else {
//
//                                        //animation
//                                        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(like, "scaleX", 1.0f, 1.5f);
//                                        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(like, "scaleY", 1.0f, 1.5f);
//                                        AnimatorSet scaleUp = new AnimatorSet();
//                                        scaleUp.play(scaleUpX).with(scaleUpY);
//                                        scaleUp.setDuration(300);
//                                        scaleUp.setInterpolator(new OvershootInterpolator());
//
//                                        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(like, "scaleX", 1.5f, 1.0f);
//                                        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(like, "scaleY", 1.5f, 1.0f);
//                                        AnimatorSet scaleDown = new AnimatorSet();
//                                        scaleDown.play(scaleDownX).with(scaleDownY);
//                                        scaleDown.setDuration(300);
//                                        scaleDown.setInterpolator(new OvershootInterpolator());
//
//                                        AnimatorSet animators = new AnimatorSet();
//                                        animators.playSequentially(scaleUp, scaleDown);
//                                        animators.start();
//
//                                        FirebaseDatabase.getInstance().getReference().child("Likes")
//                                                .child(retrievedPost.getPostId()).child(fUser.getUid()).removeValue();
//
//                                        retrieveLikeNotification(retrievedPost.getPublisher(), fUser.getUid(), retrievedPost.getPostId());
//
//
//
//                                    }
//
//                                }
//                            });
//                            boo.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//
//                                    if (boo.getTag().equals("boo")) {
//
//                                        //animation
//                                        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(boo, "scaleX", 1.0f, 1.5f);
//                                        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(boo, "scaleY", 1.0f, 1.5f);
//                                        AnimatorSet scaleUp = new AnimatorSet();
//                                        scaleUp.play(scaleUpX).with(scaleUpY);
//                                        scaleUp.setDuration(300);
//                                        scaleUp.setInterpolator(new OvershootInterpolator());
//
//                                        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(boo, "scaleX", 1.5f, 1.0f);
//                                        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(boo, "scaleY", 1.5f, 1.0f);
//                                        AnimatorSet scaleDown = new AnimatorSet();
//                                        scaleDown.play(scaleDownX).with(scaleDownY);
//                                        scaleDown.setDuration(300);
//                                        scaleDown.setInterpolator(new OvershootInterpolator());
//
//                                        AnimatorSet animators = new AnimatorSet();
//                                        animators.playSequentially(scaleUp, scaleDown);
//                                        animators.start();
//
//                                        //firebase stuff
//                                        FirebaseDatabase.getInstance().getReference().child("Boos")
//                                                .child(retrievedPost.getPostId()).child(fUser.getUid()).setValue(true);
//
//                                        FirebaseDatabase.getInstance().getReference().child("Likes")
//                                                .child(retrievedPost.getPostId()).child(fUser.getUid()).removeValue();
//
//
//                                        createAndSendBooNotification(retrievedPost.getPublisher(), "Disliked your post",fUser.getUid(),"Boo",retrievedPost.getPostId());
//                                        retrieveLikeNotification(retrievedPost.getPublisher(), fUser.getUid(), retrievedPost.getPostId());
//
//
//
////
//                                    } else {
//
//                                        //animation
//                                        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(boo, "scaleX", 1.0f, 1.5f);
//                                        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(boo, "scaleY", 1.0f, 1.5f);
//                                        AnimatorSet scaleUp = new AnimatorSet();
//                                        scaleUp.play(scaleUpX).with(scaleUpY);
//                                        scaleUp.setDuration(300);
//                                        scaleUp.setInterpolator(new OvershootInterpolator());
//
//                                        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(boo, "scaleX", 1.5f, 1.0f);
//                                        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(boo, "scaleY", 1.5f, 1.0f);
//                                        AnimatorSet scaleDown = new AnimatorSet();
//                                        scaleDown.play(scaleDownX).with(scaleDownY);
//                                        scaleDown.setDuration(300);
//                                        scaleDown.setInterpolator(new OvershootInterpolator());
//
//                                        AnimatorSet animators = new AnimatorSet();
//                                        animators.playSequentially(scaleUp, scaleDown);
//                                        animators.start();
//
//                                        FirebaseDatabase.getInstance().getReference().child("Boos")
//                                                .child(retrievedPost.getPostId()).child(fUser.getUid()).removeValue();
//
//                                        retrieveBooNotification(retrievedPost.getPublisher(), fUser.getUid(), retrievedPost.getPostId());
//
//
//                                    }
//
//                                }
//                            });
//                            noOfLikestextview.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    Intent intent = new Intent(PostDetailActivity.this, FollowersActivity.class);
//                                    intent.putExtra("id", retrievedPost.getPublisher());
//                                    intent.putExtra("title", "Likes");
//                                    intent.putExtra("postid", retrievedPost.getPostId());
//                                    PostDetailActivity.this.startActivity(intent);
//                                }
//                            });
//
//                            noOfBoostextview.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    Intent intent = new Intent(PostDetailActivity.this, FollowersActivity.class);
//                                    intent.putExtra("id", retrievedPost.getPublisher());
//                                    intent.putExtra("title", "Boos");
//                                    intent.putExtra("postid", retrievedPost.getPostId());
//                                    PostDetailActivity.this.startActivity(intent);
//                                }
//                            });
//                            comment.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//
//                                    Intent intent = new Intent(PostDetailActivity.this, commentActivity.class);
//                                    intent.putExtra("postId", retrievedPost.getPostId());
//                                    intent.putExtra("authorId", retrievedPost.getPublisher());
//                                    PostDetailActivity.this.startActivity(intent);
//                                }
//                            });
//
//
//                        } else {
//
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                    }
//                });
    }


//    private void showOptionsMenu(View view, final String postId) {
//        //manually creating a menu rather than creating a menu file and using that.
//        PopupMenu popupMenu = new PopupMenu(PostDetailActivity.this, view);
//        Menu menu = popupMenu.getMenu();
//        menu.add(Menu.NONE, 0, 0, "Delete Post");
//
//
//        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                switch (item.getItemId()) {
//                    case 0:
//
//                        deletPostPopup(postId);
//                        return true;
//
//                    default:
//                        return false;
//                }
//            }
//        });
//
//        popupMenu.show();
//    }

    private void showMuteSymbol() {
        // Show mute symbol logic (e.g., change an ImageView visibility)
        muteSymbolImageView.setVisibility(View.VISIBLE);
        unmuteSymbolImageView.setVisibility(View.GONE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Hide mute symbol after 1 second
                muteSymbolImageView.setVisibility(View.GONE);
            }
        }, 1000);
    }

    private void showUnmuteSymbol() {
        // Show unmute symbol logic (e.g., change an ImageView visibility)
        unmuteSymbolImageView.setVisibility(View.VISIBLE);
        muteSymbolImageView.setVisibility(View.GONE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Hide unmute symbol after 1 second
                unmuteSymbolImageView.setVisibility(View.GONE);
            }
        }, 1000);
    }
    private void reportPostPopup(final String postId) {
        View alertReportDialog = LayoutInflater.from(PostDetailActivity.this).inflate(R.layout.report_post_popup, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(PostDetailActivity.this);
        builder.setView(alertReportDialog);

        RadioGroup reasonRadioGroup = alertReportDialog.findViewById(R.id.reasonRadioGroup);
        MaterialButton submitReportBtn = alertReportDialog.findViewById(R.id.submitreport);

        AlertDialog dialog = builder.create();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(true);
        submitReportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedId = reasonRadioGroup.getCheckedRadioButtonId();
                if (selectedId != -1) {
                    RadioButton selectedRadioButton = alertReportDialog.findViewById(selectedId);
                    String reason = selectedRadioButton.getText().toString();
                    // Call a method to save the report to the database
                    DatabaseReference reportedPostsRef = FirebaseDatabase.getInstance().getReference().child("ReportedPosts");

                    reportedPostsRef.child(postId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // Post already reported, increment reported amount
                                int currentReportedAmount = dataSnapshot.child("reportedAmount").getValue(Integer.class);
                                reportedPostsRef.child(postId).child("reportedAmount").setValue(currentReportedAmount + 1);
                                dialog.dismiss();
                            } else {
                                // Post not reported before, set reportedAmount to 1
                                HashMap<String, Object> postMap = new HashMap<>();
                                postMap.put("postId", postId);
                                postMap.put("Reason", reason);
                                postMap.put("reportedAmount", 1); // Set reported amount to 1

                                reportedPostsRef.child(postId).setValue(postMap)
                                        .addOnSuccessListener(aVoid -> {
                                            // Report saved successfully
                                            dialog.dismiss();
                                        })
                                        .addOnFailureListener(e -> {
                                            // Failed to save report
                                            Toast.makeText(PostDetailActivity.this, "Failed to Report post", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle error
                            Toast.makeText(PostDetailActivity.this, "Error occurred: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    // Show a message to the user to select a reason
                    Toast.makeText(PostDetailActivity.this, "Please select a reason for reporting", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.show();
    }


    private void deletPostPopup(final String postId) {
        View alertDeleteDialog = LayoutInflater.from(PostDetailActivity.this).inflate(R.layout.custom_delete_dialog,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(PostDetailActivity.this);
        builder.setView(alertDeleteDialog);

        deletePost = alertDeleteDialog.findViewById(R.id.acceptBtn);
        cancelBTN = alertDeleteDialog.findViewById(R.id.cancelBtn);

        final AlertDialog dialog = builder.create();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(false);

        dialog.show();
        cancelBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
        deletePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Retrieve the post data including the image URL
                DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
                postRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            String imageUrl = dataSnapshot.child("image").getValue(String.class);
                            String audioUrl = dataSnapshot.child("audio").getValue(String.class);
                            deleteRelatedData(postId);
                            String imageName = extractImageNameFromUrl(imageUrl);
                            String audioName = extractAudioNameFromUrl(audioUrl);
                            Log.e("storage image name", imageName);
                            deleteImageFromStorage(imageName);
                            deleteAudioFromStorage(audioName);
                            dialog.dismiss();
                            startActivity(new Intent(PostDetailActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle errors or onCancelled events
                    }
                });
            }
        });

    }
    private void deleteImageFromStorage(String imageName) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("PostImages/" + imageName);

        storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

            }
        });
    }

    private void deleteAudioFromStorage(String Audioname) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("Audio/" + Audioname);

        storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

            }
        });
    }
    private String extractImageNameFromUrl(String imageUrl) {

        String prefix = "https://firebasestorage.googleapis.com/v0/b/giggly-ef5fb.appspot.com/o/PostImages%2F";
        String suffix = "?alt=media&token=";

        if (imageUrl.startsWith(prefix) && imageUrl.contains(suffix)) {
            int startIndex = prefix.length();
            int endIndex = imageUrl.indexOf(suffix);
            return imageUrl.substring(startIndex, endIndex);
        }


        return "";
    }


    private String extractAudioNameFromUrl(String audioUrl) {

        String prefix = "https://firebasestorage.googleapis.com/v0/b/giggly-ef5fb.appspot.com/o/Audio%2F";
        String suffix = "?alt=media&token=";

        if (audioUrl.startsWith(prefix) && audioUrl.contains(suffix)) {
            int startIndex = prefix.length();
            int endIndex = audioUrl.indexOf(suffix);
            return audioUrl.substring(startIndex, endIndex);
        }


        return "";
    }
    private void deleteRelatedData(final String postId) {

        //delete notifications for that post
        DatabaseReference notificationsRef = FirebaseDatabase.getInstance().getReference("notifications");

        // Query notifications with the matching post ID
        notificationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("DataChange", "onDataChange triggered");
                // go through each user's notifications
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    // go through each notification
                    for (DataSnapshot notificationSnapshot : userSnapshot.getChildren()) {

                        String notificationPostId = notificationSnapshot.child("postID").getValue(String.class);
                        if (notificationPostId != null && notificationPostId.equals(postId)) {

                            notificationSnapshot.getRef().removeValue();
                        }
                    }
                }
                // Delete likes associated with the post
                DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference("Likes").child(postId);
                likesRef.removeValue();

                // Delete comments associated with the post
                DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference("Comments").child(postId);
                commentsRef.removeValue();

                // Delete dislikes associated with the post
                DatabaseReference dislikesRef = FirebaseDatabase.getInstance().getReference("Boos").child(postId);
                dislikesRef.removeValue();

                // Remove the post itself
                DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
                postRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                        } else {

                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }



    private void isLiked(String postId, final ImageView imageView) {
        FirebaseDatabase.getInstance().getReference().child("Likes").child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(fUser.getUid()).exists()) {
                    imageView.setImageResource(R.drawable.laughing_filled);
                    imageView.setTag("liked");
                } else {
                    imageView.setImageResource(R.drawable.laughing);
                    imageView.setTag("like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void isBooed(String postId, final ImageView imageView) {
        FirebaseDatabase.getInstance().getReference().child("Boos").child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(fUser.getUid()).exists()) {
                    imageView.setImageResource(R.drawable.bored_filled);
                    imageView.setTag("booed");

                } else {
                    imageView.setImageResource(R.drawable.bored);
                    imageView.setTag("boo");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }




    private void noOfLikes (String postId, final TextView text) {
        FirebaseDatabase.getInstance().getReference().child("Likes").child(postId).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                text.setText(String.valueOf(dataSnapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void noOfBoos (String postId, final TextView text) {
        FirebaseDatabase.getInstance().getReference().child("Boos").child(postId).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                text.setText(String.valueOf(dataSnapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void isFollowed(final String id, final Button btnFollow) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Follow").child(fUser.getUid())
                .child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(id).exists())
                    btnFollow.setText("Following");
                else
                    btnFollow.setText("Follow");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

//    private void initializeMediaPlayer(String audioUrl) {
//        mediaPlayer = new MediaPlayer();
//        try {
//            mediaPlayer.setDataSource(audioUrl);
//            Log.e("audio file", audioUrl);
//            mediaPlayer.prepare();
//            mediaPlayer.setLooping(true);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    private String getSelectedPostAudioUrl() {

        Log.e("getaudioposturl method",retrievedPostaudioLink);
        return retrievedPostaudioLink;
    }

    private void startAudio(String audioUrl) {
//        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayerOld.setDataSource(audioUrl);
            Log.e("audio file", audioUrl);
            mediaPlayerOld.prepare();
            mediaPlayerOld.setLooping(true);
            mediaPlayerOld.start(); // Start playing audio immediately
            isPlaying = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void stopAudio() {
        if (isPlaying) {
            mediaPlayerOld.pause();
            isPlaying = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release MediaPlayer resources
        if (mediaPlayerOld != null) {
            mediaPlayerOld.release();
            mediaPlayerOld = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAudio();
    }

    @Override
    protected void onResume() {
        super.onResume();

        startAudio(retrievedPostaudioLink);
    }

    private void getComments (String postId, final TextView text) {
        FirebaseDatabase.getInstance().getReference().child("Comments").child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                text.setText(String.valueOf( dataSnapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void createAndSendBooNotification(String receiverUserId, String message, String senderUserId, String notifType, String postID) {
        String notificationId = FirebaseDatabase.getInstance().getReference("notifications").push().getKey();
        MyCustomNotification notification = new MyCustomNotification(receiverUserId, notificationId, message, senderUserId, notifType, postID);
        DatabaseReference notificationRef = FirebaseDatabase.getInstance().getReference("notifications").child(receiverUserId).child(notificationId);
        notificationRef.setValue(notification).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    if (!senderUserId.equals(receiverUserId)) {
                        // Send the like notification
                        sendBooNotificationToUser(receiverUserId, message, notification);
                    }
                } else {
                    // Handle failure
                }
            }
        });
    }


    private void retrieveBooNotification(String postPublisherId, String senderUserId,String postID) {
        DatabaseReference notificationsRef = FirebaseDatabase.getInstance().getReference("notifications").child(postPublisherId);

        notificationsRef.orderByChild("sentByUserID").equalTo(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MyCustomNotification notification = snapshot.getValue(MyCustomNotification.class);
                    if (notification != null && notification.getNotifType().equals("Boo") && notification.getSentByUserID().equals(senderUserId)&&notification.getPostID().equals(postID)) {

                        String notificationId = snapshot.getKey();
                        notificationsRef.child(notificationId).removeValue();
                        //Toast.makeText(PostDetailActivity.this, notificationId, Toast.LENGTH_SHORT).show();

                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void createAndSendLikeNotification(String receiverUserId, String message, String senderUserId, String notifType, String postID) {
        String notificationId = FirebaseDatabase.getInstance().getReference("notifications").push().getKey();
        MyCustomNotification notification = new MyCustomNotification(receiverUserId, notificationId, message, senderUserId, notifType, postID);
        DatabaseReference notificationRef = FirebaseDatabase.getInstance().getReference("notifications").child(receiverUserId).child(notificationId);
        notificationRef.setValue(notification).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    if (!senderUserId.equals(receiverUserId)) {
                        // Send the like notification
                        sendLikeNotificationToUser(receiverUserId, message, notification);
                    }
                } else {
                    // Handle failure
                }
            }
        });
    }


    private void retrieveLikeNotification(String postPublisherId, String senderUserId,String postID) {
        DatabaseReference notificationsRef = FirebaseDatabase.getInstance().getReference("notifications").child(postPublisherId);

        notificationsRef.orderByChild("sentByUserID").equalTo(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MyCustomNotification notification = snapshot.getValue(MyCustomNotification.class);
                    if (notification != null && notification.getNotifType().equals("Like") && notification.getSentByUserID().equals(senderUserId)&&notification.getPostID().equals(postID)) {

                        String notificationId = snapshot.getKey();
                        notificationsRef.child(notificationId).removeValue();
                        // Toast.makeText(PostDetailActivity.this, notificationId, Toast.LENGTH_SHORT).show();

                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //https://console.cloud.google.com/apis/api/googlecloudmessaging.googleapis.com/quotas?project=giggly-ef5fb
    //https://firebase.google.com/docs/reference/android/com/google/firebase/iid/FirebaseInstanceIdService
    //https://www.youtube.com/watch?v=6_t87WW6_Gc
    private void sendBooNotificationToUser(String userId, String message,MyCustomNotification notification) {

        final String[] goingtoDevicetoken = new String[1];
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // User data found, retrieve device token
                    goingtoDevicetoken[0] = dataSnapshot.child("deviceToken").getValue(String.class);
                    if (goingtoDevicetoken[0] != null && !goingtoDevicetoken[0].isEmpty()) {
//token found, send notif to device
                        OkHttpClient client = new OkHttpClient();
                        String json;
                        //Toast.makeText(PostDetailActivity.this, goingtoDevicetoken.toString(), Toast.LENGTH_SHORT).show();

                        try {
                            JSONObject jsonPayload = new JSONObject();
                            jsonPayload.put("to", goingtoDevicetoken[0]);

                            JSONObject notificationData = new JSONObject();
                            notificationData.put("title", "Uh oh...");
                            notificationData.put("body", "Someone hates your post");

                            jsonPayload.put("notification", notificationData);

                            json = jsonPayload.toString();

                            // Log the JSON payload before sending
                            Log.d("Notification", "JSON Payload: " + json);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            return; // Exit method if JSON creation fails
                        }
                        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);
                        Request request = new Request.Builder()
                                .url(FCM_API)
                                .post(requestBody)
                                .addHeader("Authorization", "Bearer " + SERVER_KEY)
                                .addHeader("Content-Type", "application/json")
                                .build();

                        client.newCall(request).enqueue(new okhttp3.Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                                // Log failure to send notification
                                Log.e("Notification", "Failed to send notification: " + e.getMessage());
                            }


                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                if (!response.isSuccessful()) {
                                    throw new IOException("Unexpected code " + response);
                                }
                                // Handle successful notification sending
                                String responseBody = response.body().string();
                                System.out.println("FCM Response: " + responseBody);
                                // Log successful notification sending
                                Log.d("Notification", "FCM Response: " + responseBody);
                                //Toast.makeText(PostDetailActivity.this, "Device tokenfound for this user sending notif now", Toast.LENGTH_SHORT).show();

                            }
                        });
                    } else {
                        // Device token not found for the user
                        Log.e("Device Token", "Device token not found for user: " + notification.getGoingToUserID());
                        //Toast.makeText(PostDetailActivity.this, "Device token not found for this user, maybe they aren't logged in?", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // User data not found
                    Log.e("User Data", "User data not found for user: " + notification.getGoingToUserID());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Error occurred while retrieving data
                Log.e("Firebase Error", "Error retrieving user data: " + databaseError.getMessage());
            }
        });


    }

    //https://stackoverflow.com/questions/24652928/flag-activity-clear-top-not-working-from-notification-section
    //https://console.cloud.google.com/apis/api/googlecloudmessaging.googleapis.com/quotas?project=giggly-ef5fb
    //https://firebase.google.com/docs/reference/android/com/google/firebase/iid/FirebaseInstanceIdService
    //https://www.youtube.com/watch?v=6_t87WW6_Gc
    private void sendLikeNotificationToUser(String recieveruserId, String message,MyCustomNotification notification) {

        final String[] goingtoDevicetoken = new String[1];
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(recieveruserId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // User data found, retrieve device token
                    goingtoDevicetoken[0] = dataSnapshot.child("deviceToken").getValue(String.class);
                    if (goingtoDevicetoken[0] != null && !goingtoDevicetoken[0].isEmpty()) {
//token found, send notif to device
                        OkHttpClient client = new OkHttpClient();
                        String json;
                        //Toast.makeText(PostDetailActivity.this, goingtoDevicetoken.toString(), Toast.LENGTH_SHORT).show();

                        try {
                            JSONObject jsonPayload = new JSONObject();
                            jsonPayload.put("to", goingtoDevicetoken[0]);

                            JSONObject notificationData = new JSONObject();
                            notificationData.put("title", "Hey You!");
                            notificationData.put("body", "Someone liked your post");

                            jsonPayload.put("notification", notificationData);

                            json = jsonPayload.toString();

                            // Log the JSON payload before sending
                            Log.d("Notification", "JSON Payload: " + json);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            return; // Exit method if JSON creation fails
                        }

                        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);

                        Request request = new Request.Builder()
                                .url(FCM_API)
                                .post(requestBody)
                                .addHeader("Authorization", "Bearer " + SERVER_KEY)
                                .addHeader("Content-Type", "application/json")
                                .build();

                        client.newCall(request).enqueue(new okhttp3.Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                                // Log failure to send notification
                                Log.e("Notification", "Failed to send notification: " + e.getMessage());
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                if (!response.isSuccessful()) {
                                    throw new IOException("Unexpected code " + response);
                                }
                                // Handle successful notification sending
                                String responseBody = response.body().string();
                                System.out.println("FCM Response: " + responseBody);
                                // Log successful notification sending
                                Log.d("Notification", "FCM Response: " + responseBody);
                               // Toast.makeText(PostDetailActivity.this, "Device tokenfound for this user sending notif now", Toast.LENGTH_SHORT).show();

                            }
                        });
                    } else {
                        // Device token not found for the user
                        Log.e("Device Token", "Device token not found for user: " + notification.getGoingToUserID());
                       // Toast.makeText(PostDetailActivity.this, "Device token not found for this user, maybe they aren't logged in?", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // User data not found
                    Log.e("User Data", "User data not found for user: " + notification.getGoingToUserID());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Error occurred while retrieving data
                Log.e("Firebase Error", "Error retrieving user data: " + databaseError.getMessage());
            }
        });

    }


    private void createAndSendFollowNotification(String receiverUserId, String message, String senderUserId,String notifType) {
        String notificationId = FirebaseDatabase.getInstance().getReference("notifications").push().getKey();
        MyCustomNotification notification = new MyCustomNotification(receiverUserId, notificationId, message, senderUserId, notifType);
        DatabaseReference notificationRef = FirebaseDatabase.getInstance().getReference("notifications").child(receiverUserId).child(notificationId);
        notificationRef.setValue(notification).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    if (!senderUserId.equals(receiverUserId)) {

                        sendFollowNotificationToUser(receiverUserId, message, notification);
                    }
                } else {
                    // Handle failure
                }
            }
        });
    }

    private void sendFollowNotificationToUser(String userId, String message,MyCustomNotification notification) {

        final String[] goingtoDevicetoken = new String[1];
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // User data found, retrieve device token
                    goingtoDevicetoken[0] = dataSnapshot.child("deviceToken").getValue(String.class);
                    if (goingtoDevicetoken[0] != null && !goingtoDevicetoken[0].isEmpty()) {
//token found, send notif to device
                        OkHttpClient client = new OkHttpClient();
                        String json;
                       // Toast.makeText(PostDetailActivity.this, goingtoDevicetoken.toString(), Toast.LENGTH_SHORT).show();

                        try {
                            JSONObject jsonPayload = new JSONObject();
                            jsonPayload.put("to", goingtoDevicetoken[0]);

                            JSONObject notificationData = new JSONObject();
                            notificationData.put("title", "WOAHHH");
                            notificationData.put("body", "It seems someone followed you because of your jokes...");

                            jsonPayload.put("notification", notificationData);

                            json = jsonPayload.toString();

                            // Log the JSON payload before sending
                            Log.d("Notification", "JSON Payload: " + json);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            return; // Exit method if JSON creation fails
                        }

                        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);

                        Request request = new Request.Builder()
                                .url(FCM_API)
                                .post(requestBody)
                                .addHeader("Authorization", "Bearer " + SERVER_KEY)
                                .addHeader("Content-Type", "application/json")
                                .build();

                        client.newCall(request).enqueue(new okhttp3.Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                                // Log failure to send notification
                                Log.e("Notification", "Failed to send notification: " + e.getMessage());
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                if (!response.isSuccessful()) {
                                    throw new IOException("Unexpected code " + response);
                                }
                                // Handle successful notification sending
                                String responseBody = response.body().string();
                                System.out.println("FCM Response: " + responseBody);
                                // Log successful notification sending
                                Log.d("Notification", "FCM Response: " + responseBody);
                               // Toast.makeText(PostDetailActivity.this, "Device tokenfound for this user sending notif now", Toast.LENGTH_SHORT).show();

                            }
                        });
                    } else {
                        // Device token not found for the user
                        Log.e("Device Token", "Device token not found for user: " + notification.getGoingToUserID());
                       // Toast.makeText(PostDetailActivity.this, "Device token not found for this user, maybe they aren't logged in?", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // User data not found
                    Log.e("User Data", "User data not found for user: " + notification.getGoingToUserID());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Error occurred while retrieving data
                Log.e("Firebase Error", "Error retrieving user data: " + databaseError.getMessage());
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
                        //Toast.makeText(PostDetailActivity.this, notificationId, Toast.LENGTH_SHORT).show();

                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void showOptionsMenu(final String postId) {
        //https://stackoverflow.com/questions/42273188/problems-with-custom-layout-for-alertdialog
        View alertDeleteDialog = LayoutInflater.from(PostDetailActivity.this).inflate(R.layout.postoptionsdialog,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(PostDetailActivity.this);
        builder.setView(alertDeleteDialog);

        deletepostBtn = alertDeleteDialog.findViewById(R.id.deleteBtn);
        whoisbtn = alertDeleteDialog.findViewById(R.id.whoisBtn);
        closebtn = alertDeleteDialog.findViewById(R.id.closebtn);
        reportBtn = alertDeleteDialog.findViewById(R.id.reportBtn);
        if (fUser != null && postAuthorId.equals(fUser.getUid())) {

            deletepostBtn.setVisibility(View.VISIBLE);



        } else {

            deletepostBtn.setVisibility(View.GONE);
        }

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

        closebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
        reportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportPostPopup(postId);
                dialog.dismiss();
            }
        });
        deletepostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deletPostPopup(postId);
                dialog.dismiss();
            }

        });
        whoisbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                PostDetailActivity.this.getSharedPreferences("PROFILE", Context.MODE_PRIVATE).edit().putString("userId", postAuthorId).apply();
//                intent.putExtra("authorId", post.getPublisher());
//                PostDetailActivity.this.startActivity(new Intent(PostDetailActivity.this, AboutAccountActivity.class));
//                dialog.dismiss();


                Intent intent = new Intent(PostDetailActivity.this, AboutAccountActivity.class);
                intent.putExtra("userId", postAuthorId);

                PostDetailActivity.this.startActivity(intent);
                dialog.dismiss();
            }
        });
    }

    private void readPosts(String postId) {
        FirebaseDatabase.getInstance().getReference().child("Posts")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        postList.clear();
                        postCount = 0;
                        Post clickedPost = null;

                        // Iterate through the dataSnapshot to find the clicked post
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Post post = snapshot.getValue(Post.class);
                            postCount++;
                            postList.add(post);

                            // Check if this is the clicked post
                            if (post.getPostId().equals(postId)) {
                                clickedPost = post;
                            }
                        }

                        if (clickedPost != null) {
                            // Remove the clicked post from the list if it already exists
                            postList.remove(clickedPost);
                            // Add the clicked post at the beginning of the list
                            postList.add(0, clickedPost);

                            // Get the category of the clicked post
                            String category = clickedPost.getCategory();

                            // Filter the postList to display posts in the same category
                            List<Post> filteredList = new ArrayList<>();
                            for (Post post : postList) {
                                if (post.getCategory().equals(category)) {
                                    filteredList.add(post);
                                }
                            }

                            // Update postList with filtered posts
                            postList.clear();
                            postList.addAll(filteredList);
                        }

                        if (postList.size() < 1) {
                            recyclerViewPosts.setVisibility(View.INVISIBLE);
                        } else {
                            recyclerViewPosts.setVisibility(View.VISIBLE);
                        }
                       // postList.sort((o1, o2) -> o2.getTimestamp().compareTo(o1.getTimestamp()));
                        postAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle error
                    }
                });
    }







}