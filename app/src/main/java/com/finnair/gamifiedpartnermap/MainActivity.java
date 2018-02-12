package com.finnair.gamifiedpartnermap;

import android.*;
import android.Manifest;
import android.app.DialogFragment;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
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

public class MainActivity extends AppCompatActivity implements LocationPermissionDialog.LocationDialogListener{

    private MActivityLayout myMainLayout;

    private Fragment mapFragment;
    private GoogleMap gMap;

    private FragmentManager fragmentManager;


    //Constants marking which permissions were granted.
    final static int locationPermission = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        fragmentManager = getSupportFragmentManager();

        //Main layout class, this instanties whole UI.
        myMainLayout = new MActivityLayout();
        myMainLayout.createUI(this, fragmentManager);
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
}

