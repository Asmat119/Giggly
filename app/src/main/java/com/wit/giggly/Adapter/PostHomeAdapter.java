package com.wit.giggly.Adapter;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.github.marlonlom.utilities.timeago.TimeAgoMessages;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hendraanggrian.appcompat.widget.SocialTextView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.wit.giggly.AboutAccountActivity;
import com.wit.giggly.AdHelper;
import com.wit.giggly.FollowersActivity;
import com.wit.giggly.Fragments.ExploreFragment;
import com.wit.giggly.Fragments.HomeFragment;
import com.wit.giggly.Fragments.PostDetailFragment;
import com.wit.giggly.Fragments.SearchFragment;
import com.wit.giggly.Model.MyCustomNotification;
import com.wit.giggly.Model.Post;
import com.wit.giggly.Model.User;
import com.wit.giggly.PostDetailActivity;
import com.wit.giggly.ProfileActivity;
import com.wit.giggly.R;
import com.wit.giggly.commentActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class PostHomeAdapter extends RecyclerView.Adapter<PostHomeAdapter.Viewholder> {

   private Context mContext;
   private List<Post> mPosts;
   private String postid;
   public Button cancelBTN;
   public Button deletePost;
   int booColor;
   int likeColor;

   public Button deletepostBtn;
   public Button whoisbtn;
   public ImageView closebtn;

   private static final String FCM_API = "https://fcm.googleapis.com/fcm/send";
   private static final String SERVER_KEY = "AAAAz-E70BY:APA91bG4JmyzoErCHnztd7d9Gg6pqQ2lAfn0xaX2pxX_UnbRGFjNE0LOwJyKhbuvzCu6crwXYv1zjBdL_XhdARiPdxbE5XC2sLOZ1qMIsDy69WOW1m0UDqt3h3QfC91q4IiYck2DBZpP";

   private List<Object> items;

   private FirebaseUser firebaseUser;

   public String postAuthorId;
   AdHelper adHelper;



   public PostHomeAdapter(Context mContext, List<Post> mPosts, AdHelper adHelper) {
      this.mContext = mContext;
      this.mPosts = mPosts;
      firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
      this.adHelper = adHelper;
   }



   @NonNull
   @Override
   public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

      FragmentActivity activity = (FragmentActivity) mContext;
      Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container);

      if (fragment instanceof HomeFragment) {
         View view = LayoutInflater.from(mContext).inflate(R.layout.new_post_item, parent, false);
         return new Viewholder(view);
      } else if(fragment instanceof PostDetailFragment){
         View view = LayoutInflater.from(mContext).inflate(R.layout.new_post_item, parent, false);
         return new Viewholder(view);

      }
//      else if(mContext instanceof PostDetailActivity){
//         //change to new item layout called postdetail item instead of new_post_item
//         View view = LayoutInflater.from(mContext).inflate(R.layout.post_detail_item, parent, false);
//         return new Viewholder(view);
//      }
      else{
         View view = LayoutInflater.from(mContext).inflate(R.layout.photo_item_all, parent, false);
         return new Viewholder(view);
      }
   }


   @Override
   public void onBindViewHolder(@NonNull final Viewholder holder, int position) {
      final Post post = mPosts.get(position);
      holder.frameLayout.setVisibility(View.GONE);

//      if (position  % 5 == 0){
//      holder.frameLayout
//              .setVisibility(View.VISIBLE);
//      adHelper.loadAndDisplayNativeAd(mContext,holder.frameLayout);
//      }
//      else {
//         holder.frameLayout.setVisibility(View.GONE);
//      }


      FragmentActivity activity = (FragmentActivity) mContext;
      Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container);

//timeago stuff
      long timeStampMillis = post.getTimestamp();
      TimeAgoMessages messages = new TimeAgoMessages.Builder().withLocale(Locale.getDefault()).build();
      String timeAgo = TimeAgo.Companion.using(timeStampMillis, messages);

      if (fragment instanceof HomeFragment|| fragment instanceof PostDetailFragment || mContext instanceof PostDetailActivity) {

         String description = post.getDescription().replaceAll("[^\\p{L}\\p{M}\\p{N}\\p{P}\\p{Z}\\p{Cf}\\p{Cs}\\s]", "");
         description = description.trim();
         String descriptionCapitalized = description.substring(0, 1).toUpperCase() + description.substring(1);

         if (mContext instanceof FragmentActivity) {

            if (fragment instanceof HomeFragment || fragment instanceof SearchFragment) {
               if (descriptionCapitalized.length() > 100) {
                  descriptionCapitalized = descriptionCapitalized.substring(0, 100) + "...";
               }
               holder.description.setText(post.getTitle());
            }
         }

         if (fragment instanceof HomeFragment || fragment instanceof PostDetailFragment) {

            holder.timestamp.setText(timeAgo);

         } else if (mContext instanceof PostDetailActivity) {

//            holder.imageProfile.setVisibility(View.GONE);
//            holder.username.setVisibility(View.GONE);
//            holder.author.setVisibility(View.GONE);
            holder.timestampdetail.setText(timeAgo);
//            holder.posttitle.setVisibility(View.VISIBLE);
//            holder.posttitle.setText(post.getTitle());
         }


         if (firebaseUser != null && post.getPublisher().equals(firebaseUser.getUid())) {
            // Show the "more" icon for the logged-in user's posts
            holder.more.setVisibility(View.VISIBLE);

            // Set an OnClickListener for the "more" icon to show the options menu
            holder.more.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                  showOptionsMenu(post.getPostId());
               }
            });
         } else {
            // Hide the "more" icon for posts not owned by the logged-in user
            holder.more.setVisibility(View.GONE);
         }




         holder.description.setText(descriptionCapitalized);

         //showing a different loading image for each theme

         Picasso.get().load(post.getImage()).placeholder(R.drawable.loadingimage).into(holder.postImage);




         FirebaseDatabase.getInstance().getReference().child("Users").child(post.getPublisher()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               User user = dataSnapshot.getValue(User.class);

               if (user != null ) {
                  if (user.getImageurl().equals("default")) {
                     holder.imageProfile.setImageResource(R.drawable.no_profile_image);
                  } else {
                     Picasso.get().load(user.getImageurl()).placeholder(R.drawable.no_profile_image).into(holder.imageProfile);
                  }
                  String username = user.getUsername().length() > 20 ? user.getUsername().substring(0, 20) : user.getUsername();
                  String author = user.getFirstname() +" "+ user.getSecondname();
                  holder.username.setText("@" + username);
                  holder.author.setText(author);
                  holder.category.setText(post.getCategory());
                  postAuthorId = post.getPublisher();



               }else{
                  holder.imageProfile.setImageResource(R.drawable.no_profile_image);
                  holder.username.setText("@DeletedAccount");
                  holder.author.setText("Delete user");
               }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
         });


         isLiked(post.getPostId(), holder.like);
         noOfLikes(post.getPostId(), holder.noOfLikes);
         isBooed(post.getPostId(),holder.boo);
         noOfBoos(post.getPostId(),holder.noOfBoos);
         getComments(post.getPostId(), holder.noOfComments);
         // isSaved(post.getPostId(), holder.save);

         holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if (holder.like.getTag().equals("like")) {
                  //animation
                  ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(holder.like, "scaleX", 1.0f, 1.5f);
                  ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(holder.like, "scaleY", 1.0f, 1.5f);
                  AnimatorSet scaleUp = new AnimatorSet();
                  scaleUp.play(scaleUpX).with(scaleUpY);
                  scaleUp.setDuration(300);
                  scaleUp.setInterpolator(new OvershootInterpolator());
                  ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(holder.like, "scaleX", 1.5f, 1.0f);
                  ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(holder.like, "scaleY", 1.5f, 1.0f);
                  AnimatorSet scaleDown = new AnimatorSet();
                  scaleDown.play(scaleDownX).with(scaleDownY);
                  scaleDown.setDuration(300);
                  scaleDown.setInterpolator(new OvershootInterpolator());
                  AnimatorSet animators = new AnimatorSet();
                  animators.playSequentially(scaleUp, scaleDown);
                  animators.start();
                  //firebase stuff
                  FirebaseDatabase.getInstance().getReference().child("Likes")
                          .child(post.getPostId()).child(firebaseUser.getUid()).setValue(true);

                  FirebaseDatabase.getInstance().getReference().child("Boos")
                          .child(post.getPostId()).child(firebaseUser.getUid()).removeValue();




                  createAndSendLikeNotification(post.getPublisher(), "Liked your post",firebaseUser.getUid(),"Like",post.getPostId());
                  retrieveBooNotification(post.getPublisher(), firebaseUser.getUid(), post.getPostId());

               } else {

                  //animation
                  ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(holder.like, "scaleX", 1.0f, 1.5f);
                  ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(holder.like, "scaleY", 1.0f, 1.5f);
                  AnimatorSet scaleUp = new AnimatorSet();
                  scaleUp.play(scaleUpX).with(scaleUpY);
                  scaleUp.setDuration(300);
                  scaleUp.setInterpolator(new OvershootInterpolator());

                  ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(holder.like, "scaleX", 1.5f, 1.0f);
                  ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(holder.like, "scaleY", 1.5f, 1.0f);
                  AnimatorSet scaleDown = new AnimatorSet();
                  scaleDown.play(scaleDownX).with(scaleDownY);
                  scaleDown.setDuration(300);
                  scaleDown.setInterpolator(new OvershootInterpolator());

                  AnimatorSet animators = new AnimatorSet();
                  animators.playSequentially(scaleUp, scaleDown);
                  animators.start();

                  FirebaseDatabase.getInstance().getReference().child("Likes")
                          .child(post.getPostId()).child(firebaseUser.getUid()).removeValue();



                  retrieveLikeNotification(post.getPublisher(), firebaseUser.getUid(), post.getPostId());




               }

            }
         });




         holder.boo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               if (holder.boo.getTag().equals("boo")) {
                  //animation
                  ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(holder.boo, "scaleX", 1.0f, 1.5f);
                  ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(holder.boo, "scaleY", 1.0f, 1.5f);
                  AnimatorSet scaleUp = new AnimatorSet();
                  scaleUp.play(scaleUpX).with(scaleUpY);
                  scaleUp.setDuration(300);
                  scaleUp.setInterpolator(new OvershootInterpolator());
                  ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(holder.boo, "scaleX", 1.5f, 1.0f);
                  ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(holder.boo, "scaleY", 1.5f, 1.0f);
                  AnimatorSet scaleDown = new AnimatorSet();
                  scaleDown.play(scaleDownX).with(scaleDownY);
                  scaleDown.setDuration(300);
                  scaleDown.setInterpolator(new OvershootInterpolator());
                  AnimatorSet animators = new AnimatorSet();
                  animators.playSequentially(scaleUp, scaleDown);
                  animators.start();
                  //firebase stuff
                  FirebaseDatabase.getInstance().getReference().child("Boos")
                          .child(post.getPostId()).child(firebaseUser.getUid()).setValue(true);

                  FirebaseDatabase.getInstance().getReference().child("Likes")
                          .child(post.getPostId()).child(firebaseUser.getUid()).removeValue();

                  createAndSendBooNotification(post.getPublisher(), "Disliked your post",firebaseUser.getUid(),"Boo",post.getPostId());
                  retrieveLikeNotification(post.getPublisher(), firebaseUser.getUid(), post.getPostId());
//
               } else {

                  //animation
                  ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(holder.boo, "scaleX", 1.0f, 1.5f);
                  ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(holder.boo, "scaleY", 1.0f, 1.5f);
                  AnimatorSet scaleUp = new AnimatorSet();
                  scaleUp.play(scaleUpX).with(scaleUpY);
                  scaleUp.setDuration(300);
                  scaleUp.setInterpolator(new OvershootInterpolator());

                  ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(holder.boo, "scaleX", 1.5f, 1.0f);
                  ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(holder.boo, "scaleY", 1.5f, 1.0f);
                  AnimatorSet scaleDown = new AnimatorSet();
                  scaleDown.play(scaleDownX).with(scaleDownY);
                  scaleDown.setDuration(300);
                  scaleDown.setInterpolator(new OvershootInterpolator());

                  AnimatorSet animators = new AnimatorSet();
                  animators.playSequentially(scaleUp, scaleDown);
                  animators.start();

                  FirebaseDatabase.getInstance().getReference().child("Boos")
                          .child(post.getPostId()).child(firebaseUser.getUid()).removeValue();

                  retrieveBooNotification(post.getPublisher(), firebaseUser.getUid(), post.getPostId());

               }

            }
         });


         holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               Intent intent = new Intent(mContext, commentActivity.class);
               intent.putExtra("postId", post.getPostId());
               intent.putExtra("authorId", post.getPublisher());
               mContext.startActivity(intent);
            }
         });









         holder.imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               if(fragment instanceof HomeFragment){
                  v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

                  if ( holder.username.equals("@DeletedAccount") && holder.author.equals("Delete user")) {

                     Toast.makeText(mContext, "Account deleted", Toast.LENGTH_SHORT).show();
                  }else{



                     mContext.getSharedPreferences("PROFILE", Context.MODE_PRIVATE)
                             .edit().putString("profileId", post.getPublisher()).apply();

                     mContext.startActivity(new Intent(mContext, ProfileActivity.class));
                  }
               }else{

               }



            }
         });

         holder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

               // Check if the user is null
               if ( holder.username.equals("@DeletedAccount")) {
                  // Handle the case where the user is null
                  Toast.makeText(mContext, "Account deleted", Toast.LENGTH_SHORT).show();
               } else {


                  mContext.getSharedPreferences("PROFILE", Context.MODE_PRIVATE)
                          .edit().putString("profileId", post.getPublisher()).apply();

                  mContext.startActivity(new Intent(mContext, ProfileActivity.class));       }
            }
         });


         holder.author.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

               if ( holder.username.equals("@DeletedAccount") && holder.author.equals("Delete user")) {
                  // Handle the case where the user is null
                  Toast.makeText(mContext, "Account deleted", Toast.LENGTH_SHORT).show();
               }else{


                  mContext.getSharedPreferences("PROFILE", Context.MODE_PRIVATE)
                          .edit().putString("profileId", post.getPublisher()).apply();


                  mContext.startActivity(new Intent(mContext, ProfileActivity.class));
               }
            }
         });

         holder.description.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               if(fragment instanceof HomeFragment){
                  v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                  mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit().putString("postid", post.getPostId()).apply();
//
//               ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction()
//                       .replace(R.id.fragment_container, new PostDetailFragment()).addToBackStack(null).commit();
                  Intent intent = new Intent(mContext, PostDetailActivity.class);
                  intent.putExtra("post_id", post.getPostId());
                  intent.putExtra("postAuthorId", post.getPublisher());
                  Log.e("timestamp",String.valueOf(post.getTimestamp()));
                  ((FragmentActivity) mContext).startActivity(intent);
               }else{

               }

            }
         });


         holder.postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(fragment instanceof HomeFragment) {
                  v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                  mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit().putString("postid", post.getPostId()).apply();
//
//               ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction()
//                       .replace(R.id.fragment_container, new PostDetailFragment()).addToBackStack(null).commit();
                  Intent intent = new Intent(mContext, PostDetailActivity.class);
                  intent.putExtra("post_id", post.getPostId());
                  intent.putExtra("postAuthorId", post.getPublisher());
                  Log.e("timestamp", String.valueOf(post.getTimestamp()));
                  ((FragmentActivity) mContext).startActivity(intent);
               }else{

               }

            }
         });



         holder.noOfLikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Intent intent = new Intent(mContext, FollowersActivity.class);
               intent.putExtra("id", post.getPublisher());
               intent.putExtra("title", "Likes");
               intent.putExtra("postid", post.getPostId());
               mContext.startActivity(intent);
            }
         });

         holder.noOfBoos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Intent intent = new Intent(mContext, FollowersActivity.class);
               intent.putExtra("id", post.getPublisher());
               intent.putExtra("title", "Boos");
               intent.putExtra("postid", post.getPostId());
               mContext.startActivity(intent);
            }
         });


      }
      else if(fragment instanceof SearchFragment){
         Picasso.get().load(post.getImage()).placeholder(R.drawable.loadingimage).into(holder.profileImage);





         holder.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
               mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit().putString("postid", post.getPostId()).apply();

//               ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction()
//                       .replace(R.id.fragment_container, new PostDetailFragment()).addToBackStack(null).commit();
               Intent intent = new Intent(mContext, PostDetailActivity.class);
               intent.putExtra("post_id", post.getPostId()); // Replace with the actual post ID

               ((FragmentActivity) mContext).startActivity(intent);
            }
         });




      }else if(fragment instanceof ExploreFragment){
         Picasso.get().load(post.getImage()).placeholder(R.drawable.loadingimage).into(holder.profileImage);

         holder.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
               mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit().putString("postid", post.getPostId()).apply();

//               ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction()
//                       .replace(R.id.fragment_container, new PostDetailFragment()).addToBackStack(null).commit();
               Intent intent = new Intent(mContext, PostDetailActivity.class);
               intent.putExtra("post_id", post.getPostId());
               intent.putExtra("postAuthorId", post.getPublisher());

               ((FragmentActivity) mContext).startActivity(intent);
            }
         });

      } else{
         String description = post.getDescription().trim();
         String firstWord = description.split(" ")[0];
         holder.postSquareProfile.setText(firstWord);
         holder.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
               mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit().putString("postid", post.getPostId()).apply();

//               ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction()
//                       .replace(R.id.fragment_container, new PostDetailFragment()).addToBackStack(null).commit();
               Intent intent = new Intent(mContext, PostDetailActivity.class);
               intent.putExtra("post_id", post.getPostId());
               intent.putExtra("postAuthorId", post.getPublisher());// Replace with the actual post ID

               ((FragmentActivity) mContext).startActivity(intent);
            }
         });
         FirebaseDatabase.getInstance().getReference().child("Users").child(post.getPublisher()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               User user = dataSnapshot.getValue(User.class);

               if (user != null) {
                  String author = user.getUsername().length() > 15 ? user.getUsername().substring(0, 15) : user.getUsername();

                  holder.atText.setText("@"+author);
                  holder.category.setText(post.getCategory());

                  if (user.getImageurl().equals("default")) {
                     holder.profileImage.setImageResource(R.drawable.no_profile_image);
                  } else {
                     Picasso.get().load(post.getImage())
                             .placeholder(R.drawable.loadingimage)

                             .into(holder.profileImage, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError(Exception e) {

                                }
                             });
                  }




               }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
         });


      }
   }





   @Override
   public int getItemCount() {
      return mPosts.size();
   }

   public class Viewholder extends RecyclerView.ViewHolder {

      public ImageView imageProfile;
      public TextView category;

      public ImageView postImage;

      public ImageView like;
      public ImageView comment;
      public ImageView save;
      public ImageView more;

      public TextView username;
      public TextView timestamp;
      public TextView noOfLikes;
      public TextView author;
      public TextView postSquareProfile;
      public TextView atText;
      public TextView posttitle;


      //public RelativeLayout newItemCard;
      public TextView noOfComments;
      public ImageView profileImage;
      public ImageView boo;

      public TextView noOfBoos;
      SocialTextView description;

      public TextView timestampdetail;
      public FrameLayout frameLayout;

      public Viewholder(@NonNull View itemView) {
         super(itemView);

         imageProfile = itemView.findViewById(R.id.image_profile);
         profileImage = itemView.findViewById(R.id.profileImage);
         postImage = itemView.findViewById(R.id.postimage);
         frameLayout = itemView.findViewById(R.id.adLayout);

         category = itemView.findViewById(R.id.category);
         postSquareProfile = itemView.findViewById(R.id.post_square_profile);
//posttitle = itemView.findViewById(R.id.postTitle);
         boo = itemView.findViewById(R.id.boo);
         // atText = itemView.findViewById(R.id.at_text);

         like = itemView.findViewById(R.id.like);
         comment = itemView.findViewById(R.id.comment);
         save = itemView.findViewById(R.id.save);
         more = itemView.findViewById(R.id.more);

         username = itemView.findViewById(R.id.username);
         noOfLikes = itemView.findViewById(R.id.no_of_likes);
         noOfBoos = itemView.findViewById(R.id.no_of_boos);
         author = itemView.findViewById(R.id.author);
         noOfComments = itemView.findViewById(R.id.no_of_comments);
         description = itemView.findViewById(R.id.description);
         //newItemCard = itemView.findViewById(R.id.new_post_item_card);
         timestamp = itemView.findViewById(R.id.timestamp);
         timestampdetail = itemView.findViewById(R.id.timestampdetail);



      }
   }





   private void deletPostPopup(final String postId) {
      View alertDeleteDialog = LayoutInflater.from(mContext).inflate(R.layout.custom_delete_dialog,null);
      AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
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

                  }
               }

               @Override
               public void onCancelled(@NonNull DatabaseError databaseError) {

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
            if (dataSnapshot.child(firebaseUser.getUid()).exists()) {
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
            if (dataSnapshot.child(firebaseUser.getUid()).exists()) {
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

   private void createAndSendBooNotification(String receiverUserId, String message, String senderUserId,String notifType,String postID) {
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

   private void createAndSendLikeNotification(String receiverUserId, String message, String senderUserId,String notifType,String postID) {
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
                  //Toast.makeText(mContext, goingtoDevicetoken.toString(), Toast.LENGTH_SHORT).show();

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
                        //Toast.makeText(mContext, "Device tokenfound for this user sending notif now", Toast.LENGTH_SHORT).show();

                     }
                  });
               } else {
                  // Device token not found for the user
                  Log.e("Device Token", "Device token not found for user: " + notification.getGoingToUserID());
                  //Toast.makeText(mContext, "Device token not found for this user, maybe they aren't logged in?", Toast.LENGTH_SHORT).show();
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


   //https://console.cloud.google.com/apis/api/googlecloudmessaging.googleapis.com/quotas?project=giggly-ef5fb
   //https://firebase.google.com/docs/reference/android/com/google/firebase/iid/FirebaseInstanceIdService
   //https://www.youtube.com/watch?v=6_t87WW6_Gc
   //https://stackoverflow.com/questions/24652928/flag-activity-clear-top-not-working-from-notification-section
   private void sendLikeNotificationToUser(String userId, String message, MyCustomNotification notification) {


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
                  Toast.makeText(mContext, goingtoDevicetoken.toString(), Toast.LENGTH_SHORT).show();

                  try {
                     JSONObject jsonPayload = new JSONObject();
                     jsonPayload.put("to", goingtoDevicetoken[0]);

                     JSONObject notificationData = new JSONObject();
                     notificationData.put("title", "He he he");
                     notificationData.put("body", "Someone likes your post");

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
                        //Toast.makeText(mContext, "Device tokenfound for this user sending notif now", Toast.LENGTH_SHORT).show();

                     }
                  });
               } else {
                  // Device token not found for the user
                  Log.e("Device Token", "Device token not found for user: " + notification.getGoingToUserID());
                  //Toast.makeText(mContext, "Device token not found for this user, maybe they aren't logged in?", Toast.LENGTH_SHORT).show();
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



   private void showOptionsMenu(final String postId) {
      //https://stackoverflow.com/questions/42273188/problems-with-custom-layout-for-alertdialog
      View alertDeleteDialog = LayoutInflater.from(mContext).inflate(R.layout.postoptionsdialog,null);
      AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
      builder.setView(alertDeleteDialog);

      deletepostBtn = alertDeleteDialog.findViewById(R.id.deleteBtn);
      whoisbtn = alertDeleteDialog.findViewById(R.id.whoisBtn);
      closebtn = alertDeleteDialog.findViewById(R.id.closebtn);



      deletepostBtn.setVisibility(View.VISIBLE);

      whoisbtn.setVisibility(View.GONE);






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


            Intent intent = new Intent(mContext, AboutAccountActivity.class);
            intent.putExtra("userId", postAuthorId);

            mContext.startActivity(intent);
         }
      });
   }


























}