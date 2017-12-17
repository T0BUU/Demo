package com.finnair.gamifiedpartnermap;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.util.Pair;
import android.view.Display;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by noctuaPC on 5.12.2017.
 */

public class MarkerClass {

    Integer screenWidth;
    Integer screenHeight;
    Activity activity;
    java.util.ArrayList<Pair<Marker, Marker>> markerArrayList = new java.util.ArrayList<>();
    GoogleMap mMap;

    private Double lat;
    private Double lng;
    private String companyName;
    private String companyBusiness;


    public MarkerClass(Activity activity, GoogleMap mMap) {

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

    public String[] addOneMarkerOnMap(Double latitude, Double longitude, String companyName, String business){

        this.lat = latitude;
        this.lng = longitude;
        this.companyName = companyName;
        this.companyBusiness = business;


        MarkerOptions balloonOptions = this.balloonMarkerOptions();
        MarkerOptions imageOptions = this.imageMarkerOptions(balloonOptions);

        Marker farMarker = this.mMap.addMarker(balloonOptions);
        Marker closeMarker =  this.mMap.addMarker(imageOptions);

        this.markerArrayList.add( new Pair<>(closeMarker, farMarker) );

        String[] tags = new String[2];
        tags[0] = closeMarker.getId();
        tags[1] = farMarker.getId();
        return tags;
    }

    public void showCloseMarkers(){
        for (Pair<Marker, Marker> markerPair : this.markerArrayList) {
            markerPair.first.setVisible(true);
            markerPair.second.setVisible(false);
        }
    }

    public void showFarMarkers(){
        for (Pair<Marker, Marker> markerPair : this.markerArrayList) {
            markerPair.first.setVisible(false);
            markerPair.second.setVisible(true);
        }
    }

    public MarkerOptions balloonMarkerOptions(){

        LatLng position = new LatLng(this.lat, this.lng);
        MarkerOptions mOptions = new MarkerOptions();
        mOptions.position(position)
                .title(this.companyName)
                .snippet(this.companyBusiness);
        return mOptions;
    }


    public MarkerOptions imageMarkerOptions(MarkerOptions balloonOptions){

        Bitmap bitmap = BitmapFactory.decodeResource(this.activity.getResources(), R.raw.aalto_logo);
        Bitmap smallBitmap = scaleDown(bitmap, this.screenWidth / 8);
        BitmapDescriptor bitmapIcon = BitmapDescriptorFactory.fromBitmap( smallBitmap );

        MarkerOptions imageOptions = new MarkerOptions();
        imageOptions.position(balloonOptions.getPosition())
                    .title(balloonOptions.getTitle())
                    .snippet(balloonOptions.getSnippet())
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
}












