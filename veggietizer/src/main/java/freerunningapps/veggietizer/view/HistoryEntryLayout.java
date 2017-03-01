package freerunningapps.veggietizer.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.LinearLayout;
import freerunningapps.veggietizer.R;

/**
 * A custom view encapsulating the <code>component_history_entry</code> layout and making it checkable.
 * <p />
 * A history entry needs to be checkable so that the user can delete it.
 *
 * @author Lukas Gebhard <freerunningapps@gmail.com>
 */
@SuppressWarnings("WeakerAccess")
public class HistoryEntryLayout extends LinearLayout implements Checkable {
    private boolean isChecked;

    public HistoryEntryLayout(Context context) {
        super(context);
    }

    public HistoryEntryLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HistoryEntryLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;

        setBackgroundResource(isChecked ? R.color.veggie_green : R.drawable.history_entry_background);

    }

    @Override
    public boolean isChecked() {
        return isChecked;
    }

    @Override
    public void toggle() {
        setChecked(!isChecked);
    }
}
