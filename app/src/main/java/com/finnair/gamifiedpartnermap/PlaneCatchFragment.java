package com.finnair.gamifiedpartnermap;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by huzla on 1.2.2018.
 */

public class PlaneCatchFragment extends DialogFragment {

    private String planeName = "ERROR";
    private int planeLevel = -1; //This could be used to indicated different kinds of cards(gold, silver etc.)
    private String locations = "ERROR";
    private int planeImage;

    private TextView nameTextView;
    private TextView countriesTextView;
    private ImageView planeImageView;


    public PlaneCatchFragment(){}  // This default constructor should be left alone

    public void setAllFragmentData(String planeName, String locations){
        // All private data should be set before calling onCreateView:
        this.planeName = planeName;
        this.locations = locations;
    }

    public void setAllFragmentData(String planeName, String locations, int imageID){
        // All private data should be set before calling onCreateView:
        this.planeName = planeName;
        this.locations = locations;
        this.planeImage = imageID;
    }

    public interface PlaneCatchListener {
        public void onPlaneDialogPositiveClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    PlaneCatchFragment.PlaneCatchListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (PlaneCatchFragment.PlaneCatchListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }


    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.fragment_plane_info, null);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(dialogView);

        // Get references to TextViews. Method setContents() places information into the TextViews
        this.nameTextView = dialogView.findViewById(R.id.plane_name);
        this.countriesTextView = dialogView.findViewById(R.id.location_list);

        this.countriesTextView.setMovementMethod(new ScrollingMovementMethod());

        this.planeImageView = dialogView.findViewById(R.id.plane_card_image);



        //Set the color of border. Maybe we should have different types of with different colors for borders cards?
        ((GradientDrawable)dialogView.findViewById(R.id.plane_info_table).getBackground()).setStroke(10, Color.parseColor("#CCCCCC"));

        this.setContent();



        // Create the AlertDialog object and return it
        Dialog result = builder.create();

        result.setCanceledOnTouchOutside(false);

        result.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return result;
    }


    private void setContent(){
        // This method should be private and only called in onCreateView() after the view has been inflated
        // By the time this is called, all private data must be available
        this.nameTextView.setText(this.planeName);

        this.countriesTextView.setText(locations);

        this.planeImageView.setImageResource(this.planeImage);

        this.planeImageView.setMaxHeight(120);
        this.planeImageView.setMaxWidth(120);

    }

}
