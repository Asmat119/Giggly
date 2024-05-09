package com.wit.giggly.Fragments;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.wit.giggly.AdHelper;
import com.wit.giggly.AdViewActivity;
import com.wit.giggly.AudioCache;
import com.wit.giggly.ImageCache;
import com.wit.giggly.MainActivity;
import com.wit.giggly.PostActivity;
import com.wit.giggly.R;
import com.wit.giggly.emailVerificationActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;




public class AddPostDetailsFragment extends Fragment {

    private static final int REQUEST_READ_EXTERNAL_STORAGE = 1;
    private MaterialButton postbutton;

    private TextView backbutton;

    private boolean isAdVisible = false;

    private View view;
    private EditText postTitle;
    private EditText postDesc;
    private AppCompatSpinner categorySpinner;
    private ArrayAdapter<String> categoryAdapter;

    private String imageFilename;
    private String uploadedImageFilename;
    private String uploadedAudioFilename;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
         view = inflater.inflate(R.layout.fragment_add_post_details, container, false);

        postbutton = view.findViewById(R.id.post);

        backbutton = view.findViewById(R.id.back);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        postTitle = view.findViewById(R.id.title);
        postDesc = view.findViewById(R.id.desc);
        categorySpinner = view.findViewById(R.id.category);
        categoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, getCategoryOptions());
        categorySpinner.setAdapter(categoryAdapter);

        MobileAds.initialize(requireContext(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });


//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(requireContext(), R.color.darkdarkgrey));
//            getActivity().getWindow().getDecorView().setSystemUiVisibility(0);
//        }

        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, new AddImageFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        postbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAdVisible = true;
               createPost();

            }
        });

        return view;
    }








    private void createPost() {

        View alertloadingDialog = LayoutInflater.from(requireContext()).inflate(R.layout.custom_progress_bar,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(alertloadingDialog);
        final AlertDialog pd = builder.create();
        pd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        pd.setCanceledOnTouchOutside(false);
        pd.show();

        Uri selectedImageUri = ImageCache.getImageUri();
        String selectedAudioPath = AudioCache.getAudioFilePath();
        Log.d(TAG, "Selected Audio Path: " + selectedAudioPath);
        if (selectedImageUri == null) {
            Log.e(TAG, "Image URI is null");
            pd.dismiss();
            return;
        }
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            pd.dismiss();
            return;
        }

        String description = postDesc.getText().toString().trim();
        String title = postTitle.getText().toString().trim();
        String category = categorySpinner.getSelectedItem().toString().trim();
        String[] words = description.split("\\s+");
        if (words.length > 0 && !containsOnlyLetters(words[0])) {

            String randomPhrase = getRandomPhrase();
            description = randomPhrase + description.substring(words[0].length());
        }

        if (description.isEmpty()) {
            Toast.makeText(requireContext(), "Please don't leave the description blank", Toast.LENGTH_SHORT).show();
            pd.dismiss();
            return;
        }

        if (title.isEmpty() || title.length() > 20) {
            Toast.makeText(requireContext(), "Please enter a valid title (1-20 characters)", Toast.LENGTH_SHORT).show();
            pd.dismiss();
            return;
        }


        if (!title.matches("\\b\\w+\\b")) {
            Toast.makeText(requireContext(), "Title should be only one word", Toast.LENGTH_SHORT).show();
            pd.dismiss();
            return;
        }


        title = title.substring(0, 1).toUpperCase() + title.substring(1);

        if (description.length() > 400) {
            Toast.makeText(requireContext(), "Description should not exceed 400 characters", Toast.LENGTH_SHORT).show();
            pd.dismiss();
            return;
        }
        description = description.substring(0, 1).toUpperCase() + description.substring(1);
        String userId = user.getUid();
        int points = 0;
        String imageFilePath = selectedImageUri.toString();


        //Toast.makeText(requireContext(), imageFilePath, Toast.LENGTH_SHORT).show();

        String finalDescription = description;
        String finalTitle = title;

        uploadAudioToFirebase(selectedAudioPath, new OnAudioUploadListener() {

            @Override
            public void onUploadSuccess(String audioUrl) {
                // Audio uploaded successfully, proceed to create post

                uploadImageToFirebase(selectedImageUri, new OnImageUploadListener() {
                    @Override
                    public void onUploadSuccess(String imageUrl) {
                        // Image uploaded successfully, proceed to create post
                        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
                        String postId = postsRef.push().getKey();

                        HashMap<String, Object> postMap = new HashMap<>();
                        postMap.put("postId", postId);
                        postMap.put("description", finalDescription);
                        postMap.put("image", imageUrl);
                        postMap.put("audio", audioUrl); // Include the audio URL
                        postMap.put("timestamp", ServerValue.TIMESTAMP);
                        postMap.put("publisher", userId);
                        postMap.put("title", finalTitle);
                        postMap.put("category", category);
                        postMap.put("points", points);

                        postsRef.child(postId).setValue(postMap)
                                .addOnSuccessListener(aVoid -> {
                                   // Toast.makeText(requireContext(), "Post created successfully", Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(requireContext(), AdViewActivity.class);

                                    ImageCache.setImageUri(null);



                                   // loadAndDisplayNativeAd();


                                        pd.dismiss();
                                    startActivity(new Intent(requireContext(), AdViewActivity.class));



                                })
                                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to create post", Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onUploadFailure() {
                        pd.dismiss();
                        // Image upload failed, handle accordingly
                        Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onUploadFailure() {
                pd.dismiss();
                // Audio upload failed, handle accordingly
                Toast.makeText(requireContext(), "Failed to upload audio", Toast.LENGTH_SHORT).show();
            }
        });

    }



        private void uploadImageToFirebase(Uri selectedImageUri, OnImageUploadListener listener) {
        uploadedImageFilename = "image_" + System.currentTimeMillis() + ".jpg";
        StorageReference imageRef = FirebaseStorage.getInstance().getReference().child("PostImages").child(uploadedImageFilename);

        imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Image upload successful
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Get the download URL
                        String imageUrl = uri.toString();
                        //Toast.makeText(requireActivity(), "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                        listener.onUploadSuccess(imageUrl);
                    }).addOnFailureListener(e -> {
                        // Failed to get download URL
                        Toast.makeText(requireActivity(), "Failed to get image URL", Toast.LENGTH_SHORT).show();
                        listener.onUploadFailure();
                    });
                })
                .addOnFailureListener(e -> {
                    // Image upload failed
                    Toast.makeText(requireActivity(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                    listener.onUploadFailure();
                });
    }

    private void uploadAudioToFirebase(String selectedAudioPath, OnAudioUploadListener listener) {
        if (selectedAudioPath == null) {
            // Handle the case where selectedAudioPath is null
            Toast.makeText(requireActivity(), "Selected audio path is null", Toast.LENGTH_SHORT).show();
            listener.onUploadFailure();
            return;
        }

        uploadedAudioFilename = "audio_" + System.currentTimeMillis() + ".mp3";
        StorageReference audioRef = FirebaseStorage.getInstance().getReference().child("Audio").child(uploadedAudioFilename);

        try {
            byte[] audioData = readFileToBytes(selectedAudioPath);

            if (audioData != null) {
                audioRef.putBytes(audioData, new StorageMetadata.Builder()
                                .setContentType("audio/mpeg")
                                .build())
                        .addOnSuccessListener(taskSnapshot -> {
                            // Audio upload successful
                            audioRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                // Get the download URL
                                String audioUrl = uri.toString();
                                //Toast.makeText(requireActivity(), "Audio uploaded successfully", Toast.LENGTH_SHORT).show();
                                listener.onUploadSuccess(audioUrl);
                            }).addOnFailureListener(e -> {
                                // Failed to get download URL
                                Toast.makeText(requireActivity(), "Failed to get Audio URL", Toast.LENGTH_SHORT).show();
                                listener.onUploadFailure();
                            });
                        })
                        .addOnFailureListener(e -> {
                            // Audio upload failed
                            Toast.makeText(requireActivity(), "Failed to upload Audio", Toast.LENGTH_SHORT).show();
                            listener.onUploadFailure();
                        });
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireActivity(), "Error reading audio file", Toast.LENGTH_SHORT).show();
            listener.onUploadFailure();
        }
    }

    private byte[] readFileToBytes(String filePath) throws IOException {
        FileInputStream inputStream = new FileInputStream(filePath);
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            byteOutput.write(buffer, 0, length);
        }
        return byteOutput.toByteArray();
    }



    //i used chatgpt to speed this up as inputting each category would be a pain
    //https://en.wikipedia.org/wiki/Index_of_joke_types this is the list of joke types
    private List<String> getCategoryOptions() {
        return Arrays.asList(
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

    interface OnImageUploadListener {
        void onUploadSuccess(String imageUrl);
        void onUploadFailure();
    }

    interface OnAudioUploadListener {
        void onUploadSuccess(String audiofilepath);
        void onUploadFailure();
    }

    private boolean containsOnlyLetters(String text) {
        String regex = "^[a-zA-Z]+$";
        return text.matches(regex);
    }


    private String getRandomPhrase() {
        String[] phrases = {"Im not safe around kids", "I just shat my pants", "Was it me or my dog who died", "Who?? asked", "Where are my SPUDSSS"};
        int randomIndex = (int) (Math.random() * phrases.length);
        return phrases[randomIndex];
    }











}
