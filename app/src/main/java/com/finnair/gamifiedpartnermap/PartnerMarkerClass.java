package com.finnair.gamifiedpartnermap;

import android.app.Activity;
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

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by noctuaPC on 5.12.2017.
 */

public class PartnerMarkerClass {

    Integer screenWidth;
    Integer screenHeight;
    Activity activity;
    ConcurrentHashMap<String, Partner> partnerHashMap = new ConcurrentHashMap<>();
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
