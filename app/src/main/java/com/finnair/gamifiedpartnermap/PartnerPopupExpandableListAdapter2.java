package com.finnair.gamifiedpartnermap;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Otto on 13.3.2018.
 */
/*
 * !!! this class may not be needed in the end !!!
 * This class is inner level adapter for partner popup window.
 * Groups are name of partner companies.
 * Child views contain some information like address and also button to locate partner on map.
 */
public class PartnerPopupExpandableListAdapter2 extends BaseExpandableListAdapter {

    private Activity activity;
    private List<String> partnerNames;
    private HashMap<String, String> partnersAndAddresses;
    private PartnerMarkerClass partnerMarkerClass;

    public PartnerPopupExpandableListAdapter2(Activity act, PartnerMarkerClass pMC){
        this.activity = act;
        partnerMarkerClass = pMC;
        partnersAndAddresses = new HashMap<>();
        partnerNames = new ArrayList<>();
        this.setData(partnerMarkerClass);
    }

    private void setData(PartnerMarkerClass pmc){
        for(Partner p : pmc.getPartnerHashMap().values()){
            if(!partnersAndAddresses.keySet().contains(p.getTitle())){
                partnersAndAddresses.put(p.getTitle(), p.getAddress());
            }
            if(!partnerNames.contains(p.getTitle())){
                partnerNames.add(p.getTitle());
            }
        }
    }

    @Override
    public Object getChild(int groupPosition, int childPosition)
    {
        return this.partnersAndAddresses.get(partnerNames.get(groupPosition));
    }

    @Override
    public int getChildrenCount(int groupPosition)
    {
        return 1;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition)
    {
        return childPosition;
    }
    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        return convertView;
    }

    @Override
    public Object getGroup(int groupPosition)
    {
        return this.partnerNames.get(groupPosition);
    }

    @Override
    public int getGroupCount()
    {
        return this.partnerNames.size();
    }

    @Override
    public long getGroupId(int groupPosition)
    {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    class ChildViewHolder {
        TextView textView;
        ImageButton imgBtn;
    }
}


