package com.finnair.gamifiedpartnermap;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by axelv on 8.12.2017.
 */

public class PartnerInfoFragment extends DialogFragment {
    private String companyInfo = "ERROR";
    private String fieldOfBusiness = "ERROR";
    private int image = -1;
    private TextView nameTextView;
    private TextView businessTextView;
    private ImageView imageView;
    private String upperButtonText;
    private String lowerButtonText;
    private int upperButtonVisibility;
    private int lowerButtonVisibility;

    private Button upper;
    private Button lower;


    public PartnerInfoFragment(){}  // This default constructor should be left alone

    public void setAllFragmentData(String companyInfo, String fieldOfBusiness, int image){
        // All private data should be set before calling onCreateView:
        this.companyInfo = companyInfo;
        this.fieldOfBusiness = fieldOfBusiness;
        this.image = image;
        upperButtonText = "Go To Map";
        lowerButtonText = "Go To Collection";
        this.upperButtonVisibility = 0;
        this.lowerButtonVisibility = 0;

    }

    public void setAllFragmentData(String companyInfo, String fieldOfBusiness, int image,
                                   String upperButtonText, String lowerButtonText,
                                   int upperButtonVisibility, int lowerButtonVisibility){
        // All private data should be set before calling onCreateView:
        this.companyInfo = companyInfo;
        this.fieldOfBusiness = fieldOfBusiness;
        this.image = image;
        this.upperButtonText = upperButtonText;
        this.lowerButtonText = lowerButtonText;
        this.upperButtonVisibility = upperButtonVisibility;
        this.lowerButtonVisibility = lowerButtonVisibility;

    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.fragment_partner_info, null);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(dialogView);

        // Get references to TextViews. Method setContents() places information into the TextViews
        this.nameTextView = dialogView.findViewById(R.id.company_info);
        this.businessTextView = dialogView.findViewById(R.id.field_of_business);
        this.imageView = dialogView.findViewById(R.id.partner_card_image);
        this.upper = dialogView.findViewById(R.id.card_button_upper);
        this.lower = dialogView.findViewById(R.id.card_button_lower);

        nameTextView.setMovementMethod(new ScrollingMovementMethod());


        //Set the color of border. Maybe we should have different types of cards with different colored borders?
        ((GradientDrawable)dialogView.findViewById(R.id.partner_info_table).getBackground()).setStroke(10, Color.parseColor("#CCCCCC"));

        this.setContent();



        // Create the AlertDialog object and return it
        Dialog result = builder.create();


        result.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return result;
    }


    private void setContent(){
        // This method should be private and only called in onCreateView() after the view has been inflated
        // By the time this is called, all private data must be available
        this.nameTextView.setText(this.companyInfo);
        this.businessTextView.setText(this.fieldOfBusiness);
        this.imageView.setImageResource(this.image);
        this.upper.setText(this.upperButtonText);
        this.lower.setText(this.lowerButtonText);
        this.upper.setVisibility(this.upperButtonVisibility);
        this.lower.setVisibility(this.lowerButtonVisibility);

    }

}
