package com.finnair.gamifiedpartnermap;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
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

import static com.finnair.gamifiedpartnermap.CardSelectionActivity.whichWasCaughtMessage;


/**
 * Created by ala-hazla on 16.12.2017.
 */

public class MainActivity extends AppCompatActivity implements ProfileResponseHandler{

    private MActivityLayout myMainLayout;

    private FragmentManager fragmentManager;

    private MapsFragment mapFragment;
    private GoogleMap gMap;

    private String planesListing = "";

    SensorActivity sensorActivity;



    //Constants.
    final static int locationPermission = 100;
    final static String catchMessagePlanes = "com.finnair.gamifiedpartnermap.planeCollection";
    final static String catchMessagePartners = "com.finnair.gamifiedpartnermap.partnerCollection";
    final static String profileInfoStartUp = "com.finnair.gamifiedpartnermap.profileInfo";
    final static String planesCaught = "com.finnair.gamifiedpartnermap.planesCaught";
    final static String partnersCaught = "com.finnair.gamifiedpartnermap.partnersCaught";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fragmentManager = getSupportFragmentManager();

        // Main layout class, this instanties whole UI.
        myMainLayout = new MActivityLayout();
        myMainLayout.createUI(this, fragmentManager,
                                (HashMap<String, String>) getIntent().getSerializableExtra(profileInfoStartUp));


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

                Intent intent = new Intent(this, PlaneCollectionActivity.class);
                intent.putExtra(whichWasCaughtMessage, true);
                intent.putExtra(catchMessagePartners,  this.myMainLayout.getPartnerCollection());
                intent.putExtra(catchMessagePlanes, this.myMainLayout.getPlaneCollection());
                startActivity(intent);
                finish();

    }

    public void onPlaneCatch(Plane caughtPlane, Plane randomPlane) {
        Intent intent = new Intent(this, CardSelectionActivity.class);

        ArrayList<String> caughtPlanes = new ArrayList<>();

        caughtPlanes.add(caughtPlane.getPlaneType());
        caughtPlanes.add(caughtPlane.getOriginCountry());
        caughtPlanes.add(randomPlane.getPlaneType());
        caughtPlanes.add(randomPlane.getOriginCountry());

        intent.putExtra(planesCaught, caughtPlanes);
        intent.putExtra(catchMessagePlanes, this.myMainLayout.getPlaneCollection());
        intent.putExtra(catchMessagePartners, this.myMainLayout.getPartnerCollection());
        startActivity(intent);
        finish();
    }

    public void onPartnerCatch(Partner caughtPartner, Partner randomPartner) {
        Intent intent = new Intent(this, CardSelectionActivity.class);

        ArrayList<String> caughtPartners = new ArrayList<>();

        caughtPartners.add(caughtPartner.getFieldOfBusiness());
        caughtPartners.add(caughtPartner.getID());
        caughtPartners.add(caughtPartner.getAddress());
        caughtPartners.add(caughtPartner.getDescription());

        caughtPartners.add(randomPartner.getFieldOfBusiness());
        caughtPartners.add(randomPartner.getID());
        caughtPartners.add(randomPartner.getAddress());
        caughtPartners.add(randomPartner.getDescription());

        intent.putExtra(partnersCaught, caughtPartners);
        intent.putExtra(catchMessagePlanes, this.myMainLayout.getPlaneCollection());
        intent.putExtra(catchMessagePartners, this.myMainLayout.getPartnerCollection());
        startActivity(intent);
        finish();
    }

}

