package com.finnair.gamifiedpartnermap;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by axelv on 8.12.2017.
 */

public class PartnerInfoFragment extends DialogFragment {

    public PartnerInfoFragment(){}

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_partner_info, container, false);
    }
}
