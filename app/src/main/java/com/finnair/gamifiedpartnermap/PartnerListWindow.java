package com.finnair.gamifiedpartnermap;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by Otto on 5.2.2018.
 */

public class PartnerListWindow extends PopupWindow {

    //Reference to FirebaseDatabase
    private DatabaseReference databaseReference;
    private ArrayList<String> partners;                              //Arraylist which we populate with partner names.
    private HashMap<String, String> partnersUnderBusiness;           //HashMap which keys are fields_of_business and values are partner names.
    private HashMap<String, Pair<Double, Double>> partnerLocations;  //HashMap which keys are partner names and values are partner locations.
    private ArrayList<String> testList;          //Just a very long test list

    private Activity mActivity;

    private RelativeLayout myLinearLayout;
    private ListView myListView;
    private ArrayAdapter<String> adapter;
    private PopupWindow popup;

    public PartnerListWindow(Activity act){

        testList = new ArrayList<String>();
        for(int i = 0; i<100; i++) {
            testList.add("Peruna");
        }
        mActivity = act;
        partners = new ArrayList<String>();
        partnerLocations = new HashMap<>();
        partnersUnderBusiness = new HashMap<>();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        setData();

    }

    //This function is called every time we want to show popup window.
    public void createPopupWindow(){

        myLinearLayout = (RelativeLayout) mActivity.findViewById(R.id.popup_partner_window);

        LayoutInflater inf = mActivity.getLayoutInflater();
        final View myPopupView = inf.inflate(R.layout.partner_list_view, myLinearLayout);

        popup = new PopupWindow(mActivity);
        popup.setFocusable(true);
        popup.setContentView(myPopupView);

        popup.setBackgroundDrawable(new BitmapDrawable());
        popup.showAtLocation(mActivity.findViewById(R.id.main_content), Gravity.CENTER, 0, 0);
        popup.setWidth(200);
        popup.setHeight(400);

        myListView = (ListView) popup.getContentView().findViewById(R.id.popup_list_view);

        adapter = new ArrayAdapter<String>(mActivity, android.R.layout.simple_list_item_1, partners);
        myListView.setAdapter(adapter);

        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String clicked = ((TextView)view).getText().toString();
             /**   if(myMap != null) {
                    Pair<Double, Double> newLoc = partnerLocations.get(((TextView)view).getText().toString());
                    LatLng newLatLng = new LatLng(newLoc.first, newLoc.second);
                    myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, 15.0f));
                } else  {System.out.println("map was null");}   */
                System.out.println("Clicked: " + clicked + "LatLng: " + partnerLocations.get(clicked));
            }
        });


        Button closeButton = (Button) myPopupView.findViewById(R.id.popup_close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popup.dismiss();
            }
        });
    }

    public void setData(){

        DatabaseReference ref = databaseReference.child("locations");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapShot : dataSnapshot.getChildren()) {
                    String companyName = singleSnapShot.child("name").getValue().toString();
                    String business = singleSnapShot.child("field_of_business").getValue().toString();
                    Double lat = Double.parseDouble(singleSnapShot.child("lat").getValue().toString());
                    Double lng = Double.parseDouble(singleSnapShot.child("lng").getValue().toString());

                    partnersUnderBusiness.put(business, companyName);
                    partners.add(companyName);
                    partnerLocations.put(companyName, new Pair<Double, Double>(lat, lng));
                    System.out.println(companyName + "LatLng" + lat +  " : " + lng);
                }
                Collections.sort(partners);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


    public void printPartners(){
        for(String single : partners){
            System.out.println(single);
        }
    }
}
