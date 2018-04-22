package com.finnair.gamifiedpartnermap;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Display;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;




public class PlaneMarkerClass {
    Integer screenWidth;
    Integer screenHeight;
    MainActivity activity;
    private ConcurrentHashMap<String, Plane> planeHashMap; // to avoid ConcurrentModificationException with HashMap
    private ConcurrentHashMap<String, HashSet<String>> collectionHashMap; // to avoid ConcurrentModificationException with HashMap
    private Location tempLocation = new Location("");
    GoogleMap mMap;

    private Location userLocation;
    public static String USER_DATA_LOCATION_PLANES = "myPlanes";

    private List<String> PLANE_TYPES = Arrays.asList("AIRBUS A350-900", "AIRBUS A330-300",
            "AIRBUS A321", "AIRBUS A321-231",
            "AIRBUS A320", "AIRBUS A319",
            "AIRBUS A319", "EMBRAER 190",
            "ATR 72-212A");
    private com.finnair.gamifiedpartnermap.ClusterManager clusterManager;
    private MarkerRenderer markerRenderer;

    OpenSkyApi openSkyApi;
    OpenSkyStates openSkyStates;


    public PlaneMarkerClass(Activity activity, GoogleMap mMap, Location userLocation, final ClusterManager clusterManager, final MarkerRenderer markerRenderer) {
        this.clusterManager = clusterManager;
        this.markerRenderer = markerRenderer;
        this.userLocation = userLocation;
        this.activity = (MainActivity) activity; // Activity is for example MapsActivity
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

    public void addPlaneOnMap(String planeID, Double latitude, Double longitude, Double directionDegree, String planeType, ArrayList<Challenge> challenges){

        if ( !this.planeHashMap.containsKey(planeID) ){
            Plane newPlane = new Plane(activity);
            newPlane.setPosition(latitude, longitude);
            newPlane.setHeading(directionDegree);
            newPlane.setID(planeID);
            newPlane.setMarkerOptions();
            newPlane.setMarkerImage(screenWidth);
            newPlane.setPlaneType( planeType );
            newPlane.setMarkerImage();
            newPlane.setBonusMarker(mMap);

            planeHashMap.put(newPlane.getID(), newPlane);
            //System.out.println("adding plane, id: " + newPlane.getID() + "icao: " + newPlane.getIcao24());
            this.clusterManager.addItem(newPlane);

            for (Challenge c : challenges) {
                if (c == null) break;
                if (c.isRelated(newPlane)) {
                    Log.d("crap2", c.getDescription() );
                    newPlane.addRelatedChallenge(c);
                }
            }

            readCollectedPlanes(activity);
        }
    }



    public void removePlaneFromMap(String planeID){
        getPlaneByID(planeID).setBonusMarkerVisible(false);
        this.clusterManager.removeItem(getPlaneByID(planeID));
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
            Log.d("POOP", "Plane locations updated.");

            ArrayList<String> callSigns = new ArrayList<>();

            Collection<OpenSkyStateVector> states = openSkyStates.getStates();

            if (states != null){
                for(OpenSkyStateVector openSkyStateVector : states){
                    updatePlanesWithStateVectors(openSkyStateVector);
                    callSigns.add(openSkyStateVector.getCallsign());
                }
                removePlanesWhichHaveLanded(callSigns);
            }

        }
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
                if (distanceKM > 300) {
                    // An existing planeMarker has flown too far:
                    removePlaneFromMap(callSign);
                } else {
                    // Animate the movement of an existing plane:
                    if (heading != null) {

                        if ( planeHashMap.get(callSign).isWithinReach(userLocation) ){
                            planeHashMap.get(callSign).setStatusDistanceClose(true);
                            planeHashMap.get(callSign).setMarkerImage();
                            markerRenderer.setMarkerImage(planeHashMap.get(callSign));
                            markerRenderer.animateMarkerPulse(planeHashMap.get(callSign));
                        } else {
                            planeHashMap.get(callSign).setStatusDistanceClose(false);
                            planeHashMap.get(callSign).setMarkerImage();
                            markerRenderer.setMarkerImage(planeHashMap.get(callSign));
                        }
                        markerRenderer.animatePlaneFlight(planeHashMap.get(callSign), new LatLng(latitude, longitude), heading - 45);
                    }
                }
            }

            else if (distanceKM < 300) {
                // Add a new plane Marker on the map:
                if (heading != null) {
                    addPlaneOnMap(openSkyStateVector.getCallsign(), openSkyStateVector.getLatitude(),
                            openSkyStateVector.getLongitude(), openSkyStateVector.getHeading() - 90,
                            PLANE_TYPES.get(new Random().nextInt(PLANE_TYPES.size())),
                            activity.getActiveChallenges());

                    addPlaneOnMap(openSkyStateVector.getCallsign(), openSkyStateVector.getLatitude(),
                            openSkyStateVector.getLongitude(), openSkyStateVector.getHeading() - 45,
                            PLANE_TYPES.get(new Random().nextInt(PLANE_TYPES.size())),
                            activity.getActiveChallenges());

                    planeHashMap.get(callSign).setPlaneMiscellaneousInformation(
                            openSkyStateVector.getGeoAltitude(),
                            openSkyStateVector.getVelocity(),
                            openSkyStateVector.getIcao24(),
                            openSkyStateVector.getOriginCountry());

                } else {
                    addPlaneOnMap(openSkyStateVector.getCallsign(), openSkyStateVector.getLatitude(),
                            openSkyStateVector.getLongitude(),
                            0.0, PLANE_TYPES.get(new Random().nextInt(PLANE_TYPES.size())),
                            activity.getActiveChallenges());
                }
            }
        }
    }


    public ConcurrentHashMap<String, HashSet<String>> getCollection() {
        return this.collectionHashMap;
    }

    public Plane getRandomPlane() {
        Random generator = new Random();
        ArrayList<Plane> entries = new ArrayList();

        entries.addAll(planeHashMap.values());

        Log.d("Random Plane", "" + generator.nextInt(entries.size()));

        return entries.get(generator.nextInt(entries.size()));
    }


    public void readCollectedPlanes(Context context) {

        ConcurrentHashMap<String, HashSet<String>> result = new ConcurrentHashMap<>();

        try {
            InputStream inputStream = context.openFileInput(USER_DATA_LOCATION_PLANES);

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


}

