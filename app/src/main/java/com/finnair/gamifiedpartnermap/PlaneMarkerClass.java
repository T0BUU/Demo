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
import android.graphics.Point;
import android.location.Location;
import android.util.Log;
import android.util.Pair;
import android.view.Display;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.GoogleMap;
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
import java.util.HashMap;
import java.util.List;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.atan;
import static java.lang.Math.atan2;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
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
    java.util.ArrayList<LatLng> planeDirectionVectors = new java.util.ArrayList<>();
    GoogleMap mMap;

    private final Float rotationMultiplier = -100.0f;
    private Location userLocation;
    private Location temporaryPlaneLocation = new Location("");
    private final float radius = 1000;
    private HashMap<Marker, Pair<ValueAnimator, ValueAnimator>> planeRadarMap = new HashMap<>();
    private HashMap<Marker, Polyline> planeRadarArcMap = new HashMap<>();

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

    public void addOneMarkerOnMap(Double latitude, Double longitude, String planeName){

        CircleOptions areaOptions = this.areaMarkerOptions(new LatLng(latitude, longitude));
        MarkerOptions imageOptions = this.imageMarkerOptions(areaOptions, planeName);

        //Add a direction vector. All planes start by facing East so a the point (latitude, longtitude - 1) is directly "behind" them.
        //The actual vector is calculated when need be. The point that is stored here is used to calculate the initial direction of the plane.
        this.planeDirectionVectors.add(new LatLng(latitude, longitude - 1));

        Circle areaMarker = this.mMap.addCircle(areaOptions);
        Marker planeMarker = this.mMap.addMarker(imageOptions);

        this.markerArrayList.add( new Pair(planeMarker, areaMarker) );

    }

    public boolean markerArrayContainsMarker(Marker marker){

        for (Pair <Marker, Circle> pair: this.markerArrayList){
            if (pair.first.equals(marker))
                return true;
        }
        return false;
    }

    public boolean planeIsWithinReach(Location location, Marker plane){
        temporaryPlaneLocation.setLatitude(plane.getPosition().latitude);
        temporaryPlaneLocation.setLongitude(plane.getPosition().longitude);
        if (location.distanceTo(temporaryPlaneLocation) < radius)
            return true;
        else
            return false;
    }


    public void animateMarkers(List<LatLng> coords, Location userLocation) {
        this.userLocation = userLocation;
        for (int i = 0; i < markerArrayList.size(); i++) {
            animatePlaneMarker(coords.get(i), markerArrayList.get(i), i);

        }

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


    private CircleOptions areaMarkerOptions(LatLng coords){

            return new CircleOptions()
                .center(coords)
                .radius(this.radius)
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
                .icon(bitmapIcon)
                .flat(true);
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

    final PolylineOptions radarPolyLineOptions(Marker plane, double degree){
        LatLng center = plane.getPosition();

        ArrayList<LatLng> arcPoints = new ArrayList<>();
        for (int i=0; i < 45; i+=2)
            arcPoints.add(getPointGivenRadiusAndDegree(center, this.radius, degree + i));

        return new PolylineOptions()
                .addAll(arcPoints)
                .width(8)
                .color(Color.argb(255, 0, 255, 0));
    }

    private ValueAnimator animateRadarPulseForClosePlane(final Circle circle){
        final float radiusCopy = this.radius;
        circle.setStrokeColor(circle.getFillColor());
        ValueAnimator circlePulseAnimator = ValueAnimator.ofInt(0, 100);
        circlePulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        circlePulseAnimator.setRepeatMode(ValueAnimator.RESTART);
        circlePulseAnimator.setDuration(2500);
        circlePulseAnimator.setEvaluator(new IntEvaluator());
        circlePulseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        circlePulseAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedFraction = valueAnimator.getAnimatedFraction();
                circle.setRadius(animatedFraction * radiusCopy); // * SIZE

            }
        });

        return circlePulseAnimator;
    }

    private Pair<Polyline, ValueAnimator> animateRadarArcForClosePlane(final Marker plane){
        final float radiusCopy = this.radius;
        final Polyline radarArc = this.mMap.addPolyline(radarPolyLineOptions(plane, 0) );


        ValueAnimator radarAnimator = ValueAnimator.ofInt(0, 100);
        radarAnimator.setRepeatCount(ValueAnimator.INFINITE);
        radarAnimator.setRepeatMode(ValueAnimator.RESTART);
        radarAnimator.setDuration(1000);
        radarAnimator.setEvaluator(new IntEvaluator());
        radarAnimator.setInterpolator(new LinearInterpolator());
        radarAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedFraction = valueAnimator.getAnimatedFraction();
                ArrayList<LatLng> points = new ArrayList<LatLng>();
                for (int i=0; i < 45; i+=2){ // Skip the first point (center)
                    points.add(getPointGivenRadiusAndDegree(plane.getPosition(), radiusCopy, 360*animatedFraction + i));
                }
                radarArc.setPoints(points);

            }
        });
        return Pair.create(radarArc, radarAnimator);

    }

    private double vectorNorm(double i, double j) {
        return sqrt(pow(i, 2)+pow(j, 2));
    }

    private double dotProduct(double i1, double j1, double i2, double j2) {
        return i1*i2+j1*j2;
    }

    private double angleOfRotation(double i1, double j1, double i2, double j2) {
        //First calculate the angle between the two vectors.
        double angle = acos( dotProduct(i1,j1,i2,j2)/( vectorNorm(i1,j1)*vectorNorm(i2,j2) ) );
        //The the cross product
        double cross = vectorNorm(i1, j1)*vectorNorm(i2, j2)*sin(angle);
        //Now using the cross product determine the direction of rotation.
        if (cross < 0) return -abs(angle);
        else return abs(angle);
    }

    private void animatePlaneMarker(LatLng destination, final Pair<Marker, Circle> planeMarkers, int i) {
        // WARNING: Animations might cause problems if they last longer than the refresh rate!

        final Marker plane = planeMarkers.first;
        final Circle area = planeMarkers.second;

        final LatLng currentPosition = plane.getPosition();
        float initialRotation = plane.getRotation();

        if (initialRotation > 360.0) initialRotation -= 360;

        final float startRotation = initialRotation;

        //This is used tp calculate a direction vector.
        LatLng directionPoint = planeDirectionVectors.get(i);

        final Pair<Double, Double> currentDirection = new Pair(currentPosition.longitude-directionPoint.longitude, currentPosition.latitude-directionPoint.latitude); //Currently the direction vector isn't used in an effort to simplify the math.
        final Pair<Double, Double> destinationDirection = new Pair(destination.longitude-currentPosition.longitude, destination.latitude-currentPosition.latitude);

        planeDirectionVectors.set(i, currentPosition);

        double signedAngle = atan2(destinationDirection.second, destinationDirection.first) - atan2(currentDirection.second,currentDirection.first);

        if (abs(signedAngle) > PI) signedAngle += 2*PI;
        else signedAngle -= 2*PI;
        //Don't know why but it seems like Google Maps has been implemented "upside down" so clockwise is anti-clockwise in Google Maps.
        final float endRotation = (float) toDegrees(-signedAngle);

        Log.d("The angle of rotation", ""+endRotation + " and  " + destinationDirection);

        ValueAnimator ltAnimation = ValueAnimator.ofFloat((float) currentPosition.latitude, (float) destination.latitude);
        ValueAnimator lgAnimation = ValueAnimator.ofFloat((float) currentPosition.longitude, (float) destination.longitude);


        plane.setRotation(startRotation + endRotation);

        Log.d("The final rotation: ", "" + plane.getRotation());

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

        // Rotation cannot happen together with directional animation (plane flies sideways):
        LtLg.playTogether(ltAnimation, lgAnimation);
        LtLg.setDuration(10000);
        LtLg.start();
        LtLg.addListener(new AnimatorListenerAdapter() {
            // Animation ending must be listened separately, because it runs in its own thread
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                // If plane has existing animations, destroy them:
                if (planeRadarMap.containsKey(plane)){
                    planeRadarMap.get(plane).first.cancel();
                    planeRadarMap.get(plane).second.cancel();
                    planeRadarMap.remove(plane);
                    planeRadarArcMap.get(plane).remove(); // Remove old arc (PolyLine)
                }

                if (planeIsWithinReach(userLocation, plane)){
                    area.setFillColor(Color.argb(100, 0, 255, 0));

                    ValueAnimator radarPulse = animateRadarPulseForClosePlane(area);
                    Pair<Polyline, ValueAnimator> radarArc = animateRadarArcForClosePlane(plane);
                    radarPulse.start();
                    planeRadarArcMap.put(plane, radarArc.first);
                    radarArc.second.start();

                    planeRadarMap.put(plane, Pair.create(radarPulse, radarArc.second));

                } else{

                    // Set area circle to have original information:
                    area.setFillColor(Color.argb(100, 0, 0, 100));
                    area.setRadius(radius);
                    area.setStrokeColor(Color.WHITE);
                }
            }
        });


    }
    public void saveCollectedPlane(Marker plane, Context context){
        // All apps (root or not) have a default data directory, which is /data/data/<package_name>
        String filename = "myPlanes";
        String earlierText = readCollectedPlanes(context);
        String text = plane.getTitle();
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

