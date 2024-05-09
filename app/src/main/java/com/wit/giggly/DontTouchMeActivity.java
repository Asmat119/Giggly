package com.wit.giggly;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class DontTouchMeActivity extends AppCompatActivity {

    private static final int DELAY_TIME = 8000;
    private static final int CAMERA_PERMISSION_REQUEST = 1001;
    private static final int CAMERA_REQUEST_CODE = 1002;
    private AudioManager audioManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dont_touch_me);

        playBombSound();


        WindowManager.LayoutParams layout = getWindow().getAttributes();
        layout.screenBrightness = 1F;
        getWindow().setAttributes(layout);
openCamera();
        onBackPressedDisabled = true;


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                onBackPressedDisabled = false;
            }
        }, DELAY_TIME);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(DontTouchMeActivity.this, OptionsActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
            }
        }, DELAY_TIME);
    }

    private void playBombSound() {

increaseVolume();

        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.moan);

        mediaPlayer.setLooping(false);
        mediaPlayer.setVolume(1.0f, 1.0f); // Set the volume to max in mediaplayer
        mediaPlayer.start();

    }

    private void increaseVolume(){


        // https://www.geeksforgeeks.org/how-to-adjust-the-volume-of-android-phone-programmatically-from-the-app/
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        //https://stackoverflow.com/questions/2539264/volume-control-in-android-application
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);
        //increases OS volume to the max
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, open camera
                openCamera();
            } else {
                // Permission denied, inform the user
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            // Process the captured image
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
            // Add your logic to handle the image (e.g., save or display)
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Check if the device has a front-facing camera
        if (hasFrontFacingCamera()) {
            // Use the front-facing camera
            intent.putExtra("android.intent.extras.CAMERA_FACING", Camera.CameraInfo.CAMERA_FACING_FRONT);
        }

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, CAMERA_REQUEST_CODE);
        } else {
            Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean hasFrontFacingCamera() {
        int numCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < numCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                return true;
            }
        }
        return false;
    }


    private boolean onBackPressedDisabled = true;


    //prevents user from exiting until ear rape is finished
    @Override
    public void onBackPressed() {
        if (onBackPressedDisabled) {

        } else {
            super.onBackPressed();
        }
    }
}
