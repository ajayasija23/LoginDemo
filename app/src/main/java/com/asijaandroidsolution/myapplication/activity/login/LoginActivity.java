package com.asijaandroidsolution.myapplication.activity.login;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.asijaandroidsolution.myapplication.R;
import com.asijaandroidsolution.myapplication.activity.BaseActivity;
import com.asijaandroidsolution.myapplication.activity.services.AppLocationServices;
import com.asijaandroidsolution.myapplication.activity.utils.Constants;
import com.asijaandroidsolution.myapplication.databinding.ActivityLoginBinding;
import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;

import java.util.Arrays;
import java.util.List;

public class LoginActivity extends BaseActivity {
    private static final int RC_SIGN_IN = 123;
    private static final int LOCATION_PERMISSION_CODE = 123;
    private static final int REQUEST_ENABLE_SETTING = 321;
    private String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private List<AuthUI.IdpConfig> providers;
    private Button btnLogout;
    private FirebaseUser user;
    private String url;
    private ActivityLoginBinding binding;
    private LocationSettingsRequest mLocationSettingRequest;
    private SettingsClient mSettingClient;
    private AppLocationServices appLocationService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setting up view binding
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        //set up toolbar title
        setSupportActionBar(binding.toolBar);
        getSupportActionBar().setTitle("User Profile");
        //setting logout button
        btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(listener);

        //check current user if user is null show login screen

        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {

            // Choose authentication providers
            providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build(),
                    new AuthUI.IdpConfig.PhoneBuilder().build(),
                    new AuthUI.IdpConfig.GoogleBuilder().build(),
                    new AuthUI.IdpConfig.FacebookBuilder().build());

            // Create and launch sign-in intent

            createAndLaunchIntent();
        }
        //setup the user profile
        else {
            setUi();
        }


    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AuthUI.getInstance()
                    .signOut(LoginActivity.this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {
                            startActivity(new Intent(getIntent()));
                        }
                    });

        }
    };

    private void createAndLaunchIntent() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setLogo(R.drawable.login_demo)
                        .setTheme(R.style.AppTheme)
                        .build(),
                RC_SIGN_IN);
    }

    //onActivityResult
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {

                user = FirebaseAuth.getInstance().getCurrentUser();
                Toast.makeText(this, "loginsuccessfull", Toast.LENGTH_LONG).show();
                setUi();
            }
        }
            else if(requestCode==REQUEST_ENABLE_SETTING)
            {
                switch (resultCode)
                {
                    case RESULT_OK:
                        showLocation();
                        break;
                    case RESULT_CANCELED:
                        //show the dialog again if user denied to turn on gps
                        enableGps();
                        break;
                }
            }

    }

    private void setUi() {
        if (user != null) {
            for (UserInfo profile : user.getProviderData()) {
                // Id of the provider (ex: google.com)
                String providerId = profile.getProviderId();

                // UID specific to the provider
                String uid = profile.getUid();

                // Name, email address, and profile photo Url

                Uri photoUrl = profile.getPhotoUrl();
                url = photoUrl+"";
                if (url.endsWith("picture")) {
                    url = url + "?type=large";
                } else {
                    url = url.replace("s96-c", "s220-c");
                }
                Glide.with(LoginActivity.this).load(url).into(binding.imgprofileImage);
                binding.editName.setText(profile.getDisplayName());
                binding.editEmail.setText(profile.getEmail());
                binding.editPhone.setText(profile.getPhoneNumber());
            }
        }
        //sett the location
        setLocation();
    }

    private void setLocation() {
        //location permission
        if (ContextCompat.checkSelfPermission(
                this, permission[0]) ==
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this, permission[0]) == PackageManager.PERMISSION_GRANTED) {

            //turn on gps is not on already
            if(!isGpsOn())
                enableGps();
            //show location if gps is on
            else
                showLocation();
        } else {
            // You can directly ask for the permission.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permission, LOCATION_PERMISSION_CODE);
            }
        }
    }

    private void showLocation() {
        appLocationService=new AppLocationServices(this);
        binding.txtLocation.setText("Loading....");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                binding.txtLocation.setText(Constants.cityName);
            }
        },4000);
    }

    private void enableGps() {
        LocationSettingsRequest.Builder builder=new LocationSettingsRequest.Builder();
        builder.addLocationRequest(new LocationRequest().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY))
                .setAlwaysShow(true);
        mLocationSettingRequest=builder.build();
        mSettingClient= LocationServices.getSettingsClient(this);
        mSettingClient.checkLocationSettings(mLocationSettingRequest)
                .addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        showLocation();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        int statusCode= ((ApiException) e).getStatusCode();
                        switch (statusCode)
                        {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                ResolvableApiException rae= (ResolvableApiException) e;
                                try {
                                    rae.startResolutionForResult(LoginActivity.this,REQUEST_ENABLE_SETTING);
                                } catch (IntentSender.SendIntentException ex) {
                                    ex.printStackTrace();
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                Log.e("GPS Error","setting change unavailable");

                                break;
                        }

                    }
                })
                .addOnCanceledListener(new OnCanceledListener() {
                    @Override
                    public void onCanceled() {

                    }
                });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_PERMISSION_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if(!isGpsOn())
                                enableGps();
                    else
                    {
                        showLocation();
                    }
                }
                else
                {
                    //show permission dialog again if user denied permission
                    setLocation();
                }
                return;
        }
    }
    //check whether the gps is on
    private boolean isGpsOn() {
        LocationManager locationManager=(LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}






