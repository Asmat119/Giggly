package com.wit.giggly.Fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.wit.giggly.MainActivity;
import com.wit.giggly.PermissionsActivity;
import com.wit.giggly.R;


public class CreatePostWaitingRoom extends Fragment {



    public Button cancelBTN;
    public Button confirmbtn;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_post_waiting_room, container, false);
    }



    private void checkPermissionsAndShowDialogIfNeeded() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // Request the RECORD_AUDIO permission
            showPermissionsConfirmationDialog();
        } else {
            // Permission already granted, proceed to AudioFragment
            navigateToAudioFragment();
        }
    }

    private void navigateToAudioFragment() {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new AudioFragment());
        transaction.addToBackStack(null);
        transaction.commit();
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
    public void onResume() {
        super.onResume();
        checkPermissionsAndShowDialogIfNeeded();
    }

}