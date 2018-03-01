package com.finnair.gamifiedpartnermap;

import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.HashMap;
import java.util.HashSet;

import static com.finnair.gamifiedpartnermap.MainActivity.planeCatchMessage;

/**
 * Created by huzla on 1.3.2018.
 */

public class CardSelectionActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_choose_layout);

        Intent intent = getIntent();

       /*LinearLayout collection = findViewById(R.id.my_collection);
        LayoutInflater inflater = getLayoutInflater();*/


        /*PlaneCatchFragment caught = new PlaneCatchFragment();
        caught.show(getFragmentManager().beginTransaction(), "Caught plane");
        caught.setAllFragmentData(plane.getIcao24(), plane.getOriginCountry());
        Log.d("POOP", plane.getOriginCountry());*/

    }



}
