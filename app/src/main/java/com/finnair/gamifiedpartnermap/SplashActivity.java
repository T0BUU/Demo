package com.finnair.gamifiedpartnermap;

import android.*;
import android.Manifest;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by huzla on 13.2.2018.
 */
import java.util.HashMap;

import static com.finnair.gamifiedpartnermap.MainActivity.locationPermission;
import static com.finnair.gamifiedpartnermap.MainActivity.profileInfoStartUp;

public class SplashActivity extends AppCompatActivity implements LocationPermissionDialog.LocationDialogListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

            HashMap<String, String> profileInformation = new HashMap();

            if(getIntent() != null){
                Log.d("Login", ""+getIntent().getAction());
                if(getIntent().getAction().equals("com.finnair.gamifiedpartnermap.AUTHORIZATION_FAILED")){
                    Toast.makeText(this, "Authorization failed", Toast.LENGTH_SHORT).show();
                }
                else if(getIntent().getAction().equals("com.finnair.gamifiedpartnermap.PROFILE_REQUEST_SUCCESSFUL")){
                    profileInformation = (HashMap<String, String>) getIntent().getSerializableExtra("profileInformation");
                    Toast.makeText(this, "Login successful", Toast.LENGTH_LONG).show();


                }
            }

            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            intent.putExtra(profileInfoStartUp ,profileInformation);

            startActivity(intent);

        finish();
        } else ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CAMERA}, locationPermission);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case (locationPermission): {

                if ( grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        startActivity(new Intent(SplashActivity.this, MainActivity.class));

                        finish();
                    }

                }
                else {

                    Log.i("assert", "Denied");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                        Log.d("Permission dialog", "Made it here");
                        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) || shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                            Log.d("Permission dialog", "Should show permission dialog.");
                            LocationPermissionDialog dialog = new LocationPermissionDialog();
                            dialog.show(this.getFragmentManager(), "permissionInfo");

                        }
                        //Location not available so center on Helsinki.
                        else {
                            startActivity(new Intent(SplashActivity.this, MainActivity.class));

                            finish();
                        }

                    }


                }

                return;
            }
        }

    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT);
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, locationPermission);
    }

}
