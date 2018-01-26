package com.finnair.gamifiedpartnermap;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.util.Log;
import android.util.Pair;
import android.view.Display;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.lang.Math.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.Math.acos;
import static java.lang.Math.atan;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;

/**
 * Created by huzla on 25.1.2018.
 */

public class PlaneMarkerClass {
    Integer screenWidth;
    Integer screenHeight;
    Activity activity;
    java.util.ArrayList<Pair<Marker, Circle>> markerArrayList = new java.util.ArrayList<>();
    GoogleMap mMap;

    private Double lat;
    private Double lng;
    private String planeName;
    private final Float rotationMultiplier = -100.0f;




    public PlaneMarkerClass(Activity activity, GoogleMap mMap) {
        // Activity is for example MapsActivity
        this.activity = activity;
        this.mMap = mMap;
        // Get window size for scaling Marker image size:
        Display display = this.activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        this.screenWidth = size.x;
        this.screenHeight = size.y;



    }

    public void addOneMarkerOnMap(Double latitude, Double longitude, String planeName, Double radius){

        this.lat = latitude;
        this.lng = longitude;
        this.planeName = planeName;


        CircleOptions areaOptions = this.areaMarkerOptions(new LatLng(latitude, longitude), radius);
        MarkerOptions imageOptions = this.imageMarkerOptions(areaOptions, planeName);

        Circle areaMarker = this.mMap.addCircle(areaOptions);
        Marker planeMarker = this.mMap.addMarker(imageOptions);



        this.markerArrayList.add( new Pair(planeMarker, areaMarker) );

    }

    public void animateMarkers(List<LatLng> coords) {
        for (int i = 0; i < markerArrayList.size(); i++) {
            animatePlaneMarker(coords.get(i), markerArrayList.get(i));
        }

    }

    public void setRadius(double r, int index) {
        markerArrayList.get(index).second.setRadius(r);
    }

    public void zoomListener(float zoom) {


        for ( Pair<Marker, Circle> temp : markerArrayList) {
            //Log.d("Circle Radius","" + temp.second.getRadius());
            //zoom = 0 the entire world and zoom 20 is the closest the camera gets.
            //zooming one level (0 -> 1 for example) halves the size of one map tile => zooming grows O(pow(2, zoom))
            if (temp.second.getRadius() < pow(2, 20-zoom)) temp.second.setVisible(false);
            else temp.second.setVisible(true);
        }

    }


    private CircleOptions areaMarkerOptions(LatLng coords, Double radius){

            return new CircleOptions()
                .center(coords)
                .radius(radius)
                .strokeWidth(10)
                .strokeColor(Color.WHITE)
                .fillColor(Color.argb(100, 0, 0, 100));

    }


    private MarkerOptions imageMarkerOptions(CircleOptions areaOptions, String title){

        Bitmap bitmap = BitmapFactory.decodeResource(this.activity.getResources(), R.raw.airplane_top_marker);
        Bitmap smallBitmap = scaleDown(bitmap, this.screenWidth / 8);
        BitmapDescriptor bitmapIcon = BitmapDescriptorFactory.fromBitmap( smallBitmap );

        MarkerOptions imageOptions = new MarkerOptions();
        imageOptions.position(areaOptions.getCenter())
                .anchor(0.5f, 0.5f)
                .title(title)
                .icon(bitmapIcon);
        return imageOptions;
    }


    private static Bitmap scaleDown(Bitmap image, float maxImageSize) {

        float ratio = Math.min(
                (float) maxImageSize / image.getWidth(),
                (float) maxImageSize / image.getHeight() );

        int width = Math.round((float) ratio * image.getWidth());
        int height = Math.round((float) ratio * image.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(image, width, height, true);
        return newBitmap;
    }



    private void animatePlaneMarker(LatLng destination, final Pair<Marker, Circle> planeMarkers) {
        final Marker plane = planeMarkers.first;
        final Circle area = planeMarkers.second;

        final LatLng currentPosition = plane.getPosition();
        final float startRotation = plane.getRotation();

        double rotationLat = (destination.latitude-currentPosition.latitude);
        double rotationLong = (destination.longitude-currentPosition.longitude);
        float rotationDirection;
        float rotationAdd;

        if (rotationLong < 0) {
            rotationDirection = 1.0f;
            rotationAdd = 180.0f;
            if (rotationLat < 0) {
                rotationDirection = -1.0f;
                rotationAdd = -180.0f;
            }
        }
        else {
            rotationDirection = 1.0f;
            rotationAdd = 0.0f;
        }

        final float endRotation = (float) -(toDegrees(atan(rotationLat/rotationLong))*rotationDirection + rotationAdd);

        Log.d("Starting Rotation", "" + startRotation);
        Log.d("End Rotation", "" + endRotation);

        ValueAnimator ltAnimation = ValueAnimator.ofFloat((float) currentPosition.latitude, (float) destination.latitude);

        ValueAnimator lgAnimation = ValueAnimator.ofFloat((float) currentPosition.longitude, (float) destination.longitude);

       ValueAnimator rotation = ValueAnimator.ofFloat(startRotation, endRotation);

        ltAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedValue = (float) valueAnimator.getAnimatedValue();
                LatLng setValue = new LatLng(animatedValue, plane.getPosition().longitude);

                plane.setPosition(setValue);
                area.setCenter(setValue);

            }
        });

        lgAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedValue = (float) valueAnimator.getAnimatedValue();
                LatLng setValue = new LatLng(plane.getPosition().latitude, animatedValue);


                plane.setPosition(setValue);
                area.setCenter(setValue);
            }
        });

        rotation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedValue = (float) valueAnimator.getAnimatedValue();

                plane.setRotation(animatedValue);

            }
        });
        Log.d("Animation", "START!");
        AnimatorSet LtLg = new AnimatorSet();
        LtLg.playTogether(ltAnimation, lgAnimation, rotation);
        LtLg.setDuration(10000);
        LtLg.start();
    }
}

