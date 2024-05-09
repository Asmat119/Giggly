package com.wit.giggly.Fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;


import com.wit.giggly.ImageCache;
import com.wit.giggly.MainActivity;
import com.wit.giggly.OptionsActivity;
import com.wit.giggly.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AddImageFragment extends Fragment {


    private static final int REQUEST_CODE_TAKE_PHOTO = 2;
    private ImageView selectedImage;
    private static final int REQUEST_CODE_SELECT_IMAGE = 1;
    private static final String SAVED_IMAGE_URI = "saved_image_uri";
    private Uri savedImageUri;
    private String uploadedImageFilename;
    private TextView nextButton;
    private Button confirmbtn;
    private TextView backbtn;
    private ImageView tutorialImage;
    public Button cancelBTN;
    public Button logoutBtn;
    private Button galleryBtn;
    private String imageFilename;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_image, container, false);

        nextButton = view.findViewById(R.id.next_page1);
        backbtn = view.findViewById(R.id.back);
        selectedImage = view.findViewById(R.id.selectedImage);
        galleryBtn = view.findViewById(R.id.choosePic);
        tutorialImage = view.findViewById(R.id.tutorialimage);

        try {
            showImageTutorial();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(requireContext(), R.color.darkdarkgrey));
            getActivity().getWindow().getDecorView().setSystemUiVisibility(0);
        }
//        Bundle bundle = getArguments();
//        if (bundle != null) {
//            imageFilename = bundle.getString("imageFilename");
//
//        }

        Uri imageUri = ImageCache.getImageUri();
        if (imageUri != null) {

            displaySelectedImage(imageUri);

        } else {

//            showNoImageDialog();
        }
//        if (savedInstanceState != null) {
//            savedImageUri = savedInstanceState.getParcelable(SAVED_IMAGE_URI);
//            if (savedImageUri != null) {
//                displaySelectedImage(savedImageUri);
//            }
//        } else if (savedImageUri == null) {
//            // Only start cropping if there's no savedImageUri
//            showNoImageDialog();
//        }







        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Store the selected image URI in the cache and proceed to AddPostDetailsFragment
                Uri imageUri = ImageCache.getImageUri();
                if (imageUri != null) {
                    FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                    AddPostDetailsFragment addPostDetailsFragment = new AddPostDetailsFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("imageUri", imageUri.toString());
                    addPostDetailsFragment.setArguments(bundle);
                    transaction.replace(R.id.fragment_container, addPostDetailsFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                } else {
                    // Handle case where no image is selected
                    showNoImageDialog();
                }
            }
        });

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                AudioFragment audioFragment = new AudioFragment();
                transaction.replace(R.id.fragment_container, audioFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
//                        .setAspectRatio(1, 1) // Set crop ratio
                        .start(requireContext(), AddImageFragment.this); // Start the activity with the fragment context
            }
        });


        return view;
    }

    private void startCropImageActivity() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                // .setAspectRatio(1, 1) // Set crop ratio
                .start(requireContext(), this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                Uri selectedImageUri = result.getUri();
                displaySelectedImage(selectedImageUri);

                // Store the selected image URI in the cache
                ImageCache.setImageUri(selectedImageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(requireActivity(), "Error cropping image: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void displaySelectedImage(Uri imageUri) {
        selectedImage.setImageURI(imageUri);
        selectedImage.setVisibility(View.VISIBLE);
        tutorialImage.setVisibility(View.GONE);
        savedImageUri = imageUri; // Save the image URI
        String imageUrl = imageUri.toString(); // Convert Uri to string
        Log.d("SelectedImage", "Image URI: " + imageUrl);
    }


    private void showNoImageDialog() {
        View alertDeleteDialog = LayoutInflater.from(requireContext()).inflate(R.layout.no_image_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(alertDeleteDialog);

        logoutBtn = alertDeleteDialog.findViewById(R.id.acceptBtn);
        cancelBTN = alertDeleteDialog.findViewById(R.id.cancelBtn);

        final AlertDialog dialog = builder.create();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        cancelBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
                // If the user cancels, make the tutorialImage visible
                tutorialImage.setVisibility(View.VISIBLE);
                selectedImage.setVisibility(View.GONE);
            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }



    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (savedImageUri != null) {
            outState.putParcelable(SAVED_IMAGE_URI, savedImageUri);
        }
        super.onSaveInstanceState(outState);
    }


    private void showImageTutorial() throws IOException {



        //https://stackoverflow.com/questions/42273188/problems-with-custom-layout-for-alertdialog
        View alertDeleteDialog = LayoutInflater.from(requireContext()).inflate(R.layout.imagetutorial,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(alertDeleteDialog);

        confirmbtn = alertDeleteDialog.findViewById(R.id.acceptBtn);
        cancelBTN = alertDeleteDialog.findViewById(R.id.cancelBtn);


        // how to use gif in slasph screen tutorial by https://www.youtube.com/watch?v=IpNLx75b0hE
        ImageDecoder.Source source = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            source = ImageDecoder.createSource(
                    getResources(), R.drawable.tutorialgigglyimage
            );
        }
        Drawable drawable = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            drawable = ImageDecoder.decodeDrawable(source);
        }

        ImageView gifView = alertDeleteDialog.findViewById(R.id.gifimagetutorial);
        gifView.setImageDrawable(drawable);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (drawable instanceof AnimatedImageDrawable) {
                ((AnimatedImageDrawable) drawable).start();
            }
        }

        final AlertDialog dialog = builder.create();

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
                dialog.dismiss();
                //overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }
}
