package com.finnair.gamifiedpartnermap;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class Plane extends ClusterMarker {

    private Double geoAltitude;
    private Double velocityKmph;
    private String originCountry;
    private String icao24;
    private String planeType;

    public Plane(Activity activity){
        super(activity);
        setCircleRadius(100000);
    }


    // SET:
    public void setPlaneMiscellaneousInformation(Double geoAltitude, Double velocity, String icao24, String originCountry){
        if (geoAltitude != null) this.geoAltitude = geoAltitude;
        if (velocity != null) this.velocityKmph = velocity * 1.852; // From knots to km/h
        if (icao24 != null) this.icao24 = icao24;
        if (originCountry != null) this.originCountry = originCountry;
    }



    public void setPlaneType(String type) { this.planeType = type; }

    // GET:
    public Double getVelocityKmph(){ return this.velocityKmph; }
    public String getOriginCountry(){ return this.originCountry; }
    public String getIcao24(){ return this.icao24; }
    public Double getGeoAltitude(){ return this.geoAltitude; }
    public String getPlaneType() { return this.planeType; }


    public void setMarkerImage(Integer screenWidth){
        setMarkerImage(bitmapDescriptorFromVector(this.activity, R.drawable.ic_airplane, 2));
    }
    public void setMarkerImage(String status){
        if (status.equalsIgnoreCase("near"))
            setMarkerImage(bitmapDescriptorFromVector(this.activity, R.drawable.ic_airplane_near, 1));
        else if (status.equalsIgnoreCase("collected"))
            setMarkerImage(bitmapDescriptorFromVector(this.activity, R.drawable.ic_airplane, 1));
        else
            setMarkerImage(bitmapDescriptorFromVector(this.activity, R.drawable.ic_airplane, 1));
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId, int sizeMultiplier) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, sizeMultiplier*vectorDrawable.getIntrinsicWidth(), sizeMultiplier*vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(sizeMultiplier*vectorDrawable.getIntrinsicWidth(), sizeMultiplier*vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    public void savePlane(Context context){
        // All apps (root or not) have a default data directory, which is /data/data/<package_name>
        String filename = "myPlanes";
        String earlierText = readCollectedPlanes(context);
        String text = getID();
        String string = earlierText + " " + text;

        try {
            FileOutputStream outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(string.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String readCollectedPlanes(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("myPlanes");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }
}
