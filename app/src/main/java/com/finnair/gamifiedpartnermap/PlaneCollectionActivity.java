package com.finnair.gamifiedpartnermap;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static com.finnair.gamifiedpartnermap.CardSelectionActivity.whichWasCaughtMessage;
import static com.finnair.gamifiedpartnermap.MainActivity.catchMessagePartners;
import static com.finnair.gamifiedpartnermap.MainActivity.catchMessagePlanes;
import static com.finnair.gamifiedpartnermap.MainActivity.isLoggedInMessage;
import static com.finnair.gamifiedpartnermap.PlaneCatchFragment.CardLevel.BASIC;
import static com.finnair.gamifiedpartnermap.PlaneCatchFragment.CardLevel.GOLD;
import static com.finnair.gamifiedpartnermap.PlaneCatchFragment.CardLevel.LUMO;
import static com.finnair.gamifiedpartnermap.PlaneCatchFragment.CardLevel.PLATINUM;
import static com.finnair.gamifiedpartnermap.PlaneCatchFragment.CardLevel.SILVER;


/**
 * Created by ala-hazla on 8.2.2018.
 */

public class PlaneCollectionActivity extends AppCompatActivity implements PlaneCatchFragment.PlaneCatchListener {


    private static final HashMap<String, Integer> modelsToImages;
    private HashMap<String, HashSet<String>> planeCollectionHashMap;
    private HashMap<String, HashSet<String>> partnerCollectionHashMap;
    private HashMap<String, PlaneCatchFragment.CardLevel> cardLevelHashMap = new HashMap<>();
    private String USER_DATA_LOCATION_CARD_LEVELS = "myCardLevels";
    private ArrayList<Reward> availableRewards;
    private Random generator = new Random();
    private TabLayout collectionTabs;
    private boolean isLoggedIn;
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

        readRewards();

        Intent intent = getIntent();
        planeCollectionHashMap = (HashMap<String, HashSet<String>>) intent.getSerializableExtra(catchMessagePlanes);

        isLoggedIn = intent.getBooleanExtra(isLoggedInMessage, false);

        Log.d("Collection", "" + (planeCollectionHashMap.size()));

        partnerCollectionHashMap = (HashMap<String, HashSet<String>>) intent.getSerializableExtra(catchMessagePartners);
        boolean openTab = (boolean) intent.getSerializableExtra(whichWasCaughtMessage);

        final LinearLayout collection = findViewById(R.id.collected_items);
        final LayoutInflater inflater = getLayoutInflater();
        collectionTabs = findViewById(R.id.collection_tab);

        findViewById(R.id.toolbar).findViewById(R.id.open_drawer_button).getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        ((Button) findViewById(R.id.toolbar).findViewById(R.id.toolbar_partners_button)).setTextColor(Color.GRAY);

        ((ImageButton)(findViewById(R.id.my_collection).
                findViewById(R.id.toolbar).
                findViewById(R.id.finnair_logo_button))).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishMe();
            }
        });

        readCollectedCardLevels(this);

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

    private void readRewards() {
        //TODO: Replace this with a call to firebase.
        InputStream is = getResources().openRawResource(R.raw.sample_prizes);
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

        availableRewards = new ArrayList<>();

        for (int i = 0; i < json.length(); ++i) {
            Log.d("Constructing rewards", "" + i);
            try {
                    Log.d("Current json obj", json.get(i).toString());
                    Reward current = new Reward((JSONObject) json.get(i));
                    availableRewards.add(current);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }


    void populatePlanes(LinearLayout baseLayout, LayoutInflater inflater) {
        for (String planeType : planeCollectionHashMap.keySet()) {
            Log.d("Collection: " , planeType);
            TableRow row = new TableRow(this);
            int amountCollected = this.planeCollectionHashMap.get(planeType).size();
            PlaneCatchFragment.CardLevel cardLevel = getCardLevel(planeType);

            Log.d("Card Level", "" + (cardLevel == null));

            ConstraintLayout card = (ConstraintLayout) inflater.inflate(R.layout.collection_row_layout, row, false);

            setBorderColor(card, cardLevel);

            TextView name = (TextView) card.findViewById(R.id.plane_model_text);
            name.setText(planeType);

            ImageView image = (ImageView) card.findViewById(R.id.plane_model_image);
            image.setImageResource(modelsToImages.get(planeType));

            TextView collectedOutOf = (TextView) card.findViewById(R.id.routes_collected_counter);
            TextView infoText = (TextView) card.findViewById(R.id.collection_info_text);
            ProgressBar collectedProgress = (ProgressBar) card.findViewById(R.id.challenge_collected_progress);
            setCollectedText(collectedOutOf, collectedProgress, amountCollected, cardLevel, infoText);

            row.addView(card);

            baseLayout.addView(row);
        }
    }

    private PlaneCatchFragment.CardLevel getCardLevel(String key) {

        PlaneCatchFragment.CardLevel level = cardLevelHashMap.get(key);

        if (level == null) {
            cardLevelHashMap.put(key, BASIC);
            return BASIC;
        }

        return level;

    }

    private void setCollectedText(TextView textView, ProgressBar progressBar,
                                  int amountCollected, PlaneCatchFragment.CardLevel cardLevel,
                                    TextView infoText) {
        int max;
        int currentProgress;

        switch (cardLevel) {
            case BASIC:
                max = 5;
                currentProgress = amountCollected - 1;
                break;
            case SILVER:
                max = 10;
                currentProgress = amountCollected - 6;
                break;
            case GOLD:
                max = 50;
                currentProgress = amountCollected - 16;
                break;
            case PLATINUM:
                max = 100;
                currentProgress = amountCollected - 66;
                break;
            case LUMO:
                max = 500;
                currentProgress = amountCollected - 166;
                break;
            default:
                max = 1;
                currentProgress = 1;
        }

        progressBar.setMax(max);

        if (currentProgress >= max) {
            textView.setText(String.format("%d/%d", max, max));
            progressBar.setProgress(max);
            infoText.setText(R.string.collection_level_up_info);
            infoText.setTextColor(Color.argb(0xFF,0xb4, 0xcb, 0x66));
            progressBar.getProgressDrawable().setColorFilter(Color.argb(0xFF,0xb4, 0xcb, 0x66), PorterDuff.Mode.SRC_IN);
        }
        else {
            textView.setText(String.format("%d/%d", currentProgress, max));
            infoText.setText(R.string.collection_more_info);
            infoText.setTextColor(getResources().getColor(R.color.nordicBlue));
            progressBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.nordicBlue), PorterDuff.Mode.SRC_IN);
            progressBar.setProgress(currentProgress);
        }

    }


    private void setBorderColor(View view, PlaneCatchFragment.CardLevel cardLevel) {
        String levelColor;

        switch (cardLevel) {
            case BASIC:
                levelColor = "#FFE9E8E8";
                break;
            case SILVER:
                levelColor = "#FFC0C0C0";
                break;
            case GOLD:
                levelColor = "#FFFFD700";
                break;
            case PLATINUM:
                levelColor = "#FFA6C6EE";
                break;
            case LUMO:
                levelColor = "#FF000000";
                break;
            default:
                levelColor = "#FFCCCCCC";
        }

        Log.d("Color", levelColor);
       ((GradientDrawable)view.getBackground()).setStroke(10, Color.parseColor(levelColor));
    }

    private PlaneCatchFragment.CardLevel levelUpCard(String key) {
       PlaneCatchFragment.CardLevel cardLevel = cardLevelHashMap.get(key);

        switch (cardLevel) {
            case BASIC:
                cardLevelHashMap.put(key, SILVER);
                break;
            case SILVER:
                cardLevelHashMap.put(key, GOLD);
                break;
            case GOLD:
                cardLevelHashMap.put(key, PLATINUM);
                break;
            case PLATINUM:
                cardLevelHashMap.put(key, LUMO);
                break;
            default:
                cardLevelHashMap.put(key, LUMO);
        }

        return cardLevelHashMap.get(key);
    }

    void populatePartners(LinearLayout baseLayout, LayoutInflater inflater) {
        for (String category : partnerCollectionHashMap.keySet()) {
            Log.d("Collection: " , category);
            TableRow row = new TableRow(this);
            int amountCollected = this.partnerCollectionHashMap.get(category).size();
            PlaneCatchFragment.CardLevel cardLevel = getCardLevel(category);

            ConstraintLayout card = (ConstraintLayout) inflater.inflate(R.layout.collection_row_layout, row, false);

            setBorderColor(card, cardLevel);

            TextView name = (TextView) card.findViewById(R.id.plane_model_text);
            name.setText(category);

            ImageView image = (ImageView) card.findViewById(R.id.plane_model_image);
            image.setImageResource(matchCategoryToImage(category));

            TextView collectedOutOf = (TextView) card.findViewById(R.id.routes_collected_counter);
            TextView infoText = (TextView) card.findViewById(R.id.collection_info_text);

            ProgressBar collectedProgress = (ProgressBar) card.findViewById(R.id.challenge_collected_progress);
            setCollectedText(collectedOutOf, collectedProgress, amountCollected, cardLevel, infoText);

            row.addView(card);

            baseLayout.addView(row);
        }
    }

    public void redeemReward(TextView v) {

        ConstraintLayout parentLayout = (ConstraintLayout) v.getParent().getParent();
        String key = "" + ((TextView) parentLayout.findViewById(R.id.plane_model_text)).getText();
        PlaneCatchFragment.CardLevel currentLevel = levelUpCard(key);

        setBorderColor(parentLayout, currentLevel);

        int amountCollected;

        if (collectionTabs.getSelectedTabPosition() == 0) {
            amountCollected = planeCollectionHashMap.get(key).size();
        }
        else {
            amountCollected = partnerCollectionHashMap.get(key).size();
        }

        TextView collectedOutOf = (TextView) parentLayout.findViewById(R.id.routes_collected_counter);
        TextView infoText = (TextView) parentLayout.findViewById(R.id.collection_info_text);
        ProgressBar collectedProgress = (ProgressBar) parentLayout.findViewById(R.id.challenge_collected_progress);
        setCollectedText(collectedOutOf, collectedProgress, amountCollected, currentLevel, v);

    }


    public void onInfoClick(View v) {
        TextView view = (TextView) v;

        if (view.getText().toString().equals(getString(R.string.collection_level_up_info))) {
            Reward randomReward = availableRewards.get(generator.nextInt(availableRewards.size()));

            RewardFragment rewardFragment = new RewardFragment();
            rewardFragment.show(this.getFragmentManager().beginTransaction(), "Show reward");

            rewardFragment.setAllFragmentData(randomReward.getDescription(), randomReward.getType(), randomReward.getImage(), randomReward.getAmount(), isLoggedIn, v);

            return;
        }

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

            caught.setAllFragmentData(planeModel, collectedCountries, modelsToImages.get(planeModel), planeCollectionHashMap.get(planeModel).size());
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

            caught.setAllFragmentData(collectedPartners, category, matchCategoryToImage(category), partnerCollectionHashMap.get(category).size());
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

    private String formatCardLevels() {
        String result = "";

        for (String key : cardLevelHashMap.keySet()) {

            result += String.format("%s#%s\n",key, cardLevelHashMap.get(key));

        }
        return result;
    }

    private void saveCardLevels(Context context){

        String result = formatCardLevels();

        try {
            FileOutputStream outputStream = context.openFileOutput(USER_DATA_LOCATION_CARD_LEVELS, Context.MODE_PRIVATE);
            outputStream.write(result.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("Cards Saving", result);
    }

    private void readCollectedCardLevels(Context context) {

        HashMap<String, PlaneCatchFragment.CardLevel> result = new HashMap<>();

        try {
            InputStream inputStream = context.openFileInput(USER_DATA_LOCATION_CARD_LEVELS);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";

                while ( (receiveString = bufferedReader.readLine()) != null ) {

                    String[] firstSplit = receiveString.split("#");
                    String cardLevel = firstSplit[1];

                    result.put(firstSplit[0], matchCardLevel(cardLevel));

                }

                inputStream.close();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        this.cardLevelHashMap = result;
    }


    private PlaneCatchFragment.CardLevel matchCardLevel(String string) {
        switch (string) {
            case "BASIC":
                return BASIC;
            case "SILVER":
                return SILVER;
            case "GOLD":
                return GOLD;
            case "PLATINUM":
                return PLATINUM;
            case "LUMO":
                return LUMO;
            default:
                return BASIC;
        }
    }

    private void finishMe() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onPause() {
        saveCardLevels(this);
        super.onPause();
    }

    @Override
    public void onPlaneDialogPositiveClick(DialogFragment dialog) {
        Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT);
    }
}
