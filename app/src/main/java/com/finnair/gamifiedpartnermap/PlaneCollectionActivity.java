package com.finnair.gamifiedpartnermap;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.finnair.gamifiedpartnermap.MainActivity.planeCatchMessage;

/**
 * Created by ala-hazla on 8.2.2018.
 */

public class PlaneCollectionActivity extends AppCompatActivity implements PlaneCatchFragment.PlaneCatchListener {


    private static final HashMap<String, Integer> modelsToImages;
    private HashMap<String, HashSet<String>> collectionHashMap;
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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plane_collection_layout);

        Intent intent = getIntent();
        collectionHashMap = (HashMap<String, HashSet<String>>) intent.getSerializableExtra(planeCatchMessage);

        LinearLayout collection = findViewById(R.id.my_collection);
        LayoutInflater inflater = getLayoutInflater();


        for (String planeType : collectionHashMap.keySet()) {
            Log.d("Collection: " , planeType);
            TableRow row = new TableRow(this);

                    ConstraintLayout card = (ConstraintLayout) inflater.inflate(R.layout.collection_row_layout, row, false);

                    TextView name = (TextView) card.findViewById(R.id.plane_model_text);
                    name.setText(planeType);

                    ImageView image = (ImageView) card.findViewById(R.id.plane_model_image);
                    image.setImageResource(modelsToImages.get(planeType));

                    TextView collectedOutOf = (TextView) card.findViewById(R.id.routes_collected_counter);
                    collectedOutOf.setText(collectionHashMap.get(planeType).size() + "/50");

                    ProgressBar collectedProgress = (ProgressBar) card.findViewById(R.id.routes_collected_progress);
                    collectedProgress.setProgress(collectionHashMap.get(planeType).size());
                    row.addView(card);

            collection.addView(row);
        }

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);

        myToolbar.setTitle("Your collection");
        setSupportActionBar(myToolbar);
       getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    //TODO: rework these two once the card layout has been finished.

    public void onPlaneInfoClick(View v) {
        PlaneCatchFragment caught = new PlaneCatchFragment();
        caught.show(this.getFragmentManager().beginTransaction(), "Caught plane");

        LinearLayout parentLayout = (LinearLayout) v.getParent();
        String planeModel = "" + ((TextView) parentLayout.findViewById(R.id.plane_model_text)).getText();
        String randomCountry = collectionHashMap.get(planeModel).iterator().next();

        caught.setAllFragmentData(planeModel, randomCountry);


    }

    public void onCardButtonClick(View v) {
        final int upper = R.id.card_button_upper;
        final int lower = R.id.card_button_lower;

        switch (v.getId()) {
            case upper: {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            }
            case lower: {
                ((DialogFragment) this.getFragmentManager().findFragmentByTag("Caught plane")).dismiss();
                break;
            }
            default: {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onPlaneDialogPositiveClick(DialogFragment dialog) {
        Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT);
    }
}
