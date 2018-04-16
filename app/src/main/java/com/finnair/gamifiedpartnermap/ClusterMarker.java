package com.finnair.gamifiedpartnermap;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Display;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;

import java.util.ArrayList;


public class ClusterMarker implements ClusterItem {

    Activity activity;

    protected float circleRadius;

    private LatLng latLng;
    private Location location = new Location("");
    private MarkerOptions markerOptions;
    private String id;
    private String snippet;
    private Double headingDegree = 0.0; // Plane overrides this. Partner doesn't
    private boolean bonusMarkerEnabled = false;
    private boolean bonusMarkerVisible = false;
    private Marker bonusMarker;
    private float screenWidth;
    private GoogleMap mMap;
    private BitmapDescriptor bonusIcon;

    private BitmapDescriptor bitmapIcon;

    private ArrayList<Challenge> relatedChallenges = new ArrayList<>();


    public ClusterMarker(Activity activity){
        this.activity = activity;
        Display display = this.activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        this.screenWidth = size.x;
    }

    @Override
    public LatLng getPosition(){
        return latLng;
    }

    @Override
    public String getTitle() { return this.id;}

    @Override
    public String getSnippet(){ return this.snippet; }


    // SET:

    public void setPosition(Double latitude, Double longitude) {
        this.latLng = new LatLng(latitude, longitude);
        this.location.setLatitude(latitude);
        this.location.setLongitude(longitude);
    }
    public void setPosition(LatLng latLng){
        this.latLng = latLng;
        this.location.setLatitude(latLng.latitude);
        this.location.setLongitude(latLng.longitude);
    }
    public void setCircleRadius(float radius){ this.circleRadius = radius; }
    public void setHeading(Double headingDegree){ this.headingDegree = headingDegree; }
    public void setID(String id){ this.id = id; }
    public void setSnippet(String snippet){ this.snippet = snippet; }

    public void setMarkerImage(BitmapDescriptor bitmapIcon){
        this.bitmapIcon = bitmapIcon;
        this.markerOptions.icon(bitmapIcon);
        this.markerOptions.anchor(0.5f, 0.5f);
    }


    public void setAlpha(float alpha){
        this.markerOptions.alpha(alpha);
    }

    public void setMarkerImage(Integer screenWidth){
        // Default image (violet balloon). This is overridden in Partner and Plane
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

    public void addRelatedChallenge(Challenge c) {
        this.relatedChallenges.add(c);
    }

    public void removeRelatedChallenge(Challenge c) {
        relatedChallenges.remove(c);
    }

    // GET:
    public Location getLocation(){ return this.location; }
    public LatLng getLatLng(){ return this.latLng; }
    public Double getHeadingDegree(){ return this.headingDegree; }
    public BitmapDescriptor getIcon(){ return this.bitmapIcon; }
    public String getID(){ return this.id; }
    public ArrayList<Challenge> getRelatedChallenges() { return this.relatedChallenges; }

    public void setBonusMarkerEnabled(boolean enable){
        this.bonusMarkerEnabled = enable;
        setBonusMarker();
    }

    public void setBonusMarkerVisible(boolean visible){
        this.bonusMarkerVisible = visible;
        setBonusMarker();
    }


    public void moveBonusMarker(LatLng latLng){
        if (this.bonusMarker != null)
            this.bonusMarker.setPosition(latLng);
    }

    public void setBonusMarker(GoogleMap map){
        this.mMap = map;
        Bitmap icon = BitmapFactory.decodeResource(this.activity.getResources(),
                R.drawable.ic_letter_b_small);
        icon = scaleDown(icon, screenWidth/12);
        this.bonusIcon = BitmapDescriptorFactory.fromBitmap(icon);

        if (this.bonusMarker == null){
            if (this.bonusMarkerVisible && this.bonusMarkerEnabled){

                this.bonusMarker = map.addMarker( new MarkerOptions()
                        .position(this.latLng)
                        .icon(bonusIcon)
                        .rotation(0)
                        .anchor(0.0f, 1.0f)
                        .flat(true));
            }
        }
    }

    public void setBonusMarker(){
        if (this.bonusMarker == null){ // No bonusMarker exists:
            if (this.bonusMarkerEnabled && this.bonusMarkerVisible){ // if both visible and enabled:
                bonusMarker = this.mMap.addMarker( new MarkerOptions()
                        .position(this.latLng)
                        .icon(this.bonusIcon)
                        .rotation(0)
                        .anchor(0.0f, 1.0f)
                        .flat(true));
            }
        } else{ // bonusMarker exists:
            if (!this.bonusMarkerEnabled || !this.bonusMarkerVisible){ // if either non-visible or disabled:
                this.bonusMarker.remove();
                this.bonusMarker = null;
            }

        }
    }

    public LatLng getBonusMarkerPosition(){
        if (this.bonusMarker != null)
            return this.bonusMarker.getPosition();
        else
            return null;
    }

    protected int chooseMarkerImage(String imageType) {

        switch (imageType) {
            case "Restaurant": return R.drawable.ic_restaurants;
            case "Car rental": return R.drawable.ic_car_rental;
            case "Charity": return R.drawable.ic_charity;
            case "Entertainment": return R.drawable.ic_entertainment;
            case "Finance and insurance": return R.drawable.ic_finance;
            case "Helsinki Airport": return R.drawable.ic_helsinki_vantaa;
            case "Golf and leisure time": return R.drawable.ic_hobbies;
            case "Hotel": return R.drawable.ic_hotels_spas;
            case "Shopping": return R.drawable.ic_shopping;
            case "Tour operators and cruise lines": return R.drawable.ic_travel;
            case "Services and Wellness": return R.drawable.ic_services_healthcare;
            case "Airplane": return R.drawable.ic_airplane;
            default: return  R.raw.aalto_logo;
        }
    }

    public boolean isWithinReach(Location userLocation){

        if (userLocation.distanceTo(this.location) < this.circleRadius)
            return true;
        else
            return false;
    }

    protected BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId, int sizeMultiplier, PorterDuffColorFilter colorFilter) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        if (colorFilter != null)
            vectorDrawable.setColorFilter(colorFilter);
        vectorDrawable.setBounds(0, 0, sizeMultiplier*vectorDrawable.getIntrinsicWidth(), sizeMultiplier*vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(sizeMultiplier*vectorDrawable.getIntrinsicWidth(), sizeMultiplier*vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
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


}
