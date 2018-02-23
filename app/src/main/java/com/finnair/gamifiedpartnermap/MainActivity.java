package com.finnair.gamifiedpartnermap;

import android.*;
import android.Manifest;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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

public class MainActivity extends AppCompatActivity implements PlaneCatchFragment.PlaneCatchListener {

    private MActivityLayout myMainLayout;

    private FragmentManager fragmentManager;


    //Constants.
    final static int locationPermission = 100;
    final static String planeCatchMessage = "com.finnair.gamifiedpartnermap.planeCaught";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        fragmentManager = getSupportFragmentManager();



            //Main layout class, this instanties whole UI.
            myMainLayout = new MActivityLayout();
            myMainLayout.createUI(this, fragmentManager);

    }


    public void onCardButtonClick(View v) {
        final int upper = R.id.card_button_upper;
        final int lower = R.id.card_button_lower;

        switch (v.getId()) {
            case upper: {
                ((DialogFragment) this.getFragmentManager().findFragmentByTag("Caught plane")).dismiss();
                break;
            }
            case lower: {
                Intent intent = new Intent(this, PlaneCollectionActivity.class);
                intent.putExtra(planeCatchMessage, this.myMainLayout.getCollection());
                startActivity(intent);
                break;
            }
            default: {
                Intent intent = new Intent(this, PlaneCollectionActivity.class);
                intent.putExtra(planeCatchMessage, this.myMainLayout.getCollection());
                startActivity(intent);
            }
        }
    }


    @Override
    public void onPlaneDialogPositiveClick(DialogFragment dialog) {
        Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT);
        Intent intent = new Intent(this, PlaneCollectionActivity.class);
        intent.putExtra(planeCatchMessage ,this.myMainLayout.getCollection());
        startActivity(intent);
    }

}

