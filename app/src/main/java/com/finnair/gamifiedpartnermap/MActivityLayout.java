package com.finnair.gamifiedpartnermap;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

/**
 * Created by Otto on 12.2.2018.
 */

public class MActivityLayout implements View.OnClickListener {

    private DrawerLayout drawerLayout;
    private PartnerListWindow partnerListWindow;  //Partner list popup window
    private Fragment mapFragment;
    private Activity mActivity;
    private FragmentManager fragmentManager;

    public MActivityLayout(){}

    public void createUI(Activity act, FragmentManager fm){

        //Set activity, fragmentManager and create new MapsFragment.
        mActivity = act;
        fragmentManager = fm;
        mapFragment = new MapsFragment();

        /* Set content view to MainActivity;
         * activity_main contains drawerLayout which has toolbar and FrameLayout(main_content) as its main view,
         * and user profile as its drawer view.  */
        mActivity.setContentView(R.layout.activity_main);
        drawerLayout = (DrawerLayout) mActivity.findViewById(R.id.drawer_layout);

        partnerListWindow = new PartnerListWindow(mActivity);

        //Navigation buttons
        Button loginButton = (Button) mActivity.findViewById(R.id.button_login);
        ImageButton settingsButton = (ImageButton) mActivity.findViewById(R.id.button_settings);
        Button partnersButton = (Button) mActivity.findViewById(R.id.toolbar_partners_button);
        Button drawerToggle = (Button) mActivity.findViewById(R.id.open_drawer_button);

        //Set click listeners to all buttons
        loginButton.setOnClickListener(this);
        settingsButton.setOnClickListener(this);
        partnersButton.setOnClickListener(this);
        drawerToggle.setOnClickListener(this);

        fragmentManager.beginTransaction()
                .replace(R.id.main_content, mapFragment)
                .commit();


    }

    //Handle button click events here.
    @Override
    public void onClick(View view){
        switch(view.getId()){
            case R.id.button_settings:
                fragmentManager.beginTransaction()
                        .replace(R.id.main_content, mapFragment)
                        .commit();
                drawerLayout.closeDrawer(GravityCompat.START);
                break;
            case R.id.button_login:
                drawerLayout.closeDrawer(GravityCompat.START);
                break;
            case R.id.finnair_logo_button:
                fragmentManager.beginTransaction()
                        .replace(R.id.main_content, mapFragment)
                        .commit();
                break;
            case R.id.toolbar_partners_button:                   //Open partner popup window.
                partnerListWindow.printPartners();
                partnerListWindow.createPopupWindow();
                break;
            case R.id.open_drawer_button:                        //Open drawer, (Profile icon)
                drawerLayout.openDrawer(GravityCompat.START);
            default: break;
        }
    }

    public void setMapVisible() {
        //Insert map fragment to main_content view.
    }

}
