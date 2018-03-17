package com.finnair.gamifiedpartnermap;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
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
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Otto on 5.2.2018.
 */

public class PartnerListWindow extends PopupWindow {

    private List<String> partners;                                    //Arraylist which we populate with partner names.
    private HashMap<String, List<String>> partnersUnderBusiness;      //HashMap which keys are fields_of_business and values are partner names.
    private List<String> fieldsOfBusinesses;                          //List containing all fields_of_business.

    private Activity mActivity;
    private MapsFragment myMap;
    private PartnerMarkerClass partnerMarkerClass;

    private PopupWindow popup;


    public PartnerListWindow(Activity act, MapsFragment mMap){

        mActivity = act;
        myMap = mMap;
    }

    /*
     * This function is called from MActivityLayout on partners button click.
     * Shows popupwindow on top of mainLayout at the center of screen.
     */
    public void showPopupWindow() {
        popup.showAtLocation(mActivity.findViewById(R.id.main_content), Gravity.CENTER, 0, 0);
    }

    /*
     * Creates popup window, this method gets called when partnerMarkerClass fetches data, so this must update too.
     * First calls setdata() method which gets data from partnerMarkerClass and initializes required Lists etc.
     * Then create new PopupWindow and inflate linearlayout containing expandable list view.
     * After that set values to popupwindow (width, height, contentview,...).
     * At last finds expandablelistview, initializes and sets its adapter and sets onClickListeners for it.
     */
    public void createPopupWindow(){

        this.setData();
        popup = new PopupWindow(mActivity);

        RelativeLayout myLinearLayout = (RelativeLayout) mActivity.findViewById(R.id.popup_partner_window);

        LayoutInflater inf = mActivity.getLayoutInflater();
        final View myPopupView = inf.inflate(R.layout.partner_list_view, myLinearLayout);

        Display display = mActivity.getWindowManager().getDefaultDisplay();
        int width = (int)((double)display.getWidth() * 0.8);
        int height = (int)((double)display.getHeight() * 0.8);
        popup.setWidth(width);
        popup.setHeight(height);
        popup.setFocusable(true);
        popup.setContentView(myPopupView);
        popup.setBackgroundDrawable(new BitmapDrawable());

        ExpandableListView myExpandableList = (ExpandableListView) myPopupView.findViewById(R.id.popup_expandable_list_view);
        if(myExpandableList == null) {
            System.out.println("!!!null expandableList!!!");
        }
        ExpandableListAdapter expandingListAdapter = new PartnerPopupExpandableListAdapter(mActivity, partnersUnderBusiness, partnerMarkerClass, myMap);
        myExpandableList.setAdapter(expandingListAdapter);

        /*myExpandableList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                expandableListView.expandGroup(i);
                return false;
            }
        });*/

        myExpandableList.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int i) {

            }
        });

        myExpandableList.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int i) {

            }
        });

        myExpandableList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
                //Partner p = partnerMarkerClass.getPartnerByID(partners.get(i1));
                //myMap.moveCameraToPartner(p);
                return false;
            }
        });
    }

    /*
     * This method gets partnerHashMap from partnerMarkerClass,
     * parses received data to partnersUnderBusiness HashMap<field_of_business, partner.name>
     * and partners List<partner.name>
     */
    public void setData(){
        partnerMarkerClass = myMap.getPartners();
        ConcurrentHashMap<String, Partner> partnerHashMap = partnerMarkerClass.getPartnerHashMap();
        partners = new ArrayList<>();

        partnersUnderBusiness = new HashMap<>();
        ArrayList<String> partnerNames = new ArrayList<>(partnerHashMap.keySet());
        for(int i = 0; i < partnerNames.size(); i++){
            Partner current = partnerHashMap.get(partnerNames.get(i));
            System.out.println("current partner: " + current.getID());
            if(!partnersUnderBusiness.containsKey(current.getFieldOfBusiness())){
                ArrayList<String> newArray = new ArrayList<>();
                newArray.add(current.getID());
                partnersUnderBusiness.put(current.getFieldOfBusiness(), newArray);
            } else if(current.getID() != null){
                List<String> addArray = partnersUnderBusiness.get(current.getFieldOfBusiness());
                addArray.add(current.getID());
                partnersUnderBusiness.put(current.getFieldOfBusiness(), addArray);
            } else {
                System.out.println("----current partner was null");
            }
        }
        List<String> pUBKeys = new ArrayList<>(partnersUnderBusiness.keySet());
        for(int j = 0; j < pUBKeys.size(); j++){
            List<String> temp = partnersUnderBusiness.get(pUBKeys.get(j));
            partners.addAll(temp);
        }
    }

}
