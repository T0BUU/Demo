package com.finnair.gamifiedpartnermap;

import android.*;
import android.Manifest;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ResponseTypeValues;

import java.util.HashMap;


/**
 * Created by ala-hazla on 16.12.2017.
 */
//Modified by Otto on 11.1.2018, added drawerLayout and toolbar to MainActivity.

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
                                                                LocationPermissionDialog.LocationDialogListener{

    private DrawerLayout drawerLayout;
    private DrawerAdapter drawerAdapter;

    private FragmentManager fragmentManager;

    private MapsFragment mapFragment;
    private GoogleMap gMap;


    //Constants marking which permissions were granted.
    final static int locationPermission = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createUI();
    }

    public void createUI(){

        drawerAdapter = new DrawerAdapter();        //DrawerAdapter instanties fragments
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        //This handles all our fragments
        fragmentManager = getSupportFragmentManager();


        //This is toolbar on top of screen.
        Toolbar myToolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolBar);

        //Navigation buttons
        Button loginButton = (Button) findViewById(R.id.button_login);
        ImageButton settingsButton = (ImageButton) findViewById(R.id.button_settings);
        Button mapButton = (Button) findViewById(R.id.toolbar_map_button);
        Button partnersButton = (Button) findViewById(R.id.toolbar_partners_button);

        //Add click listeners to all buttons
        loginButton.setOnClickListener(this);
        settingsButton.setOnClickListener(this);
        mapButton.setOnClickListener(this);
        partnersButton.setOnClickListener(this);

        //Add drawer toggle button and listener for it.
        ActionBarDrawerToggle abToggle = new ActionBarDrawerToggle(
                this, drawerLayout, myToolBar, R.string.actionbar_drawer_open, R.string.actionbar_drawer_close);
        drawerLayout.addDrawerListener(abToggle);
        abToggle.setToolbarNavigationClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        //Set default drawerToggleButton to false, and then replace it with custom icon.
        abToggle.setDrawerIndicatorEnabled(false);
        abToggle.setHomeAsUpIndicator(R.drawable.ic_person_blue);
        abToggle.syncState();

        //Insert map fragment to main_content view.
        fragmentManager.beginTransaction()
                .replace(R.id.main_content, drawerAdapter.getItem(0))
                .commit();

        if(getIntent() != null){
            if(getIntent().getAction().equals("com.finnair.gamifiedpartnermap.AUTHORIZATION_FAILED")){
                Toast.makeText(this, "Authorization failed", Toast.LENGTH_SHORT).show();
            }
            else if(getIntent().getAction().equals("com.finnair.gamifiedpartnermap.PROFILE_REQUEST_SUCCESSFUL")){
                HashMap<String, String> profileInformation = (HashMap<String, String>) getIntent().getSerializableExtra("profileInformation");
                Toast.makeText(this, "Login successful", Toast.LENGTH_LONG).show();
                TextView profileNameField = findViewById(R.id.nav_profile_name);
                profileNameField.setText(profileInformation.get("id"));

            }
        }

    }


    //Handle button click events here.
    @Override
    public void onClick(View view){
        switch(view.getId()){
            case R.id.button_settings:
                fragmentManager.beginTransaction()
                        .replace(R.id.main_content, drawerAdapter.getItem(1))
                        .commit();
             //   myTitleView.setText(R.string.tab_settings);
                drawerLayout.closeDrawer(GravityCompat.START);
                break;
            case R.id.button_login:
                drawerLayout.closeDrawer(GravityCompat.START);
                Toast.makeText(MainActivity.this, "Clicked login", Toast.LENGTH_SHORT).show();
                makeAuthorizationRequest();
                break;
            case R.id.finnair_logo_button:
                fragmentManager.beginTransaction()
                        .replace(R.id.main_content, drawerAdapter.getItem(0))
                        .commit();
            //    myTitleView.setText(R.string.tab_map);
                break;
            case R.id.toolbar_map_button:
                fragmentManager.beginTransaction()
                        .replace(R.id.main_content, drawerAdapter.getItem(0))
                        .commit();
                break;
            case R.id.toolbar_partners_button:
                Toast.makeText(MainActivity.this, "Open partner list", Toast.LENGTH_SHORT).show();
            default: break;
        }
    }

    private void makeAuthorizationRequest() {
        //Creates the configuration for the authorization service
        AuthorizationServiceConfiguration serviceConfiguration = new AuthorizationServiceConfiguration(
                Uri.parse("https://preauth.finnair.com/cas/oauth2.0/authorize"), // Authorization endpoint
                Uri.parse("https://preauth.finnair.com/cas/oauth2.0/accessToken") // Token endpoint
        );

        AuthorizationRequest.Builder authRequestBuilder =
                new AuthorizationRequest.Builder(
                        serviceConfiguration, // the authorization service configuration
                        "aalto-0Cs", // the client ID, typically pre-registered and static
                        ResponseTypeValues.CODE, // the response_type value: we want a code
                        Uri.parse("https://datademo-2a85f.firebaseapp.com/auth/finnair/login")); // the redirect URI to which the auth response is sent


        // Create the authorization request using Builder and the authorization service
        AuthorizationRequest authRequest = authRequestBuilder.build();
        AuthorizationService authService = new AuthorizationService(this);

        // Perform the request. The pending intents will be redirected through intent filters in the manifest
        PendingIntent positive = PendingIntent.getActivity(this, 0, new Intent(this, LoginActivity.class), 0);
        PendingIntent negative = PendingIntent.getActivity(this, 0, new Intent("com.finnair.gamifiedpartnermap.HANDLE_AUTHORIZATION_RESPONSE"), 0);
        authService.performAuthorizationRequest(authRequest,positive, negative);


    }

    public void setMap(GoogleMap m) {
        gMap = m;
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT);
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, locationPermission);
    }
}

