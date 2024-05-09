package com.wit.giggly.Adapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;


import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.github.marlonlom.utilities.timeago.TimeAgoMessages;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.wit.giggly.Fragments.ProfileFragment;
import com.wit.giggly.ImageCache;
import com.wit.giggly.MainActivity;
import com.wit.giggly.Model.Comment;
import com.wit.giggly.Model.MyCustomNotification;
import com.wit.giggly.Model.Post;
import com.wit.giggly.Model.User;
import com.wit.giggly.ProfileActivity;
import com.wit.giggly.R;
import com.wit.giggly.commentActivity;

import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;


public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private Context mContext;
    private List<Comment> mComments;
    private boolean isTextChanged = false;
    String postId;

    private FirebaseUser fUser;

    public CommentAdapter(Context mContext, List<Comment> mComments , String postId) {
        this.mContext = mContext;
        this.mComments = mComments;
        this.postId = postId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.comment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        fUser = FirebaseAuth.getInstance().getCurrentUser();

        final Comment comment = mComments.get(position);

        long timeStampMillis = comment.getTimestamp();
        TimeAgoMessages messages = new TimeAgoMessages.Builder().withLocale(Locale.getDefault()).build();
        String timeAgo = TimeAgo.Companion.using(timeStampMillis, messages);

        holder.comment.setText(comment.getComment());
        holder.timestamp.setText(timeAgo);



        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(comment.getPublisher())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);

                        if (user != null) {
                            holder.username.setText(user.getUsername());
                            if (user.getImageurl().equals("default")) {
                                holder.imageProfile.setImageResource(R.drawable.no_profile_image);
                            } else {
                                Picasso.get().load(user.getImageurl()).into(holder.imageProfile);
                            }
                            isVerfied(user.getId(), holder.verifiedBadge);

                        } else {

                            holder.username.setText("Deleted User");
                            holder.imageProfile.setImageResource(R.drawable.no_profile_image); // Set a default image or handle it as per your requirement
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!holder.username.equals("Deleted User")) {
                    // Save the original text
                    String originalText = holder.comment.getText().toString();


                    if (!isTextChanged) {
                        String randomPhrase = getRandomPhrase();
                        holder.comment.setText(randomPhrase);
                        isTextChanged = true;


                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                holder.comment.setText(originalText);
                                isTextChanged = false;
                            }
                        }, 4000);
                    }
                } else {
                    Toast.makeText(mContext, "User has been deleted", Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!holder.username.equals("Deleted User")) {
                    mContext.getSharedPreferences("PROFILE", Context.MODE_PRIVATE).edit().putString("profileId", comment.getPublisher()).apply();

                    mContext.startActivity(new Intent(mContext, ProfileActivity.class));
                }else{
                    Toast.makeText(mContext, "User has been deleted", Toast.LENGTH_SHORT).show();
                }
            }
        });
        holder.imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!holder.username.equals("Deleted User")) {
                    mContext.getSharedPreferences("PROFILE", Context.MODE_PRIVATE).edit().putString("profileId", comment.getPublisher()).apply();

                    mContext.startActivity(new Intent(mContext, ProfileActivity.class));
                }else{
                    Toast.makeText(mContext, "User has been deleted", Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (comment.getPublisher().endsWith(fUser.getUid())) {
                    //https://stackoverflow.com/questions/42273188/problems-with-custom-layout-for-alertdialog
                    View alertDeleteDialog = LayoutInflater.from(mContext).inflate(R.layout.delete_comment_dialog,null);
                    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(mContext);
                    builder.setView(alertDeleteDialog);

                    Button confirmbtn = alertDeleteDialog.findViewById(R.id.acceptBtn);
                    Button cancelBTN = alertDeleteDialog.findViewById(R.id.cancelBtn);

                    final androidx.appcompat.app.AlertDialog dialog = builder.create();

                    //https://stackoverflow.com/questions/10795078/dialog-with-transparent-background-in-android
                    //make window behind popup transparent
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.setCanceledOnTouchOutside(false);

                    dialog.show();

                    cancelBTN.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.cancel();

                        }
                    });

                    confirmbtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            FirebaseDatabase.getInstance().getReference().child("Comments")
                                    .child(postId).child(comment.getId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                int commentLength = comment.getComment().length();


                                                int pointsToDeduct = calculatePointsForCommentDeletion(commentLength);


                                                //updatePointsInDatabase(-pointsToDeduct);
                                                FirebaseDatabase.getInstance().getReference().child("Posts").child(postId)
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                if (dataSnapshot.exists()) {
                                                                    String postAuthorId = dataSnapshot.child("publisher").getValue(String.class);
                                                                    String postImageUrl = dataSnapshot.child("image").getValue(String.class);

                                                                    // Pass post details to createAndSendCommentNotification() method
                                                                    retrieveCommentNotification(postAuthorId, fUser.getUid(), postId);

                                                                } else {
                                                                    Toast.makeText(mContext, "Post not found!", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                Toast.makeText(mContext, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                Toast.makeText(mContext, "Comment deleted successfully!", Toast.LENGTH_SHORT).show();
                                                dialog.dismiss();
                                            }
                                        }
                                    });

                        }
                    });
                }else{

                }

                return true;
            };
        });

        holder.comment.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (comment.getPublisher().endsWith(fUser.getUid())) {
                    //https://stackoverflow.com/questions/42273188/problems-with-custom-layout-for-alertdialog
                    View alertDeleteDialog = LayoutInflater.from(mContext).inflate(R.layout.delete_comment_dialog,null);
                    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(mContext);
                    builder.setView(alertDeleteDialog);

                    Button confirmbtn = alertDeleteDialog.findViewById(R.id.acceptBtn);
                    Button cancelBTN = alertDeleteDialog.findViewById(R.id.cancelBtn);

                    final androidx.appcompat.app.AlertDialog dialog = builder.create();

                    //https://stackoverflow.com/questions/10795078/dialog-with-transparent-background-in-android
                    //make window behind popup transparent
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.setCanceledOnTouchOutside(false);

                    dialog.show();

                    cancelBTN.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.cancel();

                        }
                    });

                    confirmbtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            FirebaseDatabase.getInstance().getReference().child("Comments")
                                    .child(postId).child(comment.getId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                int commentLength = comment.getComment().length();


                                                int pointsToDeduct = calculatePointsForCommentDeletion(commentLength);


                                                updatePointsInDatabase(-pointsToDeduct);

                                                FirebaseDatabase.getInstance().getReference().child("Posts").child(postId)
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                if (dataSnapshot.exists()) {
                                                                    String postAuthorId = dataSnapshot.child("publisher").getValue(String.class);
                                                                    String postImageUrl = dataSnapshot.child("image").getValue(String.class);

                                                                    // Pass post details to createAndSendCommentNotification() method
                                                                    retrieveCommentNotification(postAuthorId, fUser.getUid(), postId);

                                                                } else {
                                                                    Toast.makeText(mContext, "Post not found!", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                Toast.makeText(mContext, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                Toast.makeText(mContext, "Comment deleted successfully!", Toast.LENGTH_SHORT).show();
                                                dialog.dismiss();
                                            }
                                        }
                                    });

                        }
                    });
                }else{

                }

                return true;
            };
        });

    }

    private String getRandomPhrase() {
        String[] phrases = {"Oooga Booga my ***", "All hail the lord saviour burger king", "Wait.... where am I", "BOOOOOOOOOO", "You suck fat juicy meat patties"};
        int randomIndex = (int) (Math.random() * phrases.length);
        return phrases[randomIndex];
    }





    @Override
    public int getItemCount() {
        return mComments.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public CircleImageView imageProfile;
        public TextView username;
        public TextView comment;
        public TextView timestamp;
        public ImageView verifiedBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageProfile = itemView.findViewById(R.id.image_profile);
            username = itemView.findViewById(R.id.username);
            comment = itemView.findViewById(R.id.comment);
            timestamp = itemView.findViewById(R.id.timestamp);
            verifiedBadge = itemView.findViewById(R.id.verifiedbadge);
        }
    }


    private void retrieveCommentNotification(String postPublisherId, String senderUserId,String postID) {
        DatabaseReference notificationsRef = FirebaseDatabase.getInstance().getReference("notifications").child(postPublisherId);

        notificationsRef.orderByChild("sentByUserID").equalTo(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                   // Toast.makeText(mContext, postID, Toast.LENGTH_SHORT).show();
                    MyCustomNotification notification = snapshot.getValue(MyCustomNotification.class);
                    if (notification != null && notification.getNotifType().equals("Comment") && notification.getSentByUserID().equals(senderUserId)&&notification.getPostID().equals(postID)) {

                        String notificationId = snapshot.getKey();
                        notificationsRef.child(notificationId).removeValue();
                        //Toast.makeText(mContext, notificationId, Toast.LENGTH_SHORT).show();

                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private int calculatePointsForCommentDeletion(int commentLength) {

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
                    Toast.makeText(mContext, "Failed to update points: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void isVerfied(final String id, final ImageView verifiedbadge){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(id);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null && user.getVerified() != null) {
                        boolean isVerified = user.getVerified();
                        if (isVerified) {
                            // Set visibility of verified badge to VISIBLE
                            verifiedbadge.setVisibility(View.VISIBLE);
                        } else {
                            // Set visibility of verified badge to GONE
                            verifiedbadge.setVisibility(View.GONE);
                        }
                    } else {
                        // User data or verified field is null, set visibility to GONE
                        verifiedbadge.setVisibility(View.GONE);
                    }
                } else {
                    // User data does not exist, set visibility to GONE
                    verifiedbadge.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
            }
        });
    }


}