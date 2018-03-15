package com.finnair.gamifiedpartnermap;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by huzla on 15.3.2018.
 */

public class Challenge {

    private String description;
    private int amount;
    private int reward;
    private int progress;
    private ArrayList<String> partnerTags;
    private ArrayList<String> planeTags;


    Challenge(JSONObject json) {
        if (json.has("saved")) {
            //This Chanllenge is being loaded in from disk.
        }

        else {

        }
    }

    //Getters
    public String getDescription() {
        return description;
    }

    public int getAmount() {
        return amount;
    }

    public int getReward() {
        return reward;
    }

    //Setters
    public void setProgress(int progress) {
        this.progress = progress;
    }

    public boolean isRelated(Plane plane) {
        return true;
    }

    public boolean isRelated(Partner partner) {
        return true;
    }

    public JSONObject saveChallenge() {
        return null;
    }
}
