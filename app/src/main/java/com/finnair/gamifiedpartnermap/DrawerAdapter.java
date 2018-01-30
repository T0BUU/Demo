package com.finnair.gamifiedpartnermap;

import android.support.v4.app.Fragment;
import android.view.MenuItem;

/**
 * Created by Otto on 11.1.2018.
 */

//This class is for instantiating and handling drawer events.
public class DrawerAdapter {

    //Instantiate all fragments here.
    private Fragment mapsFragment = new MapsFragment();
    private Fragment profileFragment = new ProfileFragment();
    private Fragment settingsFragment = new SettingsFragment();

    int numOfFragments = 3;

    public DrawerAdapter(){}

    public Fragment getItem(int position){
        switch(position){
            case 0  : return mapsFragment;
            case 1  : return profileFragment;
            case 2  : return settingsFragment;
            default : return null;
        }
    }

    public int getNumOfFragments(){
        return numOfFragments;
    }

}
