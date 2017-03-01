package freerunningapps.veggietizer.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import freerunningapps.veggietizer.R;
import freerunningapps.veggietizer.controller.FontManager;
import freerunningapps.veggietizer.controller.adapter.DetailsInfoAdapter;
import freerunningapps.veggietizer.model.Achievement;
import freerunningapps.veggietizer.model.enums.Category;
import freerunningapps.veggietizer.model.util.Formatter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Class to show a popup from everywhere.
 * Uses static methods to show popups.
 *
 * @author Lukas Gebhard <freerunningapps@gmail.com>, Matthias Heim <freerunningapps@gmail.com>
 */
public final class Popup {

	/**
     * Prevents from instantiating this class.
     */
	private Popup() {}
	
	/**
	 * Shows a popup after inserting a new meat dish. 
     * The popup gives instant feedback about how much impact the inserted meat dish has.
     * 
	 * @param carbonSaved The amount of CO2 saved.
	 * @param waterSaved The amount of water saved.
	 * @param feedSaved The amount of feed saved.
	 * @param context The application context.
	 * @param onClickListener To be executed when submitting the popup (for example, if another dialog
	 *     is to be shown after this one, it could be invoked by this listener). 
	 *     Can be <code>null</code>, then the dialog just closes.
     * @return The popup dialog.
	 */
    public static AlertDialog showFeedbackOnInsert(float meatSaved, float carbonSaved, float waterSaved, float feedSaved,
                                            Context context,
            DialogInterface.OnClickListener onClickListener) {
        Resources resources = context.getResources();
        String grammes = resources.getString(R.string.unitGrammes);
        String kilogrammes = resources.getString(R.string.unitKilogrammes);
        String litres = resources.getString(R.string.unitLitres);
        String millilitres = resources.getString(R.string.unitMillilitres);
        int numberOfDecimals = 2;

    	// Construct the popup
    	View popupLayout;
    	AlertDialog.Builder popupBuilder = new AlertDialog.Builder(context);

        // Set layout for popup
        LayoutInflater inflater = LayoutInflater.from(context);

        // Custom Title
        TextView customTitle = (TextView) inflater.inflate(R.layout.popup_title, null);
        customTitle.setText(R.string.instant_feedback_popup_title);

    	String meatSavedFormatted, carbonSavedFormatted, waterSavedFormatted, feedSavedFormatted;

        // Format impact values
    	meatSavedFormatted = Formatter.format(meatSaved, grammes, kilogrammes, Formatter.KILO, numberOfDecimals);
        carbonSavedFormatted = Formatter.format(carbonSaved, grammes, kilogrammes, Formatter.KILO, numberOfDecimals);

    	// If meat other than fish was selected, also add water and feed savings
    	if (waterSaved != 0 && feedSaved != 0) {
    		popupLayout = inflater.inflate(R.layout.popup_instant_feedback, null);

    		// Format impact values
            waterSavedFormatted = Formatter.format(waterSaved, millilitres, litres, Formatter.KILO, 0);
            feedSavedFormatted = Formatter.format(feedSaved, grammes, kilogrammes, Formatter.KILO, numberOfDecimals);

        	// Set saved values
		    TextView waterText = ((TextView) popupLayout.findViewById(R.id.instant_feedback_popup_water));
            TextView feedText = ((TextView) popupLayout.findViewById(R.id.instant_feedback_popup_feed));

		    waterText.setText(waterSavedFormatted);
            feedText.setText(feedSavedFormatted);

            Utility.setFont(FontManager.Font.ROBOTO_LIGHT, new TextView[]{
                    waterText, feedText
            });
    	}
    	else {
    		popupLayout = inflater.inflate(R.layout.popup_instant_feedback_fish, null);
        }

        // Set saved values
        TextView meatText = ((TextView) popupLayout.findViewById(R.id.instant_feedback_popup_meat));
        TextView carbonText = ((TextView) popupLayout.findViewById(R.id.instant_feedback_popup_carbon));

        meatText.setText(meatSavedFormatted);
        carbonText.setText(carbonSavedFormatted);

        Utility.setFont(FontManager.Font.ROBOTO_LIGHT, new TextView[] {
                customTitle, meatText, carbonText
        });
    	
    	popupBuilder.setView(popupLayout)
    				.setCustomTitle(customTitle)
                    .setNeutralButton(R.string.ok, onClickListener);

        AlertDialog dialog = popupBuilder.create();
        dialog.show();

        return dialog;
    }

	/**
	 * Shows a popup window.
     *
	 * @param title The title of the popup.
	 * @param description The custom description.
	 * @param context The context to show the popup in.
     * @return The popup dialog.
	 */
	public static AlertDialog show(String title, String description, Context context,
                            DialogInterface.OnClickListener onSubmitListener) {
        // Custom Title
	    TextView customTitle = (TextView) LayoutInflater.from(context).inflate(R.layout.popup_title, null);
	    customTitle.setText(title);

		AlertDialog.Builder popupBuilder = new AlertDialog.Builder(context);
		popupBuilder.setMessage(description)
		            .setNeutralButton(R.string.ok, onSubmitListener)
		            .setCustomTitle(customTitle);

		AlertDialog dialog = popupBuilder.create();
		dialog.show();

        TextView popupMessage = (TextView) dialog.findViewById(android.R.id.message);
        popupMessage.setTextColor(context.getResources().getColor(R.color.text_gray));

        Utility.setFont(FontManager.Font.ROBOTO_LIGHT, new TextView[]{
                customTitle, popupMessage
        });

        return dialog;
	}

    /**
     * Shows the popup that is opened when the user clicks on the info action button of a details page.
     * An {@link ExpandableListView} is used as a layout.
     *
     * @param context The context to show the popup in.
     * @param category The category to inform about.
     * @return The dialog.
     */
    public static AlertDialog showDetailsInfo(Context context, Category category,
                                       DialogInterface.OnClickListener onSubmitListener) {
        Resources res = context.getResources();
        AlertDialog dialog;

        switch (category) {
            case CO2:
                dialog = Popup.showDetailsInfo(context, res.getString(R.string.details_info_co2_title),
                        res.getStringArray(R.array.details_info_co2_groups),
                        res.getStringArray(R.array.details_info_co2_entries),
                        onSubmitListener);
                break;
            case WATER:
                dialog = Popup.showDetailsInfo(context, res.getString(R.string.details_info_water_title),
                        res.getStringArray(R.array.details_info_water_groups),
                        res.getStringArray(R.array.details_info_water_entries),
                        onSubmitListener);
                break;
            case FEED:
                dialog = Popup.showDetailsInfo(context, res.getString(R.string.details_info_feed_title),
                        res.getStringArray(R.array.details_info_feed_groups),
                        res.getStringArray(R.array.details_info_feed_entries),
                        onSubmitListener);
                break;
            case MEAT:
                dialog = Popup.showDetailsInfo(context, res.getString(R.string.details_info_meat_title),
                        res.getStringArray(R.array.details_info_meat_groups),
                        res.getStringArray(R.array.details_info_meat_entries),
                        onSubmitListener);
                break;
            default:
                throw new IllegalStateException("Unsupported category " + category);
        }

        return dialog;
    }

    /**
     * Shows the popup that is opened when the user clicks on the info action button of a details page.
     * An {@link ExpandableListView} is used as a layout.
     *
     * @param context The context to show the popup in.
     * @param title The popup's title.
     * @param groups The expandable categories.
     * @param entries Each category in <code>groups</code> shows one entry when expanded.
     * @return The dialog.
     */
    private static AlertDialog showDetailsInfo(Context context, String title,
                                               String[] groups, String[] entries,
                                               DialogInterface.OnClickListener onSubmitListener) {
        if (groups.length != entries.length) {
            throw new IllegalArgumentException();
        }

        // Custom title
        TextView customTitle = (TextView) LayoutInflater.from(context).inflate(R.layout.popup_title, null);
        customTitle.setText(title);

        View popupDetailsInfo = LayoutInflater.from(context)
                .inflate(R.layout.popup_details_info, null);
        ExpandableListView listViewDetailsInfo = (ExpandableListView)
                popupDetailsInfo.findViewById(R.id.expandablelistview_popup_details_info);
        ExpandableListAdapter adapter = createDetailsInfoAdapter(context, groups, entries);
        listViewDetailsInfo.setAdapter(adapter);

        AlertDialog.Builder popupBuilder = new AlertDialog.Builder(context);
        popupBuilder.setView(popupDetailsInfo)
                .setNeutralButton(R.string.ok, onSubmitListener)
                .setCustomTitle(customTitle);

        AlertDialog dialog = popupBuilder.create();
        dialog.show();

        Utility.setFont(FontManager.Font.ROBOTO_LIGHT, new TextView[]{
                customTitle
        });

        return dialog;
    }

    private static ExpandableListAdapter createDetailsInfoAdapter(Context context, String[] groups, String[] entries) {
        // Creates the upper list level
        List<Map<String, String>> groupData = new LinkedList<>();
        String groupName = "GroupName";
        String[] groupFrom = new String[] {groupName};
        int[] groupTo = new int[] {R.id.component_details_info_group};

        for (String g : groups) {
            Map<String, String> group = new HashMap<>(1);
            group.put(groupName, g);
            groupData.add(group);
        }

        // Creates the lower list level
        List<List<Map<String, String>>> childData = new LinkedList<>();
        String groupContent = "Content";
        String[] childFrom = new String[] {groupContent};
        int[] childTo = new int[] {R.id.component_details_info_entry};

        for (String e : entries) {
            List<Map<String, String>> content = new LinkedList<>();
            Map<String, String> entry = new HashMap<>(1);

            entry.put(groupContent, e);
            content.add(entry);
            childData.add(content);
        }

        return new DetailsInfoAdapter(context, groupData,
                groupFrom, groupTo, childData, childFrom, childTo);
    }

    private static AlertDialog showAchievement(Achievement achievement, Context context,
                                        DialogInterface.OnClickListener onSubmitListener,
                                        String heading, String shareText,
                                        String requirementsHeading, String requirements) {
        AlertDialog.Builder popupBuilder = new AlertDialog.Builder(context);

        // Set layout for popup
        LayoutInflater inflater = LayoutInflater.from(context);
        View popupLayout = inflater.inflate(R.layout.popup_achievements, null);

        // Custom Title
        TextView customTitle = (TextView) inflater.inflate(R.layout.popup_title, null);
        customTitle.setText(heading);
        popupBuilder.setView(popupLayout);

        // Set requirements
        TextView requirementHeading = (TextView)
                popupLayout.findViewById(R.id.achievements_popup_requirements_heading);
        requirementHeading.setText(requirementsHeading);
        TextView req = ((TextView) popupLayout.findViewById(R.id.achievements_popup_requirements));
        req.setText(requirements);

        // Set description
        TextView des = ((TextView) popupLayout.findViewById(R.id.achievements_popup_description));
        des.setText(achievement.getDescription());

        Utility.setFont(FontManager.Font.ROBOTO_LIGHT, new TextView[]{
                customTitle, req, des
        });

        // Set icon
        ((ImageView) popupLayout.findViewById(R.id.achievements_popup_image))
                .setImageResource(achievement.getIconUnlockedID());

        popupBuilder.setCustomTitle(customTitle)
                .setNeutralButton(R.string.ok, onSubmitListener)
                .setPositiveButton(context.getResources().getString(R.string.popup_share_achievement),
                        new ShareAchievementListener(context, shareText
                                + "\n\n" + context.getResources().getString(R.string.app_playstore_url)));

        AlertDialog dialog = popupBuilder.create();
        dialog.show();

        return dialog;
    }

	/**
	 * Shows an already unlocked achievement listed in the achievement activity.
     *
	 * @param achievement The unlocked achievement to show.
	 * @param context The application context.
     * @return  The achievement.
	 */
	public static AlertDialog showAchievement(Achievement achievement, Context context,
                                       DialogInterface.OnClickListener onSubmitListener) {
		return showAchievement(achievement, context, onSubmitListener, achievement.getHeading(),
                achievement.getShareText(),
                context.getResources().getString(R.string.achievement_requirements_heading_unlocked),
                achievement.getRequirements());
	}

	/**
     * Shows the just unlocked <code>achievement</code> as a popup dialog.
     *
     * @param achievement The achievement to show.
     * @param context The context in which to open the achievement in.
     * @return The achievement as a popup dialog.
     */
    public static AlertDialog showNewAchievement(Context context, Achievement achievement,
                                          DialogInterface.OnClickListener onSubmitListener) {
            String heading = context.getResources().getString(R.string.achievement_unlocked) + "\n" +
                    achievement.getHeading();
            String requirementsHeading = context.getResources().getString(
                    R.string.achievement_requirements_heading_unlocked);
            String requirements = achievement.getRequirements();
            String shareText = achievement.getShareText();

            return showAchievement(achievement, context, onSubmitListener, heading, shareText, requirementsHeading,
                    requirements);
    }

    /**
     * A listener to be invoked when the user wants to share an achievement.
     */
    public static class ShareAchievementListener implements DialogInterface.OnClickListener {
        private Context context;
        private String shareText;

        public ShareAchievementListener(Context context, String shareText) {
            this.context = context;
            this.shareText = shareText;
        }

        @Override
        public void onClick(DialogInterface dialogInterface, int buttonType) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);

            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

            context.startActivity(Intent.createChooser(shareIntent, context.getResources().getString(R.string
                    .popup_share_achievement)));
        }
    }
}
