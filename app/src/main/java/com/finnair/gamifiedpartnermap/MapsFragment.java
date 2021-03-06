package com.finnair.gamifiedpartnermap;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.content.res.Resources;
import android.graphics.Color;
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
import android.util.Pair;
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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
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
    public Location userLocation;

    private static double userLat;
    private static double userLng;
    private static double planeLat;
    private static double planeLng;
    private static double planeAlt;
    public static ArrayList calc = new ArrayList(4);

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
                        // Frankfurt (lots of planes): 50.120602, 8.68355
                        userLocation = new Location("");
                        userLocation.setLatitude(60.2055); // 60.320850 hki-vantaa
                        userLocation.setLongitude(24.6559); // 24.952630
                    }

                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()), 13));

                } // If permission granted


                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()), 13));

                        //Enable the myLocation Layer
                        mMap.setMyLocationEnabled(true);

                // Delete immediately if you see this. Only for testing purposes (Santeri) ////////////////////////////////////////
                mMap.addCircle(new CircleOptions()
                        .center(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()))
                        .radius(30000)
                        .strokeColor(Color.RED));
                ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////


                partnerClusterManager = new com.finnair.gamifiedpartnermap.ClusterManager<ClusterMarker>(getContext(), mMap, new MarkerManager(mMap));
                planeClusterManager = new com.finnair.gamifiedpartnermap.ClusterManager<ClusterMarker>(getContext(), mMap, new MarkerManager(mMap));
                partnerMarkerClass = new PartnerMarkerClass(getActivity(), mMap);



                final MarkerRenderer partnerMarkerRenderer = new com.finnair.gamifiedpartnermap.MarkerRenderer(getContext(), mMap, partnerClusterManager, false);
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


                        for (String partnerStr : partnerMarkerClass.partnerHashMap.keySet()){
                            // Only necessary for Partners. Plane animations take care of zombies
                            partnerMarkerRenderer.deleteZombieMarkers(partnerMarkerClass.getPartnerByID(partnerStr));
                        }
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

                            if(plane.getGeoAltitude() == null){  // sometimes geoAltitude takes time to update, set it to 0 if null. That's usually good estimate.
                                planeAlt = 0.0;
                            }
                            else {
                                planeAlt = plane.getGeoAltitude();
                            }

                            calc.add(0, userLat = userLocation.getLatitude());  //Set all values to calulations.
                            calc.add(1, userLng = userLocation.getLongitude());
                            calc.add(2, planeLat = plane.getLatLng().latitude);
                            calc.add(3, planeLng = plane.getLatLng().longitude);
                            calc.add(4, planeAlt);

                            Calculations calculations = new Calculations();
                            calculations.getBearing();  // set bearing and angle



                            if (plane.isWithinReach(userLocation)) {
                                ((MainActivity) getActivity()).onPlaneCatch(plane, planeMarkerClass.getRandomPlane());
                            } else {
                                plane.setBonusMarker(mMap);
                                ((MainActivity) getActivity()).onPlaneCatch(plane, planeMarkerClass.getRandomPlane());
                            }

                        } else if (partnerMarkerClass.containsMarker(marker)) {

                            //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15.0f));

                            Partner partner = partnerMarkerClass.getPartnerByID(marker.getTitle());
                            if (partner.isWithinReach(userLocation)) {
                                ((MainActivity) getActivity()).onPartnerCatch(partner, partnerMarkerClass.getRandomPartner());
                            }
                            else {
/*InfoWindowData                info = new InfoWindowData();
                                info.setData(partner.getID(), partner.getAddress(), partner.getDescription());

                                marker.setTag(info);
                                marker.showInfoWindow();*/
                                ((MainActivity) getActivity()).onPartnerCatch(partner, partnerMarkerClass.getRandomPartner());
                            }


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






        return rootView;
    }


    /*
        * Called from partnerListWindow when user clicks on partner name.
        * This method moves camera to given partner and zoom
        * Sets CancelableCallback for handling showInfoWindow and showPopupWindow after animation is ready.
        */
    public void moveCameraToPartner(final Partner partner, final PartnerListWindow pLW){
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(partner.getPosition(), 15.0f), new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish(){
                List<Marker> partnerMarkers = new ArrayList<>(partnerClusterManager.getMarkerCollection().getMarkers());
                for(Marker marker : partnerMarkers) {
                    if(marker.getPosition().equals(partner.getPosition())){
                        InfoWindowData info = new InfoWindowData();
                        info.setData(partner.getID(), partner.getAddress(), partner.getDescription());

                        marker.setTag(info);
                        marker.showInfoWindow();

                        pLW.showPopupWindow();
                    }
                }

            }
            @Override
            public void onCancel(){

            }
        });
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

    public void refreshPlaneCollection() {
        planeMarkerClass.readCollectedPlanes(getActivity());
    }

    public ConcurrentHashMap<String, HashSet<String>> getPartnerCollection() {
        return partnerMarkerClass.getCollection();
    }

    public void refreshPartnerCollection() {
        partnerMarkerClass.readCollectedPartners(getActivity());
    }

    public void removeChallenge(Challenge c) {
        partnerMarkerClass.removeChallenge(c);
        planeMarkerClass.removeChallenge(c);
    }

    public void addChallenge(Challenge c) {
        partnerMarkerClass.addChallenge(c);
        planeMarkerClass.addChallenge(c);
    }

    public Pair<ArrayList<Plane>, ArrayList<Partner>> getRandomRewards(int amount) {
        Random generator = new Random();
        int planesAmount = generator.nextInt(amount+1);
        int partnersAmount = amount - planesAmount;

        ArrayList<Plane> randomPlanes = new ArrayList<>();
        ArrayList<Partner> randomPartners = new ArrayList<>();

        while (planesAmount > 0) {
            randomPlanes.add(planeMarkerClass.getRandomPlane());
            planesAmount--;
        }

        while (partnersAmount > 0) {
            randomPartners.add(partnerMarkerClass.getRandomPartner());
            partnersAmount--;
        }

        return new Pair<>(randomPlanes, randomPartners);

    }

    public PartnerMarkerClass getPartners() {
        return partnerMarkerClass;
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
        }

        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }




}
