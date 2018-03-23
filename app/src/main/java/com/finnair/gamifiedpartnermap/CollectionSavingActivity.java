package com.finnair.gamifiedpartnermap;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.json.JSONArray;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import static com.finnair.gamifiedpartnermap.PartnerMarkerClass.USER_DATA_LOCATION_PARTNERS;
import static com.finnair.gamifiedpartnermap.PlaneMarkerClass.USER_DATA_LOCATION_PLANES;

/**
 * Created by huzla on 23.3.2018.
 */

//This class is used as an abstract super class for activities that need functionality for saving planes and partners.

public abstract class CollectionSavingActivity extends AppCompatActivity {

    protected static final HashMap<String, Integer> modelsToImages;
    static
    {
        List<String> PLANE_TYPES = Arrays.asList("AIRBUS A350-900", "AIRBUS A330-300",
                "AIRBUS A321", "AIRBUS A321-231",
                "AIRBUS A320", "AIRBUS A319",
                "EMBRAER 190", "ATR 72-212A");

        List<Integer> PLANE_IMAGES = Arrays.asList(R.drawable.a350_900, R.drawable.a330_300,
                R.drawable.a321, R.drawable.a321_sharklet, R.drawable.a320,
                R.drawable.a319, R.drawable.embraer_190, R.drawable.x350x113_norra);

        modelsToImages = new HashMap<String, Integer>();

        for (int i = 0; i < PLANE_TYPES.size(); ++i) {
            modelsToImages.put(PLANE_TYPES.get(i), PLANE_IMAGES.get(i));
        }

    }

    protected HashMap<String, HashSet<String>> planeCollectionHashMap;
    protected HashMap<String, HashSet<String>> partnerCollectionHashMap;
    protected ArrayList<Challenge> activeChallenges;

    protected int matchCategoryToImage(String category) {
        switch (category) {
            case "Restaurant": return R.drawable.ic_restaurants;
            case "Car rental": return R.drawable.ic_car_rental;
            case "Charity": return R.drawable.ic_charity;
            case "Entertainment": return R.drawable.ic_entertainment;
            case "Finance and insurance": return R.drawable.ic_finance;
            case "Helsinki Airport": return R.drawable.ic_helsinki_vantaa;
            case "Golf and leisure time": return R.drawable.ic_hobbies;
            case "Hotel": return R.drawable.ic_hotels_spas;
            case "Shopping": return R.drawable.ic_shopping;
            case "Tour operators and cruise lines": return R.drawable.ic_travel;
            case "Services and Wellness": return R.drawable.ic_services_healthcare;
            default: return  R.raw.aalto_logo;
        }
    }

    protected String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }


   protected void savePlane(String planeType, String country) {

        try {
            planeCollectionHashMap.get(planeType).add(country);
        }
        catch (java.lang.NullPointerException nil) {
            HashSet<String> addMe = new HashSet<>();
            addMe.add(country);

            planeCollectionHashMap.put(planeType, addMe);
        }

        Log.d("Plane saving: ", planeCollectionHashMap.toString());

    }

    protected void savePartner(String field, String name, String time){
        // All apps (root or not) have a default data directory, which is /data/data/<package_name>

        try {
            partnerCollectionHashMap.get(field).add(String.format("%s %s", time, name));
        }
        catch (java.lang.NullPointerException nil) {
            HashSet<String> addMe = new HashSet<>();
            addMe.add(String.format("%s %s", time, name));

            partnerCollectionHashMap.put(field, addMe);
        }

        Log.d("Partner saving: ", partnerCollectionHashMap.toString());
    }

    private String formatPlanes() {
        String result = "";

        for (String planeType : planeCollectionHashMap.keySet()) {
            Iterator<String> row = planeCollectionHashMap.get(planeType).iterator();

            result += planeType;

            while (row.hasNext()) {
                result += String.format("#%s", row.next());
            }

            result += "\n";

        }
        return result;
    }

    private String formatPartners() {
        String result = "";

        for (String  category : partnerCollectionHashMap.keySet()) {
            Iterator<String> row = partnerCollectionHashMap.get(category).iterator();

            result += category;

            while (row.hasNext()) {
                result += String.format("#%s", row.next());
            }

            result += "\n";

        }
        return result;
    }

    private String formatChallenges() {
        JSONArray result = new JSONArray();

        for ( Challenge c : activeChallenges ) {
            result.put(c.saveChallenge());
        }

        return result.toString();
    }

    protected void savePlanes(Context context){

        String result = formatPlanes();

        try {
            FileOutputStream outputStream = context.openFileOutput(USER_DATA_LOCATION_PLANES, Context.MODE_PRIVATE);
            outputStream.write(result.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("Plane Saving", result);
    }

    protected void savePartners(Context context){

        String result = formatPartners();

        try {
            FileOutputStream outputStream = context.openFileOutput(USER_DATA_LOCATION_PARTNERS, Context.MODE_PRIVATE);
            outputStream.write(result.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("Partner Saving", result);
    }

    protected void saveChallenges(Context context) {
        String result = formatChallenges();

        try {
            FileOutputStream outputStream = context.openFileOutput("activeChallenges", Context.MODE_PRIVATE);
            outputStream.write(result.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("Challenge Saving", result);
    }

}
