package com.finnair.gamifiedpartnermap;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;


public class ClusterMarker implements ClusterItem {

    Activity activity;

    protected float circleRadius;

    private LatLng latLng;
    private Location location = new Location("");
    private MarkerOptions markerOptions;
    private String id;
    private String snippet;
    private Double headingDegree = 0.0; // Plane overrides this. Partner doesn't

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

    // GET:
    public Location getLocation(){ return this.location; }
    public LatLng getLatLng(){ return this.latLng; }
    public Double getHeadingDegree(){ return this.headingDegree; }
    public float getCircleRadius(){ return this.circleRadius; }
    public BitmapDescriptor getIcon(){ return this.bitmapIcon; }
    public String getID(){ return this.id; }


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
