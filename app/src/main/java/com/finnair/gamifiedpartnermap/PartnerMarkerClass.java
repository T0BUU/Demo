package com.finnair.gamifiedpartnermap;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.ClusterManager;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import static com.finnair.gamifiedpartnermap.PlaneMarkerClass.USER_DATA_LOCATION;

/**
 * Created by noctuaPC on 5.12.2017.
 */

public class PartnerMarkerClass {

    Integer screenWidth;
    Integer screenHeight;
    Activity activity;
    ConcurrentHashMap<String, Partner> partnerHashMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Integer> collectionHashMap;

    GoogleMap mMap;
    private static final String TAG = PartnerMarkerClass.class.getSimpleName();


    public PartnerMarkerClass(Activity activity, GoogleMap mMap) {

        // Activity is for example MapsActivity
        this.activity = activity;
        this.mMap = mMap;
        // Get window size for scaling Marker image size:
        Display display = this.activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        this.screenWidth = size.x;
        this.screenHeight = size.y;

    }

    public void fetchFromFirebase(final ClusterManager clusterManager, final MarkerRenderer markerRenderer){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
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

                    addOneMarkerOnMap(lat, lng, companyName, business, description, address, clusterManager, markerRenderer);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled", databaseError.toException());
            }

        });

    }



    public void savePartner(Context context, Plane saveMe){
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



    public void addOneMarkerOnMap(Double latitude, Double longitude, String companyName, String business,
                                  String description, String address, ClusterManager clusterManager, MarkerRenderer markerRenderer){

        Partner newPartner = new Partner(activity);
        newPartner.setPosition(latitude, longitude);
        newPartner.setFieldOfBusiness(business);
        newPartner.setSnippet(description);
        newPartner.setAddress(address);
        newPartner.setID(companyName);
        newPartner.setCircleOptions();
        newPartner.setMarkerOptions();
        newPartner.setMarkerImage(screenWidth);

        partnerHashMap.put(newPartner.getID(), newPartner);
        markerRenderer.setIcon( newPartner.getID(), newPartner.getIcon() ); // Here you can set the Marker Image for the specific Partner
        clusterManager.addItem(newPartner);
    }

    public Partner getPartnerByID(String partnerID){
        return this.partnerHashMap.get(partnerID);
    }

    public boolean containsMarker(Marker partnerMarker){
        if (partnerMarker == null) return  false;
        if (partnerMarker.getTitle() == null) return false;
        return partnerHashMap.containsKey(partnerMarker.getTitle());
    }



}
