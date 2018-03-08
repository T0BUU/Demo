package com.finnair.gamifiedpartnermap;

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

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import static com.finnair.gamifiedpartnermap.MainActivity.planeCatchMessage;
import static com.finnair.gamifiedpartnermap.MainActivity.planesCaught;
import static com.finnair.gamifiedpartnermap.PlaneMarkerClass.USER_DATA_LOCATION;

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


    private String caughtPlaneType;
    private String caughtCountry;

    private String randomPlaneType;
    private String randomCountry;

    private HashMap<String, HashSet<String>> collectionHashMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_choose_layout);

        Intent intent = getIntent();
        final ArrayList<String> planes = (ArrayList<String>) intent.getSerializableExtra(planesCaught);

       collectionHashMap = (HashMap<String, HashSet<String>>) intent.getSerializableExtra(planeCatchMessage);

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
                PlaneCatchFragment caught = new PlaneCatchFragment();
                caught.setCancelable(false);
                caught.show(getFragmentManager().beginTransaction(), "Caught plane");
                caught.setAllFragmentData(randomPlaneType, randomCountry,  modelsToImages.get(randomPlaneType));
                Log.d("POOP", "TEST");
            }
        });


    }

    public void onCardButtonClick(View v) {
        final int upper = R.id.card_button_upper;
        final int lower = R.id.card_button_lower;

       savePlanes(this);

        switch (v.getId()) {
            case upper: {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
            }
            case lower: {
                Intent intent = new Intent(this, PlaneCollectionActivity.class);
                intent.putExtra(planeCatchMessage, collectionHashMap);
                startActivity(intent);
                finish();
                break;
            }
            default: {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    public void savePlane(String planeType, String country) {

        try {
            collectionHashMap.get(planeType).add(country);
        }
        catch (java.lang.NullPointerException nil) {
            HashSet<String> addMe = new HashSet<>();
            addMe.add(country);

            collectionHashMap.put(planeType, addMe);
        }

        Log.d("Plane saving: ", collectionHashMap.toString());

    }

    private String formatPlanes() {
        String result = "";

        for (String planeType : collectionHashMap.keySet()) {
            Iterator<String> row = collectionHashMap.get(planeType).iterator();

            result += planeType;

            while (row.hasNext()) {
                result += String.format("#%s", row.next());
            }

            result += "\n";

        }
        return result;
    }

    public void savePlanes(Context context){

        String result = formatPlanes();

        try {
            FileOutputStream outputStream = context.openFileOutput(USER_DATA_LOCATION, Context.MODE_PRIVATE);
            outputStream.write(result.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("Plane Saving", result);
    }

    @Override
    public void onPlaneDialogPositiveClick(DialogFragment dialog) {

    }



}
