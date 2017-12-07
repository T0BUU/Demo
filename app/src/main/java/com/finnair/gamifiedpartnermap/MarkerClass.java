package com.finnair.gamifiedpartnermap;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.view.Display;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by noctuaPC on 5.12.2017.
 */

public class MarkerClass {

    Integer screenWidth;
    Integer screenHeight;
    Activity activity;

    public MarkerClass(Activity activity) {

        // Activity is for example MapsActivity
        this.activity = activity;

        // Get window size for scaling Marker image size:
        Display display = this.activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        this.screenWidth = size.x;
        this.screenHeight = size.y;

    }

    public MarkerOptions balloonMarkerOptions(LatLng position, String title, String snippet){

        MarkerOptions mOptions = new MarkerOptions();
        mOptions.position(position)
                .title(title)
                .snippet(snippet);
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












