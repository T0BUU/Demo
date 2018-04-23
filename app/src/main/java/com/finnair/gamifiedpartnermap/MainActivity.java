package com.finnair.gamifiedpartnermap;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.FragmentManager;
import android.text.Html;
import android.util.JsonReader;
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

import com.fasterxml.jackson.core.util.InternCache;
import com.google.android.gms.maps.GoogleMap;
import com.google.gson.Gson;

import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ResponseTypeValues;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import static com.finnair.gamifiedpartnermap.CardSelectionActivity.whichWasCaughtMessage;


/**
 * Created by ala-hazla on 16.12.2017.
 */

public class MainActivity extends AppCompatActivity implements ProfileResponseHandler{

    private MActivityLayout myMainLayout;

    private FragmentManager fragmentManager;

    private MapsFragment mapFragment;
    private GoogleMap gMap;


    SensorActivity sensorActivity;

    public double azimuth;
    public double pitch;
    public double roll;

    private Plane caughtPlane;
    private Plane randomPlane;
    private Partner caughtPartner;
    private Partner randomPartner;



    //Constants.
    final static int locationPermission = 100;
    final static String catchMessagePlanes = "com.finnair.gamifiedpartnermap.planeCollection";
    final static String catchMessagePartners = "com.finnair.gamifiedpartnermap.partnerCollection";
    final static String profileInfoStartUp = "com.finnair.gamifiedpartnermap.profileInfo";
    final static String planesCaught = "com.finnair.gamifiedpartnermap.planesCaught";
    final static String partnersCaught = "com.finnair.gamifiedpartnermap.partnersCaught";
    final static String relatedChallengesToCaught = "com.finnair.gamifiedpartnermap.relatedChallengesCaught";
    final static String relatedChallengesToRandom = "com.finnair.gamifiedpartnermap.relatedChallengesRandom";
    final static String activeChallengesMessage = "com.finnair.gamifiedpartnermap.activeChallengesMessage";
    final static String relatedChallengesToPlanes = "com.finnair.gamifiedpartnermap.relatedChallengesPlanes";
    final static String relatedChallengesToPartners = "com.finnair.gamifiedpartnermap.relatedChallengesPartners";
    final static String isLoggedInMessage = "com.finnair.gamifiedpartnermap.isLoggedIn";
    final static String goToCollectionMessage = "com.finnair.gamifiedpartnermap.goToCollection";

    private int CHALLENGE_LIMIT = 5;
    private boolean isLoggedIn = false;
    private ArrayList<Challenge> activeChallenges;

    public static ArrayList<String> caughtPlanes = new ArrayList<String>(4);
    public static ArrayList<String> caughtPartners = new ArrayList<String>(8);
    public static HashMap<String, HashSet<String>> koneetHashMap = new HashMap<String, HashSet<String>>();
    public static HashMap<String, HashSet<String>> partneritHashMap = new HashMap<String, HashSet<String>>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        readChallenges();

        fragmentManager = getSupportFragmentManager();

        // Main layout class, this instanties whole UI.
        myMainLayout = new MActivityLayout();
        myMainLayout.createUI(this, fragmentManager,
                                (HashMap<String, String>) getIntent().getSerializableExtra(profileInfoStartUp));


        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("Auth",Context.MODE_PRIVATE);
        if(sharedPreferences.contains("Access Token")){
            makeProfileRequest(sharedPreferences.getString("Access Token", ""));
        }

        SensorManager sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);


        azimuth = Calculations.bearing;
        pitch = Calculations.angle;


        sensorActivity = new SensorActivity(sensorManager,azimuth, pitch, roll);
        sensorActivity.registerListeners();



    }

    private void readChallenges() {
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

        for (int i = 0; i < CHALLENGE_LIMIT; ++i) {
            activeChallenges.add(new Challenge());
        }

        //Read challenges from disk and then add additional once.

        JSONArray readJson = readCollectedActiveChallenges(this);

        for (int i = 0; i < readJson.length() && i < CHALLENGE_LIMIT; ++i) {
            try {
                Challenge current = new Challenge((JSONObject) readJson.get(i));
                activeChallenges.set(current.getIndex(), current);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Random generator = new Random();

        for (int i = 0; i < json.length() && i < CHALLENGE_LIMIT; ++i) {
            try {
                if (activeChallenges.get(i).getId() == -1) {
                    Log.d("Adding Challenges", "NEW ADD");
                    Challenge current = new Challenge((JSONObject) json.get(generator.nextInt(json.length())));
                    current.setIndex(i);
                    activeChallenges.set(i, current);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }



        //-------
    }


    public JSONArray readCollectedActiveChallenges(Context context) {

        String ret = "";


        try {
            InputStream inputStream = context.openFileInput("activeChallenges");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }


        Log.d("Collection", ret);

        try {
            return new JSONArray(ret);
        } catch (JSONException e) {
            e.printStackTrace();
            return new JSONArray();
        }

    }


    public View fillChallengeView(LinearLayout challenges) {

        LayoutInflater inflater = getLayoutInflater();

        for (int i = 0; i < challenges.getChildCount(); ++i)
        {
            Challenge challenge = activeChallenges.get(i);
            TableRow row = (TableRow) challenges.getChildAt(i);

            addChallengeToView(row, inflater, challenge);
        }

        return challenges;

    }

    public void onChallengeCancelClick(View v) {

        TableRow row = (TableRow) v.getParent().getParent().getParent().getParent();
        LinearLayout challengeList = (LinearLayout) row.getParent();
        int index = challengeList.indexOfChild(row);
        LayoutInflater inflater = getLayoutInflater();

        Challenge emptyChallenge = new Challenge();
        emptyChallenge.setIndex(index);

        activeChallenges.set(index, emptyChallenge);

        addChallengeToView(row, inflater, emptyChallenge);

    }

    public void onChallengeClick(View v) {

        TableRow row = (TableRow) v.getParent().getParent();
        LinearLayout challengeList = (LinearLayout) row.getParent();
        int index = challengeList.indexOfChild(row);
        LayoutInflater inflater = getLayoutInflater();

        if (activeChallenges.get(index).isCompleted()) {

            myMainLayout.removeChallenge(activeChallenges.get(index));

            myMainLayout.closeDrawer();

            Challenge emptyChallenge = new Challenge();
            emptyChallenge.setIndex(index);
            int reward = activeChallenges.get(index).getReward();

            activeChallenges.set(index, emptyChallenge);

            addChallengeToView(row, inflater, emptyChallenge);

            //Open the card rewards activity

            ArrayList<String> caughtPlanes = new ArrayList<>();
            ArrayList<String> caughtPartners = new ArrayList<>();

            ArrayList<ArrayList<Challenge>> relatedChallengesPlanes = new ArrayList<>();
            ArrayList<ArrayList<Challenge>> relatedChallengesPartners = new ArrayList<>();

            Pair<ArrayList<Plane>, ArrayList<Partner>> randomRewards = this.myMainLayout.getRandomRewards(reward);

            for (Plane plane : randomRewards.first) {
                caughtPlanes.add(plane.getPlaneType());
                caughtPlanes.add(plane.getOriginCountry());

                relatedChallengesPlanes.add(plane.getRelatedChallenges());
            }

            for (Partner partner : randomRewards.second) {
                caughtPartners.add(partner.getFieldOfBusiness());
                caughtPartners.add(partner.getID());
                caughtPartners.add(partner.getAddress());
                caughtPartners.add(partner.getDescription());

                relatedChallengesPartners.add(partner.getRelatedChallenges());
            }

            Intent intent = new Intent(this,CardRewardActivity.class);


            intent.putParcelableArrayListExtra(activeChallengesMessage, this.activeChallenges);
            intent.putExtra(planesCaught, caughtPlanes);
            intent.putExtra(partnersCaught, caughtPartners);
            intent.putExtra(catchMessagePlanes, this.myMainLayout.getPlaneCollection());
            intent.putExtra(catchMessagePartners, this.myMainLayout.getPartnerCollection());
            intent.putExtra(relatedChallengesToPlanes, new Gson().toJson(relatedChallengesPlanes));
            intent.putExtra(relatedChallengesToPartners, new Gson().toJson(relatedChallengesPartners));
            startActivityForResult(intent, 12);

        }

    }

    private void addChallengeToView(TableRow row, LayoutInflater inflater, Challenge challenge) {
        row.removeAllViews();

        if (challenge.getId() != -1) {
            ConstraintLayout item = (ConstraintLayout) inflater.inflate(R.layout.challenge_list_item, row, false);

            TextView name = (TextView) item.findViewById(R.id.challenge_description);
            name.setText(Html.fromHtml(challenge.getDescription()));

            TextView collectedOutOf = (TextView) item.findViewById(R.id.challenge_counter);
            collectedOutOf.setText(String.format("%d/%d", challenge.getProgress(), challenge.getAmount()));

            ProgressBar collectedProgress = (ProgressBar) item.findViewById(R.id.challenge_collected_progress);
            collectedProgress.setMax(challenge.getAmount());
            collectedProgress.setProgress(challenge.getProgress());

            if (challenge.isCompleted()) {
                collectedProgress.getProgressDrawable().setColorFilter(Color.argb(0xFF,0xb4, 0xcb, 0x66), PorterDuff.Mode.SRC_IN);
                item.findViewById(R.id.challenge_container).getBackground().setColorFilter(Color.argb(0xCC,0xb4, 0xcb, 0x66), PorterDuff.Mode.ADD);
            }

            row.addView(item);
        }
        else {
            ConstraintLayout item = (ConstraintLayout) inflater.inflate(R.layout.challenge_list_item_nonactive, row, false);

            row.addView(item);
        }

    }

    private String formatChallenges() {
        JSONArray result = new JSONArray();

        for ( Challenge c : activeChallenges ) {
            result.put(c.saveChallenge());
        }

        return result.toString();
    }

    private void saveChallenges(Context context) {
        String result = formatChallenges();

        try {
            FileOutputStream outputStream = context.openFileOutput("activeChallenges", Context.MODE_PRIVATE);
            outputStream.write(result.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("Challenge Saving", result);
    }



    public ArrayList<Challenge> getActiveChallenges() {
        return activeChallenges;
    }

    //Method to notify partnerListWindow when PartnerMarkerClass fetches partner data.
    public void notifyDataChange(){
        System.out.println("!!!!!!!Main Actitvity notifyDatachange called!!!!!!!");
        myMainLayout.notifyPartnerDataChanged();
    }




    // Logs the user out
    protected void logout() {

        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();

        isLoggedIn = false;

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

            isLoggedIn = true;
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

        openCardCollection(true);

    }

    private void openCardCollection(Boolean tab) {
        Intent intent = new Intent(this, PlaneCollectionActivity.class);
        intent.putExtra(whichWasCaughtMessage, tab);
        intent.putExtra(catchMessagePartners,  this.myMainLayout.getPartnerCollection());
        intent.putExtra(catchMessagePlanes, this.myMainLayout.getPlaneCollection());
        intent.putExtra(isLoggedInMessage, this.isLoggedIn);
        startActivity(intent);
    }

    public void onPlaneCatch(Plane caughtPlane, Plane randomPlane) {


       this.caughtPlane = caughtPlane;
       this.randomPlane = randomPlane;

        koneetHashMap.putAll(this.myMainLayout.getPlaneCollection());
        partneritHashMap.putAll(this.myMainLayout.getPartnerCollection());

        catchPlane();


    }

    public void catchPlane(){
        Intent intent = new Intent(this, CameraActivity.class);
        startActivityForResult(intent, 12);
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

        intent.putParcelableArrayListExtra(activeChallengesMessage, this.activeChallenges);
        intent.putExtra(relatedChallengesToCaught, caughtPartner.getRelatedChallenges());
        intent.putExtra(relatedChallengesToRandom, randomPartner.getRelatedChallenges());
        intent.putExtra(partnersCaught, caughtPartners);
        intent.putExtra(catchMessagePlanes, this.myMainLayout.getPlaneCollection());
        intent.putExtra(catchMessagePartners, this.myMainLayout.getPartnerCollection());
        startActivityForResult(intent, 12);

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 12) {
            myMainLayout.refreshPartnerCollection();
            myMainLayout.refreshPlaneCollection();
            myMainLayout.setChallengeVisuals();

            if(resultCode == RESULT_OK) {
                Boolean goToCollection = data.getBooleanExtra(goToCollectionMessage, false);

                if (goToCollection) {
                    openCardCollection(data.getBooleanExtra(whichWasCaughtMessage, true));
                }
                else { }

            }
            else if(resultCode == RESULT_FIRST_USER) {
                Intent intent = new Intent(this, CardSelectionActivity.class);

                ArrayList<String> caughtPlanes = new ArrayList<>();

                caughtPlanes.add(caughtPlane.getPlaneType());
                caughtPlanes.add(caughtPlane.getOriginCountry());
                caughtPlanes.add(randomPlane.getPlaneType());
                caughtPlanes.add(randomPlane.getOriginCountry());

                intent.putParcelableArrayListExtra(activeChallengesMessage, this.activeChallenges);
                intent.putExtra(relatedChallengesToCaught, caughtPlane.getRelatedChallenges());
                intent.putExtra(relatedChallengesToRandom, randomPlane.getRelatedChallenges());
                intent.putExtra(planesCaught, caughtPlanes);
                intent.putExtra(catchMessagePlanes, this.myMainLayout.getPlaneCollection());
                intent.putExtra(catchMessagePartners, this.myMainLayout.getPartnerCollection());
                startActivityForResult(intent, 12);
            }
            else {


            }
        }
    }

    @Override
    public void onPause() {
        saveChallenges(this);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        findViewById(R.id.toolbar).findViewById(R.id.open_drawer_button).getBackground().setColorFilter(getResources().getColor(R.color.nordicBlue), PorterDuff.Mode.SRC_IN);
        ((Button) findViewById(R.id.toolbar).findViewById(R.id.toolbar_partners_button)).setTextColor(getResources().getColor(R.color.nordicBlue));
        readChallenges();
    }

    public MActivityLayout getMActivityLayout(){
        return this.myMainLayout;
    }

}

