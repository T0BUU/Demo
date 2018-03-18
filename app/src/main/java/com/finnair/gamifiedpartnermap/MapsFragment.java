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
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;
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
import com.google.maps.android.MarkerManager;
import com.google.maps.android.clustering.Cluster;


import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.finnair.gamifiedpartnermap.MainActivity.locationPermission;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class MapsFragment extends Fragment {


    public MapsFragment() {

    }

    // String for class name. Can be used for reporting errors.
    private static final String TAG = MapsFragment.class.getSimpleName();

    private GoogleMap mMap;
    private PartnerMarkerClass partnerMarkerClass;
    private PlaneMarkerClass planeMarkerClass;
    private MapView mMapView;
    private com.finnair.gamifiedpartnermap.ClusterManager<ClusterMarker> partnerClusterManager;
    private com.finnair.gamifiedpartnermap.ClusterManager<ClusterMarker> planeClusterManager;

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

                PartnerInfoWindow customInfoWindow = new PartnerInfoWindow(getActivity());
                mMap.setInfoWindowAdapter(customInfoWindow);

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
                        userLocation.setLatitude(60.2055); // 60.320850 hki-vantaa
                        userLocation.setLongitude(24.6559); // 24.952630
                    }

                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()), 13));

                } // If permission granted


                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()), 13));

                        //Enable the myLocation Layer
                        mMap.setMyLocationEnabled(true);




                partnerClusterManager = new com.finnair.gamifiedpartnermap.ClusterManager<ClusterMarker>(getContext(), mMap, new MarkerManager(mMap));
                planeClusterManager = new com.finnair.gamifiedpartnermap.ClusterManager<ClusterMarker>(getContext(), mMap, new MarkerManager(mMap));
                partnerMarkerClass = new PartnerMarkerClass(getActivity(), mMap);



                MarkerRenderer partnerMarkerRenderer = new com.finnair.gamifiedpartnermap.MarkerRenderer(getContext(), mMap, partnerClusterManager, false);
                final MarkerRenderer planeMarkerRenderer = new com.finnair.gamifiedpartnermap.MarkerRenderer(getContext(), mMap, planeClusterManager, true);
                planeMarkerClass = new PlaneMarkerClass(getActivity(), mMap, userLocation, planeClusterManager, planeMarkerRenderer);

                partnerMarkerClass.fetchFromFirebase(partnerClusterManager, partnerMarkerRenderer);
                partnerClusterManager.setRenderer(partnerMarkerRenderer);
                planeClusterManager.setRenderer(planeMarkerRenderer);

                mMap.setOnCameraIdleListener(partnerClusterManager);
                // mMap.setOnCameraIdleListener(planeClusterManager);
                partnerClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<ClusterMarker>() {
                    @Override
                    public boolean onClusterClick(Cluster<ClusterMarker> cluster) {
                        Log.d("POOP", "Partner cluster clicked"); // Never called. Unclear why
                        return false;
                    }
                });

                planeClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<ClusterMarker>() {
                    @Override
                    public boolean onClusterClick(Cluster<ClusterMarker> cluster) {
                        Log.d("POOP", "Plane cluster clicked"); // Never called. Unclear why
                        return false;
                    }
                });

                mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                    @Override
                    public void onCameraMove() {
                        CameraPosition cameraPosition = mMap.getCameraPosition();
                        planeMarkerClass.zoomListener(cameraPosition.zoom);
                        partnerClusterManager.cluster();
                        planeClusterManager.cluster();
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


                        if (planeMarkerClass.containsMarker(marker)) {
                            // User clicked an airplane
                            Plane plane = planeMarkerClass.getPlaneByID(marker.getTitle());
                            if (plane.isWithinReach(userLocation)) {
                                Log.d("POOP", "You can collect this plane!");

                                ((MainActivity) getActivity()).onPlaneCatch(plane, planeMarkerClass.getRandomPlane());

                            } else{
                                ((MainActivity) getActivity()).onPlaneCatch(plane, planeMarkerClass.getRandomPlane());

                            }

                        } else if (partnerMarkerClass.containsMarker(marker)) {

                            //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15.0f));

                            Partner partner = partnerMarkerClass.getPartnerByID(marker.getTitle());
                            if (partner.isWithinReach(userLocation)) {
                                Log.d("POOP", "You can collect this plane!");

                                ((MainActivity) getActivity()).onPartnerCatch(partner, partnerMarkerClass.getRandomPartner());
                            }
                            else {
                                InfoWindowData info = new InfoWindowData();
                                info.setData(partner.getID(), partner.getAddress(), partner.getDescription());

                                marker.setTag(info);
                                marker.showInfoWindow();
                            }


                        } else {
                            Log.d("POOP", "You most likely clicked a cluster. Nothing should happen.");
                        }


                        return true;  // What am I supposed to return? public void gets rejected...
                    }
                });

                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(final Marker marker) {
                        Partner partner = partnerMarkerClass.getPartnerByID(marker.getTitle());
                        ((MainActivity) getActivity()).onPartnerCatch(partner, partnerMarkerClass.getRandomPartner());
                    }
                });

                // First call to OpenSky (AsyncTask):
                planeMarkerClass.refreshOpenSkyPlanes();
                // Timed call to OpenSky (AsyncTask):
                final long INTERVAL = 1000 * 15; // 30 seconds
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
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            mGeofencingClient = LocationServices.getGeofencingClient(getActivity());

            test = new Geofence.Builder()
                    .setRequestId("Otaniemi")
                    .setCircularRegion(60.1841, 24.8301, 100.0f)
                    .setExpirationDuration(60000L * 10L)
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
        }





        //-----------------------



        return rootView;
    }


    /*
    * This method moves camera to given partner and opens its infowindow.
    * Called from partnerListWindow when user clicks on partner name.
    */
    public void moveCameraToPartner(Partner partner){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(partner.getPosition(), 15.0f));
        List<Marker> partnerMarkers = new ArrayList<>(partnerClusterManager.getMarkerCollection().getMarkers());
        for(Marker marker : partnerMarkers) {
            if(marker.getPosition().equals(partner.getPosition())){
                InfoWindowData info = new InfoWindowData();
                info.setData(partner.getID(), partner.getAddress(), partner.getDescription());

                marker.setTag(info);
                marker.showInfoWindow();
            }
        }
    }

    /*
     * This method filters markers shown on map.
     * It gets partners we want to show as parameter List<Partner>.
     * First it clears all items from clusterManager (which manages our markers)
     * Then it adds all partners we want to show to clusterManager.  /Note: Partner extends ClusterMarker
     * And at last it clusters markers again.
     */
    public void filterPartners(List<Partner> partnersToShow){
        partnerClusterManager.clearItems();
        for(Partner p : partnersToShow){
            partnerClusterManager.addItem(p);
        }
        partnerClusterManager.cluster();
    }




    public ConcurrentHashMap<String, HashSet<String>> getPlaneCollection() {
        return planeMarkerClass.getCollection();
    }

    public ConcurrentHashMap<String, HashSet<String>> getPartnerCollection() {
        return partnerMarkerClass.getCollection();
    }

    public PartnerMarkerClass getPartners() {
        return partnerMarkerClass;
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

    public void setLocation(Location loc) {
        this.userLocation = loc;
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

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

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
        }

        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }




}
