package com.finnair.gamifiedpartnermap;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;


/**
 * Created by noctuaPC on 11.2.2018.
 */

public class Partner extends ClusterMarker {
    private String fieldOfBusiness;
    private String address;

    public Partner(Activity activity){
        super(activity);
        setCircleRadius(250);
    }

    public void setMarkerImage(Integer screenWidth){

        Bitmap bitmap = BitmapFactory.decodeResource(this.activity.getResources(), chooseMarkerImage(this.fieldOfBusiness)); //
        Bitmap smallBitmap = scaleDown(bitmap, screenWidth / 12);
        BitmapDescriptor bitmapIcon = BitmapDescriptorFactory.fromBitmap( smallBitmap );

        setMarkerImage(bitmapIcon);
    }

    public void setFieldOfBusiness(String business){ this.fieldOfBusiness = business; }
    public void setAddress(String address){ this.address = address; }

    public String getFieldOfBusiness(){ return this.fieldOfBusiness; }
    public String getAddress(){ return this.address; }
    public String getDescription(){ return getSnippet(); }

}
