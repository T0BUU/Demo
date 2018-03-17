package com.finnair.gamifiedpartnermap;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Otto on 14.2.2018.
 * Upper level adapter class for partner popup window, this sets group and child views.
 * Group contains title TextView which is field_of_business, and checkbox for filtering those on map.
 * Child contains only name of partner TextView.
 */

public class PartnerPopupExpandableListAdapter extends BaseExpandableListAdapter {

    private Activity mActivity;
    private List<String> expandingTitles;                         //List for group names (field_of_business) strings.
    private HashMap<String, List<String>> partnersUnderBusiness;  //HashMap which has group names as keys and company names as values.
    private boolean[] checkBoxStates;                             //Boolean array to hold groupCheckBoxStates.

    private PartnerMarkerClass partnerMarkerClass;
    private MapsFragment mapFragment;

    public PartnerPopupExpandableListAdapter(Activity a, HashMap<String, List<String>> partnersMap,
                                             PartnerMarkerClass pMC, MapsFragment mapF) {
        this.mActivity = a;
        this.partnersUnderBusiness = partnersMap;
        this.mapFragment = mapF;
        this.expandingTitles = new ArrayList<>(partnersUnderBusiness.keySet());
        partnerMarkerClass = pMC;
        this.initCheckBoxStates();

    }

    //Initialize checkboxes; init new ArrayList, then add value true (checked) for each group.
    private void initCheckBoxStates(){
        checkBoxStates = new boolean[expandingTitles.size()];
        Arrays.fill(checkBoxStates, Boolean.TRUE);
    }

    @Override
    public int getChildrenCount(int pos) {
        return this.partnersUnderBusiness.get(expandingTitles.get(pos)).size();
    }

    @Override
    public long getChildId(int listPos, int expListPos) {
        return expListPos;
    }

    @Override
    public Object getChild(int position, int expandedPosition){
        return partnersUnderBusiness.get(this.expandingTitles.get(position)).get(expandedPosition);
    }

    //Set new secondleveladapter for children view.
    @Override
    public View getChildView(final int listPos, final int expListPos,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final String childText = (String) getChild(listPos, expListPos);
        if(convertView == null) {
            LayoutInflater inf = mActivity.getLayoutInflater();
            convertView = inf.inflate(R.layout.popup_expanding_list_child_view, null);
        }
        TextView expListTextView = (TextView) convertView.findViewById(R.id.expanding_list_item);
        expListTextView.setText(childText);
        expListTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                String clickedChild = partnersUnderBusiness.get(expandingTitles.get(listPos)).get(expListPos);
                Partner p = partnerMarkerClass.getPartnerByID(clickedChild);
                mapFragment.moveCameraToPartner(p);
            }
        });
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int listPos, int expListPos){
        return true;
    }

    @Override
    public int getGroupCount(){
        return expandingTitles.size();
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public Object getGroup(int i) {
        return expandingTitles.get(i);
    }

    @Override
    public View getGroupView(final int listPos, final boolean isExpanded,
                             View convertView, ViewGroup parent) {
        final String expandingListTitle = (String) getGroup(listPos);
        GroupViewHolder gHolder = new GroupViewHolder();
        if(convertView == null) {
            LayoutInflater inf = mActivity.getLayoutInflater();
            convertView = inf.inflate(R.layout.popup_expanding_list_group_view, null);
            gHolder.title = (TextView)convertView.findViewById(R.id.expanding_group_title);    //Add group title TextView to GroupViewHolder.
            gHolder.cBox = (CheckBox)convertView.findViewById(R.id.expanding_group_checkbox);  //Add checkbox to GroupViewHolder
            convertView.setTag(gHolder);
        } else {
            gHolder = (GroupViewHolder)convertView.getTag();
        }
        final GroupViewHolder holder = gHolder;
        holder.title.setText(expandingListTitle);

        holder.cBox.setTag(listPos);
        final int finalPos = (Integer)listPos;
        final ExpandableListView expLV = (ExpandableListView)parent;
        holder.cBox.setChecked(checkBoxStates[listPos]);

        holder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!expLV.isGroupExpanded(finalPos)) {
                    expLV.expandGroup(finalPos);
                } else {
                    expLV.collapseGroup(finalPos);
                }
            }
        });
        holder.cBox.setFocusable(false);
        holder.cBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkBoxStates[listPos]){
                    List<Partner> partnersToFilter = partnerMarkerClass.filterFieldOfBusiness(expandingTitles.get(listPos));
                    mapFragment.filterPartners(partnersToFilter);
                    holder.cBox.setChecked(false);
                    checkBoxStates[listPos] = false;
                } else {
                    List<Partner> partnersToFilter = partnerMarkerClass.filterFieldOfBusiness(expandingTitles.get(listPos));
                    mapFragment.filterPartners(partnersToFilter);
                    holder.cBox.setChecked(true);
                    checkBoxStates[listPos] = true;
                }
            }
        });
        return convertView;
    }

    @Override
    public boolean hasStableIds(){
        return true;
    }

    class GroupViewHolder {
        public CheckBox cBox;
        public TextView title;
    }

}
