package com.finnair.gamifiedpartnermap;

import android.app.DialogFragment;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static com.finnair.gamifiedpartnermap.MainActivity.planeCatchMessage;
import static com.finnair.gamifiedpartnermap.MainActivity.planesCaught;

/**
 * Created by huzla on 1.3.2018.
 */

public class CardSelectionActivity extends AppCompatActivity implements PlaneCatchFragment.PlaneCatchListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_choose_layout);

        Intent intent = getIntent();
        final ArrayList<String> planes = (ArrayList<String>) intent.getSerializableExtra(planesCaught);

        View leftCard = (View)findViewById(R.id.card_left);
        View rightCard = (View)findViewById(R.id.card_right);

        ((TextView)leftCard.findViewById(R.id.plane_name)).setText(planes.get(0));
        ((TextView)leftCard.findViewById(R.id.plane_description)).setText(planes.get(1));

        leftCard.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                PlaneCatchFragment caught = new PlaneCatchFragment();
                caught.show(getFragmentManager().beginTransaction(), "Caught plane");
                caught.setAllFragmentData(planes.get(0), planes.get(1));
                Log.d("POOP", "TEST");
            }
        });

        rightCard.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                PlaneCatchFragment caught = new PlaneCatchFragment();
                caught.show(getFragmentManager().beginTransaction(), "Caught plane");
                caught.setAllFragmentData(planes.get(2), planes.get(3));
                Log.d("POOP", "TEST");
            }
        });


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
                Intent intent = new Intent(this, PlaneCollectionActivity.class);
                //intent.putExtra(planeCatchMessage, this.myMainLayout.getCollection());
                startActivity(intent);
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

    }



}
