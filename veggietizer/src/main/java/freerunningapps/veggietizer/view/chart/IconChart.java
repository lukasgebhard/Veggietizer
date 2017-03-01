package freerunningapps.veggietizer.view.chart;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import freerunningapps.veggietizer.R;
import freerunningapps.veggietizer.controller.FontManager;
import freerunningapps.veggietizer.model.util.Formatter;

/**
 * Custom view that shows an icon chart.
 *
 * @author Matthias Heim <freerunningapps@gmail.com>
 */
public class IconChart extends View {

    /**
     * The paint for the text to be shown above the Chart
     */
    private Paint textPaint;
    /**
     * The Icon to draw for one weight unit
     */
    private Bitmap icon;
    /**
     * The Icon to draw for one weight unit scaled
     */
    private Bitmap iconScaled;
    /**
     * Percentage of the width of the view to be filled by the Icon Chart
     */
    private float chartWidthPercentage;
    /**
     * Amount of Meat / CO2 / etc to be displayed in this Icon Chart in gramm
     */
    private float amount;
    /**
     * Amount of Meat / CO2 / etc to be represented by one icon in this Icon Chart in gramm
     */
    private float iconValue;
    /**
     * The margin between the text and the Chart
     */
    private float textChartMargin;
    /**
     * The size of the text
     */
    private float textSize;
    /**
     * The size of the icons
     */
    private float iconSize;
    /**
     * Margin between the icons
     */
    private float iconMinMargin;
    /**
     * The Title to show above the bar chart
     */
    private String sChartTitle;
    /**
     * The Value to show next to the bar chart Title
     */
    private String sChartTitleValue;
    /**
     * Duration for the fade in animation in milliseconds
     */
    private int animDuration;
    /**
     * Value Animator for fade in effect for bar chart
     */
    private ValueAnimator fadeInAnimation;
    /**
     * Index for the Icon to show in the Chart, choosable from the layout xml
     */
    private int iconImg;
    /**
     * An icon displayed on the top right to indicate that this view is clickable.
     */
    private Bitmap forwardIcon;
    /**
     * Internal helping variables for calculating paddings and margins within the view
     */
    private float textPaddingTop;
    private float numberOfIcons;
    private int iconsPerColumn;
    private float paddingLeft;
    private float chartPaddingTop;
    private double numberOfColumns;
    private float iconMargin;
    private float animValue;
    private Matrix iconTransform;
	private long startDelay;
	private boolean animateFromCenter;
	private Context context;

    /**
     * Class constructor taking only a context. Use this constructor to create
     * {@link IconChart} objects from your own code.
     *
     * @param context The application context.
     */
    public IconChart(Context context) {
        super(context);
        init();
    }

    /**
     * Class constructor taking a context and an attribute set. This constructor
     * is used by the layout engine to construct a {@link IconChart} from a set of
     * XML attributes.
     *
     * @param context The application context.
     * @param attrs An attribute set.
     */
    public IconChart(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.IconChart, 0, 0);

        try {
                chartWidthPercentage = a.getFloat(R.styleable.IconChart_chartWidthPercentage, 85);
                iconValue = a.getFloat(R.styleable.IconChart_iconValue, 100);
                textChartMargin = a.getDimension(R.styleable.IconChart_textChartMargin, 20);
                textSize = a.getDimension(R.styleable.IconChart_headingSize, 30);
                iconSize = a.getDimension(R.styleable.IconChart_iconSize, 30);
                iconMinMargin = a.getDimension(R.styleable.IconChart_iconMargin, 10);
                animDuration = a.getInteger(R.styleable.IconChart_animationDuration, 100);
                sChartTitle = a.getString(R.styleable.IconChart_heading);
                iconImg = a.getInteger(R.styleable.IconChart_iconImg, 0);
                startDelay = a.getInteger(R.styleable.IconChart_startDelayIconChart, 300);
                animateFromCenter = a.getBoolean(R.styleable.IconChart_animateFromCenter, false);

        } finally {
            // release the TypedArray so that it can be reused.
            a.recycle();
        }
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //Text
        canvas.drawText(sChartTitle + "  " + sChartTitleValue, paddingLeft, textPaddingTop, textPaint);

        // 'Forward' icon
        if (isClickable()) {
            float posX = getWidth() - paddingLeft - forwardIcon.getWidth();
            float posY = getPaddingTop();

            canvas.drawBitmap(forwardIcon, posX, posY, null);
        }

        int iconCount = 0;

        // Rows
        for (int i = 0; iconCount <= animValue; i++) {
            iconTransform.preTranslate(paddingLeft, chartPaddingTop + i * (iconSize + iconMargin));

            // Columns
            for (int k = 0; k < iconsPerColumn && iconCount <= animValue; k++) {
                if( animValue - iconCount < 1 ) {
                	if(animateFromCenter)
                		iconTransform.preScale(animValue - iconCount,
                                animValue - iconCount, iconSize / 2, iconSize / 2 );
                	else
                		iconTransform.preScale(animValue - iconCount, animValue - iconCount, iconSize / 2, iconSize );
                }

                canvas.drawBitmap(iconScaled, iconTransform, null);
                iconTransform.preTranslate(iconSize + iconMargin, 0);
                iconCount++;
            }
            iconTransform.reset();
        }
    }

    @SuppressLint("DrawAllocation")
	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int viewWidth = View.MeasureSpec.getSize(widthMeasureSpec);

        // Calculate margins and paddings
        textPaddingTop = getPaddingTop() + textPaint.getTextSize();
        float chartFullWidth = viewWidth * (chartWidthPercentage / 100);
        paddingLeft = ( viewWidth - chartFullWidth) / 2;
        chartPaddingTop = textPaddingTop + textChartMargin;
        calculateNumberOfIcons();
        iconsPerColumn = Math.round(chartFullWidth / ( iconSize + iconMinMargin ));
        float rightGap = chartFullWidth - iconsPerColumn * (iconSize + iconMinMargin);

        // Does another icon fit in the gap on the right side?
        if(rightGap >= iconSize) {
            iconsPerColumn ++;
        }

        iconMargin = ( chartFullWidth - ( iconsPerColumn * iconSize ) ) / ( iconsPerColumn - 1 );
        numberOfColumns = Math.ceil(numberOfIcons / iconsPerColumn);

        //Scaled Icon
        iconScaled = Bitmap.createScaledBitmap(icon, Math.round(iconSize), Math.round(iconSize), false);

        int viewHeight = calulateHeight();

        //Size of the view
        setMeasuredDimension(viewWidth, viewHeight);
    }

    private int calulateHeight() {
    	//value for padding between icons and bottom border
        int paddingToBorder = context.getResources().getDimensionPixelSize(R.dimen.paddingToBorder);
    	return (int) Math.round( numberOfColumns * ( iconSize + iconMargin ) - iconMargin + chartPaddingTop +
                getPaddingBottom() + paddingToBorder );
    }

	/**
	 * Calculate the Icon count
	 */
	private void calculateNumberOfIcons() {
		numberOfIcons = amount / iconValue;
		//For Animation
        fadeInAnimation.setFloatValues(0, numberOfIcons);
	}
    /**
     * Initialize the control. This code is in a separate method so that it can be
     * called from both constructors.
     */
    private void init() {
        Typeface font = FontManager.getInstance().cacheFont(context, FontManager.Font.ROBOTO_LIGHT)
            .getFont(FontManager.Font.ROBOTO_LIGHT);

        // Set up the Text Paint of the Chart
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(textSize);
        textPaint.setTypeface(font);
        // Set standard text for chart heading
        if(sChartTitle == null)
        	sChartTitle = "Set Icon Chart Title";
        sChartTitleValue = "";

        // Get the icon
        switch(iconImg) {
	        case 1:
	        	icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_meat);
	        	break;
	        case 2:
	        	icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_co2);
	        	break;
	        case 3:
	        	icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_water);
	        	break;
	        case 4:
	        	icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_feed);
	        	break;
	        case 5:
	        	icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_cow);
	        	break;
	        case 6:
	        	icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_pig);
	        	break;
	        case 7:
	        	icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_rooster);
	        	break;
	        case 8:
	        	icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_sheep);
	        	break;
	        case 9:
	        	icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_fish);
	        	break;
	        default:
	        	throw new IllegalStateException("Invalid icon specified.");
        }

        //Transformation Matrix
        iconTransform = new Matrix();

        // Set up Animation System
        fadeInAnimation = ValueAnimator.ofFloat(0, 0);
        fadeInAnimation.setDuration(0);
        fadeInAnimation.setInterpolator(new LinearInterpolator());
        fadeInAnimation.setStartDelay(startDelay);

        // Set Width of bar for every frame
        fadeInAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                animValue = (Float) animation.getAnimatedValue();
                invalidate();
            }
        });
        // Set width to full percentage on end event
        fadeInAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animValue = numberOfIcons;
                invalidate();
            }
        });

        //Set Start amount
        amount = 0;

        forwardIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_action_forward);
    }

    /**
     * Sets the amount next to the title of the icon chart indicating.
     *
     * @param amount The amount.
     * @param lowerUnit The unit for low values.
     * @param greaterUnit The unit for large values.
     * @param conversionFactor The ratio <code>greaterUnit / lowerUnit</code>.
     * @param accuracy The accuracy to use for the value.
     *                 This corresponds to the number of decimals if the value is represented
     *                 with respect to <code>greaterUnit</code>.
     * @see Formatter#format(float, String, String, float, int)
     */
    @SuppressWarnings("SameParameterValue")
    public void setAmount(float amount, String lowerUnit, String greaterUnit, float conversionFactor,
                          int accuracy)
    {
        this.amount = amount;
        calculateNumberOfIcons();
        //Get String for heading
        sChartTitleValue = Formatter.format(amount, lowerUnit,
                greaterUnit, conversionFactor, accuracy);
    }
    /**
     * The Value of one icon in gramm
     * @param iconValue the iconValue to set
     */
    @SuppressWarnings("unused")
    public void setIconValue(float iconValue)
    {
        this.iconValue = iconValue;
    }

    /**
     * Sets the title for the chart
     * @param sChartTitle the sChartTitle to set
     */
    @SuppressWarnings("unused")
    public void setChartTitle(String sChartTitle)
    {
        this.sChartTitle = sChartTitle;
    }

    /**
     * Starts the fade in animation
     */
    public void animateIcons()
    {
    	fadeInAnimation.setDuration((long) (animDuration * numberOfIcons));
        fadeInAnimation.start();
    }

    /**
     * Refreshes the Icon Chart
     */
    public void refreshView() {
    	requestLayout();
    }
}


