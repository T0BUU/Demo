package com.finnair.gamifiedpartnermap;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.FragmentManager;
import android.text.Html;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ResponseTypeValues;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
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

    private int CHALLENGE_LIMIT = 5;
    private ArrayList<Challenge> activeChallenges;


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

        //TODO: Replace this with a call to firebase.
        InputStream is = getResources().openRawResource(R.raw.sample_challenges);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        JSONArray json = null;

        try {
            json = new JSONArray(writer.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        activeChallenges = new ArrayList<>();

        for (int i = 0; i < json.length() && i < CHALLENGE_LIMIT; ++i) {
            try {
                activeChallenges.add(new Challenge((JSONObject) json.get(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //-------
        FloatingActionButton cameraButton = findViewById(R.id.camera_fab);
        cameraButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), CameraActivity.class);
                startActivity(i);
            }
        });
    }


    public View fillChallengeView(LinearLayout challenges) {

        LayoutInflater inflater = getLayoutInflater();

        for (Challenge challenge : activeChallenges)
        {
            TableRow row = new TableRow(this);

            ConstraintLayout item = (ConstraintLayout) inflater.inflate(R.layout.challenge_list_item, row, false);

            TextView name = (TextView) item.findViewById(R.id.challenge_description);
            name.setText(Html.fromHtml(challenge.getDescription()));

            TextView collectedOutOf = (TextView) item.findViewById(R.id.challenge_counter);
            collectedOutOf.setText(String.format("%d/%d", challenge.getProgress(), challenge.getAmount()));

            TextView rewardText = (TextView) item.findViewById(R.id.reward);
            rewardText.setText(Html.fromHtml(String.format("The reward is <u><b><font color=#000B1560>%d</font></b></u> cards.", challenge.getReward())));


            ProgressBar collectedProgress = (ProgressBar) item.findViewById(R.id.challenge_collected_progress);
            collectedProgress.setMax(challenge.getAmount());
            collectedProgress.setProgress(challenge.getProgress());
            row.addView(item);

            challenges.addView(row);
        }

        return challenges;

    }


    //Method to notify partnerListWindow when PartnerMarkerClass fetches partner data.
    public void notifyDataChange(){
        System.out.println("!!!!!!!Main Actitvity notifyDatachange called!!!!!!!");
        myMainLayout.notifyPartnerDataChanged();
    }



    // Logs the user out
    protected void logout() {

        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();

        // Remove the token from memory
        SharedPreferences sp = getApplicationContext().getSharedPreferences("Auth", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove("Access Token");
        editor.apply();

        // Reset the UI texts to the default ones
        TextView tw = findViewById(R.id.nav_profile_name);
        tw.setText("Not logged in");

        Button login = findViewById(R.id.button_bottom);
        LinearLayout registrationBox = findViewById(R.id.drawer_not_logged_in_points_item);
        LinearLayout pointsBox = findViewById(R.id.drawer_login_points_item);
        TextView profileLevel = findViewById(R.id.membership_level);

        registrationBox.setVisibility(View.VISIBLE);
        pointsBox.setVisibility(View.GONE);
        profileLevel.setVisibility(View.GONE);
        login.setText(getString(R.string.button_login));


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
            Button login = findViewById(R.id.button_bottom);
            LinearLayout registrationBox = findViewById(R.id.drawer_not_logged_in_points_item);
            LinearLayout pointsBox = findViewById(R.id.drawer_login_points_item);
            TextView profileLevel = findViewById(R.id.membership_level);

            registrationBox.setVisibility(View.GONE);
            pointsBox.setVisibility(View.VISIBLE);
            profileLevel.setVisibility(View.VISIBLE);
            login.setText(getString(R.string.button_logout));
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

