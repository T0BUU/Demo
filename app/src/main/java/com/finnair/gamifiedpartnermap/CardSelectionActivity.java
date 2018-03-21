package com.finnair.gamifiedpartnermap;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import static com.finnair.gamifiedpartnermap.MainActivity.activeChallengesMessage;
import static com.finnair.gamifiedpartnermap.MainActivity.catchMessagePartners;
import static com.finnair.gamifiedpartnermap.MainActivity.catchMessagePlanes;
import static com.finnair.gamifiedpartnermap.MainActivity.partnersCaught;
import static com.finnair.gamifiedpartnermap.MainActivity.planesCaught;
import static com.finnair.gamifiedpartnermap.MainActivity.relatedChallengesToCaught;
import static com.finnair.gamifiedpartnermap.MainActivity.relatedChallengesToRandom;
import static com.finnair.gamifiedpartnermap.PartnerMarkerClass.USER_DATA_LOCATION_PARTNERS;
import static com.finnair.gamifiedpartnermap.PlaneMarkerClass.USER_DATA_LOCATION_PLANES;

/**
 * Created by huzla on 1.3.2018.
 */

public class CardSelectionActivity extends AppCompatActivity implements PlaneCatchFragment.PlaneCatchListener {

    private static final HashMap<String, Integer> modelsToImages;
    static
    {
        List<String> PLANE_TYPES = Arrays.asList("AIRBUS A350-900", "AIRBUS A330-300",
                "AIRBUS A321", "AIRBUS A321-231",
                "AIRBUS A320", "AIRBUS A319",
                "EMBRAER 190", "ATR 72-212A");

        List<Integer> PLANE_IMAGES = Arrays.asList(R.drawable.a350_900, R.drawable.a330_300,
                R.drawable.a321, R.drawable.a321_sharklet, R.drawable.a320,
                R.drawable.a319, R.drawable.embraer_190, R.drawable.x350x113_norra);

        modelsToImages = new HashMap<String, Integer>();

        for (int i = 0; i < PLANE_TYPES.size(); ++i) {
            modelsToImages.put(PLANE_TYPES.get(i), PLANE_IMAGES.get(i));
        }

    }

    private int matchCategoryToImage(String category) {
        switch (category) {
            case "Restaurant": return R.drawable.ic_restaurants;
            case "Car rental": return R.drawable.ic_car_rental;
            case "Charity": return R.drawable.ic_charity;
            case "Entertainment": return R.drawable.ic_entertainment;
            case "Finance and insurance": return R.drawable.ic_finance;
            case "Helsinki Airport": return R.drawable.ic_helsinki_vantaa;
            case "Golf and leisure time": return R.drawable.ic_hobbies;
            case "Hotel": return R.drawable.ic_hotels_spas;
            case "Shopping": return R.drawable.ic_shopping;
            case "Tour operators and cruise lines": return R.drawable.ic_travel;
            case "Services and Wellness": return R.drawable.ic_services_healthcare;
            default: return  R.raw.aalto_logo;
        }
    }


    public static String whichWasCaughtMessage = "com.finnair.gamifiedpartnermap.whichCaught";
    private boolean whichCaught;

    private String caughtPlaneType;
    private String caughtCountry;

    private String randomPlaneType;
    private String randomCountry;

    private String caughtPartnerField;
    private String caughtPartnerID;
    private String caughtPartnerAddress;
    private String caughtPartnerDescript;

    private String randomPartnerField;
    private String randomPartnerID;
    private String randomPartnerAddress;
    private String randomPartnerDescript;

    private HashMap<String, HashSet<String>> planeCollectionHashMap;
    private HashMap<String, HashSet<String>> partnerCollectionHashMap;

    private ArrayList<Challenge> relatedChallengesCaught;
    private ArrayList<Challenge> relatedChallengesRandom;
    private ArrayList<Challenge> activeChallenges;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Intent intent = getIntent();
        final ArrayList<String> planes = (ArrayList<String>) intent.getSerializableExtra(planesCaught);
        final ArrayList<String> partners = (ArrayList<String>) intent.getSerializableExtra(partnersCaught);

        relatedChallengesCaught =  intent.getParcelableArrayListExtra(relatedChallengesToCaught);
        relatedChallengesRandom = intent.getParcelableArrayListExtra(relatedChallengesToRandom);
        activeChallenges = intent.getParcelableArrayListExtra(activeChallengesMessage);

        for (Challenge c : activeChallenges) {
            Log.d("Building", "" + c.getPartnerFields() + " " + c.getPartnerNames() + " " + c.getPlaneDestinations() + " " + c.getPlaneModels());
        }

        planeCollectionHashMap = (HashMap<String, HashSet<String>>) intent.getSerializableExtra(catchMessagePlanes);

        partnerCollectionHashMap = (HashMap<String, HashSet<String>>) intent.getSerializableExtra(catchMessagePartners);

        Log.d("Collections: ", (planeCollectionHashMap == null) + " " + (partnerCollectionHashMap == null));

        if (planes != null) {
            setContentView(R.layout.plane_card_choose_layout);
            whichCaught = true;
            buildPlanePicking(planes);
        }
        else {
            setContentView(R.layout.partner_card_choose_layout);
            whichCaught = false;
            buildPartnersPicking(partners);
        }


    }


    void buildPlanePicking(ArrayList<String> planes) {
        caughtPlaneType = planes.get(0);
        caughtCountry = planes.get(1);

        randomPlaneType = planes.get(2);
        randomCountry = planes.get(3);

        View leftCard = (View)findViewById(R.id.card_left);
        View rightCard = (View)findViewById(R.id.card_right);

        ((TextView)leftCard.findViewById(R.id.plane_name)).setText(caughtPlaneType);

        ((TextView)leftCard.findViewById(R.id.location_list)).setText(caughtCountry);

        ((ImageView)leftCard.findViewById(R.id.plane_card_image)).setImageResource(modelsToImages.get(caughtPlaneType));

        leftCard.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                savePlane(caughtPlaneType, caughtCountry);

                for (Challenge challenge : relatedChallengesCaught ) {
                    challenge.incrementProgress();
                }


                PlaneCatchFragment caught = new PlaneCatchFragment();
                caught.setCancelable(false);
                caught.show(getFragmentManager().beginTransaction(), "Caught plane");
                caught.setAllFragmentData(caughtPlaneType,caughtCountry, modelsToImages.get(caughtPlaneType));
                Log.d("POOP", "TEST");


            }
        });

        rightCard.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                savePlane(randomPlaneType, randomCountry);

                for (Challenge challenge : relatedChallengesRandom ) {
                    challenge.incrementProgress();
                }

                PlaneCatchFragment caught = new PlaneCatchFragment();
                caught.setCancelable(false);
                caught.show(getFragmentManager().beginTransaction(), "Caught plane");
                caught.setAllFragmentData(randomPlaneType, randomCountry,  modelsToImages.get(randomPlaneType));
                Log.d("POOP", "TEST");
            }
        });
    }

    void buildPartnersPicking(ArrayList<String> partners) {
        caughtPartnerField = partners.get(0);
        caughtPartnerID = partners.get(1);
        caughtPartnerAddress = partners.get(2);
        caughtPartnerDescript = partners.get(3);

        randomPartnerField = partners.get(4);
        randomPartnerID = partners.get(5);
        randomPartnerAddress = partners.get(6);
        randomPartnerDescript = partners.get(7);

        View leftCard = (View)findViewById(R.id.card_left);
        View rightCard = (View)findViewById(R.id.card_right);

        ((TextView)leftCard.findViewById(R.id.company_info)).setText(caughtPartnerID + ", " + caughtPartnerAddress);
        ((TextView)leftCard.findViewById(R.id.field_of_business)).setText(caughtPartnerField);

        ((ImageView)leftCard.findViewById(R.id.partner_card_image)).setImageResource(matchCategoryToImage(caughtPartnerField));

        leftCard.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String currentTime = getCurrentTimeStamp();
                savePartner(caughtPartnerField, caughtPartnerID, currentTime);

                for (Challenge challenge : relatedChallengesCaught ) {
                    activeChallenges.get(challenge.getIndex()).incrementProgress();
                }

                for (Challenge c : activeChallenges) {
                    Log.d("Collected", "" + c.getProgress());
                }

                PartnerInfoFragment caught = new PartnerInfoFragment();
                caught.setCancelable(false);
                caught.show(getFragmentManager().beginTransaction(), "Caught partner");
                String partnerInfo = String.format("%s\t%s, %s", currentTime, caughtPartnerID, caughtPartnerAddress);

                caught.setAllFragmentData(partnerInfo, caughtPartnerField, matchCategoryToImage(caughtPartnerField));
                Log.d("POOP", "TEST");


            }
        });

        rightCard.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
               String currentTime = getCurrentTimeStamp();

                for (Challenge challenge : relatedChallengesRandom ) {
                    challenge.incrementProgress();
                }

                savePartner(randomPartnerField, randomPartnerID, currentTime);

                PartnerInfoFragment caught = new PartnerInfoFragment();
                caught.setCancelable(false);
                caught.show(getFragmentManager().beginTransaction(), "Caught partner");
                String partnerInfo = String.format("%s\t%s, %s", currentTime, randomPartnerID, randomPartnerAddress);

                caught.setAllFragmentData(partnerInfo, randomPartnerField, matchCategoryToImage(randomPartnerField));
                Log.d("POOP", "TEST");
            }
        });
    }

    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    public void onCardButtonClick(View v) {
        final int upper = R.id.card_button_upper;
        final int lower = R.id.card_button_lower;

       savePlanes(this);
       savePartners(this);
       saveChallenges(this);

        switch (v.getId()) {
            case upper: {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
            }
            case lower: {
                Intent intent = new Intent(this, PlaneCollectionActivity.class);
                intent.putExtra(whichWasCaughtMessage, whichCaught);
                intent.putExtra(catchMessagePartners, partnerCollectionHashMap);
                intent.putExtra(catchMessagePlanes, planeCollectionHashMap);
                startActivity(intent);
                finish();
                break;
            }
            default: {
                Log.d("Card button click", "Something went wrong!");
            }
        }
    }

    public void savePlane(String planeType, String country) {

        try {
            planeCollectionHashMap.get(planeType).add(country);
        }
        catch (java.lang.NullPointerException nil) {
            HashSet<String> addMe = new HashSet<>();
            addMe.add(country);

            planeCollectionHashMap.put(planeType, addMe);
        }

        Log.d("Plane saving: ", planeCollectionHashMap.toString());

    }

    public void savePartner(String field, String name, String time){
        // All apps (root or not) have a default data directory, which is /data/data/<package_name>

        try {
            partnerCollectionHashMap.get(field).add(String.format("%s %s", time, name));
        }
        catch (java.lang.NullPointerException nil) {
            HashSet<String> addMe = new HashSet<>();
            addMe.add(String.format("%s %s", time, name));

            partnerCollectionHashMap.put(field, addMe);
        }

        Log.d("Partner saving: ", partnerCollectionHashMap.toString());
    }

    private String formatPlanes() {
        String result = "";

        for (String planeType : planeCollectionHashMap.keySet()) {
            Iterator<String> row = planeCollectionHashMap.get(planeType).iterator();

            result += planeType;

            while (row.hasNext()) {
                result += String.format("#%s", row.next());
            }

            result += "\n";

        }
        return result;
    }

    private String formatPartners() {
        String result = "";

        for (String  category : partnerCollectionHashMap.keySet()) {
            Iterator<String> row = partnerCollectionHashMap.get(category).iterator();

            result += category;

            while (row.hasNext()) {
                result += String.format("#%s", row.next());
            }

            result += "\n";

        }
        return result;
    }

    private String formatChallenges() {
        JSONArray result = new JSONArray();

        for ( Challenge c : activeChallenges ) {
            result.put(c.saveChallenge());
        }

        return result.toString();
    }

    public void savePlanes(Context context){

        String result = formatPlanes();

        try {
            FileOutputStream outputStream = context.openFileOutput(USER_DATA_LOCATION_PLANES, Context.MODE_PRIVATE);
            outputStream.write(result.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("Plane Saving", result);
    }

    public void savePartners(Context context){

        String result = formatPartners();

        try {
            FileOutputStream outputStream = context.openFileOutput(USER_DATA_LOCATION_PARTNERS, Context.MODE_PRIVATE);
            outputStream.write(result.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("Partner Saving", result);
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

    @Override
    public void onPlaneDialogPositiveClick(DialogFragment dialog) {

    }

}
