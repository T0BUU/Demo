package com.finnair.gamifiedpartnermap;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.util.Log;
import android.util.Pair;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by noctuaPC on 3.2.2018.
 */

public class Plane {

    private Activity activity;
    private Location planeLocation = new Location("");
    private LatLng planeLatLng;
    private String direction;
    private BitmapDescriptor markerImage;

    private Marker planeMarker;
    private Circle planeCircle;
    private MarkerOptions planeMarkerOptions;
    private CircleOptions planeCircleOptions;

    private ValueAnimator radarPulseAnimator;
    private ValueAnimator radarArcAnimator;
    private Polyline radarArcPolyLine;

    private final float circleRadius = 10000;

    private String planeID;
    private Double headingDegree;
    private Double geoAltitude;
    private Double velocityKmph;
    private String originCountry;
    private String icao24;

    // Available but not in use: //////////
    private Double verticalRate;
    private boolean onGround;
    private Double lastContact;
    private Double lastPositionUpdate;
    private String squawk;
    private boolean spi;
    private Double baroAltitude;
    ////////////////////////////////////////


    public Plane(Activity activity){
        this.activity = activity;
    }


    // SET:
    public void setPlanePosition(Double latitude, Double longitude) {
        this.planeLatLng = new LatLng(latitude, longitude);
        this.planeLocation.setLatitude(latitude);
        this.planeLocation.setLongitude(longitude);
    }
    public void setPlaneMarker(Marker planeMarker){ this.planeMarker = planeMarker; }
    public void setPlaneCircle(Circle planeCircle){ this.planeCircle = planeCircle; }
    public void setCircleVisible(Boolean yesNo){ this.planeCircle.setVisible(yesNo); }
    public void setRadarArcPolyLine(Polyline radarArcPolyLine){ this.radarArcPolyLine = radarArcPolyLine;}
    public void setPlaneID(String planeID){ this.planeID = planeID; }
    public void setHeading(Double headingDegree){ this.headingDegree = headingDegree; }
    public void setPlaneCircleOptions(){
        this.planeCircleOptions = new CircleOptions()
                .center(this.planeLatLng)
                .radius(this.circleRadius)
                .strokeWidth(10)
                .strokeColor(Color.WHITE)
                .fillColor(Color.argb(100, 0, 0, 100));
    }
    public void setPlaneMarkerOptions(Integer screenWidth){

        Bitmap bitmap = BitmapFactory.decodeResource(this.activity.getResources(), R.raw.airplane_top_marker);
        Bitmap smallBitmap = scaleDown(bitmap, screenWidth / 8);
        BitmapDescriptor bitmapIcon = BitmapDescriptorFactory.fromBitmap( smallBitmap );

        this.planeMarkerOptions = new MarkerOptions();
        this.planeMarkerOptions.position(this.planeLatLng)
                .rotation( Double.valueOf(this.headingDegree).floatValue() )
                .anchor(0.5f, 0.5f)
                .title(this.planeID)
                .icon(bitmapIcon)
                .flat(true);
    }
    public void setPlaneMiscellaneousInformation(Double geoAltitude, Double velocity, String icao24, String originCountry){
        if (geoAltitude != null) this.geoAltitude = geoAltitude;
        if (velocity != null) this.velocityKmph = velocity * 1.852; // From knots to km/h
        if (icao24 != null) this.icao24 = icao24;
        if (originCountry != null) this.originCountry = originCountry;
    }

    // GET:
    public Location getPlaneLocation(){ return this.planeLocation; }
    public LatLng getPlaneLatLng(){ return this.planeLatLng; }
    public Double getGeoAltitude(){ return this.geoAltitude; }

    public Double getHeadingDegree(){ return this.headingDegree; }
    public Double getVelocityKmph(){ return this.velocityKmph; }
    public String getOriginCountry(){ return this.originCountry; }
    public String getIcao24(){ return this.icao24; }

    public MarkerOptions getPlaneMarkerOptions(){ return this.planeMarkerOptions; }
    public CircleOptions getPlaneCircleOptions(){ return this.planeCircleOptions; }
    public float getCircleRadius(){ return this.circleRadius; }
    public Marker getPlaneMarker(){ return this.planeMarker; }
    public Circle getPlaneCircle(){ return this.planeCircle; }
    public Polyline getRadarArcPolyLine(){ return this.radarArcPolyLine; }
    public String getPlaneID(){ return this.planeID; }
    public PolylineOptions getRadarPolyLineOptions(){
        LatLng center = planeMarker.getPosition();

        ArrayList<LatLng> arcPoints = new ArrayList<>();
        for (int i=0; i < 45; i+=2)
            arcPoints.add(getPointGivenRadiusAndDegree(center, this.circleRadius, 0 + i));

        return new PolylineOptions()
                .addAll(arcPoints)
                .width(8)
                .color(Color.argb(255, 0, 255, 0));
    }

    public boolean planeIsWithinReach(Location userLocation){

        if (userLocation.distanceTo(this.planeLocation) < this.circleRadius)
            return true;
        else
            return false;

    }

    public void showRadarArcPolyline(Boolean trueFalse){ this.radarArcPolyLine.setVisible(trueFalse); }

    private static Bitmap scaleDown(Bitmap image, float maxImageSize) {

        float ratio = Math.min(
                (float) maxImageSize / image.getWidth(),
                (float) maxImageSize / image.getHeight() );

        int width = Math.round((float) ratio * image.getWidth());
        int height = Math.round((float) ratio * image.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(image, width, height, true);
        return newBitmap;
    }


    private LatLng getPointGivenRadiusAndDegree(LatLng centre, double radius, double degree){
        final double EARTH_RADIUS = 6378100.0;
        // Convert to radians
        double lat = centre.latitude * Math.PI / 180.0;
        double lon = centre.longitude * Math.PI / 180.0;

        double radians = Math.toRadians(degree);

        // Calculate points
        double latPoint = lat + (radius / EARTH_RADIUS) * Math.sin(radians);
        double lonPoint = lon + (radius / EARTH_RADIUS) * Math.cos(radians) / Math.cos(lat);

        return new LatLng(latPoint * 180.0 / Math.PI, lonPoint * 180.0 / Math.PI);
    }


    public void animateRadarPulseForClosePlane(){
        final float radiusCopy = this.circleRadius;
        planeCircle.setStrokeColor(planeCircle.getFillColor());
        this.radarPulseAnimator = ValueAnimator.ofInt(0, 100);
        this.radarPulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        this.radarPulseAnimator.setRepeatMode(ValueAnimator.RESTART);
        this.radarPulseAnimator.setDuration(2500);
        this.radarPulseAnimator.setEvaluator(new IntEvaluator());
        this.radarPulseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        this.radarPulseAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedFraction = valueAnimator.getAnimatedFraction();
                planeCircle.setRadius(animatedFraction * radiusCopy); // * SIZE
            }
        });
    }

    public void animateRadarArcForClosePlane(){
        final float radiusCopy = this.circleRadius;
        this.radarArcAnimator = ValueAnimator.ofInt(0, 100);
        this.radarArcAnimator.setRepeatCount(ValueAnimator.INFINITE);
        this.radarArcAnimator.setRepeatMode(ValueAnimator.RESTART);
        this.radarArcAnimator.setDuration(1000);
        this.radarArcAnimator.setEvaluator(new IntEvaluator());
        this.radarArcAnimator.setInterpolator(new LinearInterpolator());
        this.radarArcAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedFraction = valueAnimator.getAnimatedFraction();
                ArrayList<LatLng> points = new ArrayList<LatLng>();
                for (int i=0; i < 45; i+=2){ // Skip the first point (center)
                    points.add(getPointGivenRadiusAndDegree(getPlaneMarker().getPosition(), radiusCopy, 360*animatedFraction + i));
                }
                radarArcPolyLine.setPoints(points);
            }
        });
    }

    public void deleteRadarArcAnimation(){
        if (this.radarArcAnimator != null) this.radarArcAnimator.cancel();
        if (this.radarArcPolyLine != null) this.radarArcPolyLine.remove();
        this.radarArcPolyLine = null;
        this.radarArcAnimator = null;
    }

    public void deleteRadarPulseAnimation(){
        if (this.radarPulseAnimator != null) this.radarPulseAnimator.cancel();
        this.radarPulseAnimator = null;
    }

    public void deleteMarker(){
        if (this.planeMarker != null) this.getPlaneMarker().remove();
    }

    public void deleteCircle(){
        if(this.planeCircle != null) this.getPlaneCircle().remove();
    }


    public void animatePlaneMarker(final LatLng destination, final float newRotationDegrees, final Location userLocation) {
        // WARNING: Animations might cause problems if they last longer than the refresh rate!

        final LatLng currentPosition = this.getPlaneLatLng();

        final Pair<Double, Double> currentDirection = new Pair(currentPosition.longitude - destination.longitude, currentPosition.latitude - destination.latitude); //Currently the direction vector isn't used in an effort to simplify the math.
        final Pair<Double, Double> destinationDirection = new Pair(destination.longitude - currentPosition.longitude, destination.latitude - currentPosition.latitude);

        /*if (abs(signedAngle) > PI) signedAngle += 2*PI;
        else signedAngle -= 2*PI;*/
        // Don't know why but it seems like Google Maps has been implemented "upside down" so clockwise is anti-clockwise in Google Maps.

        ValueAnimator ltAnimation = ValueAnimator.ofFloat((float) currentPosition.latitude, (float) destination.latitude);
        ValueAnimator lgAnimation = ValueAnimator.ofFloat((float) currentPosition.longitude, (float) destination.longitude);

        this.planeMarker.setRotation(newRotationDegrees);

        ltAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedValue = (float) valueAnimator.getAnimatedValue();
                LatLng setValue = new LatLng(animatedValue, getPlaneMarker().getPosition().longitude);

                planeMarker.setPosition(setValue);
                planeCircle.setCenter(setValue);

            }
        });

        lgAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedValue = (float) valueAnimator.getAnimatedValue();
                LatLng setValue = new LatLng(getPlaneMarker().getPosition().latitude, animatedValue);

                planeMarker.setPosition(setValue);
                planeCircle.setCenter(setValue);
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

                setPlanePosition(destination.latitude, destination.longitude);

                if ( planeIsWithinReach(userLocation) ) {
                    if (radarPulseAnimator == null){
                        // Plane is within reach and doesn't yet have a radar animation
                        planeCircle.setFillColor(Color.argb(100, 0, 255, 0));

                        animateRadarPulseForClosePlane();
                        animateRadarArcForClosePlane();
                        showRadarArcPolyline(true);

                        radarPulseAnimator.start();
                        radarArcAnimator.start();
                    }

                } else { // Plane is outside of reach
                    // If plane has existing animations, destroy them:
                    if (radarPulseAnimator != null) {
                        deleteRadarArcAnimation();
                        deleteRadarPulseAnimation();
                        setCircleVisible(false);

                    }
                    // Set area circle to have original information:
                    planeCircle.setFillColor(Color.argb(100, 0, 0, 100));
                    planeCircle.setRadius(circleRadius);
                    planeCircle.setStrokeColor(Color.WHITE);
                }
            }
        });
    }





    public void savePlane(Context context){
        // All apps (root or not) have a default data directory, which is /data/data/<package_name>
        String filename = "myPlanes";
        String earlierText = readCollectedPlanes(context);
        String text = this.planeID;
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
