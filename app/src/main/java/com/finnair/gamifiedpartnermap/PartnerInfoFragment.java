package com.finnair.gamifiedpartnermap;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by axelv on 8.12.2017.
 */

public class PartnerInfoFragment extends DialogFragment {
    private String companyName = "ERROR";
    private String fieldOfBusiness = "ERROR";
    private String visitingAddress = "ERROR";
    private String companyDescription = "ERROR";
    private TextView nameTextView;
    private TextView businessTextView;
    private TextView addressTextView;
    private TextView descriptionTextView;


    public PartnerInfoFragment(){}  // This default constructor should be left alone

    public void setAllFragmentData(String companyName, String fieldOfBusiness, String visitingAddress, String companyDescription){
        // All private data should be set before calling onCreateView:
        this.companyName = companyName;
        this.fieldOfBusiness = fieldOfBusiness;
        this.visitingAddress = visitingAddress;
        this.companyDescription = companyDescription;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_partner_info, container, false);

        // Get references to TextViews. Method setContents() places information into the TextViews
        this.nameTextView = view.findViewById(R.id.company_name);
        this.businessTextView = view.findViewById(R.id.field_of_business);
        this.addressTextView = view.findViewById(R.id.visiting_address);
        this.descriptionTextView = view.findViewById(R.id.company_description);

        this.setContent();
        return view;
    }

    private void setContent(){
        // This method should be private and only called in onCreateView() after the view has been inflated
        // By the time this is called, all private data must be available
        this.nameTextView.setText(this.companyName);
        this.businessTextView.setText(this.fieldOfBusiness);
        this.addressTextView.setText(this.visitingAddress);
        this.descriptionTextView.setText(this.companyDescription);

    }

}
