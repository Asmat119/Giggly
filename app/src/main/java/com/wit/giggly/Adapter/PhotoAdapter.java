package com.wit.giggly.Adapter;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wit.giggly.Model.Post;
import com.wit.giggly.PostDetailActivity;
import com.wit.giggly.R;

import java.util.List;


public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {

   private Context mContext;
   private List<Post> mPosts;
   public ImageButton cancelBTN;
   public Button deletePost;



   public PhotoAdapter(Context mContext, List<Post> mPosts) {
      this.mContext = mContext;
      this.mPosts = mPosts;
   }

   @NonNull
   @Override
   public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(mContext).inflate(R.layout.photo_item, parent, false);
      return  new ViewHolder(view);
   }

   @Override
   public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

      final Post post = mPosts.get(position);
      //Picasso.get().load(post.getImageURL()).placeholder(R.mipmap.ic_launcher).into(holder.postImage);
      //this is the line i change to have words apear instead of the photos
      String title = post.getTitle().trim();
      String description = post.getDescription().trim();
      String firstWord = description.split(" ")[0];
      holder.postSquareProfile.setText(title); // set text instead of image


      holder.postSquareProfile.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit().putString("postid", post.getPostId()).apply();

//            ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.fragment_container, new PostDetailFragment()).commit();
            Intent intent = new Intent(mContext, PostDetailActivity.class);
            intent.putExtra("postid", post.getPostId());
            intent.putExtra("postAuthorId", post.getPublisher());
            ((FragmentActivity) mContext).startActivity(intent);
         }
      });






   }







   @Override
   public int getItemCount() {
      return mPosts.size();
   }

   public class ViewHolder extends RecyclerView.ViewHolder {

     // public ImageView postImage;
      public TextView postSquareProfile;


      public ViewHolder(@NonNull View itemView) {
         super(itemView);

         postSquareProfile = itemView.findViewById(R.id.post_square_profile);
        // postImage = itemView.findViewById(R.id.post_image);
      }
   }

}
