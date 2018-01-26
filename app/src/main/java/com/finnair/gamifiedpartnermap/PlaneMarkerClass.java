package com.finnair.gamifiedpartnermap;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.util.Pair;
import android.view.Display;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

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

    public void animateMarker(int index, LatLng destination) {
        animatePlaneMarkers(destination, markerArrayList.get(index));
    }

    public void setRadius(double r, int index) {
        markerArrayList.get(index).second.setRadius(r);
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



    private static void animatePlaneMarkers(LatLng destination, final Pair<Marker, Circle> planeMarkers) {
        final Marker plane = planeMarkers.first;
        final Circle area = planeMarkers.second;

        final LatLng currentPosition = plane.getPosition();
        float startRotation = plane.getRotation();

        ValueAnimator ltAnimation = ValueAnimator.ofFloat((float) currentPosition.latitude, (float) destination.latitude);

        ValueAnimator lgAnimation = ValueAnimator.ofFloat((float) currentPosition.longitude, (float) destination.longitude);

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

        AnimatorSet LtLg = new AnimatorSet();
        LtLg.playTogether(ltAnimation, lgAnimation);
        LtLg.setDuration(10000);
        LtLg.start();
    }
}

