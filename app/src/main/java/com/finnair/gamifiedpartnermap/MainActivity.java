package com.finnair.gamifiedpartnermap;

import android.*;
import android.Manifest;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
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
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by ala-hazla on 16.12.2017.
 */

public class MainActivity extends AppCompatActivity implements PlaneCatchFragment.PlaneCatchListener,
                                                                ProfileResponseHandler{

    private MActivityLayout myMainLayout;

    private FragmentManager fragmentManager;


    //Constants.
    final static int locationPermission = 100;
    final static String planeCatchMessage = "com.finnair.gamifiedpartnermap.planeCaught";
    final static String profileInfoStartUp = "com.finnair.gamifiedpartnermap.profileInfo";
    final static String planesCaught = "com.finnair.gamifiedpartnermap.planesCaught";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentManager = getSupportFragmentManager();

        //Main layout class, this instanties whole UI.
        myMainLayout = new MActivityLayout();
        myMainLayout.createUI(this, fragmentManager,
                                (HashMap<String, String>) getIntent().getSerializableExtra(profileInfoStartUp),
                                (Pair<String, String>) getIntent().getSerializableExtra(planesCaught));


        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("Auth",Context.MODE_PRIVATE);
        if(sharedPreferences.contains("Access Token")){
            makeProfileRequest(sharedPreferences.getString("Access Token", ""));
        }




    }



    // Logs the user out
    protected void logout() {

        // Remove the token from memory
        SharedPreferences sp = getApplicationContext().getSharedPreferences("Auth", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove("Access Token");
        editor.apply();

        // Reset the UI texts to the default ones
        TextView tw = findViewById(R.id.nav_profile_name);
        tw.setText("Not logged in");

        Button login = findViewById(R.id.button_login);
        login.setText("Login");


    }

    //Starts the login process by performing an authorization request
    protected void makeAuthorizationRequest() {
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
    private void makeProfileRequest(String accessToken){
        String profileUrl = String.format("https://preauth.finnair.com/cas/oauth2.0/profile?access_token=%s", accessToken);
        try {

            ProfileRequest pr = new ProfileRequest();
            pr.handler = this;
            pr.execute(new URL(profileUrl));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }


    // Implements handling the request to the profile request and also handles errors
    @Override
    public void onProfileResponseAcquired(String profileResponse) {
        try {

            JSONObject json = new JSONObject(profileResponse);
            TextView profileName = findViewById(R.id.nav_profile_name);
            profileName.setText(json.getString("id"));
            Button login = findViewById(R.id.button_login);
            login.setText("Logout");
        } catch (JSONException e) {
            // If the field 'id' not available in the response, the token is invalid, e.g. expired
            SharedPreferences sp = getApplicationContext().getSharedPreferences("Auth", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.remove("Access Token");
            editor.commit();
            e.printStackTrace();
        }


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

    public void onPlaneCatch(Plane caughtPlane, Plane randomPlane) {
        Intent intent = new Intent(this, CardSelectionActivity.class);

        ArrayList<String> caughtPlanes = new ArrayList<>();

        caughtPlanes.add(caughtPlane.getPlaneType());
        caughtPlanes.add(caughtPlane.getOriginCountry());
        caughtPlanes.add(randomPlane.getPlaneType());
        caughtPlanes.add(randomPlane.getOriginCountry());

        intent.putExtra(planesCaught, caughtPlanes);
        startActivity(intent);
    }


    @Override
    public void onPlaneDialogPositiveClick(DialogFragment dialog) {

    }

}

