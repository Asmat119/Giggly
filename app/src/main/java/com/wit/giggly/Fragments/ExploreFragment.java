package com.wit.giggly.Fragments;

import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wit.giggly.Adapter.PostAdapter;
import com.wit.giggly.Adapter.UserAdapter;
import com.wit.giggly.Model.Post;
import com.wit.giggly.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class ExploreFragment extends Fragment implements PostAdapter.OnClickPostImage {

    private RecyclerView recyclerViewPosts;
    private TextView exploretext;
    private NestedScrollView mAllPostsContainer;
    private List<Post> postList;
    private PostAdapter postAdapter;
    private SwipeRefreshLayout swiperefresh;
    private  LinearLayout categoryButtonsLayout;
    private AppCompatSpinner categorySpinner;
    private ArrayAdapter<String> categoryAdapter;
    private NestedScrollView mRecyclerViewContainer;
    private String category = "All";
    private int postCount = 0;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_explore, container, false);

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
                getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(requireContext(), R.color.yellow));
            }

        }

        recyclerViewPosts = view.findViewById(R.id.recycler_view_posts);
        recyclerViewPosts.setHasFixedSize(false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerViewPosts.setLayoutManager(new StaggeredGridLayoutManager(3,StaggeredGridLayoutManager.VERTICAL));
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postList,this);
        recyclerViewPosts.setAdapter(postAdapter);

        swiperefresh = view.findViewById(R.id.swiperefresh);
        swiperefresh.setProgressBackgroundColorSchemeResource(R.color.yellow);
        swiperefresh.setColorSchemeColors(Color.BLACK);

        exploretext = view.findViewById(R.id.exploretext);

        exploretext.setText("Explore "+category.toLowerCase());

        //Todo only load up 20 posts at a time.
        readAllPosts();

        categoryButtonsLayout = view.findViewById(R.id.categoryButtonsLayout);
        List<String> categoryOptions = getCategoryOptions();

        for (String category : categoryOptions) {
            Button categoryButton = new Button(requireContext());
            categoryButton.setText(category);

            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);

            drawable.setColor(getResources().getColor(R.color.yellow));
           drawable.setCornerRadius(getResources().getDimension(R.dimen.button_radius));
            categoryButton.setBackground(drawable);
            categoryButton.setPadding(30,0,30,0);
            categoryButton.setTextSize(12);
categoryButton.setTextColor(getResources().getColor(R.color.black));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );


            params.setMargins(0, 0, 15, 10);

            categoryButton.setLayoutParams(params);

            categoryButton.setOnClickListener(v -> {

                handleCategoryButtonClick(category);
            });

            categoryButtonsLayout.addView(categoryButton);
        }

        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                postList.clear();

                if ("All".equals(category)) {
                    readAllPosts();
                } else {
                    readPosts(category);
                }
            }
        });




        mRecyclerViewContainer = view.findViewById(R.id.recyclerViewContainer);
        mAllPostsContainer = view.findViewById(R.id.allPostsContainer);







        return view;
    }




    private void readPosts(String category) {

        FirebaseDatabase.getInstance().getReference().child("Posts")
                .orderByChild("category")
                .equalTo(category)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        postList.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Post post = snapshot.getValue(Post.class);

                            postList.add(post);
                        }
                        // reversing list so newest is first
                        Collections.reverse(postList);

                        postAdapter.notifyDataSetChanged();
                        swiperefresh.setRefreshing(false);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void readAllPosts() {
        FirebaseDatabase.getInstance().getReference().child("Posts")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        postList.clear();
                        postCount = 0;

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Post post = snapshot.getValue(Post.class);



                                    // Fetch likes, dislikes, and comments count for each post
                                    getLikesCountForPost(post.getPostId(), new LikesCountCallback() {
                                        @Override
                                        public void onLikesCountReceived(int likesCount) {
                                            // Update the likes count for the post
                                            updateLikesCount(post.getPostId(), likesCount);
                                            calculatePopularityScore(post);
                                        }
                                    });

                                    getDislikesCountForPost(post.getPostId(), new DislikesCountCallback() {
                                        @Override
                                        public void onDislikesCountReceived(int dislikesCount) {
                                            // Update the dislikes count for the post
                                            updateDislikesCount(post.getPostId(), dislikesCount);
                                            calculatePopularityScore(post);
                                        }
                                    });

                                    getCommentsCountForPost(post.getPostId(), new CommentsCountCallback() {
                                        @Override
                                        public void onCommentsCountReceived(int commentsCount) {
                                            // Update the comments count for the post
                                            updateCommentsCount(post.getPostId(), commentsCount);
                                            calculatePopularityScore(post);
                                        }
                                    });

                                    postCount++;
                                    postList.add(post);


                        }

                        if (postList.size() < 1) {

                            recyclerViewPosts.setVisibility(View.INVISIBLE);
                        } else {

                            recyclerViewPosts.setVisibility(View.VISIBLE);
                        }

                        // Sort by popularity score
                        postList.sort((o1, o2) -> o2.getPoints() - o1.getPoints());
                        postAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle error
                    }
                });

        postAdapter.notifyDataSetChanged();
        swiperefresh.setRefreshing(false);

    }

    private void calculatePopularityScore(Post post) {
        int likesCount = post.getLikesCount();
        int dislikesCount = post.getDislikescount();
        int commentsCount = post.getCommentcount()*2;

        int popularityScore = likesCount - dislikesCount;

        // Set popularity score to the post
        post.setPoints(popularityScore);
    }

// Define other methods for fetching likes, dislikes, and comments counts, and updating them (similar to the provided methods)

// Define callback interfaces (similar to the provided interfaces)

    //likes algorithm
    private void getLikesCountForPost(String postId, LikesCountCallback callback) {
        DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes").child(postId);
        likesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int likesCount = (int) dataSnapshot.getChildrenCount();
                // Pass the likes count to the callback interface
                callback.onLikesCountReceived(likesCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private void updateLikesCount(String postId, int likesCount) {
        for (Post post : postList) {
            if (post.getPostId().equals(postId)) {
                post.setLikesCount(likesCount);
                break; // Stop searching once the post is found
            }
        }
    }


    //dislike algorithm
    private void getDislikesCountForPost(String postId, DislikesCountCallback callback) {
        DatabaseReference dislikesRef = FirebaseDatabase.getInstance().getReference().child("Dislikes").child(postId);
        dislikesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int dislikesCount = (int) dataSnapshot.getChildrenCount();
                // Pass the dislikes count to the callback interface
                callback.onDislikesCountReceived(dislikesCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private void updateDislikesCount(String postId, int dislikesCount) {
        for (Post post : postList) {
            if (post.getPostId().equals(postId)) {
                post.setDislikescount(dislikesCount);
                break; // Stop searching once the post is found
            }
        }
    }

    //commenting algorithm
    private void getCommentsCountForPost(String postId, CommentsCountCallback callback) {
        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference().child("Comments").child(postId);
        commentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int commentsCount = (int) dataSnapshot.getChildrenCount();
                // Pass the comments count to the callback interface
                callback.onCommentsCountReceived(commentsCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private void updateCommentsCount(String postId, int commentsCount) {
        for (Post post : postList) {
            if (post.getPostId().equals(postId)) {
                post.setCommentcount(commentsCount);
                break; // Stop searching once the post is found
            }
        }
    }

    @Override
    public void onClickPost(boolean isMuted) {

    }


    interface LikesCountCallback {
        void onLikesCountReceived(int likesCount);
    }

    interface DislikesCountCallback {
        void onDislikesCountReceived(int dislikesCount);
    }
    interface CommentsCountCallback {
        void onCommentsCountReceived(int commentsCount);
    }













    private List<String> getCategoryOptions() {
        return Arrays.asList(
                "All",
                "Memes",
                "Brain rot",
                "Knock knock jokes",
                "Long story",
                "Short story",
                "Cringe",
                "Dad jokes",
                "Country jokes",
                "Animal jokes",
                "Sports jokes",
                "Idiot jokes",
                "Military jokes",
                "Car jokes",
                "Workplace jokes",
                "Puns",
                "One liners",
                "Satire",
                "Yo mama jokes"
        );

    }

    private void handleCategoryButtonClick(String category) {
        this.category = category;

        for (int i = 0; i < categoryButtonsLayout.getChildCount(); i++) {
            View child = categoryButtonsLayout.getChildAt(i);
            if (child instanceof Button) {
                Button button = (Button) child;
                boolean isSelected = button.getText().toString().equals(category);
                button.setSelected(isSelected);
            }
        }

        if ("All".equals(category)) {
            readAllPosts();
        } else {
            readPosts(category);
        }
        exploretext.setText("Explore "+category.toLowerCase());
    }





}