package com.bendaschel.sevensegmentview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

import java.util.Arrays;
import java.util.List;

public class SevenSegmentView extends View {

    private static final Path BASE_PATH = makePathFromPoints(Arrays.asList(
            new Point(4, 1),
            new Point(5, 0),
            new Point(4, -1),
            new Point(-4, -1),
            new Point(-5, 0),
            new Point(-4, 1)
    ));
    private static final String KEY_SAVE_STATE = "saved_current_value";
    private static final DIGITS DEFAULT_STATE = DIGITS.ALL_OFF;
    public static final String KEY_SUPER_STATE = "superState";

    private enum SEGMENTS {
        TOP(0, 0, 10),
        TOP_RIGHT(90, 5, 5),
        BOTTOM_RIGHT(90, 5, -5),
        BOTTOM(0, 0, -10),
        BOTTOM_LEFT(90, -5, -5),
        TOP_LEFT(90, -5, 5),
        CENTER(0, 0, 0);

        private final Path path;

        SEGMENTS(int rotate, int translateX, int translateY) {
            Matrix matrix = new Matrix();
            // Yes, this must be -Y because the graphics coord system is upside-down
            // from a normal cartesian plane
            matrix.setTranslate(translateX, -translateY);
            matrix.preRotate(rotate);
            path = new Path();
            BASE_PATH.transform(matrix, path);
        }
    }

    private enum DIGITS {
        ZERO(ON, ON, ON, ON, ON, ON, OFF),
        ONE(OFF, ON, ON, OFF, OFF, OFF, OFF),
        TWO(ON, ON, OFF, ON, ON, OFF, ON),
        THREE(ON, ON, ON, ON, OFF, OFF, ON),
        FOUR(OFF, ON, ON, OFF, OFF, ON, ON),
        FIVE(ON, OFF, ON, ON, OFF, ON, ON),
        SIX(ON, OFF, ON, ON, ON, ON, ON),
        SEVEN(ON, ON, ON, OFF, OFF, OFF, OFF),
        EIGHT(ON, ON, ON, ON, ON, ON, ON),
        NINE(ON, ON, ON, OFF, OFF, ON, ON),
        ALL_OFF(OFF, OFF, OFF, OFF, OFF, OFF, OFF);
        boolean [] activeSegments;
        DIGITS(boolean ... activeSegments) {
            this.activeSegments = activeSegments;
        }
    }

    private static final RectF SEGMENT_BOUNDS = calculateAllSegmentBounds();
    private static final int COLOR_ON = Color.RED;
    private static final int COLOR_OFF = Color.argb(50, Color.red(Color.RED), 0, 0);
    private static final boolean ON = true;
    private static final boolean OFF = false;

    private Matrix mCurrentTransformation;
    private Path mTemporaryPath;
    private int mCurrentValue = DIGITS.ALL_OFF.ordinal();
    private Paint mPaint;
    private boolean[] mActiveSegments;

    public SevenSegmentView(Context context) {
        super(context);
        init();
    }

    public SevenSegmentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SevenSegmentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCurrentTransformation = new Matrix();
        mTemporaryPath = new Path();
        mActiveSegments = DEFAULT_STATE.activeSegments;
    }

    public void setCurrentValue(int currentValue) {
        if (currentValue >=0 && currentValue <=9) {
            mActiveSegments = DIGITS.values()[currentValue].activeSegments;
        }else{
            mActiveSegments = DEFAULT_STATE.activeSegments;
        }
        mCurrentValue = currentValue;
        invalidate();
    }

    public int getCurrentValue(){
        return mCurrentValue;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // https://stackoverflow.com/questions/12266899/onmeasure-custom-view-explanation
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        float scaleFactor = calculateScaleFactor(widthSize, heightSize,
                SEGMENT_BOUNDS.width(), SEGMENT_BOUNDS.height());
        int desiredWidth = (int)(SEGMENT_BOUNDS.width() * scaleFactor);
        int desiredHeight = (int)(SEGMENT_BOUNDS.height() * scaleFactor);
        setMeasuredDimension(desiredWidth, desiredHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mCurrentTransformation.reset();
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();
        mCurrentTransformation.setTranslate(canvasWidth / 2, canvasHeight / 2);
        // Scale properly to the aspect ratio of the canvas
        int drawHeight = canvasHeight - getPaddingBottom() - getPaddingTop();
        int drawWidth = canvasWidth - getPaddingLeft() - getPaddingRight();
        float scaleFactor = calculateScaleFactor(drawWidth, drawHeight,
                SEGMENT_BOUNDS.width(), SEGMENT_BOUNDS.height());
        mCurrentTransformation.preScale(scaleFactor, scaleFactor);

        for(SEGMENTS segment: SEGMENTS.values()) {
            segment.path.transform(mCurrentTransformation, mTemporaryPath);
            int position = segment.ordinal();
            mPaint.setColor(mActiveSegments[position] ? COLOR_ON : COLOR_OFF);
            canvas.drawPath(mTemporaryPath, mPaint);
            mTemporaryPath.reset();
        }
    }

    private float calculateScaleFactor(float containerWidth, float containerHeight,
                                       float childWidth, float childHeight) {
        float heightRatio = containerHeight / childHeight;
        float widthRatio = containerWidth / childWidth;
        return Math.min(widthRatio, heightRatio);
    }

    private static RectF calculateAllSegmentBounds( ) {
        RectF outerBounds = new RectF();
        RectF segmentBounds = new RectF();
        SEGMENTS.TOP.path.computeBounds(segmentBounds, true);
        for (SEGMENTS segment: SEGMENTS.values()) {
            segment.path.computeBounds(segmentBounds, true);
            outerBounds.union(segmentBounds);
        }
        return outerBounds;
    }

    private static Path makePathFromPoints(List<Point> points){
        Path path = new Path();
        Point firstPoint = points.get(0);
        List<Point> middlePoints = points.subList(1, points.size());
        path.moveTo(firstPoint.x, firstPoint.y);

        for(Point p: middlePoints){
            path.lineTo(p.x, p.y);
        }
        path.close();
        return path;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        // https://stackoverflow.com/questions/3542333/how-to-prevent-custom-views-from-losing-state-across-screen-orientation-changes
        // Much less of a PITA than implementing a custom parcelable to hold one value;
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_SUPER_STATE, super.onSaveInstanceState());
        bundle.putInt(KEY_SAVE_STATE, mCurrentValue);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            setCurrentValue(bundle.getInt(KEY_SAVE_STATE, DEFAULT_STATE.ordinal()));
            state = bundle.getParcelable(KEY_SUPER_STATE);
        }
        super.onRestoreInstanceState(state);
    }
}
