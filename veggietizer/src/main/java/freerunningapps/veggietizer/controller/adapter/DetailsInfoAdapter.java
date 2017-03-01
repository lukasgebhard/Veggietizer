package freerunningapps.veggietizer.controller.adapter;

import android.widget.CheckedTextView;
import android.widget.SimpleExpandableListAdapter;
import freerunningapps.veggietizer.R;
import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import freerunningapps.veggietizer.controller.FontManager;

import java.util.List;
import java.util.Map;

/**
 * Fills the {@link android.widget.ExpandableListView} in the info popups of the details pages.
 *
 * @author Lukas Gebhard <freerunningapps@gmail.com>
 *
 */
public class DetailsInfoAdapter extends SimpleExpandableListAdapter {
    private Context context;

    public DetailsInfoAdapter(Context context, List<? extends Map<String, ?>> groupData,
                              String[] groupFrom, int[] groupTo,
                              List<? extends List<? extends Map<String, ?>>> childData,
                              String[] childFrom, int[] childTo) {
        super(context, groupData, R.layout.component_details_info_group, groupFrom, groupTo, childData,
                R.layout.component_details_info_entry, childFrom, childTo);

        this.context = context;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {
        View childView = super.getChildView(groupPosition, childPosition, isLastChild, convertView, parent);
        Typeface font = FontManager.getInstance().getFont(FontManager.Font.ROBOTO_LIGHT);
        TextView detailsInfoEntry = (TextView) childView.findViewById(R.id.component_details_info_entry);

        detailsInfoEntry.setTypeface(font);

        return childView;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View groupView = super.getGroupView(groupPosition, isExpanded, convertView, parent);
        Typeface font = FontManager.getInstance().getFont(FontManager.Font.ROBOTO_LIGHT);
        CheckedTextView checkedTextView = (CheckedTextView) groupView.findViewById(R.id.component_details_info_group);

        checkedTextView.setTypeface(font);
        checkedTextView.setTextAppearance(context, R.style.ListGroupText);

        return groupView;
    }
}
