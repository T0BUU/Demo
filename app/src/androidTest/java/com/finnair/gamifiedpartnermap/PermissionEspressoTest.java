package com.finnair.gamifiedpartnermap;

/**
 * Created by huzla on 2.12.2017.
 */
import android.os.Build;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.junit.runner.RunWith;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.util.Log;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class PermissionEspressoTest {

    @Rule
    public ActivityTestRule<MapsActivity> mActivityRule = new ActivityTestRule<>(
            MapsActivity.class);


    @Test
    public void allowPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT >= 23) {
            UiDevice device = UiDevice.getInstance(getInstrumentation());
            UiObject allowPermissions = device.findObject(new UiSelector().text("DENY"));
            if (allowPermissions.exists()) {
                try {
                    allowPermissions.click();
                } catch (UiObjectNotFoundException e) {
                    Log.i("assert", "There is no permissions dialog to interact with ");
                }
            }
        }
    }

}
