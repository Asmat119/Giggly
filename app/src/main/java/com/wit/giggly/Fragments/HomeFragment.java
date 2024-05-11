package com.wit.giggly.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import com.wit.giggly.Model.Post;
import com.wit.giggly.NotificationsActivity;
import com.wit.giggly.OptionsActivity;
import com.wit.giggly.R;
import com.wit.giggly.StartActivity;
import com.wit.giggly.databinding.FragmentHomeBinding;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {

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

    AppCompatActivity activity;
    Context mContext;
    private int postCount = 0;
    private boolean adsLoaded = false;

    AdmobNativeAdAdapter admobNativeAdAdapter;

    private List<String> followingList;
    private ImageView notificationsBtn;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
//        view =inflater.inflate(R.layout.fragment_home, container, false);
        view = binding.getRoot();
        notificationsBtn = view.findViewById(R.id.notificationBtn);
        swiperefresh = view.findViewById(R.id.swiperefresh);
        swiperefresh.setProgressBackgroundColorSchemeResource(R.color.yellow);
        swiperefresh.setColorSchemeColors(Color.BLACK);
        recyclerViewPosts = view.findViewById(R.id.recycler_view_posts);
        recyclerViewPosts.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
//        linearLayoutManager.setStackFromEnd(true);
//        linearLayoutManager.setReverseLayout(true);
        recyclerViewPosts.setLayoutManager(linearLayoutManager);
//        SnapHelper mSnaphelp = new PagerSnapHelper();
//        mSnaphelp.attachToRecyclerView(recyclerViewPosts);
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postList);
         admobNativeAdAdapter = AdmobNativeAdAdapter.Builder.with(
                        requireActivity().getString(R.string.native_ad_id),
                        postAdapter,
                        "medium",
                getContext()
                ).adItemIterval(5)
                .build();
         admobNativeAdAdapter.setNativeAdThemeModel();
//        recyclerViewPosts.setAdapter(postAdapter);
        recyclerViewPosts.setAdapter(admobNativeAdAdapter);

        nopostlayout = view.findViewById(R.id.noposts);
        loadingscreen = view.findViewById(R.id.loadingscreen1);
        nopostlayout.setVisibility(View.INVISIBLE);
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
                getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(requireContext(), R.color.white));
            }

        }
        followingList = new ArrayList<>();
        checkFollowingUsers();


        //Todo only load up 20 posts at a time.

        notificationsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), NotificationsActivity.class));
                Activity activity = requireActivity();
                activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {




                postList.clear();


                readPosts();

            }
        });

        return view;
    }


    private void checkFollowingUsers() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // Navigate to StartPage if the current user is null
            Intent intent = new Intent(getContext(), StartActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return;
        }

        FirebaseDatabase.getInstance().getReference().child("Follow").child(currentUser.getUid())
                .child("following").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        followingList.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            followingList.add(snapshot.getKey());
                        }
                        followingList.add(currentUser.getUid());
                        readPosts();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle onCancelled event if needed
                    }
                });
    }



    private void readPosts() {


        FirebaseDatabase.getInstance().getReference().child("Posts")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                postCount = 0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);

                    for (String id : followingList) {
                        if (post.getPublisher().equals(id)) {
                            postCount++;
                            postList.add(post);
                        }
                    }
                }

                if(postList.size() < 1){
                    nopostlayout.setVisibility(View.VISIBLE);
                    recyclerViewPosts.setVisibility(View.INVISIBLE);

                }else{
                    nopostlayout.setVisibility(View.INVISIBLE);
                    recyclerViewPosts.setVisibility(View.VISIBLE);
                }
                postList.sort((o1, o2) -> o2.getTimestamp().compareTo(o1.getTimestamp()));
                postAdapter.notifyDataSetChanged();

            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        postAdapter.notifyDataSetChanged();
        swiperefresh.setRefreshing(false);


    }


    private void showloadingscreenDialog() {

        loadingscreen.setVisibility(View.VISIBLE);

        // Delaying the dismissal of the dialog
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Dismissing the dialog after 1.5 seconds
                loadingscreen.setVisibility(View.GONE);
            }
        }, 1500); // 1500 milliseconds = 1.5 seconds
    }







}