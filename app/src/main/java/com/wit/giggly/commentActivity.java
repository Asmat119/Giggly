package com.wit.giggly;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.wit.giggly.Adapter.CommentAdapter;
import com.wit.giggly.Model.Comment;
import com.wit.giggly.Model.MyCustomNotification;
import com.wit.giggly.Model.Post;
import com.wit.giggly.Model.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class commentActivity extends AppCompatActivity {


    private RecyclerView recyclerView;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;

    private EditText addComment;
    private CircleImageView imageProfile;
    private TextView post;

    private String postId;
    private String authorId;

    private static final String FCM_API = "https://fcm.googleapis.com/fcm/send";
    private static final String SERVER_KEY = "AAAAz-E70BY:APA91bG4JmyzoErCHnztd7d9Gg6pqQ2lAfn0xaX2pxX_UnbRGFjNE0LOwJyKhbuvzCu6crwXYv1zjBdL_XhdARiPdxbE5XC2sLOZ1qMIsDy69WOW1m0UDqt3h3QfC91q4IiYck2DBZpP";




    FirebaseUser fUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

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
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TypedValue typedValueTitle = new TypedValue();
        getTheme().resolveAttribute(R.attr.paragraph2, typedValueTitle, true);
        int titleColor = typedValueTitle.data;

        toolbar.setTitleTextColor(titleColor);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Comment");
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
                finish();
            }
        });


        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");
        authorId = intent.getStringExtra("authorId");

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, commentList, postId);

        recyclerView.setAdapter(commentAdapter);

        addComment = findViewById(R.id.add_comment);
        imageProfile = findViewById(R.id.image_profile);
        post = findViewById(R.id.post);

        fUser = FirebaseAuth.getInstance().getCurrentUser();

        getUserImage();


        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String commentText = addComment.getText().toString().trim().replaceAll("[^\\p{L}\\p{M}\\p{N}\\p{P}\\p{Z}\\p{Cf}\\p{Cs}\\s]","");
                if (commentText.startsWith(" ")) {
                    // Remove leading spaces
                    addComment.getText().toString().trim();
                }
//                if (commentText.isEmpty()  ) {
//                    Toast.makeText(commentActivity.this, "Please enter a valid comment", Toast.LENGTH_SHORT).show();
//                    return;
//                }
                if (commentText.contains("   ")) {
                    Toast.makeText(commentActivity.this, "Please check the number of spaces in your comment", Toast.LENGTH_SHORT).show();
                    return;
                }
                putComment();




//                String commentText = addComment.getText().toString().trim();
//                if (TextUtils.isEmpty(commentText)) {
//                    Toast.makeText(commentActivity.this, "No comment added!", Toast.LENGTH_SHORT).show();
//                } else {
//                    putComment();
//                }
            }
        });

        getComment();
    }

    private void getComment() {


        FirebaseDatabase.getInstance().getReference().child("Comments").child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentList.clear();


                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Comment comment = snapshot.getValue(Comment.class);
                    comment.setComment(comment.getComment().trim()); // trim the comment
                    commentList.add(comment);
                }

                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void putComment() {
        String commentText = addComment.getText().toString().trim();

        if (TextUtils.isEmpty(commentText)) {
            Toast.makeText(commentActivity.this, "Wow... try that again I dare you", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference().child("Comments").child(postId);
        String commentId = commentsRef.push().getKey();

        HashMap<String, Object> commentMap = new HashMap<>();
        commentMap.put("id", commentId);
        commentMap.put("comment",  commentText);
        commentMap.put("publisher", fUser.getUid());
        commentMap.put("timestamp", ServerValue.TIMESTAMP);


        addComment.setText("");

        commentsRef.child(commentId).setValue(commentMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {



                            FirebaseDatabase.getInstance().getReference().child("Posts").child(postId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                String postAuthorId = dataSnapshot.child("publisher").getValue(String.class);
                                                String postImageUrl = dataSnapshot.child("image").getValue(String.class);

                                                createAndSendCommentNotification(postAuthorId, commentText ,  fUser.getUid(),"Comment", postId);
                                            } else {
                                                Toast.makeText(commentActivity.this, "Post not found!", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Toast.makeText(commentActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(commentActivity.this, "Failed to add comment: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void getUserImage() {

        FirebaseDatabase.getInstance().getReference().child("Users").child(fUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if(user != null) {
                    if (user.getImageurl().equals("default")) {
                        imageProfile.setImageResource(R.drawable.no_profile_image);
                    } else {
                        Picasso.get().load(user.getImageurl()).into(imageProfile);
                    }
                }else{
                    imageProfile.setImageResource(R.drawable.no_profile_image);;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void createAndSendCommentNotification(String receiverUserId, String message, String senderUserId,String notifType,String postID) {
        String notificationId = FirebaseDatabase.getInstance().getReference("notifications").push().getKey();
        MyCustomNotification notification = new MyCustomNotification(receiverUserId, notificationId, message, senderUserId, notifType, postID);
        DatabaseReference notificationRef = FirebaseDatabase.getInstance().getReference("notifications").child(receiverUserId).child(notificationId);
        notificationRef.setValue(notification).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    if (!senderUserId.equals(receiverUserId)) {
                        // Send the like notification
                        sendCommentNotificationToUser(receiverUserId, message, notification);
                    }
                } else {

                }
            }
        });
    }


    private int calculatePointsForComment(int commentLength) {
        if (commentLength >= 1 && commentLength <= 9) {
            return 2;
        } else if (commentLength >= 10 && commentLength <= 20) {
            return 3;
        } else if (commentLength >= 21 && commentLength <= 34) {
            return 4;
        } else {
            return 0;
        }
    }

    private void updatePointsInDatabase(int pointsToAdd) {
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(postId);
        postRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                Post post = mutableData.getValue(Post.class);
                if (post != null) {
                    int newPoints = post.getPoints() + pointsToAdd;
                    post.setPoints(newPoints);
                    mutableData.setValue(post);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                if (databaseError != null) {
                    Toast.makeText(commentActivity.this, "Failed to update points: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void sendCommentNotificationToUser(String userId, String message,MyCustomNotification notification) {

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
                            notificationData.put("title", "Someone heckled at your joke...");
                            notificationData.put("body", notification.getMessage());

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

}