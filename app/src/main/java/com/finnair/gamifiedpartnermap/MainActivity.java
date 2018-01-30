package com.finnair.gamifiedpartnermap;

import android.*;
import android.Manifest;
import android.app.DialogFragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
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

