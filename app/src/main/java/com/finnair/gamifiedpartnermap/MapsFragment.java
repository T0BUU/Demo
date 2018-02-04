package com.finnair.gamifiedpartnermap;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.content.res.Resources;
import android.location.Criteria;
import android.location.Location;

import android.location.LocationManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import static com.finnair.gamifiedpartnermap.MainActivity.locationPermission;

public class MapsFragment extends Fragment {


    public MapsFragment() {

    }

    // String for class name. Can be used for reporting errors.
    private static final String TAG = MapsFragment.class.getSimpleName();
    private DatabaseReference databaseReference;

    private HashMap<String, PartnerData> markerPartnerData = new HashMap<>();


    private GoogleMap mMap;
    private CompanyMarkerClass companyMarkerClass;
    private PlaneMarkerClass planeMarkerClass;
    private MapView mMapView;


    //These are used to get the users current userLocation.
    private LocationManager locationManager;
    private Criteria criteria;
    private Location userLocation;

    private GeofencingClient mGeofencingClient;
    private Geofence test;
    private PendingIntent mGeofencePendingIntent;
    private Marker geoFenceMarker;


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {





        View rootView = inflater.inflate(R.layout.content_maps, container, false);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        criteria = new Criteria();

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {

                mMap = googleMap;

                ((MainActivity) getActivity()).setMap(mMap);

                // Customize the styling of the base map using a JSON object defined in a raw resource file
                try {
                    boolean success = mMap.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(
                                    getActivity(), R.raw.style_json));

                    if (!success) {
                        Log.e(TAG, "Style parsing failed.");
                    }
                } catch (Resources.NotFoundException e) {
                    Log.e(TAG, "Can't find style. Error: ", e);
                }


                //Request permission to use the user's userLocation data.
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    userLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
                    if (userLocation == null) {
                        // Location not available so center manually . Location provider set to <"">
                        // Kamppi (good for testing firms): 60.167497, 24.934739
                        // Espoo (good for plane spotting): 60.2055, 24.6559
                        userLocation = new Location("");
                        userLocation.setLatitude(60.2055);
                        userLocation.setLongitude(24.6559);
                    }

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()), 13));

                    //Enable the myLocation Layer
                    mMap.setMyLocationEnabled(true);


                } else ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, locationPermission);

                companyMarkerClass = new CompanyMarkerClass(getActivity(), mMap);
                planeMarkerClass = new PlaneMarkerClass(getActivity(), mMap, userLocation);


                databaseReference = FirebaseDatabase.getInstance().getReference();
                DatabaseReference ref = databaseReference.child("locations");

                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Loop through children in "locations" (i.e. loop through partners in FireBase):
                        for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                            String companyName = singleSnapshot.child("name").getValue().toString();
                            Double lat = Double.parseDouble(singleSnapshot.child("lat").getValue().toString());
                            Double lng = Double.parseDouble(singleSnapshot.child("lng").getValue().toString());
                            String address = singleSnapshot.child("address").getValue().toString();
                            String business = singleSnapshot.child("field_of_business").getValue().toString();
                            String description = singleSnapshot.child("description").getValue().toString();

                            // Place two Markers (close, far) on the map and hide one depending on zoom:
                            String[] tags = companyMarkerClass.addOneMarkerOnMap(lat, lng, companyName, business);

                            // Record partner data into a HashMap which has Marker tag as key and ParterData as content:
                            PartnerData pData = new PartnerData();
                            pData.setAllData(companyName, address, business, description, lat, lng);

                            // Both Markers (close, far) must be recorded in the HashMap:
                            markerPartnerData.put(tags[0], pData);
                            markerPartnerData.put(tags[1], pData);
                        }

                        if (mMap.getCameraPosition().zoom > 10) companyMarkerClass.showCloseMarkers();
                        else companyMarkerClass.showFarMarkers();

                    }


                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "onCancelled", databaseError.toException());
                    }
                });


                if (mMap.getCameraPosition().zoom > 10) companyMarkerClass.showCloseMarkers();
                else companyMarkerClass.showFarMarkers();


                mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                    @Override
                    public void onCameraMove() {
                        CameraPosition cameraPosition = mMap.getCameraPosition();

                        planeMarkerClass.zoomListener(cameraPosition.zoom);
                        //Log.d("Zoom Level", "" + java.lang.Math.pow(2, 20-cameraPosition.zoom));
                        // Depending on the zoom level hide ones and set visible the other Markers
                        if (cameraPosition.zoom > 10) companyMarkerClass.showCloseMarkers();
                        else companyMarkerClass.showFarMarkers();

                    }
                });


                mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                    @Override
                    public boolean onMyLocationButtonClick() {
                        Toast.makeText(getActivity(), "MyLocation button clicked", Toast.LENGTH_SHORT).show();
                        // Return false so that we don't consume the event and the default behavior still occurs
                        // (the camera animates to the user's current position).
                        return false;
                    }
                });

                mMap.setOnMyLocationClickListener(new GoogleMap.OnMyLocationClickListener() {
                    @Override
                    public void onMyLocationClick(Location location) {
                        Toast.makeText(getActivity(), "Current userLocation:\n" + location, Toast.LENGTH_LONG).show();
                    }
                });

                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(final Marker marker) {

                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15.0f));

                        marker.showInfoWindow();

                        return true;  // What am I supposed to return? public void gets rejected...
                    }
                });

                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(final Marker marker) {

                        if (planeMarkerClass.containsMarker(marker)){
                            // User clicked an airplane
                            Plane plane = planeMarkerClass.getPlaneByID(marker.getTitle());
                            if (plane.planeIsWithinReach(userLocation) ){
                                Log.d("POOP", "You can collect this plane!");
                                Log.d("POOP", " " + plane.getPlaneID() + ": "
                                        + plane.getPlaneLatLng().latitude + ", "
                                        + plane.getPlaneLatLng().longitude + ", "
                                        + plane.getOriginCountry() + ", "
                                        + plane.getIcao24() + ", Alt: "
                                        + plane.getGeoAltitude() + ", speed: "
                                        + plane.getVelocityKmph() );
                                plane.savePlane(getContext());

                            } else{
                                Log.d("POOP", "Nuh-uh, you can't reach me.");
                            }

                        } else {
                            // User clicked something else than an airplane (company marker):
                            // Instantiate PartnerInfoFragment:
                            PartnerInfoFragment p = new PartnerInfoFragment();
                            p.show(getActivity().getFragmentManager().beginTransaction(), "Add data");
                            // Find PartnerData for the Marker clicked recently. Find correct by reading markerID:
                            PartnerData currentPartner = markerPartnerData.get(marker.getId());

                            // Before displaying the PartnerInfoFragment set necessary variables for the PartnerInfoFragment instance:
                            p.setAllFragmentData(currentPartner.getCompanyName(), currentPartner.getFieldOfBusiness(), currentPartner.getCompanyAddress(), currentPartner.getCompanyDescription());
                        }
                    }
                });

                // First call to OpenSky (AsyncTask):
                planeMarkerClass.refreshOpenSkyPlanes();
                // Timed call to OpenSky (AsyncTask):
                final long INTERVAL = 1000 * 30; // 30 seconds
                final Handler handler = new Handler();
                Runnable runnable = new Runnable() {

                    @Override
                    public void run() {
                        try{
                            planeMarkerClass.refreshOpenSkyPlanes();
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                        finally{
                            // Call the same runnable to call it at regular interval
                            handler.postDelayed(this, INTERVAL);
                        }
                    }
                };
                handler.postDelayed(runnable, INTERVAL);
            }

        });



        //TODO: Currently adds a simple geofence to the first 100 locations. Figure out something different.
        mGeofencingClient = LocationServices.getGeofencingClient(getActivity());

        test = new Geofence.Builder()
                .setRequestId("Otaniemi")
                .setCircularRegion(60.1841, 24.8301, 100.0f)
                .setExpirationDuration(60000L*10L)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();

        markerForGeofence(new LatLng(60.1841, 24.8301));

        mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(getActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

        //-----------------------

        return rootView;
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofence(test);
        return builder.build();

    }

    private void markerForGeofence(LatLng latLng) {
        Log.i(TAG, "markerForGeofence(" + latLng + ")");
        String title = latLng.latitude + ", " + latLng.longitude;
        // Define marker options
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .title(title);
        if (mMap != null) {
            // Remove last geoFenceMarker
            if (geoFenceMarker != null)
                geoFenceMarker.remove();

            geoFenceMarker = mMap.addMarker(markerOptions);
        }
    }




    private PendingIntent getGeofencePendingIntent() {

        if (mGeofencePendingIntent != null) {
         return mGeofencePendingIntent;
        }


        Intent intent = new Intent(getContext(), GeofenceTransitionsIntentService.class);

        mGeofencePendingIntent = PendingIntent.getService(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        return mGeofencePendingIntent;
    }

    public Criteria getCriteria() {
        return criteria;
    }

    public void setUserLocation(Location l) {
        userLocation = l;
    }

    public LocationManager getLocationManager() {
        return locationManager;
    }


    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mGeofencingClient.removeGeofences(getGeofencePendingIntent())
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(getActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }







}


