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

    private int chooseMarkerImage() {

        switch (this.fieldOfBusiness) {
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
            default: return  R.raw.aalto_logo;
        }
    }

    public void setMarkerImage(Integer screenWidth){

        Bitmap bitmap = BitmapFactory.decodeResource(this.activity.getResources(), chooseMarkerImage()); //
        Bitmap smallBitmap = scaleDown(bitmap, screenWidth / 8);
        BitmapDescriptor bitmapIcon = BitmapDescriptorFactory.fromBitmap( smallBitmap );

        setMarkerImage(bitmapIcon);
    }

    public void setFieldOfBusiness(String business){ this.fieldOfBusiness = business; }
    public void setAddress(String address){ this.address = address; }

    public String getFieldOfBusiness(){ return this.fieldOfBusiness; }
    public String getAddress(){ return this.address; }
    public String getDescription(){ return getSnippet(); }

}
