package com.finnair.gamifiedpartnermap;

import android.Manifest;
import android.annotation.SuppressLint;

import android.content.Context;
import android.content.pm.PackageManager;

import android.location.Criteria;
import android.location.Location;

import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;





public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener {


    //Constants marking which permissions were granted.
    final private int locationPermission = 100;
    final private int internetPermission = 101;


    private GoogleMap mMap;

    LocationManager locationManager;
    Criteria criteria;
    Location location;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Request permission to use user's internet connection.
        //Currently this connection isn't used for anything other than checking location data if the gps isn't available.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, internetPermission);
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, internetPermission);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        criteria = new Criteria();



    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case (locationPermission): {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);

                }
                else {
                    //TODO:
                    /*
                    If the user doesn't allow the use of location information what does the program do?
                     */
                }
                return;
            }

            case (internetPermission): {
                //Currently there isn't anything to add here.
            }

        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //In case the user's current location isn't available center the view on Helsinki.
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom( new LatLng(60.167497, 24.934739), 13));
        //Request permission to use the user's location data.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, locationPermission);
        } else {
            location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        }

        if (location != null) mMap.moveCamera(CameraUpdateFactory.newLatLngZoom( new LatLng(location.getLatitude(), location.getLongitude()), 13));

        //Enable the myLocation Layer
        mMap.setMyLocationEnabled(true);

        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);




    }


    @Override
    public void onMyLocationClick(Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }


}


