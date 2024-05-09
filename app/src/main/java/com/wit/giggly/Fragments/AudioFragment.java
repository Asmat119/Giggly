package com.wit.giggly.Fragments;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.activity.OnBackPressedDispatcherOwner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.chibde.visualizer.CircleBarVisualizer;
import com.chibde.visualizer.LineBarVisualizer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.wit.giggly.AudioCache;
import com.wit.giggly.ImageCache;
import com.wit.giggly.MainActivity;
import com.wit.giggly.Model.User;
import com.wit.giggly.PermissionsActivity;
import com.wit.giggly.R;

import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;


public class AudioFragment extends Fragment implements OnBackPressedDispatcherOwner {

    private TextView nextButton;
    private TextView closeButton;

    public Button cancelBTN;
    public Button confirmbtn;

    private CircleImageView imageProfile;

    private FloatingActionButton recordAudioButton;
    private LineBarVisualizer visualizer;
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private boolean isButtonHeldDown = false;
    private String audioFilePath = "";
    private ImageButton playAudioBtn;
    private Uri audioFileUri;
    private FloatingActionButton uploadAudiobtn;
    private boolean isPlaying = false;
    private boolean isRecording = false;
    private boolean isPaused = false;
    private StorageReference audioStorageRef;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private static final String AUDIO_FILE_PATH_KEY = "audioFilePathKey";

    private SeekBar audioSeekBar;

    private Handler recordDelayHandler = new Handler();
    private Handler seekBarHandler = new Handler();  //https://stackoverflow.com/questions/22274790/how-to-handle-the-handler-in-seekbar
    private Animation pulsate;


    private static final int PICK_AUDIO_FILE = 1;

    private CircleBarVisualizer mCircleBarVisualizer;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_audio, container, false);
//        Toast.makeText(requireContext(), "audio file path null", Toast.LENGTH_SHORT).show();
        nextButton = view.findViewById(R.id.next_page);
        closeButton = view.findViewById(R.id.close);
        recordAudioButton = view.findViewById(R.id.recordAudioButton);
        audioSeekBar = view.findViewById(R.id.audioSeekBar);
        playAudioBtn = view.findViewById(R.id.playaudiobtn);
        pulsate = AnimationUtils.loadAnimation(requireContext(), R.anim.pulsate);
        uploadAudiobtn = view.findViewById(R.id.uploadaudiobtn);
        mCircleBarVisualizer = view.findViewById(R.id.visualizerCircleBar);
        imageProfile = view.findViewById(R.id.image_profile);


        // Set the visualizer's color
        mCircleBarVisualizer.setColor(ContextCompat.getColor(requireContext(), R.color.yellow));

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseUser user = mAuth.getCurrentUser();

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(requireContext(), R.color.darkdarkgrey));
//            getActivity().getWindow().getDecorView().setSystemUiVisibility(0);
//        }

        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                showDiscardPrompt();
            }
        });

        try {
            showAudioTutorial();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        initializeAudioRecording();


        if (savedInstanceState != null) {
            // Restore the audio file path from the cache
            audioFilePath = AudioCache.getAudioFilePath();
            if (audioFilePath != null) {
                audioFileUri = Uri.fromFile(new File(audioFilePath));
            }
        }

        if (user != null) {
            String userId = user.getUid();
            DatabaseReference userRef = mDatabase.child("Users").child(userId);

            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                        User user = dataSnapshot.getValue(User.class);

                        String imageUrl = user.getImageurl(); // Get the image URL from Firebase

                        if(user.getImageurl().equals("default")){
                            imageProfile.setImageResource(R.drawable.no_profile_image);
                        }else {
                            Picasso.get().load(user.getImageurl()).into(imageProfile);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if audioFileUri is null or empty
                if (AudioCache.getAudioFilePath() == null) {
                    // Display a message to the user
                   // Toast.makeText(requireContext(), "You cant proceed without recording an audio", Toast.LENGTH_SHORT).show();
                    try {
                        showAudioTutorial();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                } else {


                    FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, new AddImageFragment());
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDiscardPrompt();
            }
        });

        uploadAudiobtn.setOnClickListener(v -> {
            Intent filePickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
            filePickerIntent.setType("audio/mpeg"); // Filter for MP3 files
            startActivityForResult(Intent.createChooser(filePickerIntent, "Select an MP3 file"), PICK_AUDIO_FILE);
        });




        // Inflate the layout for this fragment
        return view;
    }

    private void initializeAudioRecording() {
        recordAudioButton.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                isButtonHeldDown = true;
                recordDelayHandler.postDelayed(() -> {
                    if (isButtonHeldDown) {
                        startRecording();
                        recordAudioButton.startAnimation(pulsate);
                    }
                }, 100);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                isButtonHeldDown = false;
                recordDelayHandler.removeCallbacksAndMessages(null);
                stopRecording();
                recordAudioButton.clearAnimation();
            }
            return true;
        });

        playAudioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AudioCache.getAudioFilePath() != null) {
                    if (mediaPlayer == null) {
                        initializeMediaPlayer();
                    }
                    if (isPlaying) {
                        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                            mediaPlayer.pause();
                            playAudioBtn.setImageResource(R.drawable.triangle); // Set to play icon
                        }
                    } else {
                        if (mediaPlayer != null) {
                            mediaPlayer.start();
                            setPlayerToVisualizer();
                            playAudioBtn.setImageResource(R.drawable.pause_icon); // Set to pause icon
                            updateSeekBar();
                        }
                    }
                    isPlaying = !isPlaying;
                } else {
                    showNoAudioPrompt();
                }
            }
        });

        audioSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer == null && fromUser) {
                    showNoAudioPrompt();
                    seekBar.setProgress(0);
                } else {
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        mediaPlayer.seekTo(progress);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                    }
                } else {
                    showNoAudioPrompt();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null) {
                    mediaPlayer.start();
                    updateSeekBar();
                }
            }
        });
    }

    private void initializeMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(AudioCache.getAudioFilePath());
            mediaPlayer.prepare();
            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying = false;
                playAudioBtn.setImageResource(R.drawable.triangle); // Set to play icon
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void setPlayerToVisualizer() {
        if (mediaPlayer != null) {
            // https://www.studytonight.com/post/implement-audio-visualizer-in-android-app
            mCircleBarVisualizer.setVisibility(View.VISIBLE);
            mCircleBarVisualizer.setPlayer(mediaPlayer.getAudioSessionId());
        }
    }

    private void playAudio() {
        if (audioFilePath != null) {
            String cachedAudioFilePath = AudioCache.getAudioFilePath();

            Log.e("audio file name playing", cachedAudioFilePath);
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(cachedAudioFilePath);
                    mediaPlayer.prepare();
                    mediaPlayer.setOnCompletionListener(mp -> {
                        isPlaying = false;
                        isPaused = false;
                        playAudioBtn.setImageResource(R.drawable.triangle); // Set to play icon
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (mediaPlayer != null) {
                if (!isPlaying) {
                    mediaPlayer.start();
                    setPlayerToVisualizer();
                    Toast.makeText(requireContext(), "Playing audio", Toast.LENGTH_SHORT).show();
                    isPlaying = true;
                    isPaused = false;
                    audioSeekBar.setMax(mediaPlayer.getDuration());
                    updateSeekBar();
                    playAudioBtn.setImageResource(R.drawable.pause_icon); // Set to pause icon
                } else {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        isPlaying = false;
                        isPaused = true;
                        playAudioBtn.setImageResource(R.drawable.triangle); // Set to play icon
                    } else {
                        mediaPlayer.start();
                        setPlayerToVisualizer();
                        isPlaying = true;
                        isPaused = false;
                        playAudioBtn.setImageResource(R.drawable.pause_icon); // Set to pause icon
                        updateSeekBar();
                    }
                }
            }
        } else {
            Toast.makeText(requireContext(), "No audio recorded", Toast.LENGTH_SHORT).show();
        }
    }


    private void showDiscardPrompt() {
        //https://stackoverflow.com/questions/42273188/problems-with-custom-layout-for-alertdialog
        View alertDeleteDialog = LayoutInflater.from(requireContext()).inflate(R.layout.discard_post_dialog,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(alertDeleteDialog);

        confirmbtn = alertDeleteDialog.findViewById(R.id.acceptBtn);
        cancelBTN = alertDeleteDialog.findViewById(R.id.cancelBtn);

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
                ImageCache.setImageUri(null);
                AudioCache.clearAudioCache();
                Intent intent = new Intent(requireActivity(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                requireActivity().finish();

            }
        });
    }

    private void showNoAudioPrompt() {
        //https://stackoverflow.com/questions/42273188/problems-with-custom-layout-for-alertdialog
        View alertDeleteDialog = LayoutInflater.from(requireContext()).inflate(R.layout.no_audio_popup,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(alertDeleteDialog);

        confirmbtn = alertDeleteDialog.findViewById(R.id.acceptBtn);
        cancelBTN = alertDeleteDialog.findViewById(R.id.cancelBtn);

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
                dialog.cancel();

            }
        });
    }





    private void uploadAudioToFirebase() {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null && audioFileUri != null) {
            String fileName = "audio_" + System.currentTimeMillis() + ".mp3";
            audioStorageRef = FirebaseStorage.getInstance().getReference().child("Audio").child(fileName);

            audioStorageRef.putFile(audioFileUri)
                    .addOnSuccessListener(taskSnapshot -> {

                        Toast.makeText(requireContext(), "Audio uploaded successfully", Toast.LENGTH_SHORT).show();

                        // Save the download URL to use it later in AddPostDetailsFragment
                        audioStorageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String downloadUrl = uri.toString();


                        });
                    })
                    .addOnFailureListener(e -> {
                        // Audio upload failed
                        Toast.makeText(requireContext(), "Failed to upload audio", Toast.LENGTH_SHORT).show();
                    });
        }
    }



    private void stopRecording() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
                setPlayerToVisualizer();
                Toast.makeText(requireContext(), "Recording stopped", Toast.LENGTH_SHORT).show();
                audioFileUri = null;

                // Save the audio file path to the cache
                AudioCache.setAudioFilePath(audioFilePath);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateSeekBar() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            int duration = mediaPlayer.getDuration();
            int currentPosition = mediaPlayer.getCurrentPosition();
            audioSeekBar.setMax(duration);
            audioSeekBar.setProgress(currentPosition);

            if (currentPosition < duration) {
                //https://stackoverflow.com/questions/22274790/how-to-handle-the-handler-in-seekbar
                seekBarHandler.postDelayed(this::updateSeekBar, 100);
            }
        }
    }



    private void startRecording() {
        if (mediaRecorder == null) {
            mediaRecorder = new MediaRecorder();
            audioFilePath = " ";
            audioFilePath = requireContext().getExternalCacheDir().getAbsolutePath() + File.separator + "audio.mp3";

            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); // Use MPEG_4 for MP3
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC); // Use AAC for MP3
            mediaRecorder.setOutputFile(audioFilePath);

            try {
                mediaRecorder.prepare();
                mediaRecorder.start();
                setPlayerToVisualizer();
                Toast.makeText(requireContext(), "Recording started", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                // Make sure to stop recording in case of an exception
                stopRecording();
            }

            audioFileUri = Uri.fromFile(new File(audioFilePath));
        }
    }


    private void showAudioTutorial() throws IOException {
        //https://stackoverflow.com/questions/42273188/problems-with-custom-layout-for-alertdialog
        View alertDeleteDialog = LayoutInflater.from(requireContext()).inflate(R.layout.audiotutorial,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(alertDeleteDialog);

        confirmbtn = alertDeleteDialog.findViewById(R.id.acceptBtn);
        cancelBTN = alertDeleteDialog.findViewById(R.id.cancelBtn);


        // how to use gif in slasph screen tutorial by https://www.youtube.com/watch?v=IpNLx75b0hE
        ImageDecoder.Source source = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            source = ImageDecoder.createSource(
                    getResources(), R.drawable.tutorialgigglyaudio
            );
        }
        Drawable drawable = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            drawable = ImageDecoder.decodeDrawable(source);
        }

        ImageView gifView = alertDeleteDialog.findViewById(R.id.gifaudiotutorial);
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




    private void showPermissionsConfirmationDialog() {
        //https://stackoverflow.com/questions/42273188/problems-with-custom-layout-for-alertdialog
        View alertDeleteDialog = LayoutInflater.from(requireContext()).inflate(R.layout.nopermissions_dialog,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(alertDeleteDialog);

        confirmbtn = alertDeleteDialog.findViewById(R.id.acceptBtn);
        cancelBTN = alertDeleteDialog.findViewById(R.id.cancelBtn);

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
                startActivity(new Intent(requireContext(), MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));

            }
        });

        confirmbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(requireContext(), PermissionsActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
dialog.dismiss();
                //overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(AUDIO_FILE_PATH_KEY, audioFilePath);
    }

    @Override
    public OnBackPressedDispatcher getOnBackPressedDispatcher() {
       // https://www.droidcon.com/2023/02/22/handling-back-press-in-android-13-the-correct-way/
        return requireActivity().getOnBackPressedDispatcher();
    }



}