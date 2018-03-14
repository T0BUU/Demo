package com.finnair.gamifiedpartnermap;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by ala-hazla on 14.3.2018.
 */

public class PartnerInfoWindow implements GoogleMap.InfoWindowAdapter {
    private Context context;

    private TextView name;
    private TextView address;
    private ImageView img;
    private TextView description;

    public PartnerInfoWindow(Context ctx){
        context = ctx;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = ((Activity)context).getLayoutInflater()
                .inflate(R.layout.partner_info_window_layout, null);

        name = view.findViewById(R.id.company_name);
        address = view.findViewById(R.id.company_address);
        img = view.findViewById(R.id.partner_image);
        description = view.findViewById(R.id.company_description);

        InfoWindowData infoWindowData = (InfoWindowData) marker.getTag();

        name.setText(infoWindowData.getName());
        address.setText(infoWindowData.getAddress());
        description.setText(infoWindowData.getDescription());

        if (infoWindowData.getImage() == -1) {}
        else img.setImageResource(infoWindowData.getImage());

        return view;
    }

}
