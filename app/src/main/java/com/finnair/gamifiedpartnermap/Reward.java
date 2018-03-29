package com.finnair.gamifiedpartnermap;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by huzla on 29.3.2018.
 */

public class Reward {
    private String description;
    private String prettyDescription;
    private int pointsAmount;
    private int id;
    private ArrayList<String> partnerFields = new ArrayList<>();
    private ArrayList<String> planeModels = new ArrayList<>();



    Reward(JSONObject json) {

        try {
            id = json.getInt("id");
            pointsAmount = json.getInt("plus_points");
            description = json.getString("description");

            if (!json.get("partner_field").equals(JSONObject.NULL) ) { parseJsonArray(json.getJSONArray("partner_field"), this.partnerFields); }
            if (!json.get("plane_model").equals(JSONObject.NULL)) { parseJsonArray(json.getJSONArray("plane_model"), this.planeModels); }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        String nordicBlue = "#FF0B1560";
        String result = String.format("<u><b><font color=%s>%s</font></b></u>", nordicBlue, description);

        result = result.replaceAll("#PLUS_POINTS", String.format("%d", pointsAmount));

        prettyDescription = result;
    }

    public boolean isRelated(Plane plane) {
        boolean result = false;

        result = result || planeModels.contains(plane.getPlaneType());

        return result;
    }

    public boolean isRelated(Partner partner) {
        boolean result = false;

        result = result || partnerFields.contains(partner.getFieldOfBusiness());

        return result;
    }

    //Getters
    public String getDescription() { return prettyDescription; }

    public int getAmount() {
        return pointsAmount;
    }

    public int getId() { return id; }

    public ArrayList<String> getPlaneModels() {
        return planeModels;
    }

    public ArrayList<String> getPartnerFields() {
        return partnerFields;
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
}
