package com.finnair.gamifiedpartnermap;

/**
 * Created by huzla on 2.12.2017.
 */
import android.os.Build;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;

import org.junit.internal.MethodSorter;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.util.Log;

//Test the functionality used to ask for location permissions.
//THIS WILL FAIL IF PERMISSION HAS ALREADY BEEN GIVEN!
//These tests were implemented before the switch to fragment based tabs!
@RunWith(AndroidJUnit4.class)
@LargeTest

public class PermissionEspressoTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);


    @Test
    public void TestSystemPermission() {
        boolean buttonsFound = true;

            UiDevice device = UiDevice.getInstance(getInstrumentation());
            UiObject allowPermissions;
            if (Build.VERSION.SDK_INT == 24) {
                allowPermissions = device.findObject(new UiSelector().text("DENY"));
            }
            else allowPermissions = device.findObject(new UiSelector().text("Deny"));

            if (allowPermissions.exists()) {
                try {
                    Log.i("Info", "found Deny");
                    allowPermissions.click();
                } catch (UiObjectNotFoundException e) {
                    buttonsFound = false;
                    Log.i("Assert", "There is no permissions dialog to interact with ");
                }
            }
            assert(buttonsFound);
            onView(withId(R.id.permissionOk)).check(matches(isDisplayed()));
            onView(withId(R.id.permissionOk)).perform(click());


        if (Build.VERSION.SDK_INT == 24) {
            allowPermissions = device.findObject(new UiSelector().text("ALLOW"));
        }
        else allowPermissions =  device.findObject(new UiSelector().text("Allow"));
            if (allowPermissions.exists()) {
                try {
                    allowPermissions.click();
                } catch (UiObjectNotFoundException e) {
                    buttonsFound = false;
                    Log.i("assert", "There is no permissions dialog to interact with ");
                }
            }

            assert (buttonsFound);

    }

}
