package com.finnair.gamifiedpartnermap;

import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.finnair.gamifiedpartnermap.MainActivity.activeChallengesMessage;
import static com.finnair.gamifiedpartnermap.MainActivity.catchMessagePartners;
import static com.finnair.gamifiedpartnermap.MainActivity.catchMessagePlanes;
import static com.finnair.gamifiedpartnermap.MainActivity.partnersCaught;
import static com.finnair.gamifiedpartnermap.MainActivity.planesCaught;
import static com.finnair.gamifiedpartnermap.MainActivity.relatedChallengesToPartners;
import static com.finnair.gamifiedpartnermap.MainActivity.relatedChallengesToPlanes;

/**
 * Created by huzla on 23.3.2018.
 */

//This activity is used to showcase cards the user has won from challenges.
public class CardRewardActivity extends CollectionSavingActivity implements PlaneCatchFragment.PlaneCatchListener {

    private ArrayList<ArrayList<Challenge>> relatedChallenges = new ArrayList<>();
    private ArrayList<ArrayList<String>> rewards = new ArrayList<>();
    private TextView cardsLeft;
    private ArrayList<Integer> indeces = new ArrayList<>();
    private TableLayout cardBack;
    private FrameLayout cardContainer;
    private DialogFragment cardDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get the planes, partners and related challenges.
        Intent intent = getIntent();
        ArrayList<String> planes = (ArrayList<String>) intent.getSerializableExtra(planesCaught);
        ArrayList<String> partners = (ArrayList<String>) intent.getSerializableExtra(partnersCaught);

        ArrayList<ArrayList<Challenge>> relatedChallengesPlanes = new Gson().fromJson(getIntent().getStringExtra(relatedChallengesToPlanes),
                                               new TypeToken<ArrayList<ArrayList<Challenge>>>(){}.getType());

        ArrayList<ArrayList<Challenge>> relatedChallengesPartners = new Gson().fromJson(getIntent().getStringExtra(relatedChallengesToPartners),
                new TypeToken<ArrayList<ArrayList<Challenge>>>(){}.getType());

        activeChallenges = intent.getParcelableArrayListExtra(activeChallengesMessage);
        planeCollectionHashMap = (HashMap<String, HashSet<String>>) intent.getSerializableExtra(catchMessagePlanes);
        partnerCollectionHashMap = (HashMap<String, HashSet<String>>) intent.getSerializableExtra(catchMessagePartners);

        //Add all related challenges with those related to planes first.
        //This assumes that the order of relatedChallengesX matches X.
        if (relatedChallengesPlanes != null) relatedChallenges.addAll(relatedChallengesPlanes);
        if (relatedChallengesPlanes != null) relatedChallenges.addAll(relatedChallengesPartners);

        //Parse planes and partners to rewards
        int i = 0;

        while (i < planes.size()) {
            ArrayList<String> current = new ArrayList<>();

            current.add(planes.get(i));
            current.add(planes.get(i+1));

            i += 2;

            rewards.add(current);
        }

        i = 0;

        while (i < partners.size()) {
            ArrayList<String> current = new ArrayList<>();

            current.add(partners.get(i));
            current.add(partners.get(i+1));
            current.add(partners.get(i+2));
            current.add(partners.get(i+3));

            i += 4;

            rewards.add(current);
        }

        i = 0;

        for(; i < rewards.size(); ++i) {
            indeces.add(i);
        }

        Collections.shuffle(indeces);

        //Save everything in case the user backs out.

        //Save everything in case the user backs out.

        for (ArrayList<Challenge> list : relatedChallenges) {
            for (Challenge challenge : list) {
                Log.d("Increase challenge", challenge.getDescription());
                activeChallenges.get(challenge.getIndex()).incrementProgress();
            }
        }

        savePartners(this);
        savePlanes(this);
        saveChallenges(this);

        setContentView(R.layout.card_reward_layout);

        cardsLeft = findViewById(R.id.card_reward_cards_left);
        cardsLeft.setText(String.format("%d cards left to redeem!", rewards.size()));

        findViewById(R.id.toolbar).findViewById(R.id.open_drawer_button).getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        ((Button) findViewById(R.id.toolbar).findViewById(R.id.toolbar_partners_button)).setTextColor(Color.GRAY);


        cardBack = findViewById(R.id.card_back);
        cardContainer = findViewById(R.id.card_container);

        final ScaleAnimation growAnim = new ScaleAnimation(1.0f, 1.15f, 1.0f, 1.15f);
        final ScaleAnimation shrinkAnim = new ScaleAnimation(1.15f, 1.0f, 1.15f, 1.0f);

        growAnim.setDuration(2000);
        shrinkAnim.setDuration(2000);

        cardContainer.setAnimation(growAnim);
        growAnim.start();

        growAnim.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation){}

            @Override
            public void onAnimationRepeat(Animation animation){}

            @Override
            public void onAnimationEnd(Animation animation)
            {
                cardContainer.setAnimation(shrinkAnim);
                shrinkAnim.start();
            }
        });
        shrinkAnim.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation){}

            @Override
            public void onAnimationRepeat(Animation animation){}

            @Override
            public void onAnimationEnd(Animation animation)
            {
                cardContainer.setAnimation(growAnim);
                growAnim.start();
            }
        });
    }

    private void updateCardVisibility() {
        if (indeces.size() == 0) {
            cardContainer.setAnimation(null);
            cardBack.setVisibility(GONE);

        }
    }

    public void onRewardCardClick(View view) {

        if (indeces.size() > 0) {
            int index = indeces.get(0);
            indeces.remove(0);

            updateCardVisibility();

            cardsLeft.setText(String.format("%d cards left to redeem!", indeces.size()));

            ArrayList<String> reward = rewards.get(index);

            if (reward.size() == 2) {
                PlaneCatchFragment caught = new PlaneCatchFragment();

                cardDialog = caught;
                caught.show(getFragmentManager().beginTransaction(), "Caught plane");
                caught.setAllFragmentData(reward.get(0), reward.get(1),  modelsToImages.get(reward.get(0)),  "", "OK", GONE, VISIBLE, 1);

            }
            else {
                PartnerInfoFragment caught = new PartnerInfoFragment();

                cardDialog = caught;
                caught.show(getFragmentManager().beginTransaction(), "Caught partner");
                String partnerInfo = String.format("%s\t%s, %s", getCurrentTimeStamp(), reward.get(1), reward.get(2));

                caught.setAllFragmentData(partnerInfo, reward.get(0), matchCategoryToImage(reward.get(0)), "", "OK", GONE, VISIBLE, 1);
            }


        }

    }

    public void onCardButtonClick(View v) {
        cardDialog.dismiss();

        if (indeces.size() == 0) finish();
    }

    @Override
    public void onPlaneDialogPositiveClick(DialogFragment dialog) {

    }

}
