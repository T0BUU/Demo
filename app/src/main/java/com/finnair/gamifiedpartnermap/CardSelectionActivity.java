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

public class CardSelectionActivity extends CollectionSavingActivity implements PlaneCatchFragment.PlaneCatchListener {




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

    private ArrayList<Challenge> relatedChallengesCaught;
    private ArrayList<Challenge> relatedChallengesRandom;

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
                    activeChallenges.get(challenge.getIndex()).incrementProgress();
                }


                PlaneCatchFragment caught = new PlaneCatchFragment();
                caught.setCancelable(false);
                caught.show(getFragmentManager().beginTransaction(), "Caught plane");
                caught.setAllFragmentData(caughtPlaneType,caughtCountry, modelsToImages.get(caughtPlaneType), 1, 5);
                Log.d("POOP", "TEST");


            }
        });

        rightCard.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                savePlane(randomPlaneType, randomCountry);

                for (Challenge challenge : relatedChallengesRandom ) {
                    activeChallenges.get(challenge.getIndex()).incrementProgress();
                }

                PlaneCatchFragment caught = new PlaneCatchFragment();
                caught.setCancelable(false);
                caught.show(getFragmentManager().beginTransaction(), "Caught plane");
                caught.setAllFragmentData(randomPlaneType, randomCountry,  modelsToImages.get(randomPlaneType), 1, 5);
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
                    activeChallenges.get(challenge.getIndex()).incrementProgress();
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

    @Override
    public void onPlaneDialogPositiveClick(DialogFragment dialog) {

    }

}
