package com.finnair.gamifiedpartnermap;

import android.animation.Animator;
import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
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
import com.google.maps.android.clustering.ClusterItem;

import java.util.ArrayList;

/**
 * Created by noctuaPC on 11.2.2018.
 */

public class ClusterMarker implements ClusterItem {

    Activity activity;

    protected float circleRadius;

    private LatLng latLng;
    private Location location = new Location("");
    private Marker marker;
    private Circle circle;
    private MarkerOptions markerOptions;
    private CircleOptions circleOptions;
    private String id;
    private String snippet;
    private Double headingDegree = 0.0; // Plane overrides this. Partner doesn't

    private ValueAnimator radarPulseAnimator;
    private ValueAnimator radarArcAnimator;
    private Polyline radarArcPolyLine;
    private BitmapDescriptor bitmapIcon;


    public ClusterMarker(Activity activity){ this.activity = activity; }

    @Override
    public LatLng getPosition(){
        return latLng;
    }

    @Override
    public String getTitle() { return this.id;}

    @Override
    public String getSnippet(){ return this.snippet; }

    // SET:
    public void setCircleRadius(float radius){ this.circleRadius = radius; }
    public void setPosition(Double latitude, Double longitude) {
        this.latLng = new LatLng(latitude, longitude);
        this.location.setLatitude(latitude);
        this.location.setLongitude(longitude);
    }
    public void setMarker(Marker marker){ this.marker = marker; }
    public void setCircle(Circle circle){ this.circle = circle; }
    public void setCircleVisible(Boolean yesNo){ this.circle.setVisible(yesNo); }
    public void setRadarArcPolyLine(Polyline radarArcPolyLine){ this.radarArcPolyLine = radarArcPolyLine;}
    public void setHeading(Double headingDegree){ this.headingDegree = headingDegree; }
    public void setID(String id){ this.id = id; }
    public void setSnippet(String snippet){ this.snippet = snippet; }

    public void setCircleOptions(){
        this.circleOptions = new CircleOptions()
                .center(this.latLng)
                .radius(this.circleRadius)
                .strokeWidth(10)
                .strokeColor(Color.WHITE)
                .fillColor(Color.argb(100, 0, 0, 100));
    }

    public void setMarkerImage(BitmapDescriptor bitmapIcon){
        this.bitmapIcon = bitmapIcon;
        this.markerOptions.icon(bitmapIcon);
    }

    public void setMarkerImage(Integer screenWidth){
        // Default image (blue balloon). This is overridden in Partner and Plane
        setMarkerImage(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
    }

    public void setMarkerOptions(){
        this.markerOptions = new MarkerOptions();
        this.markerOptions.position(this.latLng)
                .rotation( Double.valueOf(this.headingDegree).floatValue() )
                .anchor(0.5f, 0.5f)
                .title(this.id)
                .snippet(this.snippet)
                .flat(true);
    }

    // GET:
    public Location getLocation(){ return this.location; }
    public LatLng getLatLng(){ return this.latLng; }
    public Double getHeadingDegree(){ return this.headingDegree; }
    public MarkerOptions getMarkerOptions(){ return this.markerOptions; }
    public CircleOptions getCircleOptions(){ return this.circleOptions; }
    public float getCircleRadius(){ return this.circleRadius; }
    public Marker getMarker(){ return this.marker; }
    public Circle getCircle(){ return this.circle; }
    public Polyline getRadarArcPolyLine(){ return this.radarArcPolyLine; }
    public Animator getRadarPulseAnimation(){ return this.radarPulseAnimator; }
    public BitmapDescriptor getIcon(){ return this.bitmapIcon; }
    public String getID(){ return this.id; }


    public PolylineOptions getRadarPolyLineOptions(){
        LatLng center = getPosition();

        ArrayList<LatLng> arcPoints = new ArrayList<>();
        for (int i=0; i < 45; i+=2)
            arcPoints.add(getPointGivenRadiusAndDegree(center, this.circleRadius, 0 + i));

        return new PolylineOptions()
                .addAll(arcPoints)
                .width(8)
                .color(Color.argb(255, 0, 255, 0));
    }


    public void startRadarPulseAnimation(){ this.radarPulseAnimator.start(); }
    public void startRadarArcAnimation(){ this.radarArcAnimator.start(); }

    public void animateRadarPulseForClosePlane(){
        final float radiusCopy = this.circleRadius;
        circle.setStrokeColor(circle.getFillColor());
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
                circle.setRadius(animatedFraction * radiusCopy); // * SIZE
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
                    points.add(getPointGivenRadiusAndDegree(getPosition(), radiusCopy, 360*animatedFraction + i));
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
        if (this.marker != null) this.getMarker().remove();
    }

    public void deleteCircle(){
        if(this.circle != null) this.getCircle().remove();
    }


    public boolean isWithinReach(Location userLocation){

        if (userLocation.distanceTo(this.location) < this.circleRadius)
            return true;
        else
            return false;

    }

    public void showRadarArcPolyline(Boolean trueFalse){ this.radarArcPolyLine.setVisible(trueFalse); }

    public BitmapDrawable writeOnDrawable(int drawableId, String text){

        Bitmap bm = BitmapFactory.decodeResource(this.activity.getResources(), drawableId).copy(Bitmap.Config.ARGB_8888, true);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setTextSize(400);

        Canvas canvas = new Canvas(bm);
        canvas.drawText(text, 0, bm.getHeight()/2, paint);

        return new BitmapDrawable(bm);
    }

    public static Bitmap scaleDown(Bitmap image, float maxImageSize) {

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

}
