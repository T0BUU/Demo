package com.finnair.gamifiedpartnermap;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by huzla on 29.3.2018.
 */

public class RewardFragment extends DialogFragment {

    private TextView rewardType;
    private TextView description;
    private ImageView imageView;
    private Button redeemButton;

    private String rewardTypeText;
    private String descriptionText;
    private int image;
    private String redeemText;
    private int plusPoints;
    private boolean isLoggedIn;
    private TextView parent;

    public RewardFragment(){}

    public void setAllFragmentData(String rewardInfo, String rewardType, int image, int plusPoints, boolean isLoggedIn, View parent) {

        descriptionText = rewardInfo;
        rewardTypeText = String.format("You have received:\n%s", rewardType);
        this.isLoggedIn = isLoggedIn;
        this.parent = (TextView) parent;

        if (image != -1) this.image = image;
        else this.image = R.drawable.finnair_logo;

        this.plusPoints = plusPoints;

    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.reward_pop_up_layout, null);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(dialogView);

        // Get references to TextViews. Method setContents() places information into the TextViews
        rewardType = dialogView.findViewById(R.id.reward_type_text);
        description = dialogView.findViewById(R.id.reward_description);
        imageView = dialogView.findViewById(R.id.reward_related_image);
        redeemButton = dialogView.findViewById(R.id.reward_redeem_button);

        this.setContent();
        // Create the AlertDialog object and return it
        Dialog result = builder.create();

        return result;
    }

    private void setContent(){
        // This method should be private and only called in onCreateView() after the view has been inflated
        // By the time this is called, all private data must be available


        if (!isLoggedIn) {
            redeemButton.setText(R.string.reward_redeem_not_logged_in);
           redeemButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.DARKEN);

            description.setVisibility(View.INVISIBLE);
            rewardType.setVisibility(View.INVISIBLE);

            redeemButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });
        }
        else {
            description.setText(Html.fromHtml(descriptionText));
            rewardType.setText(rewardTypeText);
            imageView.setImageResource(image);
            redeemButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((PlaneCollectionActivity) getActivity()).redeemReward(parent);
                    dismiss();
                }
            });
        }


    }
}
