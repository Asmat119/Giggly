package com.wit.giggly.Adapter;

import static com.wit.giggly.StartActivity.CHANNEL_ID;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
import com.wit.giggly.Fragments.ProfileFragment;
import com.wit.giggly.MainActivity;
import com.wit.giggly.Model.MyCustomNotification;
import com.wit.giggly.Model.User;
import com.wit.giggly.NotificationsActivity;
import com.wit.giggly.ProfileActivity;
import com.wit.giggly.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder>{

   private Context mContext;
   private List<User> mUsers;
   private boolean isFargment;
   public TextView username;
   public TextView fullname;

   private FirebaseUser firebaseUser;
   private OnItemClickListener mListener;

   public interface OnItemClickListener {
      void onItemClick(int position);
   }

   public void setOnItemClickListener(OnItemClickListener listener) {
      mListener = listener;
   }
   public UserAdapter(Context mContext, List<User> mUsers, boolean isFargment) {
      this.mContext = mContext;
      this.mUsers = mUsers;
      this.isFargment = isFargment;

   }

   @NonNull
   @Override
   public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(mContext).inflate(R.layout.user_item , parent , false);
      return new ViewHolder(view);
   }

   @Override
   public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

      int btntextColor = ContextCompat.getColor(mContext, R.color.black);

      firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

      final User user = mUsers.get(position);
      holder.btnFollow.setVisibility(View.INVISIBLE);
      holder.btnFollow.setTextColor(btntextColor);

      if (holder.username == null) {
         Log.e("UserAdapter", "Username TextView is null");
      }else{
         holder.username.setText(user.getUsername());
      }
      if (holder.fullname == null) {
         Log.e("UserAdapter", "Fullname TextView is null");
      }else{
         holder.fullname.setText(user.getFirstname()+" "+user.getSecondname());
      }



      if(user.getImageurl().equals("default")){
         holder.imageProfile.setImageResource(R.drawable.no_profile_image);
      }else {
         Picasso.get().load(user.getImageurl()).placeholder(R.drawable.no_profile_image).into(holder.imageProfile);
      }
      isFollowed(user.getId() , holder.btnFollow);
      isVerfied(user.getId(),holder.verifiedBadge);

      if (user.getId().equals(firebaseUser.getUid())){
         holder.btnFollow.setVisibility(View.GONE);
      }

      holder.btnFollow.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            if (holder.btnFollow.getText().toString().equals(("follow"))){
               FirebaseDatabase.getInstance().getReference().child("Follow").
                       child((firebaseUser.getUid())).child("following").child(user.getId()).setValue(true);

               FirebaseDatabase.getInstance().getReference().child("Follow").
                       child(user.getId()).child("followers").child(firebaseUser.getUid()).setValue(true);


            } else {
               FirebaseDatabase.getInstance().getReference().child("Follow").
                       child((firebaseUser.getUid())).child("following").child(user.getId()).removeValue();

               FirebaseDatabase.getInstance().getReference().child("Follow").
                       child(user.getId()).child("followers").child(firebaseUser.getUid()).removeValue();
            }
         }
      });

      holder.itemView.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            if (isFargment) {
               User user = mUsers.get(position);
               mContext.getSharedPreferences("PROFILE", Context.MODE_PRIVATE).edit().putString("profileId", user.getId()).apply();

               mContext.startActivity(new Intent(mContext, ProfileActivity.class));
            } else {
               User user = mUsers.get(position);
               mContext.getSharedPreferences("PROFILE", Context.MODE_PRIVATE).edit().putString("profileId", user.getId()).apply();

               mContext.startActivity(new Intent(mContext, ProfileActivity.class));

            }
            if (mListener != null) {
               mListener.onItemClick(position);
            }
         }
      });

   }

   private void isFollowed(final String id, final Button btnFollow) {

      DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
              .child("following");
      reference.addValueEventListener(new ValueEventListener() {
         @Override
         public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.child(id).exists())
               btnFollow.setText("following");
            else
               btnFollow.setText("follow");
         }

         @Override
         public void onCancelled(@NonNull DatabaseError databaseError) {

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




   @Override
   public int getItemCount() {
      return mUsers.size();
   }

   public class ViewHolder extends RecyclerView.ViewHolder{

      public CircleImageView imageProfile;
      public TextView username;
      public TextView fullname;
      public Button btnFollow;
      public ImageView verifiedBadge;

      public ViewHolder(@NonNull View itemView) {
         super(itemView);


         imageProfile = itemView.findViewById(R.id.image_profile);
         username = itemView.findViewById(R.id.username);
         fullname = itemView.findViewById(R.id.fullname);
         btnFollow = itemView.findViewById(R.id.btn_follow);
         verifiedBadge = itemView.findViewById(R.id.verifiedbadge);

      }




   }



}