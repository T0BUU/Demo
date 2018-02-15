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


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
    private ConcurrentHashMap<String, HashMap<String, String>> collectionHashMap; // to avoid ConcurrentModificationException with HashMap
    private Location tempLocation = new Location("");
    GoogleMap mMap;

    private final Float rotationMultiplier = -100.0f;
    private Location userLocation;
    private String USER_DATA_LOCATION = "myCollection";

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

        openSkyApi = new OpenSkyApi("AaltoSoftwareProject", "softaprojekti");

    }

    public void refreshOpenSkyPlanes(){
        new AsyncOpenSkyDownload().execute();
    }

    public Plane getPlaneByID(String planeID){
        return this.planeHashMap.get(planeID);
    }

    public boolean containsMarker(Marker planeMarker){
        return planeHashMap.containsKey(planeMarker.getTitle());
    }

    public void addPlaneOnMap(String planeID, Double latitude, Double longitude, Double directionDegree){

        if ( !this.planeHashMap.containsKey(planeID) ){
            Plane newPlane = new Plane(activity);
            newPlane.setPlanePosition(latitude, longitude);
            newPlane.setHeading(directionDegree);
            newPlane.setPlaneID(planeID);
            newPlane.setPlaneCircleOptions();
            newPlane.setPlaneMarkerOptions(this.screenWidth);
            newPlane.setPlaneMarker( this.mMap.addMarker( newPlane.getPlaneMarkerOptions() ) );
            newPlane.setPlaneCircle( this.mMap.addCircle( newPlane.getPlaneCircleOptions() ) );
            newPlane.setRadarArcPolyLine( this.mMap.addPolyline( newPlane.getRadarPolyLineOptions() ) );
            newPlane.showRadarArcPolyline(false);
            planeHashMap.put(planeID, newPlane);
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

        for (Plane plane : this.planeHashMap.values()) {
            //zoom = 0 the entire world and zoom 20 is the closest the camera gets.
            //zooming one level (0 -> 1 for example) halves the size of one map tile => zooming grows O(pow(2, zoom))
            if (plane.getCircleRadius() < pow(2, 20 - zoom))
                plane.setCircleVisible(true);                                // HUOM !!!! KORJAA setCircleVisible(false) ///////////////////////////////////////
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

        collectionHashMap.get(saveMe.getPlaneID().substring(0,0)).put(saveMe.getPlaneID(), saveMe.getOriginCountry());
    }

    public void savePlanes(Context context, Plane saveMe){
        // All apps (root or not) have a default data directory, which is /data/data/<package_name>
        String earlierText = readCollectedPlanes(context);
        String text = saveMe.getPlaneID();
        String string = earlierText + " " + text;

        try {
            FileOutputStream outputStream = context.openFileOutput(USER_DATA_LOCATION, Context.MODE_PRIVATE);
            outputStream.write(string.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String readCollectedPlanes(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput(USER_DATA_LOCATION);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        ((MainActivity) this.activity).setPlanesListing(ret);
        return ret;
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

            if (distanceKM < 100) {
                // Add a new plane Marker on the map:
                if (heading != null) {
                    addPlaneOnMap(openSkyStateVector.getCallsign(), openSkyStateVector.getLatitude(), openSkyStateVector.getLongitude(), openSkyStateVector.getHeading() - 90);
                    planeHashMap.get(callSign).setPlaneMiscellaneousInformation(
                            openSkyStateVector.getGeoAltitude(),
                            openSkyStateVector.getVelocity(),
                            openSkyStateVector.getIcao24(),
                            openSkyStateVector.getOriginCountry());
                } else {
                    addPlaneOnMap(openSkyStateVector.getCallsign(), openSkyStateVector.getLatitude(), openSkyStateVector.getLongitude(), 0.0);
                }

            }

        }
    }
}

