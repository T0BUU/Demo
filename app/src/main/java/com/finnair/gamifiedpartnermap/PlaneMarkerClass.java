package com.finnair.gamifiedpartnermap;

import android.app.Activity;
import android.graphics.Point;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Display;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;




public class PlaneMarkerClass {
    Integer screenWidth;
    Integer screenHeight;
    Activity activity;
    private ConcurrentHashMap<String, Plane> planeHashMap; // to avoid ConcurrentModificationException with HashMap
    private Location tempLocation = new Location("");
    GoogleMap mMap;

    private Location userLocation;
    private com.finnair.gamifiedpartnermap.ClusterManager clusterManager;
    private MarkerRenderer markerRenderer;

    OpenSkyApi openSkyApi;
    OpenSkyStates openSkyStates;


    public PlaneMarkerClass(Activity activity, GoogleMap mMap, Location userLocation, final ClusterManager clusterManager, final MarkerRenderer markerRenderer) {
        this.clusterManager = clusterManager;
        this.markerRenderer = markerRenderer;
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
        if (planeHashMap == null) return false;
        if (planeMarker.getTitle() == null) return false;
        return planeHashMap.containsKey(planeMarker.getTitle());
    }

    public void addPlaneOnMap(String planeID, Double latitude, Double longitude, Double directionDegree){

        if ( !this.planeHashMap.containsKey(planeID) ){
            Plane newPlane = new Plane(activity);
            newPlane.setPosition(latitude, longitude);
            newPlane.setHeading(directionDegree);
            newPlane.setID(planeID);
            newPlane.setMarkerOptions();
            newPlane.setMarkerImage("not near/collected");
            planeHashMap.put(newPlane.getID(), newPlane);
            this.clusterManager.addItem(newPlane);


        }
    }

    public void removePlaneFromMap(String planeID){
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
                        markerRenderer.animateMarkerMovement(planeHashMap.get(callSign), new LatLng(latitude, longitude), heading - 45);
                        if ( planeHashMap.get(callSign).isWithinReach(userLocation) ){
                            planeHashMap.get(callSign).setMarkerImage("near");
                            markerRenderer.setMarkerImage(planeHashMap.get(callSign));
                        } else {
                            planeHashMap.get(callSign).setMarkerImage("not near/collected");
                            markerRenderer.setMarkerImage(planeHashMap.get(callSign));
                        }
                    }
                }
            }

            else if (distanceKM < 300) {
                // Add a new plane Marker on the map:
                if (heading != null) {
                    addPlaneOnMap(openSkyStateVector.getCallsign(), openSkyStateVector.getLatitude(), openSkyStateVector.getLongitude(), openSkyStateVector.getHeading() - 45);
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

