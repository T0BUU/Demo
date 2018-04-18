package com.finnair.gamifiedpartnermap;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by T0BU on 03-Apr-18.
 */

public class Calculations {

    public static double bearing; //bearing to plane
    public static double distance; // this is ground distance to plane
    public static double angle; // this is angle between ground and plane
    private float[] results = new float[3]; // stores results
    public static double deviceAzimuth;
    public static double devicePitch;
    public static double deviceRoll;

    public static boolean azimuthangle;
    public static boolean rollangle;

    public static boolean showtoparrow = true;
    public static boolean showrightarrow = true;
    public static boolean showbottomrow = true;
    public static boolean showleftarrow = true;

    private double a;
    private double b;

    MainActivity activity = new MainActivity();




    public double getBearing(){

        MapsFragment mapsFragment = new MapsFragment();
        mapsFragment.userLocation.distanceBetween(((Double) MapsFragment.calc.get(0)), ((Double) MapsFragment.calc.get(1)), ((Double) MapsFragment.calc.get(2)), ((Double) MapsFragment.calc.get(3)), results);
        bearing = results[1];
        distance = results[0];
        getAngle();

        if (bearing < 0){
            bearing = bearing +360;
        }

        return bearing;

    }

    public double getAngle(){

        double tan = ((Double) MapsFragment.calc.get(4)) / distance;
        angle = Math.toDegrees(Math.atan(tan));


        return angle;
    }

    public boolean azimuthAngle(){

        azimuthangle = false;

        //deviceAzimuth = Math.toDegrees(SensorActivity.azimuth);


        if (bearing == 0){  // no plane selected, so no need to calculate angles
            return azimuthangle;
        }

        if (CameraActivity.rotaatio == 0){

            //deviceAzimuth = deviceAzimuth + 180;

            if (bearing >= 315){

                double n = bearing + 45;
                double m = bearing - 45;
                a = n - 360;

                if (deviceRoll > 100 && ((deviceAzimuth +180 >= m) || (deviceAzimuth +180 <= a && deviceAzimuth +180 >= 0))){
                    azimuthangle = true;
                }

                if (bearing > deviceAzimuth+180 && deviceAzimuth+180 < 180){
                    showrightarrow = false;
                    showleftarrow = true;
                }
                else if (bearing > deviceAzimuth+180 && deviceAzimuth+180 > 180){
                    showrightarrow = true;
                    showleftarrow = false;
                }

                else if (bearing < deviceAzimuth+180){
                    showrightarrow = false;
                    showleftarrow = true;
                }

                else {
                    azimuthangle = false;
                }

            }

            if (bearing <= 45){

                double y = bearing + 45; // upper bound for azimuth
                double x = bearing - 45; // lower bound for azimuth

                if (x < 0){
                    b = x + 360;
                }

                if (deviceRoll > 100 && (deviceAzimuth +180 <= y || (deviceAzimuth +180  >= b && (deviceAzimuth +180) <= 360))){
                    azimuthangle = true;
                }

                if (bearing > deviceAzimuth+180){
                    showrightarrow = true;
                    showleftarrow = false;
                }

                else if (bearing < deviceAzimuth+180){
                    showrightarrow = false;
                    showleftarrow = true;
                }

                else {
                    azimuthangle = false;
                }

            }

            if (bearing > 45 && bearing < 315) {
                if (bearing - (deviceAzimuth + 180) >= -45 && bearing - (deviceAzimuth +180) <= 45 && deviceRoll > 100){
                    azimuthangle = true;
                }

                if (bearing > deviceAzimuth+180){
                    showrightarrow = true;
                    showleftarrow = false;
                }

                else if (bearing < deviceAzimuth+180){
                    showrightarrow = false;
                    showleftarrow = true;
                }

                else{
                    azimuthangle = false;
                }
            }

        }

        if (CameraActivity.rotaatio == 1){


            if (bearing >= 45 && bearing <= 135){

                double n = bearing - 45;
                double m = bearing + 225;

                if (deviceRoll > 80 && ((deviceAzimuth+360 >= m) || (deviceAzimuth+360 <= n && deviceAzimuth+360 >= 0))){
                    azimuthangle = true;
                }

                if (bearing > deviceAzimuth+360){
                    showrightarrow = true;
                    showleftarrow = false;
                }

                else if (bearing < deviceAzimuth+360){
                    showrightarrow = false;
                    showleftarrow = true;
                }


                else {
                    azimuthangle = false;
                }

            }

            if (bearing < 45 && bearing > 0){

                double y = bearing + 315; // upper bound for azimuth
                double x = bearing + 225; // lower bound for azimuth


                if (deviceRoll > 80 && (deviceAzimuth+360 <= y && deviceAzimuth+360 >= x)){
                    azimuthangle = true;
                }

                if (bearing > deviceAzimuth+360){
                    showrightarrow = true;
                    showleftarrow = false;
                }

                else if (bearing < deviceAzimuth+360){
                    showrightarrow = false;
                    showleftarrow = true;
                }

                else {
                    azimuthangle = false;
                }

            }

            if (bearing > 135) {

                double y = bearing - 135; // upper bound for azimuth
                double x = bearing - 45; // lower bound for azimuth

                if (deviceAzimuth+360 >= y && deviceAzimuth+360 <= x && deviceRoll > 80){
                    azimuthangle = true;
                }

                if (bearing > deviceAzimuth+360){
                    showrightarrow = true;
                    showleftarrow = false;
                }

                else if (bearing < deviceAzimuth+360){
                    showrightarrow = false;
                    showleftarrow = true;
                }

                else{
                    azimuthangle = false;
                }
            }

        }


        if (CameraActivity.rotaatio == 3){



            if (bearing >= 225 && bearing <= 315){

                double n = bearing - 225;
                double m = bearing + 45;

                if (deviceAzimuth < 0 && deviceRoll > 80 && ((deviceAzimuth+360 >= m) || (deviceAzimuth+360 <= n && deviceAzimuth+360 >= 0))){
                    azimuthangle = true;
                }

                if (bearing > deviceAzimuth+360 && deviceAzimuth < 0){
                    showrightarrow = true;
                    showleftarrow = false;
                }

                else if (bearing < deviceAzimuth+360 && deviceAzimuth < 0){
                    showrightarrow = false;
                    showleftarrow = true;
                }

                else if (deviceRoll > 80 && ((deviceAzimuth >= m) || (deviceAzimuth <= n && deviceAzimuth >= 0))){
                    azimuthangle = true;
                }

                else if (bearing > deviceAzimuth && deviceAzimuth > 0){
                    showrightarrow = true;
                    showleftarrow = false;
                }

                else if (bearing < deviceAzimuth && deviceAzimuth > 0){
                    showrightarrow = false;
                    showleftarrow = true;
                }

                else {
                    azimuthangle = false;
                }

            }

            if (bearing > 315 && bearing <= 360){

                double y = bearing - 315; // upper bound for azimuth
                double x = bearing - 225; // lower bound for azimuth

                if (deviceAzimuth < 0 && deviceRoll > 80 && (deviceAzimuth+360 >= y && deviceAzimuth+360 <= x)){
                    azimuthangle = true;
                }

                if (bearing > deviceAzimuth+360 && deviceAzimuth < 0){
                    showrightarrow = true;
                    showleftarrow = false;
                }

                else if (bearing < deviceAzimuth+360 && deviceAzimuth < 0){
                    showrightarrow = false;
                    showleftarrow = true;
                }


                else if (deviceRoll > 80 && (deviceAzimuth >= y && deviceAzimuth <= x)){
                    azimuthangle = true;
                }

                else if (bearing > deviceAzimuth && deviceAzimuth > 0){
                    showrightarrow = true;
                    showleftarrow = false;
                }

                else if (bearing < deviceAzimuth && deviceAzimuth > 0){
                    showrightarrow = false;
                    showleftarrow = true;
                }

                else {
                    azimuthangle = false;
                }

            }

            if (bearing < 225) {

                double y = bearing + 135; // upper bound for azimuth
                double x = bearing + 45; // lower bound for azimuth

                if (deviceAzimuth+360 <= y && deviceAzimuth+360 >= x && deviceRoll > 80 && deviceAzimuth < 0){
                    azimuthangle = true;
                }

                if (bearing > deviceAzimuth+360 && deviceAzimuth < 0){
                    showrightarrow = true;
                    showleftarrow = false;
                }

                else if (bearing < deviceAzimuth+360 && deviceAzimuth < 0){
                    showrightarrow = false;
                    showleftarrow = true;
                }

                else if (deviceAzimuth <= y && deviceAzimuth >= x && deviceRoll > 80){
                    azimuthangle = true;
                }

                else if (bearing > deviceAzimuth && deviceAzimuth > 0){
                    showrightarrow = true;
                    showleftarrow = false;
                }

                else if (bearing < deviceAzimuth && deviceAzimuth > 0){
                    showrightarrow = false;
                    showleftarrow = true;
                }
                else{
                    azimuthangle = false;
                }
            }

        }

        return azimuthangle;
    }

    public boolean rollAngle(){

        rollangle = false;

        if (bearing == 0){  // no plane selected, so no need to calculate angles
            return rollangle;
        }

        if (CameraActivity.rotaatio == 0){
            if(devicePitch < 0){
                devicePitch = (devicePitch + 90);
            }
            if(((angle - devicePitch) >= -10) && ((angle - devicePitch) <= 10) && deviceRoll > 50){
                rollangle = true;
            }

            if(angle < devicePitch && deviceRoll >50){
                showtoparrow = false;
                showbottomrow = true;
            }
            else if(angle > devicePitch && deviceRoll >50){
                showtoparrow = true;
                showbottomrow = false;
            }
            else if(deviceRoll < 50){
                showtoparrow = true;
                showbottomrow = false;
            }

            else {
                rollangle = false;
            }

        }


        if (CameraActivity.rotaatio == 1){
            if (deviceRoll < 0){
                deviceRoll = (deviceRoll * -1);
            }
            if((((angle+90) - deviceRoll) >= -10) && (((angle+90) - deviceRoll) <= 10)){
                rollangle = true;
            }

            if(angle+90 < deviceRoll){
                showtoparrow = false;
                showbottomrow = true;
            }
            else if(angle+90 > deviceRoll){
                showtoparrow = true;
                showbottomrow = false;
            }
            else rollangle = false;
        }


        if (CameraActivity.rotaatio == 3){
            if((((angle+90) - deviceRoll) >= -10) && (((angle+90) - deviceRoll) <= 10)){
                rollangle = true;
            }
            if(angle+90 < deviceRoll){
                showtoparrow = false;
                showbottomrow = true;
            }
            else if(angle+90 > deviceRoll){
                showtoparrow = true;
                showbottomrow = false;
            }
            else {
                rollangle = false;
            }
        }

        return rollangle;
    }

    public boolean isPlaneCatchable(){
        if (azimuthangle == true && rollangle == true){
            return true;
        }
        else{
            return false;
        }
    }


}

