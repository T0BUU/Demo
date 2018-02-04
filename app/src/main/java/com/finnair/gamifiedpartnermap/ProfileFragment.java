package com.finnair.gamifiedpartnermap;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by ala-hazla on 16.12.2017.
 */

public class ProfileFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Santeri: Please note that clicking a plane's "Title" will save the plane in
        // memory and print it from memory to ProfileFragment's TextView.
        // This is only a silly test and can be deleted when necessary
        return inflater.inflate(R.layout.content_profile, container, false);
    }


}
