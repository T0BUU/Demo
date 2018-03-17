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


    public PartnerInfoFragment(){}  // This default constructor should be left alone

    public void setAllFragmentData(String companyInfo, String fieldOfBusiness, int image){
        // All private data should be set before calling onCreateView:
        this.companyInfo = companyInfo;
        this.fieldOfBusiness = fieldOfBusiness;
        this.image = image;

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

        nameTextView.setMovementMethod(new ScrollingMovementMethod());


        //Set the color of border. Maybe we should have different types of cards with different colored borders?
        ((GradientDrawable)dialogView.findViewById(R.id.partner_info_table).getBackground()).setStroke(10, Color.parseColor("#CCCCCC"));

        this.setContent();



        // Create the AlertDialog object and return it
        Dialog result = builder.create();

        //result.setCanceledOnTouchOutside(false);

        result.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return result;
    }

    private void setContent(){
        // This method should be private and only called in onCreateView() after the view has been inflated
        // By the time this is called, all private data must be available
        this.nameTextView.setText(this.companyInfo);
        this.businessTextView.setText(this.fieldOfBusiness);
        this.imageView.setImageResource(this.image);

    }

}
