package com.finnair.gamifiedpartnermap;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
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

    // Available but not in use: //////////
    // private Double verticalRate;
    // private boolean onGround;
    // private Double lastContact;
    // private Double lastPositionUpdate;
    // private String squawk;
    // private boolean spi;
    // private Double baroAltitude;
    ////////////////////////////////////////

    public Plane(Activity activity){
        super(activity);
        setCircleRadius(10000);
    }


    // SET:
    public void setPlaneMiscellaneousInformation(Double geoAltitude, Double velocity, String icao24, String originCountry){
        if (geoAltitude != null) this.geoAltitude = geoAltitude;
        if (velocity != null) this.velocityKmph = velocity * 1.852; // From knots to km/h
        if (icao24 != null) this.icao24 = icao24;
        if (originCountry != null) this.originCountry = originCountry;
    }

    public void setMarkerImage(Integer screenWidth){
        Bitmap bitmap = writeOnDrawable(R.raw.airplane_top_marker, this.getID()).getBitmap();
        Bitmap smallBitmap = scaleDown(bitmap, screenWidth / 8);
        BitmapDescriptor bitmapIcon = BitmapDescriptorFactory.fromBitmap( smallBitmap );
        setMarkerImage(bitmapIcon);
    }

    // GET:
    public Double getVelocityKmph(){ return this.velocityKmph; }
    public String getOriginCountry(){ return this.originCountry; }
    public String getIcao24(){ return this.icao24; }
    public Double getGeoAltitude(){ return this.geoAltitude; }


    public void animatePlaneMarker(final LatLng destination, final float newRotationDegrees, final Location userLocation) {
        // WARNING: Animations might cause problems if they last longer than the refresh rate!

        final LatLng currentPosition = this.getLatLng();

        ValueAnimator ltAnimation = ValueAnimator.ofFloat((float) currentPosition.latitude, (float) destination.latitude);
        ValueAnimator lgAnimation = ValueAnimator.ofFloat((float) currentPosition.longitude, (float) destination.longitude);

        this.getMarker().setRotation(newRotationDegrees);

        ltAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedValue = (float) valueAnimator.getAnimatedValue();
                LatLng setValue = new LatLng(animatedValue, getMarker().getPosition().longitude);

                getMarker().setPosition(setValue);
                getCircle().setCenter(setValue);

            }
        });

        lgAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedValue = (float) valueAnimator.getAnimatedValue();
                LatLng setValue = new LatLng(getMarker().getPosition().latitude, animatedValue);

                getMarker().setPosition(setValue);
                getCircle().setCenter(setValue);
            }
        });

        AnimatorSet LtLg = new AnimatorSet();

        // Rotation cannot happen together with directional animation (plane flies sideways):
        LtLg.playTogether(ltAnimation, lgAnimation);
        LtLg.setDuration(10000);
        LtLg.start();
        LtLg.addListener(new AnimatorListenerAdapter() {
            // Animation ending must be listened separately, because it runs in its own thread
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                setPosition(destination.latitude, destination.longitude);

                if ( isWithinReach(userLocation) ) {

                    if (getRadarPulseAnimation() == null){
                        // Plane is within reach and doesn't yet have a radar animation
                        getCircle().setFillColor(Color.argb(100, 0, 255, 0));

                        animateRadarPulseForClosePlane();
                        animateRadarArcForClosePlane();
                        showRadarArcPolyline(true);

                        startRadarPulseAnimation();
                        startRadarArcAnimation();
                    }

                } else { // Plane is outside of reach
                    // If plane has existing animations, destroy them:
                    if (getRadarPulseAnimation() != null) {
                        deleteRadarArcAnimation();
                        deleteRadarPulseAnimation();
                        setCircleVisible(false);

                    }
                    // Set area circle to have original information:
                    getCircle().setFillColor(Color.argb(100, 0, 0, 100));
                    getCircle().setRadius(circleRadius);
                    getCircle().setStrokeColor(Color.WHITE);
                }
            }
        });
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

        ((MainActivity) this.activity).setPlanesListing(ret);
        return ret;
    }
}
