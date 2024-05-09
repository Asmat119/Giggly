package com.wit.giggly;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.ads.nativetemplates.rvadapter.AdmobNativeAdAdapter;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wit.giggly.Adapter.PostAdapter;
import com.wit.giggly.Fragments.ExploreFragment;
import com.wit.giggly.Model.Post;
import com.wit.giggly.databinding.FragmentHomeBinding;

import java.util.ArrayList;
import java.util.List;

public class MysavesActivity extends AppCompatActivity {

    private RecyclerView recyclerViewPosts;
    private SwipeRefreshLayout swiperefresh;
    private PostAdapter postAdapter;
    private View view;
    private FragmentHomeBinding binding;
    private AdLoader adLoader;
    private NativeAd nativeAd;
    private List<Post> postList;
    private LinearLayout nopostlayout;
    private ConstraintLayout loadingscreen;
    private FirebaseUser fUser;

    AppCompatActivity activity;
    Context mContext;
    private int postCount = 0;
    private boolean adsLoaded = false;

    AdmobNativeAdAdapter admobNativeAdAdapter;

    private List<String> followingList;
    private ImageView notificationsBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mysaves);



        recyclerViewPosts = findViewById(R.id.recucler_view_pictures);
        recyclerViewPosts.setHasFixedSize(true);
        recyclerViewPosts.setLayoutManager(new GridLayoutManager(MysavesActivity.this, 3));
//        linearLayoutManager.setStackFromEnd(true);
//        linearLayoutManager.setReverseLayout(true);

//        SnapHelper mSnaphelp = new PagerSnapHelper();
//        mSnaphelp.attachToRecyclerView(recyclerViewPosts);
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(MysavesActivity.this, postList);
        recyclerViewPosts.setAdapter(postAdapter);
        fUser = FirebaseAuth.getInstance().getCurrentUser();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            boolean isNightModeEnabled = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                    == Configuration.UI_MODE_NIGHT_YES;

            if(isNightModeEnabled){
                View decor = MysavesActivity.this.getWindow().getDecorView();
                decor.setSystemUiVisibility(0);
                MysavesActivity.this.getWindow().setStatusBarColor(ContextCompat.getColor(MysavesActivity.this, R.color.darkdarkgrey));
            }else{
                View decor = MysavesActivity.this.getWindow().getDecorView();
                decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                MysavesActivity.this.getWindow().setStatusBarColor(ContextCompat.getColor(MysavesActivity.this, R.color.white));
            }

        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TypedValue typedValueTitle = new TypedValue();
        getTheme().resolveAttribute(R.attr.paragraph2, typedValueTitle, true);
        int titleColor = typedValueTitle.data;

        toolbar.setTitleTextColor(titleColor);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Saved");
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
                // Manually recreate the ProfileFragment

                onBackPressed();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

                finish();
            }
        });



        readAllPosts();
    }

    private void readAllPosts() {
        DatabaseReference savesRef = FirebaseDatabase.getInstance().getReference().child("Saves");

        savesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        String postId = postSnapshot.getKey();
                        // For each saved post, fetch its details from the "Posts" node
                        fetchPostDetails(postId);
                    }
                } else {
                    // Handle case when there are no saved posts
                    recyclerViewPosts.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private void fetchPostDetails(String postId) {
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(postId);

        postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Post post = dataSnapshot.getValue(Post.class);
                    if (post != null) {
                        postList.add(post);
                        postCount++;

                        // Check if all posts have been fetched
                        if (postCount == postList.size()) {
                            // All posts fetched, update UI
                            updateUI();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

        finish();
    }

    private void updateUI() {
        if (!postList.isEmpty()) {
            recyclerViewPosts.setVisibility(View.VISIBLE);


            postAdapter.notifyDataSetChanged();
        } else {
            recyclerViewPosts.setVisibility(View.INVISIBLE);
        }
    }

}