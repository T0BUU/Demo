package com.finnair.gamifiedpartnermap;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import com.google.android.gms.maps.model.BitmapDescriptor;



public class Plane extends ClusterMarker {

    private Double geoAltitude;
    private Double velocityKmph;
    private String originCountry;
    private String icao24;
    private String planeType;
    private boolean statusDistanceClose = false;
    private boolean statusCollected = false;
    private PorterDuffColorFilter planeColorFilter = null;


    public Plane(Activity activity){
        super(activity);
        setCircleRadius(30000);
    }


    // SET:
    public void setPlaneMiscellaneousInformation(Double geoAltitude, Double velocity, String icao24, String originCountry){
        if (geoAltitude != null) this.geoAltitude = geoAltitude;
        if (velocity != null) this.velocityKmph = velocity * 1.852; // From knots to km/h
        if (icao24 != null) this.icao24 = icao24;
        if (originCountry != null) this.originCountry = originCountry;
    }



    public void setPlaneType(String type) { this.planeType = type; }

    // GET:
    public Double getVelocityKmph(){ return this.velocityKmph; }
    public String getOriginCountry(){ return this.originCountry; }
    public String getIcao24(){ return this.icao24; }
    public Double getGeoAltitude(){ return this.geoAltitude; }
    public String getPlaneType() { return this.planeType; }


    public void setMarkerImage(Integer screenWidth){
        setMarkerImage(bitmapDescriptorFromVector(this.activity, R.drawable.ic_airplane, 2, null));
    }

    public void setStatusDistanceClose(boolean status){ this.statusDistanceClose = status; }
    public void setStatusCollected(boolean status){
        this.statusCollected = status;
    }


    public void setMarkerImage(){

        if (this.statusCollected) {
            setAlpha(0.5f);
        }
        else {
            setAlpha(1.0f);
        }

        if (statusDistanceClose)
            planeColorFilter = new PorterDuffColorFilter(Color.argb(252, 0, 0, 160), PorterDuff.Mode.SRC_IN);
        else
            planeColorFilter = null;

        BitmapDescriptor bmd = bitmapDescriptorFromVector(this.activity, R.drawable.ic_airplane, 1, this.planeColorFilter);
        setMarkerImage(bmd);

    }

}
