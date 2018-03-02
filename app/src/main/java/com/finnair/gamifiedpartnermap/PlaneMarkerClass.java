package com.finnair.gamifiedpartnermap;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Display;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.ClusterManager;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Math.abs;
import static java.lang.Math.pow;


/**
 * Created by huzla on 25.1.2018.
 */

public class PlaneMarkerClass {
    Integer screenWidth;
    Integer screenHeight;
    Activity activity;
    private ConcurrentHashMap<String, Plane> planeHashMap; // to avoid ConcurrentModificationException with HashMap
    private ConcurrentHashMap<String, HashSet<String>> collectionHashMap; // to avoid ConcurrentModificationException with HashMap
    private Location tempLocation = new Location("");
    GoogleMap mMap;

    private Location userLocation;
    public static String USER_DATA_LOCATION = "myCollection";

    private List<String> PLANE_TYPES = Arrays.asList("AIRBUS A350-900", "AIRBUS A330-300",
            "AIRBUS A321", "AIRBUS A321-231",
            "AIRBUS A320", "AIRBUS A319",
            "AIRBUS A319", "EMBRAER 190",
            "ATR 72-212A");

    OpenSkyApi openSkyApi;
    OpenSkyStates openSkyStates;


    public PlaneMarkerClass(Activity activity, GoogleMap mMap, Location userLocation) {
        this.userLocation = userLocation;
        this.activity = activity; // Activity is for example MapsActivity
        this.mMap = mMap;
        // Get window size for scaling Marker image size:
        Display display = this.activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        this.screenWidth = size.x;
        this.screenHeight = size.y;

        planeHashMap = new ConcurrentHashMap<>();
        collectionHashMap = new ConcurrentHashMap<>();

        openSkyApi = new OpenSkyApi("AaltoSoftwareProject", "softaprojekti");

        readCollectedPlanes(activity);

    }

    public void refreshOpenSkyPlanes(){
        new AsyncOpenSkyDownload().execute();
    }

    public Plane getPlaneByID(String planeID){
        return this.planeHashMap.get(planeID);
    }

    public boolean containsMarker(Marker planeMarker){
        if (planeHashMap == null) return false;
        if (planeMarker.getTitle() == null) return false;
        return planeHashMap.containsKey(planeMarker.getTitle());
    }

    public void addPlaneOnMap(String planeID, Double latitude, Double longitude, Double directionDegree, String planeType){

        if ( !this.planeHashMap.containsKey(planeID) ){
            Plane newPlane = new Plane(activity);
            newPlane.setPosition(latitude, longitude);
            newPlane.setHeading(directionDegree);
            newPlane.setID(planeID);
            newPlane.setCircleOptions();
            newPlane.setMarkerOptions();
            newPlane.setMarkerImage(screenWidth);
            newPlane.setMarker( this.mMap.addMarker( newPlane.getMarkerOptions() ) );
            newPlane.setCircle( this.mMap.addCircle( newPlane.getCircleOptions() ) );
            newPlane.setRadarArcPolyLine( this.mMap.addPolyline( newPlane.getRadarPolyLineOptions() ) );
            newPlane.setPlaneType( planeType );
            newPlane.showRadarArcPolyline(false);
            planeHashMap.put(newPlane.getID(), newPlane);

        }
    }



    public void removePlaneFromMap(String planeID){
        Plane plane = this.planeHashMap.get(planeID);
        plane.deleteRadarPulseAnimation();
        plane.deleteRadarArcAnimation();
        plane.deleteCircle();
        plane.deleteMarker();
    }

    public void removePlanesWhichHaveLanded(ArrayList<String> planesInAir){
        Set<String> keys = this.planeHashMap.keySet();
        for (String planeID : keys){
            if ( !planesInAir.contains(planeID) ){
                removePlaneFromMap(planeID);
            }
        }
    }

    public void zoomListener(float zoom) {

        for ( Plane plane : this.planeHashMap.values()) {
            //zoom = 0 the entire world and zoom 20 is the closest the camera gets.
            //zooming one level (0 -> 1 for example) halves the size of one map tile => zooming grows O(pow(2, zoom))
            if (plane.getCircleRadius() < pow(2, 20-zoom)) plane.setCircleVisible(true);                                // HUOM !!!! KORJAA setCircleVisible(false) ///////////////////////////////////////
            else plane.setCircleVisible(true);
        }
    }


    private class AsyncOpenSkyDownload extends AsyncTask<String, Integer, String> {
        // https://stackoverflow.com/questions/9671546/asynctask-android-example
        @Override
        protected void onPreExecute(){}

        @Override
        protected void onProgressUpdate(Integer... values){} // For possible future use

        @Override
        protected String doInBackground(String... params){

            int i = 0;
            publishProgress(i);

            try {
                openSkyStates = openSkyApi.getStates(0, null);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);
            Log.d("POOP", "Finished execution.");
            // Collection<OpenSkyStateVector> states

            ArrayList<String> callSigns = new ArrayList<>();

            for(OpenSkyStateVector openSkyStateVector : openSkyStates.getStates()){
                updatePlanesWithStateVectors(openSkyStateVector);
                callSigns.add(openSkyStateVector.getCallsign());
            }
            removePlanesWhichHaveLanded(callSigns);
        }
    }

    public void savePlane(Context context, Plane saveMe){
        // All apps (root or not) have a default data directory, which is /data/data/<package_name>

        try {
            collectionHashMap.get(saveMe.getPlaneType()).add(saveMe.getOriginCountry());
        }
        catch (java.lang.NullPointerException nil) {
            HashSet<String> addMe = new HashSet<>();
            addMe.add(saveMe.getOriginCountry());

            collectionHashMap.put(saveMe.getPlaneType(), addMe);
        }

        Log.d("Plane saving: ", collectionHashMap.toString());
    }


    private String formatPlanes() {
        String result = "";

        for (String planeType : collectionHashMap.keySet()) {
            Iterator<String> row = collectionHashMap.get(planeType).iterator();

            result += planeType;

            while (row.hasNext()) {
                result += String.format("#%s", row.next());
            }

            result += "\n";

        }
        return result;
    }

    public void savePlanes(Context context){

        String result = formatPlanes();

        try {
            FileOutputStream outputStream = context.openFileOutput(USER_DATA_LOCATION, Context.MODE_PRIVATE);
            outputStream.write(result.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("Plane Saving", result);
    }

    public ConcurrentHashMap<String, HashSet<String>> getCollection() {
        return this.collectionHashMap;
    }

    public Plane getRandomPlane() {
        Random generator = new Random();
        ArrayList<Plane> entries = new ArrayList();

        entries.addAll(planeHashMap.values());

        return entries.get(generator.nextInt(entries.size()));
    }


    public void readCollectedPlanes(Context context) {

        ConcurrentHashMap<String, HashSet<String>> result = new ConcurrentHashMap<>();

        try {
            InputStream inputStream = context.openFileInput(USER_DATA_LOCATION);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";

                while ( (receiveString = bufferedReader.readLine()) != null ) {

                    String[] firstSplit = receiveString.split("#");
                   HashSet<String> planes = new HashSet<>();

                    for (int i = 1; i < firstSplit.length; ++i) {
                        planes.add(firstSplit[i]);
                    }

                    result.put(firstSplit[0], planes);

                }

                inputStream.close();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        collectionHashMap = result;
    }

    private void updatePlanesWithStateVectors(OpenSkyStateVector openSkyStateVector) {
        String callSign = openSkyStateVector.getCallsign();
        Double latitude = openSkyStateVector.getLatitude();
        Double longitude = openSkyStateVector.getLongitude();
        Double heading = openSkyStateVector.getHeading();


        if (callSign != "" && latitude != null && longitude != null) {

            tempLocation.setLatitude(latitude);
            tempLocation.setLongitude(longitude);

            float distanceKM = userLocation.distanceTo(tempLocation) / 1000;

            if (planeHashMap.containsKey(callSign)) {
                if (distanceKM > 100) {
                    // An existing planeMarker has flown too far:
                    removePlaneFromMap(callSign);
                } else {
                    // Animate the movement of an existing plane:
                    if (heading != null)
                        planeHashMap.get(callSign).animatePlaneMarker(new LatLng(latitude, longitude), Double.valueOf(heading).floatValue() - 90, userLocation);
                    else
                        planeHashMap.get(callSign).animatePlaneMarker(new LatLng(latitude, longitude), 0, userLocation);
                }
            }

            else if (distanceKM < 100) {
                // Add a new plane Marker on the map:
                if (heading != null) {
                    addPlaneOnMap(openSkyStateVector.getCallsign(), openSkyStateVector.getLatitude(), openSkyStateVector.getLongitude(), openSkyStateVector.getHeading() - 90, PLANE_TYPES.get(new Random().nextInt(PLANE_TYPES.size())));
                    planeHashMap.get(callSign).setPlaneMiscellaneousInformation(
                            openSkyStateVector.getGeoAltitude(),
                            openSkyStateVector.getVelocity(),
                            openSkyStateVector.getIcao24(),
                            openSkyStateVector.getOriginCountry());
                } else {
                    addPlaneOnMap(openSkyStateVector.getCallsign(), openSkyStateVector.getLatitude(), openSkyStateVector.getLongitude(), 0.0, PLANE_TYPES.get(new Random().nextInt(PLANE_TYPES.size())));
                }

            }

        }
    }
}

