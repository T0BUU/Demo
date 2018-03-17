package com.finnair.gamifiedpartnermap;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ResponseTypeValues;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import static com.finnair.gamifiedpartnermap.MainActivity.locationPermission;

/**
 * Created by Otto on 12.2.2018.
 */
/*
 * This class is responsible for instantiating the whole UI.
 */
public class MActivityLayout implements View.OnClickListener {

    private DrawerLayout drawerLayout;
    private PartnerListWindow partnerListWindow;  //Partner list popup window
    private MapsFragment mapFragment;
    private MainActivity mActivity;
    private FragmentManager fragmentManager;

    public MActivityLayout(){}

    public void createUI(Activity act, FragmentManager fm, HashMap<String, String> profileInfo){

        //Set activity, fragmentManager and create new MapsFragment.
        mActivity = (MainActivity) act;
        fragmentManager = fm;
        mapFragment = new MapsFragment();

        /* Set content view to MainActivity;
         * activity_main contains drawerLayout which has toolbar and FrameLayout(main_content) as its main view,
         * and user profile as its drawer view.  */
        mActivity.setContentView(R.layout.activity_main);
        drawerLayout = (DrawerLayout) mActivity.findViewById(R.id.drawer_layout);

        //Navigation buttons
        Button loginButton = (Button) mActivity.findViewById(R.id.button_login);
        ImageButton settingsButton = (ImageButton) mActivity.findViewById(R.id.button_settings);
        Button partnersButton = (Button) mActivity.findViewById(R.id.toolbar_partners_button);
        Button drawerToggle = (Button) mActivity.findViewById(R.id.open_drawer_button);

        //Set click listeners to all buttons
        loginButton.setOnClickListener(this);
        settingsButton.setOnClickListener(this);
        partnersButton.setOnClickListener(this);
        drawerToggle.setOnClickListener(this);

        //Insert mapFragment to main_content FrameLayout
        fragmentManager.beginTransaction()
                .replace(R.id.main_content, mapFragment)
                .commit();

        try {
            TextView profileNameField = mActivity.findViewById(R.id.nav_profile_name);
            profileNameField.setText(profileInfo.get("id"));
            loginButton.setText("Logout");
        }
        catch (java.lang.NullPointerException e) {

        }

        //Initialize new partnerListWindow, pass MainActivity and mapFragment as parameters.
        partnerListWindow = new PartnerListWindow(mActivity, mapFragment);


    }

    //Handle button click events here.
    @Override
    public void onClick(View view){
        switch(view.getId()){
            case R.id.button_settings:
                fragmentManager.beginTransaction()
                        .replace(R.id.main_content, mapFragment)
                        .commit();
                drawerLayout.closeDrawer(GravityCompat.START);
                break;
            case R.id.button_login:
                drawerLayout.closeDrawer(GravityCompat.START);
                Button login = (Button) view;
                if(login.getText().equals("Login")){
                    Log.d("Login", "Hurraa");
                    login.setText("Logout");
                   mActivity.makeAuthorizationRequest();
                }else if(login.getText().equals("Logout")){
                    login.setText("Login");
                    mActivity.logout();
                }
                break;
            case R.id.finnair_logo_button:
                fragmentManager.beginTransaction()
                        .replace(R.id.main_content, mapFragment)
                        .commit();
                break;
            case R.id.toolbar_partners_button:                   //Open partner popup window.
                partnerListWindow.showPopupWindow();
                break;
            case R.id.open_drawer_button:                        //Open drawer, (Profile icon)
                drawerLayout.openDrawer(GravityCompat.START);
            default: break;
        }
    }



    //MainActivity calls this method after partnerMarkerClass has fetched data.
    //Calls partnerListWindows method createPopupWindow which sets data and recreates the window.
    public void notifyPartnerDataChanged(){
        partnerListWindow.createPopupWindow();
    }

    public ConcurrentHashMap<String, HashSet<String>> getPlaneCollection() {
        return mapFragment.getPlaneCollection();
    }

    public ConcurrentHashMap<String, HashSet<String>> getPartnerCollection() {
        return mapFragment.getPartnerCollection();
    }
}
