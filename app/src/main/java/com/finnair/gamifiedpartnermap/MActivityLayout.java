package com.finnair.gamifiedpartnermap;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
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
import java.util.zip.Inflater;

import static com.finnair.gamifiedpartnermap.MainActivity.locationPermission;

/**
 * Created by Otto on 12.2.2018.
 */
/*
 * This class is responsible for instantiating the whole UI.
 */
public class MActivityLayout implements View.OnClickListener {

    private DrawerLayout drawerLayout;            //User profile view in drawerlayout.
    private PartnerListWindow partnerListWindow;  //Partner list popup window
    private MapsFragment mapFragment;
    private MainActivity mActivity;
    private FragmentManager fragmentManager;
    private String logInButtonText;
    private int TEXT_NORMAL_COLOR;
    private boolean partnerDataReady = false;     //Boolean value to check if partnerdata is ready

    private Button loginButton;
    private Button partnersButton;
    private Button drawerToggle;
    private Button challengesToggle;
    private Button registrationButton;

    public MActivityLayout(){}

    public void createUI(Activity act, FragmentManager fm, HashMap<String, String> profileInfo){

        //Set activity, fragmentManager and create new MapsFragment.
        mActivity = (MainActivity) act;
        fragmentManager = fm;
        mapFragment = new MapsFragment();

        logInButtonText = mActivity.getString(R.string.button_login);

        /* Set content view to MainActivity;
         * activity_main contains drawerLayout which has toolbar and FrameLayout(main_content) as its main view,
         * and user profile as its drawer view.  */
        mActivity.setContentView(R.layout.activity_main);
        drawerLayout = (DrawerLayout) mActivity.findViewById(R.id.drawer_layout);


        //Navigation buttons
        loginButton = (Button) mActivity.findViewById(R.id.button_bottom);
        partnersButton = (Button) mActivity.findViewById(R.id.toolbar_partners_button);
        drawerToggle = (Button) mActivity.findViewById(R.id.open_drawer_button);
        challengesToggle = (Button) mActivity.findViewById(R.id.challenges_button);
        registrationButton = (Button) mActivity.findViewById(R.id.drawer_sing_up_button);

        //Set click listeners to all buttons
        loginButton.setOnClickListener(this);
        partnersButton.setOnClickListener(this);
        drawerToggle.setOnClickListener(this);
        challengesToggle.setOnClickListener(this);
        registrationButton.setOnClickListener(this);

        //Set visuals

        TextView completedChallengesTextView = mActivity.findViewById(R.id.drawer_active_challenges_text);
        TEXT_NORMAL_COLOR = completedChallengesTextView.getCurrentTextColor();

        //Handle look of challenge related items
        setChallengeVisuals();



        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {

            }

            @Override
            public void onDrawerClosed(View drawerView) {
                LinearLayout profileBasis = mActivity.findViewById(R.id.profile_basis);
                LinearLayout challengesBasis = mActivity.findViewById(R.id.challenges_basis);

                profileBasis.setVisibility(View.VISIBLE);
                challengesBasis.setVisibility(View.GONE);

                loginButton.setText(logInButtonText);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
               setChallengeVisuals();
               setCollectionVisuals();
            }
        });



        //Insert mapFragment to main_content FrameLayout
        fragmentManager.beginTransaction()
                .replace(R.id.main_content, mapFragment)
                .commit();

        try {
            TextView profileNameField = mActivity.findViewById(R.id.nav_profile_name);
            String profileName = profileInfo.get("id");

            if (profileName.trim() != "") {
                LinearLayout registrationBox = mActivity.findViewById(R.id.drawer_not_logged_in_points_item);
                LinearLayout pointsBox = mActivity.findViewById(R.id.drawer_login_points_item);
                TextView profileLevel = mActivity.findViewById(R.id.membership_level);


                registrationBox.setVisibility(View.GONE);
                pointsBox.setVisibility(View.VISIBLE);
                profileLevel.setVisibility(View.VISIBLE);
                profileNameField.setText(profileInfo.get("id"));
                loginButton.setText(R.string.button_logout);
            }

        }
        catch (java.lang.NullPointerException e) {

        }

        //Initialize new partnerListWindow, pass MainActivity and mapFragment as parameters.
        partnerListWindow = new PartnerListWindow(mActivity, mapFragment);

    }


    private void setChallengeVisuals() {
        ImageView completedChallengesIndicator = mActivity.findViewById(R.id.toolbar).findViewById(R.id.completed_challenges_indicator);

        completedChallengesIndicator.setVisibility(View.INVISIBLE);

        int completedChallengesCounter = 0;

        for (Challenge c : mActivity.getActiveChallenges()) {
            if (c.isCompleted()) {
                mActivity.findViewById(R.id.toolbar).findViewById(R.id.completed_challenges_indicator).setVisibility(View.VISIBLE);
                ++completedChallengesCounter;
            }
        }

        TextView completedChallengesTextView = mActivity.findViewById(R.id.drawer_active_challenges_text);
        String completedChallengesText = String.format("You have %d completed challenge", completedChallengesCounter);

        if (completedChallengesCounter > 0) {
            if (completedChallengesCounter > 1) {
                completedChallengesText = String.format("%ss!", completedChallengesText);
            }
            else {
                completedChallengesText = String.format("%s!", completedChallengesText);
            }
            challengesToggle.getBackground().setColorFilter(Color.argb(0xFF,0xb4, 0xcb, 0x66), PorterDuff.Mode.SRC_IN);
            completedChallengesTextView.setTextColor(mActivity.getResources().getColor(R.color.nordicBlue));
        }
        else {
            completedChallengesText = String.format("%ss.", completedChallengesText);
            completedChallengesTextView.setTextColor(TEXT_NORMAL_COLOR);
            challengesToggle.getBackground().setColorFilter(Color.argb(0xFF, 0x0B, 0x15, 0x60), PorterDuff.Mode.SRC_IN);
        }

        completedChallengesTextView.setText(completedChallengesText);
    }

    private void setCollectionVisuals() {
        TextView collectedPlanesTextView = mActivity.findViewById(R.id.planes_collected);
        TextView collectedPartnersTextView = mActivity.findViewById(R.id.partners_collected);

        ConcurrentHashMap<String, HashSet<String>> planeCollection = getPlaneCollection();
        ConcurrentHashMap<String, HashSet<String>> partnerCollection = getPartnerCollection();

        int planesAmount = 0;
        int partnersAmount = 0;

        String collectedPlanesText;
        String collectedPartnersText;

        for (String plane : planeCollection.keySet()) {
            planesAmount += planeCollection.get(plane).size();
        }

        for (String partner : partnerCollection.keySet()) {
            partnersAmount += partnerCollection.get(partner).size();
        }

        if (planesAmount > 0) {
            if (planesAmount > 1) collectedPlanesText = String.format("You have collected %d planes!", planesAmount);
            else collectedPlanesText = "You have collected 1 plane!";
        }
        else collectedPlanesText = "You haven't collected any planes yet.";

        if (partnersAmount > 0) {
            if (partnersAmount > 1) collectedPartnersText = String.format("You have collected %d partners!", partnersAmount);
            else collectedPartnersText = "You have collected 1 partner!";
        }
        else collectedPartnersText = "You haven't collected any partners yet.";

        collectedPlanesTextView.setText(collectedPlanesText);
        collectedPartnersTextView.setText(collectedPartnersText);
    }


    //Handle button click events here.
    @Override
    public void onClick(View view){
        LinearLayout profileBasis = mActivity.findViewById(R.id.profile_basis);
        LinearLayout challengesBasis = mActivity.findViewById(R.id.challenges_basis);
        Log.d("BUTTON", "clicked");

        switch(view.getId()){
            case R.id.button_bottom:
                Button login = (Button) view;

                if(login.getText().equals(mActivity.getString(R.string.button_login))){

                   mActivity.makeAuthorizationRequest();

                }else if(login.getText().equals(mActivity.getString(R.string.button_logout))){
                    drawerLayout.closeDrawer(GravityCompat.START);
                    mActivity.logout();
                }
                else {
                    login.setText(logInButtonText);
                    setChallengeVisuals();
                    setCollectionVisuals();
                    profileBasis.setVisibility(View.VISIBLE);
                    challengesBasis.setVisibility(View.GONE);
                }
                break;
            case R.id.finnair_logo_button:
                fragmentManager.beginTransaction()
                        .replace(R.id.main_content, mapFragment)
                        .commit();
                break;
            case R.id.toolbar_partners_button:                   //Open partner popup window.
                if(partnerDataReady) {
                    partnerListWindow.showPopupWindow();
                    break;
                }
            case R.id.open_drawer_button:                        //Open drawer, (Profile icon)
                drawerLayout.openDrawer(GravityCompat.START);
                break;

            case R.id.challenges_button:
                login = (Button) mActivity.findViewById(R.id.button_bottom);
                logInButtonText = login.getText().toString();
                login.setText(mActivity.getString(R.string.drawer_active_challenges_list_go_back_button));

                profileBasis.setVisibility(View.GONE);
                challengesBasis.setVisibility(View.VISIBLE);
                LinearLayout challengeList = challengesBasis.findViewById(R.id.challenge_list);

                mActivity.fillChallengeView(challengeList);

                break;

            case R.id.drawer_sing_up_button:
                mActivity.startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://www.finnair.com/int/gb/join")));

            default: break;
        }
    }

    public ConcurrentHashMap<String, HashSet<String>> getPlaneCollection() {
        return mapFragment.getPlaneCollection();
    }

    public void refreshPlaneCollection() {
        mapFragment.refreshPlaneCollection();
    }

    public ConcurrentHashMap<String, HashSet<String>> getPartnerCollection() {
        return mapFragment.getPartnerCollection();
    }

    public void refreshPartnerCollection() {
        mapFragment.refreshPartnerCollection();
    }

    public Pair<ArrayList<Plane>, ArrayList<Partner>> getRandomRewards(int amount) {
        return mapFragment.getRandomRewards(amount);
    }

    public void closeDrawer() {
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    /*
     * MainActivity calls this method after partnerMarkerClass has fetched data.
     * Sets partnersButton textcolor to color.nordicBlue and partnerDataReady to true.
     * Calls partnerListWindows method createPopupWindow which sets data and recreates the window.
     */
    public void notifyPartnerDataChanged(){
        Button tempButton = (Button)mActivity.findViewById(R.id.toolbar_partners_button);
        tempButton.setTextColor(mActivity.getResources().getColor(R.color.nordicBlue));
        this.partnerDataReady = true;
        partnerListWindow.createPopupWindow();
    }

    public PartnerListWindow getPartnerListWindow(){
        return this.partnerListWindow;
    }

}
