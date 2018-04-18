package com.finnair.gamifiedpartnermap;

import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.hasToString;

/**
 * Created by noctuaPC on 7.4.2018.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class EspressoTest {
    /***
     * 1. Klikkaa Partner-nappulaa
     * 2. Avaa "Hotellit"
     * 3. Valitse "Hotel Kämp"
     * 4. Palaa kartalle
     *
     *
     * 1. Poimi Partneri
     * 2. Varmista, että poimittujen Partnerien määrä kasvoi yhdellä
     *
     */
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule =
            new ActivityTestRule(MainActivity.class);

    @Test
    public void openPartnerThroughDropDownMenu() {

        // Open PARTNERS list from upper right corner:
        onView( withId(R.id.toolbar_partners_button) ).perform(click());

        // Select "Hotel" category from listview:
        onData( // Must use onData, because we are dealing with a list which may not be fully visible. So onView() doesn't work here
                hasToString("Hotel")
        ).perform(click());

        // Click "Hotel Kämp":
        onData(
                hasToString("Hotel Kämp")
        ).perform(click());

        onData( withId(R.id.finnair_logo_button) ).perform(click());

    }
}
