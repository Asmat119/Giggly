package com.wit.giggly;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.OnCompleteListener;
import com.google.android.play.core.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wit.giggly.Fragments.ExploreFragment;
import com.wit.giggly.Fragments.HomeFragment;
import com.wit.giggly.Fragments.ProfileFragment;
import com.wit.giggly.Fragments.SearchFragment;
import com.wit.giggly.Model.MyCustomNotification;





import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collections;
import java.util.Stack;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {

    private BottomNavigationView mBottomNavigationView;
    private Fragment selectedFragment;
    private static final int NAV_HOME = R.id.nav_home;
    private static final int REQUEST_NOTIFICATION_PERMISSION = 100;
    private static final int NAV_ADD = R.id.nav_add;
    private static final int NAV_PROFILE = R.id.nav_profile;
    private static final int NAV_SEARCH = R.id.nav_search;
    private static final int NAV_EXPLORE = R.id.nav_explore;


    private static final String FCM_API = "https://fcm.googleapis.com/fcm/send";
    private static final String SERVER_KEY = "AAAAz-E70BY:APA91bG4JmyzoErCHnztd7d9Gg6pqQ2lAfn0xaX2pxX_UnbRGFjNE0LOwJyKhbuvzCu6crwXYv1zjBdL_XhdARiPdxbE5XC2sLOZ1qMIsDy69WOW1m0UDqt3h3QfC91q4IiYck2DBZpP";
    private AppUpdateManager appUpdateManager;
    private static final int MY_REQUEST_CODE = 123;




    private Stack<Fragment> fragmentStack = new Stack<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);





        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();


        Fragment fragment = new HomeFragment(); // Replace with your actual fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();


        appUpdateManager = AppUpdateManagerFactory.create(this);
        //checkForAppUpdate();

// Add the fragment to the stack
        fragmentStack.push(fragment);
        Intent intent = getIntent();




        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TypedValue typedValue = new TypedValue();
            getTheme().resolveAttribute(R.attr.optionPageBackground2, typedValue, true);

            boolean isDarkTheme = ThemeSaver.getThemePreference(this);

            if(isDarkTheme){
                View decor = getWindow().getDecorView();
                decor.setSystemUiVisibility(0);
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.darkdarkgrey));
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

            }else{
                View decor = getWindow().getDecorView();
                decor.setSystemUiVisibility(0);
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.yellow));
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }

        }



        if (currentUser == null) {
            // User is not signed in
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish(); // This prevents the user from navigating back to the MainActivity
        }else if(!currentUser.isEmailVerified()) {
            //if user is created but not verified
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            Toast.makeText(MainActivity.this, "Please enter your details again", Toast.LENGTH_SHORT).show();

            finish();
        }else {




            mBottomNavigationView = findViewById(R.id.bottom_nav);

            mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                    int itemId = menuItem.getItemId();

                    if (itemId == NAV_HOME && !(currentFragment instanceof HomeFragment)) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new HomeFragment())
                                .commit();
                    } else if (itemId == NAV_ADD) {
                        // Handle NAV_ADD as per your requirements
                        startActivity(new Intent(MainActivity.this, PostActivity.class));
                    } else if (itemId == NAV_PROFILE && !(currentFragment instanceof ProfileFragment)) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new ProfileFragment())
                                .commit();
                    } else if (itemId == NAV_SEARCH && !(currentFragment instanceof SearchFragment)) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new SearchFragment())
                                .commit();
                    } else if (itemId == NAV_EXPLORE && !(currentFragment instanceof ExploreFragment)) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new ExploreFragment())
                                .commit();
                    }

                    return true;
                }


        });
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new HomeFragment()).commit();

        }
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

            }
        });





        }



    @Override
    public void onBackPressed() {
        if (fragmentStack.size() > 1) {
            // Remove the current fragment from the stack
            fragmentStack.pop();

            // Get the previous fragment
            Fragment fragment = fragmentStack.peek();


            // Remove the current fragment from the layout
            getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().findFragmentById(R.id.fragment_container)).commit();

            // Replace with the previous fragment
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
        } else {
            new AlertDialog.Builder(this)
                    .setMessage("Are you sure you want to exit the app?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();

        }
    }






    public void recreateProfileFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, new ProfileFragment());
        fragmentTransaction.commit();
    }


    private void checkForAppUpdate() {
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        appUpdateInfoTask.addOnCompleteListener(new OnCompleteListener<AppUpdateInfo>() {
            @Override
            public void onComplete(@NonNull Task<AppUpdateInfo> task) {
                if (task.isSuccessful()) {
                    AppUpdateInfo appUpdateInfo = task.getResult();
                    if (appUpdateInfo != null && appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                        // Prompt the user to update the app
                        try {
                            appUpdateManager.startUpdateFlowForResult(
                                    appUpdateInfo,
                                    AppUpdateType.IMMEDIATE,
                                    MainActivity.this,
                                    MY_REQUEST_CODE);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    Log.e("AppUpdate", "Error checking for app update", task.getException());
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                Log.e("AppUpdate", "Update flow failed with result code: " + resultCode);
            }
        }
    }









}
