package com.finnair.gamifiedpartnermap;

import android.Manifest;
import android.annotation.SuppressLint;

import android.app.DialogFragment;
import android.content.Context;
import android.content.pm.PackageManager;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;

import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;





public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,  LocationPermissionDialog.LocationDialogListener, GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener{

    // String for class name. Can be used for reporting errors.
    private static final String TAG = MapsActivity.class.getSimpleName();
    Marker markerClose;
    Marker markerFar;

    //Constants marking which permissions were granted.
    final static int locationPermission = 100;


    private GoogleMap mMap;


    //These are used to get the users current location.
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

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        criteria = new Criteria();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case (locationPermission): {

                if ( grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
                        if (location != null) mMap.moveCamera(CameraUpdateFactory.newLatLngZoom( new LatLng(location.getLatitude(), location.getLongitude()), 13));
                            //Location not available so center on Helsinki.
                        else mMap.moveCamera(CameraUpdateFactory.newLatLngZoom( new LatLng(60.167497, 24.934739), 13));

                        //Enable the myLocation Layer
                        mMap.setMyLocationEnabled(true);
                        mMap.setOnMyLocationButtonClickListener(this);
                        mMap.setOnMyLocationClickListener(this);

                    }

                }
                else {

                    Log.i("assert", "Denied");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                            LocationPermissionDialog dialog = new LocationPermissionDialog();
                            dialog.show(getFragmentManager(), "permissionInfo");

                        }
                        //Location not available so center on Helsinki.
                        else mMap.moveCamera(CameraUpdateFactory.newLatLngZoom( new LatLng(60.167497, 24.934739), 13));

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
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, locationPermission);
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
        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowClickListener(this);

        // Customize the styling of the base map using a JSON object
        // defined in a raw resource file
        try{
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e){
            Log.e(TAG, "Can't find style. Error: ", e);
        }


        //Request permission to use the user's location data.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
            if (location != null) mMap.moveCamera(CameraUpdateFactory.newLatLngZoom( new LatLng(location.getLatitude(), location.getLongitude()), 13));
                //Location not available so center on Helsinki.
            else mMap.moveCamera(CameraUpdateFactory.newLatLngZoom( new LatLng(60.167497, 24.934739), 13));

            //Enable the myLocation Layer
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationButtonClickListener(this);
            mMap.setOnMyLocationClickListener(this);

        }
        else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, locationPermission);
        }


        MarkerClass markerClass = new MarkerClass(this);
        // Create an ordinary balloon Marker:
        MarkerOptions farOptions = markerClass.balloonMarkerOptions(
                new LatLng( 60.1699, 24.9384), // 60.1699° N, 24.9384° E  60.1841, 24.8301
                "Otaniemi",
                "Otaniemi is here, trust me. Click to turn me BLUE!");
        // Create an image Marker by copying and modifying the balloon Marker:
        MarkerOptions closeOptions = markerClass.imageMarkerOptions(farOptions);

        // Add both (balloon, image) Markers on the map:
        markerClose = mMap.addMarker(closeOptions);
        markerFar = mMap.addMarker(farOptions);
        if (mMap.getCameraPosition().zoom > 10){
            markerClose.setVisible(true);
            markerFar.setVisible(false);
        }
        else {
            markerClose.setVisible(false);
            markerFar.setVisible(true);
        }


        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                CameraPosition cameraPosition = mMap.getCameraPosition();

                // Depending on the zoom level hide one and set visible the other Marker
                if(cameraPosition.zoom > 10) {
                    markerClose.setVisible(true);
                    markerFar.setVisible(false);
                } else {
                    markerClose.setVisible(false);
                    markerFar.setVisible(true);
                }
            }
        });

    }



    @Override
    public boolean onMarkerClick(final Marker marker){
        if (marker.equals(markerClose) || marker.equals(markerFar)){
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15.0f));
            markerClose.setVisible(true);
            markerFar.setVisible((false));

            markerClose.showInfoWindow();
        }
        return true;  // What am I supposed to return? public void gets rejected...
    }

    @Override
    public void onInfoWindowClick(final Marker marker){
        // This is just a simple event for infoWindowOnClick. This will be replaced with a proper event!
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
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


