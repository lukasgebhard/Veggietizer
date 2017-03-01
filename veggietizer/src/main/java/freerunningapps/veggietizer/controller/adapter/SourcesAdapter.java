package freerunningapps.veggietizer.controller.adapter;

import android.widget.CheckedTextView;
import android.widget.SimpleExpandableListAdapter;
import freerunningapps.veggietizer.R;
import freerunningapps.veggietizer.controller.FontManager;
import freerunningapps.veggietizer.view.activity.AboutActivity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

/**
 * Fills the sources list view in the {@link AboutActivity}.
 *
 * @author Lukas Gebhard <freerunningapps@gmail.com>
 *
 */
public class SourcesAdapter extends SimpleExpandableListAdapter {

    public SourcesAdapter(Context context, List<? extends Map<String, ?>> groupData,
                          String[] groupFrom, int[] groupTo, List<? extends List<? extends Map<String, ?>>> childData,
                          String[] childFrom, int[] childTo) {
        super(context, groupData, R.layout.component_source_group, groupFrom, groupTo, childData,
                R.layout.component_source_entry, childFrom, childTo);
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {
        View childView = super.getChildView(groupPosition, childPosition, isLastChild, convertView, parent);
        Typeface font = FontManager.getInstance().getFont(FontManager.Font.ROBOTO_LIGHT);
        TextView urlView = (TextView) childView.findViewById(R.id.textView_source_url);
        TextView dateView = (TextView) childView.findViewById(R.id.textView_source_date);

        urlView.setTypeface(font);
        dateView.setTypeface(font);

        return childView;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View groupView = super.getGroupView(groupPosition, isExpanded, convertView, parent);
        Typeface font = FontManager.getInstance().getFont(FontManager.Font.ROBOTO_LIGHT);
        CheckedTextView checkedTextView = (CheckedTextView) groupView.findViewById(R.id.component_source_group);

        checkedTextView.setTypeface(font);

        return groupView;
    }
}
