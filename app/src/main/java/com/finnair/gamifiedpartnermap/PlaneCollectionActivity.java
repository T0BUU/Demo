package com.finnair.gamifiedpartnermap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.view.LayoutInflater;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * Created by ala-hazla on 8.2.2018.
 */

public class PlaneCollectionActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plane_collection_layout);

        Intent intent = getIntent();
        String message = intent.getStringExtra("123");


        TableLayout collection = (TableLayout) findViewById(R.id.collection_table);
        LayoutInflater inflater = getLayoutInflater();


        int index = 0;
        String[] planeNames =  message.split(" ");

        while (index < planeNames.length) {
            TableRow row = new TableRow(this);

            for (int i = 0; i < 4; ++i) {
                try {
                    TableLayout card = (TableLayout) inflater.inflate(R.layout.card_layout, row, false);
                    TextView name = (TextView) card.findViewById(R.id.plane_name);
                    name.setText(planeNames[index]);
                    row.addView(card);
                    index += 1;
                }
                catch (IndexOutOfBoundsException e) {

                }
            }

            collection.addView(row);
        }


    }
}