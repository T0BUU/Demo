package com.finnair.gamifiedpartnermap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.concurrent.ConcurrentHashMap;

import static com.finnair.gamifiedpartnermap.MainActivity.planeCatchMessage;

/**
 * Created by ala-hazla on 8.2.2018.
 */

public class PlaneCollectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*setContentView(R.layout.plane_collection_layout);

        Intent intent = getIntent();
        String message = intent.getStringExtra(planeCatchMessage);
        ConcurrentHashMap<String, ConcurrentHashMap<String, String>> collectionHashMap = new ConcurrentHashMap<>();

        for (String row : message.split("\n")) {

                String[] firstSplit = row.split("#");
                ConcurrentHashMap<String, String> planes = new ConcurrentHashMap<>();

                for (int i = 1; i < firstSplit.length; ++i) {
                    String[] pair = firstSplit[i].split(":");

                    planes.put(pair[0], pair[1]);
                }

                collectionHashMap.put(firstSplit[0], planes);

        }

        LinearLayout collection = findViewById(R.id.my_collection);
        LayoutInflater inflater = getLayoutInflater();


        for (String planeType : collectionHashMap.keySet()) {
            TableRow row = new TableRow(this);

                    TableLayout card = (TableLayout) inflater.inflate(R.layout.card_layout, row, false);
                    TextView name = (TextView) card.findViewById(R.id.plane_name);
                    name.setText(planeNames[index]);
                    row.addView(card);
                    index += 1;

            collection.addView(row);
        }

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);

        myToolbar.setTitle("Your collection");
        setSupportActionBar(myToolbar);
       getSupportActionBar().setDisplayHomeAsUpEnabled(true);

*/
    }
}
