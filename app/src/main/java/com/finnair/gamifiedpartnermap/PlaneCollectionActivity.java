package com.finnair.gamifiedpartnermap;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import static com.finnair.gamifiedpartnermap.CardSelectionActivity.whichWasCaughtMessage;
import static com.finnair.gamifiedpartnermap.MainActivity.catchMessagePartners;
import static com.finnair.gamifiedpartnermap.MainActivity.catchMessagePlanes;


/**
 * Created by ala-hazla on 8.2.2018.
 */

public class PlaneCollectionActivity extends AppCompatActivity implements PlaneCatchFragment.PlaneCatchListener {


    private static final HashMap<String, Integer> modelsToImages;
    private HashMap<String, HashSet<String>> planeCollectionHashMap;
    private HashMap<String, HashSet<String>> partnerCollectionHashMap;
    private TabLayout collectionTabs;
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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plane_collection_layout);

        Intent intent = getIntent();
        planeCollectionHashMap = (HashMap<String, HashSet<String>>) intent.getSerializableExtra(catchMessagePlanes);

        Log.d("Collection", "" + (planeCollectionHashMap.size()));

        partnerCollectionHashMap = (HashMap<String, HashSet<String>>) intent.getSerializableExtra(catchMessagePartners);
        boolean openTab = (boolean) intent.getSerializableExtra(whichWasCaughtMessage);

        final LinearLayout collection = findViewById(R.id.collected_items);
        final LayoutInflater inflater = getLayoutInflater();
        collectionTabs = findViewById(R.id.collection_tab);

        ((ImageButton)(findViewById(R.id.my_collection).
                findViewById(R.id.toolbar).
                findViewById(R.id.finnair_logo_button))).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishMe();
            }
        });

        if (openTab) {
            collectionTabs.getTabAt(0).select();
            populatePlanes(collection, inflater);
        }
        else {
            collectionTabs.getTabAt(1).select();
            populatePartners(collection, inflater);
        }

        collectionTabs.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab == collectionTabs.getTabAt(0)) populatePlanes(collection, inflater);
                else populatePartners(collection, inflater);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                collection.removeAllViews();
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


    }


    void populatePlanes(LinearLayout baseLayout, LayoutInflater inflater) {
        for (String planeType : planeCollectionHashMap.keySet()) {
            Log.d("Collection: " , planeType);
            TableRow row = new TableRow(this);

            ConstraintLayout card = (ConstraintLayout) inflater.inflate(R.layout.collection_row_layout, row, false);

            TextView name = (TextView) card.findViewById(R.id.plane_model_text);
            name.setText(planeType);

            ImageView image = (ImageView) card.findViewById(R.id.plane_model_image);
            image.setImageResource(modelsToImages.get(planeType));

            TextView collectedOutOf = (TextView) card.findViewById(R.id.routes_collected_counter);
            collectedOutOf.setText(this.planeCollectionHashMap.get(planeType).size() + "/50");

            ProgressBar collectedProgress = (ProgressBar) card.findViewById(R.id.challenge_collected_progress);
            collectedProgress.setProgress(this.planeCollectionHashMap.get(planeType).size());
            row.addView(card);

            baseLayout.addView(row);
        }
    }

    void populatePartners(LinearLayout baseLayout, LayoutInflater inflater) {
        for (String category : partnerCollectionHashMap.keySet()) {
            Log.d("Collection: " , category);
            TableRow row = new TableRow(this);

            ConstraintLayout card = (ConstraintLayout) inflater.inflate(R.layout.collection_row_layout, row, false);

            TextView name = (TextView) card.findViewById(R.id.plane_model_text);
            name.setText(category);

            ImageView image = (ImageView) card.findViewById(R.id.plane_model_image);
            image.setImageResource(matchCategoryToImage(category));

            TextView collectedOutOf = (TextView) card.findViewById(R.id.routes_collected_counter);
            collectedOutOf.setText(partnerCollectionHashMap.get(category).size() + "/50");

            ProgressBar collectedProgress = (ProgressBar) card.findViewById(R.id.challenge_collected_progress);
            collectedProgress.setProgress(partnerCollectionHashMap.get(category).size());
            row.addView(card);

            baseLayout.addView(row);
        }
    }


    public void onInfoClick(View v) {
        if (collectionTabs.getSelectedTabPosition() == 0) {
            PlaneCatchFragment caught = new PlaneCatchFragment();
            caught.show(this.getFragmentManager().beginTransaction(), "Caught plane");

            LinearLayout parentLayout = (LinearLayout) v.getParent();
            String planeModel = "" + ((TextView) parentLayout.findViewById(R.id.plane_model_text)).getText();
            Iterator<String> countriesIterator = planeCollectionHashMap.get(planeModel).iterator();
            String collectedCountries = "";

            while (countriesIterator.hasNext()) {
                collectedCountries += countriesIterator.next() + "\n";
            }

            caught.setAllFragmentData(planeModel, collectedCountries, modelsToImages.get(planeModel));
        }
        else {
            PartnerInfoFragment caught = new PartnerInfoFragment();
            caught.show(this.getFragmentManager().beginTransaction(), "Caught plane");

            LinearLayout parentLayout = (LinearLayout) v.getParent();
            String category = "" + ((TextView) parentLayout.findViewById(R.id.plane_model_text)).getText();
            Iterator<String> partnersIterator = partnerCollectionHashMap.get(category).iterator();
            String collectedPartners = "";

            while (partnersIterator.hasNext()) {
                collectedPartners += partnersIterator.next() + "\n";
            }

            caught.setAllFragmentData(collectedPartners, category, matchCategoryToImage(category));
        }




    }

    public void onCardButtonClick(View v) {
        final int upper = R.id.card_button_upper;
        final int lower = R.id.card_button_lower;

        switch (v.getId()) {
            case upper: {
                finishMe();
                break;
            }
            case lower: {
                ((DialogFragment) this.getFragmentManager().findFragmentByTag("Caught plane")).dismiss();
                break;
            }
            default: {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    private void finishMe() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onPlaneDialogPositiveClick(DialogFragment dialog) {
        Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT);
    }
}
