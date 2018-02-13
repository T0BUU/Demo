package com.finnair.gamifiedpartnermap;

import android.*;
import android.Manifest;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;


/**
 * Created by ala-hazla on 16.12.2017.
 */

public class MainActivity extends AppCompatActivity implements LocationPermissionDialog.LocationDialogListener,
                                                                PlaneCatchFragment.PlaneCatchListener {

    private MActivityLayout myMainLayout;

    private FragmentManager fragmentManager;

    private MapsFragment mapFragment;
    private GoogleMap gMap;

    private String planesListing = "";


    //Constants.
    final static int locationPermission = 100;
    final static String planeCatchMessage = "com.finnair.gamifiedpartnermap.planeCaught";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        fragmentManager = getSupportFragmentManager();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            //Main layout class, this instanties whole UI.
            myMainLayout = new MActivityLayout();
            myMainLayout.createUI(this, fragmentManager);


        } else ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, locationPermission);
    }




    protected void onCardButtonClick(View v) {
        Intent intent = new Intent(this, PlaneCollectionActivity.class);
        intent.putExtra(planeCatchMessage ,this.planesListing);
        startActivity(intent);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case (locationPermission): {

                if ( grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        //Main layout class, this instanties whole UI.
                        myMainLayout = new MActivityLayout();
                        myMainLayout.createUI(this, fragmentManager);
                    }

                }
                else {

                    Log.i("assert", "Denied");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                            LocationPermissionDialog dialog = new LocationPermissionDialog();
                            dialog.show(this.getFragmentManager(), "permissionInfo");

                        }
                        //Location not available so center on Helsinki.
                        else {
                            myMainLayout = new MActivityLayout();
                            myMainLayout.createUI(this, fragmentManager);
                        }

                    }


                }

                return;
            }
        }

    }



    public void setMap(GoogleMap m) {
        gMap = m;
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT);
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, locationPermission);
    }

    @Override
    public void onPlaneDialogPositiveClick(DialogFragment dialog) {
        Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT);
        Intent intent = new Intent(this, PlaneCollectionActivity.class);
        intent.putExtra(planeCatchMessage ,this.planesListing);
        startActivity(intent);
    }

    public void setPlanesListing(String s) {
        this.planesListing = s;
    }
}

