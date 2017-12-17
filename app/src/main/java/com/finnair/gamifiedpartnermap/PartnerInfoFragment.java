package com.finnair.gamifiedpartnermap;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by axelv on 8.12.2017.
 */

public class PartnerInfoFragment extends DialogFragment {

    /*TODO:
    Add functionality to the info window.
    A way to close it easily would be a good start.
     */


    public PartnerInfoFragment(){}
    private String company;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View result = inflater.inflate(R.layout.fragment_partner_info, container, false);

        TextView companyName = ((TextView) result.findViewById(R.id.company_name));
        companyName.setText(company);
        companyName.setGravity(Gravity.CENTER);

        return result;
    }

    public void fillFields(String c) {
        company = c;
    }
}
