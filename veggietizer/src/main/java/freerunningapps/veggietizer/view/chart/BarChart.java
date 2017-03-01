package freerunningapps.veggietizer.view.chart;

import android.animation.Animator;

import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.*;
import java.lang.Override;
import java.lang.String;

import freerunningapps.veggietizer.R;
import freerunningapps.veggietizer.controller.FontManager;
import freerunningapps.veggietizer.model.util.Formatter;

/**
 * Custom view that shows a bar chart.
 *
 * @author Matthias Heim <freerunningapps@gmail.com>
 */
@SuppressWarnings("ALL")
public class BarChart extends View {

    /**
     * The paint for the background of the Bar, filled to 100 Percent
     */
    private Paint barBGPaint;
    /**
     * The paint for the border of the Bar Chart
     */
    private Paint barBorderPaint;
    /**
     * The paint for the  inner Part of the Border
     */
    private Paint barInnerPaint;
    /**
     * The paint for the text to be shown above the Chart
     */
    private Paint textPaint;
    /**
     * Percentage of the width of the view to be filled by the Bar Chart
     */
    private float barWidthPercentage;
    /**
     * The margin between the text and the Chart
     */
    private float textBarMargin;
    /**
     * The size of the text
     */
    private float textSize;
    /**
     * The height of the Bar Chart
     */
    private float barChartHeight;
    /**
     * Width of the bar chart border
     */
    private float borderWidth;
    /**
     * The Title to show above the bar chart
     */
    private String sBarTitle;
    /**
     * The Value to show next to the bar chart Title
     */
    private String sBarTitleValue;
    /**
     * Percentage to which the bar is filled
     */
    private float filledPercentage;
    /**
     * Percentage of the width of the view to be filled by the Bar Chart during animation
     */
    private float filledPercentageAnim;
    /**
     * Color for the inner part of the bar
     */
    private int innerColor;
    /**
     * Color for the border of the bar
     */
    private int borderColor;
    /**
     * Value if the grey bar in the background is drawn
     */
    private boolean drawGreyBar;
    /**
     * Duration for the fade in animation in milliseconds
     */
    private int animDuration;
    /**
     * Value Animator for fade in effect for bar chart
     */
    private ValueAnimator fadeInAnimation;
    /**
     * Internal helping variables for calculating paddings and margins within the view
     */
    private float textPaddingTop;
    private float barFullWidth;
    private float paddingLeft;
    private float paddingRight;
    private float barPaddingTop;
    private float barBottom;
    @SuppressWarnings("FieldCanBeLocal")
    private float barPercentageWidth;
    private float barPercentageRight;
    private float barBorderPercentageRight;
    private float barBorderPaddingLeft;
    private float barBorderPaddingTop;
    private float barBorderBottom;
	private long startDelay;
	private Context context;
	private boolean showBorder;

    /**
     * Class constructor taking only a context. Use this constructor to create
     * {@link BarChart} objects from your own code.
     *
     * @param context The application context.
     */
    public BarChart(Context context) {
        super(context);
        init();
    }

    /**
     * Class constructor taking a context and an attribute set. This constructor
     * is used by the layout engine to construct a {@link BarChart} from a set of
     * XML attributes.
     *
     * @param context The application context.
     * @param attrs An attribute set.
     */
    public BarChart(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;

        // attrs contains the raw values for the XML attributes
        // that were specified in the layout, which don't include
        // attributes set by styles or themes, and which may have
        // unresolved references. Call obtainStyledAttributes()
        // to get the final values for each attribute.
        //
        // This call uses R.styleable.BarChart, which is an array of
        // the custom attributes that were declared in attrs.xml.
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.BarChart,
                0, 0
        );

        try {
        	barWidthPercentage = a.getFloat(R.styleable.BarChart_barWidthPercentage, 85);
        	textBarMargin = a.getDimension(R.styleable.BarChart_textBarMargin, 20);
        	textSize = a.getDimension(R.styleable.BarChart_textSize, 30);
        	barChartHeight = a.getDimension(R.styleable.BarChart_barChartHeight, 20);
        	borderWidth = a.getDimension(R.styleable.BarChart_borderWidth, 3);
        	sBarTitle = a.getString(R.styleable.BarChart_barHeading);
        	innerColor = a.getColor(R.styleable.BarChart_innerColor,
                    getResources().getColor(R.color.veggie_green));
        	borderColor = a.getColor(R.styleable.BarChart_borderColor,
                    getResources().getColor(R.color.holo_gray_light));
        	drawGreyBar = a.getBoolean(R.styleable.BarChart_greyBar, true);
        	animDuration = a.getInteger(R.styleable.BarChart_animDuration, 1500);
        	startDelay = a.getInteger(R.styleable.BarChart_startDelayBarChart, 300);
        	showBorder = a.getBoolean(R.styleable.BarChart_showBorder, false);

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
        canvas.drawText(sBarTitle + " " + sBarTitleValue, paddingLeft, textPaddingTop, textPaint);

        //Background of bar
        if(drawGreyBar)
            canvas.drawRect(paddingLeft, barPaddingTop, paddingRight, barBottom, barBGPaint);
        //The bar
        canvas.drawRect(paddingLeft, barPaddingTop, barPercentageRight, barBottom, barInnerPaint);
        //Border of the bar
        if(showBorder)
        	canvas.drawRect(barBorderPaddingLeft, barBorderPaddingTop, barBorderPercentageRight, barBorderBottom,
                    barBorderPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int viewWidth = View.MeasureSpec.getSize(widthMeasureSpec);

        //Calculate margins and paddings within the view
        textPaddingTop = getPaddingTop() + textPaint.getTextSize();
        barFullWidth = viewWidth * ( barWidthPercentage / 100 );
        paddingLeft = ( viewWidth - barFullWidth ) / 2;
        paddingRight = paddingLeft + barFullWidth;
        barPaddingTop = textPaddingTop + textBarMargin;
        barBottom = barPaddingTop + barChartHeight;
        barBorderPaddingLeft = paddingLeft + (borderWidth / 2);
        barBorderPaddingTop = barPaddingTop + (borderWidth / 2);
        barBorderBottom = barBottom - (borderWidth / 2);

        //value for padding between icons and bottom border
        int paddingToBorder = context.getResources().getDimensionPixelSize(R.dimen.paddingToBorder);
        //Size of the view
        int viewHeight = (int) (barBorderBottom + getPaddingBottom() + paddingToBorder);

        //Prevent bar to show before animation starts
        calculateAnimPositions();

        setMeasuredDimension(viewWidth, viewHeight);
    }

    /**
     * Calculates the size of the animated bar
     */
    private void calculateAnimPositions() {
        barPercentageWidth = barFullWidth * filledPercentageAnim;
        barPercentageRight = paddingLeft + barPercentageWidth;
        barBorderPercentageRight = barPercentageRight - (borderWidth / 2);

    }

    /**
     * Initialize the control. This code is in a separate method so that it can be
     * called from both constructors.
     */
    private void init() {
        // Set up the Background of the Chart
        barBGPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barBGPaint.setColor(Color.LTGRAY);
        barBGPaint.setStyle(Style.FILL);
        // Set up the Border of the Chart
        barBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barBorderPaint.setColor(borderColor);
        barBorderPaint.setStyle(Style.STROKE);
        barBorderPaint.setStrokeWidth(borderWidth);
        // Set up the inner part of the Chart
        barInnerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barInnerPaint.setColor(innerColor);
        barInnerPaint.setStyle(Style.FILL);
        // Set up the inner part of the Chart
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(textSize);
        Typeface font = FontManager.getInstance().getFont(FontManager.Font.ROBOTO_LIGHT);
        textPaint.setTypeface(font);
        // Set standard value text for chart heading
        sBarTitleValue = "";
        // Set Percentage for debugging
        filledPercentage = 0.0f;
        filledPercentageAnim = filledPercentage;

        // Set up Animation System
        //ValueAnimator.ofInt(0, 10).setDuration(500).start().
        fadeInAnimation = ValueAnimator.ofFloat(0, 1);
        fadeInAnimation.setDuration(animDuration);
        fadeInAnimation.setStartDelay(startDelay);

        // Set Width of bar for every frame
        fadeInAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation)
            {
                float animValue = (Float) animation.getAnimatedValue();
                setBarPercentageForAnimation(animValue * filledPercentage);
                calculateAnimPositions();
            }
        });
        // Set width to full percentage on end event
        fadeInAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                filledPercentageAnim = filledPercentage;
                calculateAnimPositions();
                invalidate();
            }
        });
    }

    /**
     * Set the percentage of the bar chart for the animation.
     *
     * @param percentage The percentage.
     */
    private void setBarPercentageForAnimation(float percentage) {
        filledPercentageAnim = percentage;
        invalidate();
    }

    /**
     * Starts the fade in animation.
     */
    public void animateBar() {
        fadeInAnimation.start();
    }

    /**
     * Set how much the bar chart is filled in percent.
     *
     * @param percentage The percentage.
     */
    public void setPercentage(float percentage) {
        filledPercentage = percentage / 100;
    }

    /**
     * Sets the title of the bar chart.
     *
     * @param sTitle The title to be set.
     */
    @SuppressWarnings("unused")
    public void setTitle(String sTitle) {
        sBarTitle = sTitle;
    }

    /**
     * Sets the numeric value next to the title of the bar chart indicating its amount.
     *
     * @param titleValue The value.
     * @param lowerUnit The unit for low values.
     * @param greaterUnit The unit for large values.
     * @param conversionFactor The ratio <code>greaterUnit / lowerUnit</code>.
     * @param accuracy The accuracy to use for the value.
     *                 This corresponds to the number of decimals if the value is represented
     *                 with respect to <code>greaterUnit</code>.
     * @see Formatter#format(float, String, String, float, int)
     */
    @SuppressWarnings("SameParameterValue")
    public void setTitleValue(float titleValue, String lowerUnit, String greaterUnit, float conversionFactor,
                              int accuracy) {
    	//Get String for heading
        sBarTitleValue = Formatter.format(titleValue, lowerUnit,
                greaterUnit, conversionFactor, accuracy);
    }
}


