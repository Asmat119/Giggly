package com.wit.giggly.Adapter;

import static com.wit.giggly.StartActivity.CHANNEL_ID;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

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
import com.wit.giggly.Model.MyCustomNotification;
import com.wit.giggly.Model.Post;
import com.wit.giggly.Model.User;
import com.wit.giggly.NotificationsActivity;
import com.wit.giggly.PostDetailActivity;
import com.wit.giggly.ProfileActivity;
import com.wit.giggly.R;

import java.util.List;


public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {

   private Context mContext;
   private FirebaseUser firebaseUser;
   private List<MyCustomNotification> mNotifications;

   public NotificationsAdapter(Context mContext, List<MyCustomNotification> mNotifications) {
      this.mContext = mContext;
      this.mNotifications = mNotifications;
      firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
   }

   @NonNull
   @Override
   public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(mContext).inflate(R.layout.notification_item, parent, false);

      return new NotificationsAdapter.ViewHolder(view);
   }

   @Override
   public void onBindViewHolder(@NonNull ViewHolder holder, int position) {



         final MyCustomNotification notification = mNotifications.get(position);

         long timeStampMillis = notification.getTimestamp();
         long currentTimeMillis = System.currentTimeMillis();
         long timeDifferenceMillis = currentTimeMillis - timeStampMillis;

         long seconds = timeDifferenceMillis / 1000;
         long minutes = seconds / 60;
         long hours = minutes / 60;
         long days = hours / 24;
         long weeks = days / 7;
         long months = days / 30;
         long years = days / 365;

         String timeAgo;
         if (years > 0) {
            timeAgo = years + "y";
         } else if (months > 0) {
            timeAgo = months + "mo";
         } else if (weeks > 0) {
            timeAgo = weeks + "w";
         } else if (days > 0) {
            timeAgo = days + "d";
         } else if (hours > 0) {
            timeAgo = hours + "hr";
         } else if (minutes > 0) {
            timeAgo = minutes + "m";
         } else {
            timeAgo = seconds + "s";
         }


         getUser(holder.imageProfile, holder.username, notification.getSentByUserID(),holder.followbtn,holder.postImage,holder.postimageCard);


         // Check if the notification type is Follow
         if (notification.getNotifType().equals("Follow")) {
            holder.postImage.setVisibility(View.INVISIBLE);
            holder.followbtn.setVisibility(View.VISIBLE);
            holder.postimageCard.setVisibility(View.INVISIBLE);
            isFollowed(notification.getSentByUserID(), holder.followbtn);
         } else {
            holder.postImage.setVisibility(View.VISIBLE);
            holder.followbtn.setVisibility(View.INVISIBLE);
            holder.postimageCard.setVisibility(View.VISIBLE);
            getPostImage(holder.postImage, notification.getPostID());
         }


// editing the notification string for comments
         String message = notification.getMessage() + ".";
         String timeAgoText = " " + timeAgo + " ago";
         TypedValue typedValue = new TypedValue();
         mContext.getTheme().resolveAttribute(R.attr.notificationColors, typedValue, true);
         int color = typedValue.data;

         if (message.length() > 21) {

            message = message.substring(0, 21) + "...";
         }


         SpannableString messageSpannable = new SpannableString(message);
         messageSpannable.setSpan(new ForegroundColorSpan(color), 0, message.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

         SpannableString timeAgoSpannable = new SpannableString(timeAgoText);
         timeAgoSpannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.mediumdarkdarkGrey)), 0, timeAgoText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

         if (notification.getNotifType().equals("Comment")) {

            String commentText = "Commented on your post: ";
            SpannableString commentSpannable = new SpannableString(commentText);
            commentSpannable.setSpan(new ForegroundColorSpan(color), 0, commentText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);


            CharSequence finalText = TextUtils.concat(commentSpannable, messageSpannable, timeAgoSpannable);
            holder.comment.setText(finalText);
         } else {

            CharSequence finalText = TextUtils.concat(messageSpannable, timeAgoSpannable);
            holder.comment.setText(finalText);
         }


         if (notification.getSentByUserID().equals(notification.getGoingToUserID())) {
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
            holder.itemView.setVisibility(View.GONE);
         } else {
            // Handle click events
            holder.postImage.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                  String usernameText = holder.username.getText().toString();
                  if (usernameText.equals("Deleted Account") || usernameText.equals("User not found")) {
                     // Remove the image
                     holder.postImage.setImageDrawable(null);
                     holder.postimageCard.setVisibility(View.GONE);
                     // Disable clicks on the image
                     holder.postImage.setClickable(false);
                     return;
                  }

                  v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                  mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
                          .edit().putString("postid", notification.getPostID()).apply();

                  Intent intent = new Intent(mContext, PostDetailActivity.class);
                  intent.putExtra("post_id", notification.getPostID());
                  intent.putExtra("postAuthorId", notification.getGoingToUserID());
                  Log.e("timestamp", String.valueOf(notification.getTimestamp()));
                  ((FragmentActivity) mContext).startActivity(intent);
               }
            });


            holder.imageProfile.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                  String usernameText = holder.username.getText().toString();
                  if (usernameText.equals("Deleted Account") || usernameText.equals("User not found")) {
                     // Do nothing if the username is "Deleted Account" or "User not found"
                     return;
                  }

                  v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                  mContext.getSharedPreferences("PROFILE", Context.MODE_PRIVATE)
                          .edit().putString("profileId", notification.getSentByUserID()).apply();

                  mContext.startActivity(new Intent(mContext, ProfileActivity.class));
               }
            });


            holder.followbtn.setOnClickListener(new View.OnClickListener() {
               private boolean isClickable = true;
               Handler handler = new Handler();

               @Override
               public void onClick(View v) {
                  String usernameText = holder.username.getText().toString();
                  if (usernameText.equals("Deleted Account") || usernameText.equals("User not found")) {
                     holder.followbtn.setVisibility(View.GONE);
                     return;
                  }

                  if (!isClickable) {
                     // Button is disabled, do nothing
                     return;
                  }

                  Drawable followImage = holder.followbtn.getDrawable();
                  if (followImage != null && followImage.getConstantState().equals(ContextCompat.getDrawable(mContext, R.drawable.follow).getConstantState())) {
                     FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                             .child("following").child(notification.getSentByUserID()).setValue(true);

                     FirebaseDatabase.getInstance().getReference().child("Follow").child(notification.getSentByUserID())
                             .child("followers").child(firebaseUser.getUid()).setValue(true);
                     createAndSendFollowNotification(notification.getSentByUserID(), "Followed you", notification.getGoingToUserID(), "Follow");

                     holder.followbtn.setImageResource(R.drawable.add_friend);
                  } else if (followImage != null && followImage.getConstantState().equals(ContextCompat.getDrawable(mContext, R.drawable.add_friend).getConstantState())) {
                     FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                             .child("following").child(notification.getSentByUserID()).removeValue();

                     FirebaseDatabase.getInstance().getReference().child("Follow").child(notification.getSentByUserID())
                             .child("followers").child(firebaseUser.getUid()).removeValue();
                     retrieveFollowNotification(notification.getSentByUserID(), notification.getGoingToUserID());

                     holder.followbtn.setImageResource(R.drawable.follow);
                  }

                  // Disable the button for 2 seconds
                  isClickable = false;
                  handler.postDelayed(new Runnable() {
                     @Override
                     public void run() {
                        isClickable = true;
                     }
                  }, 2000); // 2 seconds delay
               }
            });


            holder.username.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                  String usernameText = holder.username.getText().toString();
                  if (!usernameText.equals("Deleted Account") && !usernameText.equals("User not found")) {
                     v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                     mContext.getSharedPreferences("PROFILE", Context.MODE_PRIVATE)
                             .edit().putString("profileId", notification.getSentByUserID()).apply();
                     mContext.startActivity(new Intent(mContext, ProfileActivity.class));
                  }
               }
            });

         }

   }

   @Override
   public int getItemCount() {
      return mNotifications.size();
   }

   public class ViewHolder extends RecyclerView.ViewHolder{

      public ImageView imageProfile;
      public ImageView postImage;
      public TextView username;
      public TextView comment;
      public ImageView followbtn;
      public CardView postimageCard;

      public ViewHolder(@NonNull View itemView) {
         super(itemView);

         imageProfile = itemView.findViewById(R.id.image_profile);
         postImage = itemView.findViewById(R.id.post_image);
         username = itemView.findViewById(R.id.username);
         comment = itemView.findViewById(R.id.comment);
         postimageCard = itemView.findViewById(R.id.post_imagecardv);
         followbtn = itemView.findViewById(R.id.notifFollowbtn);
      }
   }

   private void getPostImage(final ImageView imageView, String postId) {
      FirebaseDatabase.getInstance().getReference().child("Posts").child(postId).addValueEventListener(new ValueEventListener() {
         @Override
         public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
               Post post = dataSnapshot.getValue(Post.class);
               if (post != null && post.getImage() != null) {
                  Picasso.get().load(post.getImage()).placeholder(R.drawable.loadingimage).into(imageView);
               } else {

               }
            } else {

            }
         }


         @Override
         public void onCancelled(@NonNull DatabaseError databaseError) {

         }
      });
   }

   private void isFollowed(final String id, final ImageView btnFollow) {
      DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
              .child("following");
      reference.addValueEventListener(new ValueEventListener() {
         @Override
         public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.child(id).exists()) {
               // Set the "Following" image resource if the user is followed
               btnFollow.setImageResource(R.drawable.add_friend);
            } else {
               // Set the "Follow" image resource if the user is not followed
               btnFollow.setImageResource(R.drawable.follow);
            }
         }

         @Override
         public void onCancelled(@NonNull DatabaseError databaseError) {
            // Handle onCancelled
         }
      });
   }


   private void getUser(final ImageView imageView, final TextView textView, String userId,final ImageView followbtn,final ImageView postimage, final CardView cardview) {
      FirebaseDatabase.getInstance().getReference().child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
         @Override
         public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
               User user = dataSnapshot.getValue(User.class);
               if (user != null) {
                  if (user.getImageurl().equals("default")) {
                     imageView.setImageResource(R.drawable.no_profile_image);
                  } else {
                     Picasso.get().load(user.getImageurl()).into(imageView);
                  }
                  textView.setText(user.getUsername());
               } else {

                  textView.setText("Deleted Account");
                  imageView.setImageResource(R.drawable.no_profile_image);
                  followbtn.setVisibility(View.GONE);
                  postimage.setVisibility(View.GONE);
                  cardview.setVisibility(View.GONE);

               }
            } else {

               textView.setText("User not found");
               imageView.setImageResource(R.drawable.no_profile_image);
               followbtn.setVisibility(View.GONE);
               postimage.setVisibility(View.GONE);
               cardview.setVisibility(View.GONE);
            }
         }

         @Override
         public void onCancelled(@NonNull DatabaseError databaseError) {
            // Handle database error
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
               //sendFollowNotificationToUser(senderUserId, message,notification);
            } else {
               // Handle failure
            }
         }
      });
   }

   private void sendFollowNotificationToUser(String userId, String message,MyCustomNotification notification) {
      // Create an intent to launch when the notification is clicked
      Intent intent = new Intent(mContext, NotificationsActivity.class);
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

      // Create a notification
      NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
              .setSmallIcon(R.drawable.giggly_logo_2024_jan)
              .setContentTitle(notification.getNotifType())
              .setContentText("Somebody followed you...")
              .setAutoCancel(true)
              .setContentIntent(pendingIntent);

      // Get the NotificationManager service
      NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);


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
                  // Toast.makeText(mContext, notificationId, Toast.LENGTH_SHORT).show();

                  break;
               }
            }
         }

         @Override
         public void onCancelled(@NonNull DatabaseError databaseError) {

         }
      });
   }


}