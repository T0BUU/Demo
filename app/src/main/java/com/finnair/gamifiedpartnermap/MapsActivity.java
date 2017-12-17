package com.finnair.gamifiedpartnermap;

import android.Manifest;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.pm.PackageManager;

import android.content.res.Resources;
import android.location.Criteria;
import android.location.Location;

import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,  LocationPermissionDialog.LocationDialogListener, GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener{

    // String for class name. Can be used for reporting errors.
    private static final String TAG = MapsActivity.class.getSimpleName();
    private DatabaseReference databaseReference;
    private HashMap<String, PartnerData> markerPartnerData = new HashMap<>();


    //Constants marking which permissions were granted.
    final static int locationPermission = 100;

    private GoogleMap mMap;
    private MarkerClass markerClass;


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

        markerClass = new MarkerClass(this, mMap);

        // Open connection to FireBase:
        databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference ref = databaseReference.child("locations");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Loop through children in "locations" (i.e. loop through partners in FireBase):
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    String companyName = singleSnapshot.child("name").getValue().toString();
                    Double lat = Double.parseDouble( singleSnapshot.child("lat").getValue().toString() );
                    Double lng = Double.parseDouble( singleSnapshot.child("lng").getValue().toString() );
                    String address = singleSnapshot.child("address").getValue().toString();
                    String business = singleSnapshot.child("field_of_business").getValue().toString();
                    String description = singleSnapshot.child("description").getValue().toString();

                    // Place two Markers (close, far) on the map and hide one depending on zoom:
                    String[] tags = markerClass.addOneMarkerOnMap(lat, lng, companyName, business);

                    // Record partner data into a HashMap which has Marker tag as key and ParterData as content:
                    PartnerData pData = new PartnerData();
                    pData.setAllData(companyName, address, business, description, lat, lng);

                    // Both Markers (close, far) must be recorded in the HashMap:
                    markerPartnerData.put(tags[0], pData);
                    markerPartnerData.put(tags[1], pData);
                }

                if (mMap.getCameraPosition().zoom > 10) markerClass.showCloseMarkers();
                else markerClass.showFarMarkers();

            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled", databaseError.toException());
            }
        });

        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                CameraPosition cameraPosition = mMap.getCameraPosition();

                // Depending on the zoom level hide ones and set visible the other Markers
                if (cameraPosition.zoom > 10) markerClass.showCloseMarkers();
                else markerClass.showFarMarkers();

            }
        });
    }


    @Override
    public boolean onMarkerClick(final Marker marker){

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15.0f));
        markerClass.showCloseMarkers();

        marker.showInfoWindow();

        return true;  // What am I supposed to return? public void gets rejected...
    }

    @Override
    public void onInfoWindowClick(final Marker marker){

        // Instantiate PartnerInfoFragment:
        PartnerInfoFragment p = new PartnerInfoFragment();

        // Find PartnerData for the Marker clicked recently. Find correct by reading markerID:
        PartnerData currentPartner = markerPartnerData.get( marker.getId() );

        // Before displaying the PartnerInfoFragment set necessary variables for the PartnerInfoFragment instance:
        p.setAllFragmentData( currentPartner.getCompanyName(), currentPartner.getFieldOfBusiness(), currentPartner.getCompanyAddress(), currentPartner.getCompanyDescription() );

        // Display PartnerInfoFragment:
        // Notice! Content (company name etc.) could not be changed with p.show()
        FragmentManager frgManager = getFragmentManager();
        frgManager.beginTransaction()
                .add(p, marker.getTitle())
                .commit();

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


