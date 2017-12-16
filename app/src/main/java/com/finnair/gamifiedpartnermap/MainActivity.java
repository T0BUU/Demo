package com.finnair.gamifiedpartnermap;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

/**
 * Created by ala-hazla on 16.12.2017.
 */

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        android.support.design.widget.TabLayout tabLayout = findViewById(R.id.tab_layout);

        Log.i("Error", "!!!!!!!!!" + tabLayout.toString() + "!!!!!!!!!!!!!!!!");

        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_profile), 0);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_map), 1);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_game), 2);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_settings), 3);

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());

        viewPager.setAdapter(adapter);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


    }


}
