package rjsv.circularview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import rjsv.circularview.utils.GeneralUtils;

public class CircleView extends View {

    // -90 offset indicates that the progress starts from 0h
    private static final int ANGLE_OFFSET = -90;
    private static final int CLICK_THRESHOLD = 5;
    private static int circlePadding = 25;
    private boolean isCircleClockwise = true;
    private boolean isRotationEnabled = true;
    /**
     * Circle View values. Current, Minimum and Maximum
     */
    private float progressCurrentValue = 0;
    private float progressMinimumValue = 0;
    private float progressMaximumValue = 100;
    /**
     * Progress Arc Configuration
     */
    private float progressWidth = 20;
    private float progressAngle = 0;
    private float progressStep = 0;
    private boolean progressStepAsInteger = false;
    private Paint progressPaint;
    /**
     * Arc Configuration
     */
    private Paint arcPaint;
    private Paint arcBorderPaint;
    private RectF arcRect = new RectF();
    private boolean arcHasBorder = false;
    private int arcWidth = 20;
    private int arcRadius = 0;
    /**
     * Indicator Configuration
     */
    private boolean hasIndicator = false;
    private boolean progressBarSquared = false;
    private int indicatorRadius = 4;
    private Paint indicatorPaint;
    /**
     * Value Text Configuration
     */
    private boolean textEnabled = true;
    private float textSize = 72;
    private int textColor;
    private int textDecimalPlaces = 1;
    private Paint textPaint;
    private Rect textRect = new Rect();
    private Typeface textTypeFace = Typeface.DEFAULT;
    /**
     * Suffix Text Configuration
     */
    private boolean suffixEnabled = false;
    private String suffixValue = "";
    private Paint suffixPaint;
    private Rect suffixRect;
    /**
     * Auxiliary Variables
     */
    private float translationOnX;
    private float translationOnY;
    private float indicationPositionX;
    private float indicationPositionY;
    private float touchStartX;
    private float touchStartY;
    // Listener
    private CircleViewChangeListener circleViewChangeListener;

    // Constructors
    public CircleView(Context context) {
        super(context);
        init(context, null);
    }

    public CircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        float density = getResources().getDisplayMetrics().density;

        // Defaults, may need to link this into theme settings
        int arcColor = getColor(context, R.color.color_arc);
        int arcBorderColor = getColor(context, R.color.color_arc_border);
        int progressColor = getColor(context, R.color.color_progress);
        textColor = getColor(context, R.color.color_text);
        int indicatorColor = getColor(context, R.color.color_indicator);

        progressWidth = (int) (progressWidth * density);
        arcWidth = (int) (arcWidth * density);
        textSize = (int) (textSize * density);

        if (attrs != null) {
            // Attribute initialization
            final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleView, 0, 0);

            progressCurrentValue = a.getFloat(R.styleable.CircleView_progressCurrentValue, progressCurrentValue);
            progressMinimumValue = a.getFloat(R.styleable.CircleView_progressMinimumValue, progressMinimumValue);
            progressMaximumValue = a.getFloat(R.styleable.CircleView_progressMaximumValue, progressMaximumValue);
            progressStep = a.getFloat(R.styleable.CircleView_progressStepValue, progressStep);
            progressStepAsInteger = a.getBoolean(R.styleable.CircleView_progressStepAsInteger, progressStepAsInteger);

            progressWidth = (int) a.getDimension(R.styleable.CircleView_progressWidth, progressWidth);
            progressColor = a.getColor(R.styleable.CircleView_progressColor, progressColor);

            arcWidth = (int) a.getDimension(R.styleable.CircleView_arcWidth, arcWidth);
            arcColor = a.getColor(R.styleable.CircleView_arcColor, arcColor);
            arcBorderColor = a.getColor(R.styleable.CircleView_arcBorderColor, arcBorderColor);
            arcHasBorder = a.getBoolean(R.styleable.CircleView_arcHasBorder, arcHasBorder);

            textSize = (int) a.getDimension(R.styleable.CircleView_textSize, textSize);
            textColor = a.getColor(R.styleable.CircleView_textColor, textColor);
            textDecimalPlaces = a.getInteger(R.styleable.CircleView_textDecimalPlaces, textDecimalPlaces);
            textEnabled = a.getBoolean(R.styleable.CircleView_textEnabled, textEnabled);
            String textTypeFacePath = a.getString(R.styleable.CircleView_textFont);
            if (textTypeFacePath != null && GeneralUtils.fileExistsInAssets(getContext(), textTypeFacePath)) {
                textTypeFace = Typeface.createFromAsset(getResources().getAssets(), textTypeFacePath);
            }

            suffixEnabled = a.getBoolean(R.styleable.CircleView_suffixEnabled, suffixEnabled);
            suffixValue = a.getString(R.styleable.CircleView_suffixValue);

            hasIndicator = a.getBoolean(R.styleable.CircleView_hasIndicator, hasIndicator);
            progressBarSquared = a.getBoolean(R.styleable.CircleView_progressBarSquared, progressBarSquared);
            indicatorRadius = a.getInt(R.styleable.CircleView_indicatorRadius, indicatorRadius);
            indicatorColor = a.getColor(R.styleable.CircleView_indicatorColor, indicatorColor);

            isCircleClockwise = a.getBoolean(R.styleable.CircleView_clockwise, isCircleClockwise);
            isRotationEnabled = a.getBoolean(R.styleable.CircleView_enabled, isRotationEnabled);
            a.recycle();
        }

        // range check
        progressCurrentValue = (progressCurrentValue > progressMaximumValue) ? progressMaximumValue : progressCurrentValue;
        progressCurrentValue = (progressCurrentValue < progressMinimumValue) ? progressMinimumValue : progressCurrentValue;

        progressAngle = progressCurrentValue / valuePerDegree(progressMaximumValue);

        arcPaint = new Paint();
        arcPaint.setColor(arcColor);
        arcPaint.setAntiAlias(true);
        arcPaint.setStrokeCap(Paint.Cap.ROUND);
        arcPaint.setStrokeJoin(Paint.Join.ROUND);
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeWidth(arcWidth);

        arcBorderPaint = new Paint();
        arcBorderPaint.setColor(arcBorderColor);
        arcBorderPaint.setAntiAlias(true);
        arcBorderPaint.setStrokeCap(Paint.Cap.ROUND);
        arcBorderPaint.setStrokeJoin(Paint.Join.ROUND);
        arcBorderPaint.setStyle(Paint.Style.STROKE);
        arcBorderPaint.setStrokeWidth((float) (arcWidth * 1.2));

        progressPaint = new Paint();
        progressPaint.setColor(progressColor);
        progressPaint.setAntiAlias(true);
        progressPaint.setStrokeCap(progressBarSquared ? Paint.Cap.SQUARE : Paint.Cap.ROUND);
        progressPaint.setStrokeJoin(Paint.Join.ROUND);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(arcHasBorder ? progressWidth : arcWidth);

        indicatorPaint = new Paint();
        indicatorPaint.setColor(indicatorColor);
        indicatorPaint.setAntiAlias(true);
        indicatorPaint.setStrokeCap(Paint.Cap.ROUND);
        indicatorPaint.setStrokeJoin(Paint.Join.ROUND);
        indicatorPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        indicatorPaint.setStrokeWidth(indicatorRadius);

        textPaint = new Paint();
        textPaint.setColor(textColor);
        textPaint.setAntiAlias(true);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(textSize);
        textPaint.setTypeface(textTypeFace);

        if (suffixEnabled) {
            suffixRect = new Rect();
            suffixPaint = new Paint();
            suffixPaint.setColor(textColor);
            suffixPaint.setAntiAlias(true);
            suffixPaint.setStyle(Paint.Style.FILL);
            suffixPaint.setTextSize(textSize / 2);
            suffixPaint.setTypeface(textTypeFace);
        }

    }

    // Overridden View Methods
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        final int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        final int min = Math.min(width, height);

        translationOnX = width * 0.5f;
        translationOnY = height * 0.5f;

        int arcDiameter = min - circlePadding - indicatorRadius;
        arcRadius = arcDiameter / 2;
        float top = height / 2 - (arcDiameter / 2);
        float left = width / 2 - (arcDiameter / 2);
        arcRect.set(left, top, left + arcDiameter, top + arcDiameter);

        updateIndicatorPosition();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        this.setBackgroundColor(Color.TRANSPARENT);
        if (!isCircleClockwise) {
            canvas.scale(-1, 1, arcRect.centerX(), arcRect.centerY());
        }
        if (textEnabled) {
            String textPoint = progressStepAsInteger ? String.valueOf((int) progressCurrentValue) : String.valueOf(progressCurrentValue);
            textPaint.getTextBounds(textPoint, 0, textPoint.length(), textRect);
            // center the text
            int xPos = canvas.getWidth() / 2 - textRect.width() / 2;
            int yPos = (int) ((arcRect.centerY()) - ((textPaint.descent() + textPaint.ascent()) / 2));
            canvas.drawText(textPoint, xPos, yPos, textPaint);
            if (suffixEnabled) {
                String suffix = suffixValue;
                suffixPaint.getTextBounds(suffix, 0, suffix.length(), suffixRect);
                xPos += textRect.width() * 1.5;
                canvas.drawText(suffix, xPos, yPos, suffixPaint);
            }
        }
        if (arcHasBorder) {
            canvas.drawArc(arcRect, ANGLE_OFFSET, 360, false, arcBorderPaint);
        }
        canvas.drawArc(arcRect, ANGLE_OFFSET + progressAngle, 360 - progressAngle, false, arcPaint);
        canvas.drawArc(arcRect, ANGLE_OFFSET, progressAngle, false, progressPaint);
        if (isRotationEnabled && hasIndicator) {
            canvas.translate(translationOnX - indicationPositionX, translationOnY - indicationPositionY);
            canvas.drawCircle(0, 0, indicatorRadius, indicatorPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isRotationEnabled) {
            this.getParent().requestDisallowInterceptTouchEvent(true);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touchStartX = event.getX();
                    touchStartY = event.getY();
                    if (circleViewChangeListener != null) {
                        circleViewChangeListener.onStartTracking(this);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    float touchAngle = convertTouchEventPointToAngle(event.getX(), event.getY());
                    updateProgress(touchAngle, true, GeneralUtils.isAClick(CLICK_THRESHOLD, touchStartX, event.getX(), touchStartY, event.getY()));
                    break;
                case MotionEvent.ACTION_UP:
                    if (progressStep > 0) {
                        applyProgressStepRestriction();
                    }
                    if (circleViewChangeListener != null) {
                        circleViewChangeListener.onStopTracking(this);
                    }
                    this.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
                case MotionEvent.ACTION_CANCEL:
                    if (circleViewChangeListener != null) {
                        circleViewChangeListener.onStopTracking(this);
                    }
                    this.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }
            return true;
        }
        return false;
    }


    // General Methods and Utils
    public float convertAngleToProgress(float angle) {
        return valuePerDegree(progressMaximumValue) * angle;
    }

    public float convertProgressToAngle(float progress) {
        return (progress / progressMaximumValue) * 360.f;
    }

    private float convertTouchEventPointToAngle(float xPos, float yPos) {
        float x = xPos - translationOnX;
        float y = yPos - translationOnY;
        x = (isCircleClockwise) ? x : -x;
        float angle = (float) Math.toDegrees(Math.atan2(y, x) + (Math.PI / 2));
        angle = (angle < 0) ? (angle + 360) : angle;
        return angle;
    }

    private float valuePerDegree(float max) {
        return max / 360.0f;
    }

    private int getColor(Context context, int id) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            return ContextCompat.getColor(context, id);
        } else {
            return context.getResources().getColor(id);
        }
    }

    private void updateIndicatorPosition() {
        float thumbAngle = progressAngle + 90;
        indicationPositionX = (float) (arcRadius * Math.cos(Math.toRadians(thumbAngle)));
        indicationPositionY = (float) (arcRadius * Math.sin(Math.toRadians(thumbAngle)));
    }

    private void updateProgress(float newValue, boolean isAngle, boolean isAClick) {
        if (isAngle) {
            if (!isAClick) {
                newValue = getValueForQuadrantCrossing(progressAngle, newValue);
            }
            progressCurrentValue = GeneralUtils.round(convertAngleToProgress(newValue), textDecimalPlaces);
            progressAngle = newValue;
        } else {
            progressCurrentValue = GeneralUtils.round(newValue, textDecimalPlaces);
            progressAngle = convertProgressToAngle(newValue);
        }
        if (circleViewChangeListener != null) {
            circleViewChangeListener.onPointsChanged(this, progressCurrentValue);
        }
        updateIndicatorPosition();
        invalidate();
    }

    private float getValueForQuadrantCrossing(float oldProgress, float newProgress) {
        float result = newProgress;
        int oldProgressQuadrant = getProgressQuadrant(oldProgress);
        int newProgressQuadrant = getProgressQuadrant(newProgress);
        if (oldProgressQuadrant == 4 && (newProgressQuadrant != 4 && newProgressQuadrant != 3)) {
            result = 360.0f;
        } else if (oldProgressQuadrant == 1 && (newProgressQuadrant != 2 && newProgressQuadrant != 1)) {
            result = 0.0f;
        }
        return result;
    }

    private int getProgressQuadrant(float progress) {
        int quadrant;
        if (progress >= 0 && progress <= 90.0f) {
            quadrant = 1;
        } else if (progress <= 180) {
            quadrant = 2;
        } else if (progress <= 270) {
            quadrant = 3;
        } else {
            quadrant = 4;
        }
        return quadrant;
    }

    private void applyProgressStepRestriction() {
        float floor = (float) Math.floor(progressCurrentValue);
        float ceiling = (float) Math.ceil(progressCurrentValue);
        float roundToNextStep;
        if (progressCurrentValue - floor <= ceiling - progressCurrentValue) {
            roundToNextStep = floor;
        } else {
            roundToNextStep = ceiling;
        }
        setProgressValue(roundToNextStep);
    }

    public float getProgressValue() {
        return progressCurrentValue;
    }

    public void setProgressValue(float progressValue) {
        if (progressValue >= progressMinimumValue) {
            if (progressValue > progressMaximumValue) {
                progressValue = progressValue % progressMaximumValue;
            }
            updateProgress(progressValue, false, false);
        }
    }

    // Setters and Getters
    public boolean isSuffixEnabled() {
        return suffixEnabled;
    }

    public void setSuffixEnabled(boolean suffixEnabled) {
        this.suffixEnabled = suffixEnabled;
    }

    public String getSuffixValue() {
        return suffixValue;
    }

    public void setSuffixValue(String suffixValue) {
        this.suffixValue = suffixValue;
    }

    public boolean isProgressStepAsInteger() {
        return progressStepAsInteger;
    }

    public void setProgressStepAsInteger(boolean progressStepAsInteger) {
        this.progressStepAsInteger = progressStepAsInteger;
    }

    public float getProgressAngle() {
        return progressAngle;
    }

    public void setProgressAngle(float progressAngle) {
        if (progressAngle >= 0) {
            if (progressAngle >= 360.0f) {
                progressAngle = progressAngle % 360.f;
            }
            updateProgress(progressAngle, true, false);
        }
    }

    public float getProgressWidth() {
        return progressWidth;
    }

    public void setProgressWidth(int progressWidth) {
        this.progressWidth = progressWidth;
        progressPaint.setStrokeWidth(progressWidth);
    }

    public int getArcWidth() {
        return arcWidth;
    }

    public void setArcWidth(int arcWidth) {
        this.arcWidth = arcWidth;
        arcPaint.setStrokeWidth(arcWidth);
    }

    public boolean isClockwise() {
        return isCircleClockwise;
    }

    public void setClockwise(boolean isClockwise) {
        isCircleClockwise = isClockwise;
    }

    public boolean isEnabled() {
        return isRotationEnabled;
    }

    public void setEnabled(boolean enabled) {
        this.isRotationEnabled = enabled;
    }

    public int getProgressColor() {
        return progressPaint.getColor();
    }

    public void setProgressColor(int color) {
        progressPaint.setColor(color);
        invalidate();
    }

    public int getArcColor() {
        return arcPaint.getColor();
    }

    public void setArcColor(int color) {
        arcPaint.setColor(color);
        invalidate();
    }

    public void setTextColor(int textColor) {
        textPaint.setColor(textColor);
        invalidate();
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
        textPaint.setTextSize(textSize);
        invalidate();
    }

    public float getMaximumValue() {
        return progressMaximumValue;
    }

    public void setMaximumValue(int progressMaximumValue) {
        if (progressMaximumValue >= progressMinimumValue) {
            this.progressMaximumValue = progressMaximumValue;
        }
    }

    public float getMinimumValue() {
        return progressMinimumValue;
    }

    public void setMinimumValue(int min) {
        if (progressMaximumValue >= min) {
            progressMinimumValue = min;
        }
    }

    public float getProgressStep() {
        return progressStep;
    }

    public void setProgressStep(int step) {
        progressStep = step;
    }

    public Typeface getTextTypeFace() {
        return textTypeFace;
    }

    public void setTextTypeFace(Typeface textTypeFace) {
        this.textTypeFace = textTypeFace;
    }

    public void setOnCircleViewChangeListener(CircleViewChangeListener onCircleViewChangeListener) {
        circleViewChangeListener = onCircleViewChangeListener;
    }

}
