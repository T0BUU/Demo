package com.finnair.gamifiedpartnermap;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.media.Image;
import android.opengl.Visibility;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.finnair.gamifiedpartnermap.PlaneCatchFragment.CardLevel.BASIC;
import static com.finnair.gamifiedpartnermap.PlaneCatchFragment.CardLevel.GOLD;
import static com.finnair.gamifiedpartnermap.PlaneCatchFragment.CardLevel.LUMO;
import static com.finnair.gamifiedpartnermap.PlaneCatchFragment.CardLevel.PLATINUM;
import static com.finnair.gamifiedpartnermap.PlaneCatchFragment.CardLevel.SILVER;


/**
 * Created by huzla on 1.2.2018.
 */

public class PlaneCatchFragment extends DialogFragment {
    public enum CardLevel {
        BASIC, SILVER, GOLD, PLATINUM, LUMO
    }

    private String planeName = "ERROR";
    private CardLevel planeLevel = BASIC; //This could be used to indicated different kinds of cards(gold, silver etc.)
    private String locations = "ERROR";
    private int planeImage;
    private String upperButtonText;
    private String lowerButtonText;
    private int upperButtonVisibility;
    private int lowerButtonVisibility;
    private int currentProgress;

    private TextView nameTextView;
    private TextView countriesTextView;
    private ImageView planeImageView;
    private View view;

    private Button upper;
    private Button lower;


    public PlaneCatchFragment(){}  // This default constructor should be left alone

    public void setAllFragmentData(String planeName, String locations){
        // All private data should be set before calling onCreateView:
        this.planeName = planeName;
        this.locations = locations;
    }

    public void setAllFragmentData(String planeName, String locations, int imageID, int progress){
        // All private data should be set before calling onCreateView:
        this.planeName = planeName;
        this.locations = locations;
        this.planeImage = imageID;
        this.upperButtonText = "Go To Map";
        this.lowerButtonText = "Go To Collection";
        this.upperButtonVisibility = 0;
        this.lowerButtonVisibility = 0;
        this.currentProgress = progress;
        this.planeLevel = setCardLevel(progress);

    }


    public void setAllFragmentData(String planeName, String locations, int imageID,
                                   String upperButtonText, String lowerButtonText,
                                   int upperButtonVisibility, int lowerButtonVisibility,
                                   int progress){
        // All private data should be set before calling onCreateView:
        this.planeName = planeName;
        this.locations = locations;
        this.planeImage = imageID;
        this.upperButtonText = upperButtonText;
        this.lowerButtonText = lowerButtonText;
        this.upperButtonVisibility = upperButtonVisibility;
        this.lowerButtonVisibility = lowerButtonVisibility;
        this.currentProgress = progress;
        this.planeLevel = setCardLevel(progress);
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

        this.upper = dialogView.findViewById(R.id.card_button_upper);

        this.lower = dialogView.findViewById(R.id.card_button_lower);

        this.view = dialogView;

        this.setContent();

        // Create the AlertDialog object and return it
        Dialog result = builder.create();

        result.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return result;
    }

    private CardLevel setCardLevel(int progress) {

        if (progress > 166) return LUMO;

        else if( progress > 66 ) return PLATINUM;

        else if ( progress > 16 ) return GOLD;

        else if ( progress > 6 ) return SILVER;

        else return BASIC;
    }


    private void setBorderColor(View dialogView) {
        String levelColor;

        switch (this.planeLevel) {
            case BASIC:
                levelColor = "#FFe9e8e8";
                break;
            case SILVER:
                levelColor = "#FFC0C0C0";
                break;
            case GOLD:
                levelColor = "#FFFFD700";
                break;
            case PLATINUM:
                levelColor = "#FFA6C6EE";
                break;
            case LUMO:
                levelColor = "#FF000000";
                break;
            default:
                levelColor = "#CCCCCC";
        }
        ((GradientDrawable)dialogView.findViewById(R.id.plane_info_table).getBackground()).setStroke(10, Color.parseColor(levelColor));
    }




    private void setContent(){
        // This method should be private and only called in onCreateView() after the view has been inflated
        // By the time this is called, all private data must be available
        this.nameTextView.setText(this.planeName);

        this.countriesTextView.setText(locations);

        this.planeImageView.setImageResource(this.planeImage);

        this.planeImageView.setMaxHeight(120);
        this.planeImageView.setMaxWidth(120);

        setBorderColor(this.view);

        this.upper.setText(this.upperButtonText);
        this.lower.setText(this.lowerButtonText);
        this.upper.setVisibility(this.upperButtonVisibility);
        this.lower.setVisibility(this.lowerButtonVisibility);


    }

}
