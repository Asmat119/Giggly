package com.wit.giggly.Fragments;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;
import com.wit.giggly.Adapter.UserAdapter;
import com.wit.giggly.Model.Post;
import com.wit.giggly.Model.User;
import com.wit.giggly.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


public class SearchFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecyclerView recyclerViewSearchHistory;
    private List<User> mUsers;
    private List<User> searchHistory;
    private UserAdapter userAdapter;
    private UserAdapter searchHistoryAdapter;
    private TextView toptext;
    private TextView historytext;
    private ImageView closesearch;
    private Map<String, Integer> laughPercentages = new HashMap<>();
    private NestedScrollView mRecyclerViewContainer;

    private SocialAutoCompleteTextView search_bar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

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
        recyclerView = view.findViewById(R.id.recycler_view_users);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerViewContainer = view.findViewById(R.id.recyclerViewContainer);


        recyclerViewSearchHistory = view.findViewById(R.id.recycler_view_search_history);
        recyclerViewSearchHistory.setHasFixedSize(true);
        recyclerViewSearchHistory.setLayoutManager(new LinearLayoutManager(getContext()));


        mUsers = new ArrayList<>();
        searchHistory = new ArrayList<>();

        userAdapter = new UserAdapter(getContext() , mUsers , true);
        searchHistoryAdapter = new UserAdapter(getContext(), searchHistory, true);

        recyclerView.setAdapter(userAdapter);
        recyclerViewSearchHistory.setAdapter(searchHistoryAdapter);

        search_bar = view.findViewById(R.id.search_bar);
        historytext = view.findViewById(R.id.historytext);
        toptext = view.findViewById(R.id.toptext);
        closesearch = view.findViewById(R.id.closesearch);

closesearch.setVisibility(View.GONE);

        historytext.setVisibility(View.GONE);
        recyclerViewSearchHistory.setVisibility(View.GONE);
        toptext.setVisibility(View.VISIBLE);


        //calculateLaughPercentForAllUsers();

        closesearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                search_bar.clearFocus();
                search_bar.setText("");
readUsers();

            }
        });



        search_bar.addTextChangedListener(new TextWatcher() {


            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUser(s.toString());


                if (s.length() > 0) {
                    closesearch.setVisibility(View.VISIBLE);
                    historytext.setVisibility(View.GONE);
                    recyclerViewSearchHistory.setVisibility(View.GONE);
                    toptext.setVisibility(View.GONE);
                } else {
                    closesearch.setVisibility(View.GONE);
                    historytext.setVisibility(View.GONE);
                    recyclerViewSearchHistory.setVisibility(View.GONE);
                    toptext.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        userAdapter.setOnItemClickListener(new UserAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                User selectedUser = mUsers.get(position);
                Log.e("Selected user id", selectedUser.getId());


                    updateSearchHistory(selectedUser.getId(), search_bar.getText().toString());

            }
        });
        readUsers();
        return view;
    }


    private void searchUser(String s) {
        String searchQuery = s.toLowerCase();

        Query usernameQuery = FirebaseDatabase.getInstance().getReference().child("Users")
                .orderByChild("usernameLowerCase")
                .startAt(searchQuery)
                .endAt(searchQuery + "\uf8ff");

        Query firstNameQuery = FirebaseDatabase.getInstance().getReference().child("Users")
                .orderByChild("firstnameLowerCase")
                .startAt(searchQuery)
                .endAt(searchQuery + "\uf8ff");

        Query secondNameQuery = FirebaseDatabase.getInstance().getReference().child("Users")
                .orderByChild("secondnameLowerCase")
                .startAt(searchQuery)
                .endAt(searchQuery + "\uf8ff");

        Query fullNameQuery = FirebaseDatabase.getInstance().getReference().child("Users")
                .orderByChild("fullnameLowercase")
                .startAt(searchQuery)
                .endAt(searchQuery + "\uf8ff");

        Map<String, User> uniqueUsers = new HashMap<>();
        List<Query> queries = Arrays.asList(usernameQuery, firstNameQuery, secondNameQuery, fullNameQuery);

        AtomicInteger queryCount = new AtomicInteger(queries.size());

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    String username = user.getUsername().toLowerCase();
                    String firstname = user.getFirstname().toLowerCase();
                    String secondname = user.getSecondname().toLowerCase();
                    String fullname = firstname+secondname;
                    if ((username != null && username.contains(searchQuery)) ||
                            (firstname != null && firstname.contains(searchQuery)) ||
                            (secondname != null && secondname.contains(searchQuery)) ||
                            (fullname != null && fullname.contains(searchQuery))) {
                        uniqueUsers.put(user.getId(), user);
                    }
                }
                queryCount.decrementAndGet();

                if (queryCount.get() == 0) {
                    mUsers.clear();
                    mUsers.addAll(uniqueUsers.values());

                    userAdapter.notifyDataSetChanged();

                    if (!TextUtils.isEmpty(s) && !searchHistory.containsAll(mUsers)) {
                        searchHistory.clear();
                        searchHistory.addAll(mUsers);
                        searchHistoryAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                queryCount.decrementAndGet();
            }
        };

        for (Query query : queries) {
            query.addValueEventListener(valueEventListener);
        }
    }


    private void readUsers() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<User> users = new ArrayList<>();
                int count = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (count >= 10) {
                        break; // Exit the loop if 10 users have been added
                    }
                    User user = snapshot.getValue(User.class);
                    String userId = user.getId();
                    DatabaseReference followersRef = FirebaseDatabase.getInstance().getReference()
                            .child("Follow").child(userId).child("followers");
                    followersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            long followersCount = dataSnapshot.getChildrenCount();
                            user.setFollowersCount((int) followersCount);
                            users.add(user);
                            sortUsersByFollowersCount(users);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e("Giggly", "Failed to fetch followers count for user " + userId + ": " + databaseError.getMessage());
                        }
                    });
                    count++;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Giggly", "Failed to fetch users: " + databaseError.getMessage());
            }
        });

    }

    private void sortUsersByFollowersCount(List<User> users) {
        Collections.sort(users, new Comparator<User>() {
            @Override
            public int compare(User user1, User user2) {
                return Integer.compare(user2.getFollowersCount(), user1.getFollowersCount());
            }
        });
        mUsers.clear();
        mUsers.addAll(users);
        userAdapter.notifyDataSetChanged();
    }



//    private void calculateLaughPercentForAllUsers() {
//        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
//        ValueEventListener usersListener = new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()) {
//                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
//                        String userId = userSnapshot.getKey();
//                        calculateLaughPercentForUser(userId);
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                // Handle error
//                Log.e("Giggly", "Failed to fetch users: " + databaseError.getMessage());
//            }
//        };
//        usersRef.addListenerForSingleValueEvent(usersListener);
//    }
//
//    private void calculateLaughPercentForUser(String userId) {
//        DatabaseReference userPostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
//        ValueEventListener postsListener = new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()) {
//                    List<String> postIds = new ArrayList<>();
//                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                        Post post = snapshot.getValue(Post.class);
//                        if (post != null && post.getPublisher().equals(userId)) {
//                            String postId = post.getPostId();
//                            postIds.add(postId);
//                        }
//                    }
//                    // Once we have all postIds, we can fetch likes and dislikes for each post
//                    Log.d("Giggly", "Fetching likes and dislikes for user's posts...");
//                    fetchLikesAndDislikesForUser(userId, postIds);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                // Handle error
//                Log.e("Giggly", "Failed to fetch user posts: " + databaseError.getMessage());
//            }
//        };
//        userPostsRef.addListenerForSingleValueEvent(postsListener);
//    }
//
//    private void fetchLikesAndDislikesForUser(String userId, List<String> postIds) {
//        DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
//        DatabaseReference dislikesRef = FirebaseDatabase.getInstance().getReference().child("Boos");
//
//        final int[] totalLikes = {0};
//        final int[] totalDislikes = {0};
//
//        for (String postId : postIds) {
//            // Fetch likes for each post
//            likesRef.child(postId).addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                    if (dataSnapshot.exists()) {
//                        totalLikes[0] += dataSnapshot.getChildrenCount(); // Counting the number of likes for this post
//                    }
//                    // After fetching likes for all posts, calculate the laugh percent
//                    calculateLaughPercentForUser(userId, totalLikes[0], totalDislikes[0]);
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError) {
//                    // Handle error
//                    Log.e("Giggly", "Failed to fetch likes for post " + postId + ": " + databaseError.getMessage());
//                }
//            });
//
//            // Fetch dislikes for each post
//            dislikesRef.child(postId).addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                    if (dataSnapshot.exists()) {
//                        totalDislikes[0] += dataSnapshot.getChildrenCount(); // Counting the number of dislikes for this post
//                    }
//                    // After fetching dislikes for all posts, calculate the laugh percent
//                    calculateLaughPercentForUser(userId, totalLikes[0], totalDislikes[0]);
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError) {
//                    // Handle error
//                    Log.e("Giggly", "Failed to fetch dislikes for post " + postId + ": " + databaseError.getMessage());
//                }
//            });
//        }
//    }
//
//    private void calculateLaughPercentForUser(String userId, int totalLikes, int totalDislikes) {
//        // Calculate laugh percent for the user
//        // Your existing calculation logic goes here...
//        // For example:
//        double laughPercent = (double) totalLikes / (totalLikes + totalDislikes);
//        int percent = (int) (laughPercent * 100);
//        laughPercentages.put(userId, percent);
//        Log.d("Giggly", "Laugh percent calculated for user " + userId + ": " + percent + "%");
//
//
//
//        // Sort users by laugh percentage
//        sortUsersByLaughPercentage();
//    }
//    private void sortUsersByLaughPercentage() {
//        // Sort mUsers list based on laugh percentage
//        Collections.sort(mUsers, new Comparator<User>() {
//            @Override
//            public int compare(User user1, User user2) {
//                // Get laugh percentages for both users
//                Integer percent1 = laughPercentages.get(user1.getId());
//                Integer percent2 = laughPercentages.get(user2.getId());
//
//                // Sort in descending order (from highest to lowest percentage)
//                return percent2.compareTo(percent1);
//            }
//        });
//
//        // Clear the existing list and add the sorted users
//        Log.d("LaughPercentage", "Laugh Percentage HashMap: " + laughPercentages.toString());
//
//
//
//        // Notify adapter of the data change
//        userAdapter.notifyDataSetChanged();
//    }




    private void updateSearchHistory(String userId, String searchQuery) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();


        if (currentUser != null && userId.equals(currentUser.getUid())) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);

                    if (user != null) {
                        List<String> history = user.getSearchHistory();


                        if (history == null) {
                            history = new ArrayList<>();
                        }


                        history.remove(searchQuery);


                        history.add(0, searchQuery);


                        if (history.size() > 10) {
                            history = history.subList(0, 10);
                        }

                        userRef.child("searchHistory").setValue(history);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }



            public List<User> getSearchHistory() {
        return searchHistory;
    }


}