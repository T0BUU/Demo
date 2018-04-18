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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import java.util.concurrent.ConcurrentHashMap;


public class PartnerMarkerClass {

    Integer screenWidth;
    Integer screenHeight;
    MainActivity activity;
    ConcurrentHashMap<String, Partner> partnerHashMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, HashSet<String>> collectionHashMap;

    public static String USER_DATA_LOCATION_PARTNERS = "myPartners";
    private List<Partner> showingPartners;
    GoogleMap mMap;
    private static final String TAG = PartnerMarkerClass.class.getSimpleName();


    public PartnerMarkerClass(Activity activity, GoogleMap mMap) {

        // Activity is for example MapsActivity
        this.activity = (MainActivity) activity;
        this.mMap = mMap;
        // Get window size for scaling Marker image size:
        Display display = this.activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        this.screenWidth = size.x;
        this.screenHeight = size.y;

        readCollectedPartners(activity);
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

                    addOneMarkerOnMap(lat, lng, companyName, business, description, address, clusterManager, markerRenderer, activity.getActiveChallenges());

                }

                //At after fetching data add all partners to showingPartners list.
                showingPartners = new ArrayList<>(partnerHashMap.values());

                //Notify MainActivity that data is ready, it forwards notify to partnerListWindow
                if(partnerHashMap != null) {
                    MainActivity act = (MainActivity) activity;
                    act.notifyDataChange();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled", databaseError.toException());
            }

        });

    }

    /*
     * This updates showingMarkers list based on given parameter field_of_business.
     * If showingPartners contain partners under field_of_business, this method removes them from list.
     * If showingPartners doesn't contain that field_of_business, it adds those to list.
     * Returns updated list.
     */
    public List<Partner> filterFieldOfBusiness(String fob){
        for(Partner partner : partnerHashMap.values()) {
            if (partner.getFieldOfBusiness().equals(fob)) {
                if(showingPartners.contains(partner)){
                    showingPartners.remove(partner);
                } else {
                    showingPartners.add(partner);
                }
            }
        }
        return showingPartners;

    }


    public ConcurrentHashMap<String, HashSet<String>> getCollection() {
        return this.collectionHashMap;
    }

    public Partner getRandomPartner() {
        Random generator = new Random();
        ArrayList<Partner> entries = new ArrayList();

        entries.addAll(partnerHashMap.values());

        return entries.get(generator.nextInt(entries.size()));
    }


    public void readCollectedPartners(Context context) {

        ConcurrentHashMap<String, HashSet<String>> result = new ConcurrentHashMap<>();

        try {
            InputStream inputStream = context.openFileInput(USER_DATA_LOCATION_PARTNERS);

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
                                  String description, String address, ClusterManager clusterManager, MarkerRenderer markerRenderer,
                                  ArrayList<Challenge> challenges){

        Partner newPartner = new Partner(activity);
        newPartner.setPosition(latitude, longitude);
        newPartner.setFieldOfBusiness(business);
        newPartner.setSnippet(description);
        newPartner.setAddress(address);
        newPartner.setID(companyName);
        newPartner.setMarkerOptions();
        newPartner.setMarkerImage(screenWidth);
        newPartner.setBonusMarker(mMap);

        for (Challenge c : challenges) {
            if (c == null) break;
            if (c.isRelated(newPartner)){
                newPartner.addRelatedChallenge(c);
            }
        }

        partnerHashMap.put(newPartner.getID(), newPartner);
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

    //Getter for partnerHashMap.
    public ConcurrentHashMap<String, Partner> getPartnerHashMap() {
        if (partnerHashMap == null) {
            return null;
        } else {
            return this.partnerHashMap;
        }
    }


}
