package freerunningapps.veggietizer.controller.adapter;

import freerunningapps.veggietizer.R;
import freerunningapps.veggietizer.controller.FontManager;
import freerunningapps.veggietizer.model.Achievement;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AchievementAdapter extends ArrayAdapter<Achievement> {

	private Context context;
	private int layoutResourceId;
	private Achievement[] achievements = null;

	public AchievementAdapter(Context context, Achievement[] achievements) {
		super(context, R.layout.component_achievement_entry, achievements);
		
		this.context = context;
		this.layoutResourceId = R.layout.component_achievement_entry;
		this.achievements = achievements;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;

        if(row == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
        }

        Typeface font = FontManager.getInstance().getFont(FontManager.Font.ROBOTO_LIGHT);
        TextView title = (TextView) row.findViewById(R.id.achievement_title);
        title.setTypeface(font);
        ImageView icon = (ImageView) row.findViewById(R.id.achievement_icon);

        title.setText(achievements[position].getHeading());

        if(achievements[position].checkAchievement()) {
            icon.setImageResource(achievements[position].getIconUnlockedID());
            title.setTextColor(Color.BLACK);
        }
    	else {
            icon.setImageResource(achievements[position].getIconLockedID());
            title.setTextColor(context.getResources().getColor(R.color.text_gray));
        }

        return row;
	}
}
