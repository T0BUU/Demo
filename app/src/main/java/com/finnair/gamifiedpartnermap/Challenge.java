package com.finnair.gamifiedpartnermap;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by huzla on 15.3.2018.
 */

public class Challenge implements Parcelable {

    private String description;
    private int amount;
    private int reward;
    private int progress;
    private int id;
    private int index; //This is used to index the challenge in the list shown to users
    private boolean completed;
    private ArrayList<String> partnerFields = new ArrayList<>();
    private ArrayList<String> planeModels = new ArrayList<>();
    private ArrayList<String> planeDestinations = new ArrayList<>();
    private ArrayList<String> partnerNames = new ArrayList<>();
    private String prettyDescription;



    Challenge(JSONObject json) {

        try {
            id = json.getInt("id");
            amount = json.getInt("amount");
            reward = json.getInt("reward");
            description = json.getString("description");

            if (!json.get("partner_field").equals(JSONObject.NULL)) { parseJsonArray(json.getJSONArray("partner_field"), this.partnerFields); }
            if (!json.get("plane_model").equals(JSONObject.NULL)) { parseJsonArray(json.getJSONArray("plane_model"), this.planeModels); }
            if (!json.get("plane_destination").equals(JSONObject.NULL)) { parseJsonArray(json.getJSONArray("plane_destination"), this.planeDestinations); }
            if (!json.get("partner_name").equals(JSONObject.NULL)) { parseJsonArray(json.getJSONArray("partner_name"), this.partnerNames); }


        } catch (JSONException e) {
            e.printStackTrace();
        }


        try {
                progress = json.getInt("progress");
                index = json.getInt("index");
            } catch (JSONException e) {
                progress = 0;
                index = -1;
            }


        String result = description;
        String nordicBlue = "#FF0B1560";

        Log.d("Challenges", "" + id + ": " + planeModels.toString());

        result = result.replaceAll("#AMOUNT", String.format("<u><b><font color=%s>%d</font></b></u>", nordicBlue, amount));
        result = result.replaceAll("#PLANE_MODEL", String.format("<u><b><font color=%s>%s</font></b></u>", nordicBlue, planeModels.toString()));
        result = result.replaceAll("#PLANE_DESTINATION", String.format("<u><b><font color=%s>%s</font></b></u>", nordicBlue, planeDestinations.toString()));
        result = result.replaceAll("#PARTNER_FIELD", String.format("<u><b><font color=%s>%s</font></b></u>", nordicBlue, partnerFields.toString()));
        result = result.replaceAll("#PARTNER_NAME", String.format("<u><b><font color=%s>%s</font></b></u>", nordicBlue, partnerNames.toString()));

        result = result.replaceAll("[\\[\\](){}]","");

           prettyDescription = result;
    }

    Challenge() {
        //Used to create "EMPTY" challenges.
        id = -1;
        amount = -1;
        reward = -1;
        description = "EMPTY";
        prettyDescription = "CHALLENGES CAN BE OBTAINED FROM LEVELING UP CARDS OR AS A DAILY GIFT.";
    }

    protected Challenge(Parcel in) {
        description = in.readString();
        amount = in.readInt();
        reward = in.readInt();
        progress = in.readInt();
        id = in.readInt();
        index = in.readInt();
        completed = in.readByte() != 0;
        partnerFields = in.createStringArrayList();
        planeModels = in.createStringArrayList();
        planeDestinations = in.createStringArrayList();
        partnerNames = in.createStringArrayList();
        prettyDescription = in.readString();
    }

    public static final Creator<Challenge> CREATOR = new Creator<Challenge>() {
        @Override
        public Challenge createFromParcel(Parcel in) {
            return new Challenge(in);
        }

        @Override
        public Challenge[] newArray(int size) {
            return new Challenge[size];
        }
    };

    //Getters
    public String getDescription() { return prettyDescription; }

    public int getAmount() {
        return amount;
    }

    public int getProgress() {
        return progress;
    }

    public int getReward() {
        return reward;
    }

    public int getIndex() { return index; }

    public boolean isCompleted() { return completed; }

    //Setters
    public void setIndex(int index) { this.index = index; }

    public boolean incrementProgress() {
        if (this.completed) {
            return false;
        }
        this.progress++;

        if (this.progress == this.amount) {
            this.completed = true;
        }

        return true;
    }

    public boolean isRelated(Plane plane) {
        boolean result = false;

        result = result || planeModels.contains(plane.getPlaneType());
        result = result || planeDestinations.contains(plane.getOriginCountry());

        return result;
    }

    public boolean isRelated(Partner partner) {
        boolean result = false;

        result = result || partnerFields.contains(partner.getFieldOfBusiness());
        result = result || partnerNames.contains(partner.getID());

        return result;
    }

    public JSONObject saveChallenge() {
        JSONObject result = new JSONObject();

        try {
            result.put("progress", this.progress);
            result.put("id", this.id);
            result.put("amount", this.amount);
            result.put("reward", this.reward);
            result.put("description", this.description);
            result.put("partner_field", this.partnerFields);
            result.put("partner_name", this.partnerNames);
            result.put("plane_model", this.planeModels);
            result.put("plane_destination", this.planeDestinations);
            result.put("index", this.index);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }


    private void parseJsonArray(JSONArray input, ArrayList<String> output) {
        if (input != null) {
            int len = input.length();
            for (int i=0;i<len;i++){
                try {
                    output.add(input.get(i).toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(description);
        parcel.writeInt(amount);
        parcel.writeInt(reward);
        parcel.writeInt(progress);
        parcel.writeInt(id);
        parcel.writeInt(index);
        parcel.writeByte((byte) (completed ? 1 : 0));
        parcel.writeStringList(partnerFields);
        parcel.writeStringList(planeModels);
        parcel.writeStringList(planeDestinations);
        parcel.writeStringList(partnerNames);
        parcel.writeString(prettyDescription);
    }
}
